package com.example.sonaerukun;

import com.example.sonaerukun.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import java.util.List; 

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

   @PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String password, 
                    HttpSession session, Model model) {
    
    Optional<User> userOpt = userRepository.findById(username);
    
    if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
        User user = userOpt.get(); // Optionalから中身を取り出す
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
        String hostName = (String) session.getAttribute("hostName"); // ★追加
        
        if (username == null) return "redirect:/"; 
        
        List<User> members = userRepository.findByHostName(hostName);
        model.addAttribute("members", members);
        
        model.addAttribute("UserName", username);
        return "index";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false)String hostName, Model model) {
        model.addAttribute("hostName", hostName);
        return "signup";

    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, 
                        @RequestParam(required = false) String hostName) { // ★QRコード等から受け取る想定
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        if(hostName == null ||hostName.isEmpty()){
            //QR経由じゃない場合は、自分の名前をホスト名にする
            newUser.setHostName(username);
        } else{
            //QR経由の場合は、送られてきたホスト名を設定する
            newUser.setHostName(hostName);
        }
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
    String hostName = (String) session.getAttribute("hostName");
    if (username == null) return "redirect:/";

    // 家族メンバーのリストを取得
    List<User> members = userRepository.findByHostName(hostName);
    model.addAttribute("members", members);

    
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