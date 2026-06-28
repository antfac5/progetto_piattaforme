<template>
  <div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="fw-bold text-dark">
          <i class="bi bi-tags-fill text-warning me-2"></i>Gestione Categorie
        </h1>
        <p class="text-muted">Organizza le categorie dei prodotti per il catalogo di pesca</p>
      </div>
      <button @click="apriModalNuovo" class="btn btn-success shadow-sm fw-bold">
        <i class="bi bi-plus-circle me-1"></i> Nuova Categoria
      </button>
    </div>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">Caricamento categorie in corso...</p>
    </div>

    <div v-if="!loading" class="card shadow-sm border-0">
      <div class="table-responsive p-3">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th style="width: 15%">ID Categoria</th>
            <th style="width: 60%">Nome Categoria</th>
            <th style="width: 25%" class="text-end">Azioni</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="cat in categories" :key="cat.id">
            <td class="fw-bold text-secondary"># {{ cat.id }}</td>
            <td>
              <span class="fs-5 text-dark fw-semibold">{{ cat.name }}</span>
            </td>
            <td class="text-end">
              <button @click="apriModalModifica(cat)" class="btn btn-outline-primary btn-sm me-2" title="Modifica">
                <i class="bi bi-pencil-square me-1"></i> Modifica
              </button>
              <button @click="eliminaCategoria(cat.id)" class="btn btn-outline-danger btn-sm" title="Elimina">
                <i class="bi bi-trash3-fill"></i>
              </button>
            </td>
          </tr>
          <tr v-if="categories.length === 0">
            <td colspan="3" class="text-center py-4 text-muted">
              <i class="bi bi-info-circle me-1"></i> Nessuna categoria presente nel database.
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="categoryModal" tabindex="-1" ref="modalRef">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-dark text-white">
            <h5 class="modal-title fw-bold">
              {{ isEditMode ? 'Modifica Categoria #' + form.id : 'Aggiungi Nuova Categoria' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <form @submit.prevent="salvaCategoria">
            <div class="modal-body bg-light py-4">
              <div class="mb-3">
                <label class="form-label fw-semibold text-secondary">Nome della Categoria</label>
                <input
                    v-model="form.name"
                    type="text"
                    class="form-control form-control-lg border-2 shadow-sm"
                    placeholder="Es. Canne da Pesca, Esche, Mulinelli..."
                    required
                >
              </div>
            </div>
            <div class="modal-footer border-top bg-white">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annulla</button>
              <button type="submit" class="btn btn-success fw-bold px-4">
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
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import { Modal } from 'bootstrap';

const categories = ref([]);
const loading = ref(true);
const isEditMode = ref(false);
const modalRef = ref(null);
let bootstrapModal = null;

// Modello reattivo per il Form
const formDefault = () => ({
  id: null,
  name: ''
});
const form = ref(formDefault());

// READ: Recupera l'elenco completo dal server
const caricaCategorie = async () => {
  loading.value = true;
  try {
    // Chiama il metodo getAllCategories() mappato su GET /api/v1/categories
    const response = await api.get('/api/v1/categories');
    categories.value = response.data || [];
  } catch (err) {
    console.error("Errore nel caricamento delle categorie:", err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  caricaCategorie();
  // Inizializza l'istanza JavaScript del modal di Bootstrap
  bootstrapModal = new Modal(modalRef.value);
});

const apriModalNuovo = () => {
  isEditMode.value = false;
  form.value = formDefault();
  bootstrapModal.show();
};

const apriModalModifica = (categoria) => {
  isEditMode.value = true;
  // Copia profonda per evitare aggiornamenti in tempo reale sulla tabella prima di salvare
  form.value = { ...categoria };
  bootstrapModal.show();
};

// CREATE e UPDATE unificati
const salvaCategoria = async () => {
  try {
    if (isEditMode.value) {
      // Chiama updateCategory su PUT /api/v1/categories/{id}
      // Passando il nome come stringa nel Body (come richiesto dal tuo @RequestBody String name)
      await api.put(`/api/v1/categories/${form.value.id}`, form.value.name, {
        headers: { 'Content-Type': 'text/plain' }
      });
      alert("Categoria aggiornata con successo!");
    } else {
      // Chiama createNewCategory su POST /api/v1/categories
      // Passando l'oggetto DTO richiesto dal backend (CategoryDTO, che contiene il campo name)
      await api.post('/api/v1/categories', { name: form.value.name });
      alert("Nuova categoria registrata.");
    }
    bootstrapModal.hide();
    caricaCategorie(); // Ricarica i dati aggiornati
  } catch (err) {
    console.error("Errore durante il salvataggio della categoria:", err);
    alert("Operazione non riuscita. Verifica i parametri.");
  }
};

// DELETE: Rimozione della categoria
const eliminaCategoria = async (id) => {
  if (confirm("Sei sicuro di voler eliminare questa categoria? Attenzione: l'operazione fallirà se ci sono prodotti associati.")) {
    try {
      // Chiama deleteCategoryById su DELETE /api/v1/categories/{id}
      await api.delete(`/api/v1/categories/${id}`);
      alert("Categoria rimossa definitivamente.");
      caricaCategorie();
    } catch (err) {
      console.error("Errore durante l'eliminazione della categoria:", err);
      alert("Impossibile eliminare la categoria (vincolo di integrità del database attivo).");
    }
  }
};
</script>