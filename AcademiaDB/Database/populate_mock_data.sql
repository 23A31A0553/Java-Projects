-- ==========================================
-- BLUE RIDGE UNIVERSITY MOCK DATA GENERATOR
-- ==========================================

USE university;

-- Clear previous mock entries to avoid duplicate key errors
DELETE FROM activity_logs;
DELETE FROM notifications;
DELETE FROM calendar_events;
DELETE FROM study_materials;
DELETE FROM attendance;
DELETE FROM test_marks;
DELETE FROM class_tests;
DELETE FROM assignment_submissions;
DELETE FROM assignments;
DELETE FROM announcements WHERE posted_by > 1;

-- Clean student & teacher records previously added (keeping default admin/student/teacher records)
DELETE FROM students WHERE user_id > 5;
DELETE FROM teachers WHERE user_id > 5;
DELETE FROM users WHERE user_id > 5;

-- Reset Auto-Increments
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE students AUTO_INCREMENT = 3;
ALTER TABLE teachers AUTO_INCREMENT = 3;
ALTER TABLE assignments AUTO_INCREMENT = 1;
ALTER TABLE assignment_submissions AUTO_INCREMENT = 1;
ALTER TABLE class_tests AUTO_INCREMENT = 1;
ALTER TABLE test_marks AUTO_INCREMENT = 1;
ALTER TABLE attendance AUTO_INCREMENT = 1;
ALTER TABLE study_materials AUTO_INCREMENT = 1;
ALTER TABLE calendar_events AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;

-- =============================================================================
-- 1. ADD TEACHERS (1 for each of CSE, ECE, ME with natural Indian names)
-- =============================================================================

-- CSE Teacher (Dr. Rajesh Kumar)
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('rajesh.kumar', 'teacher123', 'Rajesh', 'Kumar', 'rajesh.kumar@bruniversity.edu', '9876543210', 'TEACHER', 'ACTIVE');
SET @t_cse_user = LAST_INSERT_ID();
INSERT INTO teachers (user_id, department, employee_type)
VALUES (@t_cse_user, 'Computer Science and Engineering', 'Teaching Faculty');
SET @t_cse_id = LAST_INSERT_ID();

-- ECE Teacher (Dr. Amit Varma)
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('amit.varma', 'teacher123', 'Amit', 'Varma', 'amit.varma@bruniversity.edu', '9876543211', 'TEACHER', 'ACTIVE');
SET @t_ece_user = LAST_INSERT_ID();
INSERT INTO teachers (user_id, department, employee_type)
VALUES (@t_ece_user, 'Electronics and Communication Engineering', 'Visiting Faculty');
SET @t_ece_id = LAST_INSERT_ID();

-- ME Teacher (Dr. Vikram Singh)
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('vikram.singh', 'teacher123', 'Vikram', 'Singh', 'vikram.singh@bruniversity.edu', '9876543212', 'TEACHER', 'ACTIVE');
SET @t_me_user = LAST_INSERT_ID();
INSERT INTO teachers (user_id, department, employee_type)
VALUES (@t_me_user, 'Mechanical Engineering', 'Teaching Faculty');
SET @t_me_id = LAST_INSERT_ID();


-- =============================================================================
-- 2. ADD STUDENTS (10 in CSE, 10 in ECE, 10 in ME with natural Indian names)
-- =============================================================================

-- ----------------- CSE Students (Semester 7) -----------------
-- Student 1
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('aarav.sharma', 'student123', 'Aarav', 'Sharma', 'aarav.sharma@bruniversity.edu', '9000000001', 'STUDENT', 'ACTIVE');
SET @u_cse1 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse1, 'Computer Science and Engineering', '7');
SET @s_cse1 = LAST_INSERT_ID();

-- Student 2
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('vivaan.patel', 'student123', 'Vivaan', 'Patel', 'vivaan.patel@bruniversity.edu', '9000000002', 'STUDENT', 'ACTIVE');
SET @u_cse2 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse2, 'Computer Science and Engineering', '7');
SET @s_cse2 = LAST_INSERT_ID();

-- Student 3
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('aditya.verma', 'student123', 'Aditya', 'Verma', 'aditya.verma@bruniversity.edu', '9000000003', 'STUDENT', 'ACTIVE');
SET @u_cse3 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse3, 'Computer Science and Engineering', '7');
SET @s_cse3 = LAST_INSERT_ID();

