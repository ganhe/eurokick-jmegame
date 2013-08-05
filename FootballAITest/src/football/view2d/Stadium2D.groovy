package football.view2d

import javax.swing.JPanel

import java.awt.*
import java.awt.BorderLayout as BL
import static java.awt.Color.*
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.*
import groovy.swing.j2d.*

import football.gameplay.*
import football.gameplay.ai.geom.*
import football.gameplay.ai.states.fieldplayer.*
import football.gameplay.ai.states.goalkeeper.*
import football.geom.*

import groovy.swing.SwingBuilder
import groovy.swing.j2d.*

public class Stadium2D  implements MouseListener,MouseMotionListener{
       
    GraphicsBuilder gb
    SoccerPitch pitch;
    Timer timer;
    int NORMAL_MSPF = 15;
    //To Measure FPS
    private long now = 0;
    private int framesCount = 0;
    private int framesCountAvg=0; 
    private long framesTimer=0;
    float zoomLevel=1;
    boolean createPitch = true;
    def pwidth = 550
    def pheight =  375
    def fw=zoomFactor(110);
    def fh=zoomFactor(75);
    def panel;
    
    
    boolean stoped = false;
    boolean paused = false;
    // Debug purpose
    boolean debugTeam = false;
    boolean debugPlayers = false;
    
    public static void expandoClasses(){
        Vector2D.expandoClass();        
    }
    public Stadium2D(){
        gb = new GraphicsBuilder();
        expandoClasses();
        pitch = new SoccerPitch(550, 375);
        createPitch = true;
    }
    def zoomFactor(x){
        x* 5
    }
    def getDrawOp(panel){
        this.panel = panel
        pwidth = panel.width
        pheight = panel.height
        panel.addMouseMotionListener( this )
        panel.addMouseListener( this )
        return Render();
    }
    
