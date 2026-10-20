<script setup lang="ts">
const tokens = [
  ['品牌主色', '#165DFF'], ['页面背景', '#F7F8FA'], ['内容背景', '#FFFFFF'], ['主文字', '#1D2129']
];
</script>

<template>
  <main class="design-lab">
    <header><h1>Lingjuli Design Lab</h1><p>Design System V1.0 基线</p></header>
    <a-card title="基础视觉 Token" :bordered="false">
      <a-space wrap>
        <a-tag v-for="[name, value] in tokens" :key="name" color="arcoblue">{{ name }} · {{ value }}</a-tag>
      </a-space>
    </a-card>
    <a-card title="交互状态" :bordered="false">
      <a-space><a-button type="primary">主要操作</a-button><a-button>次要操作</a-button><a-button status="danger">危险操作</a-button></a-space>
    </a-card>
  </main>
</template>

<style scoped>
.design-lab { max-width: 1080px; margin: 0 auto; padding: 40px 24px; }
header { margin-bottom: 24px; } h1 { margin: 0; font-size: 28px; } p { color: #86909c; }
:deep(.arco-card) { margin-bottom: 16px; }
</style>
