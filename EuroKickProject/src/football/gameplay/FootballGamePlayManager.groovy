package football.gameplay;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import sg.atom.gameplay.GameLevel;
import sg.atom.gameplay.GamePlayManager;
import football.FootballGame;
import football.world.FootballGameWorldManager;
import football.gameplay.info.*;
import football.gameplay.ai.states.team.*
import football.gameplay.ai.info.Params
import static football.gameplay.SoccerTeam.TeamColor.*
import football.FootballGameStageManager
import football.geom.Vector2D
import groovy.transform.*
import static football.stage.sound.SoundClip.*
import football.stage.sound.*
import com.jme3.scene.Node
/**
 *
 * @author cuong.nguyenmanh2
 */
@CompileStatic
public class FootballGamePlayManager extends GamePlayManager implements PhysicsCollisionListener {
    FbSoundManager soundManager
    //character -----------------------------------------------
    SoccerCoach currentPlayerCoach;
    League league;
    public FootballClub clubA;
    public FootballClub clubB;
    private FootballMatch fbMatch;
    private SoccerPitch pitch;
    private Spatial ballSpatial;
    protected boolean firstPersonView = true;
    float goalTimeInit = 3;
    float goalTime = 0;
    boolean normalUI = true;

    // simulation
    int simLevel = 0;
    
    // more
    Params Prm;
    SoccerBall ballController;
    SoccerTeam redTeam;
    SoccerTeam blueTeam;
    //true if a goal keeper has possession
    boolean goalKeeperHasBall;
    //true if the game is in play. Set to false whenever the players
    //are getting ready for kickoff
    boolean gameOn = false;
    //set true to pause the motion
    boolean paused = false;
  
    // CINEMATIC
    FootballCommentator commentator;
    
    // DEFAUT INSTANCE
    private static FootballGamePlayManager _defaultInstance;
  
    public FootballGamePlayManager(FootballGame app) {
        super(app);
        Prm = Params.Instance();
        _defaultInstance = this;
    }
    // Just default instance not a singleton!!!!
    public static FootballGamePlayManager getDefault(){
        return _defaultInstance;
    }
    
    
    private void setupKeys() {
    }

    @Override
    public void startLevel(GameLevel level) {
    }

    @Override
    public void configGamePlay() {
        createTestLeague();
    }

    public void createTestLeague(){
        league = new League("Champion League");
        league.fillRandomClub();
        league.participants.each{FootballClub club->
            println club.name
            club.playersList.each{info->
                println info;
            }
            println "Coach " +club.coach.name
            println "-------------------------------------"
        }

        league.scheduleSeason()
        def club = league.participants[0]
        println league.getNextMatch(club)
        println "The list of matches for" + club.name
        league.getListOfMatches(club).each{FootballMatch match->
            println match
        }
    }
    @Override
    public void loadGamePlay() {
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        //updateBall(tpf);
        if (!paused){
            if (gameOn){
                updateMatch(tpf);
            }
        }
    }
    //----------------------------- Update -----------------------------------
    //
    // this demo works on a fixed frame rate (60 by default) so we don't need
    // to pass a time_elapsed as a parameter to the game entities
    //------------------------------------------------------------------------
    public void updateMatch(float tpf){

        //update the balls
        ballController.update();
        //update the teams
        redTeam.update();
        blueTeam.update();
    }
    
    public void startGamePlay(){
        soundManager = stageManager.getSoundManager();
        // start the sound
        soundManager.play(STADIUM_BACKGROUND);
        //soundManager.play(MUSIC);
        currentPlayerCoach = league.participants[0].coach
        prepareMatch();
        startMatch();
    }
    /*
     * GOAL CHECKING
     */
    void checkGoal(float tpf) {
        //if a goal has been detected reset the pitch ready for kickoff
        if (pitch.blueGoal.checkScored(ball) || pitch.redGoal.checkScored(ball)){
            gameOn = false;
 
            println("Scored !");
            //reset the ball    
            ballController.placeAtPosition(pitch.getPlayingArea().getCenter());
            //get the teams ready for kickoff
            redTeam.getFSM().changeState(PrepareForKickOff.Instance());
            blueTeam.getFSM().changeState(PrepareForKickOff.Instance());
        }


    }
    /*
     *CINEMATIC SYSTEM
     */
    public void cineGoalBackNormal(float tpf){
        // 3D
        if (normalUI == false) {
            if (goalTime > 0) {
                goalTime -= tpf;
            } else {
                goalTime = 0;
                gameGUIManager.loadAndGotoScreen("ingameScreen");
                normalUI = true;
            }
        }
    }
    public void cineGoal(){
        goalTime = goalTimeInit;
        normalUI = false;
        //
        System.out.println("Goal");
        gameGUIManager.goToScreen("goal");
        moveBall(stageManager.getWorldManager().getStadiumMaker().getCenter());
    }
    public void cineShowTeamPositions(){
        
    }
    public void cineOpenGame(){
        // Two team out of the waitroom of the stadium
        // To the feild
        // Waving and clapping
        // Line up
        // Center passing
        // First half begin. Time start
    }
    
