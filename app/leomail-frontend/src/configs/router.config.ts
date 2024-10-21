import { createRouter, createWebHistory } from 'vue-router';
import MailView from "@/views/MailView.vue";
import Login from "@/views/Login.vue";
import PersonenView from "@/views/ContactView.vue";
import ProfilView from "@/views/ProfilView.vue";
import { useAuthStore } from "@/stores/auth.store";
import NewProject from "@/views/NewProject.vue";
import NewMail from "@/views/NewMail.vue";
import { useAppStore } from '@/stores/app.store';
import axios from 'axios';
import { refreshToken, validateToken } from "@/services/auth.service";
import ProjectView from "@/views/ProjectView.vue";
import ScheduledMailsView from "@/views/ScheduledMailsView.vue";
import GroupView from "@/views/GroupView.vue";
import TemplateView from "@/views/TemplateView.vue";
import SettingsView from "@/views/SettingsView.vue";
import AuthorisationComponent from "@/views/AuthorisationView.vue";
import { Service } from "@/services/service";
import PostLoginView from "@/views/PostLoginView.vue";
import {pinia} from "@/main";
import axiosInstance from "@/axiosInstance";

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

// Helper function to handle invalid tokens
function handleInvalidToken(authStore, next) {
  authStore.logout();
  return next({ name: 'login' });
}

// Main navigation guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore(pinia);

  if (to.meta.requiresAuth) {
    const accessToken = authStore.accessToken;

    if (!accessToken) {
      console.log('Kein Zugriffstoken gefunden, Weiterleitung zum Login');
      return next({ name: 'login' });
    }

    try {
      const isValid = await validateToken();
      if (isValid) {
        axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        return next();
      } else {
        console.log('Zugriffstoken ist ungültig, versuche zu aktualisieren');
        try {
          const { access_token } = await refreshToken();
          authStore.setTokens(access_token, authStore.$state._refreshToken);
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
          // Proceed to the intended route
          return next();
        } catch (refreshError) {
          console.log('Token-Aktualisierung fehlgeschlagen, Weiterleitung zum Login');
          return handleInvalidToken(authStore, next);
        }
      }
    } catch (error) {
      console.error('Fehler bei der Token-Validierung oder Aktualisierung:', error);
      return handleInvalidToken(authStore, next);
    }
  } else {
    // If the route does not require auth, proceed normally
    return next();
  }
});

export default router;