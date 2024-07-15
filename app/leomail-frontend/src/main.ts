import './assets/main.css';

import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import axios from 'axios';
import App from "@/App.vue";
import router from "@/router/router";
import setupAxiosInterceptors from "@/configs/interceptor.config";

const app = createApp(App);
app.component('QuillEditor', QuillEditor)

const pinia = createPinia();
app.use(pinia);
app.use(router);

setupAxiosInterceptors(pinia);

app.mount('#app');