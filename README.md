# GenSoftrix — Customizable Management Software Generator

GenSoftrix is a modular desktop management software generator built with **Java 17**, **Java Swing**, **JDBC**, and **MySQL**. It allows users to design and run custom data management applications (such as Inventory Management, Student Management, Employee Management, or Project Management) without writing SQL or GUI code for each domain.

## 1. Prerequisites

- **Java Development Kit (JDK)**: Version 17 or newer (`java -version`)
- **MySQL Server**: Version 8.0 or newer (`mysql --version`)
- **Apache Maven**: Version 3.8+ (or built-in Maven support in IntelliJ IDEA, Eclipse, or NetBeans)

## 2. Database Setup

1. Start your local MySQL server.
2. Execute the provided schema and seed script:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```
   *(Note: `DatabaseConnection.java` also automatically initializes the `gensoftrix` database and tables on startup if they do not exist.)*
3. Configure your database credentials in `src/main/resources/db.properties` or via environment variables:
   - `GENSOFTRIX_DB_URL` (default: `jdbc:mysql://localhost:3306/gensoftrix?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
   - `GENSOFTRIX_DB_USER` (default: `root`)
   - `GENSOFTRIX_DB_PASSWORD` (default: `root`)

## 3. Running in an IDE (IntelliJ IDEA / Eclipse / NetBeans)

1. Open the `gensoftrix` folder as an existing **Maven Project**.
2. Allow Maven to download `mysql-connector-j` and `org.json`.
3. Locate `src/main/java/com/gensoftrix/Main.java`.
4. Run `Main.main(String[] args)`.

## 4. Building & Running via Command Line

```bash
cd gensoftrix
mvn clean package
java -jar target/gensoftrix-1.0.0.jar
```

## 5. Default Demo Credentials

- **Username**: `admin`
- **Password**: `password123`
- You can also register a new account from the **Register** screen.
