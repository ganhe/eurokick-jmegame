package football.gameplay

import football.gameplay.ai.fsm.StateMachine
import football.gameplay.ai.info.Params
import football.gameplay.ai.event.*
import football.gameplay.ai.support.*

import football.gameplay.ai.states.team.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.gameplay.ai.fsm.State

import football.gameplay.info.*
import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Transformations.*
import static football.geom.Utils.*
import static football.geom.Geometry2DFunctions.*
import static football.gameplay.ai.event.Telegram.MessageType.*
import static football.geom.Geometry2DFunctions.SpanType.*
import football.gameplay.core.FootballEntityManager as EntityManager;
import static java.awt.Color.*
import static football.gameplay.info.PlayerRole.*
import groovy.swing.j2d.*
import groovy.transform.*
import java.util.logging.Logger

@CompileStatic
public class SoccerTeam {

    //
    FootballGamePlayManager gamePlayManager;
    SoccerCoach coach;
    
    Params prm;
    public static enum TeamColor {blue,red};
    //an instance of the state machine class
    StateMachine<SoccerTeam> stateMachine;
    //the team must know its own color!
    TeamColor color;
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

    public SoccerTeam(FootballGamePlayManager gamePlayManager){
        this.gamePlayManager = gamePlayManager;
        this.prm = Params.Instance();
        this.opponents=null;
        this.supportingPlayer=null;
        this.receivingPlayer=null;
        this.controllingPlayer=null;
        this.playerClosestToBall=null;
        this.distSqToBallOfClosestPlayer=0.0;
    }
    public void init(){
        
    }
    public void initMatch(SoccerTeam opps,SoccerPitch pitch,Goal home_goal,TeamColor color){
        setOpponents(opps);
        this.homeGoal=home_goal;

        this.pitch=pitch;
        this.color=color;

        //setup the state machine
        this.stateMachine = new StateMachine<SoccerTeam>(this);
        this.stateMachine.setCurrentState(Defending.Instance());
        this.stateMachine.setPreviousState(Defending.Instance());
        this.stateMachine.setGlobalState(null);
        //create the players and goalkeeper
        createPlayers();

        //set default steering behaviors
        players.each{ PlayerBase it->
            it.init()
            //it.getSteering().separationOn();
        }
        //create the sweet spot calculator
        supportSpotCalc = new SupportSpotCalculator(Params.Instance().NumSupportSpotsX,Params.Instance().NumSupportSpotsY,this);
    }

