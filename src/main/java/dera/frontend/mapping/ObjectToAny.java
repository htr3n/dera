package dera.frontend.mapping;

public interface ObjectToAny<T> {

    String convert(T t, String encoding) throws Throwable;

}
