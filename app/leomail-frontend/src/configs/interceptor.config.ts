// src/services/axios-interceptors.ts
import axios from 'axios';
import { KEYCLOAK_CLIENT_SECRET } from '@/configs/keycloak.config';
import {useAuthStore} from "@/stores/auth.store";

const setupAxiosInterceptors = (store: any) => {
    axios.interceptors.request.use(
        async (config) => {
            const authStore = useAuthStore(store);
            const token = authStore.accessToken;

            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }

            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    axios.interceptors.response.use(
        (response) => {
            return response;
        },
        async (error) => {
            const originalRequest = error.config;
            const authStore = useAuthStore(store);

            if (error.response.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                try {
                    const refreshToken = authStore.refreshToken;
                    if (!refreshToken) {
                        throw new Error('No refresh token available');
                    }

                    const response = await axios.post('https://kc.tommyneumaier.at/realms/2425-5bhitm/protocol/openid-connect/token', new URLSearchParams({
                        client_id: 'leomail',
                        client_secret: KEYCLOAK_CLIENT_SECRET,
                        grant_type: 'refresh_token',
                        refresh_token: refreshToken,
                    }), {
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                    });

                    const { access_token, refresh_token } = response.data;
                    authStore.setTokens(access_token, refresh_token);
                    axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
                    originalRequest.headers['Authorization'] = `Bearer ${access_token}`;

                    return axios(originalRequest);
                } catch (err) {
                    console.error('Refresh token error:', err);
                    authStore.logout();
                    return Promise.reject(err);
                }
            }

            return Promise.reject(error);
        }
    );
};

export default setupAxiosInterceptors;
