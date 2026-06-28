package com.antoniofaccioli.fishing.ecommerce_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Product")
@Table(name = "product")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "description" , columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "price")
    private double price;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "discount")
    private Integer discount; // percetuale offerta (20 per 20% off)

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producer_id")
    private Producer producer;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "num_purchases")
    private Integer numPurchases; // numero di acquisti del prodotto, utile per ordinare i prodotti più venduti

    public double getFinalPrice() {
        if (discount != null && discount > 0) {
            double discountAmount = (price * discount) / 100.0;
            double discountedPrice = BigDecimal.valueOf(price - discountAmount)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            return discountedPrice;
        } else
            return price;
    }//getFinalPrice

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return this.id.equals(p.getId());
    }//equals

    @Override
    public int hashCode(){
        final int prime = 41;
        int h = 1;
        for(int i = 0; i < getName().length(); i++)
            h *= prime;
        return h;
    }//hashCode
}
