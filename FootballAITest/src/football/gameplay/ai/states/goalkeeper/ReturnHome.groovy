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
//------------------------- ReturnHome: ----------------------------------
//
// In this state the goalkeeper simply returns back to the center of
// the goal region before changing state back to TendGoal
//------------------------------------------------------------------------
import groovy.transform.*

@CompileStatic
public class ReturnHome extends State<GoalKeeper>{
    static ReturnHome instance;
    public static ReturnHome Instance(){
        if (instance==null){
            instance = new ReturnHome();
        }
        return instance;
    }

    public void enter(GoalKeeper keeper){
        keeper.getSteering().arriveOn();
    }

    public void execute(GoalKeeper keeper){
        keeper.getSteering().SetTarget(keeper.HomeRegion().getCenter());

        //if close enough to home or the opponents get control over the ball,
        //change state to tend goal
        if (keeper.InHomeRegion() || !keeper.getTeam().inControl()){
            keeper.getFSM().changeState(TendGoal.Instance());
        }
    }

    public void exit(GoalKeeper keeper){
        keeper.getSteering().arriveOff();
    }
    public boolean onMessage(GoalKeeper keeper, Telegram telegram){}
}


