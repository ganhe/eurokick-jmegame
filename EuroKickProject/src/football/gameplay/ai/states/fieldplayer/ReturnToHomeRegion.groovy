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

//************************************************************************ RETURN TO HOME REGION
import groovy.transform.*

@CompileStatic
public class ReturnToHomeRegion extends State<FieldPlayer>{
    static ReturnToHomeRegion instance;
    public static ReturnToHomeRegion Instance(){
        if (instance==null){
            instance = new ReturnToHomeRegion();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        player.getSteering().arriveOn();

        if (!player.getHomeRegion().isInside(player.getSteering().getTarget(), Region.region_modifier.halfsize)){
            player.getSteering().setTarget(player.getHomeRegion().getCenter());
        }
        
    }

    void execute(FieldPlayer player){
        if (player.getGamePlayManager().isGameOn()){
            //if the ball is nearer this player than any other team member  &&
            //there is not an assigned receiver && the goalkeeper does not gave
            //the ball, go chase it
            if ( player.isClosestTeamMemberToBall() &&
                (player.getTeam().getReceiver() == null) &&
                !player.getGamePlayManager().isGoalKeeperHasBall()){
                player.getFSM().changeState(ChaseBall.Instance());

                return;
            }
        }

        //if game is on and close enough to home, change state to wait and set the 
        //player target to his current position.(so that if he gets jostled out of 
        //position he can move back to it)
        if (player.getGamePlayManager().isGameOn() && player.getHomeRegion().isInside(player.getPos(),
                Region.region_modifier.halfsize)){
            player.getSteering().setTarget(player.getPos());
            player.getFSM().changeState(Wait.Instance());
        }
        //if game is not on the player must return much closer to the center of his
        //home region
        else if(!player.getGamePlayManager().isGameOn() && player.isAtTarget()){
            player.getFSM().changeState(Wait.Instance());
        }
    }

    public void exit(FieldPlayer player){
        player.getSteering().arriveOff();
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }

}

