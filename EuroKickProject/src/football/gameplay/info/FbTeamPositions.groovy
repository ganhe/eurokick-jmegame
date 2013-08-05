package football.gameplay.info

import com.jme3.math.Vector3f
import football.gameplay.Region
import football.world.StadiumMaker
import football.gameplay.SoccerPitch
import football.geom.Vector2D
/**
 *
 * @author cuong.nguyenmanh2
 */
public class FbTeamPositions {
    HashMap<FootballPlayerInfo,Vector2D> positions = new HashMap<FootballPlayerInfo,Vector2D>();
    StadiumMaker stadium;
    SoccerPitch soccerPitch;
    
    FbTeamPositions(){
        
    }
    FbTeamPositions(FbTeamPositions cloneObj){
        
    }
    public void setPosFor(PlayerRole role,Vector2D pos){
        //positions.set()
            
    }
    public void setPosFor(FootballPlayerInfo player,Vector2D pos){
        positions.put(player,pos);
    }
    /*
    public FbTeamPositions convertCoordinate(StadiumMaker stadium){
    FbTeamPositions teamPos = new FbTeamPositions(this);
    convertCoordinate(teamPos,stadium);
    return teamPos;
    }
    
    public void convertCoordinate(FbTeamPositions teamPos,StadiumMaker stadium){
        
    // MAKE GOAL KEEPER
    Vector3f goalKeeperPosition = stadium.getGoalKeeperPos(0);
    goalKeeperPosition.interpolate(stadium.getCenter(), 0.05f);
    teamPos.setPosFor(PlayerRole.GoalKeeper,goalKeeperPosition);
        
    // MAKE TEAM
    for (int number = 2; number <= postions.size(); number++) {
    FootballPlayerInfo player = teamList.get(number - 1);
    String stragegyTitle = stragegy.posTitle;
    PlayerRoleInMatch roleExtra = getRoleByNumber(stragegyTitle, number);
    Vector3f vecX = stadium.getGoalLoc(2).clone();
    vecX.interpolate(stadium.getGoalLoc(1), posArr[roleExtra.roleIndex]);

    float stepCount =(float)( 1f / (roleExtra.rowTotal + 1) * (roleExtra.rowNum + 1));
    Vector3f vecZ = stadium.getConnerLoc(1).clone();
    vecZ.interpolate(stadium.getConnerLoc(2), stepCount);
    Vector3f vecY = stadium.getCenter().clone();
    Vector3f loc = new Vector3f(vecX.x, vecY.y, vecZ.z);

    //player.setLocation(new Vector3f(randX, 0, randY));
    teamPos.setPosFor(player,loc);
    }
    }
     */
}

