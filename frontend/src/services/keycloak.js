import Keycloak from 'keycloak-js';
const { VITE_KEYCLOAK_URL, VITE_KEYCLOAK_REALM, VITE_KEYCLOAK_CLIENT_ID } = import.meta.env;

const keycloakConfig = {
    url: VITE_KEYCLOAK_URL,
    realm: VITE_KEYCLOAK_REALM,             // KEYCLOAK_APP_REALM
    clientId: VITE_KEYCLOAK_CLIENT_ID
};

const keycloak = new Keycloak(keycloakConfig);

// Funzione per inizializzare Keycloak
export const initKeycloak = (onAuthenticated) => {
    keycloak
        .init({
            onLoad: 'login-required',
            checkLoginIframe: false
        })
        .then((authenticated) => {
            if (authenticated) {
                onAuthenticated();
            } else {
                window.location.reload();
            }
        })
        .catch((error) => {
            console.error("Errore durante l'inizializzazione di Keycloak:", error);
        });
};

export default keycloak;