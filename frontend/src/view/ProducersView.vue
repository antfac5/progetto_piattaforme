<template>
  <div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-5">
      <div>
        <h1 class="display-5 fw-bold text-dark"><i class="bi bi-building text-primary me-2"></i>I Nostri Produttori</h1>
        <p class="lead text-muted">Esplora le migliori marche di attrezzatura da pesca disponibili nel catalogo</p>
      </div>
      <button v-if="isAdmin" @click="apriModalNuovo" class="btn btn-success shadow-sm fw-bold">
        <i class="bi bi-plus-circle me-1"></i> Nuovo Produttore
      </button>
    </div>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">Caricamento produttori...</p>
    </div>

    <div v-if="!loading" class="row row-cols-1 row-cols-md-3 row-cols-lg-4 g-4">
      <div class="col" v-for="p in producers" :key="p.id">
        <div class="card h-100 shadow-sm border-0 position-relative card-hover">

          <span v-if="isAdmin" class="position-absolute top-0 start-0 m-2 badge bg-dark opacity-75">ID: {{ p.id }}</span>

          <img :src="p.imageUrl || 'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'300\' height=\'150\'><rect width=\'100%\' height=\'100%\' fill=\'%23f5f5f5\'/><text x=\'50%\' y=\'50%\' dominant-baseline=\'middle\' text-anchor=\'middle\' font-size=\'14\' fill=\'%23aaa\'>Nessun Logo</text></svg>'"
               class="card-img-top p-3 bg-white object-contain"
               alt="Logo Produttore"
               style="height: 140px; object-fit: contain;">

          <div class="card-body text-center border-top bg-light d-flex flex-column justify-content-between">
            <h5 class="card-title fw-bold text-dark mb-3">{{ p.name }}</h5>

            <div v-if="isAdmin" class="d-flex justify-content-center gap-2 mt-2">
              <button @click="apriModalModifica(p)" class="btn btn-outline-primary btn-sm px-3">
                <i class="bi bi-pencil-square me-1"></i> Modifica
              </button>
              <button @click="eliminaProduttore(p.id)" class="btn btn-outline-danger btn-sm px-3">
                <i class="bi bi-trash3-fill"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="producerModal" tabindex="-1" ref="modalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title fw-bold">
              {{ isEditMode ? 'Modifica Produttore #' + form.id : 'Nuovo Produttore' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="salvaProduttore">
            <div class="modal-body bg-light">
              <div class="mb-3">
                <label class="form-label fw-semibold">Nome Marchio / Produttore</label>
                <input v-model="form.name" type="text" class="form-control" placeholder="Es. Trabucco" required>
              </div>
              <div class="mb-3">
                <label class="form-label fw-semibold">URL Immagine Logo</label>
                <input v-model="form.imageUrl" type="text" class="form-control" placeholder="https://link-immagine.com/logo.png">
              </div>
            </div>
            <div class="modal-footer border-top bg-white">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annulla</button>
              <button type="submit" class="btn btn-success fw-bold">
                <i class="bi bi-check-circle me-1"></i> Salva
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import keycloak from '@/services/keycloak';
import { Modal } from 'bootstrap';

const producers = ref([]);
const loading = ref(true);
const isEditMode = ref(false);
const modalRef = ref(null);
let bootstrapModal = null;

const formDefault = () => ({
  id: null,
  name: '',
  imageUrl: ''
});
const form = ref(formDefault());

// Controllo del Ruolo ADMIN del client
const isAdmin = computed(() => {
  const clientRoles = keycloak.tokenParsed?.resource_access?.['fishing-rest-api']?.roles || [];
  return keycloak.authenticated && clientRoles.includes('ADMIN');
});

// Recupera tutti i produttori
const caricaProduttori = async () => {
  loading.value = true;
  try {
    // Adatta questo endpoint a seconda di come si chiama nel tuo ProducerController (es: /api/v1/producers)
    const response = await api.get('/api/v1/producers');
    producers.value = response.data || [];
  } catch (err) {
    console.error("Errore nel caricamento dei produttori:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  caricaProduttori();
  bootstrapModal = new Modal(modalRef.value);
});

const apriModalNuovo = () => {
  isEditMode.value = false;
  form.value = formDefault();
  bootstrapModal.show();
};

const apriModalModifica = (produttore) => {
  isEditMode.value = true;
  form.value = { ...produttore };
  bootstrapModal.show();
};

const salvaProduttore = async () => {
  try {
    if (isEditMode.value) {
      // Invia i dati al metodo updateProducer(id, p) del backend
      await api.put(`/api/v1/producers/${form.value.id}`, form.value);
      alert("Produttore aggiornato con successo!");
    } else {
      // Invia i dati al metodo addNewProducer(producer, imageUrl) del backend
      // Se il tuo controller accetta i RequestParam o un RequestBody, assicurati che combaci.
      // Esempio basandosi su parametri standard o JSON object:
      await api.post('/api/v1/producers', form.value);
      alert("Nuovo produttore inserito!");
    }
    bootstrapModal.hide();
    caricaProduttori();
  } catch (err) {
    console.error("Errore nel salvataggio del produttore:", err);
    alert("Operazione non riuscita. Verifica la configurazione dell'endpoint.");
  }
};

const eliminaProduttore = async (id) => {
  if (confirm("Sei sicuro di voler eliminare questo produttore? Attenzione: se ci sono prodotti associati l'operazione potrebbe fallire.")) {
    try {
      await api.delete(`/api/v1/producers/${id}`);
      alert("Produttore eliminato correttamente.");
      caricaProduttori();
    } catch (err) {
      console.error("Errore durante l'eliminazione:", err);
      alert("Impossibile eliminare il produttore (verifica vincoli di integrità sul database).");
    }
  }
};
</script>

<style scoped>
.card-hover {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.12)!important;
}
</style>