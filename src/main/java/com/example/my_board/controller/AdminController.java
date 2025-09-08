package com.example.my_board.controller;

import com.example.my_board.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    // 사용자 계정 관련 비즈니스 로직을 처리하는 서비스
    private final UserAccountService userAccountService;

    // 회원 목록 페이지
    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userAccountService.findAllUsers());
        return "admin"; // templates/admin.html
    }

    // 회원 강제 탈퇴
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userAccountService.deleteUser(id);
        return "redirect:/admin";
    }
}
