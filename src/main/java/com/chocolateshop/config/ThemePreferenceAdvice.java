package com.chocolateshop.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ThemePreferenceAdvice {

    @ModelAttribute
    public void addThemePreference(
            @CookieValue(name = "jaha-theme", required = false) String savedTheme,
            CsrfToken csrfToken,
            Model model) {
        csrfToken.getToken();
        String theme = "light".equals(savedTheme) || "dark".equals(savedTheme)
                ? savedTheme
                : "system";
        model.addAttribute("theme", theme);
    }
}