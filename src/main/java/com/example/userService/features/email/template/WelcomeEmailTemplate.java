package com.example.userService.features.email.template;

/**
 * Welcome Email Template
 * 
 * Pure presentation layer - generates HTML content only.
 * No business logic, no infrastructure concerns.
 * 
 * Responsible for:
 * - Building welcome email HTML structure
 * - Formatting firstName and magicLink into template
 * 
 * NOT responsible for:
 * - Constructing magic links (use-case responsibility)
 * - Sending emails (infrastructure responsibility)
 * - Business validation (use-case responsibility)
 */
public class WelcomeEmailTemplate {

    /**
     * Generate welcome email HTML content.
     * 
     * @param firstName User's first name for personalization
     * @param magicLink Complete magic link URL (constructed by use-case)
     * @return HTML email content
     */
    public static String build(String firstName, String magicLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                        .button { display: inline-block; padding: 15px 30px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🚗 Welcome to Traffic School!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Thank you for registering with Traffic School. We're excited to help you on your journey to getting your driver's license!</p>

                            <p>Click the button below to get started:</p>

                            <a href="%s" class="button">Get Started</a>

                            <p><small>This link is valid for 30 minutes.</small></p>

                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666; font-size: 12px;">%s</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 Traffic School. All rights reserved.</p>
                            <p>If you didn't create this account, please ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(firstName, magicLink, magicLink);
    }
}
