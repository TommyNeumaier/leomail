import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
    }),
    actions: {
        logout() {
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            this.user = null;
            window.location.href = '/login';
        },
    },
});
