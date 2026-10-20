<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { IconBarChart, IconCalendar, IconFile, IconStorage } from '@arco-design/web-vue/es/icon';

const apiBaseUrl = import.meta.env.VITE_IAM_BASE_URL ?? 'http://localhost:8081';
const tokenKey = 'lingjuli.platform.token';
const form = reactive({ loginName: '', password: '', remember: true });
const loading = ref(false);
const restoring = ref(true);
const errorMessage = ref('');
const user = ref<{ displayName: string; mustChangePassword?: boolean } | null>(null);
const passwordChangeRequired = ref(false);
const passwordChangeLoading = ref(false);
const passwordForm = reactive({ newPassword: '', confirmPassword: '' });

async function request(path: string, options: RequestInit = {}) {
  const token = localStorage.getItem(tokenKey) ?? sessionStorage.getItem(tokenKey);
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { satoken: token } : {}), ...options.headers },
  });
  const body = await response.json();
  if (!response.ok || !body.success) throw new Error(body.message || '请求失败，请稍后重试');
  return body.data;
}

async function login() {
  errorMessage.value = '';
  loading.value = true;
  try {
    const result = await request('/api/iam/auth/platform/login', { method: 'POST', body: JSON.stringify(form) });
    const storage = form.remember ? localStorage : sessionStorage;
    storage.setItem(tokenKey, result.token);
    user.value = result.user;
    passwordChangeRequired.value = result.mustChangePassword;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试';
  } finally {
    loading.value = false;
  }
}

async function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    errorMessage.value = '两次输入的新密码不一致';
    return;
  }
  passwordChangeLoading.value = true;
  errorMessage.value = '';
  try {
    await request('/api/iam/auth/password', {
      method: 'POST',
      body: JSON.stringify({ newPassword: passwordForm.newPassword }),
    });
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    passwordChangeRequired.value = false;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '修改密码失败，请稍后重试';
  } finally {
    passwordChangeLoading.value = false;
  }
}

async function logout() {
  try { await request('/api/iam/auth/logout', { method: 'POST' }); } finally {
    localStorage.removeItem(tokenKey);
    sessionStorage.removeItem(tokenKey);
    user.value = null;
  }
}

onMounted(async () => {
  if (!localStorage.getItem(tokenKey) && !sessionStorage.getItem(tokenKey)) {
    restoring.value = false;
    return;
  }
  try {
    const currentUser = await request('/api/iam/auth/platform/session');
    user.value = currentUser;
    passwordChangeRequired.value = currentUser.mustChangePassword === true;
  } catch {
    localStorage.removeItem(tokenKey);
    sessionStorage.removeItem(tokenKey);
  } finally { restoring.value = false; }
});
</script>

