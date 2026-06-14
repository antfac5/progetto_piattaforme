<template>
  <div style="padding: 20px; font-family: sans-serif;">
    <h1>🎣 Giamà Fishing - Dashboard</h1>
    <p>Benvenuto! Autenticazione eseguita con successo.</p>

    <div style="background: #f0f2f5; padding: 15px; border-radius: 8px; margin-top: 20px;">
      <h3>Dati Utente (estratti dal Token):</h3>
      <p><strong>Username:</strong> {{ username }}</p>
      <p><strong>Token JWT (Primi 30 caratteri):</strong> {{ tokenBreve }}...</p>
    </div>

    <button @click="logout" style="margin-top: 20px; padding: 10px; background: #ff4d4f; color: white; border: none; border-radius: 4px; cursor: pointer;">
      Invia Logout
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import keycloak from '@/services/keycloak';

const username = ref('');
const tokenBreve = ref('');

onMounted(() => {
  // Recuperiamo le informazioni direttamente dall'istanza di keycloak
  username.value = keycloak.tokenParsed?.preferred_username || 'Utente';
  tokenBreve.value = keycloak.token ? keycloak.token.substring(0, 30) : '';
});

const logout = () => {
  // Reindirizza l'utente a Keycloak per invalidare la sessione
  keycloak.logout({ redirectUri: window.location.origin });
};
</script>