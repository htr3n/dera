package dera.frontend.mapping;

import java.io.InputStream;

public interface ObjectFromAny<T> {
    T convert(Class<T> clazz, InputStream inputStream, String encoding) throws Throwable;
}
