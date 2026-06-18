<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow">
    <div class="container-fluid">
      <RouterLink class="navbar-brand fw-bold text-info" to="/">
        <i class="bi bi-water"></i> Giamà Fishing
      </RouterLink>

      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink class="nav-link" to="/">Home</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/producers">
              <i class="bi bi-grid-3x3-gap"></i> Produttori
            </RouterLink>
          </li>
        </ul>

        <div class="d-flex align-items-center gap-3 text-white">
          <span><i class="bi bi-person-circle"></i> {{ username }}</span>
          <button @click="logout" class="btn btn-outline-danger btn-sm">
            <i class="bi bi-box-arrow-right"></i> Logout
          </button>
        </div>
      </div>
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