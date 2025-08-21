# 1-Bosqich: Dasturni kompilyatsiya qilish uchun Maven imijini ishlatamiz
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2-Bosqich: Faqat Java o'rnatilgan kichikroq imijdan foydalanamiz
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Kompilyatsiya qilingan JAR faylni birinchi bosqichdan nusxalab olamiz
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]