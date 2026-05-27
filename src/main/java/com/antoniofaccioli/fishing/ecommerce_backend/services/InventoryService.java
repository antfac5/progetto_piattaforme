package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProductRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    public Product updateProductQuantityInIntentory(Long productId, Integer quantity){
        try{
            Product product = productRepository.findById(productId).orElseThrow(()-> new CustomException("Prodotto non trovato."));
            int newQuantity = product.getQuantity() + quantity;
            if (newQuantity < 0) throw new CustomException("Quantità non valida!");
            product.setQuantity(newQuantity);
            return productRepository.save(product);
        }catch(ObjectOptimisticLockingFailureException e){
            throw new CustomException("Operazione fallita. Il prodotto è stato modificato o aggiornato da un altro utente. Si prega di riprovare.");
        }
    }

    public Product incrementNumPurchases(Long productId){
        try{
            Product product = productRepository.findById(productId).orElseThrow(()-> new CustomException("Prodotto non trovato."));
            product.setNumPurchases(product.getNumPurchases()+1);
            return productRepository.save(product);
        }catch(ObjectOptimisticLockingFailureException e){
            throw new CustomException("Operazione fallita. Il prodotto è stato modificato o aggiornato da un altro utente. Si prega di riprovare.");
        }
    }

}
