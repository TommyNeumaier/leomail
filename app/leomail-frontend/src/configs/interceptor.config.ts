import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';
import { refreshToken } from '@/services/auth.service';

const setupAxiosInterceptors = (store: any) => {
    axios.interceptors.request.use(
        (config) => {
            const authStore = useAuthStore(store);
            const token = authStore.accessToken;

            if (token && !config.isLoginRequest) {
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

            if (originalRequest._retry || originalRequest.isRefreshRequest) {
                return Promise.reject(error);
            }

            if (error.response.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                originalRequest.isRefreshRequest = true;
                try {
                    const newToken = await refreshToken();
                    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken.access_token}`;
                    originalRequest.headers['Authorization'] = `Bearer ${newToken.access_token}`;
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
