<template>
  <div class="home">
    <!-- ===== 顶部标题 ===== -->
    <div class="header">
      <h1 class="title-bounce">🍱 午餐吃什么</h1>
      <p class="subtitle">今天不用愁，随机帮你选！</p>
    </div>

    <!-- ===== 分类筛选 ===== -->
    <div class="filter-section">
      <van-button
        v-for="cat in categories"
        :key="cat"
        :type="selectedCategory === cat ? 'primary' : 'default'"
        size="small"
        round
        @click="selectCategory(cat)"
        class="filter-btn"
      >
        {{ cat }}
      </van-button>
      <van-button
        :type="selectedCategory === null ? 'primary' : 'default'"
        size="small"
        round
        @click="selectCategory(null)"
        class="filter-btn"
      >
        全部
      </van-button>
    </div>

    <!-- ===== 推荐结果区域 ===== -->
    <div class="result-area">

      <!-- 滚动抽选动画 -->
      <div class="slot-machine" v-if="rolling">
        <div class="slot-window">
          <div class="slot-roller" :class="{ 'slot-stop': !rolling }">
            <span class="slot-item">{{ rollingName }}</span>
          </div>
        </div>
        <p class="rolling-hint">🎰 正在为你挑选...</p>
      </div>

      <!-- 结果卡片（带入场动画） -->
      <Transition name="card-pop" mode="out-in">
        <div class="result-card" v-if="currentFood && !rolling" :key="currentFood.id">
          <div class="card-sparkle">✨</div>
          <van-image
            round
            width="80"
            height="80"
            src="/food-placeholder.png"
            class="food-img"
          />
          <h2 class="food-name">{{ currentFood.name }}</h2>
          <van-tag type="primary" size="medium">{{ currentFood.category }}</van-tag>
          <p class="food-source">📍 {{ currentFood.source }}</p>
          <div class="hot-badge">
            <span class="hot-flame">🔥</span> 热度 {{ currentFood.hotCount }}
          </div>
        </div>
      </Transition>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!currentFood && !rolling">
        <div class="empty-animation">
          <span class="emoji-float">🍜</span>
          <span class="emoji-float delay1">🍛</span>
          <span class="emoji-float delay2">🍝</span>
          <span class="emoji-float delay3">🍖</span>
        </div>
        <van-empty description="点击下方按钮，开始随机推荐" />
      </div>
    </div>

    <!-- ===== 操作按钮 ===== -->
    <div class="action-buttons">
      <van-button
        type="primary"
        size="large"
        round
        block
        icon="shuffle"
        @click="doRecommend(false)"
        :loading="loading && !rolling"
        class="main-btn"
      >
        随机推荐
      </van-button>

      <van-button
        type="warning"
        size="large"
        round
        block
        icon="star-o"
        @click="doRecommend(true)"
        :loading="loadingFav && !rolling"
        style="margin-top: 12px"
      >
        从我的最爱推荐
      </van-button>

      <Transition name="slide-up">
        <van-button
          v-if="currentFood && !rolling"
          size="large"
          round
          block
          icon="replay"
          @click="changeOne"
          :loading="loading && !rolling"
          style="margin-top: 12px"
        >
          🔄 换一个
        </van-button>
      </Transition>

      <Transition name="slide-up">
        <van-button
          v-if="currentFood && !rolling"
          size="large"
          round
          block
          :icon="isFavorited ? 'star' : 'star-o'"
          :type="isFavorited ? 'danger' : 'default'"
          @click="toggleFav"
          class="fav-btn"
          style="margin-top: 12px"
        >
          {{ isFavorited ? '取消收藏' : '加入最爱' }}
        </van-button>
      </Transition>
    </div>

    <!-- ===== 底部导航 ===== -->
    <van-tabbar v-model="activeTab" :fixed="true">
      <van-tabbar-item icon="home-o" to="/">推荐</van-tabbar-item>
      <van-tabbar-item icon="star-o" to="/favorites">最爱</van-tabbar-item>
      <van-tabbar-item icon="fire-o" to="/hotrank">热度榜</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getUserId,
  getCategories,
  recommend,
  toggleFavorite,
} from '../utils/api.js'
import { showToast, showFailToast } from 'vant'

const userId = getUserId()
const categories = ref([])
const selectedCategory = ref(null)
const currentFood = ref(null)
const loading = ref(false)
const loadingFav = ref(false)
const activeTab = ref(0)
const isFavorited = ref(false)
const lastExcludeId = ref(null)

