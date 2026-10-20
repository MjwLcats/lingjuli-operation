import { createApp } from 'vue';
import { Button, Card, Space, Tag } from '@arco-design/web-vue';
import '@arco-design/web-vue/dist/arco.css';
import '@lingjuli/design-tokens/styles.css';
import App from './App.vue';

createApp(App)
  .component('AButton', Button)
  .component('ACard', Card)
  .component('ASpace', Space)
  .component('ATag', Tag)
  .mount('#app');

