package club.serenityutils.manager.api;

import java.util.Collection;
import java.util.Set;

public interface IRegistryManager<K, V> {
    void register(K key, V value);
    void unregister(K key);

    V get(K key);
    boolean contains(K key);

    Collection<V> values();
    Set<K> keys();

    void clear();
}
