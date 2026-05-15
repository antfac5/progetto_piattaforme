package com.antoniofaccioli.fishing.ecommerce_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Producer")
@Table(name = "producer")
public class Producer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Producer)) return false;
        Producer producer = (Producer) o;
        return id.equals(producer.getId());
    }//equals

    @Override
    public int hashCode() {
        final int prime = 31;
        int h = 0;
        for(int i = 0; i < getName().length(); i++)
            h *= prime;
        return h;
    }//hashCode
}
