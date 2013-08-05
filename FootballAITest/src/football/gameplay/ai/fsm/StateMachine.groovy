package football.gameplay.ai.fsm

import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import java.util.logging.Logger
import java.util.logging.LogRecord
import java.util.logging.Formatter
import java.util.logging.ConsoleHandler
import football.gameplay.SoccerTeam
import football.gameplay.BaseGameEntity
import groovy.transform.*


@CompileStatic
public class StateMachine<V>{

    //a pointer to the agent that owns this instance
    V owner;

    State<V> currentState;
 
    //a record of the last state the agent was in
    State<V> previousState;

    //this is called every time the FSM is updated
    State<V> globalState;

    Logger logger = Logger.getLogger("StateMachine");
    
    public StateMachine(V owner){
        this.owner=owner;
        this.currentState=null;
        this.previousState=null;
        this.globalState=null;
        
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new ShortFormatter());
        logger.addHandler(handler);
        
    }
    class ShortFormatter extends Formatter{
        public String format(LogRecord record){
            /*
            if(record.getLevel() == Level.INFO){
            return record.getMessage() + "\r\n";
            }else{
            return super.format(record);
            }
             */
            return record.getMessage() + "\r\n";
        }
    }
    //use these methods to initialize the FSM
    public void setCurrentState(State<V> s){
        this.currentState = s;
    }
    public void setGlobalState(State<V> s){
        this.globalState = s;
    }
    public void setPreviousState(State<V> s){
        this.previousState = s;
    }
 
    //call this to update the FSM
    public void update(){
        //logger.info("Update FSM" + owner.getClass().name);
        //if a global state exists, call its execute method, else do nothing
        if(globalState!=null) {
            globalState.execute(owner);
        }

        //same for the current state
        if (currentState!=null) {
            currentState.execute(owner);
            // For debug
            /*
            String objId="";
            if (owner instanceof SoccerTeam){
                objId = ((SoccerTeam)owner).Color().toString();
            } else if (owner instanceof BaseGameEntity){
                objId = ((BaseGameEntity)owner).getID();
            }
            //logger.info(shortFor(owner.getClass().name) + objId +" Execute State" + shortFor(currentState.getClass().name));
            */
        }
    }

    String shortFor(String longStr){
        int dotIndex = longStr.lastIndexOf(".") +1;
        return longStr.substring(dotIndex,longStr.length())
    }
    public boolean handleMessage(Telegram msg){
        //first see if the current state is valid and that it can handle
        //the message
        if (currentState!=null && currentState.onMessage(owner, msg)){
            return true;
        }
 
        //if not, and if a global state has been implemented, send 
        //the message to the global state
        if (globalState!=null && globalState.onMessage(owner, msg)){
            return true;
        }

        return false;
    }

    //change to a new state
    public void changeState(State<V> newState){


        //keep a record of the previous state
        previousState = currentState;

        //call the exit method of the existing state
        currentState.exit(owner);

        //change state to the new state
        currentState = newState;

        //call the entry method of the new state
        currentState.enter(owner);
    }

    //change state back to the previous state
    public void RevertTogetPreviousState(){
        changeState(previousState);
    }

    //returns true if the current state's type is equal to the type of the
    //class passed as a parameter. 
    public boolean isInState( State<V> st){
        if (currentState == st) return true;
        return false;
    }

    public State<V> getCurrentState(){
        return currentState;
    }
    public State<V> getGlobalState(){
        return globalState;
    }
    public State<V> getPreviousState(){
        return previousState;
    }

    //only ever used during debugging to grab the name of the current state
    public String getCurrentStateName(){
        String s =(currentState.toString());
        
        return s;
    }
}