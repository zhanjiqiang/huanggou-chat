# 构建阶段
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建数据目录
RUN mkdir -p /app/data

# 从构建阶段复制jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 3001

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动命令
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
