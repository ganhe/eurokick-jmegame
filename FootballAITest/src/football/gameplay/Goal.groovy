package football.gameplay

import football.geom.Vector2D
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class Goal{
    Vector2D   leftPost;
    Vector2D   rightPost;
    //a vector representing the facing direction of the goal
    Vector2D   facing;
    //the position of the center of the goal line
    Vector2D   center;
    //each time checkScored() detects a goal this is incremented
    int        numGoalsScored;
    
    public Goal(Vector2D left, Vector2D right, Vector2D facing){
        this.leftPost=left
        this.rightPost=right
        this.center=(left+right)/2.0
        this.numGoalsScored=(0)
        this.facing=facing;
    }

    //-----------------------------------------------------accessor methods
    public Vector2D getCenter(){
        return center;
    }
    public Vector2D getFacing(){
        return facing;
    }
    public Vector2D getLeftPost(){
        return leftPost;
    }
    public Vector2D getRightPost(){
        return rightPost;
    }

    public int      getNumGoalscheckScored(){
        return numGoalsScored;
    }
    public void     resetGoalscheckScored(){
        numGoalsScored = 0;
    }

    /////////////////////////////////////////////////////////////////////////
    //Given the current ball position and the previous ball position,
    //this method returns true if the ball has crossed the goal line 
    //and increments numGoalsScored
    public boolean checkScored( SoccerBall ball){
        if (LineIntersection2D(ball.getPos(), ball.OldgetPos(), leftPost, rightPost)){
            ++numGoalsScored;

            return true;
        }

        return false;
    }
}