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
            @RequestParam("maleCount") int maleCount,     // 大人（男性）
            @RequestParam("femaleCount") int femaleCount, // 大人（女性）
            @RequestParam("childCount") int childCount,   // 子供
            @RequestParam("infantCount") int infantCount, // 乳幼児
            @RequestParam("seniorCount") int seniorCount, // 高齢者
            @RequestParam("days") int days,
            @RequestParam(value = "napkinLevel", required = false, defaultValue = "standard") String napkinLevel,
            Model model) {
        
        // ロジック（SonaeruLogic）を呼び出す
        SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
                familyCount, maleCount, femaleCount, childCount, infantCount, seniorCount, days, napkinLevel); 
        
        // 結果を画面に渡す
        model.addAttribute("rankA", result.rankA);
        model.addAttribute("rankB", result.rankB);
        model.addAttribute("rankC", result.rankC);
        model.addAttribute("storageInfo", result.storageInfo);
        
        return "index";
    }
}
