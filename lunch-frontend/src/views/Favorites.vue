<template>
  <div class="favorites-page">
    <van-nav-bar title="⭐ 我的最爱" left-arrow @click-left="() => $router.push('/')" />

    <div class="content">
      <!-- 空状态 -->
      <van-empty v-if="favList.length === 0" description="还没有收藏菜品，去首页推荐并收藏吧！" />

      <!-- 收藏列表 -->
      <van-cell-group v-else inset>
        <van-swipe-cell v-for="food in favList" :key="food.id">
          <van-cell :title="food.name" :label="`${food.category} | ${food.source}`" center>
            <template #value>
              <span style="color: #ff6b35">🔥 {{ food.hotCount }}</span>
            </template>
          </van-cell>
          <template #right>
            <van-button
              square
              type="danger"
              text="取消收藏"
              @click="removeFav(food.id)"
              style="height: 100%"
            />
          </template>
        </van-swipe-cell>
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
import { getUserId, getFavorites, toggleFavorite } from '../utils/api.js'
import { showToast } from 'vant'

const userId = getUserId()
const favList = ref([])
const activeTab = ref(1)

onMounted(async () => {
  await loadFavorites()
})

async function loadFavorites() {
  try {
    const res = await getFavorites(userId)
    favList.value = res.data
  } catch (e) {
    console.error('获取收藏失败', e)
  }
}

async function removeFav(foodId) {
  try {
    await toggleFavorite(userId, foodId)
    showToast('已取消收藏')
    await loadFavorites()
  } catch (e) {
    showToast('操作失败')
  }
}
</script>

<style scoped>
.favorites-page {
  padding-bottom: 60px;
}

.content {
  padding: 12px 8px;
}
</style>
