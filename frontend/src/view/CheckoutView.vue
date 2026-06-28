<template>
  <div class="container mt-5">
    <div class="mb-4">
      <h1 class="display-5 fw-bold text-dark">
        <i class="bi bi-credit-card-2-front text-success me-2"></i>Dettagli Checkout
      </h1>
      <p class="text-muted">Completa i dati di spedizione per finalizzare il tuo ordine di pesca</p>
    </div>

    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-success" role="status"></div>
      <p class="mt-2">Verifica del carrello in corso...</p>
    </div>

    <div v-else class="row g-4">
      <div class="col-lg-7">
        <div class="card shadow-sm border-0 p-4 bg-white">
          <h4 class="fw-bold text-dark mb-4 border-bottom pb-2">
            <i class="bi bi-truck me-2 text-secondary"></i>Indirizzo di Spedizione
          </h4>

          <form @submit.prevent="gestisciCheckout">
            <div class="mb-3">
              <label class="form-label fw-semibold">Nome e Cognome del Destinatario</label>
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-person"></i></span>
                <input v-model="form.recipientName" type="text" class="form-control" placeholder="Es. Mario Rossi" required>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold">Indirizzo Completo (Via, Civico, CAP, Città)</label>
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-geo-alt"></i></span>
                <input v-model="form.shippingAddress" type="text" class="form-control" placeholder="Es. Via Roma 15, 98100 Messina" required>
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label fw-semibold">Numero di Telefono</label>
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-telephone"></i></span>
                <input v-model="form.phoneNumber" type="tel" class="form-control" placeholder="Es. 3451234567" required>
              </div>
            </div>

            <button type="submit" class="btn btn-success w-100 btn-lg shadow-sm fw-bold py-3" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-check-all me-1"></i> Conferma l'Ordine e Paga
            </button>
          </form>
        </div>
      </div>

      <div class="col-lg-5">
        <div class="card border-0 shadow-sm p-4 bg-light">
          <h4 class="fw-bold mb-4 text-dark border-bottom pb-2">Riepilogo Carrello</h4>

          <div class="overflow-auto mb-4" style="max-height: 240px;">
            <div v-for="item in cartItems" :key="item.id" class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom border-secondary-subtle">
              <div class="pe-3">
                <span class="fw-bold text-dark d-block">{{ item.product.name }}</span>
                <small class="text-muted">Quantità: {{ item.quantity }}</small>
              </div>
              <span class="fw-semibold text-primary">
                € {{ (getEffectivePrice(item.product) * item.quantity).toFixed(2) }}
              </span>
            </div>
          </div>

          <div class="d-flex justify-content-between fs-4 fw-bold text-dark pt-2 border-top border-dark">
            <span>Totale da Pagare:</span>
            <span class="text-success">€ {{ totaleOrdine.toFixed(2) }}</span>
          </div>

          <div class="alert alert-info d-flex align-items-center mt-4 border-0 shadow-sm" role="alert">
            <i class="bi bi-shield-check fs-4 me-3 text-info"></i>
            <div style="font-size: 0.85rem;">
              Cliccando su conferma, lo stock degli articoli verrà aggiornato istantaneamente sul server.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { cartState } from '@/services/cartState';

const router = useRouter();
const cartItems = ref([]);
const totaleOrdine = ref(0);
const loading = ref(true);
const submitting = ref(false);

// Form rispecchia esattamente l'oggetto OrderForm.java del tuo Backend
const form = ref({
  recipientName: '',
  shippingAddress: '',
  phoneNumber: ''
});

// Recupera i dati del carrello per mostrare il riepilogo a destra
const caricaDettagliCheckout = async () => {
  try {
    // 1. Recuperiamo gli elementi nelle righe del carrello
    const responseItems = await api.get('/api/v1/orders/cart-items');
    cartItems.value = responseItems.data || [];

    if (cartItems.value.length === 0) {
      alert("Il tuo carrello è vuoto. Impossibile procedere al checkout.");
      router.push('/');
      return;
    }

    // 2. Recuperiamo il totale aggiornato direttamente dall'oggetto Order "PENDING"
    const responseCart = await api.get('/api/v1/orders/pending-cart');
    totaleOrdine.value = responseCart.data?.totalAmount || 0;

  } catch (err) {
    console.error("Errore nel caricamento dei dati di checkout:", err);
    alert("Si è verificato un errore nel caricamento del carrello.");
    router.push('/cart');
  } finally {
    loading.value = false;
  }
};

// Calcola il prezzo tenendo conto dell'eventuale sconto
const getEffectivePrice = (product) => {
  if (product.finalPrice != null) return product.finalPrice;
  if (product.discount && product.discount > 0) {
    return product.price - (product.price * product.discount / 100);
  }
  return product.price;
};

// Sottomissione del modulo di Checkout
const gestisciCheckout = async () => {
  submitting.value = true;
  try {
    // Invia i dati a POST /api/v1/orders/checkout mappati sul DTO OrderForm
    await api.post('/api/v1/orders/checkout', form.value);

    alert("Complimenti! Ordine preso in carico con successo.");

    // Svuotiamo il contatore globale visto che il checkout genera un nuovo carrello vuoto
    await cartState.refreshCount();

    // Reindirizza l'utente alla Home (o a una eventuale pagina degli ordini effettuati)
    router.push('/');
  } catch (err) {
    console.error("Errore durante il checkout:", err);
    // Gestione dell'errore custom lanciato dal backend (es. stock esaurito)
    const messaggioErrore = err.response?.data?.message || "Errore durante il completamento dell'ordine. Riprova.";
    alert(messaggioErrore);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  caricaDettagliCheckout();
});
</script>