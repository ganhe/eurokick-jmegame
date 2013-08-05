package football.gameplay.ai.event

import groovy.transform.*

@CompileStatic
public class Telegram{
    
    public static enum MessageType{
        Msg_Null,
        Msg_ReceiveBall,
        Msg_PassToMe,
        Msg_SupportAttacker,
        Msg_GoHome,
        Msg_Wait;
        
        //converts an enumerated value to a string
        String toString(){
            switch (ordinal){
            
            case Msg_ReceiveBall:
    
                return "Msg_ReceiveBall";

            case Msg_PassToMe:
    
                return "Msg_PassToMe";

            case Msg_SupportAttacker:

                return "Msg_SupportAttacker";

            case Msg_GoHome:

                return "Msg_GoHome";

            case Msg_Wait:

                return "Msg_Wait";

            default:

                return "INVALID MESSAGE!!";
            
            }
        }      
    }
    
    //the entity that sent this telegram
    int          Sender;

    //the entity that is to receive this telegram
    int          Receiver;

    //the message itself. These are all enumerated in the file
    //"MessageTypes.h"
    MessageType Msg;

    //messages can be dispatched immediately or delayed for a specified amount
    //of time. If a delay is necessary this field is stamped with the time 
    //the message should be dispatched.
    double       DispatchTime;

    //any additional information that may accompany the message
    def        ExtraInfo;


    public Telegram(){
        DispatchTime=-1;
        Sender=-1;
        Receiver=-1;
        Msg=MessageType.Msg_Null;
  
    }

    public Telegram(double time,
        int    sender,
        int    receiver,
        MessageType    msg,
        def  info=null){ 
        DispatchTime=time;
        Sender=sender;
        Receiver=receiver;
        Msg=msg;
        ExtraInfo=info;
    }


    //these telegrams will be stored in a priority queue. Therefore the >
    //operator needs to be overloaded so that the PQ can sort the telegrams
    //by time priority. Note how the times must be smaller than
    //SmallestDelay apart before two Telegrams are considered unique.
    final double SmallestDelay = 0.25;


    public boolean equals(final Telegram t2)
    {
        Telegram t1 = this;
        return ( Math.abs(t1.DispatchTime-t2.DispatchTime) < SmallestDelay) &&
        (t1.Sender == t2.Sender)        &&
        (t1.Receiver == t2.Receiver)    &&
        (t1.Msg == t2.Msg);
    }

    public boolean compareTo(final Telegram t2)
    {
        Telegram t1 = this;
        if (t1 == t2)
        {
            return false;
        }

        else
        {
            return  (t1.DispatchTime < t2.DispatchTime);
        }
    }
}
