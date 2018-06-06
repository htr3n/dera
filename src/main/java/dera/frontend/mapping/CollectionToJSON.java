package dera.frontend.mapping;

import dera.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

public class CollectionToJSON<T> extends ObjectToJSON<T> {

    public CollectionToJSON(Module module) {
        super(module);
    }

    @Override
    public String convert(final T t, final String encoding) throws IOException {
        final ObjectMapper mapper = JacksonUtil.createSerializer();
        if (module != null)
            mapper.registerModule(module);
        Writer stringWriter = new StringWriter();
        ObjectWriter objectWriter = mapper.writerWithType(new TypeReference<Collection<T>>() {});
        objectWriter.writeValue(stringWriter, t);
        return mapper.writeValueAsString(t);
    }

}
