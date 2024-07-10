import './assets/main.css'

import {createApp, defineComponent, onMounted, ref} from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Service from "@/stores/service";

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

