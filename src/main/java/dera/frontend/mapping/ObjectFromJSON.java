package dera.frontend.mapping;

import dera.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ObjectFromJSON<T> implements ObjectFromAny<T> {

    public ObjectFromJSON() {
    }

    @Override
    public Object convert(Class clazz, InputStream inputStream, String encoding) throws IOException {
        final ObjectMapper mapper = JacksonUtil.createDeserializer();
        return mapper.readValue(inputStream, clazz);
    }
}
