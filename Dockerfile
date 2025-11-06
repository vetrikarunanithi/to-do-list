# Use an official OpenJDK image compatible with Render
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Compile the Java source
RUN javac src/Main.java

# Expose port (Render will assign its own port)
EXPOSE 8080

# Run the application
CMD ["java", "-cp", "src", "Main"]
