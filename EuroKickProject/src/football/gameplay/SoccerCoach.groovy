package football.gameplay

import football.gameplay.info.FbTeamPositions
import football.gameplay.info.FootballStragegy
import football.gameplay.info.FootballPlayerInfo
import football.gameplay.info.PlayerRoleInMatch
import football.gameplay.info.*
import static football.gameplay.info.PlayerRole.*
import football.world.StadiumMaker
import com.jme3.math.Vector3f
import sg.atom.gameplay.player.Player
/**
 *
 * @author cuong.nguyenmanh2
 */
public class SoccerCoach {
    String name
    // 
    FootballClub club;
    FootballGamePlayManager gamePlayManager;
    ArrayList<FootballStragegy> stragegies = new ArrayList<FootballStragegy>();
    float[] posArr = [0.2f, 0.4f, 0.6f];
    Player gamePlayer;
    FootballStragegy currentStragegy;
    SoccerPitch pitch;
    // The team stragistic positions which the whole team are trained in train sessions
    FbTeamPositions savedTeamPos;
    /*
     * STRAGEGY
     */
    boolean isAI = true;
    
    SoccerCoach(String name,FootballClub club){
        this.name = name;
        this.club = club;
        this.isAI = true;
    }
    
    SoccerCoach(Player gamePlayer,FootballClub club){
        this.gamePlayer = gamePlayer;
        this.name = currentGamePlayer.name;
        this.club = club;
        this.isAI = false;
    }
    public void generateStragegies(){
        this.stragegies << new FootballStragegy("Attack1","4-4-2")
        this.stragegies << new FootballStragegy("Defend1","3-3-4");
        this.stragegies << new FootballStragegy("Attack2","4-5-1");
    }
    
    public FootballStragegy findStragegy(String title){
        return this.stragegies.find{FootballStragegy it->it.posTitle.equals(title)};
    } 
    
    public FootballStragegy findStragegyForMatch(FootballMatch match){
        return findStragegy("4-4-2");
    } 
    public ArrayList<FootballStragegy> getStragegies(){
        return this.stragegies;
    }
    
    public FbTeamPositions arrangeTeamForMatch(FootballGamePlayManager gameplay,FootballMatch match,int part){
        if (gamePlayer!=null){
            
        }
        // decide a stragegy
        changeStragegy(findStragegy("4-4-2"));
        // choose player in club player list by calculating training score
        // 
        List teamList= []
        // find best Goal keeper
        //teamList << club.playersList.findAll{player-> player.role == GoalKeeper}.max{player-> player.skillGoalKeep}
        teamList.addAll(club.playersList[0..10])
        // find num of best field player
        return arrangeTeam(gameplay,teamList,part);
    }
        
    public void changeStragegy(FootballStragegy stragegy) {
        currentStragegy = stragegy;
        /*
        // random speed
        int speed = 1 + FastMath.rand.nextInt(9);
        player.setSpeed(speed);
         */

    }
    public FbTeamPositions arrangeTeam(FootballGamePlayManager gameplay,List<FootballPlayerInfo> teamList,int part){
        //StadiumMaker stadium = gamePlayManager.getStageManager().getWorldManager().getStadiumMaker();
        FbTeamPositions teamPos = new FbTeamPositions();
        pitch = SoccerPitch.getDefault();
        pitch.init(null)
        
        (0..10).each{num->
            teamPos.positions.put(teamList[num],pitch.regions[part * 12 + num].center)
            //println "p " + num + " "+pitch.regions[num].center
        }
        println teamPos.positions
        return teamPos;
    }
    
    public void arrangeTeam3d(){
        /*
         *       // MAKE GOAL KEEPER
        Vector3f goalKeeperPosition = stadium.getGoalKeeperPos(0);
        goalKeeperPosition.interpolate(stadium.getCenter(), 0.05f);
        strTeamPos.setPosFor(PlayerRole.GoalKeeper,goalKeeperPosition);
        
        // MAKE TEAM
        for (int number = 2; number <= teamList.size(); number++) {
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
        strTeamPos.setPosFor(player,loc);
        }
        teamPos = strTeamPos.convertCoordinate(stadium);
         */
    }
    
    PlayerRoleInMatch getRoleByNumber(String stragegyPosTitle, int number) {
        if (number == 1) {
            return new PlayerRoleInMatch(GoalKeeper, 1, 1, 0);
        } else {
            String[] strSplit = stragegyPosTitle.split("-");
            int count = 1;
            for (int i = 0; i < strSplit.length; i++) {
                int rowTotal = Integer.parseInt(strSplit[i]);
                if (number > count + rowTotal) {
                    count += rowTotal;
                } else {
                    PlayerRole role = Striker; //byIndex(i + 1);
                    return new PlayerRoleInMatch(role, number - count - 1, rowTotal, i);
                }
            }
        }
        return null;
    }
}

