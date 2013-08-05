package football.gameplay.ai.steering


import football.gameplay.PlayerBase
import football.gameplay.SoccerBall
import football.gameplay.SoccerPitch
import football.gameplay.EntityManager;
import football.gameplay.ai.info.Params
import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*

import groovy.transform.*

@CompileStatic
public class SteeringBehaviors
{
    private PlayerBase player;    

    private SoccerBall ball;

    //the steering force created by the combined effect of all
    //the selected behaviors
    private Vector2D steeringForce;

    //the current target (usually the ball or predicted ball position)
    private Vector2D target;

    //the distance the player tries to interpose from the target
    private double interposeDist;

    //multipliers. 
    private double multSeparation;

    //how far it can 'see'
    private double viewDistance;


    //binary flags to indicate whether or not a behavior should be active
    private int flags;

    public static enum behavior_type
    {
        none (0x0000),
        seek (0x0001),
        arrive (0x0002),
        separation (0x0004),
        pursuit (0x0008),
        interpose (0x0010);
 
        int value;
 
        behavior_type(int value){
            this.value = value;
        }
    }

    //used by group behaviors to tag neighbours
    public boolean tagged;
 
    //Arrive makes use of these to determine how quickly a vehicle
    //should decelerate to its target
    public enum Deceleration{slow(3), normal(2), fast(1);
 
        int value;
 
        Deceleration(int value){
            this.value = value;
        }}

    //this function tests if a specific bit of flags is set
    private boolean On(behavior_type bt){
        //return true;
        return (flags & bt.value) == bt.value;
    }
 
    //this function tests if a specific bit of flags is set
    private boolean On(int bt){
        //return true;
        return (flags & bt) == bt;
    }
 
    //a vertex buffer to contain the feelers rqd for dribbling
    List<Vector2D> Antenna;

    public Vector2D Force(){
        return steeringForce;
    }

    public Vector2D Target(){
        return target;
    }
    void SetTarget(Vector2D t){
        target = t;
    }

    public double getInterposeDistance(){
        return interposeDist;
    }
    public void SetInterposeDistance(double d){
        interposeDist = d;
    }

    public boolean Tagged(){return tagged;}
    public void setTag(){
        tagged = true;
    }
    public void unsetTag(){
        tagged = false;
    }
 

    public void seekOn(){
        flags |= behavior_type.seek.value;
    }
    public void arriveOn(){
        flags |= behavior_type.arrive.value;
    }
    public void pursuitOn(){
        flags |= behavior_type.pursuit.value;
    }
    public void separationOn(){
        flags |= behavior_type.separation.value;
    }
    public void interposeOn(double d){
        flags |= behavior_type.interpose.value; 
        interposeDist = d;
    }

 
    public void seekOff() {
        if(
            On(behavior_type.seek)) 
        flags ^=behavior_type.seek.value;
    }
    public void arriveOff(){
        if(
            On(behavior_type.arrive)) 
        flags ^=behavior_type.arrive.value;
    }
    public void pursuitOff(){
        if(
            On(behavior_type.pursuit)) 
        flags ^=behavior_type.pursuit.value;
    }
    public void separationOff(){
        if(On(behavior_type.separation)) 
        flags ^=behavior_type.separation.value;
    }
    public void interposeOff(){
        if(On(behavior_type.interpose)) 
        flags ^=behavior_type.interpose.value;
    }


    public boolean SeekIsOn(){
        return On(behavior_type.seek.value);
    }
    public boolean ArriveIsOn(){
        return On(behavior_type.arrive.value);
    }
    public boolean PursuitIsOn(){
        return On(behavior_type.pursuit.value);
    }
    public boolean SeparationIsOn(){
        return On(behavior_type.separation.value);
    }
    public boolean InterposeIsOn(){
        return On(behavior_type.interpose.value);
    }
 
    public SteeringBehaviors(PlayerBase agent,SoccerPitch world,SoccerBall ball){
   
        this.player=agent;
        this.flags=0;
        this.multSeparation=Params.Instance().SeparationCoefficient;
        this.tagged=false;
        this.viewDistance=Params.Instance().ViewDistance;
        this.ball=ball;
        this.interposeDist=0.0;
        this.Antenna=[];
 
        this.steeringForce = new Vector2D();

        //the current target (usually the ball or predicted ball position)
        this.target = new Vector2D();
    }

