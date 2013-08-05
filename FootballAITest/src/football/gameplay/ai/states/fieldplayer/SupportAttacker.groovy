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
//*****************************************************************************SUPPORT ATTACKING PLAYER
import groovy.transform.*

@CompileStatic
public class SupportAttacker extends State<FieldPlayer>{
    static SupportAttacker instance;
    
    public static SupportAttacker Instance(){
        if (instance==null){
            instance = new SupportAttacker();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        player.getSteering().arriveOn();
        player.getSteering().SetTarget(player.getTeam().getSupportSpot());

    }

    public void execute(FieldPlayer player){
        //if his team loses control go back home
        if (!player.getTeam().inControl()){
            player.getFSM().changeState(ReturnToHomeRegion.Instance()); return;
        } 

        //if the best supporting spot changes, change the steering target
        if (player.getTeam().getSupportSpot() != player.getSteering().Target()){    
            player.getSteering().SetTarget(player.getTeam().getSupportSpot());

            player.getSteering().arriveOn();
        }

        //if this player has a shot at the goal AND the attacker can pass
        //the ball to him the attacker should pass the ball to this player
        if( player.getTeam().CanShoot(player.getPos(),
                Params.Instance().MaxShootingForce)){
            player.getTeam().RequestPass(player);
        }


        //if this player is located at the support spot and his team still have
        //possession, he should remain still and turn to face the ball
        if (player.AtTarget()){
            player.getSteering().arriveOff();
        
            //the player should keep his eyes on the ball!
            player.trackBall();

            player.SetVelocity(new Vector2D(0,0));

            //if not threatened by another player request a pass
            if (!player.isThreatened()){
                player.getTeam().RequestPass(player);
            }
        }
    }


    public void exit(FieldPlayer player){
        //set supporting player to null so that the team knows it has to 
        //determine a new one.
        player.getTeam().SetSupportingPlayer(null);

        player.getSteering().arriveOff();
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}

