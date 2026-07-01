<template>
  <div id="app-container">
    <router-view />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  background-color: #f0f2f5;
  min-height: 100vh;
}
#app-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
</style>
