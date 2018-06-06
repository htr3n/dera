package dera.frontend.mapping;

import dera.util.JacksonUtil;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ObjectToJSON<T> implements ObjectToAny<T> {

    protected final Module module;

    public ObjectToJSON(final Module module) {
        this.module = module;
    }

    @Override
    public String convert(final T t, final String encoding) throws IOException {
        String result = null;
        final ObjectMapper mapper = JacksonUtil.createSerializer();
        if (module != null) {
            mapper.registerModule(module);
        }
        return mapper.writeValueAsString(t);
    }

}
