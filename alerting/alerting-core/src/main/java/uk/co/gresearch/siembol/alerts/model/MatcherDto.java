package uk.co.gresearch.siembol.alerts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;

import java.util.List;

@Attributes(title = "matcher", description = "Matcher for matching fields")
public class MatcherDto {
    @JsonProperty("matcher_type")
    @Attributes(required = true, description = "Type of matcher, either Regex match or list of strings " +
            "(newline delimited) or a composite matcher composing several matchers",
            enums = {"REGEX_MATCH", "IS_IN_SET"})
    private MatcherTypeDto type;

    @JsonProperty("is_negated")
    @Attributes(description = "The matcher is negated")
    private Boolean negated = false;

    @Attributes(required = true, description = "Field on which the matcher will be evaluated")
    private String field;

    @JsonProperty("case_insensitive")
    @Attributes(description = "Use case insensitive string compare")
    private Boolean caseInsensitiveCompare = false;

    @Attributes(description = "Matcher expression as defined by matcher type")
    private String data;

    @SchemaIgnore
    @Attributes(description = "List of matchers of the composite matcher")
    private List<MatcherDto> matchers;

    public MatcherTypeDto getType() {
        return type;
    }

    public void setType(MatcherTypeDto type) {
        this.type = type;
    }

    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getCaseInsensitiveCompare() {
        return caseInsensitiveCompare;
    }

    public void setCaseInsensitiveCompare(Boolean caseInsensitiveCompare) {
        this.caseInsensitiveCompare = caseInsensitiveCompare;
    }

    public List<MatcherDto> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<MatcherDto> matchers) {
        this.matchers = matchers;
    }
}

