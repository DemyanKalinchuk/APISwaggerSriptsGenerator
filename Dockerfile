FROM eclipse-temurin:11-jdk
WORKDIR /app

COPY . /app
RUN ./mvnw clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*jar && curl -X GET 'http://localhost:8080/api/k6/generate?https://new-api.maps.itsrv.xyz/v1-api-swagger=https://petstore3.swagger.io/api/v3/openapi.json&bearerToken=DGPu5_xebwt6hn-pCSrUSBZOHLt5cCOS6synrMioOYsnPdL46YQk-MvpopNsM7I4'"]
