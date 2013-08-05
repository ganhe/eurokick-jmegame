package football.gameplay

import football.gameplay.ai.fsm.StateMachine
import football.gameplay.ai.info.Params
import football.gameplay.ai.event.*
import football.gameplay.ai.support.*

import football.gameplay.ai.states.team.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.gameplay.ai.fsm.State

import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Transformations.*
import static football.geom.Utils.*
import static football.geom.Geometry2DFunctions.*
import static football.gameplay.ai.event.Telegram.MessageType.*
import static football.geom.Geometry2DFunctions.SpanType.*

import static java.awt.Color.*

import groovy.swing.j2d.*
import groovy.transform.*
import java.util.logging.Logger

@CompileStatic
public class SoccerTeam {

    Params prm;
    public static enum TeamColor {blue,red};
    
    //an instance of the state machine class
    StateMachine<SoccerTeam> stateMachine;
    //the team must know its own color!
    TeamColor Color;
    //pointers to the team members
    ArrayList<PlayerBase> players=new ArrayList<PlayerBase>();
    //a pointer to the soccer pitch
    SoccerPitch pitch;
    //pointers to the goals
    Goal opponentsGoal;
    Goal homeGoal;
 
    //a pointer to the opposing team
    SoccerTeam opponents;
  
    //pointers to 'key' players
    PlayerBase controllingPlayer;
    PlayerBase supportingPlayer;
    PlayerBase receivingPlayer;
    PlayerBase playerClosestToBall;
    //the squared distance the closest player is from the ball
    double distSqToBallOfClosestPlayer;
    //players use this to determine strategic positions on the playing field
    SupportSpotCalculator supportSpotCalc;
    
    Logger logger = Logger.getLogger("SoccerTeam");
    
    /* Setter getter */
    public List<PlayerBase> getMembers(){
        return players;
    } 
    
    public StateMachine<SoccerTeam> getFSM(){
        return stateMachine;
    }
 
    public Goal getHomeGoal(){
        return homeGoal;
    }
    public Goal getOpponentsGoal(){
        return opponentsGoal;
    }
    public SoccerPitch getPitch(){
        return pitch;
    }  
    public SoccerTeam getOpponents(){
        return opponents;
    }
    public void setOpponents(SoccerTeam opps){
        opponents = opps;
    }
    public TeamColor Color(){
        return Color;
    }
    public void setPlayerClosestToBall(PlayerBase plyr){
        playerClosestToBall=plyr;
    }
    public PlayerBase getPlayerClosestToBall(){
        return playerClosestToBall;
    }
 
    public double getClosestDistToBallSq(){
        return distSqToBallOfClosestPlayer;
    }
    public Vector2D getSupportSpot(){
        return supportSpotCalc.getBestSupportingSpot();
    }
    public PlayerBase getSupportingPlayer(){
        return supportingPlayer;
    }
    public void SetSupportingPlayer(PlayerBase plyr){
        supportingPlayer = plyr;
    }
    public PlayerBase Receiver(){
        return receivingPlayer;
    }
    public void SetReceiver(PlayerBase plyr){
        receivingPlayer = plyr;
    }
    public PlayerBase getControllingPlayer(){
        return controllingPlayer;
    }
  
    public void setControllingPlayer(PlayerBase plyr)
    {
        controllingPlayer = plyr;
        //rub it in the opponents faces!
        getOpponents().lostControl();
    }
    public boolean inControl(){
        if(controllingPlayer!=null){
            return true; 
        } else {
            return false;
        }
    }
    public void lostControl(){
        controllingPlayer = null;
    }
 
