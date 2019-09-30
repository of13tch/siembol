package uk.co.gresearch.nortem.parsers.application.parsing;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.common.utils.TimeProvider;
import uk.co.gresearch.nortem.parsers.common.ParserFields;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.common.SerializableNortemParser;

import java.util.*;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SingleApplicationParserTest {
    /**
     * {
     *
     *     "a": "string",
     *     "b": 1,
     *     "c": true
     * }
     **/
    @Multiline
    public static String metadata;

    private SerializableNortemParser nortemParser;
    private String sourceType = "test_type";
    private SingleApplicationParser appParser;
    private Map<String, Object> message1;
    private Map<String, Object> message2;
    private List<Map<String, Object>> parsed;
    private String errorTopic = "error";
    private String outputTopic = "output";
    private byte[] input = "test".getBytes();
    private ParserResult parserResult;
    TimeProvider timeProvider;
    long currentTime = 1L;


    @Before
    public void setUp() {
        timeProvider = Mockito.mock(TimeProvider.class);
        when(timeProvider.getCurrentTimeInMs()).thenReturn(currentTime);
        nortemParser = Mockito.mock(SerializableNortemParser.class);
        when(nortemParser.getSourceType()).thenReturn(sourceType);

        message1 = new HashMap<>();
        message1.put("test_field", "a");
        message1.put("timestamp", 1);

        message2 = new HashMap<>();
        message2.put("test_field", "b");
        message2.put("timestamp", 2);

        parsed = new ArrayList<>();
        parsed.add(message1);
        parsed.add(message2);
        parserResult = new ParserResult();
        parserResult.setParsedMessages(parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingARguments() {
        appParser = SingleApplicationParser.builder()
                .errorTopic(errorTopic)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingARguments2() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(null)
                .build();
    }

    @Test
    public void testParseTwoMessages() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(nortemParser.parseToResult(input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(ParserFields.PARSING_TIME.toString() + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                ParserFields.SENSOR_TYPE.toString() + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(ParserFields.PARSING_TIME.toString() + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                ParserFields.SENSOR_TYPE.toString() + "\":\"test_type\""));
    }

    @Test
    public void testParseOneMessages() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.getParsedMessages().remove(1);
        when(nortemParser.parseToResult(input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(ParserFields.PARSING_TIME.toString() + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                ParserFields.SENSOR_TYPE.toString() + "\":\"test_type\""));
    }

    @Test
    public void testExceptionParsing() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(null);
        parserResult.setException(new IllegalStateException("test_exception"));
        when(nortemParser.parseToResult(input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_exception"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"test_type\""));
    }

    @Test
    public void testRuntimeExceptionParsing() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test-app")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        when(nortemParser.parseToResult(input)).thenThrow(new RuntimeException("runtime_exception"));
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getMessages().size());

        Assert.assertEquals(errorTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("runtime_exception"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("\"failed_sensor_type\":\"test-app\""));
    }

    @Test
    public void testParsingFiltered() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .build();

        parserResult.setParsedMessages(new ArrayList<>());
        when(nortemParser.parseToResult(input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testParseTwoMessagesMetadata() throws Exception {
        appParser = SingleApplicationParser.builder()
                .parser(outputTopic, nortemParser)
                .name("test")
                .errorTopic(errorTopic)
                .timeProvider(timeProvider)
                .parseMetadata(true)
                .metadataPrefix("test_metadata")
                .build();

        when(nortemParser.parseToResult(input)).thenReturn(parserResult);
        List<ParsingApplicationResult> result = appParser.parse(input, metadata);
        verify(timeProvider, times(1)).getCurrentTimeInMs();
        verify(nortemParser, times(1)).parseToResult(input);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(0).getMessages().size());
        Assert.assertEquals(outputTopic, result.get(0).getTopic());
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(ParserFields.PARSING_TIME.toString() + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_field" + "\":\"a"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains("timestamp" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(0).contains(
                ParserFields.SENSOR_TYPE.toString() + "\":\"test_type\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(ParserFields.PARSING_TIME.toString() + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_field" + "\":\"b"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("timestamp" + "\":2"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:a" + "\":\"string\""));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:b" + "\":1"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains("test_metadata:c" + "\":true"));
        Assert.assertTrue(result.get(0).getMessages().get(1).contains(
                ParserFields.SENSOR_TYPE.toString() + "\":\"test_type\""));
    }
}