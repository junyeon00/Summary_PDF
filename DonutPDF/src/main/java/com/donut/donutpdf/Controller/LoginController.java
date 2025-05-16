package com.donut.donutpdf.Controller;

import com.donut.donutpdf.Dto.AddUserRequest;
import com.donut.donutpdf.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup"; // signup.html
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute AddUserRequest request) {
        userService.save(request);
        return "redirect:/login";
    }

    @GetMapping("/upload")
    public String uploadPage(){
        return "upload";
    }

    @GetMapping("/chat")
    public String chatPage(){
        return "chat";
    }

    @GetMapping("/mypage")
    public String mypage(){
        return "mypage";
    }

    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/login";
    }
}
