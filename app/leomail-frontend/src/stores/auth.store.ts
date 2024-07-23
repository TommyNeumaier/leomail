import { defineStore } from 'pinia';
import axios from 'axios';
import router from "@/configs/router";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: '',
        refreshToken: '',
    }),
    actions: {
        setTokens(accessToken: string, refreshToken: string) {
            console.log('Setting tokens:', { accessToken, refreshToken }); // Debugging step
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        },
        async refreshToken() {
            if (!this.refreshToken) {
                throw new Error('No refresh token available');
            }
            try {
                console.log('Attempting to refresh token with refresh token:', this.refreshToken); // Debugging step
                const response = await axios.post('/api/auth/refresh', new URLSearchParams({
                    refresh_token: this.refreshToken,
                }), {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                });

                const { access_token, refresh_token: new_refresh_token } = response.data;
                this.setTokens(access_token, new_refresh_token);
                return access_token;
            } catch (error) {
                console.error('Error refreshing token:', error);
                this.logout();
                throw error;
            }
        },
        logout() {
            console.log('Logging out');
            this.accessToken = '';
            this.refreshToken = '';
            router.push("/login").then(() => {});
        }
    },
    persist: true
});
