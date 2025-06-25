# **K6 Script Generator from Swagger (OpenAPI)**

**_This Java-based tool automatically generates a complete k6 performance testing script using a Swagger/OpenAPI definition URL. It supports modern Java features, REST endpoints, and dynamic script generation with error checks, tag grouping, and token-based authentication.**_

* ## **_🚀 Features
*     🔗 Connects to a Swagger URL and parses OpenAPI 3.x definitions
*     🔐 Supports Bearer token authentication
*     📄 Generates dynamic JS scripts for k6
*     📦 Includes all HTTP methods: GET, POST, PUT, DELETE
*     📂 Groups endpoints by Swagger tags
*     🧪 Adds dynamic response status checks (200, 201, 204, 404)
*     🧠 Builds request bodies from Swagger schemas
*     🔎 Adds query params dynamically
*     🧾 Opens and saves .txt file in ~/Documents/scripts_**
    
## **📁 Project Structure

    src/main/java
    └── com.example.service
    └── K6ScriptGeneratorService.java**

## *** **🛠 Requirements**
*     Java 11
*     Maven
*     Internet connection (to access Swagger JSON)**

## *** ⚙️ Setup Instructions**
*     Clone the repo
*     Navigate to the root directory
*     Build the project:
*     mvn clean install

## *** Run the generator manually:**
    K6ScriptGeneratorService service = new K6ScriptGeneratorService(new ObjectMapper());
    service.generateK6ScriptFromSwagger("https://example.com/swagger.json", "your-bearer-token");

## ***     **Generated script path:****
*     ~/Documents/scripts/generatedK6Script.txt

# **🧪 How It Works**
## **Step-by-Step Flow:**

1. Connects to Swagger URL and retrieves OpenAPI JSON
3. Parses paths and tags into logical groups
5. Generates k6 group() sections for each tag
7. For each endpoint:
9. Parses HTTP method
11. Replaces {company} param with unicorn if base URL includes new-api.maps.itsrv.xyz
13. Detects and appends query parameters
15. Generates request body from Swagger schema
17. Adds k6 checks:
19. Status matches Swagger spec (first status in response block)
21. Not 404
23. Response body exists
25. Response time < 500ms
27. Writes script into a .txt file and opens it

## *** ✅ Sample k6 Features Used**
*     group() for organizing by tag
*     http.get/post/put/delete()
*     check() for asserting status code, body, and latency
*     textSummary() for clean CLI output

## *** 🧪 Testing**
*     To test the service, use JUnit and Mockito:
*     Mock Request.get(...).execute() to return custom Swagger JSON
*     Validate correct grouping, status checks, and generated file output

## ** 🐳 Docker Support**
*     To run this in Docker:
*     Dockerfile:
*     FROM openjdk:11-jdk
*     COPY target/*.jar app.jar
*     ENTRYPOINT ["java", "-jar", "/app.jar"]
*     Volume Mount & Auto Execution:
*     docker run -v ~/Documents/scripts:/root/scripts my-k6-generator**

* ## **🧠 Future Improvements**
*     Support multipart/form-data and formUrlEncoded
*     Smart example data generation
*     CLI integration for triggering from terminal
*     Export to .js or .k6.js instead of .txt
*     CI/CD integration