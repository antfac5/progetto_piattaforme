package com.antoniofaccioli.fishing.ecommerce_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "order_product")
//questa classe rappresenta la relazione molti a molti tra Order e Product, con l'aggiunta di un campo quantity per
// indicare la quantità di quel prodotto in quell'ordine
public class OrderProduct {
    @EmbeddedId
    @JsonIgnore
    private OrderProductPK pk;

    @Column(nullable = false)
    private Integer quantity;

    public OrderProduct( Order order, Product product, Integer quantity ) {
        pk = new OrderProductPK();
        pk.setOrder(order);
        pk.setProduct(product);
        this.quantity = quantity;
    }

    public OrderProduct() {}

    @Transient // serve per indicare che questo metodo non deve essere considerato come una proprietà persistente dell'entità, ma è solo un metodo di convenienza per accedere al prodotto associato a questa relazione.
    public Product getProduct() {
        return this.pk.getProduct();
    }

    @Transient
    public Double getProductFinalPrice(){
        return getProduct().getFinalPrice() * getQuantity();
    }
}