// 滚动抽选动画
const rolling = ref(false)
const rollingName = ref('🍱')

// 用于滚动效果的所有菜名池
const allFoodNames = [
  '🍱', '红烧肉套餐', '麻辣香锅', '兰州拉面', '鸡腿饭', '水煮鱼',
  '番茄鸡蛋面', '糖醋里脊饭', '炒河粉', '黄焖鸡米饭', '牛肉板面',
  '麻辣烫', '回锅肉盖饭', '蛋炒饭', '酸辣粉', '宫保鸡丁饭',
  '🍜', '🍛', '🍖', '🥘', '🍗', '🍝', '🥟', '🍲',
]

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (e) {
    console.error('获取分类失败', e)
  }
})

function selectCategory(cat) {
  selectedCategory.value = cat
}

// ===== 滚动抽选动画 =====
function startRolling(duration = 1000) {
  rolling.value = true
  currentFood.value = null

  const startTime = Date.now()
  let interval = 60 // 开始慢

  function tick() {
    const elapsed = Date.now() - startTime
    if (elapsed >= duration) return // 结束

    // 越到后面越慢（减速效果）
    const progress = elapsed / duration
    interval = 60 + progress * 200

    const idx = Math.floor(Math.random() * allFoodNames.length)
    rollingName.value = allFoodNames[idx]

    setTimeout(tick, interval)
  }

  tick()

  return new Promise(resolve => {
    setTimeout(() => {
      rolling.value = false
      resolve()
    }, duration)
  })
}

// 随机推荐（带滚动动画）
async function doRecommend(fromFav) {
  if (fromFav) {
    loadingFav.value = true
  } else {
    loading.value = true
  }

  // 启动滚动动画，同时发起请求
  const [_, res] = await Promise.all([
    startRolling(1200),
    recommend({
      userId,
      category: fromFav ? null : selectedCategory.value,
      fromFav,
      excludeId: null,
    }).catch(e => e), // 捕获错误不中断
  ])

  loading.value = false
  loadingFav.value = false

  if (res instanceof Error || !res?.data) {
    const msg = res?.response?.data?.error || '推荐失败，请重试'
    showFailToast(msg)
    return
  }

  currentFood.value = res.data
  lastExcludeId.value = res.data.id
  isFavorited.value = false
  showToast('🎉 推荐成功！')
}

// 换一个
async function changeOne() {
  loading.value = true

  const [_, res] = await Promise.all([
    startRolling(800), // 换一个时滚动更快
    recommend({
      userId,
      category: selectedCategory.value,
      fromFav: false,
      excludeId: lastExcludeId.value,
    }).catch(e => e),
  ])

  loading.value = false

  if (res instanceof Error || !res?.data) {
    const msg = res?.response?.data?.error || '换个口味失败'
    showFailToast(msg)
    return
  }

  currentFood.value = res.data
  lastExcludeId.value = res.data.id
  isFavorited.value = false
  showToast('🔄 已换一个！')
}

// 切换收藏（按钮弹跳反馈）
async function toggleFav() {
  try {
    const res = await toggleFavorite(userId, currentFood.value.id)
    isFavorited.value = res.data.favorited
    showToast(res.data.favorited ? '⭐ 已收藏' : '已取消收藏')
  } catch (e) {
    showFailToast('操作失败')
  }
}
</script>

<style scoped>
.home {
  padding: 20px 16px 60px;
  text-align: center;
  overflow-x: hidden;
}

/* ===== 标题 ===== */
.header {
  margin-bottom: 20px;
}

.header h1 {
  font-size: 28px;
  color: #ff6b35;
}

.title-bounce {
  animation: titleBounce 0.8s cubic-bezier(0.68, -0.55, 0.27, 1.55);
}

@keyframes titleBounce {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.1); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); opacity: 1; }
}

.subtitle {
  color: #999;
  margin-top: 6px;
  font-size: 14px;
}

/* ===== 分类按钮 ===== */
.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-bottom: 24px;
}

.filter-btn {
  transition: transform 0.2s, box-shadow 0.2s;
}

.filter-btn:active {
  transform: scale(0.92);
}

