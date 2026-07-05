# 🍱 午餐决策助手

一个帮助同学解决「今天中午吃什么」的随机推荐系统，支持分类筛选、收藏管理、热度排行。

**演示地址**: http://120.26.222.56

---

## 功能模块

| 模块 | 功能 |
|------|------|
| 菜品浏览 | 菜品列表展示、按分类（米饭/面食/小吃）筛选、按辣度（辣/不辣）筛选 |
| 随机推荐 | 普通随机推荐、从收藏中推荐、🔄换一个（LRU缓存防重复）、滚动抽选动画 |
| 收藏管理 | 一键收藏/取消收藏、收藏列表、左滑删除 |
| 热度榜 | 按推荐次数排名 Top 5 |
| 算法演示 | 归并排序 O(n log n)、小顶堆 Top-K O(N log K) |

---

## 技术栈

| 层 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Vant 4 + Vue Router 4 + Axios |
| 后端 | Spring Boot 3.2.0 + Spring Data JPA + Hibernate |
| 数据库 | MySQL 8（生产） / H2（开发/测试） |
| Web 服务器 | Nginx 1.18 |
| 测试 | JUnit 5 + Mockito + MockMvc + @DataJpaTest |
| 部署 | 阿里云 ECS Ubuntu 22.04 |
| 版本控制 | Git + GitHub（3 人分支协作） |

---

## 项目结构

```
lunch-assistant/
├── lunch-backend/                        # Java 后端
│   ├── pom.xml                           # Maven 依赖配置
│   └── src/
│       ├── main/java/com/demo/lunch/
│       │   ├── LunchApplication.java     # 启动类
│       │   ├── entity/                   # 实体类
│       │   │   ├── Food.java             # 菜品（id/name/category/spicy/source/hotCount）
│       │   │   ├── Favorite.java         # 收藏
│       │   │   └── FavoriteId.java       # 联合主键
│       │   ├── repository/              # 数据访问层
│       │   │   ├── FoodRepository.java
│       │   │   └── FavoriteRepository.java
│       │   ├── service/
│       │   │   └── RecommendService.java # 核心业务逻辑
│       │   ├── controller/
│       │   │   └── LunchController.java  # REST API（9 个接口）
│       │   ├── algorithm/               # 手写算法
│       │   │   ├── LRUCache.java         # LRU 缓存（HashMap+双向链表）
│       │   │   ├── SortAlgorithms.java   # 归并排序等
│       │   │   └── TopKHeap.java         # 小顶堆 Top-K
│       │   └── config/
│       │       └── CorsConfig.java       # 跨域配置
│       ├── main/resources/
│       │   ├── application.yml           # 开发配置（H2）
│       │   └── data.sql                  # 种子数据
│       └── test/                         # 测试（37 个用例）
│
├── lunch-frontend/                       # Vue3 前端
│   ├── vite.config.js                    # Vite 配置 + 开发代理
│   ├── index.html
│   └── src/
│       ├── main.js                       # 入口
│       ├── App.vue                       # 根组件
│       ├── router/index.js              # 路由配置
│       ├── utils/api.js                 # API 封装 + UUID 用户识别
│       └── views/
│           ├── Home.vue                 # 首页（推荐+筛选+动画）
│           ├── Favorites.vue            # 收藏页
│           └── HotRank.vue             # 热度榜
│
├── deploy/                              # 部署配置文件
│   ├── application-prod.yml             # 生产环境配置
│   ├── lunch-backend.service            # systemd 服务
│   └── nginx.conf                       # Nginx 配置
│
└── README.md
```

---

## 快速开始（本地开发）

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8（可选，开发用内置 H2）

### 启动后端

```bash
cd lunch-backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
# H2 控制台: http://localhost:8080/h2-console
```

### 启动前端

```bash
cd lunch-frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
# 自动代理 /api 到 localhost:8080
```

### 运行测试

```bash
cd lunch-backend
mvn test
# 37 个测试用例，覆盖算法/数据层/业务层/接口层
```

---

## API 接口文档

### 菜品相关

| 方法 | 地址 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/foods` | 菜品列表 | `category`(可选), `spicy`(可选) |
| GET | `/api/categories` | 分类选项 | 无 |
| GET | `/api/spicies` | 辣度选项 | 无 |

### 推荐相关

| 方法 | 地址 | 说明 | 参数 |
|------|------|------|------|
| POST | `/api/recommend` | 随机推荐 | `{userId, category?, spicy?, fromFav?, excludeId?}` |

### 收藏相关

| 方法 | 地址 | 说明 | 参数 |
|------|------|------|------|
| POST | `/api/favorite/toggle` | 切换收藏 | `{userId, foodId}` |
| GET | `/api/favorites` | 收藏列表 | `userId` |

### 热度榜

| 方法 | 地址 | 说明 |
|------|------|------|
| GET | `/api/hotrank` | Top 5 热度榜 |

### 算法演示

| 方法 | 地址 | 说明 | 参数 |
|------|------|------|------|
| GET | `/api/algorithm/sort` | 归并排序 | `by`(字段), `desc`(降序) |
| GET | `/api/algorithm/topk` | Top-K 堆 | `k`(数量), `by`(字段) |

---

## 部署

### 服务器信息

| 项 | 详情 |
|----|------|
| 云服务 | 阿里云 ECS |
| 系统 | Ubuntu 22.04 |
| 配置 | 2 核 2G |
| IP | 120.26.222.56 |

### 部署架构

```
浏览器 → Nginx :80 → /            → 前端静态文件
                    → /api/*       → Spring Boot :8080 → MySQL :3306
```

### 运维命令

```bash
# 后端
systemctl start/stop/restart lunch-backend
tail -f /opt/lunch/app.log

# Nginx
systemctl reload nginx

# 数据库
mysql -u lunchuser -pLunch@123456 lunchdb
```

详细说明见 [部署及运维说明.md](部署及运维说明.md)

---

## 测试

| 测试类 | 数量 | 层级 |
|--------|------|------|
| LRUCacheTest | 5 | 算法层 |
| SortAlgorithmsTest | 6 | 算法层 |
| TopKHeapTest | 3 | 算法层 |
| FoodRepositoryTest | 4 | 数据层 |
| RecommendServiceTest | 9 | 业务层 |
| LunchControllerTest | 10 | 接口层 |
| **合计** | **37** | **全部通过 ✅** |

---

## 团队成员

| 成员 | 负责模块 | 分支 |
|------|---------|------|
| XXX | 数据层（实体、数据库、种子数据） | feature/database-layer |
| XXX | 业务层（API、推荐逻辑、排序算法） | feature/business-api |
| XXX | 前端（页面、API对接、动画效果） | feature/frontend |

---

## 数据说明

- 菜品数量：21 道
- 分类：米饭 🍚（10道）/ 面食 🍜（2道）/ 小吃 🍢（9道）
- 辣度：辣 🌶️ / 不辣 🥬
- 校园送：支持 🛵 / 不支持
- 用户识别：浏览器 localStorage UUID，无需注册登录

---

## 许可证

本项目仅用于课程答辩和学习目的。
