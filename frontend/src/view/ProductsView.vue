<template>
  <div class="container mt-5">
    <div class="text-center mb-5">
      <h1 class="display-4 fw-bold text-primary"><i class="bi bi-water"></i> Giamà Fishing</h1>
      <p class="lead text-muted">Benvenuto! Esplora il nostro catalogo di attrezzatura da pesca</p>
    </div>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">Caricamento prodotti in corso...</p>
    </div>

    <div v-if="error" class="alert alert-danger text-center shadow-sm" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
    </div>

    <div v-if="!loading && !error" class="row row-cols-1 row-cols-md-3 g-4">
      <div class="col" v-for="prodotto in prodotti" :key="prodotto.id">
        <div class="card h-100 shadow-sm border-0 img-hover-effect">
          <img :src="prodotto.imageUrl || 'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'300\' height=\'200\' viewBox=\'0 0 300 200\'><rect width=\'100%\' height=\'100%\' fill=\'%23eee\'/><text x=\'50%\' y=\'50%\' dominant-baseline=\'middle\' text-anchor=\'middle\' font-family=\'sans-serif\' font-size=\'16\' fill=\'%23aaa\'>Nessuna Immagine</text></svg>'"
               class="card-img-top"
               alt="Immagine prodotto"
               style="height: 200px; object-fit: cover;">

          <div class="card-body d-flex flex-column">
            <h5 class="card-title fw-bold text-dark">{{ prodotto.name }}</h5>
            <p class="card-text text-muted flex-grow-1">{{ prodotto.description }}</p>

            <div class="d-flex justify-content-between align-items-center mt-3">
              <span class="fs-4 fw-bold text-success">€ {{ prodotto.price != null ? prodotto.price.toFixed(2) : '0.00' }}</span>
              <button class="btn btn-primary btn-sm px-3 shadow-sm">
                <i class="bi bi-cart-plus-fill me-1"></i> Aggiungi
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <nav v-if="!loading && !error && totalePagine > 1" class="d-flex justify-content-center mt-5">
      <ul class="pagination shadow-sm">
        <li class="page-item" :class="{ disabled: paginaCorrente === 0 }">
          <button class="page-link" @click="cambiaPagina(paginaCorrente - 1)">Precedente</button>
        </li>

        <li class="page-item" v-for="p in totalePagine" :key="p" :class="{ active: paginaCorrente === (p - 1) }">
          <button class="page-link" @click="cambiaPagina(p - 1)">{{ p }}</button>
        </li>

        <li class="page-item" :class="{ disabled: paginaCorrente === (totalePagine - 1) }">
          <button class="page-link" @click="cambiaPagina(paginaCorrente + 1)">Successivo</button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api'; // Tua istanza con baseURL e token corretti

const prodotti = ref([]);
const loading = ref(true);
const error = ref(null);

// Stato della paginazione (Spring Boot ragiona a partire da pagina 0)
const paginaCorrente = ref(0);
const totalePagine = ref(0);
const elementiPerPagina = 20; // Impostiamo il limite massimo a 20 prodotti richiesto

const fetchProdotti = async () => {
  loading.value = true;
  try {
    const response = await api.get('/api/v1/products', {
      params: {
        page: paginaCorrente.value,
        size: elementiPerPagina
      }
    });

    // Sottoresponse corretta estraendo la mappa e la pagina di Spring
    const pageData = response.data.data.products;

    // Assegniamo i valori reattivi a Vue
    prodotti.value = pageData.content || [];
    totalePagine.value = pageData.totalPages || 0;

  } catch (err) {
    console.error("Errore nel recupero prodotti:", err);
    error.value = "Impossibile caricare il catalogo. Verifica la connessione al backend.";
  } finally {
    loading.value = false;
  }
};

const cambiaPagina = (nuovaPagina) => {
  if (nuovaPagina >= 0 && nuovaPagina < totalePagine.value) {
    paginaCorrente.value = nuevaPagina;
    fetchProdotti();
    // Scorrimento fluido verso l'alto al cambio pagina
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

onMounted(() => {
  fetchProdotti();
});
</script>

<style scoped>
.img-hover-effect {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.img-hover-effect:hover {
  transform: translateY(-5px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}
</style>