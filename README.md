# ✈️ Chat-Driven Flight Path Finder

This Spring Boot application allows users to find the fastest flight paths between airports, and integrates with an LLM (e.g., Together.ai) to accept natural language input and generate human-friendly replies.

---

## 🚀 Features

- Load airline schedules from a CSV (in-memory database)
- Find top 5 fastest flight paths (direct and one-stop with layover rules)
- REST APIs for flight lookups and summaries
- LLM integration to convert natural language to structured search context
- Maintains conversation context via conversation ID

---

## 📁 Project Structure

- `FlightSearchService`: Computes flight paths from input CSV
- `ChatFlightContextService`: Manages chat logic and LLM interaction
- `testfile.csv`: Input file with flight schedule (to be placed under `resources/`)

---

## 🔧 Getting Started

### 1. Clone the Repo
```bash
git clone https://github.com/your-username/flight-test.git
cd flight-test

🏃 Running the Application
mvn spring-boot:run

✅ Get Fastest Flights (Direct + 1-Stop)

GET /api/flight-summary?from=ATQ&to=BLR


🔁 Round Trip Support (Stub)
GET /api/roundtrip-summary?from=DEL&to=BLR


💬 Chat with LLM (Pluggable Interface)
POST /api/chat
{
  "conversationId": "abc123",
  "message": "Book a round trip from Delhi to Bangalore between May 20 to May 25"
}

response 



{
  "context": {
    "from": "DEL",
    "to": "BLR",
    "tripType": "round trip"
  },
  "reply": "I found 5 fastest options from Delhi to Bangalore..."
}


📎 Notes
Flights require minimum 120 minutes for a layover at intermediate airports.

Only one-stop routes are considered (no multi-hop).

LLM responses may contain backticks (json ... ), which are stripped before parsing.

Supports natural language prompts like “Find flights from Amritsar to Delhi”.



🛠 Built With
Java 17+

Spring Boot (Web)

Jackson (for JSON parsing)

Together.ai / OpenRouter (LLM API)

Maven