    def Render(){

        if (createPitch){
            createPitch = false;
            return gb.group{
                transformations{
                    scale( id:"zoom",x:zoomLevel,y: zoomLevel )
                }
                group(id:"insideCourt"){
                    /*
                    transformations{
                    translate(x:zoom(5),y:zoom(5))
                    }
                     */
                
                    // Field
                    //rect( x: 0, y: 0, width:fw, height: fh, borderColor: 'black', fill:'green' )
            
                    //line(x1:fw/2 ,y1:0 ,x2:fw/2 ,y2:fh,borderColor: 'white',borderWidth: 3)
                    //image(x: 0, y: 0,file:"assets\\Textures\\Football_Pitch2.png")
                
                    pitch.Render(gb)
                    /*
                    // Goals
                    rect( x: 0, y: fh/2-zoom(18)/2, width: zoom(5), height: zoom(18), borderColor: 'white',borderWidth: 3, fill:'orange' )
                    rect( x: fw-zoom(5), y: fh/2-zoom(18)/2, width: zoom(5), height: zoom(18), borderColor: 'white',borderWidth: 3, fill:'orange' )
            
                
                    circle( cx: 155, cy: 35, radius: 25, borderColor: 'black', fill: 'darkGreen' )
                    ellipse( cx: 225, cy: 35, radiusx: 35, radiusy: 25, borderColor: 'black', fill: 'blue' )
                    arc( x: -40, y: 70, width: 100, height: 100, start: 0, extent: 90, 
                    borderColor: 'black', fill: 'cyan', close: 'pie' )
                    arc( x: 20, y: 70, width: 100, height: 100, start: 0, extent: 90, 
                    borderColor: 'black', fill: 'magenta', close: 'chord' )
            
                    polygon( points: [190,70,225,90,260,70,250,120,225,110,200,120], 
                    borderColor: 'black', fill: 'black' )
                    path( borderColor: 'black', fill: 'purple', winding: 'nonzero' ){
                    moveTo( x: 40, y: 130 )
                    lineTo( x: 20, y: 200 )
                    lineTo( x: 70, y: 158 )
                    lineTo( x: 10, y: 158 )
                    lineTo( x: 60, y: 200 )
                    close()
                    }
                    path( borderColor: 'black', fill: 'lime', winding: 'evenodd' ){
                    moveTo( x: 120, y: 130 )
                    lineTo( x: 100, y: 200 )
                    lineTo( x: 150, y: 158 )
                    lineTo( x: 90, y: 158 )
                    lineTo( x: 140, y: 200 )
                    close()
                    }
                     */
                
                    //circle( cx: 155, cy: 35, radius: 5, borderColor: 'black', fill: 'darkGreen' )

                }
            }
            
        } else {
            pitch.Render(gb)
        }
        
        
    }
    void startGame(){
   
        //long beforeTime = System.nanoTime();
        Render();
        Thread.start{
            while (!stoped){
                if (!paused){
                    try{
                        pitch.Update(0.01f);

                    } catch (Exception e){
                        e.printStackTrace();
                        stoped = true;
                    }
                    sleep 40;
                } else {
                    //println " Stoped";
                    sleep 180;
                }
                
                if (debugTeam){
                    println pitch.redgetTeam().getFSM().getCurrentStateName();
                    pitch.redgetTeam().debuggetTeam();
                    println " -----------------------------------------------";
                }
            }
        }
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                //println("============================ Step " + framesCount + " : " + (System.currentTimeMillis() - now));
                //pitch.Update(0.01f);
                now=System.currentTimeMillis(); 
                if (!paused){
                    Render();
                }
                //println(" Draw take : " + (System.currentTimeMillis() - now));
                framesCount++;
                now=System.currentTimeMillis(); 
                
            }
        };
        now=System.currentTimeMillis(); 
        //countFramePerSecond(beforeTime);
        timer = new Timer( NORMAL_MSPF , taskPerformer);
        timer.setRepeats(true);
        timer.start();
    }
    
    void countFramePerSecond(long beforeTime){
        framesCount++; 
        if(now-framesTimer>1000)
        { 
            framesTimer=now; 
            framesCountAvg=framesCount; 
            framesCount=0; 
        }
    }
    
    public void zoomUp(int amount){
        zoomLevel += 0.1f*amount;
        setZoom()
    }
    
    public void zoomDown(int amount){
        zoomLevel -= 0.1f*amount;
        setZoom()
    }
    
    public void setZoom(){
        gb."zoom".x=zoomLevel;
        gb."zoom".y=zoomLevel;
    }
    /* ===== MouseListener ===== */

    public void mouseEntered( MouseEvent e ){
        //lastTargets.clear()
    }

    public void mouseExited( MouseEvent e ){
        //lastTargets.clear()
    }

    public void mousePressed( MouseEvent e ){
        //fireMouseEvent( e, "mousePressed" )
    }

    public void mouseReleased( MouseEvent e ){
        //fireMouseEvent( e, "mouseReleased" )
    }

    public void mouseClicked( MouseEvent e ){
        if (e.getButton() == e.BUTTON1){
            /*
            pitch.redgetTeam().getMembers().each{player->
            //println "player" + player.getID() + " : " +player.getFSM().getCurrentState().getClass.name;
            if (FieldPlayer.isInstance(player)){
            player.getFSM().changeState(ChaseBall.Instance())
            //println "change state player" + player.getID() +" to "+player.getFSM().getCurrentState().getClass().name;
            }
            }
             */
            pitch.getBall().PlaceAtPosition(new Vector2D(e.x,e.y))
        }
         
    }
     
    /* ===== MouseMotionListener ===== */
    public void mouseMoved( MouseEvent e ){
        //
        //println "Mouse move!"
    }
    public void mouseDragged( MouseEvent e ){
        //fireMouseEvent( e, "mouseDragged" )
    }
    
    public static void main(String[] args){
        def stad= new Stadium2D();
        def swing = new SwingBuilder();
        def testBedComp
        swing.build {
            jFrame = frame( title: 'Football Testbed', size: [600,450],
                locationRelativeTo: null, show: true ,defaultCloseOperation: JFrame.EXIT_ON_CLOSE){

                borderLayout()

                testBedComp=panel(constraints:BL.CENTER, new GraphicsPanel(),preferredSize:[440,300])
                //testBedComp.addMouseWheelListener(new ZoomOnMouse(this))
                panel(constraints:BL.SOUTH){
                    label(text:"Debug")
                    button(text:"Team",actionPerformed:{stad.debugTeam = !stad.debugTeam})
                    button(text:"Players",actionPerformed:{stad.debugPlayers = !stad.debugPlayers})
                    button(text:"Pause",actionPerformed:{stad.paused = !stad.paused})
                    button(text:"Stop",actionPerformed:{stad.stoped = !stad.stoped})
                }
            }
                
        }
    
        testBedComp.go = stad.getDrawOp(testBedComp)
        stad.startGame();
    }
}

