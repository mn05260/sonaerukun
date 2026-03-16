package com.example.sonaerukun;
import org.springframework.stereotype.Service;

@Service
public class SonaeruLogic {
    public String calculate(int familyCount, int days) {
        int water = familyCount * 3 * days;
        int food = familyCount * 3 * days;
        int toilet = familyCount * 5 * days; // 1日5回分が推奨
        int wetTissue = familyCount * 2;     // 日数に関わらずまずは2個くらい

        return String.format("【%d日分の備蓄】水: %dL, 食料: %d食, トイレ: %d回分, ウェットティッシュ: %d個", 
                             days, water, food, toilet, wetTissue);
    }
}
