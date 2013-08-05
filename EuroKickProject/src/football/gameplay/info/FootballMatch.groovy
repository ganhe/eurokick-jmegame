package football.gameplay.info;

import com.jme3.math.Vector3f;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class FootballMatch{
    private String matchName;
    int scoreA = 0;
    int scoreB = 0;
    FootballClub clubA, clubB;
    String stadiumName;
    
    String weather;
    Date startTime;
    float timeRemain;
    float addedTime;
    int status;
    boolean ballIn;
    
    public FootballMatch(FootballClub clubA, FootballClub clubB) {
        this.clubA = clubA;
        this.clubB = clubB;
    }
    public FootballMatch(FootballClub clubA, FootballClub clubB, String matchName) {
        this.clubA = clubA;
        this.clubB = clubB;
        this.matchName = matchName;
    }

    public void gameStart() {

    }

    public void gamePause() {
        // All character pause
        // Show the pause panel
    }

    public void gameBreak() {
        // Two team out
        // Review the match
        // After wait
        // Next half
    }

    public void gameEnd() {
        // Timeout reach
        // Refree whistle
        // Two team out
    }

    Vector3f whereGoal(boolean goalA) {
        if (goalA) {
            return new Vector3f(1, 1, 1);
        } else {
            return new Vector3f(0, 0, 0);
        }
    }
    
    String getShortIntro(){
        return "final match of the season";
    }
    FootballClub opponent(FootballClub aClub){
        if (aClub==clubA){
            return clubB;
        } else {
            return clubA;
        }
    }
    
    String toString(){
        return clubA.name +"vs"+clubB.name +" at "+startTime
    }
}
