package football.gameplay.ai.info
import groovy.transform.*

@CompileStatic
public class Params{
    private static Params singleton;
    public double GoalWidth;

    public int NumSupportSpotsX;
    public int NumSupportSpotsY;

    //these values tweak the various rules used to calculate the support spots
    public double Spot_PassSafeScore;
    public double Spot_CanScoreFromPositionScore;
    public double Spot_DistFromControllingPlayerScore;
    public double Spot_ClosenessToSupportingPlayerScore;
    public double Spot_AheadOfAttackerScore; 
 
    public double SupportSpotUpdateFreq ;

    public double ChancePlayerAttemptsPotShot; 
    public double ChanceOfUsingArriveTypeReceiveBehavior;

    public double BallSize;
    public double BallMass;
    public double Friction;

    public double KeeperInBallRange;
    public double KeeperInBallRangeSq;

    public double PlayerInTargetRange;
    public double PlayerInTargetRangeSq;
 
    public double PlayerMass;
 
    //max steering force
    public double PlayerMaxForce; 
    public double PlayerMaxSpeedWithBall;
    public double PlayerMaxSpeedWithoutBall;
    public double PlayerMaxTurnRate;
    public double PlayerScale;
    public double PlayerComfortZone;

    public double PlayerKickingDistance;
    public double PlayerKickingDistanceSq;

    public double PlayerKickFrequency; 

    public double MaxDribbleForce;
    public double MaxShootingForce;
    public double MaxPassingForce;

    public double PlayerComfortZoneSq;

    //in the range zero to 1.0. adjusts the amount of noise added to a kick,
    //the lower the value the worse the players get
    public double PlayerKickingAccuracy;

    //the number of times the SoccerTeam::CanShoot method attempts to find
    //a valid shot
    public int NumAttemptsToFindValidStrike;

    //the distance away from the center of its home region a player
    //must be to be considered at home
    public double WithinRangeOfHome;

    //how close a player must get to a sweet spot before he can change state
    public double WithinRangeOfSupportSpot;
    public double WithinRangeOfSupportSpotSq;
 
 
    //the minimum distance a receiving player must be from the passing player
    public double MinPassDist;
    public double GoalkeeperMinPassDist;

    //this is the distance the keeper puts between the back of the net
    //and the ball when using the interpose steering behavior
    public double GoalKeeperTendingDistance;

    //when the ball becomes within this distance of the goalkeeper he
    //changes state to intercept the ball
    public double GoalKeeperInterceptRange;
    public double GoalKeeperInterceptRangeSq;

    //how close the ball must be to a receiver before he starts chasing it
    public double BallWithinReceivingRange;
    public double BallWithinReceivingRangeSq;


    //these values control what debug info you can see
    public boolean bStates;
    public boolean bIDs;
    public boolean bSupportSpots;
    public boolean bRegions;
    public boolean bShowControllingTeam;
    public boolean bViewTargets;
    public boolean bHighlightIfThreatened;

    public int FrameRate;

 
    public double SeparationCoefficient;

    //how close a neighbour must be before an agent perceives it
    public double ViewDistance;

    //zero this to turn the constraint off
    public boolean bNonPenetrationConstraint;
 
