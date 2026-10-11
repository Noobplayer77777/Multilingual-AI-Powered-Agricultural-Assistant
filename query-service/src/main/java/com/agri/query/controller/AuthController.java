package com.agri.query.controller;

import com.agri.query.dto.ForgotPasswordRequest;
import com.agri.query.dto.PasswordResetRequest;
import com.agri.query.dto.RegisterRequest;
import com.agri.query.dto.UserDto;
import com.agri.query.security.RateLimiterService;
import com.agri.query.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiterService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
        this.authenticationManager = authenticationManager;
    }

    // --- MVC View Endpoints ---

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            @RequestParam(value = "reset", required = false) String reset,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in with your credentials.");
        }
        if (reset != null) {
            model.addAttribute("successMessage", "Password updated successfully! Please sign in with your new password.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String handleWebRegister(@ModelAttribute RegisterRequest request,
                                    HttpServletRequest httpRequest,
                                    RedirectAttributes redirectAttributes) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Too many requests. Please wait a minute and try again.");
            return "redirect:/register";
        }
        try {
            authService.register(request);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleWebForgotPassword(@RequestParam("email") String email,
                                          HttpServletRequest httpRequest,
                                          RedirectAttributes redirectAttributes) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Too many requests. Please wait a minute and try again.");
            return "redirect:/forgot-password";
        }
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail(email);
        authService.forgotPassword(req);
        redirectAttributes.addFlashAttribute("successMessage", "If an account matches that email, password reset instructions have been issued.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "token", required = false) String token, Model model) {
        model.addAttribute("token", token != null ? token : "");
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleWebResetPassword(@RequestParam("token") String token,
                                         @RequestParam("newPassword") String newPassword,
                                         @RequestParam("confirmPassword") String confirmPassword,
                                         HttpServletRequest httpRequest,
                                         RedirectAttributes redirectAttributes) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Too many requests. Please wait a minute and try again.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            PasswordResetRequest req = new PasswordResetRequest(token, newPassword);
            authService.resetPassword(req);
            return "redirect:/login?reset=true";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    // --- REST API Endpoints ---

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many requests. Rate limit is 15 requests per minute."));
        }
        try {
            UserDto user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> credentials, HttpServletRequest httpRequest) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many requests. Rate limit is 15 requests per minute."));
        }
        String email = credentials.get("email");
        String password = credentials.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(), password)
            );
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
            UserDto user = authService.getCurrentUserDto();
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<?> apiForgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many requests. Rate limit is 15 requests per minute."));
        }
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If an account matches that email, reset instructions have been issued."));
    }

    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<?> apiResetPassword(@RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        if (!rateLimiterService.isAllowed(getClientIp(httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many requests. Rate limit is 15 requests per minute."));
        }
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", "Password has been successfully updated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/me")
    @ResponseBody
    public ResponseEntity<?> apiMe() {
        UserDto user = authService.getCurrentUserDto();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(user);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
