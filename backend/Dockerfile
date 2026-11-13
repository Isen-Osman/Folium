# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s "$(dirname "$(find /usr/share -name eng.traineddata | head -n 1)")" /opt/tessdata

ENV TESSDATA_PREFIX=/opt/tessdata

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