    public void determineBestSupportingPosition(){
        supportSpotCalc.determineBestSupportingPosition();
    }
    public String Name(){
        if (Color == blue) {
            return "Blue"; 
        } else {
            return "Red";
        }
    }
    public SoccerTeam(Goal home_goal,Goal opponents_goal,SoccerPitch pitch,TeamColor  color)
    {
        this.prm = Params.Instance();
        this.opponentsGoal=opponents_goal;
        this.homeGoal=home_goal;
        this.opponents=null;
        this.pitch=pitch;
        this.Color=color;
        this.distSqToBallOfClosestPlayer=0.0;
        this.supportingPlayer=null;
        this.receivingPlayer=null;
        this.controllingPlayer=null;
        this.playerClosestToBall=null;
        
        //setup the state machine
        this.stateMachine = new StateMachine<SoccerTeam>(this);
        this.stateMachine.setCurrentState(Defending.Instance());
        this.stateMachine.setPreviousState(Defending.Instance());
        this.stateMachine.setGlobalState(null);
        //create the players and goalkeeper
        createPlayers();
 
        //set default steering behaviors
        players.each
        { PlayerBase it->
            it.getSteering().separationOn();  
        }
        //create the sweet spot calculator
        supportSpotCalc = new SupportSpotCalculator(Params.Instance().NumSupportSpotsX,Params.Instance().NumSupportSpotsY,this);
    }
    /**-------------------------- update --------------------------------------
     *
     * iterates through each player's update function and calculates 
     * frequently accessed info
     */
    public void update()
    {

        //this information is used frequently so it's more efficient to 
        //calculate it just once each frame
        calculateClosestPlayerToBall();
        //the team state machine switches between attack/defense behavior. It
        //also handles the 'kick off' state where a team must return to their
        //kick off positions before the whistle is blown

        stateMachine.update();
        //now update each player
        players.each{ PlayerBase it->
            it.update();
        }
    }

    public boolean checkPlayerPos(String message){
        
        getMembers().each{PlayerBase pl->
            if (pl.getPos().x < 0 || pl.getPos().y < 0 ){
                println "Team " + Color().toString();
                println "Something wrong !"
                println pl.getID();
                println " Pos "+ pl.getPos().toString();
                println " Vec:" + pl.getVelocity().toString();

                throw new RuntimeException(message);
                return false;
            }
        }
        getOpponents().getMembers().each{PlayerBase pl->
            if (pl.getPos().x < 0 || pl.getPos().y < 0 ){
                println "Team " + Color().toString();
                println "wrong Opponents !";
                println pl.getID();
                println " Pos "+ pl.getPos().toString();
                println " Vec:" + pl.getVelocity().toString();

                throw new RuntimeException(message);
                return false;
            }
        }
        return true;
    }
    
