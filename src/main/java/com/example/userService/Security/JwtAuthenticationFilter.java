package com.example.userService.Security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.userService.Service.UserServiceInterface;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; 

    @Autowired
    private UserServiceInterface userService;
    
    //Denna metod körs på varje HTTP anrop 
    //Den kontrollerar om anropet har en giltig JWT token

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                //STEG 1: Hämta Authorization header
                final String authorizationHeader = request.getHeader("Authorization"); 
                
                String email = null; 
                String jwtToken = null; 


                //STEG 2: Kontrollera om header innehåller Bearer token
                if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

                    //Extrahera token (ta bort "bearer " prefix)
                    jwtToken = authorizationHeader.substring(7); 

                    try {
                        //STEG 3: Extrahera email från token
                        email = jwtUtil.extractEmail(jwtToken);
                    } catch (Exception e) {

                        //Token trasig eller ogiltig
                        logger.warn("JWT token kunde inte parsas: " + e.getMessage()); 

                    }
                    
                }

                //STEG 4: Validera token och sätt authentication 
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    try{
                        //Kontrollera att användaren finns
                        var userResponse = userService.findByEmail(email); 

                        if (userResponse != null) {

                            //STEG 5: Validera token mot användare
                            if (jwtUtil.validateToken(jwtToken, email)) {

                                //STEG 6: Skapa Spring security Authentication
                                //Vi använder enkel implementation utan userDetails för nu
                                UsernamePasswordAuthenticationToken authToken = 
                                    new UsernamePasswordAuthenticationToken(email, null, null); //email, credentials authorities
                                
                                //Sätt request details
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                //STEG 7: Sätt authentication i security context 
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                                logger.info("JWT authentication succesful for user: " + email);
                            }else{
                                logger.warn("JWT token validation failed for user: " + email);
                            }

                            
                        } else{
                            logger.warn("User not found for email: " + email);
                        }
                    } catch (Exception e) {
                        logger.error("Error during JWT authentication: " + e.getMessage());

                    }
                    
                }

                //STEG 8: Fortsätt med request chain 
                filterChain.doFilter(request, response);
                
    } 
}
