package com.yatrify.ai.service;

import com.yatrify.ai.dto.AiChatRequest;
import com.yatrify.ai.dto.AiChatResponse;
import com.yatrify.config.properties.YatrifyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final YatrifyProperties properties;
    private final WebClient.Builder webClientBuilder;

    private static final String SYSTEM_PROMPT_USER = """
            You are Yatri, a friendly and knowledgeable AI travel assistant for Yatrify — India's premier trip booking platform.
            
            Your role is to help users:
            - Find suitable trips (religious, honeymoon, family, adventure, bachelor, wellness)
            - Understand trip details, inclusions, and pricing
            - Navigate the booking process step by step
            - Answer questions about documents needed (Aadhaar, passport, etc.)
            - Provide travel tips and packing guidance
            - Assist elderly users with simple, clear explanations
            
            Important guidelines:
            - Always be warm, patient, and encouraging
            - Use simple language — many users may be elderly or first-time travelers
            - Provide step-by-step guidance when explaining processes
            - If unsure, suggest contacting the organizer or Yatrify support
            - Never share personal financial advice
            - Keep responses concise and actionable
            - Support Hindi and English (mix if user prefers)
            
            Always end with a helpful follow-up question or next step.
            """;

    private static final String SYSTEM_PROMPT_ORGANIZER = """
            You are Yatri Pro, an AI assistant for trip organizers on Yatrify.
            
            Your role is to help organizers:
            - Plan detailed trip itineraries day by day
            - Suggest appropriate pricing strategies
            - Create compelling trip descriptions and highlights
            - Identify important inclusions/exclusions
            - Draft terms & conditions and cancellation policies
            - Suggest popular religious/tourist destinations for specific trip types
            - Help with hotel selection criteria
            - Provide market insights on popular trip types
            
            Be professional, precise, and commercially aware. Help organizers create trips that are both appealing and profitable.
            """;

    public Mono<AiChatResponse> chat(AiChatRequest request, boolean isOrganizer) {
        String systemPrompt = isOrganizer ? SYSTEM_PROMPT_ORGANIZER : SYSTEM_PROMPT_USER;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        if (request.getHistory() != null) {
            messages.addAll(request.getHistory());
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        Map<String, Object> requestBody = Map.of(
                "model", properties.getOpenai().getModel(),
                "messages", messages,
                "max_tokens", properties.getOpenai().getMaxTokens(),
                "temperature", 0.7
        );

        return webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                        return AiChatResponse.builder()
                                .message(message.get("content"))
                                .role("assistant")
                                .build();
                    }
                    return AiChatResponse.builder()
                            .message("I'm sorry, I couldn't process your request. Please try again.")
                            .role("assistant")
                            .build();
                })
                .onErrorReturn(AiChatResponse.builder()
                        .message("AI assistant is temporarily unavailable. Please try again later.")
                        .role("assistant")
                        .build());
    }

    public Mono<AiChatResponse> generateTripItinerary(String destination, int days, String tripType, String preferences) {
        String prompt = String.format("""
                Create a detailed %d-day trip itinerary for a %s trip to %s.
                Special preferences: %s
                
                For each day provide:
                1. Day title
                2. 3-5 activities with brief descriptions
                3. Suggested meals (local cuisine recommendations)
                4. Accommodation type
                5. Transport suggestions
                
                Format as a structured plan that can be directly used in Yatrify's trip creation.
                """, days, tripType, destination, preferences);

        return chat(AiChatRequest.builder().message(prompt).build(), true);
    }
}
