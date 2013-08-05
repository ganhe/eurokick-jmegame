package football.gameplay

import football.geom.Vector2D;
import football.gameplay.ai.steering.SteeringBehaviors
import football.gameplay.ai.info.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.gameplay.ai.event.*

import static football.gameplay.ai.event.Telegram.MessageType.*
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*
import groovy.transform.*

@CompileStatic
public class PlayerBase extends MovingEntity{
    public static enum PlayerRole{goal_keeper, attacker, defender};
    //this player's role in the team
    protected PlayerRole PlayerRole;
    //a pointer to this player's team
    protected SoccerTeam team;
 
    //the steering behaviors
    protected SteeringBehaviors steering;
    //the region that this player is assigned to.
    protected int homeRegion;
    //the region this player moves to before kickoff
    protected int  defaultRegion;
    //the distance to the ball (in squared-space). This value is queried 
    //a lot so it's calculated once each time-step and stored here.
    protected double distSqToBall;
    //the vertex buffer
    protected List<Vector2D>   vecPlayerVB = [];
    //the buffer for the transformed vertices
    protected List<Vector2D>   vecPlayerVBTrans = [];
    protected Params Prm;
    
    public PlayerBase(SoccerTeam home_team,int home_region,Vector2D heading,Vector2D velocity,double mass,double max_force,double max_speed,double max_turn_rate,double scale,PlayerRole role){
  
        super(home_team.getPitch().GetRegionFromIndex(home_region).getCenter(),scale*10.0,velocity,max_speed,heading,mass,new Vector2D(scale,scale),max_turn_rate,max_force);
        this.Prm = Params.Instance();
        this.team=home_team;
        this.distSqToBall=Double.MAX_VALUE;
        this.homeRegion=home_region;
        this.defaultRegion=home_region;
        this.PlayerRole=role;
        
        //setup the vertex buffers and calculate the bounding radius
        int NumPlayerVerts = 4;
        Vector2D[] player = [new Vector2D(-3,8),new Vector2D(3,10),new Vector2D(3,-10),new Vector2D(-3,-8)];
        for (int vtx=0; vtx<NumPlayerVerts; ++vtx){
            vecPlayerVB.add(player[vtx]);
            //set the bounding radius to the length of the 
            //greatest extent
            if (Math.abs(player[vtx].x) > boundingRadius){
                this.boundingRadius = Math.abs(player[vtx].x);
            }
            if (Math.abs(player[vtx].y) > boundingRadius){
                this.boundingRadius = Math.abs(player[vtx].y);
            }
        }
        //set up the steering behavior class
        this.steering = new SteeringBehaviors(this,team.getPitch(),getBall());  
  
        //a player's start target is its start position (because it's just waiting)
        steering.SetTarget(home_team.getPitch().GetRegionFromIndex(home_region).getCenter());
    }
    //----------------------------- TrackBall --------------------------------
    //
    //  sets the player's heading to point at the ball
    //------------------------------------------------------------------------
    public void trackBall(){
        rotateHeadingToFacePosition(getBall().getPos());  
    }
    //----------------------------- TrackTarget --------------------------------
    //
    //  sets the player's heading to point at the current target
    //------------------------------------------------------------------------
    public void trackTarget(){
        SetHeading(Vec2DNormalize(getSteering().Target() - getPos()));
    }
    //------------------------------------------------------------------------
    //
    //binary predicates for std::sort (see CanPassForward/Backward)
    //------------------------------------------------------------------------
    public boolean  sortByDistanceToOpponentsGoal( PlayerBase p1,PlayerBase p2){
        return (p1.DistToOppGoal() < p2.DistToOppGoal());
    }
    
    public boolean  sortByReversedDistanceToOpponentsGoal( PlayerBase p1,PlayerBase p2){
        return (p1.DistToOppGoal() > p2.DistToOppGoal());
    }
    //------------------------- WithinFieldOfView ---------------------------
    //
    //  returns true if subject is within field of view of this player
    //-----------------------------------------------------------------------
    public boolean positionInFrontOfPlayer(Vector2D position){
        Vector2D ToSubject = position - getPos();
        if (ToSubject.dot(Heading()) > 0){
            return true;
        } else{
            return false;
        }
    }
    //------------------------- IsThreatened ---------------------------------
    //
    //  returns true if there is an opponent within this player's 
    //  comfort zone
    //------------------------------------------------------------------------
    public boolean isThreatened(){
        //check against all opponents to make sure non are within this
        //player's comfort zone

        getTeam().getOpponents().getMembers().each{PlayerBase curOpp->

            //calculate distance to the player. if dist is less than our
            //comfort zone,and the opponent is infront of the player,return true
            if (positionInFrontOfPlayer((curOpp).getPos()) &&
                (vec2DDistanceSq(getPos(),(curOpp).getPos()) < Prm.PlayerComfortZoneSq)){        
                return true;
            }
   
        }// next opp
        return false;
    
    }
    //----------------------------- FindSupport -----------------------------------
    //
    //  determines the player who is closest to the SupportSpot and messages him
    //  to tell him to change state to SupportAttacker
    //-----------------------------------------------------------------------------
    public void findSupport(){    
        //if there is no support we need to find a suitable player.
        if (getTeam().getSupportingPlayer() == null){
            PlayerBase BestSupportPly = getTeam().DetermineBestSupportingAttacker();
            getTeam().SetSupportingPlayer(BestSupportPly);
            Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,getID(),getTeam().getSupportingPlayer().getID(),Msg_SupportAttacker,null);
        }
    
