# TA Recruitment System
Software Engineering Course Project - TA Recruitment Application

## Project Description
This project is a TA Recruitment System developed based on **JavaFX** and **MVC architecture**. It implements core functions such as TA applicant registration and data persistence, fully meeting the user story requirements of the course.

## Implemented Core Features
- **TA Applicant Registration**: Supports input of name, email, and skills.
- **Form Validation**: Checks required fields and email format with user-friendly messages.
- **Data Persistence**: Automatically saves registration data to `data/applicants.csv` and `data/applicants.json`.
- **UI Optimization**: Default window size 600×450, auto-centered, improved user experience.
- **Layered Architecture**: Follows standard MVC pattern with clear structure: model / repository / service / controller / view.

## Tech Stack
- Java 21
- JavaFX (GUI)
- Maven (Dependency Management)
- OpenCSV & Jackson (File Storage)
- JUnit 5 (Unit Testing)

## How to Run
In IntelliJ IDEA, open the **Maven** panel on the right side:
Expand `Plugins → javafx → javafx:run` and double-click to start the application.


## Update Log (Recent Improvements)
1. Upgraded UI to a complete TA registration form.
2. Added input validation and auto-generated unique applicant ID.
3. Refactored Service and Repository layers for data persistence.
4. Optimized window size and layout.
5. Achieved permanent data storage in CSV and JSON formats.

## Developer
Li Jingyu