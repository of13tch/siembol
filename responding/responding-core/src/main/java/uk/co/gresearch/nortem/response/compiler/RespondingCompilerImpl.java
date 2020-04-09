package uk.co.gresearch.nortem.response.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import uk.co.gresearch.nortem.common.jsonschema.JsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.NortemJsonSchemaValidator;
import uk.co.gresearch.nortem.common.jsonschema.UnionJsonType;
import uk.co.gresearch.nortem.common.jsonschema.UnionJsonTypeOption;
import uk.co.gresearch.nortem.common.result.NortemResult;
import uk.co.gresearch.nortem.common.testing.TestingLogger;
import uk.co.gresearch.nortem.response.common.*;
import uk.co.gresearch.nortem.response.engine.ResponseEngine;
import uk.co.gresearch.nortem.response.engine.ResponseRule;
import uk.co.gresearch.nortem.response.engine.RulesEngine;
import uk.co.gresearch.nortem.response.model.ResponseEvaluatorDto;
import uk.co.gresearch.nortem.response.model.RuleDto;
import uk.co.gresearch.nortem.response.model.RulesDto;

import java.util.*;
import java.util.stream.Collectors;

import static uk.co.gresearch.nortem.response.common.RespondingResult.StatusCode.OK;

public class RespondingCompilerImpl implements RespondingCompiler {
    private static final String EVALUATOR_TITLE = "response evaluator";
    private static final ObjectReader RULES_READER = new ObjectMapper().readerFor(RulesDto.class);
    private static final String RULES_WRAP_MSG = "{\"rules_version\":1, \"rules\":[%s]}";
    private static final String UNSUPPORTED_EVALUATOR_TYPE_MSG = "Unsupported response evaluator type %s";
    private final Map<String, RespondingEvaluatorFactory> respondingEvaluatorFactoriesMap;
    private final Map<String, RespondingEvaluatorValidator> respondingEvaluatorValidatorsMap;
    private final String rulesJsonSchemaStr;
    private final JsonSchemaValidator rulesSchemaValidator;
    private final MetricFactory metricFactory;

    public RespondingCompilerImpl(Builder builder) {
        this.respondingEvaluatorFactoriesMap = builder.respondingEvaluatorFactoriesMap;
        this.respondingEvaluatorValidatorsMap = builder.respondingEvaluatorValidatorsMap;
        this.rulesJsonSchemaStr = builder.rulesJsonSchemaStr;
        this.rulesSchemaValidator = builder.rulesSchemaValidator;
        this.metricFactory = builder.metricFactory;
    }

    private ResponseRule createResponseRule(RuleDto ruleDto, TestingLogger logger) {
        ResponseRule.Builder builder = new ResponseRule.Builder();
        builder
                .metricFactory(metricFactory)
                .logger(logger)
                .ruleName(ruleDto.getRuleName())
                .ruleVersion(ruleDto.getRuleVersion());

        for (ResponseEvaluatorDto evaluatorDto : ruleDto.getEvaluators()) {
            String evaluatorType = evaluatorDto.getEvaluatorType();
            if (!respondingEvaluatorFactoriesMap.containsKey(evaluatorType)) {
                throw new IllegalArgumentException(String.format(
                        UNSUPPORTED_EVALUATOR_TYPE_MSG, evaluatorType));
            }
            RespondingResult evaluatorResult = respondingEvaluatorFactoriesMap.get(evaluatorType)
                    .createInstance(evaluatorDto.getEvaluatorAttributesContent());
            if (evaluatorResult.getStatusCode() != OK) {
                throw new IllegalArgumentException(evaluatorResult.getAttributes().getMessage());
            }
            builder.addEvaluator(evaluatorResult.getAttributes().getRespondingEvaluator());
        }
        return builder.build();
    }

