package football.gameplay.ai.states.goalkeeper
import football.gameplay.ai.*
import football.gameplay.GoalKeeper
import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.PlayerBase
import football.gameplay.Region
import football.geom.Vector2D
import football.gameplay.ai.info.*
import football.gameplay.ai.event.*
import football.gameplay.ai.event.SoccerMessages.MessageType.*
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Transformations.*
import static football.geom.Utils.*
// STATES OF GOAL KEEPER FOR STATE MACHINE

//--------------------------- PutBallBackInPlay --------------------------
//
//------------------------------------------------------------------------
import groovy.transform.*

@CompileStatic
class PutBallBackInPlay extends State<GoalKeeper>{
    static PutBallBackInPlay instance;
    public static PutBallBackInPlay Instance(){
        if (instance==null){
            instance = new PutBallBackInPlay();
        }
        return instance;
    }

    public void enter(GoalKeeper keeper){
        //let the team know that the keeper is in control
        keeper.getTeam().setControllingPlayer(keeper);

        //send all the players home
        keeper.getTeam().getOpponents().returnAllFieldPlayersToHome();
        keeper.getTeam().returnAllFieldPlayersToHome();
    }


    public void execute(GoalKeeper keeper){
        PlayerBase receiver = null;
        Vector2D BallTarget = new Vector2D();
 
        //test if there are players further forward on the field we might
        //be able to pass to. If so, make a pass.
        if (keeper.getTeam().FindPass(keeper,
                receiver,
                BallTarget,
                Params.Instance().MaxPassingForce,
                Params.Instance().GoalkeeperMinPassDist)){ 
            //make the pass 
            keeper.getBall().kick(Vec2DNormalize(BallTarget - keeper.getBall().getPos()),
                Params.Instance().MaxPassingForce);

            //goalkeeper no longer has ball 
            keeper.getPitch().SetGoalKeeperHasBall(false);

            //let the receiving player know the ball's comin' at him
            Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,
                keeper.getID(),
                receiver.getID(),
                Telegram.MessageType.Msg_ReceiveBall,
                BallTarget);
 
            //go back to tending the goal 
            keeper.getFSM().changeState(TendGoal.Instance());

            return;
        } 

        keeper.SetVelocity(new Vector2D());
    }

    public void exit(GoalKeeper keeper){
    }
 
    public boolean onMessage(GoalKeeper keeper, Telegram telegram){
 
    }
}
 



