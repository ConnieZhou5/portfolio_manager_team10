# 💼 PortfolioManager
An application to manage a financial portfolio. 
Final project for the computer science foundation training.
This is part of TAP training 2025.

## 🚀 Tech Stack

- **Frontend**: React, Axios, Tailwind CSS
- **Backend**: Java + Springboot
- **Database**: MySQL
- **Financial Data**: Yahoo Finance API / Cached Sample API
- **Charts**: Chart.js 
- **API Docs**: Swagger

## 📦 Features

- Add/remove portfolio items (e.g., stocks, bonds)
- View current portfolio and total value
- Visualize performance using charts

## 👥 Team

- Connie Zhou
- Mya Thanegi Soe
- Beatriz de Carvalho Pacheco Lourenco

<br><br>

# 🛠️ Getting Started

### Database Setup (You may skip step 1-3 if you are Neueda instructors)

1. **Install MySQL if not yet installed:**
   - Download and install from: https://dev.mysql.com/downloads/mysql/
   - During setup, set:
     - **Username**: `root`
     - **Password**: `mspm123!` (or choose your own, but update the Java code)

2. **Verify MySQL is Running:**
   - Start MySQL service. You can test it via terminal:
     ```bash
     mysql -u root -p
     ```

3. **Install Java & Maven if not yet installed:**
   - Install Java 17+ from: https://adoptopenjdk.net/
   - Install Maven from: https://maven.apache.org/download.cgi
   You can test it using:
   ```bash
   java -version
   mvn -v
   ```

4. **Create the Database:**
   - Run the Java program that creates the `portfolio_db` database.
    ```bash
    cd backend
    mvn compile exec:java -Dexec.mainClass="com.portfolio.backend.db.CreateDatabase"
    ```

### Backend Setup
   - To start the Spring Boot backend server:
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   Once started, the backend will be accessible at:
   ```bash
   http://localhost:8080
   ```

### Frontend Setup
   - Run the following from inside the `frontend/` folder:
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Once started, the app will open at:
   ```bash
   http://localhost:3000
   ```

<br><br>

### 🧪 API Testing (with REST Client in VS Code)
   - We use the REST Client extension in VS Code to test the API using the portfolio-api.http file.
   - Make sure your Spring Boot app is running on localhost:8080
   ```
   Open portfolio-api.http in VS Code
   Hover over any GET or POST request line
   Click "Send Request" to execute the call and see the response inline
   ```

### 📘 API Documentation (Swagger UI that help view all API endpoints)
   - We use **Swagger/OpenAPI** to automatically generate interactive API documentation.
   - Start the backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   - Open browser and go to:
   http://localhost:8080/swagger-ui/index.html

