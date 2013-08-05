package football.gameplay.core;

import football.gameplay.info.*;

/**
 *
 * @author hungcuong
 */
public class FootballRuleManager {

    // Chance
    public static float CHANCE_SCORE = 0.4f;
    static float DISTANCE_NEAR = 4;

    // Feild
    // Range
    boolean isScored() {
        return true;

    }

    boolean isFault() {
        return true;
    }

    boolean isBallInFeild() {
        return true;
    }
    // Luat bi duoi hon 6 cau thu
    boolean continueAfterFault(FootballMatch aMatch) {
        //if (team.)
        return true;
    }
}
