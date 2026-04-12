# ASPAS Backend

## Project Overview

**ASPAS** (Auto Parts Stock and Analysis System) is a comprehensive backend service for managing spare parts inventory, vendor relationships, orders, and sales analytics. The system is designed to track auto parts inventory across multiple storage locations, manage vendor relationships, process orders, and generate detailed sales and revenue reports.

## Features

- **Inventory Management**: Track spare parts across multiple storage racks with real-time stock monitoring
- **Vendor Management**: Maintain vendor relationships with multi-vendor support for parts
- **Order Processing**: Create and manage customer orders with inventory validation
- **Sales Tracking**: Record and monitor sales transactions
- **JIT Inventory**: Just-In-Time inventory calculations and recommendations
- **Reporting**: Generate daily revenue and monthly graph reports
- **Scheduled Tasks**: Automated end-of-day processing
- **Global Exception Handling**: Centralized error handling across the application

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Build Tool**: Maven
- **Relational Database**: MySQL / MariaDB
- **NoSQL Database**: MongoDB
- **Data Access**: JPA (Hibernate) + Spring Data MongoDB
- **API**: RESTful Web Services
- **Scheduling**: Spring Scheduling

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or MariaDB
- MongoDB 5.0+

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd aspas-backend
```

### 2. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/aspas_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/aspas_reports
spring.data.mongodb.database=aspas_reports

# Server Configuration
server.port=8080
```

### 3. Create MySQL Database

```sql
CREATE DATABASE IF NOT EXISTS aspas_db;
USE aspas_db;
```

The schema will be automatically created by Hibernate on first run, or manually run `schema.sql` if needed.

### 4. Initialize Sample Data

The project includes sample data for storage racks, vendors, spare parts, and relationships. Contact the development team for seed data scripts.

### 5. Build the Project

```bash
mvn clean install
```

### 6. Run the Application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

## Project Structure

```
aspas-backend/
├── src/main/java/com/aspas/
│   ├── AspasApplication.java           # Spring Boot entry point
│   ├── config/
│   │   └── SchedulerConfig.java        # Scheduler configuration
│   ├── model/
│   │   ├── document/                   # MongoDB documents
│   │   │   ├── DailyRevenueReportDoc.java
│   │   │   ├── MonthlyGraphReportDoc.java
│   │   │   └── SalesTransactionDoc.java
│   │   ├── dto/                        # Data Transfer Objects
│   │   │   ├── OrderResponseDTO.java
│   │   │   ├── ReportRequestDTO.java
│   │   │   ├── ReportResponseDTO.java
│   │   │   ├── SaleRequestDTO.java
│   │   │   └── SaleResponseDTO.java
│   │   ├── entity/                     # JPA Entities
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderList.java
│   │   │   ├── SparePart.java
│   │   │   ├── StorageRack.java
│   │   │   └── Vendor.java
│   │   └── interfaces/
│   │       └── Printable.java          # Marker interface
│   ├── controller/                     # REST Controllers
│   │   ├── SaleController.java
│   │   ├── OrderController.java
│   │   ├── ReportController.java
│   │   ├── InventoryController.java
│   │   └── VendorController.java
│   ├── service/                        # Business Logic
│   │   ├── SaleService.java
│   │   ├── JITService.java
│   │   ├── OrderService.java
│   │   ├── ReportService.java
│   │   ├── InventoryService.java
│   │   ├── VendorService.java
│   │   └── SystemControllerService.java
│   ├── repository/                     # Data Access Layer
│   │   ├── jpa/
│   │   │   ├── OrderItemRepository.java
│   │   │   ├── OrderListRepository.java
│   │   │   ├── SparePartRepository.java
│   │   │   ├── StorageRackRepository.java
│   │   │   └── VendorRepository.java
│   │   └── mongo/
│   │       ├── DailyRevenueReportRepository.java
│   │       ├── MonthlyGraphReportRepository.java
│   │       └── SalesTransactionRepository.java
│   ├── scheduler/                      # Scheduled Tasks
│   │   └── EndOfDayScheduler.java
│   ├── exception/                      # Exception Handling
│   │   ├── PartNotFoundException.java
│   │   ├── InsufficientStockException.java
│   │   ├── VendorNotFoundException.java
│   │   ├── OrderNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   └── util/                           # Utility Classes
│       ├── JITCalculator.java
│       └── OrderFormatter.java
├── src/main/resources/
│   ├── application.properties           # Configuration
│   └── schema.sql                       # Database schema
├── target/                              # Compiled output
└── pom.xml                              # Maven configuration
```

