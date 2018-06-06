package dera.data;

public interface DataAccess<T> {

    void put(String key, T value);

    T get(String key);

    void update(String key, T value);

    T delete(String key);

}
