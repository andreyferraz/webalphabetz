package com.alphabetz.webalphabetz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPagesController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String overview() {
        return "admin/index";
    }

    @GetMapping("/admin/slides")
    public String slides() {
        return "admin/slides";
    }

    @GetMapping("/admin/blog")
    public String blog() {
        return "admin/blog";
    }

    @GetMapping("/admin/seguranca")
    public String security() {
        return "admin/seguranca";
    }
}
