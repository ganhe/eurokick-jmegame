package football.gameplay
import com.jme3.math.*
import football.geom.Vector2D
import football.geom.Wall2D
import football.gameplay.ai.info.Params
import football.gameplay.ai.states.team.*
import static football.gameplay.SoccerTeam.TeamColor.*
import static football.geom.Geometry2DFunctions.*

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class SoccerPitch{ 
    SoccerBall ball;
    SoccerTeam redTeam;
    SoccerTeam blueTeam;
    Goal redGoal;
    Goal blueGoal;
    //container for the boundary walls
    ArrayList<Wall2D> vecWalls =new ArrayList<Wall2D>();
    //defines the dimensions of the playing area
    Region playingArea;
    //the playing field is broken up into regions that the team
    //can make use of to implement strategies.
    List<Region> Regions=[];
    //true if a goal keeper has possession
    boolean goalKeeperHasBall;
    //true if the game is in play. Set to false whenever the players
    //are getting ready for kickoff
    boolean gameOn;
    //set true to pause the motion
    boolean paused;
    //local copy of client window dimensions
    int cxClient,cyClient; 
    int NumRegionsHorizontal = 6; 
    int NumRegionsVertical = 3;
    Params Prm;
 

 
    public SoccerPitch(int cx,int cy){
        cxClient=cx;
        cyClient=cy;
        paused=false;
        goalKeeperHasBall=false;
        Regions=[];
        gameOn=true;
        //define the playing area
        playingArea = new Region(20,20,cx-20,cy-20);
        Prm = Params.Instance();
        //create the regions 
        CreateRegions(PlayingArea().Width() / (double)NumRegionsHorizontal,
            PlayingArea().Height() / (double)NumRegionsVertical);
 
        //create the goals
        redGoal = new Goal(new Vector2D( playingArea.Left(),(cy-Prm.GoalWidth)/2),new Vector2D(playingArea.Left(),cy - (cy-Prm.GoalWidth)/2),new Vector2D(1,0));
 
        blueGoal = new Goal( new Vector2D( playingArea.Right(),(cy-Prm.GoalWidth)/2),new Vector2D(playingArea.Right(),cy - (cy-Prm.GoalWidth)/2),new Vector2D(-1,0));
        //create the soccer ball
 
        try{
            ball = new SoccerBall(new Vector2D((double)cxClient/2.0,(double)cyClient/2.0),Prm.BallSize,Prm.BallMass,vecWalls);
        } catch (InstantiationError e){
  
        }
        //create the teams 
        redTeam = new SoccerTeam(redGoal,blueGoal,this,red);
        blueTeam = new SoccerTeam(blueGoal,redGoal,this,blue);
        //make sure each team knows who their opponents are
        redTeam.setOpponents(blueTeam);
        blueTeam.setOpponents(redTeam); 
        //create the walls
        Vector2D TopLeft=new Vector2D (playingArea.Left(),playingArea.Top());     
        Vector2D TopRight=new Vector2D (playingArea.Right(),playingArea.Top());
        Vector2D BottomRight=new Vector2D (playingArea.Right(),playingArea.Bottom());
        Vector2D BottomLeft=new Vector2D (playingArea.Left(),playingArea.Bottom());
     
        vecWalls.add(new Wall2D(BottomLeft,redGoal.getRightPost()));
        vecWalls.add(new Wall2D(redGoal.getLeftPost(),TopLeft));
        vecWalls.add(new Wall2D(TopLeft,TopRight));
        vecWalls.add(new Wall2D(TopRight,blueGoal.getLeftPost()));
        vecWalls.add(new Wall2D(blueGoal.getRightPost(),BottomRight));
        vecWalls.add(new Wall2D(BottomRight,BottomLeft));
    }
    //----------------------------- Update -----------------------------------
    //
    // this demo works on a fixed frame rate (60 by default) so we don't need
    // to pass a time_elapsed as a parameter to the game entities
    //------------------------------------------------------------------------
    public void Update(float tpf){
        if (paused) return;
 
        int tick = 0;
        //update the balls
        ball.update();
        //update the teams
        redTeam.update();
        blueTeam.update();
        //if a goal has been detected reset the pitch ready for kickoff
        if (blueGoal.checkScored(ball) || redGoal.checkScored(ball)){
            gameOn = false;
 
            println("Scored !");
            //reset the ball       
            ball.PlaceAtPosition(new Vector2D((double)cxClient/2.0,(double)cyClient/2.0));
            //get the teams ready for kickoff
            redTeam.getFSM().changeState(PrepareForKickOff.Instance());
            blueTeam.getFSM().changeState(PrepareForKickOff.Instance());
        }

    }
    //------------------------- CreateRegions --------------------------------
    public void CreateRegions(double width,double height){ 
        //index into the vector
        int idx = NumRegionsHorizontal * NumRegionsVertical-1;
 
        for (int col=0; col<NumRegionsHorizontal; ++col){
            for (int row=0; row<NumRegionsVertical; ++row){
                int rindex=idx - ( col * NumRegionsVertical + row);
                Regions[rindex] = new Region(PlayingArea().Left()+col*width,
                    PlayingArea().Top()+row*height,
                    PlayingArea().Left()+(col+1)*width,
                    PlayingArea().Top()+(row+1)*height,rindex);
                //println ("Create region " + rindex);
            }
        }
    }
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
                /*
                rect(x:0,
                y:0,
                width:cxClient,
                height:cyClient){
                texturePaint(file:"assets\\Textures\\Football_Pitch2.png",
                x:0,
                y:0,
                width:cxClient,
                height:cyClient)
                }
                 */
                //render regions
                if (Prm.bRegions){ 
                    for ( int r=0; r<Regions.size(); ++r){
                        Regions[r].Render(gdi,true);
                    }
                }
  
                //render the goals

                rect(x:playingArea.Left(),
                    y:(cyClient-Prm.GoalWidth)/2,
                    width:40,
                    height:Prm.GoalWidth,
                    borderColor:'red');

                rect(x:playingArea.Right()-40,
                    y:(cyClient-Prm.GoalWidth)/2,
                    width:40,
                    height:Prm.GoalWidth,
                    borderColor:'blue');
 
                //render the pitch markings

                circle(cx:playingArea.getCenter().x,
                    cy:playingArea.getCenter().y,
                    radius:playingArea.Width() * 0.125,
                    borderWidth:2,
                    borderColor:'white');
  
                line(x1:playingArea.getCenter().x,
                    y1:playingArea.Top(),
                    x2:playingArea.getCenter().x,
                    y2:playingArea.Bottom(),
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
            int start,end;
            //the ball
            ball.Render(gdi);
  
            //Render the teams
            start = System.currentTimeMillis()
            redTeam.Render(gdi);
            end = System.currentTimeMillis()
            //println ("Draw Red take " + (end - start))
  
            start = System.currentTimeMillis()
            blueTeam.Render(gdi);
            end = System.currentTimeMillis()
            //println ("Draw Blue take " + (end - start))
        }
    }
 
    public void TogglePause(){
        paused = !paused;
    }
    public boolean Paused(){
        return paused;
    }
    public int cxClient(){
        return cxClient;
    }
    public int cyClient(){
        return cyClient;
    }
    public boolean isGoalKeeperHasBall(){
        return goalKeeperHasBall;
    }
    public void SetGoalKeeperHasBall(boolean b){
        goalKeeperHasBall = b;
    }
    public Region PlayingArea(){
        return playingArea;
    }
    List<Wall2D> Walls(){
        return vecWalls;
    }   
    public SoccerBall getBall(){
        return ball;
    }
    public Region GetRegionFromIndex(int idx){
        //println ("Ask for region " + idx);
        //assert ( (idx > 0) && (idx < Regions.size()) );
        return Regions[idx];
    }
    public boolean GameOn(){
        return gameOn;
    }
    public void SetGameOn(){
        gameOn = true;
    }
    public void SetGameOff(){
        gameOn = false;
    }
 
    public SoccerTeam redgetTeam(){
        return redTeam;
    }
 
    public SoccerTeam bluegetTeam(){
        return blueTeam;
    }
}