    //--------------------- AccumulateForce ----------------------------------
    //
    // This function calculates how much of its max steering force the 
    // vehicle has left to apply and then applies that amount of the
    // force to add.
    //------------------------------------------------------------------------
    public boolean AccumulateForce(Vector2D sf, Vector2D ForceToAdd)
    {
        //first calculate how much steering force we have left to use
        double MagnitudeSoFar = sf.length();

        double magnitudeRemaining = player.MaxForce() - MagnitudeSoFar;

        //return false if there is no more force left to use
        if (magnitudeRemaining <= 0.0) return false;

        //calculate the magnitude of the force we want to add
        double MagnitudeToAdd = ForceToAdd.length();
 
        //now calculate how much of the force we can really add 
        if (MagnitudeToAdd > magnitudeRemaining)
        {
            MagnitudeToAdd = magnitudeRemaining;
        }

        //add it to the steering force
        sf.minusLocal((Vec2DNormalize(ForceToAdd) * MagnitudeToAdd)); 
 
        return true;
    }

    //---------------------- Calculate ---------------------------------------
    //
    // calculates the overall steering force based on the currently active
    // steering behaviors. 
    //------------------------------------------------------------------------
    public Vector2D calculate()
    {     
        //reset the force
        steeringForce.Zero();
 
        //this will hold the value of each individual steering force
        steeringForce = sumForces();

        //make sure the force doesn't exceed the vehicles maximum allowable
        steeringForce.Truncate(player.MaxForce());
 
        //println("Player "+player.getID() +"--------------------------------" );
        /*
        if (player.getID() == 2){
        println(" SteeringForce : "+steeringForce);
        }
         */
        //println("---------------------------------------------")
        //steeringForce = new Vector2D(1,1)
        return steeringForce;
    }

    //-------------------------- SumForces -----------------------------------
    //
    // this method calls each active steering behavior and acumulates their
    // forces until the max steering force magnitude is reached at which
    // time the function returns the steering force accumulated to that 
    // point
    //------------------------------------------------------------------------
    public Vector2D sumForces()
    {
        Vector2D force = new Vector2D();
 
        //the soccer players must always tag their neighbors
        FindNeighbours();

        if (On(behavior_type.separation))
        {
            //println("separation on");
            force.minusLocal(Separation() * multSeparation);

            if (!AccumulateForce(steeringForce, force)) 
            return steeringForce;
        } 

        if (On(behavior_type.seek))
        {
            //println("seek on");
            force.minusLocal(Seek(target));

            if (!AccumulateForce(steeringForce, force)) 
            return steeringForce;
        }

        if (On(behavior_type.arrive))
        {
            //println("arrive on");
            force.minusLocal(Arrive(target, Deceleration.fast));

            if (!AccumulateForce(steeringForce, force)) 
            return steeringForce;
        }

        if (On(behavior_type.pursuit))
        {
            //println("pursuit on");
            force.minusLocal(Pursuit(ball));

            if (!AccumulateForce(steeringForce, force)) 
            return steeringForce;
        }

        if (On(behavior_type.interpose))
        {
            //println("interpose on");
            force.minusLocal(Interpose(ball, target, interposeDist));

            if (!AccumulateForce(steeringForce, force)) 
            return steeringForce;
        }

        return steeringForce;
    }

    //------------------------- ForwardComponent -----------------------------
    //
    // calculates the forward component of the steering force
    //------------------------------------------------------------------------
    public double getForwardComponent()
    {
        return player.Heading().dot(steeringForce);
    }

    //--------------------------- SideComponent ------------------------------
    //
    // // calculates the side component of the steering force
    //------------------------------------------------------------------------
    public double getSideComponent()
    {
        return player.Side().dot(steeringForce) * player.MaxTurnRate();
    }


    //------------------------------- Seek -----------------------------------
    //
    // Given a target, this behavior returns a steering force which will
    // allign the agent with the target and move the agent in the desired
    // direction
    //------------------------------------------------------------------------
    public Vector2D Seek(Vector2D target)
    {
 
        Vector2D DesiredVelocity = Vec2DNormalize(target - player.getPos())* player.MaxSpeed();

        return (DesiredVelocity - player.getVelocity());
    }


