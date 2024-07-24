import axios, {AxiosResponse} from 'axios';
import {useAuthStore} from '@/stores/auth.store';

export const login = async (username: string, password: string) => {
    try {
        const authStore = useAuthStore();

        await axios.post('/api/auth/login', new URLSearchParams({
            username,
            password
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            isLoginRequest: true
        }).then((response) => {
            const {access_token, refresh_token} = response.data;
            authStore.setTokens(access_token, refresh_token);
        });
        return true;
    } catch (error) {
        throw new Error("Kein Benutzer, passend zu den eingegebenen Daten, gefunden.");
    }
};

export const refreshToken = async () => {
    const authStore = useAuthStore();
    const refresh_token = authStore.$state.refreshToken;
    if (refresh_token) {
        try {
            const response = await axios.post('/api/auth/refresh', new URLSearchParams({
                "refresh_token": refresh_token,
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            });

            const { access_token, refresh_token: new_refresh_token } = response.data;
            authStore.setTokens(access_token, new_refresh_token);
            return {access_token, new_refresh_token};
        } catch (error) {
            authStore.logout();
        }
    }
};

export const validateToken = async (token: string) => {
    try {
        await axios.get('/api/auth/validate', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).then((response: AxiosResponse<boolean, boolean>) => {
            return response.status === 200;
        });
    } catch (error) {
        return false;
    }
};

export const logout = () => {
    const authStore = useAuthStore();
    authStore.logout();
};