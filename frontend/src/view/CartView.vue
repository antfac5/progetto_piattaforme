<template>
  <div class="container mt-5">
    <div class="mb-4">
      <h1 class="display-5 fw-bold text-dark">
        <i class="bi bi-cart3 text-primary me-2"></i>Il tuo Carrello
      </h1>
      <p class="text-muted">Gestisci gli articoli prima di completare l'ordine</p>
    </div>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-2">Caricamento del carrello...</p>
    </div>

    <div v-if="error" class="alert alert-danger text-center shadow-sm" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
    </div>

    <div v-if="!loading && !error && cartItems.length === 0" class="text-center my-5 py-5 border rounded bg-light shadow-sm">
      <i class="bi bi-cart-x text-muted display-1"></i>
      <h3 class="mt-3 fw-bold text-secondary">Il tuo carrello è vuoto</h3>
      <p class="text-muted">Non hai ancora aggiunto nessun articolo al tuo catalogo di pesca.</p>
      <RouterLink to="/" class="btn btn-primary px-4 mt-2 shadow-sm">
        <i class="bi bi-arrow-left me-2"></i>Torna allo Shopping
      </RouterLink>
    </div>

    <div v-if="!loading && !error && cartItems.length > 0" class="row g-4">

      <div class="col-lg-8">
        <div class="card shadow-sm border-0 bg-white">
          <div class="table-responsive p-3">
            <table class="table table-align-middle border-0 mb-0">
              <thead class="table-light">
              <tr>
                <th scope="col" class="border-0">Prodotto</th>
                <th scope="col" class="border-0 text-center">Quantità</th>
                <th scope="col" class="border-0 text-end">Prezzo Unitario</th>
                <th scope="col" class="border-0 text-end">Totale</th>
              </tr>
              </thead>
              <tbody>
                <tr v-for="item in cartItems" :key="item.id" class="align-middle">
                  <td class="py-3 border-bottom">
                    <div class="d-flex align-items-center">
                      <img :src="item.product.imageUrl || 'https://via.placeholder.com/60'"
                           alt="Prodotto"
                           class="rounded me-3 border"
                           style="width: 60px; height: 60px; object-fit: cover;">
                      <div>
                        <h6 class="mb-0 fw-bold text-dark">{{ item.product.name }}</h6>
                        <small class="text-muted d-block" style="font-size: 0.8rem;">
                          Produttore: {{ item.product.producer?.name || 'Sconosciuto' }}
                        </small>
                        <span v-if="item.product.discount > 0" class="badge bg-danger-subtle text-danger p-1 px-2 mt-1" style="font-size: 0.7rem;">
                          Sconto {{ item.product.discount }}% applicato
                        </span>
                      </div>
                    </div>
                  </td>
                  <td class="py-3 border-bottom text-center">
                    <div class="input-group input-group-sm justify-content-center mx-auto" style="max-width: 110px;">
                      <button @click="riduciQuantita(item)" class="btn btn-outline-secondary" type="button" :disabled="item.quantity <= 1">
                        <i class="bi bi-minus"></i>
                      </button>
                      <span class="form-control text-center bg-white border-secondary-subtle fw-bold" style="min-width: 35px;">
                        {{ item.quantity }}
                      </span>
                      <button @click="aumentaQuantita(item)" class="btn btn-outline-secondary" type="button">
                        <i class="bi bi-plus"></i>
                      </button>
                    </div>
                  </td>
                  <td class="py-3 border-bottom text-end">
                    <div v-if="item.product.discount > 0">
                      <span class="text-muted text-decoration-line-through small d-block">
                        € {{ item.product.price.toFixed(2) }}
                      </span>
                      <span class="text-danger fw-bold">
                        € {{ getEffectivePrice(item.product).toFixed(2) }}
                      </span>
                    </div>
                    <div v-else class="text-dark fw-semibold">
                      € {{ item.product.price.toFixed(2) }}
                    </div>
                  </td>
                  <td class="py-3 border-bottom text-end fw-bold text-primary">
                    € {{ (getEffectivePrice(item.product) * item.quantity).toFixed(2) }}
                  </td>
                  <td class="py-3 border-bottom text-center">
                    <button @click="rimuoviDalCarrello(item.product.id)" class="btn btn-link text-danger p-0" title="Rimuovi dal carrello">
                      <i class="bi bi-trash3 fs-5"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <div class="card border-0 shadow-sm p-4 bg-light">
          <h4 class="fw-bold mb-4 text-dark border-bottom pb-2">Riepilogo Ordine</h4>

          <div class="d-flex justify-content-between mb-3">
            <span class="text-muted">Prezzo di listino:</span>
            <span class="fw-semibold text-dark">€ {{ calcolaPrezzoListino.toFixed(2) }}</span>
          </div>

          <div v-if="calcolaRisparmioSconto > 0" class="d-flex justify-content-between mb-3 text-danger">
            <span>Sconto totale:</span>
            <span class="fw-bold">- € {{ calcolaRisparmioSconto.toFixed(2) }}</span>
          </div>

          <div class="d-flex justify-content-between mb-4 border-top pt-3 fs-5 fw-bold text-dark">
            <span>Totale:</span>
            <span class="text-success">€ {{ calcolaTotaleFinale.toFixed(2) }}</span>
          </div>

          <button @click="procediAlCheckout" class="btn btn-success w-100 btn-lg shadow-sm fw-bold py-3">
            <i class="bi bi-credit-card-2-front me-2"></i>Procedi al Checkout
          </button>

          <RouterLink to="/" class="btn btn-outline-primary w-100 mt-2 btn-sm border-0">
            <i class="bi bi-arrow-left me-1"></i>Continua lo shopping
          </RouterLink>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import api from '@/services/api';
