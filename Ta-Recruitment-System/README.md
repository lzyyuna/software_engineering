# TA Recruitment System

A desktop application that manages the full lifecycle of Teaching Assistant
recruitment — from posting positions and applying for them, to reviewing
applicants and tracking TA workload — built in **Java 21 + JavaFX 21** with
**Maven**. Optional AI-assisted skill matching and workload analysis are
available via the DeepSeek API.

Developed by **Group 4**.

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Project Layout](#project-layout)
5. [Setup](#setup)
6. [Configuration](#configuration)
7. [Running the Application](#running-the-application)
8. [Default Accounts](#default-accounts)
9. [Running the Tests](#running-the-tests)
10. [Packaging](#packaging)
11. [Troubleshooting](#troubleshooting)

---

## Features

**TA role**
- Register / log in with an invite code.
- Create and edit a personal profile (student ID, skills, courses, contact).
- Upload, replace, and preview a résumé (txt / pdf / doc / docx, ≤ 10 MB).
- Browse open positions with skill-tag and position-type filters; results
  are sorted by skill-match score.
- View position details, an automatic skill-match analysis, and an
  optional AI-generated fit report (DeepSeek).
- Submit applications and track Pending / Approved / Rejected status.

**MO (Module Organiser / Teacher) role**
- Register / log in with an invite code.
- Post, edit, and close TA positions.
- Review applications with optional comments; approve or reject.
- Inspect each applicant's profile, résumé, and skill-match badge; run
  AI analysis for a deeper fit summary.

**Admin role**
- Log in with a pre-provisioned admin account (no self-registration).
- Manage users and generate invite codes for TAs and MOs.
- Review the consolidated TA workload table with configurable
  weekly-hour thresholds, filters, and CSV export.
- View statistics aggregated by course and department.
- Trigger AI workload analysis with verdict and recommended actions.

---

## Tech Stack

| Area              | Choice                                          |
| ----------------- | ----------------------------------------------- |
| Language          | Java 21                                         |
| UI framework      | JavaFX 21 (controls + FXML)                     |
| Build tool        | Maven (with `javafx-maven-plugin` 0.0.8)        |
| Persistence       | Local CSV / JSON files under `data/`            |
| Serialization     | Jackson Databind, OpenCSV                       |
| Testing           | JUnit 5, Mockito                                |
| Optional AI calls | DeepSeek REST API                               |

---

## Prerequisites

Install these before building the project:

| Tool                | Version          | Notes                                              |
| ------------------- | ---------------- | -------------------------------------------------- |
| **JDK**             | **21 or later**  | `java -version` must report 21+.                   |
| **Apache Maven**    | 3.8+             | `mvn -v` must succeed.                             |
| **Git** (optional)  | any recent       | Only needed if you clone the repo.                 |

JavaFX itself does **not** need a separate install — `javafx-controls` and
`javafx-fxml` are declared as Maven dependencies and downloaded
automatically.

---

## Project Layout

```
Group4_software_engineering/
└── Ta-Recruitment-System/        ← Maven project root (run mvn from here)
    ├── pom.xml
    ├── config/
    │   └── api_keys.properties.example   ← template; copy & rename
    ├── data/                              ← CSV/JSON storage
    │   ├── user.csv
    │   ├── applicants.csv
    │   ├── jobs.csv
    │   ├── applications.csv
    │   ├── admin_settings.properties
    │   └── resumes/                       ← uploaded résumés land here
    ├── src/main/java/com/group4/tarecruitment/
    │   ├── HelloFxApp.java                ← JavaFX entry point
    │   ├── controller/  service/  repository/  model/  view/  util/
    └── src/main/resources/
        └── styles/app-theme.css           ← shared visual theme
```

> **Important:** every `mvn` command in this README assumes your working
> directory is `Ta-Recruitment-System/`, not the repository root.

---

## Setup

1. **Get the source**

   ```bash
   git clone <repo-url>
   cd Group4_software_engineering/Ta-Recruitment-System
   ```

   Or unzip `TA-Recruitment-System.zip` and `cd` into the extracted
   `Ta-Recruitment-System/` folder.

2. **Verify Java and Maven**

   ```bash
   java -version   # expect 21.x
   mvn -v          # expect Maven 3.8+ using the JDK above
   ```

3. **Resolve dependencies and compile**

   ```bash
   mvn clean compile
   ```

   The first run downloads JavaFX, Jackson, OpenCSV, JUnit, and Mockito
   into your local Maven repository.

---

## Configuration

### 1. Data files (required)

The application reads and writes from `Ta-Recruitment-System/data/`.
Sample CSV / JSON files are committed to the repo so you can launch
immediately; uploaded résumés are stored in `data/resumes/`.

If you want to start clean, you may delete user-generated rows from
`applicants.csv`, `applications.csv`, `jobs.csv`, and the JSON files, but
**keep the header rows** — the CSV reader relies on them.

### 2. Admin workload threshold (optional)

The Admin → TA Workload page persists its weekly-hour threshold in
`data/admin_settings.properties`. You can change it from the UI, or
edit the file directly:

```properties
threshold=10.0
```

### 3. DeepSeek API key (optional, for AI features)

AI features (skill-match analysis on `JobDetail` / `MOViewApplications`
and workload analysis on `AdminWorkload`) require a DeepSeek API key.
Without one, the rest of the app works normally and the AI buttons show
a friendly "API key missing" message.

Pick **one** of these methods:

**Option A — properties file (recommended)**

```bash
cp config/api_keys.properties.example config/api_keys.properties
```

Edit `config/api_keys.properties` and replace the placeholder:

```properties
deepseek.api.key=sk-your-real-key-here
```

The real file is gitignored.

**Option B — environment variable**

```bash
# Linux / macOS
export DEEPSEEK_API_KEY=sk-your-real-key-here

# Windows PowerShell
$env:DEEPSEEK_API_KEY = "sk-your-real-key-here"
```

Get a key at <https://platform.deepseek.com/api_keys>.

---

## Running the Application

From inside `Ta-Recruitment-System/`:

```bash
mvn javafx:run
```

This launches the JavaFX window starting on the **Role Select / Login**
page. Pick a role (TA / MO / Admin), enter credentials (and an invite
code for TA/MO registration), and proceed.

> **Tip:** If you accidentally run `mvn javafx:run` from the repository
> root you will see
> `No plugin found for prefix 'javafx' in the current project`.
> Either `cd Ta-Recruitment-System` first, or run
> `mvn -f Ta-Recruitment-System/pom.xml javafx:run`.

---

## Default Accounts

The pre-seeded accounts in `data/user.csv` let you log in straight away:

| Role  | Username | Password  |
| ----- | -------- | --------- |
| Admin | `a`      | `1`       |
| MO    | `mo`     | `123456`  |
| TA    | `li`     | `123456`  |
| TA    | `lijing` | `123456`  |

To create new TA or MO accounts:

1. Log in as Admin (`a` / `1`).
2. Open **Manage System → Invite Code Management**.
3. Generate a code for the desired role and copy it.
4. From the login page, choose Register, fill in username / password,
   and paste the invite code.

Admin accounts cannot be self-registered; add them directly to
`data/user.csv` if you need more.

---

## Running the Tests

```bash
mvn test
```

The test suite (93 tests across model and service layers) runs with
JUnit 5 + Mockito and should report **0 failures, 0 errors**.

---

## Packaging

To produce a runnable JAR in `target/`:

```bash
mvn clean package
```

To launch the packaged app via Maven without rebuilding:

```bash
mvn javafx:run
```

A fully bundled, double-clickable installer is out of scope for this
release; use `mvn javafx:run` for day-to-day execution.

---

## Troubleshooting

| Symptom                                                     | Likely cause and fix                                                                                                                                  |
| ----------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `No plugin found for prefix 'javafx' …`                     | You're at the repository root. `cd Ta-Recruitment-System` or use `mvn -f Ta-Recruitment-System/pom.xml javafx:run`.                                   |
| `release version 21 not supported`                          | The active JDK is older than 21. Install JDK 21 and ensure `JAVA_HOME` points to it.                                                                  |
| `Error: JavaFX runtime components are missing`              | You're launching a built JAR directly with `java -jar`. Use `mvn javafx:run` instead — the plugin sets the JavaFX module path.                        |
| Login fails with the seeded accounts                        | `data/user.csv` was edited or removed. Restore it from version control; keep the `username,password,role` header row intact.                          |
| AI buttons report "API Key Missing"                         | DeepSeek key not configured. Follow [Configuration §3](#3-deepseek-api-key-optional-for-ai-features).                                                  |
| Résumé upload appears to succeed but the file is missing    | The app writes to `data/resumes/`. Make sure that folder exists and is writable by the user running Maven.                                            |
| Tests fail with `UnsupportedClassVersionError`              | The test JVM is older than 21. Same fix as the `release version 21` row above.                                                                        |

---

Happy recruiting!
