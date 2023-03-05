package MainServer.ElectionCore;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.net.InetAddress;

public class ElectionStateDeserializer extends StdDeserializer<ElectionState> {
    public ElectionStateDeserializer() {
        this(null);
    }

    public ElectionStateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ElectionState deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        String serverIP = node.get("serverIP").asText();
        String leaderIP = node.get("leaderIP").asText();
        boolean isRunning = node.get("isRunning").isBoolean();

        return new ElectionState(InetAddress.getByName(leaderIP), InetAddress.getByName(serverIP),isRunning);
    }

}
