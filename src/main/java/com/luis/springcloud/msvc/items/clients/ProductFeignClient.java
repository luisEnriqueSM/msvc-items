package com.luis.springcloud.msvc.items.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.luis.springcloud.msvc.items.models.Product;

@FeignClient(name = "msvc-products")
public interface ProductFeignClient {

    @GetMapping
    public List<Product> findAll();

    @GetMapping("/{id}")
    public Product details(@PathVariable Long id);
}
