package com.example.my_board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // view resolver
public class MainController {
    @GetMapping
    public String index() {
        return "index";
    }
}