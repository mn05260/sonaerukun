package com.example.sonaerukun;
import com.example.sonaerukun.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class Homecontroller {

    @Autowired
    private SonaeruLogic sonaeruLogic;

    @Autowired
    private UserRepository userRepository; // データベースの窓口を追加

    // --- 1. 初期画面（ログイン画面を表示） ---
    @GetMapping("/")
    public String index() {
        return "login"; // まずはログイン画面を見せる
    }

    // --- 2. ログイン処理 ---
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        // データベースからユーザーを探す
        Optional<User> user = userRepository.findById(username);
        
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            model.addAttribute("UserName", username);
            return "index"; // 成功：いつものメイン画面へ
        } else {
            model.addAttribute("error", "ユーザー名またはパスワードが違います");
            return "login"; // 失敗：ログイン画面に戻る
        }
    }

    // --- 3. 新規登録画面の表示 ---
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    // --- 4. 新規登録の実行（データベースへ保存） ---
    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        
        userRepository.save(newUser); // ここでDBに保存！
        return "redirect:/"; // 登録が終わったらログイン画面（ルート）へ
    }

    // --- 5. 計算ロジック（既存のまま） ---
    @PostMapping("/calculate")
    public String calculate(
            @RequestParam("familyCount") int familyCount,
            @RequestParam("maleCount") int maleCount,
            @RequestParam("femaleCount") int femaleCount,
            @RequestParam("childCount") int childCount,
            @RequestParam("infantCount") int infantCount,
            @RequestParam("seniorCount") int seniorCount,
            @RequestParam("days") int days,
            @RequestParam(value = "napkinLevel", required = false, defaultValue = "standard") String napkinLevel,
            Model model) {
        
        SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
                familyCount, maleCount, femaleCount, childCount, infantCount, seniorCount, days, napkinLevel); 
        
        model.addAttribute("rankA", result.rankA);
        model.addAttribute("rankB", result.rankB);
        model.addAttribute("rankC", result.rankC);
        model.addAttribute("storageInfo", result.storageInfo);
        model.addAttribute("UserName", "username");
        return "index";
    }
}