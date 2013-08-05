package football.gameplay

import java.awt.Color


import static football.geom.Utils.*
import football.geom.Vector2D

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
public class Region{

    public static enum region_modifier{halfsize, normal};
    public static enum RegionDirection{North(0,-1),NorthEast(1,-1),East(1,0),SouthEast(1,1),South(0,1),SouthWest(-1,1),West(-1,0),NorthWest(0,0);
        float x;
        float y;
        RegionDirection(float x,float y){
            this.x = x;
            this.y = y;
        }
    }
    double top;
    double left;
    double right;
    double bottom;

    double width;
    double height;

    Vector2D center;
 
    int iID;

    public Region(){
        this.top=0;
        this.bottom=0;
        this.left=0;
        this.right=0;
    }

    public Region(double left,
        double top,
        double right,
        double bottom,
        int id = -1){
        this.top=top;
        this.right=right;
        this.left=left;
        this.bottom=bottom;
        iID=id;
 
        calculate();
    }

    public void calculate(){
        //calculate center of region
        center = new Vector2D( (left+right)*0.5, (top+bottom)*0.5 );
        width = Math.abs(right-left);
        height = Math.abs(bottom-top);
    }
    //-------------------------------
    public double getTop(){
        return top;
    }
    public double getBottom(){
        return bottom;
    }
    double getLeft(){
        return left;
    }
    public double getRight(){
        return right;
    }
    public double getWidth(){
        return Math.abs(right - left);
    }
    public double getHeight(){
        return Math.abs(top - bottom);
    }
    public double length(){
        return Math.max(getWidth(), getHeight());
    }
    public double getBreadth(){
        return Math.min(getWidth(), getHeight());
    }
    public Vector2D getCenter(){
        return center;
    }
    public int getID(){
        return iID;
    }

    public Vector2D getPosDirection(RegionDirection dir){
        switch (dir){
        case RegionDirection.North : 
            return new Vector2D((right+left)/2,top);
            break;

        }
    }
    //returns a vector representing a random location
    //within the region
    public Vector2D getRandomPosition(){
        return new Vector2D(RandInRange(left, right),
            RandInRange(top, bottom));
    }

    //returns true if the given position lays inside the region. The
    //region modifier can be used to contract the region bounderies
    public boolean isInside(Vector2D pos, region_modifier r=region_modifier.normal){
        if (r == region_modifier.normal){
            return ((pos.x > left) && (pos.x < right) &&
                (pos.y > top) && (pos.y < bottom));
        }
        else{
            double marginX = width * 0.25;
            double marginY = height * 0.25;

            return ((pos.x > (left+marginX)) && (pos.x < (right-marginX)) &&
                (pos.y > (top+marginY)) && (pos.y < (bottom-marginY)));
        }

    }
    /*
    GraphicsBuilder gdi=null;
    @CompileDynamic
    public void Render(GraphicsBuilder gdi,boolean ShowID){
    if (this.gdi!=gdi){
    this.gdi = gdi
    gdi.group(){
    rect(x:left, 
    y:top, 
    width:right-left, 
    height:bottom-top,
    borderColor:"white");

    if (ShowID){ 
    text(x:getCenter().x,
    y:getCenter().y, 
    text:getID().toString(),
    fill:"green",
    borderColor:"green");
    }
    }
    }
    }
     */
}