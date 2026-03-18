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
        @RequestParam("infantCount") int infantCount,   // ★追加
        @RequestParam("seniorCount") int seniorCount,   // ★追加
        @RequestParam("days") int days,
        @RequestParam(value = "category", required = false) String category, // ★任意に
        @RequestParam("femaleCount") int femaleCount,
        @RequestParam(value = "napkinLevel", required = false, defaultValue = "standard") String napkinLevel,
        Model model) {
    
    // ロジックへ渡す引数に乳幼児と高齢者を追加
    SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
            familyCount, infantCount, seniorCount, days, femaleCount, napkinLevel); 
    
    model.addAttribute("rankA", result.rankA);
    model.addAttribute("rankB", result.rankB);
    model.addAttribute("rankC",result.rankC);
    model.addAttribute("storageInfo", result.storageInfo);
    return "index";
}
}