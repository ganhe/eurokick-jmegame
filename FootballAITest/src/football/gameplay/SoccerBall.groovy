package football.gameplay

import com.jme3.math.*
import football.geom.Wall2D
import football.geom.Vector2D
import football.gameplay.ai.info.*
import static football.geom.Vector2D.*
import static football.geom.Geometry2DFunctions.*
import static football.geom.Geometry2DFunctions.SpanType.*
import static football.geom.Transformations.*
import football.geom.Utils
import static football.geom.Utils.*

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class SoccerBall extends MovingEntity{

    //keeps a record of the ball's position at the last update
    Vector2D oldPos;

    //a local reference to the Walls that make up the pitch boundary
    List<Wall2D> pitchBoundary; 

    GraphicsBuilder gdi = null;
    
    public SoccerBall(Vector2D pos, 
        double BallSize,
        double mass,
        ArrayList<Wall2D> pitchBoundary){
 
        //set up the base class
        super(pos,
            BallSize,
            new Vector2D(0d,0d),
            -1.0, //max speed - unused
            new Vector2D(0d,1d),
            mass,
            new Vector2D(1d,1d), //scale - unused
            0, //turn rate - unused
            0); //max force - unused
            
        this.pitchBoundary= pitchBoundary;
        oldPos = new Vector2D(0d,0d);
    }
 
    //----------------------------- AddNoiseToKick --------------------------------
    //
    // this can be used to vary the accuracy of a player's kick. Just call it 
    // prior to kicking the ball using the ball's position and the ball target as
    // parameters.
    //-----------------------------------------------------------------------------
    public static Vector2D AddNoiseToKick(Vector2D BallPos, Vector2D BallTarget){
        double Pi =Utils.Pi;
        
        double displacement = (Pi - Pi*Params.Instance().PlayerKickingAccuracy) * RandomClamped();

        Vector2D toTarget = BallTarget - BallPos;

        vec2DRotateAroundOrigin(toTarget, displacement);

        return toTarget + BallPos;
    }

 

    //-------------------------- Kick ----------------------------------------
    //   
    // applys a force to the ball in the direction of heading. Truncates
    // the new velocity to make sure it doesn't exceed the max allowable.
    //------------------------------------------------------------------------
    public void kick(Vector2D direction, double force){ 
        //ensure direction is normalized
        Vector2D d = direction.cloneVec();
        d.Normalize();
 
        //calculate the acceleration
        Vector2D acceleration = (d * force) / mass;

        //update the velocity
        velocity.cloneVec(acceleration);
    }

    //----------------------------- Update -----------------------------------
    //
    // updates the ball physics, tests for any collisions and adjusts
    // the ball's velocity accordingly
    //------------------------------------------------------------------------
    public void update(){
        //keep a record of the old position so the goal::scored method
        //can utilize it for goal testing
        if (position != null){
            oldPos.cloneVec(position);
        }
        //Test for collisions
        TestCollisionWithWalls(pitchBoundary);

        //Simulate Params.Instance().Friction. Make sure the speed is positive 
        //first though
        double friction = Params.Instance().Friction;
        //println (velocity)
        //println("LengthSq" + velocity.LengthSq() +" friction " + friction)
        if (velocity.LengthSq() > friction * friction){
            velocity = velocity + Vec2DNormalize(velocity) * friction;
            //println (velocity)
            position = position + velocity;
            //println (position)
            //update heading
            heading = Vec2DNormalize(velocity);
        } 
        
        
    }

    //---------------------- TimeToCoverDistance -----------------------------
    //
    // Given a force and a distance to cover given by two vectors, this
    // method calculates how long it will take the ball to travel between
    // the two points
    //------------------------------------------------------------------------
    public double TimeToCoverDistance(Vector2D A, Vector2D B, double force){
        //this will be the velocity of the ball in the next time step *if*
        //the player was to make the pass. 
        double speed = force / mass;

        //calculate the velocity at B using the equation
        //
        // v^2 = u^2 + 2as
        //

        //first calculate s (the distance between the two positions)
        double DistanceToCover = vec2DDistance(A, B);

        double term = speed*speed + 2.0*DistanceToCover*Params.Instance().Friction;

        //if (u^2 + 2as) is negative it means the ball cannot reach point B.
        if (term <= 0.0) return -1.0;

        double v = Math.sqrt(term);

        //it IS possible for the ball to reach B and we know its speed when it
        //gets there, so now it's easy to calculate the time using the equation
        //
        // t = v-u
        // ---
        // a
        //
        return (v-speed)/Params.Instance().Friction;
    }

    //--------------------- FuturePosition -----------------------------------
    //
    // given a time this method returns the ball position at that time in the
    // future
    //------------------------------------------------------------------------
    public Vector2D FuturePosition(double time){
        //using the equation s = ut + 1/2at^2, where s = distance, a = friction
        //u=start velocity

        //calculate the ut term, which is a vector
        Vector2D ut = velocity * time;

        //calculate the 1/2at^2 term, which is scalar
        double half_a_t_squared = 0.5 * Params.Instance().Friction * time * time;

        //turn the scalar quantity into a vector by multiplying the value with
        //the normalized velocity vector (because that gives the direction)
        Vector2D ScalarToVector = Vec2DNormalize(velocity) * half_a_t_squared;

        //the predicted position is the balls position plus these two terms
        return getPos() + ut + ScalarToVector;
    }


    //----------------------------- Render -----------------------------------
    //
    // Renders the ball
    //------------------------------------------------------------------------
    @CompileDynamic
    public void Render(GraphicsBuilder gdi){
        if (this.gdi!=gdi){
            this.gdi = gdi
        
            gdi.group{

                circle(id:"ball",
                    cx:position.x,
                    cy:position.y,
                    radius:boundingRadius,
                    borderColor:"black",fill:"black");
                //println("ball "+ position.x + " "+position.y)
                /*
                for (int i=0; i<IPPoints.size(); ++i){
                circle(cx:IPPoints[i].x,
                cy:IPPoints[i].y,
                radius:3,
                borderColor:"Green");
                }
                 */
            
            }
        } else {
            // update only
            gdi."ball".cx = position.x
            gdi."ball".cy = position.y
            //println("ball pos : " +position.getClass())
            //println("ball "+ position.x + " "+position.y)
        }
    }


    //----------------------- TestCollisionWithWalls -------------------------
    //
    public void TestCollisionWithWalls(List<Wall2D> walls){ 
        //test ball against each wall, find out which is closest
        int idxClosest = -1;
        
        //println ("Colision " +velocity)
        Vector2D VelNormal = Vec2DNormalize(velocity);

        Vector2D IntersectionPoint= new Vector2D();
        Vector2D CollisionPoint= new Vector2D();

        double DistToIntersection = Double.MAX_VALUE;

        //iterate through each wall and calculate if the ball intersects.
        //If it does then store the index into the closest intersecting wall
        for (int w=0; w<walls.size(); ++w){
            //assuming a collision if the ball continued on its current heading 
            //calculate the point on the ball that would hit the wall. This is 
            //simply the wall's normal(inversed) multiplied by the ball's radius
            //and added to the balls center (its position)
            Vector2D ThisCollisionPoint = getPos() - (walls[w].Normal() * getBRadius());

            //calculate exactly where the collision point will hit the plane 
            if (WhereIsPoint(ThisCollisionPoint,
                    walls[w].From(),
                    walls[w].Normal()) == plane_backside){
                double DistToWall = DistanceToRayPlaneIntersection(ThisCollisionPoint,
                    walls[w].Normal(),
                    walls[w].From(),
                    walls[w].Normal());

                IntersectionPoint = ThisCollisionPoint + (walls[w].Normal() * DistToWall);
 
            }

            else{
                double DistToWall = DistanceToRayPlaneIntersection(ThisCollisionPoint,
                    VelNormal,
                    walls[w].From(),
                    walls[w].Normal());

                IntersectionPoint = ThisCollisionPoint + (VelNormal * DistToWall);
            }
 
            //check to make sure the intersection point is actually on the line
            //segment
            boolean OnLineSegment = false;

            if (LineIntersection2D(walls[w].From(), 
                    walls[w].To(),
                    ThisCollisionPoint - walls[w].Normal()*20.0,
                    ThisCollisionPoint + walls[w].Normal()*20.0)){

                OnLineSegment = true;  
            }

 
            //Note, there is no test for collision with the end of a line segment
 
            //now check to see if the collision point is within range of the
            //velocity vector. [work in distance squared to avoid Math.sqrt] and if it
            //is the closest hit found so far. 
            //If it is that means the ball will collide with the wall sometime
            //between this time step and the next one.
            double distSq = vec2DDistanceSq(ThisCollisionPoint, IntersectionPoint);

            if ((distSq <= velocity.LengthSq()) && (distSq < DistToIntersection) && OnLineSegment){ 
                DistToIntersection = distSq;
                idxClosest = w;
                CollisionPoint = IntersectionPoint;
            } 
        }//next wall

 
        //to prevent having to calculate the exact time of collision we
        //can just check if the velocity is opposite to the wall normal
        //before reflecting it. This prevents the case where there is overshoot
        //and the ball gets reflected back over the line before it has completely
        //reentered the playing area.
        if ( (idxClosest >= 0 ) && VelNormal.dot(walls[idxClosest].Normal()) < 0){
            velocity.Reflect(walls[idxClosest].Normal()); 
        }
    }

    //----------------------- PlaceAtLocation -------------------------------------
    //
    // positions the ball at the desired location and sets the ball's velocity to
    // zero
    //-----------------------------------------------------------------------------
    public void PlaceAtPosition(Vector2D NewPos){
        position = NewPos;

        oldPos.cloneVec(position);
 
        velocity.Zero();
    }
    //this is used by players and goalkeepers to 'trap' a ball -- to stop
    //it dead. That player is then assumed to be in possession of the ball
    //and pOwner is adjusted accordingly
    public void Trap(){
        velocity.Zero();
    } 

    public Vector2D OldgetPos(){
        return oldPos;
    }
}