## Database Schema Overview

### Core Entities

- **StorageRack**: Physical storage locations for parts
- **Vendor**: Supplier information
- **SparePart**: Individual spare part inventory items
- **OrderList**: Customer orders
- **OrderItem**: Individual items within an order

### Reporting Collections (MongoDB)

- **DailyRevenueReport**: Daily sales revenue aggregates
- **MonthlyGraphReport**: Monthly trends for visualization
- **SalesTransaction**: Detailed transaction records

## API Endpoints (Overview)

### Sales Management
- `POST /api/sales` - Record a sale
- `GET /api/sales/{id}` - Get sale details
- `GET /api/sales` - List sales

### Order Management
- `POST /api/orders` - Create an order
- `GET /api/orders/{id}` - Get order details
- `GET /api/orders` - List orders
- `PATCH /api/orders/{id}` - Update order status

### Inventory Management
- `GET /api/inventory/parts` - List all spare parts
- `GET /api/inventory/parts/{id}` - Get part details
- `PATCH /api/inventory/parts/{id}` - Update stock
- `GET /api/inventory/racks` - List storage racks

### Vendor Management
- `GET /api/vendors` - List vendors
- `GET /api/vendors/{id}` - Get vendor details
- `POST /api/vendors` - Add vendor
- `PUT /api/vendors/{id}` - Update vendor

### Reports
- `GET /api/reports/daily-revenue` - Daily revenue report
- `GET /api/reports/monthly-graph` - Monthly trends report
- `GET /api/reports/sales-transactions` - Transaction history

## Scheduled Jobs

### End-of-Day Scheduler
- **Cron**: `0 0 0 * * ?` (Runs daily at midnight)
- **Purpose**: Process end-of-day reports, reset daily counters, perform cleanup

## Exception Handling

The application uses a centralized global exception handler that provides consistent error responses:

- `PartNotFoundException` (404): Spare part not found
- `InsufficientStockException` (400): Not enough inventory
- `VendorNotFoundException` (404): Vendor not found
- `OrderNotFoundException` (404): Order not found
- Generic exceptions return 500 Internal Server Error

## Utilities

- **JITCalculator**: Computes Just-In-Time inventory levels and reorder points
- **OrderFormatter**: Formats order data for display and export

## Building for Production

```bash
mvn clean package
java -jar target/aspas-backend-1.0.0.jar
```

## Configuration for Different Environments

### Development
```properties
spring.jpa.show-sql=true
logging.level.root=INFO
```

### Production
```properties
spring.jpa.show-sql=false
logging.level.root=WARN
server.compression.enabled=true
```

## Contributing

1. Follow Spring Boot and Java coding conventions
2. Ensure all new features have corresponding unit tests
3. Update this README for significant changes
4. Create a feature branch from `main` before making changes

## Future Enhancements

- [ ] Authentication & Authorization (Spring Security)
- [ ] API Documentation (Swagger/SpringDoc)
- [ ] Email notifications for low stock
- [ ] Dashboard with real-time inventory visualization
- [ ] Advanced analytics and forecasting
- [ ] Integration with shipping carriers
- [ ] Mobile app support

## Troubleshooting

### MySQL Connection Issues
- Verify MySQL is running: `mysql -u root -p`
- Check credentials in `application.properties`
- Ensure database `aspas_db` exists

### MongoDB Connection Issues
- Verify MongoDB is running: `mongosh`
- Check connection URI in `application.properties`
- Ensure `aspas_reports` database permissions

### Build Failures
- Clear Maven cache: `mvn clean`
- Update dependencies: `mvn dependency:resolve`
- Check Java version: `java -version` (must be 17+)

## Support

For issues, questions, or contributions, please contact the development team.

---

**Last Updated**: March 2026  
**Version**: 1.0.0  
**Status**: Active Development
