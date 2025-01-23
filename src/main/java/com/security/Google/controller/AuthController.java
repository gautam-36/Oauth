package com.security.Google.controller;

import com.security.Google.service.MailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@RestController
public class AuthController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MailService mailService;


    @Value("${google.client.id}")
    private String CLIENT_ID;

    @Value("${google.client.secret}")
    private String CLIENT_SECRET;

    @Value("${google.redirect.uri}")
    private String REDIRECT_URI;


    @GetMapping("/google/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        String authorizationEndpoint = "https://accounts.google.com/o/oauth2/auth";

        // creating redirection url
        String redirectUrl = authorizationEndpoint +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=openid%20email%20profile";

         response.sendRedirect(redirectUrl);
    }

    // google callback api
    @GetMapping("/grantcode")
    public String handleGoogleCallback(@RequestParam("code") String code) throws Exception {
        // Step 1: Exchange authorization code for access token
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        // Prepare request
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("grant_type", "authorization_code");

        // Set headers for URL-encoded content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build the HTTP request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // Make the POST request to get the token response
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, String.class);

        System.out.println("google token: " + tokenResponse);
        // Parse the token response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenJson = mapper.readTree(tokenResponse.getBody());
        String accessToken = tokenJson.get("access_token").asText();

//         Step 2: Use the access token to fetch user info
        JsonNode userInfo =  fetchUserInfo(accessToken);

        String emailId = userInfo.get("email").asText();
        System.out.println("Sending mail to " + emailId);

        // sending mail by calling our mail service method sendMail()
        String subject = "Welcome to All Safe";
        String mailBody = "Hello " + userInfo.get("name").asText() +  " You have logged in to All Safe successfully";

        try{
            mailService.sendMail(emailId,subject,mailBody);
            return "Logged in Sucessfully";
        } catch (Exception e) {
            return "Failed to send email. Error: " + e.getMessage();
        }
    }

    private JsonNode fetchUserInfo(String accessToken) throws Exception {
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";

        // Set headers with bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // Build the HTTP request
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Make the GET request to fetch user info
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                request,
                String.class
        );

        // Parse and return the user info
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(userInfoResponse.getBody());
    }



// just for testing mail is service is working or not
    @GetMapping("/sendMail")
    public String sendMail(){
        mailService.sendMail("gautamkumarg660@gmail.com","Hii", "Hi welcome");
        return "Sucess";
    }
}
