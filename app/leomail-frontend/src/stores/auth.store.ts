// src/stores/auth.ts
import { defineStore } from 'pinia';
import axios from 'axios';
import { KEYCLOAK_CLIENT_SECRET } from '@/configs/keycloak.config';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: localStorage.getItem('access_token') || null,
        refreshToken: localStorage.getItem('refresh_token') || null,
    }),
    actions: {
        async login(credentials: { username: string; password: string }) {
            try {
                const response = await axios.post('https://kc.tommyneumaier.at/realms/2425-5bhitm/protocol/openid-connect/token', new URLSearchParams({
                    client_id: 'leomail',
                    client_secret: KEYCLOAK_CLIENT_SECRET,
                    grant_type: 'password',
                    username: credentials.username,
                    password: credentials.password,
                    scope: 'openid',
                }), {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                });

                const { access_token, refresh_token } = response.data;
                this.setTokens(access_token, refresh_token);
            } catch (error) {
                console.error('Login error', error);
                throw error;
            }
        },
        setTokens(accessToken: string, refreshToken: string) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            localStorage.setItem('access_token', accessToken);
            localStorage.setItem('refresh_token', refreshToken);
        },
        logout() {
            this.accessToken = null;
            this.refreshToken = null;
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
        },
    },
});
