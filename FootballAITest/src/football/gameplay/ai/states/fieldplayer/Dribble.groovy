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

//*************************************************************************** DRIBBLE
import groovy.transform.*

@CompileStatic
public class Dribble extends State<FieldPlayer>{
    static Dribble instance;
    public static Dribble Instance(){
        if (instance==null){
            instance = new Dribble();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        //let the team know this player is controlling
        player.getTeam().setControllingPlayer(player);
    }

    public void execute(FieldPlayer player){
        double dot = player.getTeam().getHomeGoal().getFacing().dot(player.Heading());

        //if the ball is between the player and the home goal, it needs to swivel
        // the ball around by doing multiple small kicks and turns until the player 
        //is facing in the correct direction
        if (dot < 0){
            //the player's heading is going to be rotated by a small amount (Pi/4) 
            //and then the ball will be kicked in that direction
            Vector2D direction = player.Heading().cloneVec();

            //calculate the sign (+/-) of the angle between the player heading and the 
            //facing direction of the goal so that the player rotates around in the 
            //correct direction
            double angle = QuarterPi * -1 *
            player.getTeam().getHomeGoal().getFacing().Sign(player.Heading());

            vec2DRotateAroundOrigin(direction, angle);

            //this value works well when the player is attempting to control the
            //ball and turn at the same time
            double KickingForce = 0.8;

            player.getBall().kick(direction, KickingForce);
        } else{
            //kick the ball down the field
            player.getBall().kick(player.getTeam().getHomeGoal().getFacing(),
                Params.Instance().MaxDribbleForce);  
        }
        player.getTeam().checkPlayerPos("Dribble - Before changeState")
        //the player has kicked the ball so he must now change state to follow it
        player.getFSM().changeState(ChaseBall.Instance());
        player.getTeam().checkPlayerPos("Dribble - After changeState")
        return;  
    }
    public void exit(FieldPlayer player){
        
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}

