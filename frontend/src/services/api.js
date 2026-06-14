import axios from 'axios';
import keycloak from './keycloak';

// Creiamo un'istanza globale di Axios puntando al tuo backend Spring Boot
const api = axios.create({
    baseURL: import.meta.env.BASE_URL
});

// Intercettiamo ogni richiesta HTTP prima che parta per inserire il Token JWT
api.interceptors.request.use(
    (config) => {
        // Se l'utente è loggato ed ha un token valido, lo aggiungiamo all'header Authorization
        if (keycloak.token) {
            config.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;