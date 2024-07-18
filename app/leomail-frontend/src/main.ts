import './assets/main.css';

import {QuillEditor} from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import axios from 'axios';
import App from "@/App.vue";
import routerConfig from "@/configs/router.config";
import setupAxiosInterceptors from "@/configs/interceptor.config";
import VueDatePicker from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css';

const app = createApp(App);
app.component('QuillEditor', QuillEditor)
app.component('VueDatePicker', VueDatePicker);

const pinia = createPinia();
app.use(pinia);
app.use(routerConfig);

setupAxiosInterceptors(pinia);

app.mount('#app');