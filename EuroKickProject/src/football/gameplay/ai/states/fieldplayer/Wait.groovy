package football.gameplay.ai.states.fieldplayer
import football.gameplay.ai.*
import football.gameplay.ai.fsm.State

import football.gameplay.ai.event.Telegram
import football.gameplay.FieldPlayer
import football.gameplay.PlayerBase
import football.gameplay.ai.info.*
import static football.gameplay.ai.event.Telegram.MessageType.*
import football.gameplay.Region

import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*

//***************************************************************************** WAIT
import groovy.transform.*

@CompileStatic
public class Wait extends State<FieldPlayer>{
    static Wait instance;
    public static Wait Instance(){
        if (instance==null){
            instance = new Wait();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        //if the game is not on make sure the target is the center of the player's
        //home region. This is ensure all the players are in the correct positions
        //ready for kick off
        if (!player.getGamePlayManager().isGameOn()){
            player.getSteering().setTarget(player.getHomeRegion().getCenter());
        }
    }

    public void execute(FieldPlayer player){ 
        
        //if the player has been jostled out of position, get back in position  
        if (!player.isAtTarget()){
            player.getSteering().arriveOn();
            println "Wrong behavior" + player.ID
            return;
        } else{
            player.getSteering().arriveOff();
            player.setVelocity(new Vector2D(0,0));
            //the player should keep his eyes on the ball!
            player.trackBall();
        }

        //if this player's team is controlling AND this player is not the Attacker
        //AND is further up the field than the Attacker he should request a pass.
        if ( player.getTeam().inControl()    &&
            (!player.isgetControllingPlayer()) &&
            player.isAheadOfAttacker() ){
            player.getTeam().requestPass(player);

            return;
        }

        if (player.getGamePlayManager().isGameOn()){
            println "Go Chase ball" + player.ID
            //if the ball is nearer this player than any other team member  AND
            //there is not an assigned receiver AND neither goalkeeper has
            //the ball, go chase it
            if (player.isClosestTeamMemberToBall() &&
                player.getTeam().getReceiver() == null  &&
                !player.getGamePlayManager().isGoalKeeperHasBall()){
                
                player.getFSM().changeState(ChaseBall.Instance());
                return;
            }
        } 
        
    }

    public void exit(FieldPlayer player){
        
    }

    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }

}
