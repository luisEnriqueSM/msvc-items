# msvc-items

# Comandos Docker para levantar el contenedor de msvc-items

```bash
# Limpiar, generar Jar file y omitir tests
.\mvnw clean package -DskipTests
 
 # Construir imagen
docker build -t msvc-items:v1 .

# Correr contenedor
docker run -d -p 8005:8005 --name msvc-items --network springcloud msvc-items:v1
```

## Componentes de relacionados de la solución:

- ### msvc-eureka-server: https://github.com/luisEnriqueSM/msvc-eureka-server
- ### msvc-products: https://github.com/luisEnriqueSM/msvc-products
- ### msvc-gateway-server: https://github.com/luisEnriqueSM/msvc-gateway-server
- ### msvc-oauth: https://github.com/luisEnriqueSM/msvc-oauth
- ### msvc-users: https://github.com/luisEnriqueSM/msvc-users
- ### msvc-config-server: https://github.com/luisEnriqueSM/msvc-config-server
- ### msvc-docker-compose: https://github.com/luisEnriqueSM/msvc-docker-compose
