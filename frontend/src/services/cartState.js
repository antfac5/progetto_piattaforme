import { reactive } from 'vue';
import api from './api';

export const cartState = reactive({
    count: 0,
    // Funzione per recuperare il conteggio iniziale dal backend
    async refreshCount() {
        try {
            const response = await api.get('/api/v1/orders/cart-items');
            // Calcoliamo la somma delle quantità di tutti i prodotti nel carrello
            const items = response.data || [];
            this.count = items.reduce((total, item) => total + item.quantity, 0);
        } catch (err) {
            console.error("Errore nel recupero del carrello:", err);
            this.count = 0;
        }
    }
});