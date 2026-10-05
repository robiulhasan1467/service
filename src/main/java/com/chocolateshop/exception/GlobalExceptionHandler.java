package com.chocolateshop.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for handling unexpected errors gracefully.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public void handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) throws org.springframework.web.servlet.resource.NoResourceFoundException {
        throw ex;
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled exception caught by GlobalExceptionHandler: ", ex);
        model.addAttribute("errorTitle", "An Unexpected Error Occurred");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
