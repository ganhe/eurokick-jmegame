package football.gameplay.info;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import football.FootballGameStageManager;
import football.world.StadiumMaker;
import java.util.ArrayList;
import football.gameplay.SoccerCoach
import football.gameplay.FootballGamePlayManager
import football.gameplay.info.FootballPlayerInfo;
import football.gameplay.core.FootballEntityManager as EntityManager;
import groovy.transform.*
import football.gameplay.info.*;
import football.gameplay.info.training.TrainingCourse
import football.gameplay.SoccerTeam
import football.gameplay.SoccerCoach
import football.gameplay.PlayerBase
/**
 *
 * @author cuong.nguyenmanh2
 */
@CompileStatic
public class FootballClub {
    String name;
    String country;
    String logoPath;
    int rate;
    boolean currentChampion;
  
    ArrayList<FootballPlayerInfo> playersList;
    SoccerCoach coach;
    int numOfMembers = 11;
    SoccerTeam team;
    // Training
    TrainingCourse trainingCourse;
    
    // Team possitions for the game
    FbTeamPositions startTeamPos;
    FbTeamPositions currentTeamPos;
    String shirt;
    
    public FootballClub(String name) {
        this.name = name;
        playersList = new ArrayList<FootballPlayerInfo>();
    }

    public void fillPlayerList(){
        RandomPlayerGenerator gen=new RandomPlayerGenerator();
        for (int i = 0; i < numOfMembers; i++) {
            FootballPlayerInfo player = makePlayerInfo( "Player " + i, PlayerRole.Striker);
            gen.getRandomPlayerInfo(player);
            playersList.add(player);
            if (i==0){
                player.setRole(PlayerRole.GoalKeeper)
            }
        }
        this.shirt = PlayerCustomizeSystem.getRandomShirt();;
        this.coach = new SoccerCoach(gen.getRandomName(),this);
    }
    public void hireACoach(){
        
    }
    public FootballPlayerInfo makePlayerInfo(String name,PlayerRole role){
        FootballPlayerInfo player = new FootballPlayerInfo(this, name, role);
    } 
    
    public SoccerTeam createTeamForMatch(FootballGamePlayManager gamePlayManager,FootballMatch match,int part) {
        this.startTeamPos = arrangeTeamForMatch(gamePlayManager,match,part);
        this.team = new SoccerTeam(gamePlayManager);
        
        for (FootballPlayerInfo playerInfo:startTeamPos.positions.keySet()) {
            playerInfo.createPlayer(team);
            team.players.add(playerInfo.getPlayerBase());
        }
        
        return this.team;
    }
    
    public void arrangeStartPos(int part){
        team.arrangeStartPos(startTeamPos);
    }

    public FbTeamPositions arrangeTeamForMatch(FootballGamePlayManager gamePlayManager,FootballMatch match,int part){
        // reposition
        //gamePlayManager.getCurrentPlayerMatch()
        return coach.arrangeTeamForMatch(gamePlayManager,match,part);
        // fbCinematic.teamReady(this);
    }
    /*
    public FootballPlayerInfo makePlayer(FootballGamePlayManager gamePlayManager, String name, PlayerRole pos) {
    FootballPlayerInfo player = new FootballPlayerInfo(this, name, pos);
    player.initPlayer(gamePlayManager);
    return player;
    }
     */
    public void attachModels(AssetManager assetManager, Node rootNode) {
        if (team!=null){
            for (PlayerBase base: team.getMembers()) {
                FootballPlayerInfo player = base.getInfo();
                player.loadModel(assetManager);
                player.attachPlayer(rootNode);
                //player.setupMatchInfo();
            }
        }
    }
    
    public String getShortIntro(){
        return "" + name;
    }
    
    public String toString(){
        return getShortIntro();
    }
}
