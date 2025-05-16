package com.example.flightfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatFlightContextService {

    private final Map<String, FlightQueryContext> contextStore = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TOGETHER_API_KEY = "TOGETHER_API_KEY";

    public ChatResponse handleMessage(String conversationId, String userMessage) {
        FlightQueryContext currentContext = contextStore.getOrDefault(conversationId, new FlightQueryContext());

        String prompt = "You are a helpful assistant planning flights.\n"
                + "Current context: " + currentContext.toJson() + "\n"
                + "User said: '" + userMessage + "'\n"
                + "Update the context with any new values and return the updated context as JSON.\n"
                + "Then, based on the updated context, generate a friendly, natural-language reply.\n"
                + "Respond in JSON with two fields: 'context' and 'reply'.";

        String llmResponse = callLLM(prompt);
        System.out.println("🔍 LLM Raw Response: " + llmResponse);

        LLMResult result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            llmResponse = llmResponse.replaceAll("(?s)^```json\\s*", "").trim();
            System.out.println("📦 content field string: after cleaning " + llmResponse);
            result = mapper.readValue(llmResponse, LLMResult.class);

        } catch (Exception e) {
            e.printStackTrace();
            result = new LLMResult();
            result.reply = "Sorry, I couldn't understand your request.";
            result.context = new FlightQueryContext();
        }

        FlightQueryContext updatedContext = result.context;
        String reply = result.reply;

        Map<String, String> cityToIata = Map.of(
                "Amritsar", "ATQ", "Delhi", "DEL", "Bangalore", "BLR", "Chandigarh", "IXC",
                "Guwahati", "GAU", "Kolkata", "CCU", "Mumbai", "BOM", "Hyderabad", "HYD",
                "Pune", "PNQ", "Chennai", "MAA"
        );

        Map<String, String> iataToAirport = Map.of(
                "ATQ", "Sri Guru Ram Dass Jee International Airport",
                "DEL", "Indira Gandhi International Airport",
                "BLR", "Kempegowda International Airport",
                "IXC", "Chandigarh International Airport",
                "GAU", "Lokpriya Gopinath Bordoloi Airport",
                "CCU", "Netaji Subhas Chandra Bose International Airport",
                "BOM", "Chhatrapati Shivaji Maharaj International Airport",
                "HYD", "Rajiv Gandhi International Airport",
                "PNQ", "Pune International Airport",
                "MAA", "Chennai International Airport"
        );

        if (updatedContext.from != null) {
            String normalizedFrom = capitalize(updatedContext.from);
            String iata = cityToIata.getOrDefault(normalizedFrom, updatedContext.from);
            updatedContext.from = iata;
            reply += String.format(" Departing from %s.", iataToAirport.getOrDefault(iata, iata));
        }
        if (updatedContext.to != null) {
            String normalizedTo = capitalize(updatedContext.to);
            String iata = cityToIata.getOrDefault(normalizedTo, updatedContext.to);
            updatedContext.to = iata;
            reply += String.format(" Arriving at %s.", iataToAirport.getOrDefault(iata, iata));
        }

        contextStore.put(conversationId, updatedContext);

        List<Map<String, Object>> flightOptions = new ArrayList<>();
        if (updatedContext.from != null && updatedContext.to != null) {
            try {
                String url = String.format("http://localhost:8080/api/%s?from=%s&to=%s",
                        "round-trip".equalsIgnoreCase(updatedContext.tripType) ? "roundtrip-summary" : "flight-summary",
                        updatedContext.from.toUpperCase(),
                        updatedContext.to.toUpperCase()
                );
                ResponseEntity<List> flightResponse = restTemplate.exchange(
                        url, HttpMethod.GET, null, List.class
                );
                flightOptions = flightResponse.getBody();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        reply += flightOptions != null && !flightOptions.isEmpty()
                ? String.format(" I found %d possible options. %s", flightOptions.size(), flightOptions.toString())
                : " I couldn't find any matching flights.";

        System.out.printf("Calling flight search with from=%s, to=%s, type=%s\n",
                updatedContext.from, updatedContext.to, updatedContext.tripType);

        return new ChatResponse(updatedContext, reply);
    }

    private String callLLM(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + TOGETHER_API_KEY);

        Map<String, Object> requestBody = Map.of(
                "model", "meta-llama/Llama-3.3-70B-Instruct-Turbo-Free",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant for booking flights."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.together.xyz/v1/chat/completions",
                    request,
                    Map.class
            );

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"context\":{},\"reply\":\"Sorry, I couldn't process that.\"}";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static class FlightQueryContext {
        public String from;
        public String to;
        public String departureDate;
        public String returnDate;
        public String tripType;

        public String toJson() {
            return String.format("{\"from\":\"%s\",\"to\":\"%s\",\"departureDate\":\"%s\",\"returnDate\":\"%s\",\"tripType\":\"%s\"}",
                    nullSafe(from), nullSafe(to), nullSafe(departureDate), nullSafe(returnDate), nullSafe(tripType));
        }

        public static FlightQueryContext fromJson(String json) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, FlightQueryContext.class);
            } catch (Exception e) {
                e.printStackTrace();
                return new FlightQueryContext();
            }
        }

        private static String nullSafe(String value) {
            return value == null ? "" : value;
        }
    }

    public static class ChatResponse {
        public FlightQueryContext context;
        public String reply;

        public ChatResponse(FlightQueryContext context, String reply) {
            this.context = context;
            this.reply = reply;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LLMResult {
        @JsonProperty("context")
        public FlightQueryContext context;

        @JsonProperty("reply")
        public String reply;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TogetherResponse {
        public List<Choice> choices;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            public Message message;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            public String role;
            public String content;
        }
    }
}
