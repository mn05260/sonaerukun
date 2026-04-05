package com.example.sonaerukun.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ★追加
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sonaerukun.Service.SonaeruLogic;
import com.example.sonaerukun.model.User;
import com.example.sonaerukun.repository.UserRepository;

import java.util.Optional;

import java.util.List; 



    @Controller
    public class Homecontroller {

    // 1. @Autowiredを消して、private finalに変える
    private final SonaeruLogic sonaeruLogic;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 2. このコンストラクタを追加する
    public Homecontroller(SonaeruLogic sonaeruLogic, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.sonaeruLogic = sonaeruLogic;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index() {
        return "login";
    }

   @PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String password, 
                    HttpSession session, Model model) {
    
    if (username == null || username.isEmpty()) {
        model.addAttribute("error", "ユーザー名またはパスワードが違います");
        return "login";
    }
    
    Optional<User> userOpt = userRepository.findById(username);
    
    // ★修正：passwordEncoder.matches を使うように変更（それ以外はそのまま）
    if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
        User user = userOpt.get(); 
        if (!user.isEnabled()) { 
            model.addAttribute("error", "このアカウントは現在停止されています。管理者にお問い合わせください。");
            return "login";
        }

        session.setAttribute("userName", username); 
        session.setAttribute("hostName", user.getHostName()); 
        
        return "redirect:/index"; 
    } else {
        model.addAttribute("error", "ユーザー名またはパスワードが違います");
        return "login";
    }
}

  @GetMapping("/index")
public String showIndex(HttpSession session, Model model) {
    String username = (String) session.getAttribute("userName");
    if (username == null) return "redirect:/"; 
    
    User user = userRepository.findById(username).orElse(null);
    if (user == null) return "redirect:/";
    if (user.getJoinCode() == null || user.getJoinCode().isEmpty()) {
        String newCode = sonaeruLogic.generateFamilyCode(); 
        user.setJoinCode(newCode);
        userRepository.save(user); 
    }
    
    String hostName = user.getHostName();
    List<User> members = userRepository.findByHostName(hostName);
    
    model.addAttribute("members", members);
    model.addAttribute("UserName", username);
    model.addAttribute("hostName", hostName);
    model.addAttribute("joinCode", user.getJoinCode()); 

    return "index";
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
        
        // ★修正：保存するパスワードをハッシュ化（それ以外はそのまま）
        newUser.setPassword(passwordEncoder.encode(password));

        if(hostName == null ||hostName.isEmpty()){
            newUser.setHostName(username);
        } else{
            newUser.setHostName(hostName);
        }
        userRepository.save(newUser);
        return "redirect:/";
    }

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
    if (username == null) return "redirect:/";

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
    
    return "index";
}

@PostMapping("/joinFamily")
public String joinFamily(@RequestParam String keyword, HttpSession session) {

    String username = (String) session.getAttribute("userName");
    if (username == null) return "redirect:/";

    User user = userRepository.findById(username).orElse(null);
    if (user != null) {
        user.setHostName(keyword); 
        userRepository.save(user); 
    }
    session.setAttribute("hostName", keyword);

    return "redirect:/index";
}

@GetMapping("/debug")
public String debug() {
    List<User> users = userRepository.findAll();
    for (User u : users) {
        // ★修正：デバッグで見やすいように文言だけ少し調整
        System.out.println("ユーザー: " + u.getUsername() + " / パスワード(ハッシュ): " + u.getPassword());
    }
    return "login"; 
}
}