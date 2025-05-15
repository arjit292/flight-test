# ✈️ Flight Path Finder

This Spring Boot application finds the **5 fastest flight paths** (direct or one-stop with layover) between any two airports based on a provided flight schedule CSV.

---

## 🚀 Features

- RESTful API to get fastest flight paths
- Direct and one-stop combinations (min 120 min layover)
- Handles flights crossing midnight
- Uses in-memory H2 database, loaded from CSV at startup
- Fully self-contained, no external database required

---

## 🧰 Tech Stack

- Java 17+
- Spring Boot
- Spring Data JPA (H2 database)
- Maven

---

## 📂 Flight CSV Format

The input CSV must have the following columns:
