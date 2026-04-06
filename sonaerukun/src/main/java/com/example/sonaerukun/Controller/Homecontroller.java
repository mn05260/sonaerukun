package com.example.sonaerukun.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sonaerukun.Service.SonaeruLogic;
import com.example.sonaerukun.model.User;
import com.example.sonaerukun.repository.UserRepository;

import java.util.Optional;
import java.security.Principal;
import java.util.List;

@Controller
public class Homecontroller {

    private final SonaeruLogic sonaeruLogic;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Homecontroller(SonaeruLogic sonaeruLogic, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.sonaeruLogic = sonaeruLogic;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. 【修正】無限ループ防止のため、直接表示せず /login へ飛ばす
  @GetMapping("/index")
public String showIndex(Principal principal, HttpSession session, Model model) {
    if (principal == null) return "redirect:/login"; 
    
    String username = principal.getName();
    session.setAttribute("userName", username);

    User user = userRepository.findById(username).orElse(null);
    if (user == null) return "redirect:/login";

    String hostName = user.getHostName();
    session.setAttribute("hostName", hostName);
    
    // 家族コード生成
    if (user.getJoinCode() == null || user.getJoinCode().isEmpty()) {
        user.setJoinCode(sonaeruLogic.generateFamilyCode());
        userRepository.save(user); 
    }
    
    List<User> members = userRepository.findByHostName(hostName);
    
    // ⭐ 重要：ここで「空の計算結果」または「デフォルトの計算結果」を渡す
    // これがないと、画面側で前のユーザーのキャッシュが表示されることがあります
    model.addAttribute("rankA", "計算ボタンを押してください");
    model.addAttribute("rankB", "計算ボタンを押してください");
    model.addAttribute("rankC", "計算ボタンを押してください");
    model.addAttribute("storageInfo", "");

    model.addAttribute("members", members);
    model.addAttribute("UserName", username);
    model.addAttribute("hostName", hostName);
    model.addAttribute("joinCode", user.getJoinCode()); 

    return "main";
}
    // 2. 【追加】ログイン画面を表示するための専用メソッド
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false)String hostName, Model model) {
        model.addAttribute("hostName", hostName);
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, 
                        @RequestParam(name = "hostName", required = false) String hostName, Model model) { 
        if(userRepository.existsById(username)){
            model.addAttribute("error", "このユーザー名は既に存在しています");
            return "signup";
        }
        if(password.length()<8){
            model.addAttribute("error", "パスワードは8文字以上で設定してください");
            return "signup";
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        if(hostName == null ||hostName.isEmpty()){
            newUser.setHostName(username);
        } else{
            newUser.setHostName(hostName);
        }
        userRepository.save(newUser);
        return "redirect:/login"; // 登録後はログインへ
    }

    // --- 以降、計算ロジックなどは一切変更なし ---

    @PostMapping("/calculate")
    public String calculate(
            @RequestParam("prefecture") String prefecture, 
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
        String hostName = (String) session.getAttribute("hostName");
        if (username == null) return "redirect:/login";

        List<User> members = userRepository.findByHostName(hostName);
        model.addAttribute("members", members);

        SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
                prefecture, familyCount, maleCount, femaleCount, childCount, infantCount, seniorCount, days, napkinLevel); 
        
        model.addAttribute("rankA", result.rankA);
        model.addAttribute("rankB", result.rankB);
        model.addAttribute("rankC", result.rankC);
        model.addAttribute("storageInfo", result.storageInfo);
        model.addAttribute("UserName", username);
        model.addAttribute("selectedPref", prefecture); 
        model.addAttribute("hostName", hostName);
        
        return "main";
    }

    @PostMapping("/joinFamily")
    public String joinFamily(@RequestParam String keyword, HttpSession session) {
        String username = (String) session.getAttribute("userName");
        if (username == null) return "redirect:/login";

        User user = userRepository.findById(username).orElse(null);
        if (user != null) {
            user.setHostName(keyword); 
            userRepository.save(user); 
        }
        session.setAttribute("hostName", keyword);
        return "redirect:/index";
    }
    @GetMapping("/logout")
    public String logout (HttpSession session) {
        session.invalidate(); 
        return "redirect:/login?logout"; 
    }

    @GetMapping("/debug")
    public String debug() {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("ユーザー: " + u.getUsername() + " / パスワード(ハッシュ): " + u.getPassword());
        }
        return "login"; 
    }
}