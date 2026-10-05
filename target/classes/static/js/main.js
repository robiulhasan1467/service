/**
 * Chocolate Shop Management System - Client JavaScript
 */
document.addEventListener('DOMContentLoaded', () => {
    // 0. Persisted interface language
    const languageToggle = document.getElementById('languageToggle');
    const languageToggleLabel = document.getElementById('languageToggleLabel');
    const languageKey = 'jaha-language';

    const setLanguage = (language) => {
        const isBengali = language === 'bn';
        document.documentElement.lang = language;
        document.querySelectorAll('.language-en').forEach((element) => {
            element.hidden = isBengali;
        });
        document.querySelectorAll('.language-bn').forEach((element) => {
            element.hidden = !isBengali;
        });

        if (languageToggle && languageToggleLabel) {
            languageToggleLabel.textContent = isBengali ? 'English' : 'বাংলা';
            languageToggle.setAttribute('aria-label', `Switch language to ${isBengali ? 'English' : 'Bengali'}`);
            languageToggle.setAttribute('aria-pressed', String(isBengali));
        }
    };

    let savedLanguage = 'en';
    try {
        savedLanguage = localStorage.getItem(languageKey) === 'bn' ? 'bn' : 'en';
    } catch (error) {
        // Keep English as the default when browser storage is unavailable.
    }
    setLanguage(savedLanguage);

    if (languageToggle) {
        languageToggle.addEventListener('click', () => {
            const language = document.documentElement.lang === 'bn' ? 'en' : 'bn';
            setLanguage(language);
            try {
                localStorage.setItem(languageKey, language);
            } catch (error) {
                // The language still changes for the current page.
            }
        });
    }

    // Persist light, dark, and system theme preference
    const themeKey = 'jaha-theme';
    const themeChoices = document.querySelectorAll('[data-theme-choice]');
    const applyTheme = (theme) => {
        document.documentElement.dataset.theme = theme;
        themeChoices.forEach((choice) => {
            choice.setAttribute('aria-pressed', String(choice.dataset.themeChoice === theme));
        });
        document.dispatchEvent(new CustomEvent('jaha-theme-change', { detail: { theme } }));
    };

    let savedTheme = ['light', 'dark', 'system'].includes(document.documentElement.dataset.theme)
        ? document.documentElement.dataset.theme
        : 'system';
    try {
        const storedTheme = localStorage.getItem(themeKey);
        if (['light', 'dark', 'system'].includes(storedTheme)) savedTheme = storedTheme;
    } catch (error) {
        // System appearance remains the default when browser storage is unavailable.
    }
    applyTheme(savedTheme);

    themeChoices.forEach((choice) => {
        choice.addEventListener('click', () => {
            const theme = choice.dataset.themeChoice;
            applyTheme(theme);
            try {
                localStorage.setItem(themeKey, theme);
            } catch (error) {
                // The selected theme still applies for the current page.
            }
            document.cookie = `${themeKey}=${theme}; Max-Age=31536000; Path=/; SameSite=Lax`;
        });
    });

    // Search visible tables and product cards
    const globalSearch = document.getElementById('globalSearch');
    if (globalSearch) {
        globalSearch.addEventListener('input', () => {
            const query = globalSearch.value.trim().toLocaleLowerCase();
            document.querySelectorAll('tbody tr').forEach((row) => {
                row.hidden = query !== '' && !row.textContent.toLocaleLowerCase().includes(query);
            });
            document.querySelectorAll('.product-card, .product-grid-item').forEach((card) => {
                card.hidden = query !== '' && !card.textContent.toLocaleLowerCase().includes(query);
            });
        });
    }

    // 1. Password visibility toggle
    const togglePasswordBtn = document.getElementById('togglePasswordBtn');
    const passwordInput = document.getElementById('password');

    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = togglePasswordBtn.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    }

    // 2. Quick Demo Credentials Autofill Helper
    const fillAdminBtn = document.getElementById('btnFillAdmin');
    const fillStaffBtn = document.getElementById('btnFillStaff');
    const usernameInput = document.getElementById('username');

    if (fillAdminBtn && usernameInput && passwordInput) {
        fillAdminBtn.addEventListener('click', (e) => {
            e.preventDefault();
            usernameInput.value = 'admin';
            passwordInput.value = 'admin123';
            usernameInput.focus();
        });
    }

    if (fillStaffBtn && usernameInput && passwordInput) {
        fillStaffBtn.addEventListener('click', (e) => {
            e.preventDefault();
            usernameInput.value = 'cashier';
            passwordInput.value = 'cashier123';
            usernameInput.focus();
        });
    }

    // 3. Form Submit Loading State
    const loginForm = document.getElementById('loginForm');
    const submitBtn = document.getElementById('submitLoginBtn');
    if (loginForm && submitBtn) {
        loginForm.addEventListener('submit', () => {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Signing in...
            `;
        });
    }

    // 4. Dynamic Greeting on Home Page
    const greetingEl = document.getElementById('dynamicGreeting');
    if (greetingEl) {
        const hour = new Date().getHours();
        let greeting = 'Welcome';
        if (hour < 12) greeting = 'Good Morning';
        else if (hour < 18) greeting = 'Good Afternoon';
        else greeting = 'Good Evening';
        greetingEl.textContent = greeting;
    }

    // 5. Auto dismiss alert after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            try {
                const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
                if (bsAlert) bsAlert.close();
            } catch (err) {
                // Ignore if bootstrap is not fully loaded
            }
        }, 6000);
    });
});
