package football.gameplay.ai.states.team

import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.*
import groovy.transform.*
/**
 *
 * @author cuong.nguyenmanh2
 */
// DEFENDING
@CompileStatic
public class Defending extends State<SoccerTeam>{
    static Defending instance;

    public static Defending Instance(){
        if (instance==null){
            instance = new Defending();
        }
        return instance;
    }

    public void enter(SoccerTeam team){

        //these define the home regions for this state of each of the players
        int[] BlueRegions = [1,6,8,3,5];
        int[] RedRegions = [16,9,11,12,14];

        //set up the player's home regions
        if (team.Color() == SoccerTeam.TeamColor.blue){
            team.changePlayerHomeRegions(BlueRegions);
        }
        else{
            team.changePlayerHomeRegions(RedRegions);
        }
  
        //if a player is in either the Wait or ReturnToHomeRegion states, its
        //steering target must be updated to that of its new home region
        team.updateTargetsOfWaitingPlayers();
    }

    public void execute(SoccerTeam team){
        //if in control change states
        if (team.inControl()){
            team.getFSM().changeState(Attacking.Instance()); 
            return;
        }
    }


    public void exit(SoccerTeam team){}

    public boolean onMessage(SoccerTeam aObj,Telegram tele){}
}

