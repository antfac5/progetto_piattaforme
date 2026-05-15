package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Producer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

}
