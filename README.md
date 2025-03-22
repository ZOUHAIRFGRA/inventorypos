# Inventory POS System

A desktop Point of Sale (POS) system for inventory management built with Spring Boot and JavaFX. This application provides a comprehensive solution for managing inventory, sales, and business operations.

## Features

- **User Role Management**
  - Owner/Admin access
  - Cashier access
  - Role-based access control

- **Product Management**
  - Add, edit, and delete products
  - Image support for products
  - Category management
  - Stock tracking

- **Sales Management**
  - Point of Sale interface for cashiers
  - Shopping cart functionality
  - Multiple payment methods
  - Sales history tracking
  - Receipt generation (PDF)

- **Invoice Management**
  - Generate and manage invoices
  - PDF invoice generation
  - Invoice history tracking

- **Business Settings**
  - Customize business information
  - Logo management
  - Backup and restore functionality

## Technology Stack

- Java 21
- Spring Boot 3.4.3
- JavaFX
- MaterialFX (Modern UI components)
- H2 Database
- iText 7 (PDF generation)
- Maven

## Prerequisites

- Java Development Kit (JDK) 21 or higher
- Maven 3.6 or higher

## Installation

1. Clone the repository:
```bash
git clone https://github.com/ZOUHAIRFGRA/inventorypos.git
```

2. Navigate to the project directory:
```bash
cd inventorypos
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## Default Credentials

- **Admin Account**
  - Username: admin
  - Password: admin123

## Project Structure

- `src/main/java/com/fouiguira/pos/inventorypos/`
  - `controllers/` - JavaFX controllers
  - `entities/` - JPA entities
  - `repositories/` - Spring Data repositories
  - `services/` - Business logic
  - `config/` - Configuration classes
  - `utils/` - Utility classes

- `src/main/resources/`
  - `view/` - FXML files
  - `styles/` - CSS files
  - `images/` - Image resources
  - `fonts/` - Custom fonts

## Database Configuration

The application uses H2 database by default. Database configuration can be found in `application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./inventory
spring.datasource.username=sa
spring.datasource.password=
```

## Features in Detail

### Admin Dashboard
- Overview of sales, products, and users
- Access to all management features
- Business settings configuration

### Cashier Dashboard
- Product search and filtering
- Shopping cart management
- Sales processing
- Receipt generation

### Product Management
- Product CRUD operations
- Image upload support
- Category assignment
- Stock level tracking

### Sales History
- Detailed sales records
- Filter by date, cashier, or payment method
- Print/export capabilities

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the project repository.