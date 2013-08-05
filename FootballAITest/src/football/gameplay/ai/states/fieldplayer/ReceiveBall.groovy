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

//************************************************************************     RECEIVEBALL
import groovy.transform.*

@CompileStatic
class ReceiveBall extends State<FieldPlayer>{
    static ReceiveBall instance;
    public static ReceiveBall Instance(){
        if (instance==null){
            instance = new ReceiveBall();
        }
        return instance;
    }


    public void enter(FieldPlayer player){
        //let the team know this player is receiving the ball
        player.getTeam().SetReceiver(player);
  
        //this player is also now the controlling player
        player.getTeam().setControllingPlayer(player);

        //there are two types of receive behavior. One uses arrive to direct
        //the receiver to the position sent by the passer in its telegram. The
        //other uses the pursuit behavior to pursue the ball. 
        //This statement selects between them dependent on the probability
        //ChanceOfUsingArriveTypeReceiveBehavior, whether or not an opposing
        //player is close to the receiving player, and whether or not the receiving
        //player is in the opponents 'hot region' (the third of the pitch closest
        //to the opponent's goal
        double PassThreatRadius = 70.0;

        if (( player.InHotRegion() ||
                RandFloat() < Params.Instance().ChanceOfUsingArriveTypeReceiveBehavior) &&
            !player.getTeam().isOpponentWithinRadius(player.getPos(), PassThreatRadius)){
            player.getSteering().arriveOn();
        }
        else{
            player.getSteering().pursuitOn();
        }
    }

    public void execute(FieldPlayer player){
        //if the ball comes close enough to the player or if his team lose control
        //he should change state to chase the ball
        if (player.BallWithinReceivingRange() || !player.getTeam().inControl()){
            player.getFSM().changeState(ChaseBall.Instance());

            return;
        }  

        if (player.getSteering().PursuitIsOn()){
            player.getSteering().SetTarget(player.getBall().getPos());
        }

        //if the player has 'arrived' at the steering target he should wait and
        //turn to face the ball
        if (player.AtTarget()){
            player.getSteering().arriveOff();
            player.getSteering().pursuitOff();
            player.trackBall();    
            player.SetVelocity(new Vector2D(0,0));
        } 
    }

    public void exit(FieldPlayer player){
        player.getSteering().arriveOff();
        player.getSteering().pursuitOff();

        player.getTeam().SetReceiver(null);
    }
    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}