    @Override
    public RespondingResult compile(String rules, TestingLogger logger) {
        RespondingResult validationResult = validateConfigurations(rules);
        if (validationResult.getStatusCode() != OK) {
            return validationResult;
        }

        try {
            RulesDto rulesDto = RULES_READER.readValue(rules);
            List<ResponseRule> responseRules = rulesDto.getRules().stream()
                    .map(x -> createResponseRule(x, logger))
                    .collect(Collectors.toList());

            ResponseEngine responseEngine = new RulesEngine.Builder()
                    .rules(responseRules)
                    .metricFactory(metricFactory)
                    .testingLogger(logger)
                    .build();

            RespondingResultAttributes attr = new RespondingResultAttributes();
            attr.setResponseEngine(responseEngine);
            return new RespondingResult(OK, attr);
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getSchema() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRulesSchema(rulesJsonSchemaStr);
        return new RespondingResult(OK, attributes);
    }

    @Override
    public RespondingResult validateConfiguration(String rule) {
        try {
            return validateConfigurations(wrapRuleToRules(rule));
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
    }

    @Override
    public RespondingResult getRespondingEvaluatorFactories() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorFactories(respondingEvaluatorFactoriesMap.values().stream()
                .collect(Collectors.toList()));
        return new RespondingResult(OK, attributes);
    }

    @Override
    public RespondingResult getRespondingEvaluatorValidators() {
        RespondingResultAttributes attributes = new RespondingResultAttributes();
        attributes.setRespondingEvaluatorValidators(respondingEvaluatorValidatorsMap.values().stream()
                .collect(Collectors.toList()));
        return new RespondingResult(OK, attributes);
    }

    private String wrapRuleToRules(String ruleStr) {
        return String.format(RULES_WRAP_MSG, ruleStr);
    }

    @Override
    public RespondingResult validateConfigurations(String rules) {
        NortemResult validationResult = rulesSchemaValidator.validate(rules);
        if (validationResult.getStatusCode() != NortemResult.StatusCode.OK) {
            return RespondingResult.fromNortemResult(validationResult);
        }

        try {
            RulesDto rulesDto = RULES_READER.readValue(rules);
            for (RuleDto ruleDto : rulesDto.getRules()) {
                for (ResponseEvaluatorDto evaluatorDto : ruleDto.getEvaluators()) {
                    String evaluatorType = evaluatorDto.getEvaluatorType();
                    if (!respondingEvaluatorValidatorsMap.containsKey(evaluatorType)) {
                        throw new IllegalArgumentException(String.format(
                                UNSUPPORTED_EVALUATOR_TYPE_MSG, evaluatorType));
                    }
                    RespondingResult validationAttributesResult = respondingEvaluatorValidatorsMap.get(evaluatorType)
                            .validateAttributes(evaluatorDto.getEvaluatorAttributesContent());
                    if (validationAttributesResult.getStatusCode() != OK) {
                        return validationAttributesResult;
                    }
                }
            }
        } catch (Exception e) {
            return RespondingResult.fromException(e);
        }
        return new RespondingResult(OK, new RespondingResultAttributes());
    }

    public static class Builder {
        private static final String EVALUATOR_DUPLICATE_TYPE = "Evaluator type: %s already registered";
        private static final String EMPTY_EVALUATORS = "Response evaluators are empty";
        private Map<String, RespondingEvaluatorFactory> respondingEvaluatorFactoriesMap = new HashMap<>();
        private Map<String, RespondingEvaluatorValidator> respondingEvaluatorValidatorsMap = new HashMap<>();
        private String rulesJsonSchemaStr;
        private JsonSchemaValidator rulesSchemaValidator;
        private MetricFactory metricFactory;

        public Builder metricFactory(MetricFactory metricFactory) {
            this.metricFactory = metricFactory;
            return this;
        }

        public Builder addRespondingEvaluatorFactories(List<RespondingEvaluatorFactory> factories) {
            factories.forEach(this::addRespondingEvaluatorFactory);
            return this;
        }

        public Builder addRespondingEvaluatorFactory(RespondingEvaluatorFactory factory) {
            if (respondingEvaluatorFactoriesMap.containsKey(factory.getType().getAttributes().getEvaluatorType())) {
                throw new IllegalArgumentException(String.format(EVALUATOR_DUPLICATE_TYPE, factory.getType()));
            }

            respondingEvaluatorFactoriesMap.put(factory.getType().getAttributes().getEvaluatorType(), factory);
            return this;
        }

        public Builder addRespondingEvaluatorValidator(RespondingEvaluatorValidator validator) {
            if (respondingEvaluatorValidatorsMap.containsKey(validator.getType().getAttributes().getEvaluatorType())) {
                throw new IllegalArgumentException(String.format(EVALUATOR_DUPLICATE_TYPE, validator.getType()));
            }

            respondingEvaluatorValidatorsMap.put(validator.getType().getAttributes().getEvaluatorType(), validator);
            return this;
        }

        public RespondingCompilerImpl build() throws Exception {
            if (respondingEvaluatorFactoriesMap.isEmpty() && respondingEvaluatorValidatorsMap.isEmpty()) {
                throw new IllegalArgumentException(EMPTY_EVALUATORS);
            }

            respondingEvaluatorFactoriesMap.forEach((k, v) -> addRespondingEvaluatorValidator(v));

            List<UnionJsonTypeOption> evaluatorOptions = respondingEvaluatorValidatorsMap.keySet().stream()
                    .map(x ->
                            new UnionJsonTypeOption(
                                    respondingEvaluatorValidatorsMap.get(x).getType()
                                            .getAttributes().getEvaluatorType(),
                                    respondingEvaluatorValidatorsMap.get(x).getAttributesJsonSchema()
                                            .getAttributes().getAttributesSchema()))
                    .collect(Collectors.toList());

            UnionJsonType options = new UnionJsonType(EVALUATOR_TITLE, evaluatorOptions);
            rulesSchemaValidator = new NortemJsonSchemaValidator(RulesDto.class, Optional.of(Arrays.asList(options)));
            rulesJsonSchemaStr = rulesSchemaValidator.getJsonSchema().getAttributes().getJsonSchema();
            return new RespondingCompilerImpl(this);
        }
    }
}
