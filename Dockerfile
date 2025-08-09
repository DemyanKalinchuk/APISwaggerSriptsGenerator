FROM eclipse-temurin:21
WORKDIR /app

COPY . /app
RUN ./mvnw clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*jar && curl -X GET 'http://localhost:8080/api/k6/generate?https://url=https://petstore3.swagger.io/api/v3/openapi.json&bearerToken='"]
