package com.example.my_board.controller;

import com.example.my_board.service.UserAccountService;
import com.example.my_board.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // view -> template -> html(thymeleaf)
@RequiredArgsConstructor // 생성자 주입
@RequestMapping("/auth") // prefix -> /auth/**
public class AuthController {
    private final UserAccountService userAccountService;

    // 회원가입용 페이지로 전달하는...
    @GetMapping("/register") // join? new? // GET
    public String registerForm() {
        return "register"; // templates/register.html
    }

    // 해당 처리를 Service로 전달해주는...
    // 해당 처리를 Service로 전달해주는...
    @PostMapping("/register") // POST
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        // @Valid -> 유효성 검증
        try {
            userAccountService.register(username, password);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            // 중복사용자
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
//        return "redirect:/auth/login"; // login은 없으니까 403?
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // login.html
    }

    // 의존성 주입
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {

        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            String accessToken = jwtUtil.generateToken(username, authentication.getAuthorities().toString(), false);

            ResponseCookie cookie = ResponseCookie.from("access_token", accessToken).httpOnly(true).path("/").maxAge(3600).build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 마이페이지로 이동
            return "redirect:/my-page";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "로그인 실패");
            return "redirect:/auth/login";
        }
    }

}