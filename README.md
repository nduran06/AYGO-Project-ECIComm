## AYGO-Project

# ECIComm

This project aims to propose the design of an architecture for one of the applications of distributed systems with integrated AI and ML capabilities, in this case, an e-commerce platform called ECIComm. The architecture follows a microservices approach, leveraging AWS services to create a scalable, resilient, and intelligent shopping experience.

## Architecture Overview

![alt text](imgs/arqui.png)

The platform is designed with multiple layers, each serving specific functions while maintaining loose coupling and high cohesion. The architecture diagram (shown above) illustrates the following key layers:

### Client Layer

Handles user interactions through web and mobile applications, utilizing CloudFront for content delivery and caching.

### API Layer

Manages API requests through API Gateway, implementing security through AWS WAF and Cognito. APIs are organized by domain (Product, Order, User, Search, Cart) for better maintainability.

### Application Layer
Houses the core business logic in microservices running on ECS/EKS clusters.

Services include:

- Product Service
- Order Service
- Inventory Service
- Search Service
- User Service
- Recommendation Service
- Cart Service

### Data Layer
Utilizes different databases optimized for specific use cases:

- Aurora for product catalog and user data
- DynamoDB for orders and inventory
- ElastiCache for cart management
- OpenSearch for search functionality

### Storage Layer

Manages file storage through S3:

- Product images
- Backup files
- System logs

### Caching Layer

Implements Redis clusters for:

- Performance optimization
- Session management
- Temporary data storage

### Message Queue
Handles asynchronous operations using:

- Amazon MQ
- SQS Queues

### AI/ML Layer
Provides intelligent features through:

- SageMaker for custom ML models
- Personalize for recommendation systems
- Forecast for inventory predictions

### Analytics Layer

Processes and analyzes data using:

- Kinesis Data Streams
- EMR for processing
- Redshift for data warehousing
- QuickSight for visualization

## Prototype Implementation

This repository contains a prototype implementation focusing on two key layers:

### Application Layer Implementation

- REST controllers for service endpoints
- Service layer business logic
- Repository layer for data access
- Event publishing for system integration
- Error handling and validation

### ML Layer Implementation

- Recommendation engine integration
- Product similarity calculations
- User behavior analysis
- Real-time prediction serving
- Model training pipeline

## Technologies Used

- Java 21
- Spring Boot
- Maven
- AWS SDK
- PostgreSQL
- AWS Services (SageMaker, Personalize, ...)