    public static Params Instance(){
        if (singleton==null){
            singleton = new Params();
            singleton.load();
 
        }
        return singleton;
    }
    public void load(){
        //def config = new ConfigSlurper().parse(new File(path).toURL())
        GoalWidth = 100

        //use to set up the sweet spot calculator
        NumSupportSpotsX = 13
        NumSupportSpotsY = 6

        //these values tweak the various rules used to calculate the support spots
        Spot_PassSafeScore = 2.0
        Spot_CanScoreFromPositionScore = 1.0
        Spot_DistFromControllingPlayerScore = 2.0
        Spot_ClosenessToSupportingPlayerScore= 0.0
        Spot_AheadOfAttackerScore = 0.0 

        //how many times per second the support spots will be calculated
        SupportSpotUpdateFreq = 1

        //the chance a player might take a random pot shot at the goal
        ChancePlayerAttemptsPotShot = 0.005

        //this is the chance that a player will receive a pass using the arrive
        //steering behavior, rather than Pursuit
        ChanceOfUsingArriveTypeReceiveBehavior= 0.5

        BallSize = 5.0
        BallMass = 1.0
        Friction = -0.015

        //the goalkeeper has to be this close to the ball to be able to interact with it
        KeeperInBallRange = 10.0
        PlayerInTargetRange = 10.0

        //player has to be this close to the ball to be able to kick it. The higher
        //the value this gets, the easier it gets to tackle. 
        PlayerKickingDistance = 6.0

        //the number of times a player can kick the ball per second
        PlayerKickFrequency = 8

        PlayerMass = 3.0
        PlayerMaxForce = 1.0
        PlayerMaxSpeedWithBall = 1.2
        PlayerMaxSpeedWithoutBall = 1.6
        PlayerMaxTurnRate = 0.4
        PlayerScale = 1.0

        //when an opponents comes within this range the player will attempt to pass
        //the ball. Players tend to pass more often, the higher the value
        PlayerComfortZone = 60.0

        //in the range zero to 1.0. adjusts the amount of noise added to a kick,
        //the lower the value the worse the players get.
        PlayerKickingAccuracy = 0.99

        //the number of times the SoccerTeam::CanShoot method attempts to find
        //a valid shot
        NumAttemptsToFindValidStrike = 5

        MaxDribbleForce = 1.5
        MaxShootingForce = 6.0
        MaxPassingForce = 3.0


        //the distance away from the center of its home region a player
        //must be to be considered at home
        WithinRangeOfHome = 15.0

        //how close a player must get to a sweet spot before he can change state
        WithinRangeOfSupportSpot = 15.0

        //the minimum distance a receiving player must be from the passing player
        MinPassDist = 120.0
        //the minimum distance a player must be from the goalkeeper before it will
        //pass the ball
        GoalkeeperMinPassDist = 50.0

        //this is the distance the keeper puts between the back of the net 
        //and the ball when using the interpose steering behavior
        GoalKeeperTendingDistance = 20.0

        //when the ball becomes within this distance of the goalkeeper he
        //changes state to intercept the ball
        GoalKeeperInterceptRange = 100.0

        //how close the ball must be to a receiver before he starts chasing it
        BallWithinReceivingRange = 10.0

        //these (boolean) values control the amount of player and pitch info shown
        //1=ON; 0=OFF
 
        bStates = true
        bIDs = true
        bSupportSpots = true
        bRegions = true
        bShowControllingTeam = true
        bViewTargets = false
        bHighlightIfThreatened = false

        //simple soccer's physics are calculated using each tick as the unit of time
        //so changing this will adjust the speed
        FrameRate = 60;


        //--------------------------------------------steering behavior stuff
        SeparationCoefficient = 10.0;

        //how close a neighbour must be to be considered for separation
        ViewDistance = 30.0;

        //1=ON; 0=OFF
        bNonPenetrationConstraint = false



        BallWithinReceivingRangeSq = BallWithinReceivingRange * BallWithinReceivingRange;
        KeeperInBallRangeSq = KeeperInBallRange * KeeperInBallRange;
        PlayerInTargetRangeSq = PlayerInTargetRange * PlayerInTargetRange; 
        PlayerKickingDistance += BallSize;
        PlayerKickingDistanceSq = PlayerKickingDistance * PlayerKickingDistance;
        PlayerComfortZoneSq = PlayerComfortZone * PlayerComfortZone;
        GoalKeeperInterceptRangeSq = GoalKeeperInterceptRange * GoalKeeperInterceptRange;
        WithinRangeOfSupportSpotSq = WithinRangeOfSupportSpot * WithinRangeOfSupportSpot;
 
 
    }
}