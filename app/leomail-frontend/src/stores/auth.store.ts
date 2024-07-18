import { defineStore } from 'pinia';
import axios from 'axios';
import routerConfig from "@/configs/router.config";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: '',
        refreshToken: '',
    }),
    actions: {
        setTokens(accessToken: string, refreshToken: string) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        },
        async refreshToken() {
            if (!this.refreshToken) {
                throw new Error('No refresh token available');
            }
            try {
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
                this.logout();
                throw error;
            }
        },
        logout() {
            this.accessToken = '';
            this.refreshToken = '';
            routerConfig.push("/login").then(() => {});
        }
    },
    persist: true
});