package com.productcnit.apigateway.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HomeController {
    @GetMapping("/usertoken")
    public String index(Principal principal) {

        return principal.getName();
    }
    @GetMapping("/usertoken1")
    public String index1() {

        return "usertoken1";
    }
}
