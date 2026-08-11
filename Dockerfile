FROM eclipse-temurin:11-jre as builder
WORKDIR /app

# Use Maven to build
COPY pom.xml mvnw .
COPY .mvn .mvn
COPY src src

RUN ./mvnw -B -DskipTests package -DskipITs

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /app/target/gekko-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
