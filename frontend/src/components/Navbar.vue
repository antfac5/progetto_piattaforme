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
          <li class="nav-item"><RouterLink class="nav-link" to="/">Home</RouterLink></li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/producers">
              <i class="bi bi-grid-3x3-gap"></i> Produttori
            </RouterLink>
          </li>
          <li v-if="isAdmin" class="nav-item">
            <RouterLink class="nav-link text-warning fw-semibold" to="/admin/categories">
              <i class="bi bi-tags-fill"></i> Categorie
            </RouterLink>
          </li>
        </ul>

        <div class="d-flex align-items-center gap-3 text-white">
          <RouterLink v-if="isUser" to="/cart" class="text-white position-relative text-decoration-none me-2">
            <i class="bi bi-cart3 fs-4"></i>
            <span v-if="cartState.count > 0"
                  class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                  style="font-size: 0.6rem;">
              {{ cartState.count }}
            </span>
          </RouterLink>

          <RouterLink v-if="isAdmin" to="/admin/products" class="btn btn-outline-warning btn-sm me-2">
            <i class="bi bi-shield-lock"></i> Pannello Admin
          </RouterLink>

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
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import keycloak from '@/services/keycloak';
import { cartState } from '@/services/cartState'; // Importiamo lo stato del carrello

const username = ref('');

const isUser = computed(() => {
  const clientRoles = keycloak.tokenParsed?.resource_access?.['fishing-rest-api']?.roles || [];
  return keycloak.authenticated && clientRoles.includes('USER') && !clientRoles.includes('ADMIN');
});

const isAdmin = computed(() => {
  const clientRoles = keycloak.tokenParsed?.resource_access?.['fishing-rest-api']?.roles || [];
  return keycloak.authenticated && clientRoles.includes('ADMIN');
});

onMounted(() => {
  username.value = keycloak.tokenParsed?.preferred_username || 'Utente';

  // STAMPA IL CONTENUTO DEL TOKEN PER VEDERE I RUOLI
  console.log("Ecco l'intero Token decodificato:", keycloak.tokenParsed);
  console.log("I ruoli del Realm rilevati da Keycloak:", keycloak.tokenParsed?.realm_access?.roles);

  // Recupera il conteggio solo se l'utente è un USER autenticato
  if (isUser.value) {
    cartState.refreshCount();
  }
});

const logout = () => {
  keycloak.logout({ redirectUri: window.location.origin });
};
</script>