-- =========================================================
-- AcademiaDB Unified Schema
-- Database: university
-- =========================================================

CREATE DATABASE IF NOT EXISTS university;
USE university;

-- =========================================================
-- DROP OLD TABLES
-- =========================================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS timetable;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS calendar_events;
DROP TABLE IF EXISTS study_materials;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS test_marks;
DROP TABLE IF EXISTS class_tests;
DROP TABLE IF EXISTS assignment_submissions;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS academic_years;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. USERS TABLE
-- =========================================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(15),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 2. TEACHERS TABLE
-- =========================================================
CREATE TABLE teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    employee_type VARCHAR(50),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- =========================================================
-- 3. STUDENTS TABLE
-- =========================================================
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- =========================================================
-- 4. DEPARTMENTS TABLE
-- =========================================================
CREATE TABLE departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_code VARCHAR(50) NOT NULL UNIQUE,
    department_name VARCHAR(100) NOT NULL,
    hod_name VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 5. ACADEMIC YEARS TABLE
-- =========================================================
CREATE TABLE academic_years (
    academic_id INT PRIMARY KEY AUTO_INCREMENT,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    UNIQUE (academic_year, semester)
);

-- =========================================================
-- 6. SUBJECTS TABLE
-- =========================================================
CREATE TABLE subjects (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_code VARCHAR(50) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    units INT DEFAULT 4,
    teacher_id INT,
    CONSTRAINT fk_subject_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

-- =========================================================
-- 7. TIMETABLE SCHEDULE TABLE
-- =========================================================
CREATE TABLE timetable (
    timetable_id INT PRIMARY KEY AUTO_INCREMENT,
    department VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    subject_id INT NOT NULL,
    room VARCHAR(50) NOT NULL,
    CONSTRAINT fk_timetable_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

-- =========================================================
-- 8. ACTIVITY LOGS TABLE
-- =========================================================
CREATE TABLE activity_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =========================================================
-- 9. ANNOUNCEMENTS TABLE
-- =========================================================
CREATE TABLE announcements (
    announcement_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    posted_by INT,
    status VARCHAR(20) DEFAULT 'PUBLISHED',
    audience VARCHAR(50) DEFAULT 'ALL',
    subject VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_user FOREIGN KEY (posted_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =========================================================
-- 10. ASSIGNMENTS TABLE
-- =========================================================
CREATE TABLE assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    CONSTRAINT fk_assignment_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);

-- =========================================================
-- 11. ASSIGNMENT SUBMISSIONS TABLE
-- =========================================================
CREATE TABLE assignment_submissions (
    submission_id INT PRIMARY KEY AUTO_INCREMENT,
    assignment_id INT NOT NULL,
    student_id INT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    file_name VARCHAR(255) DEFAULT NULL,
    file_path VARCHAR(255) DEFAULT NULL,
    marks_obtained DECIMAL(5,2) DEFAULT NULL,
    CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- =========================================================
-- 12. CLASS TESTS TABLE
-- =========================================================
CREATE TABLE class_tests (
    test_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    test_title VARCHAR(255) NOT NULL,
    test_date DATE NOT NULL,
    total_marks INT NOT NULL,
    description TEXT,
    CONSTRAINT fk_test_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);

-- =========================================================
-- 13. TEST MARKS TABLE
-- =========================================================
CREATE TABLE test_marks (
    mark_id INT PRIMARY KEY AUTO_INCREMENT,
    test_id INT NOT NULL,
    student_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    CONSTRAINT unique_student_test UNIQUE (test_id, student_id),
    CONSTRAINT fk_mark_test FOREIGN KEY (test_id) REFERENCES class_tests(test_id) ON DELETE CASCADE,
    CONSTRAINT fk_mark_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- =========================================================
-- 14. ATTENDANCE TABLE
-- =========================================================
CREATE TABLE attendance (
    attendance_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    teacher_id INT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL, -- 'PRESENT' or 'ABSENT'
    CONSTRAINT unique_student_date_subject UNIQUE (student_id, attendance_date, subject),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);

-- =========================================================
-- 15. STUDY MATERIALS TABLE
-- =========================================================
CREATE TABLE study_materials (
    material_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_material_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);

-- =========================================================
-- 16. CALENDAR EVENTS TABLE
-- =========================================================
CREATE TABLE calendar_events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- 'ASSIGNMENT', 'TEST', 'EVENT', 'ANNOUNCEMENT'
    description TEXT,
    CONSTRAINT fk_event_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE CASCADE
);

-- =========================================================
-- 17. NOTIFICATIONS TABLE
-- =========================================================
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =========================================================
-- DEFAULT SEED DATA (ADMIN & DEPARTMENTS)
-- =========================================================

-- Admin User
INSERT INTO users (username, password, role, first_name, last_name, email, mobile, status)
VALUES ('admin', 'admin123', 'ADMIN', 'System', 'Administrator', 'admin@university.com', '9999999999', 'ACTIVE');

-- Core Departments
INSERT INTO departments (department_code, department_name, hod_name, status)
VALUES 
('CSE', 'Computer Science and Engineering', 'Dr. Rajesh Sharma', 'ACTIVE'),
('ECE', 'Electronics and Communication Engineering', 'Dr. Amit Patel', 'ACTIVE'),
('ME', 'Mechanical Engineering', 'Dr. Vikram Sen', 'ACTIVE');
