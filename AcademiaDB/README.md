# 🏫 AcademiaDB - Blue Ridge University Management System

AcademiaDB is a comprehensive, production-grade **University Management System** designed for **Blue Ridge University**. Built on the enterprise Java stack, the platform facilitates administrative oversight, teaching workflows, and student engagement.

---

## 🚀 Key Features & Portals

### 🛡️ Administrator Portal
* **Full Access Control**: Search, activate, deactivate, or delete user accounts.
* **Password Management**: Force-reset student and teacher passwords.
* **Academic Control**: Define and control departments, academic years, and semesters.
* **Overview Dashboard**: View real-time user statistics (total, active, student, and teacher counts).

### 👨‍🏫 Teacher Portal
* **Student Tracking**: Search and inspect details of assigned students.
* **Attendance Management**: Mark and update attendance for class sections.
* **Assignments & Class Tests**: Create assignments, post due dates, publish class test schedules, and record marks.
* **Study Material Repository**: Upload and distribute reference notes and documents.
* **Announcements & Calendar**: Broadcast class announcements and schedule events.

### 🎓 Student Portal
* **Mobile-Responsive Dashboard**: Features dynamic counters showing attendance percentage, assignment submissions, test completions, and upcoming tasks.
* **Timetable Schedule**: View weekday class slots and room assignments.
* **Academic Repository**: Access and download teacher-uploaded study materials.
* **Submissions & Grades**: Upload assignment files, view exam marks, and inspect official semester results.
* **Activity Logs & Notifications**: Keep track of announcements and profile updates.

---

## 🛠️ Technology Stack

* **Language**: Java 21 (JDK 21)
* **Backend**: Jakarta Servlets, JDBC, MVC architecture
* **Frontend**: JSP (JavaServer Pages), Vanilla CSS (harmonious blue palette, glassmorphism elements), JavaScript
* **Database**: MySQL 8.x+
* **Application Server**: Apache Tomcat 10.0.x / 10.1.x
* **IDE**: Eclipse IDE for Enterprise Java Web Developers

---

## 📂 Project Directory Structure

```text
AcademiaDB/
├── Database/
│   ├── schema.sql              # Clean unified database structure (17 tables)
│   └── populate_mock_data.sql  # Mock seed data (Indian names, grades, and attendance)
├── src/main/java/              # Servlet source code and database connection configurations
├── src/main/webapp/            # JSPs, Web resources (HTML/CSS/JS)
│   ├── css/                    # Modular layout stylesheets
│   ├── images/                 # University logos and images
│   └── WEB-INF/
│       ├── lib/                # MySQL Connector-J JAR
│       └── web.xml             # Servlet deployment descriptor
├── .gitignore                  # Git settings (ignores build folders and IDE metadata)
└── README.md                   # This project manual
```

---

## ⚙️ Installation & Setup

### 1. Database Configuration
1. Open your MySQL client (e.g., MySQL Workbench or Command Line).
2. Execute **[schema.sql](file:///c:/Users/LENOVO/eclipse-workspace/AcademiaDB/Database/schema.sql)** to set up the structural database schema.
3. Execute **[populate_mock_data.sql](file:///c:/Users/LENOVO/eclipse-workspace/AcademiaDB/Database/populate_mock_data.sql)** to populate the system with sandbox test data.
4. Verify/update connection settings in `src/main/java/com/university/db/DBConnection.java`:
   * **URL**: `jdbc:mysql://localhost:3306/university`
   * **Username**: `root`
   * **Password**: *Your MySQL root password*

### 2. IDE Import (Eclipse)
1. Open Eclipse IDE.
2. Select **File** ➔ **Import...** ➔ **Existing Projects into Workspace**.
3. Point to the root directory of this repository and click **Finish**.
4. Configure your Tomcat runtime target (Tomcat 10.0.x or 10.1.x) under Project Properties ➔ Targeted Runtimes.

---

## 🔑 Login Credentials

For quick sandbox testing, log in with the following default accounts:

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | System Administrator |
| **Teacher** | `teacher1` | `teacher123` | Dr. Rajesh Kumar (CSE Faculty) |
| **Student** | `student1` | `student123` | Ashish Student (CSE Student) |

*(Refer to `🔑 Administrator Logins.txt` in the root workspace folder for a complete list of all 33 sandbox accounts).*

---

## 📄 License & Credits

* **University**: BLUE RIDGE UNIVERSITY
* **Developer**: Made by **Penugonda Devashish**
* *Copyright © 2026 BLUE RIDGE UNIVERSITY. All Rights Reserved.*