<template>
  <main class="login-page">
    <section class="brand-panel">
      <div class="brand-header"><div class="brand-mark">同</div><div><h1>同庆云</h1><p>餐饮经营 SaaS 平台</p></div></div>
      <div class="brand-content">
        <div class="brand-copy"><strong>让经营决策<br />更聪明</strong><span>统一管理租户、套餐与订阅运营。</span></div>
        <div class="operation-scene" aria-label="智能餐饮经营能力展示">
          <div class="scene-glow"></div>
          <div class="feature-card analysis-card"><div class="feature-icon"><icon-bar-chart /></div><div><b>经营分析</b><span>实时洞察</span></div><div class="mini-chart"><i></i><i></i><i></i><i></i><i></i></div></div>
          <div class="feature-card schedule-card"><div class="feature-icon"><icon-calendar /></div><div><b>智能排班</b><span>人效协同</span></div></div>
          <div class="feature-card purchase-card"><div class="feature-icon"><icon-file /></div><div><b>智能申购</b><span>精准补货</span></div></div>
          <div class="feature-card inventory-card"><div class="feature-icon"><icon-storage /></div><div><b>库存盘点</b><span>实时可控</span></div></div>
          <div class="scene-board"><div class="board-top"><span>今日经营概览</span><em></em></div><div class="board-metrics"><div><b>86%</b><span>门店健康度</span></div><div><b>12</b><span>待处理事项</span></div></div><div class="board-line"><i></i><i></i><i></i><i></i><i></i><i></i></div></div>
        </div>
      </div>
      <p class="brand-footer">© 2026 同庆云 · 餐饮经营 SaaS 平台</p>
    </section>
    <section class="login-panel"><a-spin :loading="restoring" class="login-spin">
      <div v-if="user" class="signed-in"><h2>欢迎回来，{{ user.displayName }}</h2><p>您已登录同庆云平台运营中心。</p><a-button type="primary" long @click="logout">退出登录</a-button></div>
      <div v-else class="login-card">
        <h2>欢迎登录同庆云</h2><p class="hint">平台运营中心</p>
        <a-alert v-if="errorMessage" type="error" :show-icon="true" :closable="true" class="error" @close="errorMessage = ''">{{ errorMessage }}</a-alert>
        <a-form :model="form" layout="vertical" @submit-success="login">
          <a-form-item field="loginName" label="账号" :rules="[{ required: true, message: '请输入账号' }]"><a-input v-model="form.loginName" placeholder="请输入平台运营账号" allow-clear /></a-form-item>
          <a-form-item field="password" label="密码" :rules="[{ required: true, message: '请输入密码' }]"><a-input-password v-model="form.password" placeholder="请输入密码" /></a-form-item>
          <a-checkbox v-model="form.remember">保持登录状态</a-checkbox><a-button html-type="submit" type="primary" long :loading="loading" class="submit">登录</a-button>
        </a-form><p class="notice">账号由 IAM 管理员开通。如无法登录，请联系平台管理员。</p>
      </div>
    </a-spin></section>
    <a-modal
      :visible="passwordChangeRequired"
      title="请修改初始密码"
      :closable="false"
      :mask-closable="false"
      :esc-to-close="false"
      :hide-cancel="true"
      :ok-loading="passwordChangeLoading"
      ok-text="确认修改"
      @ok="changePassword"
    >
      <p class="password-change-hint">为保障账号安全，请先修改初始密码后再继续使用。</p>
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="新密码" required>
          <a-input-password v-model="passwordForm.newPassword" placeholder="请输入 8 至 128 位新密码" />
        </a-form-item>
        <a-form-item label="确认新密码" required>
          <a-input-password v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.login-page{min-height:100vh;display:grid;grid-template-columns:minmax(600px,55%) minmax(480px,45%);background:var(--lj-color-surface)}.brand-panel{position:relative;overflow:hidden;min-height:760px;padding:52px clamp(40px,7vw,132px);color:#fff;background:var(--lj-color-primary)}.brand-panel::before,.brand-panel::after{content:"";position:absolute;border:1px solid rgba(255,255,255,.18);border-radius:50%;pointer-events:none}.brand-panel::before{width:760px;height:760px;right:-380px;top:-390px}.brand-panel::after{width:560px;height:560px;left:-350px;bottom:-350px;box-shadow:0 0 0 74px rgba(255,255,255,.045)}.brand-header{position:relative;z-index:1;display:flex;align-items:center;gap:12px}.brand-mark{width:38px;height:38px;display:grid;place-items:center;border-radius:11px;background:var(--lj-color-surface);color:var(--lj-color-primary);font-size:19px;font-weight:700}.brand-panel h1{margin:0;font-size:19px;letter-spacing:.04em}.brand-panel p{margin:3px 0 0;color:rgba(255,255,255,.72);font-size:12px}.brand-content{position:relative;z-index:1;max-width:590px;margin:clamp(66px,12vh,132px) auto 0}.brand-copy{display:grid;gap:14px}.brand-copy strong{font-size:clamp(36px,3.2vw,54px);line-height:1.2;letter-spacing:.02em}.brand-copy span{color:rgba(255,255,255,.76);font-size:16px}.operation-scene{position:relative;height:310px;margin-top:46px}.scene-glow{position:absolute;inset:52px 18% 0;border-radius:50%;background:rgba(255,255,255,.1);filter:blur(34px)}.scene-board{position:absolute;z-index:2;left:50%;bottom:0;width:270px;height:166px;padding:20px;transform:translateX(-50%);border:1px solid rgba(255,255,255,.52);border-radius:16px;background:rgba(255,255,255,.17);box-shadow:0 16px 34px rgba(12,56,153,.18);backdrop-filter:blur(8px)}.board-top{display:flex;justify-content:space-between;font-size:13px}.board-top em{width:28px;height:6px;border-radius:99px;background:rgba(255,255,255,.56)}.board-metrics{display:flex;gap:36px;margin-top:18px}.board-metrics div{display:grid;gap:3px}.board-metrics b{font-size:21px}.board-metrics span{font-size:11px;color:rgba(255,255,255,.7)}.board-line{display:flex;align-items:flex-end;gap:6px;height:38px;margin-top:12px}.board-line i{width:14%;border-radius:3px 3px 0 0;background:rgba(255,255,255,.78)}.board-line i:nth-child(1){height:32%}.board-line i:nth-child(2){height:48%}.board-line i:nth-child(3){height:40%}.board-line i:nth-child(4){height:72%}.board-line i:nth-child(5){height:61%}.board-line i:nth-child(6){height:92%}.feature-card{position:absolute;z-index:3;display:flex;align-items:center;gap:9px;padding:11px 13px;border:1px solid rgba(255,255,255,.42);border-radius:12px;background:rgba(19,96,241,.7);box-shadow:0 10px 24px rgba(12,56,153,.14);backdrop-filter:blur(8px)}.feature-card b,.feature-card span{display:block}.feature-card b{font-size:13px;line-height:1.45}.feature-card span{font-size:10px;color:rgba(255,255,255,.72)}.feature-icon{display:grid;place-items:center;width:28px;height:28px;border-radius:8px;background:rgba(255,255,255,.16);font-size:16px}.analysis-card{left:3%;top:28px}.schedule-card{right:3%;top:56px}.purchase-card{left:10%;bottom:58px}.inventory-card{right:10%;bottom:76px}.mini-chart{display:flex;align-items:flex-end;gap:2px;height:25px;margin-left:5px}.mini-chart i{width:3px;border-radius:2px;background:#fff}.mini-chart i:nth-child(1){height:30%}.mini-chart i:nth-child(2){height:70%}.mini-chart i:nth-child(3){height:45%}.mini-chart i:nth-child(4){height:92%}.mini-chart i:nth-child(5){height:62%}.brand-footer{position:absolute;z-index:1;bottom:28px;left:clamp(40px,7vw,132px);font-size:11px!important;color:rgba(255,255,255,.55)!important}.login-panel{display:grid;place-items:center;padding:48px clamp(48px,8vw,150px);background:var(--lj-color-surface)}.login-spin{width:min(100%,390px)}.login-card,.signed-in{width:100%}.login-card h2,.signed-in h2{margin:0 0 8px;font-size:28px;line-height:1.35;letter-spacing:.01em}.hint,.signed-in p{margin:0 0 32px;color:#86909c;font-size:15px}.error{margin-bottom:16px}.login-card :deep(.arco-form-item){margin-bottom:20px}.login-card :deep(.arco-form-item-label-col){margin-bottom:7px;color:#4e5969;font-weight:500}.login-card :deep(.arco-input-wrapper){height:42px;border:1px solid var(--lj-color-border);border-radius:8px;background:var(--lj-color-surface)}.login-card :deep(.arco-input-wrapper:hover),.login-card :deep(.arco-input-wrapper-focus){border-color:var(--lj-color-primary);box-shadow:none}.login-card :deep(.arco-checkbox){font-size:13px;color:#4e5969}.submit{height:42px;margin-top:22px;border-radius:8px;font-weight:500}.notice{margin:23px 0 0;color:#86909c;font-size:12px;line-height:1.7}.password-change-hint{margin:0 0 20px;color:#4e5969}@media(max-width:960px){.login-page{grid-template-columns:minmax(430px,1fr) minmax(400px,1fr)}.brand-panel{padding-inline:42px}.feature-card{transform:scale(.9)}.login-panel{padding-inline:52px}}@media(max-width:720px){.login-page{display:block}.brand-panel{min-height:248px;padding:30px 28px}.brand-content{margin:38px 0 0}.brand-copy strong{font-size:30px}.brand-copy span,.operation-scene,.brand-footer{display:none}.login-panel{min-height:calc(100vh - 248px);padding:48px 24px}.login-spin{max-width:390px}}
.notice{overflow-wrap:anywhere}
@media(max-width:720px){.login-panel,.login-card,.signed-in,.login-spin{min-width:0;max-width:100%;overflow:hidden}}
</style>
