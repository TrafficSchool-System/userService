package com.example.userService.Service.email;

public interface EmailServiceInterface {

    //Skickar välkomstbrev till nya användare
    void sendWelcomeEmail(String email, String firstName, String welcomeToken); 

    //Skickar länken till befintliga användare 
    void sendMagicLinkEmail(String email, String firstName, String loginToken); 

}
