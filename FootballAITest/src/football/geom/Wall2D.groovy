package football.geom

import static football.geom.Vector2D.*

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class Wall2D {

    Vector2D vA,vB,vN;
    boolean RenderNormals = true;
    public void CalculateNormal()
    {
        Vector2D temp = Vec2DNormalize(vB - vA);

        vN.x = -temp.y;
        vN.y = temp.x;
    }


    public Wall2D(){}

    public Wall2D(Vector2D A,Vector2D B){
        vA=(A);
        vB=(B);
        vN = new Vector2D();
        CalculateNormal();
    }

    public Wall2D(Vector2D A,Vector2D B,Vector2D N){
        vA=(A); 
        vB=(B); 
        vN=(N);
  
    }

    GraphicsBuilder gdi = null;
    
    @CompileDynamic
    public void Render(GraphicsBuilder gdi)
    {
        if (this.gdi!=gdi){
            this.gdi = gdi;
        
            gdi.group{
                line(x1:vA.x,
                    y1:vA.y,
                    x2:vB.x,
                    y2:vB.y,
                    borderColor:"white");

                //render the normals if rqd
                if (RenderNormals)
                {
                    int MidX = (int)((vA.x+vB.x)/2);
                    int MidY = (int)((vA.y+vB.y)/2);

                    line(x1:MidX,
                        y1:MidY,
                        x2:(int)(MidX+(vN.x * 5)),
                        y2:(int)(MidY+(vN.y * 5)),
                        borderColor:"red"
                    );
                }
            }
        }
    }

    public Vector2D From()  {
        return vA;
    }
    public void SetFrom(Vector2D v){
        vA = v; CalculateNormal();
    }

    public Vector2D To()    {
        return vB;
    }
    public void SetTo(Vector2D v){
        vB = v; CalculateNormal();
    }
  
    public Vector2D Normal(){
        return vN;
    }
    public void SetNormal(Vector2D n){
        vN = n;
    }
  
    public Vector2D getCenter(){
        return (vA+vB)/2.0;
    }
}
