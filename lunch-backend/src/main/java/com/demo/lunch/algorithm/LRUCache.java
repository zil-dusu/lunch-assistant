package com.demo.lunch.algorithm;

import java.util.*;

/**
 * 基于 HashMap + 双向链表 实现的 LRU (Least Recently Used) 缓存。
 *
 * 数据结构原理：
 * - HashMap<Key, Node>：O(1) 查找
 * - 双向链表：维护访问顺序，最近访问的移到链表头，淘汰时移除链表尾
 *
 * 用途：缓存用户最近推荐过的菜品，实现"换一个"防重复推荐。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LRUCache<K, V> {

    // ---- 双向链表节点 ----
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev, next;
        Node(K key, V value) { this.key = key; this.value = value; }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map;   // HashMap: O(1) 定位节点
    private final Node<K, V> head;           // 哨兵头节点
    private final Node<K, V> tail;           // 哨兵尾节点

    public LRUCache(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.map = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    /** O(1) — 获取值，并将该节点移到链表头部（标记为最近使用） */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) return null;
        moveToHead(node);
        return node.value;
    }

    /** O(1) — 放入键值对，容量超限时淘汰最近最少使用的 */
    public void put(K key, V value) {
        Node<K, V> node = map.get(key);
        if (node != null) {
            node.value = value;
            moveToHead(node);
            return;
        }
        node = new Node<>(key, value);
        map.put(key, node);
        addToHead(node);

        if (map.size() > capacity) {
            Node<K, V> removed = removeTail();
            map.remove(removed.key);
        }
    }

    /** 检查是否存在某 key，O(1) */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /** 返回缓存中所有值（按访问顺序，最近→最旧） */
    public List<V> values() {
        List<V> result = new ArrayList<>();
        Node<K, V> curr = head.next;
        while (curr != tail) {
            result.add(curr.value);
            curr = curr.next;
        }
        return result;
    }

    public int size() { return map.size(); }

    // ===== 双向链表操作 =====

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private Node<K, V> removeTail() {
        Node<K, V> node = tail.prev;
        removeNode(node);
        return node;
    }
}
