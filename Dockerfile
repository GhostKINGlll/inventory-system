# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先复制 pom，利用 Maven 缓存加速后续构建
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区设置为东八区
ENV TZ=Asia/Shanghai

COPY --from=build /app/target/inventory-system-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