-- Student 4 (Critical Attendance Warning target)
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('vihaan.reddy', 'student123', 'Vihaan', 'Reddy', 'vihaan.reddy@bruniversity.edu', '9000000004', 'STUDENT', 'ACTIVE');
SET @u_cse4 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse4, 'Computer Science and Engineering', '7');
SET @s_cse4 = LAST_INSERT_ID();

-- Student 5
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('arjun.rao', 'student123', 'Arjun', 'Rao', 'arjun.rao@bruniversity.edu', '9000000005', 'STUDENT', 'ACTIVE');
SET @u_cse5 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse5, 'Computer Science and Engineering', '7');
SET @s_cse5 = LAST_INSERT_ID();

-- Student 6
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('sai.krishna', 'student123', 'Sai', 'Krishna', 'sai.krishna@bruniversity.edu', '9000000006', 'STUDENT', 'ACTIVE');
SET @u_cse6 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse6, 'Computer Science and Engineering', '7');
SET @s_cse6 = LAST_INSERT_ID();

-- Student 7
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('reyansh.gupta', 'student123', 'Reyansh', 'Gupta', 'reyansh.gupta@bruniversity.edu', '9000000007', 'STUDENT', 'ACTIVE');
SET @u_cse7 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse7, 'Computer Science and Engineering', '7');
SET @s_cse7 = LAST_INSERT_ID();

-- Student 8
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('arnav.joshi', 'student123', 'Arnav', 'Joshi', 'arnav.joshi@bruniversity.edu', '9000000008', 'STUDENT', 'ACTIVE');
SET @u_cse8 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse8, 'Computer Science and Engineering', '7');
SET @s_cse8 = LAST_INSERT_ID();

-- Student 9
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('krishna.nair', 'student123', 'Krishna', 'Nair', 'krishna.nair@bruniversity.edu', '9000000009', 'STUDENT', 'ACTIVE');
SET @u_cse9 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse9, 'Computer Science and Engineering', '7');
SET @s_cse9 = LAST_INSERT_ID();

-- Student 10
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('ishaan.choudhury', 'student123', 'Ishaan', 'Choudhury', 'ishaan.choudhury@bruniversity.edu', '9000000010', 'STUDENT', 'ACTIVE');
SET @u_cse10 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_cse10, 'Computer Science and Engineering', '7');
SET @s_cse10 = LAST_INSERT_ID();


-- ----------------- ECE Students (Semester 5) -----------------
-- Student 1
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('diya.menon', 'student123', 'Diya', 'Menon', 'diya.menon@bruniversity.edu', '9000000011', 'STUDENT', 'ACTIVE');
SET @u_ece1 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece1, 'Electronics and Communication Engineering', '5');

-- Student 2
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('isha.kapoor', 'student123', 'Isha', 'Kapoor', 'isha.kapoor@bruniversity.edu', '9000000012', 'STUDENT', 'ACTIVE');
SET @u_ece2 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece2, 'Electronics and Communication Engineering', '5');

-- Student 3
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('ananya.chawla', 'student123', 'Ananya', 'Chawla', 'ananya.chawla@bruniversity.edu', '9000000013', 'STUDENT', 'ACTIVE');
SET @u_ece3 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece3, 'Electronics and Communication Engineering', '5');

-- Student 4
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('aadhya.bose', 'student123', 'Aadhya', 'Bose', 'aadhya.bose@bruniversity.edu', '9000000014', 'STUDENT', 'ACTIVE');
SET @u_ece4 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece4, 'Electronics and Communication Engineering', '5');

-- Student 5
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('aaradhya.roy', 'student123', 'Aaradhya', 'Roy', 'aaradhya.roy@bruniversity.edu', '9000000015', 'STUDENT', 'ACTIVE');
SET @u_ece5 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece5, 'Electronics and Communication Engineering', '5');

-- Student 6
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('pihu.sinha', 'student123', 'Pihu', 'Sinha', 'pihu.sinha@bruniversity.edu', '9000000016', 'STUDENT', 'ACTIVE');
SET @u_ece6 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece6, 'Electronics and Communication Engineering', '5');