    public void prepareMatch(){
        fbMatch = league.getNextMatch(currentPlayerCoach.getClub());
        this.clubA = currentPlayerCoach.getClub();
        this.clubB = fbMatch.opponent(this.clubA);
        /*
        if (clubA ==fbMatch.getHomeClub()){
        // The player club play in home stadium
        } else {
            
        }
         */
        // Prepare the interview and the commentator
        commentator = new FootballCommentator("Bob");
        commentator.say("Hello, I'm "+commentator.name+" and welcome to the match "+fbMatch.getShortIntro()+" in " + fbMatch.stadiumName);
        commentator.say("between "+ clubA.getShortIntro() + " against "+ clubB.getShortIntro());
        //clubA = new FootballClub("Team A");
        //clubB = new FootballClub("Team B");
        //fbMatch = new FootballMatch(clubA, clubB, "Final");
    }
    void arrangeTeams(){
        //create the teams AI
        redTeam = clubA.createTeamForMatch(this,fbMatch,0);
        blueTeam = clubB.createTeamForMatch(this,fbMatch,1);
        //
        //make sure each team knows who their opponents are
        /*
        redTeam.setOpponents(blueTeam);
        blueTeam.setOpponents(redTeam); 
         */
        redTeam.initMatch(blueTeam,pitch,pitch.redGoal,red);
        blueTeam.initMatch(redTeam,pitch,pitch.blueGoal,blue);
        Node worldNode = getStageManager().getWorldManager().getWorldNode();
        clubA.attachModels(assetManager, worldNode);
        clubB.attachModels(assetManager, worldNode);
        clubA.arrangeStartPos(0);
        clubB.arrangeStartPos(1);
        // attach

    }


    public void startMatch() {
        paused=false;
        goalKeeperHasBall=false;
        
        pitch = SoccerPitch.getDefault();
        pitch.init(getStageManager().getWorldManager().getStadiumMaker());
        
        try{
            arrangeTeams();
            createBall();
        }catch(Error er){
            //er.printStackTrace()
        }catch(Exception ex){
            //ex.printStackTrace()
        }
        getStageManager().getWorldManager().getPhysicsSpace().addCollisionListener(this);
        gameOn=true;
        redTeam.startMatch();
        blueTeam.startMatch();
    }
    /**
     * BALL CONTROL, GAME SIMULATION CONTROL
     */
    void createBall(){
        ballController = new SoccerBall(pitch.getPlayingArea().getCenter(),Prm.BallSize,Prm.BallMass,pitch.getWalls());
        ballSpatial = ((FootballGameWorldManager) stageManager.getWorldManager()).createBall();
        ballController.setSpatial(ballSpatial)
    }
    void updateBall(float tpf) {
        Camera cam = stageManager.getCurrentActiveCamera();
        Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        ((FootballGameWorldManager) stageManager.getWorldManager()).getStadiumMaker().getFieldNode().collideWith(ray, results);
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            moveBall(closest.getContactPoint());
        }
    }

    void moveBall(Vector3f pos) {
        //RigidBodyControl physicsBallControl = ball.getControl(RigidBodyControl.class);
        //physicsBallControl.applyForce(pos, pos);


        ballSpatial.setLocalTranslation(pos);
        /*
        Quaternion q = new Quaternion();
        q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
        ball.setLocalRotation(q);
         */
    }
    /**
     * STRAGEGY
     */
    public void changeStragegy(String selectedItem) {
    
    }
    /* 
     * PHYSIC 
     */
    public boolean isCollisonBetween(PhysicsCollisionEvent event, String nodeA, String nodeB) {
        if (nodeA.equals(event.getNodeA().getName()) || nodeA.equals(event.getNodeB().getName())) {
            if (nodeB.equals(event.getNodeA().getName()) || nodeB.equals(event.getNodeB().getName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (isCollisonBetween(event, "goalA", "Soccer ball") || isCollisonBetween(event, "goalB", "Soccer ball")) {
            if (goalTime <= 0) {
                cineGoal();
            }
        }
    }
    /* 
     * SIMULATION SETTINGS
     */
    public void simulate(int simLevel){
        // level 0
        // just calculate the points and random the score
        // level 1
        // short simulation in another thread and count the score
        // level 2
        // full simulation with the 3d models
    }
    /*
     *shortcut getter setter
     */
    public FootballGameStageManager getStageManager() {
        return (FootballGameStageManager) super.getStageManager();
    }
    public void togglePause(){
        paused = !paused;
    }
    public boolean isPaused(){
        return paused;
    }
    public boolean isGoalKeeperHasBall(){
        return goalKeeperHasBall;
    }
    public void setGoalKeeperHasBall(boolean b){
        goalKeeperHasBall = b;
    }
    public SoccerBall getBall(){
        return ballController;
    }

    public boolean isGameOn(){
        return gameOn;
    }
    public void setGameOn(){
        gameOn = true;
    }
    public void setGameOff(){
        gameOn = false;
    }
 
    public SoccerTeam getRedTeam(){
        return redTeam;
    }
 
    public SoccerTeam getBlueTeam(){
        return blueTeam;
    }
  
    // gameplay
    public SoccerCoach getCurrentPlayerAsCoach(){
        return clubA.getCoach();
    }
    
    public SoccerPitch getPitch(){
        return pitch;
    }
}