/* ===== 滚动抽选机 ===== */
.result-area {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slot-machine {
  text-align: center;
  width: 100%;
}

.slot-window {
  height: 80px;
  line-height: 80px;
  overflow: hidden;
  background: linear-gradient(135deg, #fff6e5, #fff0f0);
  border-radius: 16px;
  position: relative;
  box-shadow: inset 0 2px 8px rgba(0,0,0,0.06);
}

.slot-roller {
  animation: slotSpin 0.15s linear infinite;
}

.slot-roller.slot-stop {
  animation: slotFinish 0.3s ease-out forwards;
}

@keyframes slotSpin {
  0%   { transform: translateY(0); }
  50%  { transform: translateY(-3px); }
  100% { transform: translateY(0); }
}

@keyframes slotFinish {
  0%   { transform: translateY(-2px); opacity: 0.8; }
  100% { transform: translateY(0); opacity: 0; }
}

.slot-item {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b35;
}

.rolling-hint {
  margin-top: 12px;
  color: #ff6b35;
  font-size: 14px;
  animation: pulse 0.6s ease-in-out infinite alternate;
}

@keyframes pulse {
  from { opacity: 0.5; transform: scale(0.98); }
  to   { opacity: 1;   transform: scale(1.02); }
}

/* ===== 结果卡片 ===== */
.result-card {
  background: linear-gradient(135deg, #fff6e5 0%, #fff0f0 50%, #fff8f0 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.12);
  position: relative;
  overflow: hidden;
  width: 100%;
}

.card-sparkle {
  position: absolute;
  top: -15px;
  right: -15px;
  font-size: 40px;
  animation: sparkleFloat 2s ease-in-out infinite;
}

@keyframes sparkleFloat {
  0%, 100% { transform: rotate(0deg) scale(1); }
  25%  { transform: rotate(10deg) scale(1.2); }
  75%  { transform: rotate(-10deg) scale(0.9); }
}

.food-img {
  margin-bottom: 12px;
  animation: imgPulse 2s ease-in-out infinite;
}

@keyframes imgPulse {
  0%, 100% { transform: scale(1); }
  50%  { transform: scale(1.08); }
}

.food-name {
  font-size: 22px;
  margin: 8px 0;
  color: #333;
}

.food-source {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}

.hot-badge {
  margin-top: 8px;
  color: #ff6b35;
  font-size: 14px;
  font-weight: 600;
}

.hot-flame {
  display: inline-block;
  animation: flameFlicker 0.4s ease-in-out infinite alternate;
}

@keyframes flameFlicker {
  from { transform: scale(1) rotate(-5deg); }
  to   { transform: scale(1.15) rotate(5deg); }
}

/* ===== 卡片入场动画 ===== */
.card-pop-enter-active {
  animation: cardPopIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card-pop-leave-active {
  animation: cardPopOut 0.2s ease-in;
}

@keyframes cardPopIn {
  0%   { transform: scale(0.3) translateY(30px); opacity: 0; }
  100% { transform: scale(1) translateY(0); opacity: 1; }
}

@keyframes cardPopOut {
  0%   { transform: scale(1); opacity: 1; }
  100% { transform: scale(0.7); opacity: 0; }
}

/* ===== 按钮滑入 ===== */
.slide-up-enter-active {
  animation: slideUp 0.35s ease-out;
}

.slide-up-leave-active {
  animation: slideDown 0.2s ease-in;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes slideDown {
  from { opacity: 1; transform: translateY(0); }
  to   { opacity: 0; transform: translateY(20px); }
}

/* ===== 主按钮脉冲 ===== */
.main-btn {
  transition: transform 0.15s;
}

.main-btn:active {
  transform: scale(0.95);
}

/* ===== 收藏按钮动画 ===== */
.fav-btn {
  transition: all 0.3s;
}

/* ===== 空状态 ===== */
.empty-state {
  margin: 40px 0;
  width: 100%;
}

.empty-animation {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 10px;
}

.emoji-float {
  font-size: 28px;
  animation: floatEmoji 2.5s ease-in-out infinite;
}

.emoji-float.delay1 { animation-delay: 0.4s; }
.emoji-float.delay2 { animation-delay: 0.8s; }
.emoji-float.delay3 { animation-delay: 1.2s; }

@keyframes floatEmoji {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25%  { transform: translateY(-15px) rotate(5deg); }
  50%  { transform: translateY(-5px) rotate(-3deg); }
  75%  { transform: translateY(-20px) rotate(2deg); }
}
</style>
