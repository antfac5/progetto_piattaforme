<template>
  <nav style="display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; background: #001529; color: white;">
    <div style="font-weight: bold; font-size: 1.2rem;">🎣 Fishing Shop</div>
    <div style="display: flex; gap: 20px;">
      <RouterLink to="/" style="color: white; text-decoration: none;">Home</RouterLink>
      <RouterLink to="/prodotti" style="color: white; text-decoration: none;">Catalogo</RouterLink>
    </div>
    <div style="display: flex; align-items: center; gap: 15px;">
      <span>{{ username }}</span>
      <button @click="logout" style="padding: 5px 10px; background: #ff4d4f; color: white; border: none; border-radius: 4px; cursor: pointer;">
        Logout
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import keycloak from '@/services/keycloak';

const username = ref('');

onMounted(() => {
  username.value = keycloak.tokenParsed?.preferred_username || 'Utente';
});

const logout = () => {
  keycloak.logout({ redirectUri: window.location.origin });
};
</script>