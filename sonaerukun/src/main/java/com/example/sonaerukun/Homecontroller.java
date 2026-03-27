package com.example.sonaerukun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.HashMap;
import java.util.Map;
@Controller
public class Homecontroller {

    @Autowired
    private SonaeruLogic sonaeruLogic;
//ユーザーIDと２０文字のカギを生成する
private static final Map<String, String> USER_KEYS = new HashMap<>();
static{
    USER_KEYS.put("user1","12345678901234567890");
    USER_KEYS.put("user2","09876543210987654321");
}
    @GetMapping("/")
    public String index(
        @RequestParam(name="id", required = false) String id,
        @RequestParam(name="key", required = false) String key,
        Model model

    ) {
        if(id !=null && key != null && key.equals(USER_KEYS.get(id))){
            model.addAttribute("UserName", id);
        return "index";//成功:いつもの画面に移動
    }else{
        return "error";//失敗:エラー画面へ
    }
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
