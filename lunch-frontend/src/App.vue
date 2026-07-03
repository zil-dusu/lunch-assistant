<template>
  <div id="app-container">
    <router-view />
  </div>
</template>

<script setup>
// App根组件
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f7f8fa;
  color: #323233;
}

#app-container {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  background: #fff;
}
</style>
