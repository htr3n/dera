package dera.extension;

import dera.core.Action;
import dera.core.Event;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger extends Action {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogger.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public EventLogger() {
        super("EventLogger");
    }

    @Override
    public void notified(final Event incoming) {
        LOG.info("{} :: {}", new Object[]{DATE_TIME_FORMATTER.print(new DateTime()), incoming});
    }

    @Override
    public void addOutput(String... events) {
        throw new UnsupportedOperationException();
    }

}
