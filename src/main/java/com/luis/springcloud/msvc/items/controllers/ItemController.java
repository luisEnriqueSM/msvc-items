package com.luis.springcloud.msvc.items.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.luis.springcloud.msvc.items.models.Item;
import com.luis.springcloud.msvc.items.models.Product;
import com.luis.springcloud.msvc.items.services.ItemService;

@RestController
public class ItemController {

    private final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final CircuitBreakerFactory cBreakerFactory;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService itemService, 
            CircuitBreakerFactory cBreakerFactory) {
        this.itemService = itemService;
        this.cBreakerFactory = cBreakerFactory;
    }

    @GetMapping
    public ResponseEntity<List<Item>> list(@RequestParam(name = "name", required = false) String name, 
            @RequestHeader(name = "token-request", required = false) String token){
        // ToDo quitar los parametros        
        System.out.println(name);
        System.out.println(token);
        return ResponseEntity.ok(this.itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id){
        Optional<Item> iteOptional = this.cBreakerFactory.create("items").run(() -> this.itemService.findById(id), e -> {
            // camino alternativo cuando se abre el circuito
            logger.error(e.getMessage());
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony");
            product.setPrice(500D);
            return Optional.of(new Item(product, 5));
        });

        if(iteOptional.isPresent()){
            return ResponseEntity.ok(iteOptional.get());
        }

        return ResponseEntity.status(404)
            .body(Collections.singletonMap("message", "No existe el producto en el microservicio msvc-products"));
    }
}
