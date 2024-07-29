import {createRouter, createWebHistory} from 'vue-router';
import MailView from "@/views/MailView.vue";
import GeplanteMails from "@/views/GeplanteMailsView.vue";
import Gruppe from "@/views/GruppeView.vue";
import Vorlagen from "@/views/TemplateView.vue";
import Settings from "@/views/SettingsView.vue";
import Login from "@/views/Login.vue";
import ProjekteView from "@/views/ProjekteView.vue";
import PersonenView from "@/views/ContactView.vue";
import ProfilView from "@/views/ProfilView.vue";
import {refreshToken, validateToken} from "@/services/auth.service";
import {useAuthStore} from "@/stores/auth.store";
import NewProject from "@/views/NewProject.vue";
import AuthTest from "@/views/AuthTest.vue";
import NewMail from "@/views/NewMail.vue";
import {useAppStore} from '@/stores/app.store';

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'mail', component: MailView, meta: { requiresAuth: true } },
  { path: '/mail/neu', name: 'neu', component: NewMail, meta: { requiresAuth: true } },
  { path: '/geplanteMails', name: 'geplant', component: GeplanteMails, meta: { requiresAuth: true } },
  { path: '/gruppen', name: 'gruppen', component: Gruppe, meta: { requiresAuth: true } },
  { path: '/vorlagen', name: 'vorlagen', component: Vorlagen, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: Settings, meta: { requiresAuth: true } },
  { path: '/projekte', name: 'projekte', component: ProjekteView, meta: { requiresAuth: true } },
  { path: '/projekte/neu', name: 'neueProjekte', component: NewProject, meta: { requiresAuth: true } },
  { path: '/personen', name: 'personen', component: PersonenView, meta: { requiresAuth: true } },
  { path: '/profil', name: 'profil', component: ProfilView, meta: { requiresAuth: true } },
  { path: '/authtest', name: 'authtest', component: AuthTest, meta: { requiresAuth: true}}
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  let authOk = false;

  if (to.meta.requiresAuth) {
    const accessToken = authStore.$state.accessToken;

    if (!accessToken) {
      console.log('No access token found, redirecting to login');
      return next({ name: 'login' });
    }

    try {
      const isValid = validateToken(accessToken).then((value: boolean) => {
        return value;
      });

      if (isValid) {
        authOk = true;
      } else {
        console.log('Access token is invalid, attempting to refresh');
        const newToken = await refreshToken();
        if (newToken) {
          const newIsValid = await validateToken(newToken.access_token);
          if (newIsValid) {
            console.log('New access token is valid, proceeding to', to.name);
            authOk = true;
          }
        }
        console.log('New access token is invalid, redirecting to login');
        authStore.logout();
        return next({ name: 'login' });
      }
    } catch (error) {
      console.error('Error during token validation or refresh:', error);
      authStore.logout();
      return next({ name: 'login' });
    }
  } else {
    authOk = true;
  }

  if (authOk) {
    const exceptions = ["/projekte", "/projekte/neu", "/login", "/personen", "/authtest", "/settings", "/profil"];

    const appStore = useAppStore();
    if (appStore.$state.project === '' && !exceptions.includes(to.path)) {
      return next({name: "projekte"});
    }

    // TODO: Check if user has permission for this project, if not, then return him to the project view
    /*
        let hasPermission = Service.getInstance().checkPermission(appStore.$state.project).then((value: boolean) => {
          return value;
        });

        if(!hasPermission){
          return next({ name: "projekte"});
        }*/
    return next();
  }
});

export default router;