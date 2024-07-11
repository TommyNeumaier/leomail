import './assets/main.css';

import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import axios from 'axios';
const app = createApp(App)
app.component('QuillEditor', QuillEditor)

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);
app.use(router);

const setupAxiosInterceptors = (store: any) => {
    axios.interceptors.request.use(
        async (config) => {
            const token = localStorage.getItem('access_token');
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

            if (error.response && error.response.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                try {
                    const refreshToken = localStorage.getItem('refresh_token');
                    if (!refreshToken) {
                        console.error('No refresh token available');
                        store.dispatch('auth/logout');
                        return Promise.reject(error);
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
                    localStorage.setItem('access_token', access_token);
                    localStorage.setItem('refresh_token', refresh_token);
                    axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
                    originalRequest.headers['Authorization'] = `Bearer ${access_token}`;

                    return axios(originalRequest);
                } catch (err) {
                    console.error('Refresh token error:', err);
                    store.dispatch('auth/logout');
                    return Promise.reject(err);
                }
            }

            return Promise.reject(error);
        }
    );
};

setupAxiosInterceptors(pinia);

app.mount('#app');