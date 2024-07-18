import axios from 'axios';
import { useAuthStore } from '@/stores/auth.store';

export const login = async (username: string, password: string) => {
    try {
        const response = await axios.post('/api/auth/login', new URLSearchParams({
            username,
            password
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            isLoginRequest: true
        });
        const { access_token, refresh_token } = response.data;
        const authStore = useAuthStore();

        const isValid = await validateToken(access_token);

        if (!isValid) {

        }

        authStore.setTokens(access_token, refresh_token);
        return access_token;
    } catch (error) {return false;}
};

export const refreshToken = async () => {
    const authStore = useAuthStore();
    const refresh_token = authStore.refreshToken;
    if (refresh_token) {
        try {
            const response = await axios.post('/api/auth/refresh', new URLSearchParams({
                refresh_token: refresh_token,
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            });

            const { access_token, refresh_token: new_refresh_token } = response.data;
            authStore.setTokens(access_token, new_refresh_token);
            return access_token;
        } catch (error) {
            authStore.logout();
            throw error;
        }
    }
};

export const validateToken = async (token: string) => {
    try {
        const response = await axios.get('/api/auth/validate', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.status === 200;
    } catch (error) {
        throw error;
    }
};

export const logout = () => {
    const authStore = useAuthStore();
    authStore.logout();
};