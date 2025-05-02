# msvc-items

# Comandos Docker para levantar el contenedor de msvc-items

```bash
# Limpiar, generar Jar file y omitir tests
.\mvnw clean package -DskipTests
 
 # Construir imagen
docker build -t msvc-items:v1 .

# Correr contenedor
docker run -d -p 8005:8005 --name msvc-items --network springcloud msvc-items:v1