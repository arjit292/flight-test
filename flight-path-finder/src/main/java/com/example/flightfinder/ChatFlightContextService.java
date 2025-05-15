package com.example.flightfinder;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatFlightContextService {

    private final Map<String, FlightQueryContext> contextStore = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENROUTER_API_KEY = "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"; // replace with env/secure config

    public ChatResponse handleMessage(String conversationId, String userMessage) {
        FlightQueryContext currentContext = contextStore.getOrDefault(conversationId, new FlightQueryContext());

        String prompt = "You are a helpful assistant planning flights.\n"
                + "Current context: " + currentContext.toJson() + "\n"
                + "User said: '" + userMessage + "'\n"
                + "Update the context with any new values and return the updated context as JSON.\n"
                + "Then, based on the updated context, generate a friendly, natural-language reply.\n"
                + "Respond in JSON with two fields: 'context' and 'reply'.";

        String llmResponse = callLLM(prompt);

        String contextJson = extractJsonValue(llmResponse, "context");
        String reply = extractJsonValue(llmResponse, "reply");

        FlightQueryContext updatedContext = FlightQueryContext.fromJson(contextJson);
        contextStore.put(conversationId, updatedContext);

        return new ChatResponse(updatedContext, reply);
    }

    private String callLLM(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENROUTER_API_KEY);

        Map<String, Object> requestBody = Map.of(
                "model", "microsoft/phi-4-reasoning:free",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant for booking flights."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);


        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://openrouter.ai/api/v1/chat/completions",
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

    private String extractJsonValue(String json, String key) {
        try {
            String[] parts = json.split("\"" + key + "\":");
            if (parts.length < 2) return null;
            String valuePart = parts[1].trim();
            if (valuePart.startsWith("{")) {
                int end = valuePart.indexOf("}") + 1;
                return valuePart.substring(0, end);
            } else {
                int end = valuePart.indexOf("\"");
                return valuePart.substring(1, end);
            }
        } catch (Exception e) {
            return null;
        }
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
            FlightQueryContext ctx = new FlightQueryContext();
            ctx.from = extract(json, "from");
            ctx.to = extract(json, "to");
            ctx.departureDate = extract(json, "departureDate");
            ctx.returnDate = extract(json, "returnDate");
            ctx.tripType = extract(json, "tripType");
            return ctx;
        }

        private static String extract(String json, String key) {
            try {
                String[] parts = json.split("\"" + key + "\":\"");
                return parts.length > 1 ? parts[1].split("\"")[0] : null;
            } catch (Exception e) {
                return null;
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
}