    @CompileDynamic
    public void debuggetTeam(){
        getMembers().each{pl->
            println pl.getID();
            println " Pos "+pl.getPos().toString();
            println " Vec:" + pl.getVelocity().toString();
            println " state " + pl.getFSM().getCurrentStateName();
            println " target "+ pl.getSteering().Target().toString();
            println " force " + pl.getSteering().Force().toString();
        }
    }
    //------------------------ CalculateClosestPlayerToBall ------------------
    //
    // sets iClosestPlayerToBall to the player closest to the ball
    //------------------------------------------------------------------------
    public void calculateClosestPlayerToBall()
    {
        double ClosestSoFar = Double.MAX_VALUE;
        players.each{ PlayerBase it->
            //calculate the dist. Use the squared value to avoid sqrt
            double dist = vec2DDistanceSq(it.getPos(),getPitch().getBall().getPos());
            //keep a record of this value for each player
            it.SetDistSqToBall(dist);
  
            if (dist < ClosestSoFar)
            {
                ClosestSoFar = dist;
                playerClosestToBall = it;
            }
        }
        distSqToBallOfClosestPlayer = ClosestSoFar;
    }
    //------------- DetermineBestSupportingAttacker ------------------------
    //
    // calculate the closest player to the SupportSpot
    //------------------------------------------------------------------------
    public PlayerBase DetermineBestSupportingAttacker()
    {
        double ClosestSoFar = Double.MAX_VALUE;
        PlayerBase BestPlayer = null;
        players.each{PlayerBase it->
            //only attackers utilize the BestSupportingSpot
            if ( (it.Role() == PlayerBase.PlayerRole.attacker) && (it != controllingPlayer) )
            {
                //calculate the dist. Use the squared value to avoid sqrt
                double dist = vec2DDistanceSq(it.getPos(),supportSpotCalc.getBestSupportingSpot());
  
                //if the distance is the closest so far and the player is not a
                //goalkeeper and the player is not the one currently controlling
                //the ball,keep a record of this player
                if ((dist < ClosestSoFar) )
                {
                    ClosestSoFar = dist;
                    BestPlayer = it;
                }
            }
        }
        return BestPlayer;
    }
    //-------------------------- FindPass ------------------------------
    //
    // The best pass is considered to be the pass that cannot be intercepted 
    // by an opponent and that is as far forward of the receiver as possible
    //------------------------------------------------------------------------
    public boolean FindPass(PlayerBase passer,PlayerBase  receiver,Vector2D  PassTarget,double  power,double  MinPassingDistance)
    { 
        double  ClosestToGoalSoFar = Double.MAX_VALUE;
        Vector2D Target = new Vector2D();
        //iterate through all this player's team members and calculate which
        //one is in a position to be passed the ball 
        getMembers().each{PlayerBase curPlyr->
            //make sure the potential receiver being examined is not this player
            //and that it is further away than the minimum pass distance
            if ((curPlyr != passer) && 
                (vec2DDistanceSq(passer.getPos(),curPlyr.getPos()) > 
                    MinPassingDistance*MinPassingDistance))  
            {  
                if (GetBestPassToReceiver(passer,curPlyr,Target,power))
                {
                    //if the pass target is the closest to the opponent's goal line found
                    // so far,keep a record of it
                    double Dist2Goal = Math.abs(Target.x - getOpponentsGoal().getCenter().x);
                    if (Dist2Goal < ClosestToGoalSoFar)
                    {
                        ClosestToGoalSoFar = Dist2Goal;
  
                        //keep a record of this player
                        receiver = curPlyr;
                        //and the target
                        PassTarget = Target;
                    } 
                }
            }
        }//next team member
        if (receiver !=null) {
            return true;
        }else {
            return false;
        }
    }
    //---------------------- GetBestPassToReceiver ---------------------------
    //
    // Three potential passes are calculated. One directly toward the receiver's
    // current position and two that are the tangents from the ball position
    // to the circle of radius 'range' from the receiver.
    // These passes are then tested to see if they can be intercepted by an
    // opponent and to make sure they terminate within the playing area. If
    // all the passes are invalidated the function returns false. Otherwise
    // the function returns the pass that takes the ball closest to the 
    // opponent's goal area.
    //------------------------------------------------------------------------
    public boolean GetBestPassToReceiver( PlayerBase passer,PlayerBase receiver,Vector2D  PassTarget,double power)
    { 
        //first,calculate how much time it will take for the ball to reach 
        //this receiver,if the receiver was to remain motionless 
        double time = getPitch().getBall().TimeToCoverDistance(getPitch().getBall().getPos(),receiver.getPos(),power);
        //return false if ball cannot reach the receiver after having been
        //kicked with the given power
        if (time < 0) return false;
        //the maximum distance the receiver can cover in this time
        double InterceptRange = time * receiver.MaxSpeed();
 
        //Scale the intercept range
        double ScalingFactor = 0.3;
        InterceptRange *= ScalingFactor;
        //now calculate the pass targets which are positioned at the intercepts
        //of the tangents from the ball to the receiver's range circle.
        Vector2D ip1= new Vector2D();
        Vector2D ip2= new Vector2D();
        GetTangentPoints(receiver.getPos(),InterceptRange,getPitch().getBall().getPos(),ip1,ip2);
 
        int NumPassesToTry = 3;
        Vector2D[] Passes = [ip1,receiver.getPos(),ip2];
 
 
        // this pass is the best found so far if it is:
        //
        // 1. Further upfield than the closest valid pass for this receiver
        // found so far
        // 2. Within the playing area
        // 3. Cannot be intercepted by any opponents
        double ClosestSoFar = Double.MAX_VALUE;
        boolean bResult = false;
        for (int pass=0; pass<NumPassesToTry; ++pass)
        {  
            double dist = Math.abs(Passes[pass].x - getOpponentsGoal().getCenter().x);
            if (( dist < ClosestSoFar) &&
                getPitch().PlayingArea().Inside(Passes[pass]) &&
                isPassSafeFromAllOpponents(getPitch().getBall().getPos(),Passes[pass],receiver,power))
 
            {
                ClosestSoFar = dist;
                PassTarget  = Passes[pass];
                bResult = true;
            }
        }
        return bResult;
    }
    //----------------------- isPassSafeFromOpponent -------------------------
    //
    // test if a pass from 'from' to 'to' can be intercepted by an opposing
    // player
    //------------------------------------------------------------------------
    public boolean isPassSafeFromOpponent(Vector2D  from,Vector2D  target,PlayerBase receiver,PlayerBase opp,double  PassingForce)
    {
        //move the opponent into local space.
        Vector2D ToTarget = target - from;
        Vector2D ToTargetNormalized = Vec2DNormalize(ToTarget);
        Vector2D LocalPosOpp = PointToLocalSpace(opp.getPos().cloneVec(),ToTargetNormalized,ToTargetNormalized.perp(),from.cloneVec());
        //if opponent is behind the kicker then pass is considered okay(this is 
        //based on the assumption that the ball is going to be kicked with a 
        //velocity greater than the opponent's max velocity)
        if ( LocalPosOpp.x < 0 )
        { 
            return true;
        }
 
        //if the opponent is further away than the target we need to consider if
        //the opponent can reach the position before the receiver.
        if (vec2DDistanceSq(from,target) < vec2DDistanceSq(opp.getPos(),from))
        {
            if (receiver !=null)
            {
                if ( vec2DDistanceSq(target,opp.getPos()) > 
                    vec2DDistanceSq(target,receiver.getPos()) )
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return true;
            } 
        }
 
        //calculate how long it takes the ball to cover the distance to the 
        //position orthogonal to the opponents position
        double TimeForBall = getPitch().getBall().TimeToCoverDistance(new Vector2D(0,0),new Vector2D(LocalPosOpp.x,0),PassingForce);
        //now calculate how far the opponent can run in this time
        double reach = opp.MaxSpeed() * TimeForBall +getPitch().getBall().getBRadius()+opp.getBRadius();
        //if the distance to the opponent's y position is less than his running
        //range plus the radius of the ball and the opponents radius then the
        //ball can be intercepted
        if ( Math.abs(LocalPosOpp.y) < reach )
        {
            return false;
        }
        return true;
    }
    //---------------------- isPassSafeFromAllOpponents ----------------------
    //
    // tests a pass from position 'from' to position 'target' against each member
    // of the opposing team. Returns true if the pass can be made without
    // getting intercepted
    //------------------------------------------------------------------------
    public boolean isPassSafeFromAllOpponents(Vector2D from,Vector2D target,PlayerBase receiver,double PassingForce)
    {
        getOpponents().getMembers().each{ PlayerBase opp->
        
            if (!isPassSafeFromOpponent(from,target,receiver,opp,PassingForce))
            {
                return false;
            }
        }
        return true;
    }
    //------------------------ CanShoot --------------------------------------
    //
    // Given a ball position,a kicking power and a reference to a vector2D
    // this function will sample random positions along the opponent's goal-
    // mouth and check to see if a goal can be scored if the ball was to be
    // kicked in that direction with the given power. If a possible shot is 
    // found,the function will immediately return true,with the target 
    // position stored in the vector ShotTarget.
    //------------------------------------------------------------------------
    public boolean CanShoot(Vector2D BallPos,double power,Vector2D ShotTarget = new Vector2D())
    {
        //the number of randomly created shot targets this method will test 
        int NumAttempts = Params.Instance().NumAttemptsToFindValidStrike;
        while (NumAttempts--)
        {
            //choose a random position along the opponent's goal mouth. (making
            //sure the ball's radius is taken into account)
            ShotTarget = getOpponentsGoal().getCenter();
            //the y value of the shot position should lay somewhere between two
            //goalposts (taking into consideration the ball diameter)
            int MinYVal = (int) (getOpponentsGoal().getLeftPost().y + getPitch().getBall().getBRadius());
            int MaxYVal = (int) (getOpponentsGoal().getRightPost().y - getPitch().getBall().getBRadius());
            ShotTarget.y = (double)RandInt(MinYVal,MaxYVal);
            //make sure striking the ball with the given power is enough to drive
            //the ball over the goal line.
            double time = getPitch().getBall().TimeToCoverDistance(BallPos,ShotTarget,power);
  
            //if it is,this shot is then tested to see if any of the opponents
            //can intercept it.
            if (time >= 0)
            {
                if (isPassSafeFromAllOpponents(BallPos,ShotTarget,null,power))
                {
                    return true;
                }
            }
        }
 
        return false;
    }
 
