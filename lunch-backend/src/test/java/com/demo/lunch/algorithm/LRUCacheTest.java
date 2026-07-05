package com.demo.lunch.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** LRU 缓存测试 */
class LRUCacheTest {
    private LRUCache<Integer, String> cache;

    @BeforeEach
    void setUp() { cache = new LRUCache<>(3); }

    @Test
    void putAndGet_Success() {
        cache.put(1, "红烧肉");
        assertEquals("红烧肉", cache.get(1));
    }

    @Test
    void get_NotExists_ShouldReturnNull() {
        assertNull(cache.get(999));
    }

    @Test
    void exceedCapacity_ShouldEvictOldest() {
        cache.put(1, "A"); cache.put(2, "B"); cache.put(3, "C");
        cache.get(1);      // A 变为最近使用
        cache.put(4, "D"); // 淘汰 B
        assertNull(cache.get(2));
        assertEquals(3, cache.size());
    }

    @Test
    void values_ShouldReturnRecentFirst() {
        cache.put(1, "A"); cache.put(2, "B"); cache.put(3, "C");
        assertEquals(List.of("C", "B", "A"), cache.values());
    }

    @Test
    void containsKey_Success() {
        cache.put(1, "test");
        assertTrue(cache.containsKey(1));
        assertFalse(cache.containsKey(999));
    }
}
