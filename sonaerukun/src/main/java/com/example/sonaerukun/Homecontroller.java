package com.example.sonaerukun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Homecontroller {

    @Autowired
    private SonaeruLogic sonaeruLogic;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    
@PostMapping("/calculate")
public String calculate(
        @RequestParam("familyCount") int familyCount,
        @RequestParam("days") int days,
        @RequestParam("category") String category,
        @RequestParam("femaleCount") int femaleCount,
        @RequestParam(value = "napkinLevel", required = false, defaultValue = "standard") String napkinLevel,
        Model model) {
    String result = sonaeruLogic.calculate(familyCount, days); 
    
    model.addAttribute("result", result);
    return "index";
}
}