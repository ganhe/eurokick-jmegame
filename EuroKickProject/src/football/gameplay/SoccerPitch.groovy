package football.gameplay
import com.jme3.math.*
import football.geom.Vector2D
import football.geom.Wall2D
import football.gameplay.ai.info.Params

import static football.gameplay.SoccerTeam.TeamColor.*
import static football.geom.Geometry2DFunctions.*

import groovy.swing.j2d.*
import groovy.transform.*
import football.world.StadiumMaker

@CompileStatic
public class SoccerPitch { 
    // 
    FootballGamePlayManager gamePlayManager;
    Goal redGoal;
    Goal blueGoal;
    //container for the boundary walls
    ArrayList<Wall2D> vecWalls =new ArrayList<Wall2D>();
    //defines the dimensions of the playing area
    Region playingArea;
    //the playing field is broken up into regions that the team
    //can make use of to implement strategies.
    List<Region> regions=[];
 
    //local copy of client window dimensions
    float cxClient,cyClient; 
    int numRegionsHorizontal = 6; 
    int numRegionsVertical = 4;
    int border = 20;
    Params Prm;
    StadiumMaker stadium;
    Transform transformTo3D;
    static SoccerPitch _defaultInstance;
    
    public SoccerPitch(int cx,int cy,int cborder = 20){
        cxClient=cx;
        cyClient=cy;
        border = cborder;
        regions=[];
        _defaultInstance= this;
    }
    
    // get the default size football pitch which is
    // width :
    // height :
    static SoccerPitch getDefault(){
        if (_defaultInstance == null){
            _defaultInstance= new SoccerPitch(100,75);
        }
        return _defaultInstance;
    }
    
