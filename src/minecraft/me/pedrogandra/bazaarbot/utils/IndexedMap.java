package me.pedrogandra.bazaarbot.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexedMap<K, V> {
    private final Map<K, V> map = new LinkedHashMap<>();
    private final List<K> indexList = new ArrayList<>();

    public void put(K key, V value) {
        if (!map.containsKey(key)) {
            indexList.add(key);
        }
        map.put(key, value);
    }

    public V getByKey(K key) {
        return map.get(key);
    }

    public V getByIndex(int index) {
        if (index < 0 || index >= indexList.size()) return null;
        K key = indexList.get(index);
        return map.get(key);
    }
    
    public int getIndexOf(K key) {
        return indexList.indexOf(key);
    }
    
    public K getKeyByIndex(int index) {
        return indexList.get(index);
    }

    public void remove(K key) {
        if (map.containsKey(key)) {
            map.remove(key);
            indexList.remove(key);
        }
    }

    public void clear() {
        map.clear();
        indexList.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public List<K> keyList() {
        return new ArrayList<>(indexList);
    }

    public List<V> valueList() {
        List<V> values = new ArrayList<>();
        for (K key : indexList) {
            values.add(map.get(key));
        }
        return values;
    }

    public List<Map.Entry<K,V>> entryListInOrder() {
        List<Map.Entry<K,V>> entries = new ArrayList<>();
        for (K key : indexList) {
            entries.add(new AbstractMap.SimpleEntry<>(key, map.get(key)));
        }
        return entries;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
    
    public void sort(Comparator<V> comparator) {
        indexList.sort((k1, k2) -> {
            V v1 = map.get(k1);
            V v2 = map.get(k2);
            return comparator.compare(v1, v2);
        });
    }
}
