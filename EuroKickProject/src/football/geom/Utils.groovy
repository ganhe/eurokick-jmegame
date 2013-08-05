package football.geom

/**
 *
 * @author cuong.nguyenmanh2
 */
import static java.lang.Math.*
import groovy.transform.*

@CompileStatic
public class Utils {
    public static Random RND = new Random();
    
    //a few useful constants
    public static int     MaxInt    = Integer.MAX_VALUE;
    public static double  MaxDouble = Double.MAX_VALUE;
    public static double  MinDouble = Double.MIN_VALUE;
    public static float   MaxFloat  = Float.MAX_VALUE;
    public static float   MinFloat  = Float.MIN_VALUE;

    public static double   Pi        = 3.14159;
    public static double   TwoPi     = Pi * 2;
    public static double   HalfPi    = Pi / 2;
    public static double   QuarterPi = Pi / 4;

    public static Random rand(){
        return RND;
    }
    //returns true if the value is a NaN
    public static <T> boolean  isNaN(T val)    {
        return val != val;
    }

    public static double DegsToRads(double degs){
        return TwoPi * (degs/360.0);
    }



    //returns true if the parameter is equal to zero
    public static boolean IsZero(double val){
        return ((-MinDouble < val) && (val < MinDouble) );
    }

    //returns true is the third parameter is in the range described by the
    //first two
    public static boolean InRange(double start, double end, double val){
        if (start < end)
        {
            if ( (val > start) && (val < end) ) return true;
            else return false;
        }

        else
        {
            if ( (val < start) && (val > end) ) return true;
            else return false;
        }
    }

    @CompileDynamic
    public static <T> T Maximum(T v1, T v2)
    {
        return v1 > v2 ? v1 : v2;
    }



    //----------------------------------------------------------------------------
    //  some random number functions.
    //----------------------------------------------------------------------------

    //returns a random integer between x and y
    public static int   RandInt(int x,int y) {
        return rand().nextInt()+x;
    }

    //returns a random double between zero and 1
    public static double RandFloat()      {
        return rand().nextFloat();
    }

    public static double RandInRange(double x, double y)
    {
        return x + RandFloat()*(y-x);
    }

    //returns a random boolean
    public static boolean RandBool()
    {
        /*
        if (RandInt(0,1)) return true;

        else return false;
         */
        return rand().nextBoolean();
    }

    //returns a random double in the range -1 < n < 1
    public static double RandomClamped()    {
        return RandFloat() - RandFloat();
    }


    //returns a random number with a normal distribution. See method at
    //http://www.taygeta.com/random/gaussian.html
    public static double RandGaussian(double mean = 0.0, double standard_deviation = 1.0)
    {				        
	double x1, x2, w, y1;
	double y2;
	int use_last = 0;

	if (use_last)		        /* use value from previous call */
	{
            y1 = y2;
            use_last = 0;
	}
	else {
             
            while ( w >= 1.0 ) {
                x1 = 2.0 * RandFloat() - 1.0;
                x2 = 2.0 * RandFloat() - 1.0;
                w = x1 * x1 + x2 * x2;
            } 
            

            w = sqrt( (-2.0 * log( w ) ) / w );
            y1 = x1 * w;
            y2 = x2 * w;
            use_last = 1;
	}

	return( mean + y1 * standard_deviation );
    }



    //-----------------------------------------------------------------------
    //  
    //  some handy little functions
    //-----------------------------------------------------------------------


    public static double Sigmoid(double input, double response = 1.0)
    {
	return ( 1.0 / ( 1.0 + exp(-input / response)));
    }


    //returns the maximum of two values
    @CompileDynamic
    public static <T> T MaxOf(T a, T b){
        if (a>b) {
            return a;        
        } else {
            return b;
        }
    }

    //returns the minimum of two values
    @CompileDynamic
    public static <T> T MinOf(T a, T b)
    {
        if (a<b) {
            return a;        
        } else {
            return b;
        }
    }


    //clamps the first argument between the second two
    @CompileDynamic
    public static <T, U, V> void Clamp(T arg, U minVal, V maxVal)
    {

        if (arg < minVal)
        {
            arg = minVal;
        }

        if (arg > maxVal)
        {
            arg = maxVal;
        }
    }


    //rounds a double up or down depending on its value
    public static int Rounded(double val)
    {
        int    integral = (int)val;
        double mantissa = val - integral;

        if (mantissa < 0.5)
        {
            return integral;
        }

        else
        {
            return integral + 1;
        }
    }

    //rounds a double up or down depending on whether its 
    //mantissa is higher or lower than offset
    public static int RoundUnderOffset(double val, double offset)
    {
        int    integral = (int)val;
        double mantissa = val - integral;

        if (mantissa < offset)
        {
            return integral;
        }

        else
        {
            return integral + 1;
        }
    }

    //compares two real numbers. Returns true if they are equal
    public static boolean isNumberEqual(float a, float b)
    {
        if (Math.abs(a-b) < 1E-12)
        {
            return true;
        }

        return false;
    }

    public static boolean isNumberEqual(double a, double b)
    {
        if (Math.abs(a-b) < 1E-12)
        {
            return true;
        }

        return false;
    }


    public static <T> double Average(List<T> v)
    {
        double average = 0.0;
  
        for (int i=0; i < v.size(); ++i)
        {    
            average += (double)v[i];
        }

        return average / (double)v.size();
    }


    public static double StandardDeviation(List<Double> v)
    {
        double sd      = 0.0;
        double average = Average(v);

        for (int i=0; i<v.size(); ++i)
        {     
            sd += (v[i] - average) * (v[i] - average);
        }

        sd = sd / v.size();

        return sqrt(sd);
    }

    public static long timeGetTime(){
        return System.currentTimeMillis();
    }    
    
    /*
    <container> void DeleteSTLContainer(container c)
    {
    for (container::iterator it = c.begin(); it!=c.end(); ++it)
    {
    delete *it;
     *it = NULL;
    }
    }

    template <class map>
    void DeleteSTLMap(map& m)
    {
    for (map::iterator it = m.begin(); it!=m.end(); ++it)
    {
    delete it->second;
    it->second = NULL;
    }
    }
     */

}

