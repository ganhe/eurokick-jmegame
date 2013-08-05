package football.gameplay.ai.states.fieldplayer

import football.gameplay.ai.*
import football.gameplay.ai.fsm.*
import football.gameplay.ai.event.*
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

//************************************************************************ Global state
import groovy.transform.*

@CompileStatic
public class GlobalPlayerState extends State<FieldPlayer>{
    static GlobalPlayerState instance;
    public static  GlobalPlayerState Instance(){
        if (instance==null){
            instance = new GlobalPlayerState();
        }
        return instance;
    }

    public void enter(FieldPlayer player){
    
    }
    public void execute(FieldPlayer player){
        //if a player is in possession and close to the ball reduce his max speed
        if((player.BallWithinReceivingRange()) && (player.isgetControllingPlayer())){
            player.SetMaxSpeed(Params.Instance().PlayerMaxSpeedWithBall);
        }

        else{
            player.SetMaxSpeed(Params.Instance().PlayerMaxSpeedWithoutBall);
        }
    
    }
    public void exit(FieldPlayer player){
    
    }

    public boolean onMessage(FieldPlayer player,Telegram telegram){
        switch(telegram.Msg){
        case Msg_ReceiveBall:
            //set the target
            player.getSteering().SetTarget((Vector2D)telegram.ExtraInfo);
            //change state 
            player.getFSM().changeState(ReceiveBall.Instance());
            return true;
            break;

        case Msg_SupportAttacker:
            
            //if already supporting just return
            if (player.getFSM().isInState(SupportAttacker.Instance())){
                return true;
            }
      
            //set the target to be the best supporting position
            player.getSteering().SetTarget(player.getTeam().getSupportSpot());
            //change the state
            player.getFSM().changeState(SupportAttacker.Instance());
            return true;
            break;

        case Msg_Wait:
            //change the state
            player.getFSM().changeState(Wait.Instance());
            return true;
            break;

        case Msg_GoHome:
            
            player.SetDefaultHomeRegion();
            player.getFSM().changeState(ReturnToHomeRegion.Instance());
            return true;
            
            break;

        case Msg_PassToMe:
            
      
            //get the position of the player requesting the pass 
            FieldPlayer receiver = (FieldPlayer)(telegram.ExtraInfo);

            //if the ball is not within kicking range or their is already a 
            //receiving player, this player cannot pass the ball to the player
            //making the request.
            if (player.getTeam().Receiver() != null ||
                !player.BallWithinKickingRange() ){

                return true;
            }
      
            //make the pass   
            player.getBall().kick(receiver.getPos() - player.getBall().getPos(),
                Params.Instance().MaxPassingForce);

       
            //let the receiver know a pass is coming 
            Dispatcher.Instance().dispatchMsg(Dispatcher.Instance().SEND_MSG_IMMEDIATELY,
                player.getID(),
                receiver.getID(),
                Msg_ReceiveBall,
                receiver.getPos());

   

            //change state   
            player.getFSM().changeState(Wait.Instance());

            player.findSupport();
            return true;

            break;

        }//end switch

        return false;
    }
    

} 

