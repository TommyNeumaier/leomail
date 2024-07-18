import { createRouter, createWebHistory } from 'vue-router';
import MailView from "@/views/MailView.vue";
import GeplanteMails from "@/views/GeplanteMailsView.vue";
import Gruppe from "@/views/GruppeView.vue";
import Vorlagen from "@/views/TemplateView.vue";
import Settings from "@/views/SettingsView.vue";
import Login from "@/views/Login.vue";
import ProjekteView from "@/views/ProjekteView.vue";
import PersonenView from "@/views/PersonenView.vue";
import ProfilView from "@/views/ProfilView.vue";
import { validateToken } from "@/services/auth.service";
import {useAuthStore} from "@/stores/auth.store";

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'mail', component: MailView, meta: { requiresAuth: true } },
  { path: '/geplanteMails', name: 'geplant', component: GeplanteMails, meta: { requiresAuth: true } },
  { path: '/gruppen', name: 'gruppen', component: Gruppe, meta: { requiresAuth: true } },
  { path: '/vorlagen', name: 'vorlagen', component: Vorlagen, meta: { requiresAuth: true } },
  { path: '/vorlagen/create', name: 'createVorlage', component: Vorlagen, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: Settings, meta: { requiresAuth: true } },
  { path: '/projekte', name: 'projekte', component: ProjekteView, meta: { requiresAuth: true } },
  { path: '/personen', name: 'personen', component: PersonenView, meta: { requiresAuth: true } },
  { path: '/profil', name: 'profil', component: ProfilView, meta: { requiresAuth: true } },
];

const routerConfig = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

routerConfig.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    const accessToken = useAuthStore().accessToken;
    if (accessToken) {
      const isValid = await validateToken(accessToken);
      if (isValid) {
        next();
      } else {
        next({ name: 'login' });
      }
    } else {
      next({ name: 'login' });
    }
  } else {
    next();
  }
});

export default routerConfig;