    public void init(StadiumMaker stadium=null){
        calculateArea(stadium);
    }
    public void calculateArea(StadiumMaker stadium=null){
        if (stadium!=null){
            setStadium(stadium);
            //define the playing area
            //playingArea = new Region(stadium.getConnerLoc(1),stadium.getConnerLoc(2),stadium.getConnerLoc(3),stadium.getConnerLoc(4));
            //cxClient=stadium.getFieldX();
            //cyClient=stadium.getFieldY();
        } else {
            //define the playing area
            
        }
        playingArea = new Region(0,0,cxClient,cyClient);
        Prm = Params.Instance();
 
        //create the regions 
        createRegions(getPlayingArea().getWidth() / (double)numRegionsHorizontal,
            getPlayingArea().getHeight() / (double)numRegionsVertical);
 
        //create the goals
        redGoal = new Goal(new Vector2D( playingArea.getLeft(),
                (cyClient-Prm.GoalWidth)/2),
            new Vector2D(playingArea.getLeft(),
                cyClient - (cyClient-Prm.GoalWidth)/2),
            new Vector2D(1,0));
        blueGoal = new Goal( new Vector2D( playingArea.getRight(),
                (cyClient-Prm.GoalWidth)/2),
            new Vector2D(playingArea.getRight(),
                cyClient - (cyClient-Prm.GoalWidth)/2),
            new Vector2D(-1,0));

        //create the walls
        Vector2D topLeft=new Vector2D (playingArea.getLeft(),playingArea.getTop()); 
        Vector2D topRight=new Vector2D (playingArea.getRight(),playingArea.getTop());
        Vector2D bottomRight=new Vector2D (playingArea.getRight(),playingArea.getBottom());
        Vector2D bottomLeft=new Vector2D (playingArea.getLeft(),playingArea.getBottom());
 
        if (!vecWalls.isEmpty()){
            vecWalls.clear();
        }
        vecWalls.add(new Wall2D(bottomLeft,redGoal.getRightPost()));
        vecWalls.add(new Wall2D(redGoal.getLeftPost(),topLeft));
        vecWalls.add(new Wall2D(topLeft,topRight));
        vecWalls.add(new Wall2D(topRight,blueGoal.getLeftPost()));
        vecWalls.add(new Wall2D(blueGoal.getRightPost(),bottomRight));
        vecWalls.add(new Wall2D(bottomRight,bottomLeft));
    }
    //------------------------- Createregions --------------------------------
    public void createRegions(double width,double height){ 
        //index into the vector
        int idx = numRegionsHorizontal * numRegionsVertical-1;
 
        for (int col=0; col<numRegionsHorizontal; ++col){
            for (int row=0; row<numRegionsVertical; ++row){
                int rindex=idx - ( col * numRegionsVertical + row);
                regions[rindex] = new Region(getPlayingArea().getLeft()+col*width,
                    getPlayingArea().getTop()+row*height,
                    getPlayingArea().getLeft()+(col+1)*width,
                    getPlayingArea().getTop()+(row+1)*height,rindex);
                //println ("Create region " + rindex);
            }
        }
    }
    /*
    GraphicsBuilder gdi = null;
    //------------------------------ Render ----------------------------------
    @CompileDynamic
    public def Render(GraphicsBuilder gdi){
    if (this.gdi!=gdi){
    this.gdi = gdi
    gdi.group{
    //draw the grass

    rect(x:0,
    y:0,
    width:cxClient,
    height:cyClient,
    borderColor:'darkGreen',
    fill:'darkGreen');
    // Texture
                
    //                rect(x:0,
    //                y:0,
    //                width:cxClient,
    //                height:cyClient){
    //                texturePaint(file:"assets\\Textures\\Football_Pitch2.png",
    //                x:0,
    //                y:0,
    //                width:cxClient,
    //                height:cyClient)
    //                }
                 
    //render regions
    if (Prm.bregions){ 
    for ( int r=0; r<regions.size(); ++r){
    regions[r].Render(gdi,true);
    }
    }
 
    //render the goals

    rect(x:playingArea.getLeft(),
    y:(cyClient-Prm.GoalWidth)/2,
    width:40,
    height:Prm.GoalWidth,
    borderColor:'red');

    rect(x:playingArea.getRight()-40,
    y:(cyClient-Prm.GoalWidth)/2,
    width:40,
    height:Prm.GoalWidth,
    borderColor:'blue');
 
    //render the pitch markings

    circle(cx:playingArea.getCenter().x,
    cy:playingArea.getCenter().y,
    radius:playingArea.getWidth() * 0.125,
    borderWidth:2,
    borderColor:'white');
 
    line(x1:playingArea.getCenter().x,
    y1:playingArea.getTop(),
    x2:playingArea.getCenter().x,
    y2:playingArea.getBottom(),
    borderColor:'white');
    circle(cx:playingArea.getCenter().x,
    cy:playingArea.getCenter().y,
    radius:2.0,
    borderWidth:2,
    borderColor:'white');
 
    //the ball
    ball.Render(gdi);
 
    //Render the teams
    redTeam.Render(gdi);
    blueTeam.Render(gdi); 
 
    //render the walls

    for ( int w=0; w<vecWalls.size(); ++w){
    vecWalls[w].Render(gdi);
    }
 
    //show the score
    text(id:"redScore",
    x:(cxClient/2)-50,
    y:cyClient-18,
    text:"Red: " + blueGoal.getNumGoalscheckScored(),
    fill:'red',borderColor:"red");
 
    text(id:"blueScore",
    x:(cxClient/2)+10,
    y:cyClient-18,
    text:"Blue: " + redGoal.getNumGoalscheckScored(),
    fill:'blue',borderColor:"blue");
    }
    } else {
    //update only
    gdi.redScore.text = "Red: " + blueGoal.getNumGoalscheckScored()
    gdi.blueScore.text = "Blue: " + redGoal.getNumGoalscheckScored()
    //the ball
    ball.Render(gdi);
 
    //Render the teams
    redTeam.Render(gdi);
    blueTeam.Render(gdi);
    }
    }
     */
    public Region getRegionFromIndex(int idx){
        //println ("Ask for region " + idx);
        //assert ( (idx > 0) && (idx < regions.size()) );
        return regions[idx];
    }
 
    public float getCxClient(){
        return cxClient;
    }
 
    public float getCyClient(){
        return cyClient;
    }

    public Region getPlayingArea(){
        return playingArea;
    }
 
    List<Wall2D> getWalls(){
        return vecWalls;
    } 

    // coordinate transform
        
    public Region fromPosToRegion(){
        return new Region();
    }
    
    public Vector3f fromRegionToPos(){
        return new Vector3f();
    }
    
    public void setStadium(StadiumMaker stadium){
        this.stadium = stadium;
        this.transformTo3D = new Transform();
        transformTo3D.setScale((float)(stadium.getFieldX() / cxClient),1f,-(float)(stadium.getFieldY() / cyClient));
        Vector3f halfSize = new Vector3f((float)(-stadium.getFieldX() / 2),0f,(float)(stadium.getFieldY() / 2));
        
        transformTo3D.setTranslation(halfSize.add(stadium.getCenter()));
        //transformTo3D.setRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI,Vector3f.UNIT_Y));
        transformTo3D.setRotation(new Quaternion());
    }

    /*
    public void convertCoordinate(StadiumMaker stadium){
        
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
    
    public Vector3f vec2DToVec3D(Vector2D vec2D){
        Vector3f result = new Vector3f();
        Vector3f vec3D =new Vector3f((float)vec2D.x,0f,(float)vec2D.y);
        
        if (this.stadium!=null){
            transformTo3D.transformVector(vec3D,result);
        }
        return result;
    }
}
