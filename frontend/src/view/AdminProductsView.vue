<template>
  <div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="fw-bold text-dark"><i class="bi bi-gear-fill text-warning me-2"></i>Gestione Catalogo</h1>
        <p class="text-muted">Aggiungi, modifica o elimina gli articoli del negozio</p>
      </div>
      <button @click="apriModalNuovo" class="btn btn-success shadow-sm fw-bold">
        <i class="bi bi-plus-circle me-1"></i> Nuovo Prodotto
      </button>
    </div>

    <div class="card shadow-sm border-0">
      <div class="table-responsive p-3">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>Prodotto</th>
            <th>Prezzo Base</th>
            <th>Sconto</th>
            <th>Quantità Magazzino</th>
            <th class="text-end">Azioni</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="p in prodotti" :key="p.id">
            <td>{{ p.id }}</td>
            <td>
              <div class="d-flex align-items-center">
                <img :src="p.imageUrl || 'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'40\' height=\'40\'><rect width=\'100%\' height=\'100%\' fill=\'%23f5f5f5\'/></svg>'" alt="" class="rounded me-2 border" style="width: 40px; height: 40px; object-fit: cover;">
                <div>
                  <span class="fw-bold d-block text-dark">{{ p.name }}</span>
                  <small class="text-muted">{{ p.producer?.name }}</small>
                </div>
              </div>
            </td>
            <td>€ {{ p.price.toFixed(2) }}</td>
            <td>
              <span v-if="p.discount > 0" class="badge bg-danger">-{{ p.discount }}%</span>
              <span v-else class="text-muted">-</span>
            </td>
            <td>
                <span class="badge" :class="p.quantity > 5 ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'">
                  {{ p.quantity }} pz
                </span>
            </td>
            <td class="text-end">
              <button @click="apriModalModifica(p)" class="btn btn-outline-primary btn-sm me-2" title="Modifica">
                <i class="bi bi-pencil-square"></i>
              </button>
              <button @click="eliminaProdotto(p.id)" class="btn btn-outline-danger btn-sm" title="Elimina">
                <i class="bi bi-trash3-fill"></i>
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="productModal" tabindex="-1" ref="modalRef">
      <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title fw-bold">
              {{ isEditMode ? 'Modifica Prodotto #' + form.id : 'Crea Nuovo Prodotto' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="salvaProdotto">
            <div class="modal-body bg-light">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold">Nome Prodotto</label>
                  <input v-model="form.name" type="text" class="form-control" required>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold">URL Immagine</label>
                  <input v-model="form.imageUrl" type="text" class="form-control">
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">Prezzo (€)</label>
                  <input v-model.number="form.price" type="number" step="0.01" class="form-control" required>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">Sconto (%)</label>
                  <input v-model.number="form.discount" type="number" min="0" max="100" class="form-control">
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">Quantità Stock</label>
                  <input v-model.number="form.quantity" type="number" min="0" class="form-control" required>
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold">Descrizione</label>
                  <textarea v-model="form.description" class="form-control" rows="3" required></textarea>
                </div>

                <div class="col-md-6">
                  <label class="form-label fw-semibold">Categoria</label>
                  <select v-model.number="form.category.id" class="form-select" required>
                    <option :value="null" disabled>Scegli la categoria...</option>
                    <option v-for="c in categorie" :key="c.id" :value="c.id">
                      {{ c.id }} - {{ c.name }}
                    </option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label fw-semibold">Produttore / Marchio</label>
                  <select v-model.number="form.producer.id" class="form-select" required>
                    <option :value="null" disabled>Scegli il produttore...</option>
                    <option v-for="p in produttori" :key="p.id" :value="p.id">
                      {{ p.id }} - {{ p.name }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
            <div class="modal-footer border-top bg-white">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annulla</button>
              <button type="submit" class="btn btn-success fw-bold">
                <i class="bi bi-check-circle me-1"></i> Salva Modifiche
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { Modal } from 'bootstrap';

const prodotti = ref([]);
const categorie = ref([]);   // Array per memorizzare le categorie
const produttori = ref([]);  // Array per memorizzare i produttori

const isEditMode = ref(false);
const modalRef = ref(null);
let bootstrapModal = null;

const formDefault = () => ({
  id: null,
  name: '',
  description: '',
  price: 0.0,
  discount: 0,
  imageUrl: '',
  quantity: 0,
  category: { id: null },
  producer: { id: null }
});
const form = ref(formDefault());

// 1. Carica tutti i prodotti
const caricaProdotti = async () => {
  try {
    const response = await api.get('/api/v1/products', { params: { page: 0, size: 50 } });
    prodotti.value = response.data.data.products.content || [];
  } catch (err) {
    console.error("Errore nel caricamento prodotti:", err);
  }
};

// 2. Carica le Categorie (con controllo flessibile della struttura)
const caricaCategorie = async () => {
  try {
    const response = await api.get('/api/v1/categories');
    console.log("DEBUG CATEGORIE - Struttura completa ricevuta:", response.data);

    if (Array.isArray(response.data)) {
      categorie.value = response.data;
    } else if (response.data?.data && Array.isArray(response.data.data)) {
      categorie.value = response.data.data;
    } else if (response.data?.data?.categories) {
      categorie.value = response.data.data.categories;
    } else {
      console.warn("Attenzione: Struttura JSON categorie non riconosciuta automaticamente.");
    }
  } catch (err) {
    console.error("Errore HTTP nel caricamento delle categorie (Verifica l'URL del Controller):", err);
  }
};

// 3. Carica i Produttori (con controllo flessibile della struttura)
const caricaProduttori = async () => {
  try {
    const response = await api.get('/api/v1/producers');
    console.log("DEBUG PRODUTTORI - Struttura completa ricevuta:", response.data);

    if (Array.isArray(response.data)) {
      produttori.value = response.data;
    } else if (response.data?.data && Array.isArray(response.data.data)) {
      produttori.value = response.data.data;
    } else {
      console.warn("Attenzione: Struttura JSON produttori non riconosciuta automaticamente.");
    }
  } catch (err) {
    console.error("Errore HTTP nel caricamento dei produttori:", err);
  }
};

onMounted(() => {
  caricaProdotti();
  caricaCategorie();   // Carica le categorie all'avvio
  caricaProduttori();  // Carica i produttori all'avvio
  bootstrapModal = new Modal(modalRef.value);
});

const apriModalNuovo = () => {
  isEditMode.value = false;
  form.value = formDefault();
  bootstrapModal.show();
};

const apriModalModifica = (prodotto) => {
  isEditMode.value = true;
  const copiaProdotto = JSON.parse(JSON.stringify(prodotto));

  const categoryId = copiaProdotto.category?.id || copiaProdotto.categoryId || (copiaProdotto.category && typeof copiaProdotto.category === 'number' ? copiaProdotto.category : null);
  const producerId = copiaProdotto.producer?.id || copiaProdotto.producerId || (copiaProdotto.producer && typeof copiaProdotto.producer === 'number' ? copiaProdotto.producer : null);

  copiaProdotto.category = { id: categoryId };
  copiaProdotto.producer = { id: producerId };

  form.value = copiaProdotto;
  bootstrapModal.show();
};

const salvaProdotto = async () => {
  try {
    if (isEditMode.value) {
      await api.put('/api/v1/products', form.value);
      alert("Prodotto aggiornato con successo!");
    } else {
      await api.post('/api/v1/products', form.value);
      alert("Nuovo prodotto inserito nel catalogo!");
    }
    bootstrapModal.hide();
    caricaProdotti();
  } catch (err) {
    console.error("Errore nel salvataggio:", err);
    alert("Operazione non riuscita. Controlla i dati immessi.");
  }
};

const eliminaProdotto = async (id) => {
  if (confirm("Sei sicuro di voler eliminare definitivamente questo prodotto dal catalogo?")) {
    try {
      await api.delete('/api/v1/products/product', { params: { productId: id } });
      alert("Prodotto eliminato correttamente.");
      caricaProdotti();
    } catch (err) {
      console.error("Errore durante l'eliminazione:", err);
      alert("Impossibile eliminare il prodotto.");
    }
  }
};
</script>