        PlayerBase BestSupportPly = getTeam().DetermineBestSupportingAttacker();
    
        //if the best player available to support the attacker changes,update
        //the pointers and send messages to the relevant players to update their
        //states
        if (BestSupportPly && (BestSupportPly != getTeam().getSupportingPlayer())){
    
            if (getTeam().getSupportingPlayer()){
                Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,getID(),getTeam().getSupportingPlayer().getID(),Msg_GoHome,null);
            }
    
            getTeam().SetSupportingPlayer(BestSupportPly);
            Dispatcher.Instance().dispatchMsg(Dispatcher.SEND_MSG_IMMEDIATELY,getID(),getTeam().getSupportingPlayer().getID(),Msg_SupportAttacker,null);
        }
    }
    public double       DistSqToBall(){
        return distSqToBall;
    }
    public void        SetDistSqToBall(double val){
        distSqToBall = val;
    }
    public PlayerRole Role(){
        return PlayerRole;
    }
    
    public void SetDefaultHomeRegion(){
        homeRegion = defaultRegion;
    }
    //calculate distance to opponent's goal. Used frequently by the passing//methods
    public double DistToOppGoal(){
        return Math.abs(getPos().x - getTeam().getOpponentsGoal().getCenter().x);
    }
    public double DistTogetHomeGoal(){
        return Math.abs(getPos().x - getTeam().getHomeGoal().getCenter().x);
    }
    public boolean isgetControllingPlayer(){
        return getTeam().getControllingPlayer()==this;
    }
    public boolean BallWithinKeeperRange(){
        return (vec2DDistanceSq(getPos(),getBall().getPos()) < Prm.KeeperInBallRangeSq);
    }
    public boolean BallWithinReceivingRange(){
        return (vec2DDistanceSq(getPos(),getBall().getPos()) < Prm.BallWithinReceivingRangeSq);
    }
    public boolean BallWithinKickingRange(){
        return (vec2DDistanceSq(getBall().getPos(),getPos()) < Prm.PlayerKickingDistanceSq);
    }
    public boolean InHomeRegion(){
        if (PlayerRole == PlayerRole.goal_keeper){
            return getPitch().GetRegionFromIndex(homeRegion).Inside(getPos(),Region.region_modifier.normal);
        }
        else{
            return getPitch().GetRegionFromIndex(homeRegion).Inside(getPos(),Region.region_modifier.halfsize);
        }
    }
    public boolean AtTarget(){
        return (vec2DDistanceSq(getPos(),getSteering().Target()) < Prm.PlayerInTargetRangeSq);
    }
    public boolean isClosestTeamMemberToBall(){
        return getTeam().getPlayerClosestToBall() == this;
    }
    public boolean isClosestPlayerOnPitchToBall(){
        return isClosestTeamMemberToBall() && 
        (DistSqToBall() < getTeam().getOpponents().getClosestDistToBallSq());
    }
    public boolean InHotRegion(){
        return Math.abs(getPos().y - getTeam().getOpponentsGoal().getCenter().y ) <
        getPitch().PlayingArea().length()/3.0;
    }
    public boolean isAheadOfAttacker(){
        return Math.abs(getPos().x - getTeam().getOpponentsGoal().getCenter().x) <
        Math.abs(getTeam().getControllingPlayer().getPos().x - getTeam().getOpponentsGoal().getCenter().x);
    }
    public SoccerBall getBall(){
        return getTeam().getPitch().getBall();
    }
    
    public SoccerPitch  getPitch(){
        return getTeam().getPitch();
    }
    
    public Region  HomeRegion(){
        return getPitch().GetRegionFromIndex(homeRegion);
    }
    
    public SteeringBehaviors getSteering(){
        return steering;
    }
    
    public void setHomeRegion(int NewRegion){
        homeRegion = NewRegion;
    }
    
    public SoccerTeam getTeam(){
        return team;
    }
    
    public String toString(){
        return "Player "+getID();
    }
}
