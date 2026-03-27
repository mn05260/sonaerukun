# --- 第1段階：組み立て ---
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

# 1. 今見えているもの全部をDockerの中にコピー
COPY . .

# 2. 【重要】sonaerukunフォルダに入ってからビルドする
RUN cd sonaerukun && mvn clean package -DskipTests

# --- 第2段階：実行 ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# 3. ビルドされた場所（sonaerukun/target/）から jar を持ってくる
COPY --from=build /app/sonaerukun/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]