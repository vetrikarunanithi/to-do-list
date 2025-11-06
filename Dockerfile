# Use an official lightweight OpenJDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy all files into the container
COPY . .

# Compile the Java source code
RUN javac src/Main.java

# Expose the port (Render will override this, but good practice)
EXPOSE 8080

# Run the Java application
CMD ["java", "-cp", "src", "Main"]
