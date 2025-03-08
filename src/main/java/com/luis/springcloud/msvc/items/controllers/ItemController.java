package com.luis.springcloud.msvc.items.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.luis.springcloud.msvc.items.models.Item;
import com.luis.springcloud.msvc.items.services.ItemService;

@RestController
public class ItemController {

    private final ItemService itemService;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> list(){
        return ResponseEntity.ok(this.itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id){
        Optional<Item> iteOptional = this.itemService.findById(id);
        if(iteOptional.isPresent()){
            return ResponseEntity.ok(iteOptional.get());
        }
        return ResponseEntity.status(404)
            .body(Collections.singletonMap("message", "No existe el producto en el microservicio msvc-products"));
    }
}
