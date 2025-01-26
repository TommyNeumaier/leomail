import { createRouter, createWebHistory } from 'vue-router';
import MailView from "@/views/MailView.vue";
import Login from "@/views/Login.vue";
import PersonenView from "@/views/ContactView.vue";
import ProfilView from "@/views/ProfilView.vue";
import { useAuthStore } from "@/stores/auth.store";
import NewProject from "@/views/NewProject.vue";
import NewMail from "@/views/NewMail.vue";
import { refreshToken, validateToken } from "@/services/auth.service";
import ProjectView from "@/views/ProjectView.vue";
import ScheduledMailsView from "@/views/ScheduledMailsView.vue";
import GroupView from "@/views/GroupView.vue";
import TemplateView from "@/views/TemplateView.vue";
import SettingsView from "@/views/SettingsView.vue";
import AuthorisationComponent from "@/views/AuthorisationView.vue";
import PostLoginView from "@/views/PostLoginView.vue";
import {pinia} from "@/main";
import axiosInstance from "@/axiosInstance";
import {useImportStatusStore} from "@/stores/import-status.store";

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/post-login', name: 'post-login', component: PostLoginView, meta: { requiresAuth: true } },
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
  { path: '/authorisation', name: 'authorisation', component: AuthorisationComponent, meta: { requiresAuth: true } },
  {
    path: '/mail/:id/:projectId',
    name: 'MailDetail',
    component: () => import('@/views/MailDetailView.vue'),
    props: true,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

function handleInvalidToken(authStore, next) {
  authStore.logout();
  return next({ name: 'login' });
}

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore(pinia);
  const importStatusStore = useImportStatusStore();

  if (to.meta.requiresAuth) {
    const accessToken = authStore.accessToken;

    if (!accessToken) {
      return next({ name: 'login' });
    }

    try {
      const isValid = await validateToken();
      if (isValid) {
        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

        await importStatusStore.updateImportStatus();

        if (importStatusStore.importing && to.name !== 'post-login') {
          return next({ name: 'post-login' });
        }

        return next();
      } else {
        try {
          const { access_token } = await refreshToken();
          authStore.setTokens(access_token, authStore.$state._refreshToken);
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;

          await importStatusStore.updateImportStatus();

          if (importStatusStore.importing && to.name !== 'post-login') {
            return next({ name: 'post-login' });
          }

          return next();
        } catch (refreshError) {
          return handleInvalidToken(authStore, next);
        }
      }
    } catch (error) {
      return handleInvalidToken(authStore, next);
    }
  } else {
    return next();
  }
});

export default router;