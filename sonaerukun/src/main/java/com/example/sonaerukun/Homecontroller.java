package com.example.sonaerukun;

import com.example.sonaerukun.UserRepository;
import jakarta.servlet.http.HttpSession; // ← これを追加！
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
    private UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "login";
    }

    // --- 2. ログイン処理（セッションに保存するように修正） ---
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, 
                        HttpSession session, Model model) {
        
        Optional<User> user = userRepository.findById(username);
        
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            session.setAttribute("userName", username); 
            return "redirect:/index"; 
        } else {
            model.addAttribute("error", "ユーザー名またはパスワードが違います");
            return "login";
        }
    }
    @GetMapping("/index")
    public String showIndex(HttpSession session, Model model) {
        String username = (String) session.getAttribute("userName");
        if (username == null) return "redirect:/"; // ログインしてなきゃ追い返す
        
        model.addAttribute("UserName", username);
        return "index";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userRepository.save(newUser);
        return "redirect:/";
    }
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
            HttpSession session, 
            Model model) {
        String username = (String) session.getAttribute("userName");
        if (username == null) return "redirect:/";

        SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
                familyCount, maleCount, femaleCount, childCount, infantCount, seniorCount, days, napkinLevel); 
        
        model.addAttribute("rankA", result.rankA);
        model.addAttribute("rankB", result.rankB);
        model.addAttribute("rankC", result.rankC);
        model.addAttribute("storageInfo", result.storageInfo);
        model.addAttribute("UserName", username);
        return "index";
    }
}