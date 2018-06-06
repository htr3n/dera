package dera.frontend.mapping;

import dera.frontend.command.CommandResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DeraCommandResultToJSON extends ObjectToJSON<CommandResponse> {

    public DeraCommandResultToJSON(Module module) {
        super(module);
    }

    @Override
    public String convert(CommandResponse response, String encoding) {
        String result = null;
        final ObjectMapper mapper = createSerializer();
        if (module != null){
            mapper.registerModule(module);
        }
        try {
            result = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
        }
        return result;
    }

    private ObjectMapper createSerializer() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }

}
