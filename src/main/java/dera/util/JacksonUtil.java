package dera.util;

import dera.frontend.command.CommandResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import dera.frontend.mapping.*;
import org.apache.http.Consts;

import java.io.IOException;

public final class JacksonUtil {

    public static final Module MIX_IN_MODULE = new MixInModule();
    public static final ObjectToJSON OBJECT_TO_JSON_MAPPER = JacksonUtil.getObjectToJsonMapper();
    public static final ObjectFromJSON OBJECT_FROM_JSON_MAPPER = JacksonUtil.getObjectFromJsonMapper();
    public static final CollectionToJSON COLLECTION_TO_JSON_MAPPER = JacksonUtil.getCollectionToJsonMapper();

    private JacksonUtil() {
    }

    public static ObjectMapper createSerializer() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }

    public static ObjectMapper createDeserializer() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }


    public static ObjectToJSON getObjectToJsonMapper() {
        return new ObjectToJSON(MIX_IN_MODULE);
    }

    public static CollectionToJSON getCollectionToJsonMapper() {
        return new CollectionToJSON(MIX_IN_MODULE);
    }

    public static DeraCommandResultToJSON getDeraCommandResultToJsonMapper() {
        return new DeraCommandResultToJSON(MIX_IN_MODULE);
    }

    public static ObjectFromJSON getObjectFromJsonMapper() {
        return new ObjectFromJSON();
    }

    public static String convert(CommandResponse response){
        StringBuffer content = new StringBuffer();
        try {
            content.append(JacksonUtil.getObjectToJsonMapper().convert(response, Consts.UTF_8.name()));
        } catch (IOException e) {
        }
        return content.toString();
    }
}
