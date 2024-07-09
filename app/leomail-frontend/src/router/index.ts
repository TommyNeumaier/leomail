import { createRouter, createWebHistory } from 'vue-router'
import MailView from "@/views/MailView.vue";
import GeplanteMails from "@/views/GeplanteMailsView.vue";
import Gruppe from "@/views/GruppeView.vue";
import Vorlagen from "@/views/VorlagenView.vue";
import Settings from "@/views/SettingsView.vue";
import Login from "@/views/Login.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/',
      name: 'mail',
      component: MailView
    },
    {
      path: '/geplanteMails',
      name: 'geplant',
      component: GeplanteMails
    },
    {
      path: '/gruppen',
      name: 'gruppen',
      component: Gruppe
    },
    {
      path: '/vorlagen',
      name: 'vorlagen',
      component: Vorlagen
    },
    {
      path: '/settings',
      name: 'settings',
      component: Settings
    }
  ]
})

export default router
