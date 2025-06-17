FROM openjdk:23
WORKDIR /app
COPY target/scrapper-1.0.jar /app/scrapper.jar
EXPOSE 8081
EXPOSE 8001
CMD ["java", "-jar", "scrapper.jar"]
