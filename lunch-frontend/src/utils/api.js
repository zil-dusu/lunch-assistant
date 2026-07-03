/**
 * API 请求模块
 * 统一封装 axios 请求，管理用户身份
 */
import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// ==================== 用户身份管理 ====================

/**
 * 获取或生成用户唯一标识（UUID）
 * 存储在 localStorage 中，首次访问自动生成
 */
export function getUserId() {
  let userId = localStorage.getItem('lunch_user_id')
  if (!userId) {
    // 使用 crypto.randomUUID() 生成 UUID（现代浏览器都支持）
    userId = crypto.randomUUID()
    localStorage.setItem('lunch_user_id', userId)
  }
  return userId
}

// ==================== API 接口 ====================

/** 获取所有菜品（可按分类过滤） */
export function getFoods(category = null) {
  const params = category ? { category } : {}
  return api.get('/foods', { params })
}

/** 获取所有分类 */
export function getCategories() {
  return api.get('/categories')
}

/** 随机推荐 */
export function recommend({ userId, category, fromFav, excludeId }) {
  return api.post('/recommend', { userId, category, fromFav, excludeId })
}

/** 切换收藏状态 */
export function toggleFavorite(userId, foodId) {
  return api.post('/favorite/toggle', { userId, foodId })
}

/** 获取我的收藏 */
export function getFavorites(userId) {
  return api.get('/favorites', { params: { userId } })
}

/** 获取热度榜 */
export function getHotRank() {
  return api.get('/hotrank')
}
