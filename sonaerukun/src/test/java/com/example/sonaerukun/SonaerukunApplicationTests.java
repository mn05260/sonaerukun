package com.example.sonaerukun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // これを追加！
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired; // これを追加！
import com.example.sonaerukun.Service.SonaeruLogic; // あなたのLogicをインポート！
import com.example.sonaerukun.repository.UserRepository;
import com.example.sonaerukun.model.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class SonaerukunApplicationTests {

    @Autowired
    private SonaeruLogic sonaeruLogic; // Springにお願いしてLogicを持ってきてもらう
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    // ... 既存の Autowired の近くに追加 ...
    @org.springframework.beans.factory.annotation.Value("${firebase.api.key:MISSING}")
    private String apiKey;

    @Test
    void contextLoads() {
        
    }

    @Test
    void testWaterCalculation() {
        var result = sonaeruLogic.calculate("東京都", 2, 1, 1, 0, 0, 0, 3, "standard");
        assertNotNull(result.rankB);
        assertTrue(result.rankB.contains("水"), "結果に水が含まれていません！");
        System.out.println("テスト成功！計算結果: " + result.rankB);
    }
	@ParameterizedTest
@ValueSource(strings = {"北海道", "静岡県", "東京都","沖縄県","青森県 "})
void testVariousPrefectures(String pref) {
    var result = sonaeruLogic.calculate(pref, 1, 1, 0, 0, 0, 0, 3, "standard");
    assertNotNull(result.rankB);
    System.out.println(pref + "のテスト完了");
}
@Autowired
private UserRepository userRepository;

@Test
void testJoinFamilyLogic() {
    // 1. 準備：テスト用のユーザーを作る
    User testUser = new User();
    testUser.setUsername("testUser1");
    testUser.setPassword("password123");
    testUser.setHostName("initialHost"); // 最初は自分一人のホスト名
    userRepository.save(testUser);

    // 2. 実行：合言葉「KAZOKU_2024」で同期を試みる（Controllerのロジックを再現）
    String keyword = "KAZOKU_2024";
    User user = userRepository.findById("testUser1").orElseThrow();
    user.setHostName(keyword); // 合言葉をセット
    userRepository.save(user);

    // 3. 検証：DBからもう一度取ってきて、ホスト名が変わっているか？
    User updatedUser = userRepository.findById("testUser1").orElseThrow();
    assertEquals("KAZOKU_2024", updatedUser.getHostName(), "合言葉による同期が失敗しています");
}
@Test
void testRandomCodeGeneration() {
    SonaeruLogic logic = new SonaeruLogic();
    String code1 = logic.generateFamilyCode();
    String code2 = logic.generateFamilyCode();

    assertNotNull(code1);
    assertEquals(10, code1.length()); 
    assertNotEquals(code1, code2);  
}

@Test
void testNegativeValues() {
    // 家族人数を -5 とかにして計算
    var result = sonaeruLogic.calculate("東京都", -5, 0, 0, 0, 0, 0, -3, "standard");

    assertNotNull(result);

    // rankA, rankB, rankC の文字列の中に "-" が入っていないかチェック
    // (もし計算結果がマイナスなら "-15L" のようにマイナス記号が出るはずなので)
    assertFalse(result.rankA.contains("-"), "Rank A にマイナスの数値が含まれています: " + result.rankA);
    assertFalse(result.rankB.contains("-"), "Rank B にマイナスの数値が含まれています: " + result.rankB);
    assertFalse(result.rankC.contains("-"), "Rank C にマイナスの数値が含まれています: " + result.rankC);
    
    System.out.println("異常系テスト成功！結果: " + result.rankA);
}
@Test
void testZeroDaysCalculation() {
    var result = sonaeruLogic.calculate("東京都", 3, 1, 1, 1, 0, 0, 0, "standard");
    assertNotNull(result);
    assertTrue(result.rankB.contains("0"), "0日分なら備蓄量は0になるはず");
}
@Test
void testEmptyPrefecture() {
    // prefecture に "" (空文字) を入れても落ちないか
    var result = sonaeruLogic.calculate("", 3, 1, 1, 1, 0, 0, 3, "standard");
    assertNotNull(result);
} 
   
	@Test
void testPasswordEncryption() {
    String rawPassword = "myPassword123";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    // 生のパスワードと暗号化後の文字列が違うことを確認
    assertNotEquals(rawPassword, encodedPassword);
    // passwordEncoder.matches で正しく照合できるか確認
    assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
}
@Test
    void testFirebaseApiKeySecurity() throws java.io.IOException {
        // 1. 環境変数が正しくロードされているかチェック
        assertNotEquals("MISSING", apiKey, "🚨 APIキーが環境変数から読み込めていません！.envファイルを確認してください。");
        assertTrue(apiKey.startsWith("AIza"), "🚨 APIキーの形式が不正です（AIzaから始まっていません）。");

        // 2. ソースコード（app.js）に生のキーが書き込まれていないかスキャン
        // パスはあなたの環境に合わせて調整してください（通常は以下）
        java.nio.file.Path appJsPath = java.nio.file.Path.of("src/main/resources/static/javascript/app.js");
        
        if (java.nio.file.Files.exists(appJsPath)) {
            String content = java.nio.file.Files.readString(appJsPath);
            // 「"AIza」という文字列がファイル内に直接書かれていたらアウト
            assertFalse(content.contains("\"AIza"), "🚨 警告: app.js にAPIキーが直書きされています！");
            System.out.println("✅ ソースコードのスキャン成功: 生のキーは見つかりませんでした。");
        }
    }

    @Test
    void testFinalSafetyCheck() {
        // 履歴に残っていた「古いキー」が今の環境に使われていないか念のため確認
        // (あなたが新しく発行したキーの冒頭数文字をここに入れる)
        assertTrue(apiKey.contains("SyDjS"), "🚨 新しいキーが適用されていません！古いキーが残っている可能性があります。");
        System.out.println("✅ 安全確認完了: 現在は新しいAPIキーが有効です。");
    }
}
