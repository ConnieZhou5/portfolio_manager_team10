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