import { cartState } from '@/services/cartState';

const cartItems = ref([]);
const loading = ref(true);
const error = ref(null);
const router = useRouter();

// Recupera i prodotti effettivi dentro al carrello
const fetchCart = async () => {
  loading.value = true;
  try {
    const response = await api.get('/api/v1/orders/cart-items');
    // Assegniamo l'array restituito dall'endpoint del backend
    cartItems.value = response.data || [];
  } catch (err) {
    console.error("Errore nel recupero del carrello:", err);
    error.value = "Impossibile caricare il carrello. Riprova più tardi.";
  } finally {
    loading.value = false;
  }
};

// INCREMENTA QUANTITÀ (+1)
const aumentaQuantita = async (item) => {
  try {
    // In accordo con @PutMapping(value = "/incr-quantity/product") e @RequestParam(name = "id")
    await api.put('/api/v1/orders/incr-quantity/product', null, {
      params: { id: item.product.id }
    });

    // Rinfreschiamo i dati globali del carrello e il badge sulla Navbar
    await fetchCart();
    await cartState.refreshCount();
  } catch (err) {
    console.error("Errore nell'incremento della quantità:", err);
    alert("Impossibile incrementare la quantità.");
  }
};

// RIDUCI QUANTITÀ (-1)
const riduciQuantita = async (item) => {
  // Evitiamo il decremento se la quantità è già a 1
  if (item.quantity <= 1) return;

  try {
    // In accordo con @PutMapping(value = "/decr-quantity/product") e @RequestParam(name = "id")
    await api.put('/api/v1/orders/decr-quantity/product', null, {
      params: { id: item.product.id }
    });

    // Rinfreschiamo i dati globali del carrello e il badge sulla Navbar
    await fetchCart();
    await cartState.refreshCount();
  } catch (err) {
    console.error("Errore nel decremento della quantità:", err);
    alert("Impossibile decrementare la quantità.");
  }
};

// RIMUOVI COMPLETAMENTE IL PRODOTTO DAL CARRELLO
const rimuoviDalCarrello = async (productId) => {
  if (confirm("Vuoi rimuovere completamente questo prodotto dal carrello?")) {
    try {
      // In accordo con @DeleteMapping(value = "/cart") e @RequestParam(name = "id")
      await api.delete('/api/v1/orders/cart', {
        params: { id: productId }
      });

      alert("Prodotto rimosso dal carrello.");
      await fetchCart();
      await cartState.refreshCount();
    } catch (err) {
      console.error("Errore durante la rimozione totale dal carrello:", err);
      alert("Impossibile rimuovere il prodotto.");
    }
  }
};

// Funzione helper per ottenere il prezzo scontato corretto di un singolo prodotto
const getEffectivePrice = (product) => {
  if (product.finalPrice != null) return product.finalPrice;
  if (product.discount && product.discount > 0) {
    return product.price - (product.price * product.discount / 100);
  }
  return product.price;
};

// COMPUTED - Calcola il totale basandosi sui prezzi iniziali di listino
const calcolaPrezzoListino = computed(() => {
  return cartItems.value.reduce((acc, item) => acc + (item.product.price * item.quantity), 0);
});

// COMPUTED - Calcola quanti soldi risparmia il cliente grazie agli sconti
const calcolaRisparmioSconto = computed(() => {
  return cartItems.value.reduce((acc, item) => {
    const listino = item.product.price;
    const effettivo = getEffectivePrice(item.product);
    return acc + ((listino - effettivo) * item.quantity);
  }, 0);
});

// COMPUTED - Calcola il prezzo finale effettivo da pagare
const calcolaTotaleFinale = computed(() => {
  return cartItems.value.reduce((acc, item) => acc + (getEffectivePrice(item.product) * item.quantity), 0);
});

const procediAlCheckout = () => {
  try{
    router.push('/checkout');
  } catch(err){
    console.error("Si e' verificato un problema!", err);
    alert("Impossibile procedere con l'acquisto.");
  }
};

onMounted(() => {
  fetchCart();
  // Ne approfittiamo per sincronizzare il contatore sulla Navbar
  cartState.refreshCount();
});
</script>

<style scoped>
.table-align-middle td, .table-align-middle th {
  vertical-align: middle;
}
</style>