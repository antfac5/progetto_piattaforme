package com.antoniofaccioli.fishing.ecommerce_backend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
//questa classe rappresenta la chiave primaria composta per la tabella di associazione OrderBook, che collega un ordine
// a un libro specifico. Contiene due campi: order e book, entrambi sono chiavi esterne che fanno riferimento
// rispettivamente alla tabella degli ordini e alla tabella dei libri. La classe implementa Serializable per consentire
// l'uso come chiave primaria in JPA.
public class OrderProductPK implements Serializable {

    @ManyToOne(optional = false, fetch = FetchType.LAZY) //specifica che questa relazione è obbligatoria (optional = false) e che i dati associati a questa relazione devono essere caricati solo quando vengono effettivamente richiesti (fetch = FetchType.LAZY).
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference //gestisce la serializzazione JSON e previene problemi di riferimento circolare quando si serializzano oggetti che hanno relazioni bidirezionali. In questo caso, indica che questa parte della relazione è quella "indietro" (back) e che l'altra parte (in Order) sarà quella "gestita" (managed).
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Override
    public int hashCode() {
        final int prime = 31;
        int h = 1;
        h = prime * h + ((order == null) ? 0 : order.hashCode());
        h = prime * h + ((product == null) ? 0 : product.hashCode());
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof OrderProductPK)) return false;
        OrderProductPK oppk = (OrderProductPK) o;
        if (order == null) {
            if (oppk.order != null) return false;
        } else if (!order.equals(oppk.order)) return false;
        if (product == null) {
            if (oppk.product != null) return false;
        } else if (!product.equals(oppk.product)) return false;
        return true;
    }

}
