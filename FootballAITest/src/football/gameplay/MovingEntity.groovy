package football.gameplay

import football.geom.Vector2D
import football.geom.C2DMatrix
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Utils.*
import static football.geom.Transformations.*

import groovy.transform.*

@CompileStatic
public class MovingEntity extends BaseGameEntity{
    protected Vector2D velocity = new Vector2D();
 
    //a normalized vector pointing in the direction the entity is heading. 
    protected Vector2D heading = new Vector2D();

    //a vector perpendicular to the heading vector
    protected Vector2D side = new Vector2D(); 

    protected double mass = 0;
 
    //the maximum speed this entity may travel at.
    protected double maxSpeed = 0;

    //the maximum force this entity can produce to power itself 
    //(think rockets and thrust)
    protected double maxForce = 0;
 
    //the maximum rate (radians per second)this vehicle can rotate  
    protected double maxTurnRate = 0;

    public MovingEntity(Vector2D position,
        double radius,
        Vector2D velocity,
        double max_speed,
        Vector2D heading,
        double mass,
        Vector2D scale,
        double turn_rate,
        double max_force){
 
        super(BaseGameEntity.getNextValidgetID());
        this.heading=heading;
        this.velocity=velocity;
        this.mass=mass;
        this.side=heading.perp();
        this.maxSpeed=max_speed;
        this.maxTurnRate=turn_rate;
        this.maxForce=max_force;
 
        this.position = position;
        this.boundingRadius = radius;
        this.scale = scale;
    }

    //accessors
    public Vector2D getVelocity(){
        return velocity;
    }
    public void SetVelocity( Vector2D NewVel){
        velocity = NewVel;
    }
 
    public double Mass(){
        return mass;
    }
 
    public Vector2D Side(){
        return side;
    }

    public double MaxSpeed(){
        return maxSpeed;
    }   
    public void SetMaxSpeed(double new_speed){
        maxSpeed = new_speed;
    }
 
    public double MaxForce(){
        return maxForce;
    }
    public void SetMaxForce(double mf){
        maxForce = mf;
    }

    public boolean IsSpeedMaxedOut(){
        return maxSpeed*maxSpeed >= velocity.LengthSq();
    }
    public double Speed(){
        return velocity.length();
    }
    public double SpeedSq(){
        return velocity.LengthSq();
    }
 
    public Vector2D Heading(){
        return heading;
    }

    public double MaxTurnRate(){
        return maxTurnRate;
    }
    public void SetMaxTurnRate(double val){
        maxTurnRate = val;
    }

    //--------------------------- RotateHeadingToFacePosition ---------------------
    //
    // given a target position, this method rotates the entity's heading and
    // side vectors by an amount not greater than maxTurnRate until it
    // directly faces the target.
    //
    // returns true when the heading is facing in the desired direction
    //-----------------------------------------------------------------------------
    boolean rotateHeadingToFacePosition(Vector2D target){
        Vector2D toTarget = Vec2DNormalize(target - position);

        double dot = heading.dot(toTarget);

        //some compilers lose acurracy so the value is clamped to ensure it
        //remains valid for the acos
        Clamp(dot, -1, 1);

        //first determine the angle between the heading vector and the target
        double angle = Math.acos(dot);

        //return true if the player is facing the target
        if (angle < 0.00001) return true;

        //clamp the amount to turn to the max turn rate
        if (angle > maxTurnRate) angle = maxTurnRate;
 
        //The next few lines use a rotation matrix to rotate the player's heading
        //vector accordingly
        C2DMatrix rotationMatrix = new C2DMatrix();
 
        //notice how the direction of rotation has to be determined when creating
        //the rotation matrix
        rotationMatrix.Rotate(heading.Sign(toTarget) * angle);	
        rotationMatrix.transformVector2Ds(heading);
        rotationMatrix.transformVector2Ds(velocity);

        //finally recreate side
        side = heading.perp();

        return false;
    }


    //------------------------- SetHeading ----------------------------------------
    //
    // first checks that the given heading is not a vector of zero length. If the
    // new heading is valid this fumction sets the entity's heading and side 
    // vectors accordingly
    //-----------------------------------------------------------------------------
    void SetHeading(Vector2D new_heading){
        assert( (new_heading.LengthSq() - 1.0) < 0.00001);
 
        heading.cloneVec(new_heading);

        //the side vector must always be perpendicular to the heading
        side = heading.perp();
    }
 
    public void Render(def g){
 
    }
}