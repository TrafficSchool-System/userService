package com.example.userService.features.email.template;

/**
 * Magic Link Email Template
 * 
 * Pure presentation layer - generates HTML content only.
 * No business logic, no infrastructure concerns.
 * 
 * Responsible for:
 * - Building magic link login email HTML structure
 * - Formatting firstName and magicLink into template
 * 
 * NOT responsible for:
 * - Constructing magic links (use-case responsibility)
 * - Sending emails (infrastructure responsibility)
 * - Business validation (use-case responsibility)
 */
public class MagicLinkEmailTemplate {

    /**
     * Generate magic link login email HTML content.
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
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                        .button { display: inline-block; padding: 15px 30px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 10px; margin: 15px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔑 Your Login Link</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>You requested a login link for your Traffic School account.</p>

                            <p>Click the button below to log in:</p>

                            <a href="%s" class="button">Log In to Traffic School</a>

                            <div class="warning">
                                <strong>⚠️ Security Notice:</strong>
                                <p>This link is valid for only 5 minutes and can only be used once.</p>
                            </div>

                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #666; font-size: 12px;">%s</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 Traffic School. All rights reserved.</p>
                            <p><strong>Didn't request this?</strong> You can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(firstName, magicLink, magicLink);
    }
}
