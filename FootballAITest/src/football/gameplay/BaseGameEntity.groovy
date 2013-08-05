package football.gameplay

import football.geom.Vector2D
import football.gameplay.ai.event.Telegram
import static java.lang.Math.*
import static football.geom.Geometry2DFunctions.*

import groovy.transform.*

@CompileStatic
public abstract class BaseGameEntity{
    public static int default_entity_type = -1;
 
    //each entity has a unique ID
    public int ID;

    //every entity has a type associated with it (health, troll, ammo etc)
    public int type;

    //this is a generic flag. 
    public boolean tag;

    //this is the next valid ID. Each time a BaseGameEntity is instantiated
    //this value is updated
    public static int nextValidID=0;

    //its location in the environment
    protected Vector2D position;

    protected Vector2D scale;

    //the magnitude of this object's bounding radius
    protected double boundingRadius;


    public BaseGameEntity(int ID){
        boundingRadius=0.0;
        scale=new Vector2D(1.0d,1.0d);
        position=new Vector2D(0d,0d);
        type=default_entity_type;
        tag=false;
        setID(ID);
    }

    public void update(){}; 

    public abstract void Render(def g);
 
    public boolean handleMessage(Telegram msg){
        return false;
    }
 
    //use this to grab the next valid ID
    public static int getNextValidgetID(){
        return nextValidID;
    }
 
    //this can be used to reset the next ID
    public static void resetNextValidgetID(){
        nextValidID = 0;
    }

    public Vector2D getPos(){
        return position;
    }
    public void setPos(Vector2D new_pos){
        position = new_pos;
    }

    public double getBRadius(){
        return boundingRadius;
    }
    public void setBRadius(double r){
        boundingRadius = r;
    }
    public int getID(){
        return ID;
    }

    public boolean isTagged(){
        return tag;
    }
    public void setTag(){
        tag = true;
    }
    public void unsetTag(){
        tag = false;
    }

    public Vector2D getScale(){
        return scale;
    }
    public void setScale(Vector2D val){
        boundingRadius *= max(val.x, val.y)/max(scale.x, scale.y); 
        scale = val;
    }
    public void setScale(double val){
        boundingRadius *= (val/max(scale.x, scale.y)); 
        scale = new Vector2D(val, val);
    } 

    public int getEntityType(){
        return type;
    }
    public void setEntityType(int new_type){
        type = new_type;
    }
    //----------------------------- SetID -----------------------------------------
    //
    //  this must be called within each constructor to make sure the ID is set
    //  correctly. It verifies that the value passed to the method is greater
    //  or equal to the next valid ID, before setting the ID and incrementing
    //  the next valid ID
    //-----------------------------------------------------------------------------
    public void setID(int val){
        //make sure the val is equal to or greater than the next available ID
        assert ((val >= nextValidID));

        ID = val;
    
        nextValidID = ID + 1;
    }
    
    //------------------------- Overlapped -----------------------------------
    //
    //  tests to see if an entity is overlapping any of a number of entities
    //  stored in a std container
    //------------------------------------------------------------------------
    public boolean isOverlapped(BaseGameEntity ob, ArrayList<BaseGameEntity> conOb, double MinDistBetweenObstacles = 40.0)
    {
        conOb.each{ BaseGameEntity it->
            if (TwoCirclesisOverlapped(ob.getPos(),
                    ob.getBRadius()+MinDistBetweenObstacles,                             
                    it.getPos(),
                    it.getBRadius()))
            {
                return true;
            }
        }

        return false;
    }

    //----------------------- TagNeighbors ----------------------------------
    //
    //  tags any entities contained in a std container that are within the
    //  radius of the single entity parameter
    //------------------------------------------------------------------------
    void tagNeighbors(BaseGameEntity entity, ArrayList<BaseGameEntity> others, double radius)
    {


        //iterate through all entities checking for range
        others.each{ BaseGameEntity it->
            //first clear any current tag
            it.unsetTag();

            //work in distance squared to avoid sqrts
            Vector2D to = it.getPos() - entity.getPos();

            //the bounding radius of the other is taken into account by adding it 
            //to the range
            double range = radius + it.getBRadius();

            //if entity within range, tag for further consideration
            if ( (it != entity) && (to.LengthSq() < range*range))
            {
                it.setTag();
            }
    
        }//next entity
    }


    //------------------- EnforceNonPenetrationContraint ---------------------
    //
    //  Given a pointer to an entity and a std container of pointers to nearby
    //  entities, this function checks to see if there is an overlap between
    //  entities. If there is, then the entities are moved away from each
    //  other
    //------------------------------------------------------------------------

    public <T extends BaseGameEntity , U extends BaseGameEntity> void enforceNonPenetrationContraint(T entity, ArrayList<U> others)
    {

        //iterate through all entities checking for any overlap of bounding
        //radii
        for (U it in others){
            //make sure we don't check against this entity
            if (it == entity) continue;

            //calculate the distance between the positions of the entities
            Vector2D ToEntity = entity.getPos() - it.getPos();

            double DistFromEachOther = ToEntity.length();

            //if this distance is smaller than the sum of their radii then this
            //entity must be moved away in the direction parallel to the
            //ToEntity vector   
            double AmountOfOverLap = it.getBRadius() + entity.getBRadius() -
            DistFromEachOther;

            if (AmountOfOverLap >= 0)
            {
                //move the entity a distance away equivalent to the amount of overlap.
                entity.setPos(entity.getPos() + (ToEntity/DistFromEachOther) *
                    AmountOfOverLap);
            }
        }//next entity
    }
}
