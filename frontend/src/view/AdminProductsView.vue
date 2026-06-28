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
                <img :src="p.imageUrl" alt="" class="rounded me-2 border" style="width: 40px; height: 40px; object-fit: cover;">
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
                  <input v-model="form.name" type="text" class="form-validate form-control" required>
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
                  <label class="form-label fw-semibold">ID Categoria</label>
                  <input v-model.number="form.category.id" type="number" class="form-control" required>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold">ID Produttore</label>
                  <input v-model.number="form.producer.id" type="number" class="form-control" required>
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
// Importiamo l'oggetto Modal nativo di Bootstrap (assicurati che bootstrap sia installato via npm)
import { Modal } from 'bootstrap';

const prodotti = ref([]);
const isEditMode = ref(false);
const modalRef = ref(null);
let bootstrapModal = null;

// Modello dati del form (strutturato come Product.java)
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

// Carica tutti i prodotti (per semplicità prendiamo la prima pagina corposa)
const caricaProdotti = async () => {
  try {
    const response = await api.get('/api/v1/products', { params: { page: 0, size: 50 } });
    prodotti.value = response.data.data.products.content || [];
  } catch (err) {
    console.error("Errore nel caricamento prodotti:", err);
  }
};

onMounted(() => {
  caricaProdotti();
  // Inizializziamo il modal Bootstrap programmaticamente
  bootstrapModal = new Modal(modalRef.value);
});

const apriModalNuovo = () => {
  isEditMode.value = false;
  form.value = formDefault(); // Resetta il form
  bootstrapModal.show();
};

const apriModalModifica = (prodotto) => {
  isEditMode.value = true;
  // Copia profonda dell'oggetto per evitare modifiche reattive immediate in tabella
  const copiaProdotto = JSON.parse(JSON.stringify(prodotto));
  //Garantisco che gli oggetti innestati esistano e abbiano l'ID originario precompilato.
  const categoryId = copiaProdotto.category?.id || copiaProdotto.categoryId || (copiaProdotto.category && typeof copiaProdotto.category === 'number' ? copiaProdotto.category : null);
  const producerId = copiaProdotto.producer?.id || copiaProdotto.producerId || (copiaProdotto.producer && typeof copiaProdotto.producer === 'number' ? copiaProdotto.producer : null);

  copiaProdotto.category = { id: categoryId };
  copiaProdotto.producer = { id: producerId };

  form.value = copiaProdotto;
  bootstrapModal.show();
};

// Funzione Unica di Salvataggio (POST per inserire, PUT per aggiornare)
const salvaProdotto = async () => {
  try {
    if (isEditMode.value) {
      // In accordo con ProductController @RequestMapping(method = RequestMethod.PUT)
      await api.put('/api/v1/products', form.value);
      alert("Prodotto aggiornato con successo!");
    } else {
      // In accordo con ProductController @PostMapping
      await api.post('/api/v1/products', form.value);
      alert("Nuovo prodotto inserito nel catalogo!");
    }
    bootstrapModal.hide();
    caricaProdotti(); // Ricarica la tabella aggiornata
  } catch (err) {
    console.error("Errore nel salvataggio:", err);
    alert("Operazione non riuscita. Controlla i dati immessi.");
  }
};

// Funzione di Eliminazione
const eliminaProdotto = async (id) => {
  if (confirm("Sei sicuro di voler eliminare definitivamente questo prodotto dal catalogo?")) {
    try {
      // In accordo con ProductController @RequestMapping(value = "/product", method = RequestMethod.DELETE)
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