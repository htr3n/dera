package dera.frontend.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

public class JodaTimeSerializer extends StdSerializer<DateTime> {
    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

    protected JodaTimeSerializer() {
        super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(formatter.print(value));
    }
}
