import axios from 'axios';
import {KEYCLOAK_CLIENT_SECRET} from "@/configs/keycloak.config";
import router from "@/router/router";

const refreshToken = async () => {
    const refresh_token = localStorage.getItem('refresh_token');
    if (refresh_token) {
        try {
            const response = await axios.post('https://auth.htl-leonding.ac.at/realms/2425-5bhitm/protocol/openid-connect/token', new URLSearchParams({
                client_id: 'leomail',
                client_secret: KEYCLOAK_CLIENT_SECRET,
                grant_type: 'refresh_token',
                refresh_token,
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            });

            const { access_token, refresh_token: new_refresh_token } = response.data;
            localStorage.setItem('access_token', access_token);
            localStorage.setItem('refresh_token', new_refresh_token);
            return access_token;
        } catch (error) {
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            return null;
        }
    }
    return null;
};

const getToken = async () => {
    const accessToken = localStorage.getItem('access_token');
    if (accessToken) {
        return accessToken;
    }
    return await refreshToken();
};

const validateToken = async (token: string): Promise<boolean> => {
    try {
        const response = await axios.get('https://auth.htl-leonding.ac.at/realms/2425-5bhitm/protocol/openid-connect/userinfo', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.status === 200;
    } catch (error) {
        console.error('Token validation failed');
        return false;
    }
};

const logout = () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    router.push({ name: 'login' }).then((r) => {})
}

export { getToken, validateToken, logout};
