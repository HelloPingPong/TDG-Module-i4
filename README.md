**Project Structure**

src/main/java/com/example/tdg/
├── config/               # Configuration classes
├── controller/           # REST API endpoints
├── model/                # Domain objects
│   ├── entity/           # Database entities
│   ├── dto/              # Data transfer objects
├── repository/           # Data access layer
├── service/              # Business logic
│   ├── generator/        # Data generation framework
│   ├── scheduler/        # Scheduling functionality
│   ├── pdf/              # PDF analysis functionality
├── exception/            # Custom exceptions
├── util/                 # Utility classes
└── TestDataGeneratorApplication.java  # Main application class

Core Components:

Data generator framework with interfaces and implementations
Template and column definition model
Data type registry for extensibility


Data Generation:

CSV, JSON, and XML output formats
Batch generation capabilities
Scheduled generation with one-time and recurring options


PDF Analysis:

Extraction of variables from redline PDFs
Automatic template creation based on detected variables
Type inference based on variable names


API Endpoints:

Template management
Data generation
Scheduling
Batch operations
PDF analysis


Exception Handling:

Custom exceptions for different error scenarios
Global exception handler for consistent error responses
