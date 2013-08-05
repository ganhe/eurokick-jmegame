package football.geom
import  football.geom.Utils
import static football.geom.Utils.*
/**
 *  Desc:   2D vector struct
 */

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class Vector2D{
    double x;
    double y;

    public Vector2D(){
        x=0.0;
        y=0.0;
    }
    public Vector2D(double a, double b){
        x=a;
        y=b;
    }

    //sets x and y to zero
    public void Zero(){
        x=0.0; 
        y=0.0;}

    //returns true if both x and y are zero
    boolean isZero(){
        return (x*x + y*y) < Utils.MinDouble;
    }
    
    //------------------------------------------------------------------------operator overloads
    public Vector2D multiply(Vector2D val){
        Vector2D result =  new Vector2D(this.x,this.y);
        result.x *= val.x;
        result.y *=  val.y;
        return result;
    }
    
    public Vector2D multiply(double val){
        Vector2D result =  new Vector2D(this.x,this.y);
        result.x *= val;
        result.y *= val;
        return result;
    }

    //overload the - operator
    public Vector2D minus( Vector2D rhs){
        Vector2D result = new Vector2D(this.x,this.y);
        result.x -= rhs.x;
        result.y -= rhs.y;
  
        return result;
    }

    //overload the + operator
    public Vector2D plus(Vector2D rhs){
        Vector2D result = new Vector2D(this.x,this.y);
        result.x += rhs.x;
        result.y += rhs.y;
  
        return result;
    }

    //overload the / operator
    public Vector2D divide(double val){
        Vector2D result = new Vector2D(this.x,this.y);
        result.x /= val;
        result.y /= val;

        return result;
    }
    public Vector2D div(Vector2D val){
        Vector2D result = new Vector2D(this.x,this.y);
        result.x /= val.x;
        result.y /= val.y;

        return result;
    }
    public Vector2D div(double val){
        Vector2D result = new Vector2D(this.x,this.y);
        result.x /= val;
        result.y /= val;

        return result;
    }
    //------------------------------------------------------
    //we need some overloaded operators
    public Vector2D plusLocal(Vector2D rhs){
        x += rhs.x;
        y += rhs.y;

        return this;
    }

    public Vector2D minusLocal(Vector2D rhs){
        x -= rhs.x;
        y -= rhs.y;

        return this;
    }

    public Vector2D cloneVec(){
        return new Vector2D(x,y);
    }
    public void cloneVec(Vector2D org){
        x = org.x;
        y = org.y;
    }
    public Vector2D multiplyLocal(double rhs){
        x *= rhs;
        y *= rhs;

        return this;
    }

    public Vector2D divideLocal(double rhs){
        x /= rhs;
        y /= rhs;

        return this;
    }

    public boolean equals( Vector2D rhs){
        return (Utils.isNumberEqual(x, rhs.x) && Utils.isNumberEqual(y,rhs.y) );
    }
    

    //------------------------- Length ---------------------------------------
    //
    //  returns the length of a 2D vector
    //------------------------------------------------------------------------
    public double length(){
        return Math.sqrt(x * x + y * y);
    }


    //------------------------- LengthSq -------------------------------------
    //
    //  returns the squared length of a 2D vector
    //------------------------------------------------------------------------
    public double LengthSq(){
        return (x * x + y * y);
    }


    //------------------------- Vec2DDot -------------------------------------
    //
    //  calculates the dot product
    //------------------------------------------------------------------------
    public double dot(Vector2D v2){
        return x*v2.x + y*v2.y;
    }



    //------------------------------ Perp ------------------------------------
    //
    //  Returns a vector perpendicular to this vector
    //------------------------------------------------------------------------
    public Vector2D perp(){
        return new Vector2D(-y, x);
    }

    //------------------------------ Distance --------------------------------
    //
    //  calculates the euclidean distance between two vectors
    //------------------------------------------------------------------------
    public double Distance( Vector2D v2){
        double ySeparation = v2.y - y;
        double xSeparation = v2.x - x;

        return Math.sqrt(ySeparation*ySeparation + xSeparation*xSeparation);
    }
    //------------------------ Sign ------------------------------------------
    //
    //  returns positive if v2 is clockwise of this vector,
    //  minus if anticlockwise (Y axis pointing down, X axis to right)
    //------------------------------------------------------------------------
    public static enum SignType{clockwise(1), anticlockwise(-1);
        public int value;
        SignType(int value){
            this.value = value;
        }
    };

    public int Sign( Vector2D v2){
        if (y*v2.x > x*v2.y){ 
            return SignType.anticlockwise.value;
        }
        else{
            return SignType.clockwise.value;
        }
    }

    //------------------------------ DistanceSq ------------------------------
    //
    //  calculates the euclidean distance squared between two vectors 
    //------------------------------------------------------------------------
    public double DistanceSq( Vector2D v2){
        double ySeparation = v2.y - y;
        double xSeparation = v2.x - x;

        return ySeparation*ySeparation + xSeparation*xSeparation;
    }

    //----------------------------- Truncate ---------------------------------
    //
    //  truncates a vector so that its length does not exceed max
    //------------------------------------------------------------------------
    public void Truncate(double max){
        if (this.length() > max){
            this.Normalize();
            this.multiplyLocal(max);
        } 
    }
    
    //------------------------- Normalize ------------------------------------
    //
    //  normalizes a 2D Vector
    //------------------------------------------------------------------------
    public void Normalize(){ 
        double vector_length = this.length();

        if (vector_length > 0){
            this.x /= vector_length;
            this.y /= vector_length;
        }
    }
    
    //--------------------------- Reflect ------------------------------------
    //
    //  given a normalized vector this method reflects the vector it
    //  is operating upon. (like the path of a ball bouncing off a wall)
    //------------------------------------------------------------------------
    public void Reflect( Vector2D norm){
        this.plus(norm.GetReverse() *  this.dot(norm) *2.0);
    }

    //----------------------- GetReverse ----------------------------------------
    //
    //  returns the vector that is the reverse of this vector
    //------------------------------------------------------------------------
    public Vector2D GetReverse(){
        return new Vector2D(-this.x, -this.y);
    }
    
    //------------------------------------------------------------------------non member functions

    public static Vector2D Vec2DNormalize(Vector2D v){
        Vector2D vec = new Vector2D(v.x,v.y);
        double vector_length = vec.length();

        if (vector_length > 0){
            vec.x = vec.x/vector_length;
            vec.y = vec.y/vector_length;
        }
        return vec;
    }


    public static double vec2DDistance( Vector2D v1,  Vector2D v2){

        double ySeparation = v2.y - v1.y;
        double xSeparation = v2.x - v1.x;

        return Math.sqrt(ySeparation*ySeparation + xSeparation*xSeparation);
    }

    public static double vec2DDistanceSq( Vector2D v1,  Vector2D v2){

        double ySeparation = v2.y - v1.y;
        double xSeparation = v2.x - v1.x;

        return ySeparation*ySeparation + xSeparation*xSeparation;
    }

    public static double vec2DLength( Vector2D v){
        return Math.sqrt(v.x*v.x + v.y*v.y);
    }

    public static double vec2DLengthSq( Vector2D v){
        return (v.x*v.x + v.y*v.y);
    }

    /*
    // Convertion functions
    public static Vector2D POINTStoVector( def p){
    return new Vector2D(p.x, p.y);
    }

    public static Vector2D POINTtoVector( def p){
    return Vector2D((double)p.x, (double)p.y);
    }

    public static def VectorToPOINTS( Vector2D v){
    def p=[:];
    p.x = (short)v.x;
    p.y = (short)v.y;

    return p;
    }

    public static def VectorToPOINT( Vector2D v){
    def p=[:];
    p.x = (long)v.x;
    p.y = (long)v.y;

    return p;
    }
     */
    ///////////////////////////////////////////////////////////////////////////////


    //treats a window as a toroid
    public static void WrapAround(Vector2D pos, int MaxX, int MaxY){
        if (pos.x > MaxX){pos.x = 0.0;}

        if (pos.x < 0){pos.x = (double)MaxX;}

        if (pos.y < 0){pos.y = (double)MaxY;}

        if (pos.y > MaxY){pos.y = 0.0;}
    }

    //returns true if the point p is not inside the region defined by top_left
    //and bot_rgt
    public static boolean NotInsideRegion(Vector2D p,
        Vector2D top_left,
        Vector2D bot_rgt){
        return (p.x < top_left.x) || (p.x > bot_rgt.x) || 
        (p.y < top_left.y) || (p.y > bot_rgt.y);
    }

    public static boolean InsideRegion(Vector2D p,
        Vector2D top_left,
        Vector2D bot_rgt){
        return !((p.x < top_left.x) || (p.x > bot_rgt.x) || 
            (p.y < top_left.y) || (p.y > bot_rgt.y));
    }

    public static boolean InsideRegion(Vector2D p, int left, int top, int right, int bottom){
        return !( (p.x < left) || (p.x > right) || (p.y < top) || (p.y > bottom) );
    }

    //------------------ isSecondInFOVOfFirst -------------------------------------
    //
    //  returns true if the target position is in the field of view of the entity
    //  positioned at posFirst facing in facingFirst
    //-----------------------------------------------------------------------------
    public static boolean isSecondInFOVOfFirst(Vector2D posFirst,
        Vector2D facingFirst,
        Vector2D posSecond,
        double    fov){
        Vector2D toTarget = Vec2DNormalize(posSecond - posFirst);

        return facingFirst.dot(toTarget) >= Math.cos(fov/2.0);
    }
      
    public String toString(){
        return " x: "+x+" y: "+y;
    }
    @CompileDynamic
    public static void expandoClass(){
        Double.metaClass.multiply={Vector2D v->
            return v.multiply(delegate)
        }
    }
    
    public static Vector2D multiply(Double d,Vector2D v){
        return v.multiply(d)
    }
}
