# Vulnerable Job Search Application

## ⚠️ SECURITY WARNING
This application is **INTENTIONALLY VULNERABLE** and designed for security training purposes only. **DO NOT** deploy this application in a production environment or expose it to the internet without proper security measures.

## Overview
This is a full-stack job search application built with Spring Boot (backend) and Vue.js (frontend) that contains intentional security vulnerabilities for educational purposes. The application simulates a job portal where job seekers can apply for jobs and companies can post job openings.

## Architecture
- **Backend**: Spring Boot with MySQL database
- **Frontend**: Vue.js with Bootstrap
- **Database**: MySQL 8.0
- **Containerization**: Docker & Docker Compose

## Features

### For Job Seekers (Members)
- User registration and login
- Profile management
- CV upload
- Browse and search job listings
- Apply for jobs with cover letters
- View application history
- Manage skills, education, and experience

### For Companies
- Company registration and login
- Company profile management
- Post job openings
- View job applications
- Manage application status (accept/reject/review)
- View applicant profiles

## Intentional Vulnerabilities

This application includes the following OWASP Top 10 vulnerabilities for training purposes:

### 1. Injection (SQL Injection)
- **Location**: `UserRepository.findByUsernameAndPassword()`, `JobRepository.searchJobs()`
- **Impact**: Attackers can manipulate SQL queries to bypass authentication or extract data
- **Example**: Login with username: `admin' OR '1'='1' --`

### 2. Broken Authentication
- **Location**: Password storage, session management
- **Impact**: Passwords stored in plain text, weak session handling
- **Example**: Passwords are not hashed, sessions don't expire properly

### 3. Sensitive Data Exposure
- **Location**: API responses, database configuration
- **Impact**: Sensitive information exposed in responses and logs
- **Example**: Full user objects returned in API responses including passwords

### 4. XML External Entities (XXE)
- **Location**: File upload functionality
- **Impact**: XML files can reference external entities
- **Example**: Upload XML files with external entity references

### 5. Broken Access Control
- **Location**: All API endpoints
- **Impact**: Users can access resources they shouldn't have access to
- **Examples**:
  - View any user's profile: `GET /api/user/{any_user_id}`
  - Update any user's profile by manipulating user ID
  - View any job's applications: `GET /api/applications/job/{any_job_id}`
  - Delete any job: `DELETE /api/jobs/{any_job_id}`

### 6. Security Misconfiguration
- **Location**: CORS settings, error handling, server configuration
- **Impact**: Overly permissive configurations
- **Examples**:
  - CORS allows all origins (`*`)
  - Detailed error messages expose system information
  - No security headers

### 7. Cross-Site Scripting (XSS)
- **Location**: Frontend templates, user input display
- **Impact**: Malicious scripts can be executed in user browsers
- **Examples**:
  - Job titles, descriptions, and user profiles display unescaped HTML
  - Search functionality reflects user input without sanitization
  - Error messages display unescaped content

### 8. Insecure Deserialization
- **Location**: Session handling
- **Impact**: Session data manipulation
- **Example**: Session attributes can be manipulated

### 9. Using Components with Known Vulnerabilities
- **Location**: Dependencies
- **Impact**: Outdated libraries with known security issues
- **Example**: Using older versions of Spring Boot and other dependencies

### 10. Insufficient Logging & Monitoring
- **Location**: Throughout the application
- **Impact**: Security events are not properly logged
- **Example**: No logging of failed login attempts or suspicious activities

## Additional Vulnerabilities

### File Upload Vulnerabilities
- **Location**: CV and profile image upload
- **Impact**: Arbitrary file upload, directory traversal
- **Examples**:
  - No file type validation
  - No file size limits
  - Directory traversal in file paths
  - Executable files can be uploaded

### Business Logic Flaws
- **Location**: Application workflow
- **Impact**: Users can perform unauthorized actions
- **Examples**:
  - Apply for jobs as any user by manipulating user ID
  - Create jobs for any company
  - Update application status without authorization

## Setup Instructions

### Prerequisites
- Docker and Docker Compose
- Git

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd job-search-vulnerable-app
```

2. Build and start the application:
```bash
docker-compose up --build
```

### Running the Application

To start the application:
```bash
docker-compose up --build
```

To run in background:
```bash
docker-compose up -d --build
```

To stop the application:
```bash
docker-compose down
```

To view logs:
```bash
docker-compose logs -f
```

### Access Information

- **Frontend (Vue.js)**: http://localhost:8003
- **Backend API**: http://localhost:8083
- **Database (MySQL)**: localhost:3307
- **phpMyAdmin**: http://localhost:9003
  - Server: mysql
  - Username: root
  - Password: rootpassword

### Default Credentials

**Job Seeker Account:**
- Username: `john_doe`
- Password: `password123`

**Company Account:**
- Username: `admin`
- Password: `admin123`

## Testing Vulnerabilities

### SQL Injection Examples

1. **Login Bypass**:
   - Username: `admin' OR '1'='1' --`
   - Password: `anything`

2. **Job Search Injection**:
   - Search for: `test' UNION SELECT id,username,password,email,role,null,null,null,null,null,null,null,null,null,null FROM users --`

### XSS Examples

1. **Profile XSS**:
   - Update full name to: `<script>alert('XSS')</script>`

2. **Job Posting XSS**:
   - Job title: `<img src=x onerror=alert('XSS')>`

### Broken Access Control Examples

1. **View Any User Profile**:
   - `GET http://localhost:8080/api/user/1`

2. **Apply as Another User**:
   - Manipulate `userId` in application request

3. **Access Any Job Applications**:
   - `GET http://localhost:8080/api/applications/job/1`

### File Upload Vulnerabilities

1. **Upload Executable Files**:
   - Upload `.php`, `.jsp`, or other executable files as CV

2. **Directory Traversal**:
   - Use filenames like `../../../etc/passwd`

## Learning Objectives

After working with this application, you should understand:

1. How to identify and exploit common web application vulnerabilities
2. The impact of security misconfigurations
3. Importance of input validation and output encoding
4. Proper authentication and authorization mechanisms
5. Secure file upload implementations
6. Database security best practices
7. The importance of security headers and CORS configuration

## Remediation Guidelines

For each vulnerability, consider these remediation strategies:

1. **SQL Injection**: Use parameterized queries, input validation
2. **XSS**: Output encoding, Content Security Policy
3. **Broken Access Control**: Implement proper authorization checks
4. **File Upload**: Validate file types, scan for malware, store outside web root
5. **Authentication**: Hash passwords, implement proper session management
6. **Configuration**: Remove debug information, implement security headers

## Disclaimer

This application is created solely for educational and training purposes. The vulnerabilities are intentional and should not be replicated in production applications. Always follow secure coding practices and conduct regular security assessments in real-world applications.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
