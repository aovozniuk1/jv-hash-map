package core.basesyntax;

import static core.basesyntax.MyHashMap.Node.hash;

import java.util.Map;
import java.util.Objects;

public class MyHashMap<K, V> implements MyMap<K, V> {
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_CAPACITY = 16;
    private Node<K, V>[] table;
    private int size;
    private int newCapacity;
    private int threshold;
    private boolean isResize;

    public MyHashMap() {
        table = new Node[DEFAULT_CAPACITY];
        threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
        newCapacity = DEFAULT_CAPACITY * 2;
        isResize = false;
    }

    @Override
    public void put(K key, V value) {
        int index;
        if (size > threshold) {
            resize();
            isResize = true;
        }
        if (isResize) {
            index = hash(key, newCapacity / 2);
        } else {
            index = hash(key, DEFAULT_CAPACITY);
        }
        Node<K, V> newNode = new Node<>(key, value, null);
        Node<K, V> node = table[index];
        if (node == null) {
            table[index] = newNode;
            size++;
        } else {
            while (node.next != null || checkKey(key, node)) {
                if (checkKey(key, node)) {
                    node.value = value;
                    return;
                }
                node = node.next;
            }
            node.next = newNode;
            size++;
        }
    }

    @Override
    public V getValue(K key) {
        int index;
        if (isResize) {
            index = hash(key, newCapacity / 2);
        } else {
            index = hash(key, DEFAULT_CAPACITY);
        }
        Node<K, V> node = table[index];
        if (node == null) {
            return null;
        }
        while (node != null) {
            if (checkKey(key, node)) {
                return node.value;
            }
            node = node.next;
        }
        return null;
    }

    @Override
    public int getSize() {
        return size;
    }

    private Node<K, V> transfer(Node<K, V> node, Node<K, V>[] newTable) {
        Node<K, V> tempNode = null;
        if (node.next != null) {
            tempNode = node.next;
        }
        node.next = null;
        int index = hash(node.key, newCapacity);
        if (newTable[index] == null) {
            newTable[index] = node;
        } else {
            Node<K, V> nextNode = newTable[index].next;
            Node<K, V> lastNode = newTable[index];
            while (nextNode != null) {
                if (nextNode.next == null) {
                    lastNode = nextNode;
                    break;
                }
                nextNode = nextNode.next;
            }
            lastNode.next = node;
        }
        return tempNode;
    }

    private void resize() {
        Node<K, V>[] newTable = new Node[newCapacity];
        Node<K, V> nextNode = null;
        for (Node<K, V> node : table) {
            if (node == null) {
                continue;
            }
            nextNode = transfer(node, newTable);

            while (nextNode != null) {
                nextNode = transfer(nextNode, newTable);
            }
        }
        table = newTable;
        newCapacity *= 2;
        threshold = (int) (newCapacity * DEFAULT_LOAD_FACTOR);
    }

    private boolean checkKey(K key, Node<K, V> node) {
        if (node.key == key || node.key != null && node.key.equals(key)) {
            return true;
        }
        return false;
    }

    static class Node<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;
        private Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public static int hash(Object key, int capacity) {
            return Math.abs((key == null) ? 0 : key.hashCode() % capacity);
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof Map.Entry<?, ?> e
                    && Objects.equals(key, e.getKey())
                    && Objects.equals(value, e.getValue());
        }

    }
}
