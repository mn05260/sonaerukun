package com.example.sonaerukun;
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
    
    if (username == null || username.isEmpty()) {
        model.addAttribute("error", "ユーザー名またはパスワードが違います");
        return "login";
    }
    
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
        //合言葉をHTMLに渡す
        model.addAttribute("hostName", hostName);
        return "index";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(required = false)String hostName, Model model) {
        model.addAttribute("hostName", hostName);
        return "signup";

    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, 
                        @RequestParam(name = "hostName", required = false) String hostName, Model model) { // ★QRコード等から受け取る想定
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
        @RequestParam("prefecture") String prefecture, // ★追加：都道府県を受け取る
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

    // ★修正：第1引数に prefecture を追加
    SonaeruLogic.PreparednessResult result = sonaeruLogic.calculate(
            prefecture, familyCount, maleCount, femaleCount, childCount, infantCount, seniorCount, days, napkinLevel); 
    
    // 結果を画面に渡す
    model.addAttribute("rankA", result.rankA);
    model.addAttribute("rankB", result.rankB);
    model.addAttribute("rankC", result.rankC);
    model.addAttribute("storageInfo", result.storageInfo);
    model.addAttribute("UserName", username);
    model.addAttribute("selectedPref", prefecture); // 画面に「〇〇県の計算結果」と出すならこれも便利
    
    // 合言葉を渡すのを忘れずに（既存のindex表示に合わせる）
    model.addAttribute("hostName", hostName);
    
    return "index";
}
@PostMapping("/joinFamily")
public String joinFamily(@RequestParam String keyword, HttpSession session) {

    String username = (String) session.getAttribute("userName");
    if (username == null) return "redirect:/";

    // 今ログインしてるユーザー取得
    User user = userRepository.findById(username).orElse(null);
    if (user != null) {
        user.setHostName(keyword); // ← 合言葉をhostNameにセット
        userRepository.save(user); // ← DB更新
    }
    session.setAttribute("hostName", keyword);

    return "redirect:/index";
}
@GetMapping("/debug")
public String debug() {
    List<User> users = userRepository.findAll();
    for (User u : users) {
        System.out.println("ユーザー: " + u.getUsername() + " / hostName: " + u.getHostName());
    }
    return "login"; 
}
}