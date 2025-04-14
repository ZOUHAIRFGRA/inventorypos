# Inventory POS System

A desktop Point of Sale (POS) system for inventory management built with Spring Boot and JavaFX. This application provides a comprehensive solution for managing inventory, sales, and business operations.

**Current Version: 1.0**

## Features

### User Role Management
- **Multi-level Access Control**
  - Owner/Admin full system access
  - Cashier restricted access
  - Support admin for system maintenance
  - Staff limited access (Coming in next version)
- **Security Features**
  - Secure password hashing
  - Password reset via support admin
  - Temporary password system
  - Basic session management

### Product Management
- **Product Control**
  - Add, edit, and delete products
  - Product image management
  - Custom product descriptions
  - Product history tracking
  - Stock quantity management
- **Category Management**
  - Create and manage categories
  - Category-based filtering
  - Basic category organization
- **Inventory Control**
  - Real-time stock tracking
  - Low stock visual indicators
  - Auto stock updates on sales/returns
  - Initial stock tracking
  - Returns processing

### Sales Management
- **Modern POS Interface**
  - Intuitive product search
  - Category-based filtering
  - Real-time stock validation
  - Product grid display with images
- **Cart Features**
  - Multiple items management
  - Quantity adjustments with stock validation
  - Real-time total calculation
  - Quick item removal
  - Customer name tracking
- **Payment Processing**
  - Cash and card payment methods
  - Basic payment processing
  - Sale record keeping
- **Receipt & Returns**
  - Professional PDF invoice generation
  - Digital invoice storage
  - Invoice reprint capability
  - Product returns processing
  - Return stock adjustment

### Analytics & Reporting
- **Dashboard Analytics**
  - Basic sales overview
  - Sales trend visualization
  - Average ticket size tracking
  - Growth rate indicators
- **Product Analytics** (Basic Implementation)
  - Stock level monitoring
  - Low stock indicators
  - Basic sales tracking
- **Export Features**
  - Sales data export to CSV
  - Product data export
  - Inventory reports export

### Business Management
- **Business Settings**
  - Basic company information management
  - Logo customization
  - Contact details management
- **Data Management**
  - Manual backup functionality
  - Data restore capability
  - Basic data export tools
- **Invoice System**
  - Basic invoice generation
  - PDF format invoices
  - Invoice history tracking

### User Interface
- **Modern Design**
  - Material design components
  - Responsive layouts
  - Professional styling
- **Usability Features**
  - Basic keyboard shortcuts
  - Quick actions
  - Search functionality
  - Sorting and filtering
- **Notifications**
  - Basic system alerts
  - Operation status alerts
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