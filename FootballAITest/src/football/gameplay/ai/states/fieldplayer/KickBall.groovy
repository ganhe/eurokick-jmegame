package football.gameplay.ai.states.fieldplayer

import football.gameplay.ai.*
import football.gameplay.ai.fsm.*
import football.gameplay.ai.event.*
import football.gameplay.ai.info.*

import static football.gameplay.ai.event.Telegram.MessageType.*
import football.gameplay.*

import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*
//************************************************************************ KICK BALL

import groovy.transform.*

@CompileStatic
class KickBall extends State<FieldPlayer>{
    static KickBall instance;
    public static KickBall Instance(){
        if (instance==null){
            instance = new KickBall();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        //let the team know this player is controlling
        player.getTeam().setControllingPlayer(player);
   
        //the player can only make so many kick attempts per second.
        if (!player.isReadyForNextKick()){
            player.getFSM().changeState(ChaseBall.Instance());
        }
        println "Kicked Ball!"
    }

    public void execute(FieldPlayer player){ 
        //calculate the dot product of the vector pointing to the ball
        //and the player's heading
        Vector2D ToBall = player.getBall().getPos() - player.getPos();
        double   dot    = player.Heading().dot(Vec2DNormalize(ToBall)); 

        //cannot kick the ball if the goalkeeper is in possession or if it is 
        //behind the player or if there is already an assigned receiver. So just
        //continue chasing the ball
        if (player.getTeam().Receiver() != null   ||
            player.getPitch().isGoalKeeperHasBall() ||
            (dot < 0) ){
            player.getTeam().checkPlayerPos("KickBall-ChaseBall")
            player.getFSM().changeState(ChaseBall.Instance());
            
            return;
        }

        /* Attempt a shot at the goal */

        //if a shot is possible, this vector will hold the position along the 
        //opponent's goal line the player should aim for.
        Vector2D    BallTarget = new Vector2D();

        //the dot product is used to adjust the shooting force. The more
        //directly the ball is ahead, the more forceful the kick
        double power = Params.Instance().MaxShootingForce * dot;

        //if it is determined that the player could score a goal from this position
        //OR if he should just kick the ball anyway, the player will attempt
        //to make the shot
        if (player.getTeam().CanShoot(player.getBall().getPos(),power,BallTarget)|| 
            (RandFloat() < Params.Instance().ChancePlayerAttemptsPotShot)){

            //add some noise to the kick. We don't want players who are 
            //too accurate! The amount of noise can be adjusted by altering
            //Params.Instance().PlayerKickingAccuracy
            BallTarget = SoccerBall.AddNoiseToKick(player.getBall().getPos(), BallTarget);

            //this is the direction the ball will be kicked in
            Vector2D KickDirection = BallTarget - player.getBall().getPos();
   
            player.getBall().kick(KickDirection, power);
            player.getTeam().checkPlayerPos("KickBall-Kick the ball")
            //change state   
            player.getFSM().changeState(Wait.Instance());
   
            player.findSupport();
  
            return;
        }


        /* Attempt a pass to a player */

        //if a receiver is found this will point to it
        PlayerBase receiver = null;

        power = Params.Instance().MaxPassingForce * dot;
  
        //test if there are any potential candidates available to receive a pass
        if (player.isThreatened()  &&
            player.getTeam().FindPass(player,
                receiver,
                BallTarget,
                power,
                Params.Instance().MinPassDist)){     
            //add some noise to the kick
            BallTarget = SoccerBall.AddNoiseToKick(player.getBall().getPos(), BallTarget);

            Vector2D KickDirection = BallTarget - player.getBall().getPos();
   
            player.getBall().kick(KickDirection, power);
    
            //let the receiver know a pass is coming 
            Dispatcher.Instance().dispatchMsg(Dispatcher.Instance().SEND_MSG_IMMEDIATELY,
                player.getID(),
                receiver.getID(),
                Msg_ReceiveBall,
                BallTarget);                            
   

            //the player should wait at his current position unless instruced
            //otherwise  
            player.getTeam().checkPlayerPos("KickBall - Ask receiver")
            player.getFSM().changeState(Wait.Instance());

            player.findSupport();
             
            return;
        }

        //cannot shoot or pass, so dribble the ball upfield
        else{   
            player.getTeam().checkPlayerPos("FindSupport - 0")
            player.findSupport();
            player.getTeam().checkPlayerPos("FindSupport")
            player.getFSM().changeState(Dribble.Instance());
            
        }   
        
        
    }
    public void exit(FieldPlayer player){
        
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}
