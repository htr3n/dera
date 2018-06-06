package dera.core;

public interface Namespace {

    String NS_SEPARATOR = "::";

    String getUri();

    void setUri(String uri);

}
