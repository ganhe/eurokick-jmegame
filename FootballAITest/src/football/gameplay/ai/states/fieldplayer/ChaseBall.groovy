package football.gameplay.ai.states.fieldplayer

import football.gameplay.ai.*
import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.ai.info.*
import static football.gameplay.ai.event.Telegram.MessageType.*
import football.gameplay.FieldPlayer
import football.gameplay.PlayerBase
import football.gameplay.Region
import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*
//***************************************************************************** CHASEBALL

import groovy.transform.*

@CompileStatic
public class ChaseBall extends State<FieldPlayer>{
    static ChaseBall instance;
    public static ChaseBall Instance(){
        if (instance==null){
            instance = new ChaseBall();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        player.getSteering().seekOn();
    }

    public void execute(FieldPlayer player){
        //if the ball is within kicking range the player changes state to KickBall.
        if (player.BallWithinKickingRange()){
            player.getTeam().checkPlayerPos("ChaseBall - Before changeState")
            player.getFSM().changeState(KickBall.Instance());
            player.getTeam().checkPlayerPos("ChaseBall - After changeState")
            return;
        }
                                                                              
        //if the player is the closest player to the ball then he should keep
        //chasing it
        if (player.isClosestTeamMemberToBall()){
            player.getSteering().SetTarget(player.getBall().getPos());
            return;
        }
  
        //if the player is not closest to the ball anymore, he should return back
        //to his home region and wait for another opportunity
        player.getFSM().changeState(ReturnToHomeRegion.Instance());
    }


    public void exit(FieldPlayer player){
        player.getSteering().seekOff();
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}


