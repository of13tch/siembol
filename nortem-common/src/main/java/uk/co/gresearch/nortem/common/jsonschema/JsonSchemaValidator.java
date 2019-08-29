package uk.co.gresearch.nortem.common.jsonschema;

import uk.co.gresearch.nortem.common.result.NortemResult;

public interface JsonSchemaValidator {
    NortemResult getJsonSchema();
    NortemResult validate(String json);
}