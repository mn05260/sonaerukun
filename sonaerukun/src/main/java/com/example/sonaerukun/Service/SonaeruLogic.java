package com.example.sonaerukun.Service;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SonaeruLogic {
    public static class PreparednessResult {
        public String rankA;
        public String rankB;
        public String rankC;
        public String storageInfo;
    }

    // 引数に infantCount と seniorCount を追加
    public PreparednessResult calculate(String prefecture, int familyCount, int maleCount, int femaleCount, int childCount, int infantCount, int seniorCount, int days, String napkinLevel) {
    PreparednessResult result = new PreparednessResult();
    familyCount = Math.max(0, familyCount);
    maleCount = Math.max(0, maleCount);
    femaleCount = Math.max(0, femaleCount);
    childCount = Math.max(0, childCount);
    infantCount = Math.max(0, infantCount);
    seniorCount = Math.max(0, seniorCount);
    days = Math.max(1, days); // 日数は最低1日にしておくのが安全

    // --- 🌍 地域特性の判定ロジック --- (ここから下はそのまま)
    // --- 🌍 地域特性の判定ロジック ---
    String regionalItemsA = "";
    String regionalItemsB = "";
    String regionalMessage = "";

    // 💡 判定グループ（キーワードで判定すると47個書かなくて済みます）
    if (prefecture.equals("北海道") || prefecture.contains("青森") || prefecture.contains("岩手") || prefecture.contains("秋田") || prefecture.contains("山形") || prefecture.contains("新潟")) {
        // ❄️ 豪雪・寒冷地グループ
        regionalItemsA = ", 折りたたみスコップ, 携帯カイロ(10個)";
        regionalItemsB = ", ポータブルストーブ, 厚手の毛布, 燃料予備";
        regionalMessage = "【地域アドバイス】冬の寒さと積雪への備えを強化しています。";
    } else if (prefecture.equals("東京都") || prefecture.equals("神奈川県") || prefecture.equals("大阪府") || prefecture.equals("愛知県") || prefecture.equals("兵庫県")) {
        // 🚶 都市型・帰宅困難グループ
        regionalItemsA = ", 帰宅困難用スニーカー, 携帯地図";
        regionalItemsB = ", 大容量モバイルバッテリー, 簡易トイレ(多め)";
        regionalMessage = "【地域アドバイス】都市部での孤立と帰宅困難を想定した備えを追加しました。";
    } else if (prefecture.equals("静岡県") || prefecture.equals("高知県") || prefecture.equals("和歌山県") || prefecture.equals("徳島県")) {
        // 🌊 津波・沿岸リスクグループ
        regionalItemsA = ", ライフジャケット, ホイッスル";
        regionalItemsB = ", 浸水防止袋, 浮力体付きリュック";
        regionalMessage = "【地域アドバイス】沿岸部特有の津波避難を想定したアイテムを推奨しています。";
    }

    // --- 基礎知識：大人と子供で係数を分ける ---
    // 食料：大人は1日3食、子供は2食（または少なめ）として計算
    double foodVolume = (maleCount + femaleCount + seniorCount) * 3.0 + (childCount * 2.0);
    // 水：大人は1日3L、子供は2L
    double waterVolumePerDay = (maleCount + femaleCount + seniorCount) * 3.0 + (childCount * 2.0);

    // --- ランクA（持ち出し用） ---
    int waterA = familyCount * 1; 
    int foodA = familyCount * 3;
    int toiletA = familyCount * 5;
    int napkinA = femaleCount * 5; 

    String extraA = "";
    if (infantCount > 0) extraA += ", 液体ミルク, 抱っこ紐, おむつ(5枚)";
    if (seniorCount > 0) extraA += ", 持病の薬, お薬手帳のコピー";
    if (childCount > 0) extraA += ", 家族の写真, お気に入りのお菓子"; // 子供向け追加
    if (maleCount > 0) extraA += ", カミソリ"; // 男性向け追加
extraA += regionalItemsA;
    result.rankA = String.format("水: %dL, 食料: %d食, トイレ: %d回分, 生理用品: %d個%s, モバイルバッテリー, ライト, 現金", 
                                  waterA, foodA, toiletA, napkinA, extraA);

    // --- ランクB（自宅備蓄用） ---
    // 日数に応じた合計から、持ち出し分を引く
    int waterB = (int)(waterVolumePerDay * days) - waterA;
    int foodB = (int)(foodVolume * days) - foodA;
    int toiletB = (familyCount * 5 * days) - toiletA;

    // 生理用品計算
    double napkinMultiplier = napkinLevel.equals("high") ? 1.5 : (napkinLevel.equals("low") ? 0.7 : 1.0);
    int napkinB = (int)((femaleCount * 5 * days * napkinMultiplier) - napkinA);
    if (napkinB < 0) napkinB = 0;

    StringBuilder rankBBuilder = new StringBuilder();
    rankBBuilder.append(String.format("水: %dL, 食料: %d食, トイレ: %d回分, 生理用品: %d個", Math.max(0, waterB), Math.max(0, foodB), Math.max(0, toiletB), napkinB));
rankBBuilder.append(regionalItemsB);
    if (infantCount > 0) {
        int diapersB = infantCount * 5 * days;
        rankBBuilder.append(String.format(", おむつ: %d枚, おしりふき, 離乳食", diapersB));
    }
    if (seniorCount > 0) {
        rankBBuilder.append(", おかゆ・とろみ剤, 入れ歯洗浄剤, 予備の老眼鏡");
    }
    if (childCount > 0) {
        rankBBuilder.append(", 子供用歯ブラシ, 簡易トランプ（暇つぶし用）");
    }

    // --- ランクC ---
    StringBuilder rankCBuilder = new StringBuilder();
    rankCBuilder.append("軍手, マスク, ブルーシート, 工具セット, 筆記用具, 予備の電池");
    if(infantCount > 0) rankCBuilder.append(", 哺乳瓶の予備, おもちゃ");
    if(seniorCount > 0) rankCBuilder.append(", 補聴器電池, 湿布, 塗り薬");

    result.rankB = rankBBuilder.toString();
    result.rankC = rankCBuilder.toString();

    // 備蓄スペース計算
    int waterTotal = waterA + Math.max(0, waterB);
    int waterBoxes = (int) Math.ceil((double) waterTotal / 12); 
    int foodTotal = foodA + Math.max(0, foodB);
    int foodBoxes = (int) Math.ceil((double) foodTotal / 18);

    result.storageInfo = String.format("📦 備蓄スペースの目安：水（2L×6本入り）約%d箱、食料 約%d箱分", waterBoxes, foodBoxes);

    return result;
}
public String generateFamilyCode() {
    // 10文字にする場合は substring(0, 10) に書き換えてください
    return UUID.randomUUID().toString().substring(0,10).toUpperCase();
}
}