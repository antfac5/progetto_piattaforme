package com.antoniofaccioli.fishing.ecommerce_backend.support.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// questa classe serve a rappresentare i dati necessari per creare un ordine, inclusi i dettagli del destinatario e la lista dei prodotti ordinati
public class OrderForm {
    private String recipientName; // nome del destinatario dell'ordine
    private String shippingAddress; // indirizzo di spedizione dell'ordine
    private String phoneNumber; // numero di telefono del destinatario dell'ordine
}