-- Student 7
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('khushi.mishra', 'student123', 'Khushi', 'Mishra', 'khushi.mishra@bruniversity.edu', '9000000017', 'STUDENT', 'ACTIVE');
SET @u_ece7 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece7, 'Electronics and Communication Engineering', '5');

-- Student 8
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('shreya.dutt', 'student123', 'Shreya', 'Dutt', 'shreya.dutt@bruniversity.edu', '9000000018', 'STUDENT', 'ACTIVE');
SET @u_ece8 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece8, 'Electronics and Communication Engineering', '5');

-- Student 9
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('riya.sen', 'student123', 'Riya', 'Sen', 'riya.sen@bruniversity.edu', '9000000019', 'STUDENT', 'ACTIVE');
SET @u_ece9 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece9, 'Electronics and Communication Engineering', '5');

-- Student 10
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('ira.mehta', 'student123', 'Ira', 'Mehta', 'ira.mehta@bruniversity.edu', '9000000020', 'STUDENT', 'ACTIVE');
SET @u_ece10 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_ece10, 'Electronics and Communication Engineering', '5');


-- ----------------- ME Students (Semester 3) -----------------
-- Student 1
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('kabir.gill', 'student123', 'Kabir', 'Gill', 'kabir.gill@bruniversity.edu', '9000000021', 'STUDENT', 'ACTIVE');
SET @u_me1 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me1, 'Mechanical Engineering', '3');

-- Student 2
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('rohan.dhillon', 'student123', 'Rohan', 'Dhillon', 'rohan.dhillon@bruniversity.edu', '9000000022', 'STUDENT', 'ACTIVE');
SET @u_me2 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me2, 'Mechanical Engineering', '3');

-- Student 3
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('sameer.khan', 'student123', 'Sameer', 'Khan', 'sameer.khan@bruniversity.edu', '9000000023', 'STUDENT', 'ACTIVE');
SET @u_me3 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me3, 'Mechanical Engineering', '3');

-- Student 4
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('armaan.malik', 'student123', 'Armaan', 'Malik', 'armaan.malik@bruniversity.edu', '9000000024', 'STUDENT', 'ACTIVE');
SET @u_me4 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me4, 'Mechanical Engineering', '3');

-- Student 5
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('aryan.sood', 'student123', 'Aryan', 'Sood', 'aryan.sood@bruniversity.edu', '9000000025', 'STUDENT', 'ACTIVE');
SET @u_me5 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me5, 'Mechanical Engineering', '3');

-- Student 6
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('dhruv.sodhi', 'student123', 'Dhruv', 'Sodhi', 'dhruv.sodhi@bruniversity.edu', '9000000026', 'STUDENT', 'ACTIVE');
SET @u_me6 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me6, 'Mechanical Engineering', '3');

-- Student 7
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('parth.bhasin', 'student123', 'Parth', 'Bhasin', 'parth.bhasin@bruniversity.edu', '9000000027', 'STUDENT', 'ACTIVE');
SET @u_me7 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me7, 'Mechanical Engineering', '3');

-- Student 8
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('veer.khanna', 'student123', 'Veer', 'Khanna', 'veer.khanna@bruniversity.edu', '9000000028', 'STUDENT', 'ACTIVE');
SET @u_me8 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me8, 'Mechanical Engineering', '3');

-- Student 9
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('dev.sareen', 'student123', 'Dev', 'Sareen', 'dev.sareen@bruniversity.edu', '9000000029', 'STUDENT', 'ACTIVE');
SET @u_me9 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me9, 'Mechanical Engineering', '3');

-- Student 10
INSERT INTO users (username, password, first_name, last_name, email, mobile, role, status)
VALUES ('karan.kothari', 'student123', 'Karan', 'Kothari', 'karan.kothari@bruniversity.edu', '9000000030', 'STUDENT', 'ACTIVE');
SET @u_me10 = LAST_INSERT_ID();
INSERT INTO students (user_id, department, semester) VALUES (@u_me10, 'Mechanical Engineering', '3');


-- =============================================================================
-- 3. MOCK DATA FOR CSE TEACHER (RAJESH KUMAR)
-- =============================================================================

