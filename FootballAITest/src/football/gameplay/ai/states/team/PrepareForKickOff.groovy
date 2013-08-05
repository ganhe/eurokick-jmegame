package football.gameplay.ai.states.team
import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.SoccerTeam
import groovy.transform.*
/**
 *
 * @author cuong.nguyenmanh2
 */

// KICKOFF
@CompileStatic
public class PrepareForKickOff extends State<SoccerTeam>{
    static PrepareForKickOff instance;
    public static PrepareForKickOff Instance(){
        if (instance==null){
            instance = new PrepareForKickOff();
        }
        return instance;
    }

    public void enter(SoccerTeam team){
        //reset key player pointers
        team.setControllingPlayer(null);
        team.SetSupportingPlayer(null);
        team.SetReceiver(null);
        team.setPlayerClosestToBall(null);

        //send Msg_GoHome to each player.
        team.returnAllFieldPlayersToHome();
    }

    public void execute(SoccerTeam team){
        //if both teams in position, start the game
        if (team.allPlayersAtHome() && team.getOpponents().allPlayersAtHome()){
            team.getFSM().changeState(Defending.Instance());
        }
    }

    public void exit(SoccerTeam team){
        team.getPitch().SetGameOn();
    }
    public boolean onMessage(SoccerTeam aObj,Telegram tele){}
}

