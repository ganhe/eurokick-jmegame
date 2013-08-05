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

import groovy.transform.*

/**
 *
 * @author cuong.nguyenmanh2
 */
public class GKPrepareForKickOff extends State<GoalKeeper>{
    static GKPrepareForKickOff instance;
    public static GKPrepareForKickOff Instance(){
        if (instance==null){
            instance = new GKPrepareForKickOff();
        }
        return instance;
    }


    public void enter(GoalKeeper keeper){

    }

    public void execute(GoalKeeper keeper){

    }


    public void exit(GoalKeeper keeper){

    }
 
    public boolean onMessage(GoalKeeper keeper, Telegram telegram){}
}

