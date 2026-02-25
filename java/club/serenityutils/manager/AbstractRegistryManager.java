package club.serenityutils.manager;

import club.serenityutils.manager.api.IRegistryManager;

import java.util.*;

public abstract class AbstractRegistryManager<K, V> implements IRegistryManager<K, V> {
    protected final Map<K, V> map = new HashMap<>();

    @Override
    public void register(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void unregister(K key) {
        map.remove(key);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public Set<K> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public void clear() {
        map.clear();
    }
}