-- Assignments
INSERT INTO assignments (teacher_id, subject, unit, title, description, due_date, status)
VALUES (@t_cse_id, 'Java Servlets', 'Unit 3', 'Servlet Web App Integration', 'Integrate Jakarta Servlets and JSPs with MySQL database backend connection pools.', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'ACTIVE');
SET @a_cse1 = LAST_INSERT_ID();

INSERT INTO assignments (teacher_id, subject, unit, title, description, due_date, status)
VALUES (@t_cse_id, 'DBMS', 'Unit 1', 'Database Schema Normalization', 'Design a fully normalized 3NF schema mapping university departments, classes and courses.', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'CLOSED');
SET @a_cse2 = LAST_INSERT_ID();

-- Assignment Submissions for 'Servlet Web App Integration'
INSERT INTO assignment_submissions (assignment_id, student_id, status, file_name, file_path, marks_obtained)
VALUES (@a_cse1, @s_cse1, 'SUBMITTED', 'Aarav_Sharma_ServletApp.zip', 'uploads/submissions/Aarav_Sharma_ServletApp.zip', 92.5);

INSERT INTO assignment_submissions (assignment_id, student_id, status, file_name, file_path, marks_obtained)
VALUES (@a_cse1, @s_cse2, 'SUBMITTED', 'Vivaan_Patel_Integration.zip', 'uploads/submissions/Vivaan_Patel_Integration.zip', 87.0);

INSERT INTO assignment_submissions (assignment_id, student_id, status, file_name, file_path, marks_obtained)
VALUES (@a_cse1, @s_cse3, 'SUBMITTED', 'Aditya_Verma_Project.zip', 'uploads/submissions/Aditya_Verma_Project.zip', 78.5);

-- Submissions pending grading
INSERT INTO assignment_submissions (assignment_id, student_id, status, file_name, file_path, marks_obtained)
VALUES (@a_cse1, @s_cse6, 'SUBMITTED', 'Sai_Krishna_WebApp.zip', 'uploads/submissions/Sai_Krishna_WebApp.zip', NULL);

-- Submissions submitted LATE
INSERT INTO assignment_submissions (assignment_id, student_id, status, file_name, file_path, marks_obtained)
VALUES (@a_cse1, @s_cse7, 'LATE', 'Reyansh_Gupta_LateSubmission.zip', 'uploads/submissions/Reyansh_Gupta_LateSubmission.zip', NULL);

-- Class Tests
INSERT INTO class_tests (teacher_id, subject, unit, test_title, test_date, total_marks, description)
VALUES (@t_cse_id, 'Java Core', 'Unit 1', 'Java Collection Framework and OOPs', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 20, 'Covers ArrayList, HashMap, Interfaces and Inheritances.');
SET @ct_cse1 = LAST_INSERT_ID();

INSERT INTO class_tests (teacher_id, subject, unit, test_title, test_date, total_marks, description)
VALUES (@t_cse_id, 'Java Servlets', 'Unit 2', 'Servlet Lifecycle & Session Filters', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 30, 'Covers init/service/destroy lifecycle and HttpServletSessions.');
SET @ct_cse2 = LAST_INSERT_ID();

-- Test Marks (Java Core Test)
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse1, 18.5);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse2, 19.0);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse3, 14.5);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse4, 7.0); -- Fail target (below 40% of 20 = 8 marks)
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse5, 12.0);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse6, 16.0);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse7, 11.5);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse8, 17.5);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse9, 13.0);
INSERT INTO test_marks (test_id, student_id, marks_obtained) VALUES (@ct_cse1, @s_cse10, 15.0);

-- Attendance (For 3 different dates)
-- Today
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse1, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse2, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse3, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse4, @t_cse_id, 'Java Servlets', CURDATE(), 'ABSENT'); -- Warning target
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse5, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse6, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse7, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse8, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse9, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse10, @t_cse_id, 'Java Servlets', CURDATE(), 'PRESENT');

-- Yesterday
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse1, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse2, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse3, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'ABSENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse4, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'ABSENT'); -- Warning target
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse5, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse6, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse7, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse8, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse9, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse10, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'PRESENT');

