package football.gameplay

import football.gameplay.ai.event.Telegram
import football.gameplay.ai.fsm.State
import football.geom.Vector2D
import football.gameplay.ai.fsm.StateMachine
import football.gameplay.ai.states.*
import football.gameplay.ai.event.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.gameplay.ai.info.*

import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*
import football.gameplay.core.FootballEntityManager as EntityManager;
import football.gameplay.info.*
import java.awt.Color
import groovy.swing.j2d.*
import groovy.transform.*
import football.gameplay.info.FootballPlayerInfo
@CompileStatic
public class GoalKeeper extends PlayerBase{
 
    //an instance of the state machine class
    private StateMachine<GoalKeeper> stateMachine;
 
    //this vector is updated to point towards the ball and is used when
    //rendering the goalkeeper (instead of the underlaying vehicle's heading)
    //to ensure he always appears to be watching the ball
    private Vector2D lookAt;

    boolean drawBody = false;
 
    public GoalKeeper(SoccerTeam home_team,FootballPlayerInfo info){
 
        super(home_team,PlayerRole.GoalKeeper,info);

    }
    
    public void init(State<GoalKeeper> start_state){
        super.init()
        //set up the state machine
        stateMachine = new StateMachine<GoalKeeper>(this);
        stateMachine.setCurrentState(start_state);
        stateMachine.setPreviousState(start_state);
        stateMachine.setGlobalState(GlobalKeeperState.Instance());

        stateMachine.getCurrentState().enter(this);
    }

    public void startMatch(){

    }

    //-------------------------- Update --------------------------------------

    public void update(){ 
        //run the logic for the current state
        stateMachine.update();

        if (steeringActived){
            //calculate the combined force from each steering behavior 
            Vector2D SteeringForce = steering.calculate();

            //Acceleration = Force/Mass
            Vector2D Acceleration = SteeringForce / mass;

            //update velocity
            velocity = velocity + Acceleration;

            //make sure player does not exceed maximum velocity
            velocity.Truncate(maxSpeed);

            //update the position
            position = position +velocity;


            //enforce a non-penetration raint if desired
            if(Params.Instance().bNonPenetrationConstraint){
                enforceNonPenetrationContraint(this, EntityManager.Instance().getAllEntitiesByClass(PlayerBase));
            }

            //update the heading if the player has a non zero velocity
            if ( !velocity.isZero()){ 
                heading = Vec2DNormalize(velocity);

                side = heading.perp();
            }

            if (getBall()!=null){
                //look-at vector always points toward the ball
                if (!getTeam().getGamePlayManager().isGoalKeeperHasBall()){
                    lookAt = Vec2DNormalize(getBall().getPos() - getPos());
                }
            }
        }
        // update rotation
        // lookAt
        // update location
        super.update();
    }


    public boolean isBallWithinRangeForIntercept(){
        return (vec2DDistanceSq(getTeam().getHomeGoal().getCenter(), getBall().getPos()) <=
            Params.Instance().GoalKeeperInterceptRangeSq);
    }

    public boolean isTooFarFromGoalMouth(){
        return (vec2DDistanceSq(getPos(), getRearInterposeTarget()) >
            Params.Instance().GoalKeeperInterceptRangeSq);
    }

    public Vector2D getRearInterposeTarget(){
        double xPosTarget = getTeam().getHomeGoal().getCenter().x;

        double yPosTarget = getPitch().getPlayingArea().getCenter().y - 
        Params.Instance().GoalWidth*0.5 + (getBall().getPos().y*Params.Instance().GoalWidth) /
        getPitch().getPlayingArea().getHeight();

        return new Vector2D(xPosTarget, yPosTarget); 
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
    /*
    GraphicsBuilder gdi=null
    @CompileDynamic
    public void Render(GraphicsBuilder gdi){
 
    String tcolor="";
    //set appropriate team color
    if (getTeam().getColor() == SoccerTeam.TeamColor.blue){
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
    vecPlayerVBTrans = WorldTransform(vecPlayerVBTrans, getPos(), getHeading(), Side(), Scale());
    def vs=vecPlayerVBTrans;
    group(id:prefix_id+"_body"){
    path(
    borderColor: 'white', fill: tcolor, winding: 'nonzero' ){
    moveTo( x: vs[0].x, y: vs[0].y )
    (1..vs.size()-1).each(){
    lineTo( x: vs[it].x, y: vs[it].y )
    }
    close()
    }
    }
    }
    //draw the head
    circle(id:prefix_id+"_head",
    cx:getPos().x,
    cy:getPos().y, 
    radius:6,
    borderColor:"darkBrown",fill:"brown");

    //show IDs
    if (Params.Instance().bIDs){
    text(id:prefix_id+"_id",
    x:getPos().x-20, 
    y:getPos().y-20, 
    text:getID().toString(),
    fill:new Color(0, 170, 0),borderColor:new Color(0, 170, 0));
    }

    //render the state
    if (Params.Instance().bStates){ 
    String stateName = stateMachine.getCurrentStateName().toString();
    stateName = stateName[stateName.lastIndexOf(".")+1..stateName.indexOf("@")-1]
    text(id:prefix_id+"_state",
    x:position.x, 
    y:position.y -20, 
    text:stateName,
    fill:"magenta",borderColor:"magenta");
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
    def vs = WorldTransform(vecPlayerVBTrans, getPos(), getHeading(), Side(), Scale());

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
     */
    /* getter setter
     */
    public StateMachine<GoalKeeper> getFSM(){
        return stateMachine;
    }

    public Vector2D getLookAt(){
        return lookAt;
    }
    public void setLookAt(Vector2D v){
        lookAt=v;
    }
}

 