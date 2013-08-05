package football.gameplay.info;

import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import football.FootballGameStageManager;
import football.gameplay.control.FootballPlayerControl
import groovy.transform.*
import football.gameplay.info.*;
import football.gameplay.info.training.*;
import football.gameplay.FootballGamePlayManager
import football.gameplay.PlayerBase
import football.gameplay.GoalKeeper
import football.gameplay.FieldPlayer
import football.gameplay.SoccerTeam
import com.jme3.font.BitmapText
import com.jme3.scene.control.BillboardControl
import com.jme3.font.BitmapFont
import com.jme3.scene.Geometry
import com.jme3.animation.AnimControl
import com.jme3.animation.AnimChannel
import football.geom.shape.Tube
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
/**
 *
 * @author cuong.nguyenmanh2
 */
@CompileStatic
public class FootballPlayerInfo {
    
    public PlayerRole role;
    PlayerRole matchRole;
    String name;
    String country;

    // static attribute
    public int skillSpeed;
    public int skillBallControl;
    public int skillBallTake;
    public int skillBallKeep;
    public int skillGoalKeep;
    public int skillPass;
    
    // physic attribute
    public float height;
    public float weight;
    public float eyeSight;
    public Date birthDate;

    // real time Control things
    public int speed;
    public int energy;
    public int attitude;
    public int decay;

    Node playerModel;
    FootballClub club;
    FootballPlayerControl playerControl;
    float realSpeed;
    Quaternion initalRot;

    // training result
    TrainingResult training;
    int status = 0;
    PlayerBase playerBase;
    BitmapFont myFont; 
    public FootballPlayerInfo(FootballClub club, String name) {
        this.name = name;
        this.club = club;
        this.role = null;
    }
    public FootballPlayerInfo(FootballClub club, String name, PlayerRole role) {
        this.name = name;
        this.club = club;
        this.role = role;
    }

    void setEnergy(int speed, int energy, int attitude, int decay) {

        this.speed = speed;
        this.energy = energy;
        this.attitude = attitude;
        this.decay = decay;

    }

    void setSkills(int skillBallControl, int skillBallTake, int skillBallKeep, int skillGoalKeep, int skillPass) {

        this.skillBallControl = skillBallControl;
        this.skillBallTake = skillBallTake;
        this.skillBallKeep = skillBallKeep;
        this.skillGoalKeep = skillGoalKeep;
        this.skillPass = skillPass;
    }

    void loadModel(AssetManager assetManager) {
        //playerModel = (Node) assetManager.loadModel("Models/Player/PlayerOld/PlayerAni.j3o");
        playerModel = (Node) assetManager.loadModel("Models/Player/Player2/base_male.j3o");
        myFont = assetManager.loadFont("Interface/Fonts/Console.fnt");
        
        Node armature = (Node)playerModel.getChild("Armature");
        Node cloth = (Node)armature.getChild("Body");
        Geometry clothGeo = (Geometry)cloth.getChild("cloth1");
        String texPath = this.club.shirt;
        clothGeo.getMaterial().setTexture("DiffuseMap",assetManager.loadTexture(texPath))
        //armature.setShadowMode(ShadowMode.Cast);
                        
        AnimControl animControl = cloth.getControl(AnimControl.class);
        AnimChannel channel= animControl.createChannel();
        channel.setAnim("Stand");
        
        Material redMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        if (playerBase.getTeam().getColor()==SoccerTeam.TeamColor.red){
            redMat.setColor("Color",new ColorRGBA(1,0,0,1));
        } else {
            redMat.setColor("Color",new ColorRGBA(0,0,1,1));
        }
        Tube tube = new Tube(1.2f,1f,0.02f);
        Geometry tubeGeo = new Geometry("Tube1",tube);
        tubeGeo.setMaterial(redMat)
        armature.attachChild(tubeGeo);
    }

    
    void setLocation(Vector3f pos) {
        /*
        if (initialLocation == null) {
        initialLocation = pos;
        }
        if (playerModel != null) {
        playerModel.setLocalTranslation(pos);
        }
         */
    }

    void initPlayer(FootballGamePlayManager gamePlayManager) {
        //playerControl = new FootballPlayerControl();
        //playerControl.initPlayerControl(this, gamePlayManager.getStageManager().getWorldManager().getBallSpatial());

    }

    void attachPlayer(Node rootNode) {
        //playerModel.scale(0.02f);
        //initalRot = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
        //playerModel.setLocalRotation(initalRot);
        rootNode.attachChild(playerModel);
        playerModel.setLocalScale(1f);

        this.playerBase.setSpatial(playerModel);


        //setLocation(initialLocation);
        //playerModel.addControl(playerControl);
    }

    void addNumber(){
        BitmapText number = new BitmapText(myFont,true);
        number.setText(this.name[0..8]);
        number.setLocalTranslation(new Vector3f(0f,3f,0f));
        number.addControl(new BillboardControl());
        number.scale(5);
        playerModel.attachChild(number);
    }
    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        //realSpeed = speed * 0.02f;
    }

    public FootballClub getClub() {
        return club;
    }

    public void setClub(FootballClub club) {
        this.club = club;
    }
    public PlayerRole setRole(PlayerRole newRole){
        this.role = newRole;
    }
    public PlayerRole getRole(){
        return role;
    }
    
    public void createPlayer(SoccerTeam team){
        if (role.equals(PlayerRole.GoalKeeper)){
            this.playerBase = new GoalKeeper(team,this);     

        } else {
            this.playerBase = new FieldPlayer(team,role,this);
        }

    }
    
    public String toString(){
        return "Player "+name+" : "+
        club +" "+
        role +" "+
        skillSpeed +" "+
        skillBallControl +" "+
        skillBallTake +" "+
        skillBallKeep +" "+
        skillGoalKeep +" "+
        skillPass +" "+
        height +" "+
        weight +" "+
        eyeSight +" "+
        birthDate +" "+
        speed +" "+
        energy +" "+
        attitude +" "+
        decay;
    }
}


