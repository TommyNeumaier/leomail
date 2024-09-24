import { createRouter, createWebHistory } from 'vue-router';
import MailView from "@/views/MailView.vue";
import GeplanteMails from "@/views/ScheduledMailsView.vue";
import Gruppe from "@/views/GroupView.vue";
import Vorlagen from "@/views/TemplateView.vue";
import Settings from "@/views/SettingsView.vue";
import Login from "@/views/Login.vue";
import ProjekteView from "@/views/ProjectView.vue";
import PersonenView from "@/views/ContactView.vue";
import ProfilView from "@/views/ProfilView.vue";
import { useAuthStore } from "@/stores/auth.store";
import NewProject from "@/views/NewProject.vue";
import AuthTest from "@/views/AuthTest.vue";
import NewMail from "@/views/NewMail.vue";
import { useAppStore } from '@/stores/app.store';
import axios from 'axios';
import {refreshToken, validateToken} from "@/services/auth.service";
import {loadConfigFromFile} from "vite";
import ProjectView from "@/views/ProjectView.vue";
import ScheduledMailsView from "@/views/ScheduledMailsView.vue";
import GroupView from "@/views/GroupView.vue";
import TemplateView from "@/views/TemplateView.vue";
import SettingsView from "@/views/SettingsView.vue";

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/mail', name: 'mail', component: MailView, meta: { requiresAuth: true } },
  { path: '/mail/new', name: 'newMail', component: NewMail, meta: { requiresAuth: true } },
  { path: '/scheduledMails', name: 'scheduled', component: ScheduledMailsView, meta: { requiresAuth: true } },
  { path: '/groups', name: 'groups', component: GroupView, meta: { requiresAuth: true } },
  { path: '/template', name: 'template', component: TemplateView, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: SettingsView, meta: { requiresAuth: true } },
  { path: '/', name: 'projects', component: ProjectView, meta: { requiresAuth: true } },
  { path: '/projects/new', name: 'newProjects', component: NewProject, meta: { requiresAuth: true } },
  { path: '/contacts', name: 'contacts', component: PersonenView, meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: ProfilView, meta: { requiresAuth: true } },
  { path: '/authtest', name: 'authtest', component: AuthTest, meta: { requiresAuth: true } }
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
      const isValid = await validateToken(accessToken);
      if (isValid) {
        return proceedWithAuthorization(to, next);
      } else {
        console.log('Access token is invalid, attempting to refresh');
        try {
          const { access_token } = await refreshToken();
          axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
          return proceedWithAuthorization(to, next);
        } catch (refreshError) {
          console.log('Failed to refresh token, redirecting to login');
          return handleInvalidToken(authStore, next);
        }
      }
    } catch (error) {
      console.error('Error during token validation or refresh:', error);
      return handleInvalidToken(authStore, next);
    }
  } else {
    return proceedWithAuthorization(to, next);
  }
});

function handleInvalidToken(authStore, next) {
  authStore.logout();
  return next({ name: 'login' });
}

function proceedWithAuthorization(to, next) {
  const exceptions = ["/", "/projects/new", "/login", "/people", "/authtest", "/settings", "/profile"];
  const appStore = useAppStore();

  if (appStore.project === '' && !exceptions.includes(to.path)) {
    return next({ name: "projects" });
  }

  // TODO: Check if user has permission for this project, if not, then return him to the project view
  /*
  let hasPermission = Service.getInstance().checkPermission(appStore.project).then((value: boolean) => {
    return value;
  });

  if (!hasPermission) {
    return next({ name: "projekte" });
  }
  */
  return next();
}

export default router;