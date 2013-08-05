package football.geom

import groovy.transform.*


/**
 * Matrix functions
 */
@CompileStatic
public class C2DMatrix
{
 
    public class Matrix
    {

        double _11, _12, _13;
        double _21, _22, _23;
        double _31, _32, _33;

        Matrix()
        {
            _11=0.0; _12=0.0; _13=0.0;
            _21=0.0; _22=0.0; _23=0.0;
            _31=0.0; _32=0.0; _33=0.0;
        }

    };

    Matrix Matrix = new Matrix();

    public C2DMatrix()
    {
        //initialize the matrix to an identity matrix
        Identity();
    }

    //accessors to the matrix elements
    public void _11(double val){Matrix._11 = val;}
    public void _12(double val){Matrix._12 = val;}
    public void _13(double val){Matrix._13 = val;}

    public void _21(double val){Matrix._21 = val;}
    public void _22(double val){Matrix._22 = val;}
    public void _23(double val){Matrix._23 = val;}

    public void _31(double val){Matrix._31 = val;}
    public void _32(double val){Matrix._32 = val;}
    public void _33(double val){Matrix._33 = val;}

    //multiply two matrices together
    public void MatrixMultiply(Matrix mIn)
    {
        Matrix mat_temp = new Matrix();
  
        //first row
        mat_temp._11 = (Matrix._11*mIn._11) + (Matrix._12*mIn._21) + (Matrix._13*mIn._31);
        mat_temp._12 = (Matrix._11*mIn._12) + (Matrix._12*mIn._22) + (Matrix._13*mIn._32);
        mat_temp._13 = (Matrix._11*mIn._13) + (Matrix._12*mIn._23) + (Matrix._13*mIn._33);

        //second
        mat_temp._21 = (Matrix._21*mIn._11) + (Matrix._22*mIn._21) + (Matrix._23*mIn._31);
        mat_temp._22 = (Matrix._21*mIn._12) + (Matrix._22*mIn._22) + (Matrix._23*mIn._32);
        mat_temp._23 = (Matrix._21*mIn._13) + (Matrix._22*mIn._23) + (Matrix._23*mIn._33);

        //third
        mat_temp._31 = (Matrix._31*mIn._11) + (Matrix._32*mIn._21) + (Matrix._33*mIn._31);
        mat_temp._32 = (Matrix._31*mIn._12) + (Matrix._32*mIn._22) + (Matrix._33*mIn._32);
        mat_temp._33 = (Matrix._31*mIn._13) + (Matrix._32*mIn._23) + (Matrix._33*mIn._33);

        Matrix = mat_temp;
    }

    //applies a 2D transformation matrix to a std::vector of Vector2Ds
    public void transformVector2Ds(List<Vector2D> vPoint)
    {
        vPoint.each(){Vector2D point->
            transformVector2Ds(point)  
        }
    }

    //applies a 2D transformation matrix to a single Vector2D
    public void transformVector2Ds(Vector2D vPoint)
    {

        double tempX =(Matrix._11*vPoint.x) + (Matrix._21*vPoint.y) + (Matrix._31);

        double tempY = (Matrix._12*vPoint.x) + (Matrix._22*vPoint.y) + (Matrix._32);
  
        vPoint.x = tempX;

        vPoint.y = tempY;
    }



    //create an identity matrix
    public void Identity()
    {
        Matrix._11 = 1; Matrix._12 = 0; Matrix._13 = 0;

        Matrix._21 = 0; Matrix._22 = 1; Matrix._23 = 0;

        Matrix._31 = 0; Matrix._32 = 0; Matrix._33 = 1;

    }

    //create a transformation matrix
    public void Translate(double x, double y)
    {
        Matrix mat = new Matrix();
  
        mat._11 = 1; mat._12 = 0; mat._13 = 0;
  
        mat._21 = 0; mat._22 = 1; mat._23 = 0;
  
        mat._31 = x;    mat._32 = y;    mat._33 = 1;
  
        //and multiply
        MatrixMultiply(mat);
    }

    //create a scale matrix
    public void Scale(double xScale, double yScale)
    {
        Matrix mat = new Matrix();
  
        mat._11 = xScale; mat._12 = 0; mat._13 = 0;
  
        mat._21 = 0; mat._22 = yScale; mat._23 = 0;
  
        mat._31 = 0; mat._32 = 0; mat._33 = 1;
  
        //and multiply
        MatrixMultiply(mat);
    }


    //create a rotation matrix
    public void Rotate(double rot)
    {
        Matrix mat = new Matrix();

        double Sin = Math.sin(rot);
        double Cos = Math.cos(rot);
  
        mat._11 = Cos;  mat._12 = Sin; mat._13 = 0;
  
        mat._21 = -Sin; mat._22 = Cos; mat._23 = 0;
  
        mat._31 = 0; mat._32 = 0;mat._33 = 1;
  
        //and multiply
        MatrixMultiply(mat);
    }


    //create a rotation matrix from a 2D vector
    public void Rotate( Vector2D fwd,  Vector2D side)
    {
        Matrix mat = new Matrix();
  
        mat._11 = fwd.x;  mat._12 = fwd.y; mat._13 = 0;
  
        mat._21 = side.x; mat._22 = side.y; mat._23 = 0;
  
        mat._31 = 0; mat._32 = 0;mat._33 = 1;
  
        //and multiply
        MatrixMultiply(mat);
    }
}
