package com.learn2code.jobportal.controller;

import org.springframework.stereotype.Controller;

@Controller
public class HomeController {
    public String home(){
        return "index";
    }
}
