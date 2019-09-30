package uk.co.gresearch.nortem.parsers.extractors;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;


public class KeyValueExtractorTest {
    private String name = "test_name";
    private String field = "test_field";
    private EnumSet<ParserExtractor.ParserExtractorFlags> extractorFlags;
    private EnumSet<KeyValueExtractor.KeyValueExtractorFlags> keyValueFlags;
    /**
     * Level=1 Category=UNKNOWN Type=abc
     **/
    @Multiline
    public static String simpleNoQuotas;

    /**
     * Threat=Evil Level='A' Category="UN  =KNOWN"
     **/
    @Multiline
    public static String simpleQuotes;

    /**
     * Threat|Evil,Level|'A',Category|"UN,|KNOWN"
     **/
    @Multiline
    public static String nonStandardDelimiters;

    /**
     * Threat|Evil,Level|'\'A',Category|"UN,|KN\"OWN"
     **/
    @Multiline
    public static String nonStandartDelimitersEscaping;

    @Before
    public void setUp() {
        extractorFlags =
                EnumSet.noneOf(ParserExtractor.ParserExtractorFlags.class);
        keyValueFlags =
                EnumSet.noneOf(KeyValueExtractor.KeyValueExtractorFlags.class);
    }

    @Test
    public void testGoodSimpleNoQuotes() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_REMOVE_FIELD);
        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertTrue(extractor.shouldRemoveField());
        Assert.assertFalse(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(simpleNoQuotas.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("1", out.get("Level"));
        Assert.assertEquals("UNKNOWN", out.get("Category"));
        Assert.assertEquals("abc", out.get("Type"));
    }

    @Test
    public void testGoodSimpleQuotesRemove() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);
        extractorFlags.add(KeyValueExtractor.ParserExtractorFlags.REMOVE_QUOTES);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("UN  =KNOWN", out.get("Category"));
        Assert.assertEquals("A", out.get("Level"));
    }

    @Test
    public void testGoodSimpleQuotesLeave() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(simpleQuotes.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN  =KNOWN\"", out.get("Category"));
        Assert.assertEquals("'A'", out.get("Level"));
    }
    @Test
    public void testGoodNonStandartsDelimiter() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(nonStandardDelimiters.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KNOWN\"", out.get("Category"));
        Assert.assertEquals("'A'", out.get("Level"));
    }

    @Test
    public void testGoodNonStandartsDelimiterEscaping() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.ESCAPING_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .escapedChar('\\')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(nonStandartDelimitersEscaping.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KN\\\"OWN\"", out.get("Category"));
        Assert.assertEquals("'\\'A'", out.get("Level"));
    }

    @Test
    public void testGoodNonStandartsDelimiterEscapingNextKey() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.ESCAPING_HANDLING);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.NEXT_KEY_STRATEGY);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .wordDelimiter(',')
                .keyValueDelimiter('|')
                .escapedChar('\\')
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());
        Assert.assertFalse(extractor.shouldRemoveField());
        Assert.assertTrue(extractor.shouldOverwiteFields());

        Map<String, Object> out = extractor.extract(nonStandartDelimitersEscaping.trim());
        Assert.assertEquals(3, out.size());
        Assert.assertEquals("Evil", out.get("Threat"));
        Assert.assertEquals("\"UN,|KN\\\"OWN\"", out.get("Category"));
        Assert.assertEquals("'\\'A'", out.get("Level"));
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongEmptyKey() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.THROWN_EXCEPTION_ON_ERROR);

        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("=abc");
        Assert.assertNull(out);
    }

    @Test
    public void testGoodEmptyValue() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("a=");
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("", out.get("a"));
    }

    @Test
    public void testGoodEmptyValueQuota() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);
        extractorFlags.add(KeyValueExtractor.ParserExtractorFlags.REMOVE_QUOTES);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Assert.assertEquals(name, extractor.getName());
        Assert.assertEquals(field, extractor.getField());

        Map<String, Object> out = extractor.extract("a=\"\"");
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("", out.get("a"));
    }

    @Test
    public void testDuplicateValueRename() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.RENAME_DUPLICATE_KEYS);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=1 a=2");
        Assert.assertEquals(2, out.size());
        Assert.assertEquals("1", out.get("a"));
        Assert.assertEquals("2", out.get("duplicate_a"));
    }

    @Test
    public void testDuplicateValueOverwrite() {
        extractorFlags.add(
                ParserExtractor.ParserExtractorFlags.SHOULD_OVERWRITE_FIELDS);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=1 a=2");
        Assert.assertEquals(1, out.size());
        Assert.assertEquals("2", out.get("a"));
    }

    @Test
    public void testWrongQuotesFaultTolerant() {
        keyValueFlags.add(KeyValueExtractor.KeyValueExtractorFlags.QUOTA_VALUE_HANDLING);

        KeyValueExtractor extractor = KeyValueExtractor.builder()
                .keyValueExtractorFlags(keyValueFlags)
                .extractorFlags(extractorFlags)
                .name(name)
                .field(field)
                .build();

        Map<String, Object> out = extractor.extract("a=\"dhdhd0ehe");
        Assert.assertEquals("\"dhdhd0ehe", out.get("a"));
    }
}