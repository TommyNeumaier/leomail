import axiosInstance from '@/axiosInstance';
import { useAuthStore } from '@/stores/auth.store';
import { useAppStore } from '@/stores/app.store';

export const login = async (username: string, password: string) => {
    const authStore = useAuthStore();
    const appStore = useAppStore();

    try {
        const response = await axiosInstance.post(
            '/auth/login',
            new URLSearchParams({ username, password }),
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            }
        );

        appStore.project = '';
        const { access_token: accessToken, refresh_token: refreshToken } = response.data;
        authStore.setTokens(accessToken, refreshToken);

        return true;
    } catch (error) {
        throw new Error('Invalid username or password.');
    }
};

export const refreshToken = async () => {
    const authStore = useAuthStore();
    const refresh_Token = authStore.$state._refreshToken;

    if (refresh_Token) {
        try {
            const response = await axiosInstance.post(
                '/auth/refresh',
                new URLSearchParams({ refresh_token: refresh_Token }),
                {
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                }
            );

            const { access_token: newAccessToken, refresh_token: newRefreshToken } = response.data;
            authStore.setTokens(newAccessToken, newRefreshToken);

            return { access_token: newAccessToken, refresh_token: newRefreshToken };
        } catch (error) {
            console.error('Error refreshing token:', error);
            authStore.logout();
            throw error;
        }
    } else {
        authStore.logout();
        throw new Error('No refresh token available');
    }
};

// Other methods remain unchanged, but use axiosInstance instead of axios


export const validateToken = async () => {
    try {
        const response = await axiosInstance.get('/api/auth/validate');
        return response.data;
    } catch (error) {
        return false;
    }
};

export const getRoles = async () => {
    try {
        const response = await axiosInstance.get('/api/auth/roles');
        return response.data;
    } catch (error) {
        return [];
    }
}

export const logout = () => {
    const authStore = useAuthStore();
    authStore.logout();
};