    public void startMatch(){
        this.opponentsGoal = opponents.getHomeGoal()
        players.each{PlayerBase player->
            player.getSteering().separationOn();
            player.startMatch();
            if (player instanceof FieldPlayer){
                ((FieldPlayer)player).getFSM().changeState(Wait.Instance())
                //player.getFSM().changeState(ChaseBall.Instance())
            }
            
        }
    }
    /**-------------------------- update --------------------------------------
     *
     * iterates through each player's update function and calculates 
     * frequently accessed info
     */
    public void update()
    {
        println ("Team" + color.toString() + "update")
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
                println "Team "+ getColor().toString();
                println "Something wrong !"
                println pl.getID();
                println " Pos "+ pl.getPos().toString();
                println " Vec:"+ pl.getVelocity().toString();

                //throw new RuntimeException(message);
                return false;
            }
        }
        getOpponents().getMembers().each{PlayerBase pl->
            if (pl.getPos().x < 0 || pl.getPos().y < 0 ){
                println "Team "+ getColor().toString();
                println "wrong Opponents !";
                println pl.getID();
                println " Pos "+ pl.getPos().toString();
                println " Vec:"+ pl.getVelocity().toString();

                //throw new RuntimeException(message);
                return false;
            }
        }
        return true;
    }
    /*
    @CompileDynamic
    public void debugTeam(){
    getMembers().each{pl->
    println pl.getID();
    println " Pos "+pl.getPos().toString();
    println " Vec:"+ pl.getVelocity().toString();
    println " state "+ pl.getFSM().getCurrentStateName();
    println " target "+ pl.getSteering().Target().toString();
    println " force "+ pl.getSteering().getForce().toString();
    }
    }
     */
    //------------------------ CalculateClosestPlayerToBall ------------------
    //
    // sets iClosestPlayerToBall to the player closest to the ball
    //------------------------------------------------------------------------
    public void calculateClosestPlayerToBall()
    {
        double closestSoFar = Double.MAX_VALUE;
        players.each{ PlayerBase it->
            //calculate the dist. Use the squared value to avoid sqrt
            double dist = vec2DDistanceSq(it.getPos(),getBall().getPos());
            //keep a record of this value for each player
            it.SetDistSqToBall(dist);

            if (dist < closestSoFar)
            {
                closestSoFar = dist;
                playerClosestToBall = it;
            }
        }
        distSqToBallOfClosestPlayer = closestSoFar;
    }
    //------------- DetermineBestSupportingAttacker ------------------------
    //
    // calculate the closest player to the SupportSpot
    //------------------------------------------------------------------------
    public PlayerBase determineBestSupportingAttacker()
    {
        double closestSoFar = Double.MAX_VALUE;
        PlayerBase bestPlayer = null;
        players.each{PlayerBase it->
            //only Attackers utilize the BestSupportingSpot
            if ((it.getRole()== PlayerRole.Attacker)&&(it != controllingPlayer))
            {
                //calculate the dist. Use the squared value to avoid sqrt
                double dist = vec2DDistanceSq(it.getPos(),supportSpotCalc.getBestSupportingSpot());

                //if the distance is the closest so far and the player is not a
                //goalkeeper and the player is not the one currently controlling
                //the ball,keep a record of this player
                if ((dist < closestSoFar))
                {
                    closestSoFar = dist;
                    bestPlayer = it;
                }
            }
        }
        return bestPlayer;
    }
    //-------------------------- FindPass ------------------------------
    //
    // The best pass is considered to be the pass that cannot be intercepted 
    // by an opponent and that is as far forward of the receiver as possible
    //------------------------------------------------------------------------
    public boolean findPass(PlayerBase passer,PlayerBase  receiver,Vector2D  passTarget,double  power,double  minPassingDistance)
    {
        double  closestToGoalSoFar = Double.MAX_VALUE;
        Vector2D target = new Vector2D();
        //iterate through all this player's team members and calculate which
        //one is in a position to be passed the ball 
        getMembers().each{PlayerBase curPlyr->
            //make sure the potential receiver being examined is not this player
            //and that it is further away than the minimum pass distance
            if ((curPlyr != passer)&&
                (vec2DDistanceSq(passer.getPos(),curPlyr.getPos())>
                    minPassingDistance*minPassingDistance))
            {
                if (getBestPassToReceiver(passer,curPlyr,target,power))
                {
                    //if the pass target is the closest to the opponent's goal line found
                    // so far,keep a record of it
                    double dist2Goal = Math.abs(target.x - getOpponentsGoal().getCenter().x);
                    if (dist2Goal < closestToGoalSoFar)
                    {
                        closestToGoalSoFar = dist2Goal;

                        //keep a record of this player
                        receiver = curPlyr;
                        //and the target
                        passTarget = target;
                    }
                }
            }
        }//next team member
        if (receiver !=null){
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
    public boolean getBestPassToReceiver( PlayerBase passer,PlayerBase receiver,Vector2D  passTarget,double power)
    {
        //first,calculate how much time it will take for the ball to reach 
        //this receiver,if the receiver was to remain motionless 
        double time = getBall().timeToCoverDistance(getBall().getPos(),receiver.getPos(),power);
        //return false if ball cannot reach the receiver after having been
        //kicked with the given power
        if (time < 0) return false;
        //the maximum distance the receiver can cover in this time
        double interceptRange = time * receiver.getMaxSpeed();

        //Scale the intercept range
        double scalingFactor = 0.3;
        interceptRange *= scalingFactor;
        //now calculate the pass targets which are positioned at the intercepts
        //of the tangents from the ball to the receiver's range circle.
        Vector2D ip1= new Vector2D();
        Vector2D ip2= new Vector2D();
        GetTangentPoints(receiver.getPos(),interceptRange,getBall().getPos(),ip1,ip2);

        int numPassesToTry = 3;
        Vector2D[] passes =[ip1,receiver.getPos(),ip2];


        // this pass is the best found so far if it is:
        //
        // 1. Further upfield than the closest valid pass for this receiver
        // found so far
        // 2. Within the playing area
        // 3. Cannot be intercepted by any opponents
        double closestSoFar = Double.MAX_VALUE;
        boolean bResult = false;
        for (int pass=0; pass<numPassesToTry;++pass)
        {
            double dist = Math.abs(passes[pass].x - getOpponentsGoal().getCenter().x);
            if (( dist < closestSoFar)&&
                getPitch().getPlayingArea().isInside(passes[pass])&&
                isPassSafeFromAllOpponents(getBall().getPos(),passes[pass],receiver,power))

            {
                closestSoFar = dist;
                passTarget.cloneVec(passes[pass]);
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
        Vector2D toTarget = target - from;
        Vector2D toTargetNormalized = Vec2DNormalize(toTarget);
        Vector2D localPosOpp = PointToLocalSpace(opp.getPos().cloneVec(),toTargetNormalized,toTargetNormalized.perp(),from.cloneVec());
        //if opponent is behind the kicker then pass is considered okay(this is 
        //based on the assumption that the ball is going to be kicked with a 
        //velocity greater than the opponent's max velocity)
        if ( localPosOpp.x < 0 )
        {
            return true;
        }

        //if the opponent is further away than the target we need to consider if
        //the opponent can reach the position before the receiver.
        if (vec2DDistanceSq(from,target)< vec2DDistanceSq(opp.getPos(),from))
        {
            if (receiver !=null)
            {
                if ( vec2DDistanceSq(target,opp.getPos())>
                    vec2DDistanceSq(target,receiver.getPos()))
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
        double timeForBall = getBall().timeToCoverDistance(new Vector2D(0,0),new Vector2D(localPosOpp.x,0),PassingForce);
        //now calculate how far the opponent can run in this time
        double reach = opp.getMaxSpeed()* timeForBall +getBall().getBRadius()+opp.getBRadius();
        //if the distance to the opponent's y position is less than his running
        //range plus the radius of the ball and the opponents radius then the
        //ball can be intercepted
        if ( Math.abs(localPosOpp.y)< reach )
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
    // position stored in the vector shotTarget.
    //------------------------------------------------------------------------
    public boolean canShoot(Vector2D BallPos,double power,Vector2D shotTarget = new Vector2D())
    {
        //the number of randomly created shot targets this method will test 
        int numAttempts = Params.Instance().NumAttemptsToFindValidStrike;
        while (numAttempts--)
        {
            //choose a random position along the opponent's goal mouth.(making
            //sure the ball's radius is taken into account)
            println shotTarget
            println "Goal" + getOpponentsGoal()
            shotTarget = getOpponentsGoal().getCenter();
            //the y value of the shot position should lay somewhere between two
            //goalposts (taking into consideration the ball diameter)
            int MinYVal =(int)(getOpponentsGoal().getLeftPost().y + getBall().getBRadius());
            int MaxYVal =(int)(getOpponentsGoal().getRightPost().y - getBall().getBRadius());
            shotTarget.y =(double)RandInt(MinYVal,MaxYVal);
            //make sure striking the ball with the given power is enough to drive
            //the ball over the goal line.
            double time = getBall().timeToCoverDistance(BallPos,shotTarget,power);

            //if it is,this shot is then tested to see if any of the opponents
            //can intercept it.
            if (time >= 0)
            {
                if (isPassSafeFromAllOpponents(BallPos,shotTarget,null,power))
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
            if (it.getRole()!= PlayerRole.GoalKeeper)
            {
                Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,1,it.getID(),Msg_GoHome,null);
            }
        }
    }
    //--------------------------- Render -------------------------------------
    //
    // renders the players and any team related info
    //------------------------------------------------------------------------
    /*
    GraphicsBuilder gdi=null;

    @CompileDynamic
    public void Render(GraphicsBuilder gdi)
    {
    String teamName =""
    if (color()== TeamColor.blue){
    teamName ="blue"
    } else {
    teamName ="red"
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
    text(x:getPitch().getCxClient()-150,y:3,
    text:"Controlling Player:"+ controllingPlayer.getID().toString(),
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
    y:((color()== TeamColor.red)?20:(getPitch().getCyClient()-40)),
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
    //gdi.redScore.text ="10"
    //gdi.blueScore.text ="15"
    gdi.(teamName+"TeamState").text = getStateName()

    players.each{ PlayerBase player->
    //int start = System.currentTimeMillis()
    player.Render(gdi);
    //int end = System.currentTimeMillis()
    //println ("Draw player "+ player.getID()+" take "+(end - start))
    }
    }
    }
     */
    public def getStateName(){
        if (getCurrentState()== Attacking.Instance()){
            return "Attacking";
        } else if (getCurrentState()== Defending.Instance()){
            return "Defending";
        } else if (getCurrentState()== PrepareForKickOff.Instance()){
            return "KickOff";
        }
    }
    //------------------------- createPlayers --------------------------------
    //
    // creates the players
    //------------------------------------------------------------------------
    public void createPlayers()
    {
        /*
    
        if (getColor()== TeamColor.blue)
        {
        //goalkeeper
        players.add(new GoalKeeper(this,1,TendGoal.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale));

        //create the players
        players.add(new FieldPlayer(this,6,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Attacker));
        players.add(new FieldPlayer(this,8,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Attacker));

        players.add(new FieldPlayer(this,3,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Defender));
        players.add(new FieldPlayer(this,5,Wait.Instance(),new Vector2D(0,1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Defender));
        } else {
        //goalkeeper
        players.add(new GoalKeeper(this,16,TendGoal.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale));
        //create the players
        players.add(new FieldPlayer(this,9,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Attacker));
        players.add(new FieldPlayer(this,11,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Attacker));

        players.add(new FieldPlayer(this,12,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Defender));
        players.add(new FieldPlayer(this,14,Wait.Instance(),new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerMass,prm.PlayerMaxForce,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerScale,PlayerRole.Defender));

        }
         */
        //register the players with the entity manager
        players.each{ PlayerBase player->
            if (player.role == Attacker){
                FieldPlayer fp = (FieldPlayer)player;
                fp.init(FPPrepareForKickOff.Instance());
                fp.setDirectionParams(new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerScale);
                fp.setEnergy(prm.PlayerMass,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerMaxForce);
            } else {
                GoalKeeper gk = (GoalKeeper)player;
                gk.init(GKPrepareForKickOff.Instance());
                gk.setDirectionParams(new Vector2D(0,-1),new Vector2D(0.0,0.0),prm.PlayerScale);
                gk.setEnergy(prm.PlayerMass,prm.PlayerMaxSpeedWithoutBall,prm.PlayerMaxTurnRate,prm.PlayerMaxForce);
            }
        
            EntityManager.getInstance().addEntity(player);
        }
         
    }

    
    PlayerBase getPlayerFromID(int id)
    {
        players.each{PlayerBase it->
            if (it.getID()== id) return it;
        }
        return null;
    }

    public void setPlayerHomeRegion(int plyr,int region)
    {
        //assert ((plyr>=0)&&(plyr<players.size()));
        players[plyr].setHomeRegion(region);
    }
    //---------------------- UpdateTargetsOfWaitingplayers ------------------------
    //
    //
    public void updateTargetsOfWaitingPlayers()
    {
        players.each{ PlayerBase it->
            if ( it.getRole()!= PlayerRole.GoalKeeper ){
                //cast to a field player
                FieldPlayer plyr =(FieldPlayer)it;

                if ( plyr.getFSM().isInState(Wait.Instance())||
                    plyr.getFSM().isInState(ReturnToHomeRegion.Instance())){
                    plyr.getSteering().setTarget(plyr.getHomeRegion().getCenter());
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
            if (it.IngetHomeRegion()== false){
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
    public void requestPass(FieldPlayer requester)
    {
        //maybe put a restriction here
        if (RandFloat()> 0.1) return;

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
            if (vec2DDistanceSq(pos,it.getPos())< rad*rad)
            {
                return true;
            }
        }
        return false;
    }

    public void changePlayerHomeRegions(int[] newRegions){
        for (int plyr=0; plyr<players.size(); plyr++){
            if (plyr > 4) return;
            setPlayerHomeRegion(plyr, newRegions[plyr]);
        }
    }

    State getCurrentState(){
        return getFSM().getCurrentState();
    }

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
    public TeamColor getColor(){
        return color;
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
    public void setSupportingPlayer(PlayerBase plyr){
        supportingPlayer = plyr;
    }
    public PlayerBase getReceiver(){
        return receivingPlayer;
    }
    public void setReceiver(PlayerBase plyr){
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
    public String getName(){
        if (color == blue){
            return "Blue";
        } else {
            return "Red";
        }
    }
    
    /*
     *
     */
    public void arrangeTeam(){
       
        // player.setLocation();
    }
    public void arrangeStartPos(FbTeamPositions teamPos){
        println "Arrange " + teamPos.positions
        println "Players " + players
        for (PlayerBase player : players){
            Vector2D loc = teamPos.positions.get(player.info);
            println "Player " + player + " " + loc
            player.setPosUpdate(loc);
        }
    }
    public FootballGamePlayManager getGamePlayManager(){
        return gamePlayManager;
    }
    
    SoccerBall getBall(){
        return getGamePlayManager().getBall();
    }
}