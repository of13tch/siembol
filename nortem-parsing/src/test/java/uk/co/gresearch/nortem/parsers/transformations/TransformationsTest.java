package uk.co.gresearch.nortem.parsers.transformations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.co.gresearch.nortem.parsers.model.TransformationDto;

import java.io.IOException;

import java.util.Map;

public class TransformationsTest {
    private static final ObjectReader JSON_TRANSFORMATION_READER =
            new ObjectMapper().readerFor(TransformationDto.class);
    private static final ObjectReader JSON_LOG_READER = new ObjectMapper()
            .readerFor(new TypeReference<Map<String, Object>>() { });

    private Map<String, Object> log;
    private Transformation transformation;
    private final TransformationFactory factory = new TransformationFactory();

    @Before
    public void setUp() throws IOException {
        log = JSON_LOG_READER.readValue(message);
    }

    /**
     * {
     *   "transformation_type": "field_name_string_replace",
     *   "attributes": {
     *     "string_replace_target": " ",
     *     "string_replace_replacement": "_"
     *   }
     * }
     **/
    @Multiline
    public static String transformationReplace;

    /**
     * {
     *   "transformation_type": "field_name_string_replace_all",
     *   "attributes": {
     *     "string_replace_target": " ",
     *     "string_replace_replacement": "_"
     *   }
     * }
     **/
    @Multiline
    public static String transformationReplaceAll;

    /**
     *{
     *   "transformation_type": "trim_value",
     *   "attributes": {
     *     "fields_filter": {
     *      "including_fields": ["timestamp", "trim_field"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationTrim;



    /**
     *{
     *   "transformation_type": "delete_fields",
     *   "attributes": {
     *     "fields_filter": {
     *       "including_fields": [".*"],
     *       "excluding_fields": ["timestamp"]
     *     }
     *   }
     * }
     **/
    @Multiline
    public static String transformationDelete;

    /**
     *{
     *   "transformation_type": "rename_fields",
     *   "attributes": {
     *     "field_rename_map": [
     *     {
     *       "field_to_rename": "timestamp",
     *       "new_name": "timestamp_renamed"
     *     },
     *     {
     *       "field_to_rename": "dummy field",
     *       "new_name": "dummy_field_renamed"
     *     }
     *     ]
     *   }
     * }
     **/
    @Multiline
    public static String transformationRename;


    /**
     * {"timestamp":12345, "test field a" : "true", "trim_field" : "   message     ", "dummy field" : "abc"}
     **/
    @Multiline
    public static String message;

    @Test
    public void testGoodReplace() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationReplace));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("true", transformed.get("test_field a"));
        Assert.assertEquals("abc", transformed.get("dummy_field"));
    }

    @Test
    public void testGoodReplaceAll() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationReplaceAll));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("true", transformed.get("test_field_a"));
        Assert.assertEquals("abc", transformed.get("dummy_field"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadReplace() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationReplace);
        specification.getAttributes().setStringReplaceTarget(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodTrim() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationTrim));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals("message", transformed.get("trim_field"));
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadTrim() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationTrim);
        specification.getAttributes().setFieldsFilter(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodDelete() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationDelete));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(1, transformed.size());
        Assert.assertEquals(12345, transformed.get("timestamp"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadDelete() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationDelete);
        specification.getAttributes().setFieldsFilter(null);
        transformation = factory.create(specification);
    }

    @Test
    public void testGoodRename() throws IOException {
        transformation = factory.create(JSON_TRANSFORMATION_READER.readValue(transformationRename));
        Assert.assertTrue(transformation != null);

        Map<String, Object> transformed = transformation.apply(log);
        Assert.assertEquals(4, transformed.size());
        Assert.assertEquals(12345, transformed.get("timestamp_renamed"));
        Assert.assertEquals("true", transformed.get("test field a"));
        Assert.assertEquals("   message     ", transformed.get("trim_field"));
        Assert.assertEquals("abc", transformed.get("dummy_field_renamed"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadRename() throws IOException {
        TransformationDto specification = JSON_TRANSFORMATION_READER.readValue(transformationRename);
        specification.getAttributes().setFieldRenameMap(null);
        transformation = factory.create(specification);
    }
}