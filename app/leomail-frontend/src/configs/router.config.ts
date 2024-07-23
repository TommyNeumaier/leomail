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
import { refreshToken, validateToken } from "@/services/auth.service";
import { useAuthStore } from "@/stores/auth.store";

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'mail', component: MailView, meta: { requiresAuth: true } },
  { path: '/geplanteMails', name: 'geplant', component: GeplanteMails, meta: { requiresAuth: true } },
  { path: '/gruppen', name: 'gruppen', component: Gruppe, meta: { requiresAuth: true } },
  { path: '/vorlagen', name: 'vorlagen', component: Vorlagen, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: Settings, meta: { requiresAuth: true } },
  { path: '/projekte', name: 'projekte', component: ProjekteView, meta: { requiresAuth: true } },
  { path: '/personen', name: 'personen', component: PersonenView, meta: { requiresAuth: true } },
  { path: '/profil', name: 'profil', component: ProfilView, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth) {
    const accessToken = authStore.accessToken;

    if (!accessToken) {
      console.log('No access token found, redirecting to login');
      return next({ name: 'login' });
    }

    try {
      const isValid = await validateToken(accessToken).then((value: boolean) => {
        return !value;
      });

      if (isValid) {
        console.log('Access token is valid, proceeding to', to.name);
        return next();
      } else {
        console.log('Access token is invalid, attempting to refresh');
        const newToken = await refreshToken();
        if (newToken) {
          const newIsValid = await validateToken(newToken);
          if (newIsValid) {
            console.log('New access token is valid, proceeding to', to.name);
            return next();
          }
        }
        console.log('New access token is invalid, redirecting to login');
        return next({ name: 'login' });
      }
    } catch (error) {
      console.error('Error during token validation or refresh:', error);
      return next({ name: 'login' });
    }
  } else {
    return next();
  }
});

export default router;