import { createRouter, createWebHistory } from 'vue-router'
import MailView from "@/views/MailView.vue";
import GeplanteMails from "@/views/GeplanteMails.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'mail',
      component: MailView
    },
    {
      path: '/geplanteMails',
      name: 'geplant',
      component: GeplanteMails
    }
  ]
})

export default router
