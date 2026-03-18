package com.example.sonaerukun;
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
    public PreparednessResult calculate(int familyCount, int infantCount, int seniorCount, int days, int femaleCount, String napkinLevel) {
        PreparednessResult result = new PreparednessResult();

        // --- ランクA（持ち出し用：すぐに逃げるためのセット） ---
        int waterA = familyCount * 1; 
        int foodA = familyCount * 3;
        int toiletA = familyCount * 5;
        int napkinA = femaleCount * 5; 
       

        // 乳幼児・高齢者用の持ち出し品
        String extraA = "";
        if (infantCount > 0) extraA += ", 液体ミルク, 抱っこ紐, おむつ(5枚)";
        if (seniorCount > 0) extraA += ", 持病の薬, お薬手帳のコピー";

        result.rankA = String.format("水: %dL, 食料: %d食, トイレ: %d回分, 生理用品: %d個%s, モバイルバッテリー, ライト, 現金", 
                                      waterA, foodA, toiletA, napkinA, extraA);

        // --- ランクB（自宅備蓄用：ライフライン停止に備える） ---
        int waterB = (familyCount * 3 * days) - waterA;
        int foodB = (familyCount * 3 * days) - foodA;
        int toiletB = (familyCount * 5 * days) - toiletA;

        // 生理用品の追加計算（napkinLevelによる倍率）
        double napkinMultiplier = napkinLevel.equals("high") ? 1.5 : (napkinLevel.equals("low") ? 0.7 : 1.0);
        int napkinB = (int)((femaleCount * 5 * days * napkinMultiplier) - napkinA);
        if (napkinB < 0) napkinB = 0;

        StringBuilder rankBBuilder = new StringBuilder();
        rankBBuilder.append(String.format("水: %dL, 食料: %d食, トイレ: %d回分, 生理用品: %d個", waterB, foodB, toiletB, napkinB));

        // 乳幼児の備蓄品計算
        if (infantCount > 0) {
            int diapersB = infantCount * 5 * days; // 1日5枚計算
            rankBBuilder.append(String.format(", おむつ: %d枚, おしりふき, 離乳食", diapersB));
        }

        // 高齢者の備蓄品
        if (seniorCount > 0) {
            rankBBuilder.append(", おかゆ・とろみ剤, 入れ歯洗浄剤, 予備の老眼鏡");
        }
        StringBuilder rankCBuilder = new StringBuilder();
        rankCBuilder.append("軍手,マスク,ブルーシート,工具セット,筆記用具,予備の電池");
        if(infantCount > 0){
            rankCBuilder.append(",哺乳瓶の予備,お気に入りのおもちゃ");
        }
        if(seniorCount > 0){
            rankCBuilder.append(",予備の補聴器電池,湿布,塗り薬");
        }

    result.rankB = rankBBuilder.toString();
    result.rankC = rankCBuilder.toString();

    int waterTotal = waterA + waterB;
    int waterBoxes = (int) Math.ceil((double) waterTotal / 12); 

    // 食料のボリューム感（ざっくり 18食で段ボール1箱分と仮定）
    int foodTotal = foodA + foodB;
    int foodBoxes = (int) Math.ceil((double) foodTotal / 18);

    // 結果にセット（Resultクラスに String storageInfo を追加しておくと楽です）
    result.storageInfo = String.format("📦 備蓄スペースの目安：水（6本入り）約%d箱、食料 約%d箱分", 
                                        waterBoxes, foodBoxes);

    return result;
}
}