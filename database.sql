-- Database initialization for job search application
CREATE DATABASE IF NOT EXISTS job_search_db;
USE job_search_db;

-- Users table (both members and companies)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('member', 'company') NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    profile_image VARCHAR(255),
    cv_file VARCHAR(255),
    company_name VARCHAR(100),
    company_description TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Skills table
CREATE TABLE skills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    skill_name VARCHAR(100) NOT NULL,
    level ENUM('beginner', 'intermediate', 'advanced', 'expert') DEFAULT 'beginner',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Education table
CREATE TABLE education (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    institution VARCHAR(100) NOT NULL,
    degree VARCHAR(100),
    field_of_study VARCHAR(100),
    start_date DATE,
    end_date DATE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Experience table
CREATE TABLE experience (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    company VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Jobs table
CREATE TABLE jobs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    salary_min DECIMAL(10,2),
    salary_max DECIMAL(10,2),
    location VARCHAR(100),
    job_type ENUM('full-time', 'part-time', 'contract', 'internship') DEFAULT 'full-time',
    status ENUM('active', 'closed') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Job applications table
CREATE TABLE job_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT,
    user_id INT,
    cover_letter TEXT,
    status ENUM('pending', 'reviewed', 'accepted', 'rejected') DEFAULT 'pending',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_application (job_id, user_id)
);

-- Insert sample data
INSERT INTO users (username, email, password, role, full_name, phone, company_name) VALUES
('admin', 'admin@example.com', 'admin123', 'company', 'Admin User', '081234567890', 'Tech Corp'),
('john_doe', 'john@example.com', 'password123', 'member', 'John Doe', '081234567891', NULL),
('jane_smith', 'jane@example.com', 'password123', 'member', 'Jane Smith', '081234567892', NULL),
('company1', 'hr@company1.com', 'company123', 'company', 'HR Manager', '081234567893', 'Startup Inc');

INSERT INTO jobs (company_id, title, description, requirements, salary_min, salary_max, location) VALUES
(1, 'Software Developer', 'We are looking for a skilled software developer to join our team. You will be responsible for developing and maintaining web applications using modern technologies.', 'Java, Spring Boot, MySQL, 2+ years experience', 8000000, 15000000, 'Jakarta'),
(4, 'Frontend Developer', 'Join our team as a frontend developer. You will work on creating beautiful and responsive user interfaces using Vue.js and modern CSS frameworks.', 'Vue.js, HTML, CSS, JavaScript, 1+ years experience', 6000000, 12000000, 'Bandung'),
(1, 'Backend Developer', 'We need a backend developer to work on our API services and database design.', 'Node.js, Express, MongoDB, REST API', 7000000, 13000000, 'Jakarta'),
(4, 'UI/UX Designer', 'Create amazing user experiences and beautiful interfaces for our products.', 'Figma, Adobe XD, Prototyping, User Research', 5000000, 10000000, 'Surabaya');

INSERT INTO skills (user_id, skill_name, level) VALUES
(2, 'Java', 'intermediate'),
(2, 'Spring Boot', 'beginner'),
(2, 'MySQL', 'intermediate'),
(3, 'Vue.js', 'advanced'),
(3, 'JavaScript', 'expert'),
(3, 'CSS', 'advanced');

INSERT INTO education (user_id, institution, degree, field_of_study, start_date, end_date, description) VALUES
(2, 'Universitas Indonesia', 'Bachelor of Computer Science', 'Computer Science', '2018-08-01', '2022-07-01', 'Graduated with honors, focused on software engineering and database systems.'),
(3, 'Institut Teknologi Bandung', 'Bachelor of Informatics', 'Informatics Engineering', '2017-08-01', '2021-07-01', 'Specialized in web development and user interface design.');

INSERT INTO experience (user_id, company, position, start_date, end_date, description) VALUES
(2, 'PT. Teknologi Maju', 'Junior Developer', '2022-08-01', '2024-01-01', 'Developed web applications using Java Spring Boot. Worked on e-commerce platform with team of 5 developers.'),
(3, 'CV. Digital Creative', 'Frontend Developer', '2021-09-01', '2023-12-01', 'Created responsive web interfaces using Vue.js and modern CSS. Collaborated with designers and backend developers.');