-- Day before yesterday
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse1, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse2, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse3, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse4, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'ABSENT'); -- Warning target
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse5, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse6, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse7, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse8, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse9, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'ABSENT');
INSERT INTO attendance (student_id, teacher_id, subject, attendance_date, status) VALUES (@s_cse10, @t_cse_id, 'Java Servlets', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PRESENT');

-- Study Materials
INSERT INTO study_materials (teacher_id, title, subject, unit, description, file_name, file_path)
VALUES (@t_cse_id, 'Unit 1 Introduction: Architecture Patterns', 'Java Servlets', 'Unit 1', 'Introductory slides on MVC architecture, Servlets container model and configuration.', 'ServletMVCIntro.pdf', 'uploads/materials/ServletMVCIntro.pdf');

INSERT INTO study_materials (teacher_id, title, subject, unit, description, file_name, file_path)
VALUES (@t_cse_id, 'Unit 2: Session Filters & Cookies Code', 'Java Servlets', 'Unit 2', 'Sample codes and helper classes for session attributes validation using filters.', 'SessionFilterSample.zip', 'uploads/materials/SessionFilterSample.zip');

-- Calendar Events
INSERT INTO calendar_events (teacher_id, title, event_date, event_type, description)
VALUES (@t_cse_id, 'National CSE Research Symposium', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'EVENT', 'College symposium hosting paper submissions from researchers across South Asia.');

INSERT INTO calendar_events (teacher_id, title, event_date, event_type, description)
VALUES (@t_cse_id, 'Guest Lecture: Serverless Architectures', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'MEETING', 'Expert session hosted by AWS Cloud Architect Rajesh Sen.');

-- Activity Logs
INSERT INTO activity_logs (user_id, action, description)
VALUES (@t_cse_user, 'TEACHER_LOGIN', 'Logged into teacher management dashboard.');

INSERT INTO activity_logs (user_id, action, description)
VALUES (@t_cse_user, 'ADD_ASSIGNMENT', 'Teacher published new assignment: Servlet Web App Integration.');

INSERT INTO activity_logs (user_id, action, description)
VALUES (@t_cse_user, 'ADD_CLASS_TEST', 'Teacher scheduled new class test: Java Collection Framework.');

-- Announcements
INSERT INTO announcements (title, message, posted_by, status, audience, subject)
VALUES ('Java Project Guidelines', 'Please ensure all assignments are properly uploaded as a .zip package containing only src files.', @t_cse_user, 'PUBLISHED', 'STUDENT', 'Java Core');

-- Notifications
INSERT INTO notifications (user_id, title, message)
VALUES (@t_cse_user, 'Project submission alerts', '5 students have submitted integration assignments.');

INSERT INTO notifications (user_id, title, message)
VALUES (@u_cse1, 'Assignment Published', 'New assignment: Servlet Web App Integration is active.');

INSERT INTO notifications (user_id, title, message)
VALUES (@u_cse4, 'Critical Attendance Warning', 'Your attendance in Java Servlets is below 75%. Please meet your faculty advisor.');


-- =============================================================================
-- 4. MOCK DATA FOR ECE TEACHER (AMIT VARMA)
-- =============================================================================

-- Assignments
INSERT INTO assignments (teacher_id, subject, unit, title, description, due_date, status)
VALUES (@t_ece_id, 'Digital System Design', 'Unit 1', 'Verilog Logic Gates Integration', 'Design and simulate standard logic gates (AND, OR, XOR) using behavioral modeling.', DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'ACTIVE');

-- Class Tests
INSERT INTO class_tests (teacher_id, subject, unit, test_title, test_date, total_marks, description)
VALUES (@t_ece_id, 'Semiconductors', 'Unit 2', 'PN Junction Diode and Transistors Basics', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 30, 'Covers diode forward/reverse characteristics.');


-- =============================================================================
-- 5. MOCK DATA FOR ME TEACHER (VIKRAM SINGH)
-- =============================================================================

-- Assignments
INSERT INTO assignments (teacher_id, subject, unit, title, description, due_date, status)
VALUES (@t_me_id, 'Thermodynamics', 'Unit 2', 'First Law Application problems', 'Solve exercise problems mapping First Law applications on closed systems.', DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'ACTIVE');

-- Class Tests
INSERT INTO class_tests (teacher_id, subject, unit, test_title, test_date, total_marks, description)
VALUES (@t_me_id, 'Fluid Mechanics', 'Unit 1', 'Fluid Statics and Bernoulli Equation', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 20, 'Covers pressure measurement, manometers and Euler equations.');
