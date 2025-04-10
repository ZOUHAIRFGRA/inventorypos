# Inventory POS System

A desktop Point of Sale (POS) system for inventory management built with Spring Boot and JavaFX. This application provides a comprehensive solution for managing inventory, sales, and business operations.

## Features

### User Role Management
- **Multi-level Access Control**
  - Owner/Admin full system access
  - Cashier restricted access
  - Staff limited access
  - Support admin for system maintenance
- **Security Features**
  - Secure password hashing
  - Password reset functionality
  - Temporary password system
  - Session management

### Product Management
- **Comprehensive Product Control**
  - Add, edit, and delete products
  - Bulk product import/export
  - Product image management
  - Custom product descriptions
  - Barcode support
- **Category Management**
  - Create and manage categories
  - Category-based filtering
  - Category analytics
- **Inventory Control**
  - Real-time stock tracking
  - Low stock alerts
  - Stock history
  - Auto stock updates on sales
  - Initial stock tracking

### Sales Management
- **Modern POS Interface**
  - Intuitive product search
  - Quick category filters
  - Real-time stock validation
  - Dynamic pricing updates
- **Advanced Cart Features**
  - Multiple items management
  - Quantity adjustments
  - Real-time total calculation
  - Cart save/restore
  - Quick item removal
- **Payment Processing**
  - Multiple payment methods
  - Split payment support
  - Payment validation
  - Change calculation
- **Receipt System**
  - Custom receipt generation
  - PDF receipt export
  - Digital receipt copies
  - Receipt reprint functionality

### Analytics & Reporting
- **Sales Analytics**
  - Daily/Monthly/Annual reports
  - Sales trend analysis
  - Top selling products
  - Revenue tracking
- **Product Analytics**
  - Stock level analysis
  - Product performance metrics
  - Margin calculations
  - Category-wise analysis
- **Export Capabilities**
  - Sales data export
  - Inventory reports
  - Custom report generation
  - Multiple export formats

### Business Management
- **Business Settings**
  - Company information management
  - Logo customization
  - Contact details
  - Business hours
- **Data Management**
  - Automated backups
  - Data restore functionality
  - Data export tools
  - System logs
- **Invoice System**
  - Professional invoice generation
  - Invoice customization
  - PDF export
  - Invoice history tracking

### User Interface
- **Modern Design**
  - Material design components
  - Responsive layouts
  - Dark/Light theme support
  - Custom styling
- **Usability Features**
  - Keyboard shortcuts
  - Quick actions
  - Search functionality
  - Sorting and filtering
- **Notifications**
  - Low stock alerts
  - Sale completion alerts
  - System notifications
  - Error notifications

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

This project is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License (CC BY-NC-ND 4.0). This means:

- ✅ You can use and share the code
- ✅ You can contribute via pull requests
- ❌ You cannot use it commercially
- ❌ You cannot distribute modified versions
- ❌ You must provide attribution

See the [LICENSE](LICENSE) file for details.

## Support

For support and questions, please open an issue in the project repository.