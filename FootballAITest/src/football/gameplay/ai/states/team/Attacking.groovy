package football.gameplay.ai.states.team


import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.SoccerTeam
import groovy.transform.*
/**
 *
 * @author cuong.nguyenmanh2
 */
// ATTACKING
@CompileStatic
public class Attacking extends State<SoccerTeam>{
    static Attacking instance;

    public static  Attacking Instance(){
        if (instance==null){
            instance = new Attacking();
        }
        return instance;
    }


    public void enter(SoccerTeam team){
        //these define the home regions for this state of each of the players
        int[] BlueRegions = [1,12,14,6,4];
        int[] RedRegions = [16,3,5,9,13];

        //set up the player's home regions
        if (team.Color() == SoccerTeam.TeamColor.blue){
            team.changePlayerHomeRegions(BlueRegions);
        }
        else{
            team.changePlayerHomeRegions(RedRegions);
        }

        //if a player is in either the Wait or ReturnToHomeRegion states, its
        //steering target must be updated to that of its new home region to enable
        //it to move into the correct position.
        team.updateTargetsOfWaitingPlayers();
    }


    public void execute(SoccerTeam team){
        //if this team is no longer in control change states
        if (!team.inControl()){
            team.getFSM().changeState(Defending.Instance()); 
            return;
        }

        //calculate the best position for any supporting attacker to move to
        team.determineBestSupportingPosition();
    }

    public void exit(SoccerTeam team){
        //there is no supporting player for defense
        team.SetSupportingPlayer(null);
    }

    boolean onMessage(SoccerTeam aObj,Telegram tele){}
}

