package football.geom

import static java.lang.Math.*
import groovy.transform.*

@CompileStatic
public class Transformations{

    //--------------------------- WorldTransform -----------------------------
    //
    //  given aList of 2D vectors, a position, orientation and scale,
    //  this function transforms the 2D vectors into the object's world space
    //------------------------------------------------------------------------
    public static List<Vector2D> WorldTransform(List<Vector2D> points,
        Vector2D   pos,
        Vector2D   forward,
        Vector2D   side,
        Vector2D   scale)
    {
	//copy the original vertices into the buffer about to be transformed
        List<Vector2D> TranVector2Ds = points;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();
	
	//scale
        if ( (scale.x != 1.0) || (scale.y != 1.0) )
        {
            matTransform.Scale(scale.x, scale.y);
        }

	//rotate
	matTransform.Rotate(forward, side);

	//and translate
	matTransform.Translate(pos.x, pos.y);
	
        //now transform the object's vertices
        matTransform.transformVector2Ds(TranVector2Ds);

        return TranVector2Ds;
    }

    //--------------------------- WorldTransform -----------------------------
    //
    //  given aList of 2D vectors, a position and  orientation
    //  this function transforms the 2D vectors into the object's world space
    //------------------------------------------------------------------------
    public static List<Vector2D> WorldTransform(List<Vector2D> points,
        Vector2D   pos,
        Vector2D   forward,
        Vector2D   side)
    {
	//copy the original vertices into the buffer about to be transformed
        List<Vector2D> TranVector2Ds = points;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();

	//rotate
	matTransform.Rotate(forward, side);

	//and translate
	matTransform.Translate(pos.x, pos.y);
	
        //now transform the object's vertices
        matTransform.transformVector2Ds(TranVector2Ds);

        return TranVector2Ds;
    }

    //--------------------- PointToWorldSpace --------------------------------
    //
    //  Transforms a point from the agent's local space into world space
    //------------------------------------------------------------------------
    public static Vector2D PointToWorldSpace( Vector2D point,
        Vector2D AgentHeading,
        Vector2D AgentSide,
        Vector2D AgentPosition)
    {
	//make a copy of the point
        Vector2D TransPoint = point;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();

	//rotate
	matTransform.Rotate(AgentHeading, AgentSide);

	//and translate
	matTransform.Translate(AgentPosition.x, AgentPosition.y);
	
        //now transform the vertices
        matTransform.transformVector2Ds(TransPoint);

        return TransPoint;
    }

    //--------------------- VectorToWorldSpace --------------------------------
    //
    //  Transforms a vector from the agent's local space into world space
    //------------------------------------------------------------------------
    public static Vector2D VectorToWorldSpace( Vector2D vec,
        Vector2D AgentHeading,
        Vector2D AgentSide)
    {
	//make a copy of the point
        Vector2D TransVec = vec;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();

	//rotate
	matTransform.Rotate(AgentHeading, AgentSide);

        //now transform the vertices
        matTransform.transformVector2Ds(TransVec);

        return TransVec;
    }


    //--------------------- PointToLocalSpace --------------------------------
    //
    //------------------------------------------------------------------------
    public static Vector2D PointToLocalSpace( Vector2D point,
        Vector2D AgentHeading,
        Vector2D AgentSide,
        Vector2D AgentPosition)
    {

	//make a copy of the point
        Vector2D TransPoint = point;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();

        double Tx = -AgentPosition.dot(AgentHeading);
        double Ty = -AgentPosition.dot(AgentSide);

        //create the transformation matrix
        matTransform._11(AgentHeading.x); matTransform._12(AgentSide.x);
        matTransform._21(AgentHeading.y); matTransform._22(AgentSide.y);
        matTransform._31(Tx);           matTransform._32(Ty);
	
        //now transform the vertices
        matTransform.transformVector2Ds(TransPoint);

        return TransPoint;
    }

    //--------------------- VectorToLocalSpace --------------------------------
    //
    //------------------------------------------------------------------------
    public static Vector2D VectorToLocalSpace( Vector2D vec,
        Vector2D AgentHeading,
        Vector2D AgentSide)
    { 

	//make a copy of the point
        Vector2D TransPoint = vec;
  
        //create a transformation matrix
	C2DMatrix matTransform= new C2DMatrix();

        //create the transformation matrix
        matTransform._11(AgentHeading.x); matTransform._12(AgentSide.x);
        matTransform._21(AgentHeading.y); matTransform._22(AgentSide.y);
	
        //now transform the vertices
        matTransform.transformVector2Ds(TransPoint);

        return TransPoint;
    }

    //-------------------------- Vec2DRotateAroundOrigin --------------------------
    //
    //  rotates a vector ang rads around the origin
    //-----------------------------------------------------------------------------
    public static void vec2DRotateAroundOrigin(Vector2D v, double ang)
    {
        //create a transformation matrix
        C2DMatrix mat= new C2DMatrix();

        //rotate
        mat.Rotate(ang);
	
        //now transform the object's vertices
        mat.transformVector2Ds(v);
    }
    public static void vec2DRotateAroundOrigin(Vector2D v, Double ang)
    {
        //create a transformation matrix
        C2DMatrix mat= new C2DMatrix();

        //rotate
        mat.Rotate(ang.doubleValue());
	
        //now transform the object's vertices
        mat.transformVector2Ds(v);
    }
    //------------------------ CreateWhiskers ------------------------------------
    //
    //  given an origin, a facing direction, a 'field of view' describing the 
    //  limit of the outer whiskers, a whisker length and the number of whiskers
    //  this method returns a vector containing the end positions of a series
    //  of whiskers radiating away from the origin and with equal distance between
    //  them. (like the spokes of a wheel clipped to a specific segment size)
    //----------------------------------------------------------------------------
    public static List<Vector2D> CreateWhiskers( int  NumWhiskers,
        double        WhiskerLength,
        double        fov,
        Vector2D      facing,
        Vector2D      origin)
    {
        //this is the magnitude of the angle separating each whisker
        double SectorSize = fov/(double)(NumWhiskers-1);

        List<Vector2D> whiskers=[];
        Vector2D temp= new Vector2D();
        double angle = -fov*0.5; 

        for ( int w=0; w<NumWhiskers; ++w)
        {
            //create the whisker extending outwards at this angle
            temp = facing;
            vec2DRotateAroundOrigin(temp, angle);
            whiskers.add(origin + temp * WhiskerLength);

            angle+=SectorSize;
        }

        return whiskers;
    }
}
