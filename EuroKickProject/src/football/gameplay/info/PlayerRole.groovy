package football.gameplay.info

import groovy.transform.*
/**
 *
 * @author cuong.nguyenmanh2
 */
@CompileStatic
public enum PlayerRole {
    GoalKeeper, Attacker,Striker, Middler, Defender;
    
    PlayerRole byIndex(int i){
        return (PlayerRole) values()[i];
    }
    
    String toShortString(){
        
    }
}

