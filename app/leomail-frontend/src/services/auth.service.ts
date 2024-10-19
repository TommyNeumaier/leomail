import axios from 'axios';
import {useAuthStore} from '@/stores/auth.store';
import {useAppStore} from "@/stores/app.store";

export const login = async (username: string, password: string) => {
    try {
        const authStore = useAuthStore();
        const appStore = useAppStore();

        await axios.post('/api/auth/login', new URLSearchParams({
            username,
            password
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            isLoginRequest: true
        }).then((response) => {
            appStore.project = '';
            const {access_token: accessToken, refresh_token: refreshToken} = response.data;
            authStore.setTokens(accessToken, refreshToken);
        });
        return true;
    } catch (error) {
        throw new Error("Kein Benutzer, passend zu den eingegebenen Daten, gefunden.");
    }
};

export const refreshToken = async () => {
    const authStore = useAuthStore();
    const refreshToken = authStore.$state._refreshToken;

    if (refreshToken) {
        console.log("Attempting to refresh token with:", refreshToken);
        try {
            const response = await axios.post('/api/auth/refresh', new URLSearchParams({
                'refresh_token': refreshToken,
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Authorization': 'Bearer ' + authStore.$state.accessToken,
                },
                isRefreshRequest: true
            });

            console.log("Refresh token response:", response.data);
            const { accessToken, refreshToken: new_refresh_token } = response.data;
            authStore.setTokens(access_token, new_refresh_token);
            return { accessToken, refreshToken: new_refresh_token };
        } catch (error) {
            console.error("Error refreshing token:", error);
            authStore.logout();
            throw error;
        }
    } else {
        authStore.logout();
        throw new Error('No refresh token available');
    }
};

export const validateToken = async () => {
    try {
        const response = await axios.get('/api/auth/validate');
        return response.data;
    } catch (error) {
        return false;
    }
};

export const logout = () => {
    const authStore = useAuthStore();
    authStore.logout();
};