<template>
  <div class="container mt-5">

    <div class="text-center mb-5 py-4 position-relative overflow-hidden rounded-4 bg-white bg-opacity-50 backdrop-blur shadow-sm border border-white">
      <div class="position-absolute top-50 start-50 translate-middle opacity-10 z-0" style="font-size: 10rem; transform: translate(-50%, -50%) rotate(-15deg) !important;">
        <i class="bi bi-waves text-info"></i>
      </div>

      <div class="position-relative z-1">
        <h1 class="display-3 fw-black logo-text mb-2 text-uppercase tracking-tight">
          <i class="bi bi-water me-2 animate-float"></i>Giamà Fishing
        </h1>
        <p class="lead text-muted fw-normal max-w-600 mx-auto px-3">
          Attrezzatura da pesca professionale selezionata per le tue avventure in mare e acqua dolce.
        </p>
      </div>
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
          <img :src="prodotto.imageUrl || 'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'300\' height=\'200\' viewBox=\'0 0 300 200\'><rect width=\'100%\' height=\'100%\' fill=\'%23f5f5f5\'/><text x=\'50%\' y=\'50%\' dominant-baseline=\'middle\' text-anchor=\'middle\' font-family=\'sans-serif\' font-size=\'16\' fill=\'%23aaa\'>Nessuna Immagine</text></svg>'"
               class="card-img-top p-3 bg-white"
               alt="Immagine prodotto"
               style="height: 200px; object-fit: contain;">

          <div class="card-body d-flex flex-column">
            <h5 class="card-title fw-bold text-dark">{{ prodotto.name }}</h5>
            <div class="mb-2">
              <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">
                <i class="bi bi-building me-1"></i> {{ prodotto.producer?.name || 'Produttore sconosciuto' }}
              </span>
            </div>
            <p class="card-text text-muted flex-grow-1">{{ prodotto.description }}</p>

            <div class="d-flex justify-content-between align-items-center mt-3">
              <div v-if="prodotto.discount && prodotto.discount > 0" class="d-flex flex-column">
                <div>
                  <span class="text-muted text-decoration-line-through fs-6 me-2">
                    € {{ prodotto.price.toFixed(2) }}
                  </span>
                  <span class="badge bg-danger text-white rounded-pill fw-bold p-1 px-2 animate-pulse" style="font-size: 0.75rem;">
                   -{{ prodotto.discount }}%
                  </span>
                </div>
                <span class="fs-4 fw-bold text-danger">
                  € {{ prodotto.finalPrice != null ? prodotto.finalPrice.toFixed(2) : '0.00' }}
                </span>
              </div>

              <div v-else>
                <span class="fs-4 fw-bold text-success">
                  € {{ prodotto.price != null ? prodotto.price.toFixed(2) : '0.00' }}
                </span>
              </div>
              <button
                  @click="aggiungiAlCarrello(prodotto.id)"
                  class="btn btn-primary btn-sm px-3 shadow-sm"
                  :disabled="!isUser || prodotto.quantity <= 0"
                  :title="!isUser
                          ? 'Solo gli utenti acquirenti possono aggiungere prodotti al carrello'
                          : (prodotto.quantity <= 0 ? 'Prodotto momentaneamente esaurito' : '')">
                <i class="bi bi-cart-plus-fill me-1"></i>
                {{ prodotto.quantity <= 0 ? 'Esaurito' : 'Aggiungi' }}
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
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api'; // Tua istanza con baseURL e token corretti
import { cartState } from '@/services/cartState';
import keycloak from '@/services/keycloak';

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

// Funzione per aggiungere al carrello
const aggiungiAlCarrello = async (prodottoId) => {
  try {
    await api.post('/api/v1/orders/cart', prodottoId, {
      headers: { 'Content-Type': 'application/json' }
    });

    // Aggiornamento del numero sulla Navbar
    await cartState.refreshCount();

    alert("Prodotto aggiunto al carrello!");
  } catch (err) {
    console.error("Errore nell'aggiunta al carrello:", err);
    // In base all'errore do un messaggio d'errore diverso
    if (err.response) {
      const status = err.response.status;
      const serverMessage = err.response.data?.message;

      if (status === 401 || status === 403) {
        // Utente non autenticato o sessione scaduta
        alert("Sessione scaduta o non valida. Effettua nuovamente il login.");
      } else if (status === 400 && serverMessage) {
        alert(serverMessage);
      } else if (status === 409) {
        // Conflitto di concorrenza (Optimistic Locking)
        alert("Il carrello è stato aggiornato altrove. Riprova tra un istante.");
      } else if(status === 500){
        alert ("Quantità massima raggiunta per questo prodotto.");
      } else {
        // Altri errori del server
        alert(serverMessage || "Si è verificato un errore sul server. Riprova più tardi.");
      }
    }
    // 2. Il server non ha risposto (problema di rete locale o server spento)
    else if (err.request) {
      alert("Impossibile contattare il server. Controlla la tua connessione a internet.");
    }
    // 3. Errore generico di configurazione nel frontend
    else {
      alert("Si è verificato un errore imprevisto.");
    }
  }
};

const isUser = computed(() => {
  const clientRoles = keycloak.tokenParsed?.resource_access?.['fishing-rest-api']?.roles || [];
  return keycloak.authenticated && clientRoles.includes('USER') && !clientRoles.includes('ADMIN');
});

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