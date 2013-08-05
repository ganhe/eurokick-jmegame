package football.gameplay

import football.gameplay.ai.fsm.StateMachine
import football.geom.Vector2D
import football.gameplay.ai.fsm.State
import football.gameplay.ai.event.Telegram
import football.gameplay.ai.event.*
import football.gameplay.ai.info.Regulator
import football.gameplay.ai.states.team.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.gameplay.ai.info.*
import java.awt.Color
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class FieldPlayer extends PlayerBase{
    //an instance of the state machine class
    StateMachine<FieldPlayer> stateMachine;
 
    //limits the number of kicks a player may take per second
    Regulator   kickLimiter;
    boolean drawBody = false;
    public StateMachine<FieldPlayer> getFSM(){
        return stateMachine;
    }

    boolean isReadyForNextKick(){
        return kickLimiter.isReady();
    }
 
    public FieldPlayer(SoccerTeam home_team, int  home_region, State<FieldPlayer> start_state, Vector2D heading, Vector2D velocity, double  mass, double  max_force, double  max_speed, double  max_turn_rate, double  scale, PlayerBase.PlayerRole role){ 
        super(home_team, home_region, heading, velocity, mass, max_force, max_speed, max_turn_rate, scale, role)      

        //set up the state machine
        this.stateMachine = new StateMachine<FieldPlayer>(this);

        if (start_state!=null){  
            stateMachine.setCurrentState(start_state);
            stateMachine.setPreviousState(start_state);
            stateMachine.setGlobalState(GlobalPlayerState.Instance());

            stateMachine.getCurrentState().enter(this);
        }  

        this.steering.separationOn();

        //set up the kick regulator
        this.kickLimiter = new Regulator(Params.Instance().PlayerKickFrequency);
    }

    //------------------------------ Update ----------------------------------
    //
    // 
    //------------------------------------------------------------------------
    public void update(){ 

        //run the logic for the current state
        getTeam().checkPlayerPos("Before FSM! ID= "+this.getID()+" State: "+this.getFSM().getCurrentStateName())
        stateMachine.update();
        getTeam().checkPlayerPos("Before steering! ID= "+this.getID()+" State: "+this.getFSM().getCurrentStateName())

        
        //calculate the combined steering force
        steering.calculate();

        //if no steering force is produced decelerate the player by applying a
        //braking force
        if (steering.Force().isZero()){
            double BrakingRate = 0.8; 

            velocity = velocity * BrakingRate;       
        }
        //the steering force's side component is a force that rotates the 
        //player about its axis. We must limit the rotation so that a player
        //can only turn by PlayerMaxTurnRate rads per update.
        double TurningForce =  steering.getSideComponent();

        //Clamp(TurningForce, -Params.Instance().PlayerMaxTurnRate, Params.Instance().PlayerMaxTurnRate);

        //rotate the heading vector
        vec2DRotateAroundOrigin(heading, TurningForce);

        //make sure the velocity vector points in the same direction as
        //the heading vector
        velocity = heading * velocity.length();

        //and recreate side
        side = heading.perp();


        //now to calculate the acceleration due to the force exerted by
        //the forward component of the steering force in the direction
        //of the player's heading
        Vector2D accel = heading * steering.getForwardComponent() / mass;

        velocity = velocity + accel;

        //make sure player does not exceed maximum velocity
        velocity.Truncate(maxSpeed);

        //update the position
        position = position + velocity;


        //enforce a non-penetration raint if desired
        if(Params.Instance().bNonPenetrationConstraint){
            enforceNonPenetrationContraint(this, EntityManager.Instance().getAllEntitiesByClass(PlayerBase.class));
        }

    }

    public void checkPlayergetPos(){
        if (position.x < 0 || position.y < 0 ){
            println "Something wrong (1)!";
        }
        
        Vector2D oldPos = position.cloneVec();
        if (team.getOpponents().getFSM().isInState(Attacking.Instance())){
            println "A. Pos" + position;
            println "A. Dis" + oldPos.Distance(position);
        }
        if (position.x < 0 || position.y < 0 ){
            println "Something wrong (2)! Pos : " + position.toString() + " Vec:" + velocity.toString();
        }
        if (oldPos.Distance(position)> 20){
            println "Something wrong (3)!";
        }
        
        if (team.getOpponents().getFSM().isInState(Attacking.Instance())){
            println "B. Pos" + position;
            println "B. Dis" + oldPos.Distance(position);
        }
    }
    //-------------------- HandleMessage -------------------------------------
    //
    // routes any messages appropriately
    //------------------------------------------------------------------------
    public boolean handleMessage( Telegram msg){
        return stateMachine.handleMessage(msg);
    }

    //--------------------------- Render -------------------------------------
    //
    //------------------------------------------------------------------------
    def gdi = null;
    
    @CompileDynamic
    public void Render(def gdi){
        String tcolor="";
        //set appropriate team color
        if (getTeam().Color() == SoccerTeam.TeamColor.blue){
            tcolor="blue";
        }
        else{
            tcolor="red";
        }
        String prefix_id="player_"+getID()
        
        if (this.gdi!=gdi){
            this.gdi = gdi
            gdi.group{
                if (drawBody){
                    //render the player's body
                    vecPlayerVBTrans.clear();
                    vecPlayerVB.each{
                        vecPlayerVBTrans.add(new Vector2D(it.x,it.y));
                    }
                    vecPlayerVBTrans = WorldTransform(vecPlayerVBTrans, getPos(), Heading(), Side(), Scale());
                    def vs=vecPlayerVBTrans;
                    group(id:prefix_id+"_body"){
                        path(borderColor: 'black', fill: tcolor, winding: 'nonzero' ){
                            moveTo( x: vs[0].x, y: vs[0].y )
                            (1..vs.size()-1).each(){
                                lineTo( x: vs[it].x, y: vs[it].y )
                            }
                            close()
                        }
                    }
                }
                //and 'is 'ead

                if (Params.Instance().bHighlightIfThreatened && (getTeam().getControllingPlayer() == this) && isThreatened()) {
                
                }
                //gdi.YellowBrush();
                circle(id:prefix_id+"_head",
                    cx:getPos().x,
                    cy:getPos().y, 
                    radius:6,
                    borderColor:"darkBrown",
                    fill:tcolor);

  
                //render the state
                if (Params.Instance().bStates){ 
                    String stateName = stateMachine.getCurrentStateName().toString();
                    stateName = stateName[stateName.lastIndexOf(".")+1..stateName.indexOf("@")-1]
                    text(id:prefix_id+"_state",
                        x:position.x, 
                        y:position.y -20, 
                        text:stateName,
                        fill:"yellow",borderColor:"yellow");
                }

                //show IDs
                if (Params.Instance().bIDs){
                    text(id:prefix_id+"_id",
                        x:getPos().x-20, 
                        y:getPos().y-20, 
                        text:getID().toString(),
                        fill:new Color(0, 170, 0),
                        borderColor:new Color(0, 170, 0));
                }


                if (Params.Instance().bViewTargets){
                    circle(getSteering().Target(), 3);
                    text(id:prefix_id+"_state",
                        x:getSteering().Target().x,
                        y:getSteering().Target().y,
                        text:getID().toString(),
                        fill:"red",
                        borderColor:"red");
                }  
            }
        } else {
            // -------update only
            // update text
            String stateName = stateMachine.getCurrentStateName().toString();
            stateName = stateName[stateName.lastIndexOf(".")+1..stateName.indexOf("@")-1]
            gdi.(prefix_id+"_state").text = stateName
            gdi.(prefix_id+"_state").x = position.x
            gdi.(prefix_id+"_state").y = position.y - 20
            
            
            gdi.(prefix_id+"_id").text = getID().toString()
            gdi.(prefix_id+"_id").x = position.x - 20
            gdi.(prefix_id+"_id").y = position.y - 20
            // head
            gdi.(prefix_id+"_head").cx=getPos().x
            gdi.(prefix_id+"_head").cy=getPos().y
            
            if (drawBody){
                //render the player's body

                vecPlayerVBTrans.clear();
                vecPlayerVB.each{
                    vecPlayerVBTrans.add(new Vector2D(it.x,it.y));
                }
                def vs = WorldTransform(vecPlayerVBTrans, getPos(), Heading(), Side(), Scale());

                def drawPath = gdi.path(
                    borderColor: 'yellow', fill: tcolor, winding: 'nonzero' ){
                    moveTo( x: vs[0].x, y: vs[0].y )
                    (1..vs.size()-1).each(){
                        lineTo( x: vs[it].x, y: vs[it].y )
                    }
                    close()
                }
                gdi.(prefix_id+"_body").getOps().clear()
                gdi.(prefix_id+"_body").addOperation(drawPath)
            }
            
        }
    }
}