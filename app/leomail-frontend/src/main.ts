import './assets/main-css/main.css';

import {QuillEditor} from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {createApp} from 'vue';
import {createPinia} from 'pinia';
import piniaPersist from "pinia-plugin-persistedstate";
import App from "@/App.vue";
import routerConfig from "@/configs/router.config";
import setupAxiosInterceptors from "@/configs/interceptor.config";
import VueDatePicker from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css';
import PrimeVue from 'primevue/config';
import Aura from '@primevue/themes/aura';
import Tooltip from 'primevue/tooltip';



const app = createApp(App);
app.component('QuillEditor', QuillEditor)
app.component('VueDatePicker', VueDatePicker);
app.directive('tooltip', Tooltip);


export const pinia = createPinia();
app.use(pinia);
app.use(routerConfig);
app.use(PrimeVue, {
    theme: {
        preset: Aura
    }
})

pinia.use(piniaPersist);

app.mount('#app');