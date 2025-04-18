package com.luis.springcloud.msvc.items.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.luis.springcloud.msvc.items.models.Item;
import com.luis.springcloud.msvc.items.models.Product;
import com.luis.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RefreshScope
@RestController
public class ItemController {

    private final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final CircuitBreakerFactory cBreakerFactory;

    @Value("${configuracion.texto}")
    private String text;

    @Autowired
    private Environment env;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService itemService, 
            CircuitBreakerFactory cBreakerFactory) {
        this.itemService = itemService;
        this.cBreakerFactory = cBreakerFactory;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port){
        Map<String, String> json = new HashMap<>();
        json.put("text", this.text);
        json.put("port", port);
        logger.info(port);
        logger.info(text);
        if(env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")){
            json.put("autor.nombre", env.getProperty("configuracion.autor.nombre"));
            json.put("autor.email", env.getProperty("configuracion.autor.email"));
        }
        return ResponseEntity.ok(json);
    }

    @GetMapping
    public ResponseEntity<List<Item>> list(@RequestParam(name = "name", required = false) String name, 
            @RequestHeader(name = "token-request", required = false) String token){

        logger.info("Llamada a metodo del controller ItemController::list()");
        // ToDo quitar los parametros        
        logger.info("Request Parameter: {}", name);
        logger.info("Token: {}", token);
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

    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> details2(@PathVariable Long id){
        Optional<Item> iteOptional =  this.itemService.findById(id);

        if(iteOptional.isPresent()){
            return ResponseEntity.ok(iteOptional.get());
        }

        return ResponseEntity.status(404)
            .body(Collections.singletonMap("message", "No existe el producto en el microservicio msvc-products"));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct2")
    @TimeLimiter(name = "items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> details3(@PathVariable Long id){
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> iteOptional =  this.itemService.findById(id);

            if(iteOptional.isPresent()){
                return ResponseEntity.ok(iteOptional.get());
            }
    
            return ResponseEntity.status(404)
                .body(Collections.singletonMap("message", "No existe el producto en el microservicio msvc-products"));
        });
    }

    public ResponseEntity<?> getFallBackMethodProduct(Throwable e){
        // camino alternativo cuando se abre el circuito
        logger.error(e.getMessage());
        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camara Sony");
        product.setPrice(500D);
        return ResponseEntity.ok(new Item(product, 5));
    }

    public CompletableFuture<?> getFallBackMethodProduct2(Throwable e){
        return CompletableFuture.supplyAsync(() -> {
            // camino alternativo cuando se abre el circuito
            logger.error(e.getMessage());
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony");
            product.setPrice(500D);
            return ResponseEntity.ok(new Item(product, 5));
        });
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product){
        logger.info("Product creando: {}", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.itemService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@RequestBody Product product, @PathVariable Long id){
        logger.info("Product actualizando: {}", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.itemService.update(product, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        logger.info("Product eliminado con id: {}", id);
        this.itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