    //--------------------------- Arrive -------------------------------------
    //
    // This behavior is similar to seek but it attempts to arrive at the
    // target with a zero velocity
    //------------------------------------------------------------------------
    public Vector2D Arrive(Vector2D target,Deceleration deceleration)
    {
        Vector2D ToTarget = target - player.getPos();

        //calculate the distance to the target
        double dist = ToTarget.length();

        if (dist > 0)
        {
            //because Deceleration is enumerated as an int, this value is required
            //to provide fine tweaking of the deceleration..
            double DecelerationTweaker = 0.3;

            //calculate the speed required to reach the target given the desired
            //deceleration
            double speed = dist / ((double)deceleration.value * DecelerationTweaker);  

            //make sure the velocity does not exceed the max
            speed = Math.min(speed, player.MaxSpeed());

            //from here proceed just like Seek except we don't need to normalize 
            //the ToTarget vector because we have already gone to the trouble
            //of calculating its length: dist. 
            Vector2D DesiredVelocity = ToTarget * speed / dist;

            return (DesiredVelocity - player.getVelocity());
        }

        return new Vector2D(0,0);
    }


    //------------------------------ Pursuit ---------------------------------
    //
    // this behavior creates a force that steers the agent towards the 
    // ball
    //------------------------------------------------------------------------
    public Vector2D Pursuit(SoccerBall ball)
    {
        Vector2D ToBall = ball.getPos() - player.getPos();
 
        //the lookahead time is proportional to the distance between the ball
        //and the pursuer; 
        double LookAheadTime = 0.0;

        if (ball.Speed() != 0.0)
        {
            LookAheadTime = ToBall.length() / ball.Speed();
        }

        //calculate where the ball will be at this time in the future
        target = ball.FuturePosition(LookAheadTime);

        //now seek to the predicted future position of the ball
        return Arrive(target, Deceleration.fast);
    }


    //-------------------------- FindNeighbours ------------------------------
    //
    // tags any vehicles within a predefined radius
    //------------------------------------------------------------------------
    public void FindNeighbours()
    {
        List<PlayerBase> AllPlayers = EntityManager.Instance().getAllEntitiesByClass(PlayerBase.class);
        AllPlayers.each{PlayerBase curPlyr->
            //first clear any current tag
            curPlyr.getSteering().unsetTag();

            //work in distance squared to avoid sqrts
            Vector2D to = curPlyr.getPos() - player.getPos();

            if (to.LengthSq() < (viewDistance * viewDistance))
            {
                curPlyr.getSteering().setTag();
            }
        }//next
    }


    //---------------------------- Separation --------------------------------
    //
    // this calculates a force repelling from the other neighbors
    //------------------------------------------------------------------------
    public Vector2D Separation()
    { 
        //iterate through all the neighbors and calculate the vector from the
        Vector2D SteeringForce = new Vector2D();
 
        List<PlayerBase> AllPlayers = EntityManager.Instance().getAllEntitiesByClass(PlayerBase.class);
        AllPlayers.each{PlayerBase curPlyr->
 
            //make sure this agent isn't included in the calculations and that
            //the agent is close enough
            if((curPlyr != player) && curPlyr.getSteering().Tagged())
            {
                Vector2D ToAgent = player.getPos() - curPlyr.getPos();

                //scale the force inversely proportional to the agents distance 
                //from its neighbor.
                SteeringForce = SteeringForce+Vec2DNormalize(ToAgent)/ToAgent.length();
            }
        }

        return SteeringForce;
    }

 
    //--------------------------- Interpose ----------------------------------
    //
    // Given an opponent and an object position this method returns a 
    // force that attempts to position the agent between them
    //------------------------------------------------------------------------
    public Vector2D Interpose(SoccerBall ball,Vector2D target,double DistFromTarget)
    {
        return Arrive(target + Vec2DNormalize(ball.getPos() - target) * 
            DistFromTarget, Deceleration.normal);
    }


    //----------------------------- RenderAids -------------------------------
    //
    //------------------------------------------------------------------------
    @CompileDynamic
    public void RenderAids(def gdi)
    { 
        gdi.group{
            def v=player.getPos() + steeringForce * 20;
            //render the steering force
            line(x1:player.getPos().x,
                y1:player.getPos().y,
                x2:v.x,
                y2:v.y);
        }

 
    }
}
