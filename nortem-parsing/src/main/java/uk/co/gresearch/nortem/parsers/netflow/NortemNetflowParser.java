package uk.co.gresearch.nortem.parsers.netflow;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.gresearch.nortem.parsers.common.NortemParser;
import uk.co.gresearch.nortem.parsers.common.ParserFields;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static uk.co.gresearch.nortem.parsers.common.ParserFields.ORIGINAL;
import static uk.co.gresearch.nortem.parsers.common.ParserFields.TIMESTAMP;


public class NortemNetflowParser implements NortemParser {
    private static final Logger LOG = LoggerFactory
            .getLogger(MethodHandles.lookup().lookupClass());
    public static final String NETFLOW_SOURCE_ID = "netflow_source_id";
    public static final String NETFLOW_GLOBAL_SOURCE = "netflow_source";
    public static final String NETFLOW_UNKNOWN_TEMPLATE = "netflow_unknown_template";

    private final NetflowParser netflowParser;

    private Map<String, Object> getUnknownTemplateObject(NetflowParsingResult parsingResult, byte[] bytes) {
        Map<String, Object> ret = new HashMap<>();

        ret.put(TIMESTAMP.getName(), System.currentTimeMillis());
        ret.put(NETFLOW_GLOBAL_SOURCE, parsingResult.getGlobalSource());
        ret.put(NETFLOW_SOURCE_ID, parsingResult.getSourceId());
        ret.put(ORIGINAL.getName(), Base64.getEncoder().encodeToString(bytes));
        ret.put(NETFLOW_UNKNOWN_TEMPLATE, true);

        return ret;
    }

    public NortemNetflowParser() {
        final NetflowTransportProvider<String> provider = new SimpleTransportProvider();
        netflowParser = new NetflowParser<>(provider);
    }

    @Override
    public List<Map<String, Object>> parse(byte[] bytes) {
        ArrayList<Map<String, Object>> ret = new ArrayList<>();
        try {
            NetflowParsingResult result = netflowParser.parse(bytes);
            if (result.getStatusCode() == NetflowParsingResult.StatusCode.UNKNOWN_TEMPLATE) {
                ret.add(getUnknownTemplateObject(result, bytes));
                return ret;
            }

            if (result.getStatusCode() != NetflowParsingResult.StatusCode.OK) {
                throw new IllegalStateException(result.getStatusCode().toString());
            }

            if (result.getDataFlowSet() != null) {
                ret.ensureCapacity(result.getDataFlowSet().size());
                for (List<Pair<String, Object>> dataFlowset : result.getDataFlowSet()) {
                    Map<String, Object> current = new HashMap<>();

                    current.put(ORIGINAL.toString(), result.getOriginalString());
                    current.put(TIMESTAMP.toString(), result.getTimestamp());
                    current.put(NETFLOW_SOURCE_ID, result.getSourceId());
                    current.put(NETFLOW_GLOBAL_SOURCE, result.getGlobalSource());

                    dataFlowset.forEach(item -> current.put(item.getKey(), item.getValue()));
                    ret.add(current);
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to parse message: %s",
                    Base64.getEncoder().encodeToString(bytes));
            LOG.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }

        return ret;
    }
}