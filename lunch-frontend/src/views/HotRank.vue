<template>
  <div class="hotrank-page">
    <van-nav-bar title="🔥 今日热度榜" left-arrow @click-left="() => $router.push('/')" />

    <div class="content">
      <van-empty v-if="rankList.length === 0" description="暂无数据，快去推荐菜品吧！" />

      <!-- 排行榜列表 -->
      <van-cell-group v-else inset>
        <van-cell
          v-for="(food, index) in rankList"
          :key="food.id"
          center
        >
          <template #title>
            <span class="rank-badge" :class="'rank-' + (index + 1)">
              {{ index + 1 }}
            </span>
            <span class="food-name">{{ food.name }}</span>
          </template>
          <template #label>
            {{ food.category }} | {{ food.spicy === '辣' ? '🌶️' : '🥬' }}{{ food.spicy }} | 📍 {{ food.source }}
          </template>
          <template #value>
            <span class="hot-count">🔥 {{ food.hotCount }}</span>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" :fixed="true">
      <van-tabbar-item icon="home-o" to="/">推荐</van-tabbar-item>
      <van-tabbar-item icon="star-o" to="/favorites">最爱</van-tabbar-item>
      <van-tabbar-item icon="fire-o" to="/hotrank">热度榜</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHotRank } from '../utils/api.js'

const rankList = ref([])
const activeTab = ref(2)

onMounted(async () => {
  try {
    const res = await getHotRank()
    rankList.value = res.data
  } catch (e) {
    console.error('获取热度榜失败', e)
  }
})
</script>

<style scoped>
.hotrank-page {
  padding-bottom: 60px;
}

.content {
  padding: 12px 8px;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 6px;
  color: #fff;
  font-size: 13px;
  font-weight: bold;
  margin-right: 8px;
  vertical-align: middle;
}

.rank-1 {
  background: linear-gradient(135deg, #ff6b35, #ff4500);
  font-size: 15px;
}

.rank-2 {
  background: linear-gradient(135deg, #ff8c00, #ffa500);
}

.rank-3 {
  background: linear-gradient(135deg, #daa520, #b8860b);
}

.rank-4,
.rank-5 {
  background: #999;
}

.food-name {
  vertical-align: middle;
  font-weight: 500;
}

.hot-count {
  color: #ff6b35;
  font-weight: bold;
  font-size: 15px;
}
</style>
