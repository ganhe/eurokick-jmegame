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

//***************************************************************************** WAIT
import groovy.transform.*

/**
 *
 * @author cuong.nguyenmanh2
 */
public class FPPrepareForKickOff extends State<FieldPlayer>{
    static FPPrepareForKickOff instance;
    public static FPPrepareForKickOff Instance(){
        if (instance==null){
            instance = new FPPrepareForKickOff();
        }
        return instance;
    }


    public void enter(FieldPlayer player){

    }

    public void execute(FieldPlayer player){ 

        
    }

    public void exit(FieldPlayer player){
        
    }

    public boolean onMessage(FieldPlayer player, final Telegram t){
        
    }
}

