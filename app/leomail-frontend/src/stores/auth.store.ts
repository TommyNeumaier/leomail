// src/stores/auth.store.js
import { defineStore } from 'pinia';
import axios from 'axios';
import routerConfig from "@/configs/router.config";
import setupAxiosInterceptors from "@/configs/interceptor.config";
import {useAppStore} from "@/stores/app.store";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: '',
        _refreshToken: '',
    }),
    actions: {
        setTokens(accessToken: string, refreshToken: string) {
            console.log('Setting tokens:', { accessToken, refreshToken }); // Debugging step
            this.accessToken = accessToken;
            this._refreshToken = refreshToken;

            setupAxiosInterceptors(this);
        },
        logout() {
            this.accessToken = '';
            this._refreshToken = '';
            delete axios.defaults.headers.common['Authorization'];

            const appStore = useAppStore();
            appStore.project = '';
            routerConfig.push("/login").then(() => {});
        }
    },
    persist: true
});