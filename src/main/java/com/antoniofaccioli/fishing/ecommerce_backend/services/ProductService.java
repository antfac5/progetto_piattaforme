package com.antoniofaccioli.fishing.ecommerce_backend.services;

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

    @Transactional
    public Product addNewProduct(@NotNull Product product) {
        // controllo della presenza di produttore e categoria
        if((product.getProducer().getId() == null && product.getProducer().getName() == null)
                || (product.getCategory().getId() == null && product.getCategory().getName() == null))
            throw new CustomException("Il prodotto deve avere un produttore e una categoria associati.");

        //controllo dell'esistenza del produttore, se non esiste viene creato
        if( product.getProducer().getId() != null )
            producerRepository.findById(product.getProducer().getId())
                    .orElseGet(
                            () -> producerRepository.save(product.getProducer())
                    );
        else producerRepository.save(product.getProducer()); // viene creato un nuovo produttore, viene generato l'id
        product.setProducer(product.getProducer()); // // Assegna il produttore salvato (ora con ID generato)

        //controllo dell'esistenza della categoria, se non esiste viene creata
        if( product.getCategory().getId() != null )
            categoryRepository.findById(product.getCategory().getId())
                    .orElseGet(
                            () -> categoryRepository.save(product.getCategory())
                    );
        else categoryRepository.save(product.getCategory());
        product.setCategory(product.getCategory()); // Assegna la categoria salvata (ora con ID generato)
        //salvataggio del prodotto
        return productRepository.save(product);
    }

    public List<Product> getAllProductsOfProducer(Long producerId) {
        return productRepository.findProductsByProducerId(producerId);
    }

    public List<Product> getAllDiscountedProducts() {
        return productRepository.findAllProducts();
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
        if(product.getProducer().getId() == null) {
            // Il nuovo prodotto non possiede l'id del produttore
            if (product.getProducer().getName() == null) product.setProducer(exist.getProducer()); // non avendo nemmeno il nome del produttore, inserisco quello del prodotto precedente alla modifica
            else producerRepository.save(product.getProducer()); // avendo almeno il nome, lo registro come nuovo produttore
        }
        else{
            // Il nuovo prodotto possiede l'id del produttore
            // controllo che esista
            producerRepository.findById(product.getProducer().getId()).orElseGet(
                    () -> producerRepository.save(product.getProducer())
            );
        }
        if (product.getCategory().getId() == null){
            // Il nuovo prodotto non possiede l'id della categoria
            if(product.getCategory().getName() == null) product.setCategory(exist.getCategory()); // non avendo nemmeno il nome della categoria, inserisco quella del prodotto precedente alla modifica
            else categoryRepository.save(product.getCategory()); // avendo almeno il nome, la registro come nuova categoria
        }
        else{
            // Il nuovo prodotto possiede l'id della categoria
            // controllo che esista
            categoryRepository.findById(product.getCategory().getId()).orElseGet(
                    () -> categoryRepository.save(product.getCategory())
            );
        }
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
