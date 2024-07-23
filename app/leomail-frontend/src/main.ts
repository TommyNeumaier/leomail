import './assets/main.css';

import {QuillEditor} from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import piniaPersist from "pinia-plugin-persistedstate";
import App from "@/App.vue";
import router from "@/configs/router";
import setupAxiosInterceptors from "@/configs/interceptor.config";
import VueDatePicker from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css';

const app = createApp(App);
app.component('QuillEditor', QuillEditor)
app.component('VueDatePicker', VueDatePicker);

const pinia = createPinia();
app.use(pinia);
app.use(router);

pinia.use(piniaPersist);

setupAxiosInterceptors(pinia);

app.mount('#app');