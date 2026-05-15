package com.antoniofaccioli.fishing.ecommerce_backend.repositories;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
