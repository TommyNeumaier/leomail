// src/setupAxiosInterceptors.ts

import axiosInstance from '@/axiosInstance';
import { useAuthStore } from '@/stores/auth.store';
import { refreshToken } from '@/services/auth.service';

const setupAxiosInterceptors = (store: any) => {
    axiosInstance.interceptors.request.use(
        (config) => {
            const authStore = useAuthStore(store);
            const token = authStore.accessToken;

            if (token && !config.headers['Authorization']) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }

            return config;
        },
        (error) => Promise.reject(error)
    );

    axiosInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            const originalRequest = error.config;
            const authStore = useAuthStore(store);

            if (error.response && error.response.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                try {
                    const newToken = await refreshToken();
                    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${newToken.access_token}`;
                    originalRequest.headers['Authorization'] = `Bearer ${newToken.access_token}`;
                    return axiosInstance(originalRequest);
                } catch (err) {
                    authStore.logout();
                    return Promise.reject(err);
                }
            }

            return Promise.reject(error);
        }
    );
};

export default setupAxiosInterceptors;
