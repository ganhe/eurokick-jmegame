package football.gameplay.ai.fsm

import football.gameplay.ai.event.Telegram
import groovy.transform.*


@CompileStatic
public abstract class State<V>{
    public abstract void enter(V keeper);

    public abstract void execute(V keeper);

    public abstract void exit(V keeper);
    
    //this executes if the agent receives a message from the 
    //message dispatcher
    public abstract boolean onMessage(V object, final Telegram t);
}
