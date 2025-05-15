# ✈️ Flight Path Finder

A Spring Boot application that finds the 5 fastest flight paths between two airports, supporting direct and one-stop journeys with a layover constraint.

---

## 📦 Features

- Loads flight schedules from a CSV file at startup
- Uses in-memory H2 database (auto-loaded for both app and tests)
- Exposes REST API to get fastest paths
- Handles overnight flights and 120-minute layover minimum
- Fully testable with `@SpringBootTest`

---

## 🚀 Requirements

- Java 17+
- Maven 3.6+

---

## 📁 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/arjit292/flight-test.git
cd flight-test
