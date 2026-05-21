package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.entities.Producer;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.Product;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.CategoryRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProducerRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.ProductRepository;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProducerRepository producerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /*@Autowired
    private*/

    @Transactional
    public Product addNewProduct(@NotNull Product product) {
        if( product.getProducer() == null || product.getCategory() == null )
            throw new IllegalArgumentException("Il prodotto deve avere un produttore e una categoria associati.");
        //controllo dell'esistenza del produttore e della categoria, se non esistono vengono creati
        producerRepository.findById(product.getProducer().getId())
                .orElse(producerRepository.save( product.getProducer())
                );
        categoryRepository.findById(product.getCategory().getId())
                .orElse(
                        categoryRepository.save(product.getCategory())
                );
        //salvataggio del prodotto
        return productRepository.save(product);
    }

    public List<Product> getAllProductsOfProducer(Long producerId) {
        return productRepository.findProductsByProducerId(producerId);
    }

    public List<Product> getAllDiscountedProducts() {
        return productRepository.findAllProducts();
    }

     public List<Product> getAllBestSellingProducts() {
        return productRepository.findAllProductsByNumPurchasesIsNotNull();
    }

    public List<Product> getBestSellingProducts() {
        List<Product> bestSellingProducts = productRepository.findAllProductsByNumPurchasesIsNotNull();
        bestSellingProducts.sort(Comparator.comparingInt(Product::getNumPurchases).reversed());
        if(bestSellingProducts.size() > 10)
            bestSellingProducts = bestSellingProducts.subList(0, 9); //restituisce solo i primi 10 prodotti più venduti
        return bestSellingProducts;
    }

    @Transactional
    public Product updateProduct(@NotNull Product product) {
        Product exist = productRepository.findById( product.getId() ).orElseThrow( ()-> new IllegalArgumentException("Prodotto non trovato."));
        //controllo dell'esistenza del produttore e della categoria, se non esistono vengono creati
        if( product.getProducer().getId() == null )
            product.setProducer(exist.getProducer());
        if ( product.getCategory().getId() == null )
            product.setCategory(exist.getCategory());
        return productRepository.save(product);
    }

    @Transactional
    public String deleteProduct(@NotNull Long productId) {
        Product exist = productRepository.findById( productId ).orElseThrow( ()-> new IllegalArgumentException("Prodotto non trovato."));
        productRepository.delete(exist);
        return "Il prodotto con id " + productId + "("+ exist.getName() + ", "+ exist.getProducer() +
                ") è stato eliminato con successo.";
    }

    // Metodo per ottenere una pagina di prodotti con ordinamento personalizzato
    public Page<Product> getProducts(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching products for page {} of size {}", page, size);
        return productRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy)));
    }

    public Product getProductById(Long productId) { return productRepository.findById(productId).orElseThrow( ()-> new IllegalArgumentException("Prodotto non trovato.")); }

    // Metodo che restituisce una pagina di prodotti filtrati per categorie il cui id è contenuto in categoryIds
    public Page<Product> getAllProductsOfCategory(List<Long> categoryIds) {
        Pageable pageable =  PageRequest.of(0, 12);
        return productRepository.findProductsByCategoryIdIn(categoryIds, pageable);
    }

}
