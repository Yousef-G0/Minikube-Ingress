# Simple Java App

This is a simple Spring Boot application that exposes a GET API at `/` that returns "hello [name]". The name is read from an environment variable called `YOUR_NAME`.

## Running the application

### Prerequisites
- Java 17
- Maven

## Health Endpoint
This application exposes a health endpoint at `/actuator/health`. You can use this endpoint to monitor the health of the application.

## Docker

### Build the Docker image
```bash
docker build -t simple-java-app .
```

### Run the Docker container
```bash
docker run -p 8080:8080 -e YOUR_NAME="your name" simple-java-app
```
