package com.example.flightfinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatFlightContextService chatFlightContextService;

    @PostMapping("/flight-chat")
    public Map<String, String> chatWithFlightAssistant(@RequestBody Map<String, String> body) {
        String sessionId = body.getOrDefault("sessionId", "default");
        String message = body.getOrDefault("message", "");

        ChatFlightContextService.ChatResponse response = chatFlightContextService.handleMessage(sessionId, message);
        return Map.of("reply", response.reply);
    }
}