    //--------------------- ReturnAllFieldplayersToHome ---------------------------
    //
    // sends a message to all players to return to their home areas forthwith
    //------------------------------------------------------------------------
    public void returnAllFieldPlayersToHome()
    {
        players.each{PlayerBase it->
            if (it.Role() != PlayerBase.PlayerRole.goal_keeper)
            {
                Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,1,it.getID(),Msg_GoHome,null);
            }
        }
    }
    //--------------------------- Render -------------------------------------
    //
    // renders the players and any team related info
    //------------------------------------------------------------------------
    GraphicsBuilder gdi=null;
    
    @CompileDynamic
    public void Render(GraphicsBuilder gdi)
    {
        String teamName = ""
        if (Color() == TeamColor.blue){
            teamName = "blue"
        } else {
            teamName = "red"
        }
        
        if (this.gdi!=gdi){
            this.gdi = gdi
            
            players.each{ PlayerBase player->
                player.Render(gdi);
            }

            gdi.group{
                //show the controlling team and player at the top of the display
                if (Params.Instance().bShowControllingTeam)
                { 
                    if (inControl())
                    {
                        text(id:"ControlStatus",x:20,y:3,text:teamName+" in Control",borderColor:"white",fill:"white");
                    }
                    if (controllingPlayer != null)
                    {
                        text(x:getPitch().cxClient()-150,y:3,
                            text:"Controlling Player: " + controllingPlayer.getID().toString(),
                            borderColor:"Red");
                    }
                }
                //render the sweet spots
                if (Params.Instance().bSupportSpots && inControl())
                {
                    supportSpotCalc.Render(gdi);
                }

                text(id:teamName+"TeamState",
                    x:160,
                    y:((Color() == TeamColor.red)?20:(getPitch().cyClient()-40)),
                    text:"",
                    fill:teamName,borderColor:teamName);
                gdi.(teamName+"TeamState").text = getStateName()


                if (supportingPlayer !=null){
                    Vector2D targetPos = supportingPlayer.getSteering().Target();
                    circle(x:targetPos.x,
                        y:targetPos.y,
                        radius:4,
                        fill:"blue",
                        borderColor:"blue");
                }

            }
        }else{
            //update only
            //gdi.redScore.text = "10"
            //gdi.blueScore.text = "15"
            gdi.(teamName+"TeamState").text = getStateName()
            
            players.each{ PlayerBase player->
                //int start = System.currentTimeMillis()
                player.Render(gdi);
                //int end = System.currentTimeMillis()
                //println ("Draw player " + player.getID() +" take " + (end - start))
            }
        }
    }
    
    public def getStateName(){
        if (getCurrentState() == Attacking.Instance()){
            return "Attacking";
        } else if (getCurrentState() == Defending.Instance()){
            return "Defending";
        } else if (getCurrentState() == PrepareForKickOff.Instance()){
            return "KickOff";
        } 
    }
    //------------------------- createPlayers --------------------------------
    //
    // creates the players
    //------------------------------------------------------------------------
    public void createPlayers()
    {
        if (Color() == TeamColor.blue)
        {
            //goalkeeper
            players.add(new GoalKeeper(this,1,TendGoal.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale));
 
            //create the players
            players.add(new FieldPlayer(this,6,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.attacker));
            players.add(new FieldPlayer(this,8,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.attacker));
 
            players.add(new FieldPlayer(this,3,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.defender));
            players.add(new FieldPlayer(this,5,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.defender));
        } else {
            //goalkeeper
            players.add(new GoalKeeper(this,16,TendGoal.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale));
            //create the players
            players.add(new FieldPlayer(this,9,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.attacker));
            players.add(new FieldPlayer(this,11,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.attacker));
 
            players.add(new FieldPlayer(this,12,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.defender));
            players.add(new FieldPlayer(this,14,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerBase.PlayerRole.defender));
  
        }
        //register the players with the entity manager
        players.each{ PlayerBase it->
            EntityManager.Instance().Instance().registerEntity(it);
        }
    }
    PlayerBase getPlayerFromID(int id)
    {
        players.each{PlayerBase it->
            if (it.getID() == id) return it;
        }
        return null;
    }
    
    public void SetPlayerHomeRegion(int plyr,int region)
    {
        //assert ( (plyr>=0) && (plyr<players.size()) );
        players[plyr].setHomeRegion(region);
    }
    //---------------------- UpdateTargetsOfWaitingplayers ------------------------
    //
    // 
    public void updateTargetsOfWaitingPlayers()
    {
        players.each{ PlayerBase it->
            if ( it.Role() != PlayerBase.PlayerRole.goal_keeper ){
                //cast to a field player
                FieldPlayer plyr = (FieldPlayer)it;
 
                if ( plyr.getFSM().isInState(Wait.Instance()) ||
                    plyr.getFSM().isInState(ReturnToHomeRegion.Instance()) ){
                    plyr.getSteering().SetTarget(plyr.HomeRegion().getCenter());
                }
            }
        }
    }
    //--------------------------- AllplayersAtHome --------------------------------
    //
    // returns false if any of the team are not located within their home region
    //-----------------------------------------------------------------------------
    public boolean allPlayersAtHome()
    {
        players.each{ PlayerBase it->
            if (it.InHomeRegion() == false){
                return false;
            }
        }
        return true;
    }
    //------------------------- RequestPass ---------------------------------------
    //
    // this tests to see if a pass is possible between the requester and
    // the controlling player. If it is possible a message is sent to the
    // controlling player to pass the ball asap.
    //-----------------------------------------------------------------------------
    public void RequestPass(FieldPlayer requester)
    {
        //maybe put a restriction here
        if (RandFloat() > 0.1) return;
 
        if (isPassSafeFromAllOpponents(getControllingPlayer().getPos(),requester.getPos(),requester,Params.Instance().MaxPassingForce))
        {
            //tell the player to make the pass
            //let the receiver know a pass is coming 
            Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,requester.getID(),getControllingPlayer().getID(),Msg_PassToMe,requester); 
        }
    }
    //----------------------------- isOpponentWithinRadius ------------------------
    //
    // returns true if an opposing player is within the radius of the position
    // given as a parameter
    //-----------------------------------------------------------------------------
    public boolean isOpponentWithinRadius(Vector2D pos,double rad)
    {
        getOpponents().getMembers().each{ PlayerBase it->
            if (vec2DDistanceSq(pos,it.getPos()) < rad*rad)
            {
                return true;
            }
        }
        return false;
    }
    
    public void changePlayerHomeRegions(int[] NewRegions){
        for (int plyr=0; plyr<players.size(); plyr++){
            SetPlayerHomeRegion(plyr, NewRegions[plyr]);
        }
    }
    
    
    State getCurrentState(){
        return getFSM().getCurrentState();
    }
}