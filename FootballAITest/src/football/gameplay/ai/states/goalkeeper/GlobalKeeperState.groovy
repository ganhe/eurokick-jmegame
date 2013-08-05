package football.gameplay.ai.states.goalkeeper;

import football.gameplay.ai.*

import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.GoalKeeper
import football.gameplay.PlayerBase
import football.geom.Vector2D
import football.gameplay.Region
import football.gameplay.ai.info.*
import football.gameplay.ai.event.*
import static football.gameplay.ai.event.Telegram.MessageType.*

// STATES OF GOAL KEEPER FOR STATE MACHINE
//--------------------------- GlobalKeeperState -------------------------------

import groovy.transform.*

@CompileStatic
public class GlobalKeeperState extends State<GoalKeeper>{
    static GlobalKeeperState instance;
    public static GlobalKeeperState Instance(){
        if (instance==null){
            instance = new GlobalKeeperState();
        }
        return instance;
    }
    public void enter(GoalKeeper keeper){
    }

    public void execute(GoalKeeper keeper){
    }

    public void exit(GoalKeeper keeper){
    }
 
    public boolean onMessage(GoalKeeper keeper, Telegram telegram){
        switch(telegram.Msg){
        case Msg_GoHome:
            keeper.SetDefaultHomeRegion();
            keeper.getFSM().changeState(ReturnHome.Instance());
            break;

        case Msg_ReceiveBall:
            keeper.getFSM().changeState(InterceptBall.Instance());
            break;

        }//end switch

        return false;
    }

}



