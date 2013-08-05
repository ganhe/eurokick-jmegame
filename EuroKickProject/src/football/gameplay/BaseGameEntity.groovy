package football.gameplay

import static java.lang.Math.*
import sg.atom.entity.SpatialEntity;
import football.geom.Vector2D
import football.gameplay.ai.event.Telegram
import static football.geom.Geometry2DFunctions.*
import football.gameplay.core.FootballEntityManager as EntityManager;

import groovy.transform.*
import com.jme3.math.Vector3f

@CompileStatic
public class BaseGameEntity extends SpatialEntity{
    public static String default_entity_type = "football_";
 
    //each entity has a unique ID
    public int ID;

    //this is a generic flag. 
    public boolean tag;

    //its location in the environment
    protected Vector2D position;
    protected Vector2D scale;

    //the magnitude of this object's bounding radius
    protected double boundingRadius;


    public BaseGameEntity(int ID){
        super(ID,default_entity_type);
        boundingRadius=0.0;
        scale=new Vector2D(1.0d,1.0d);
        position=new Vector2D(0d,0d);
        tag=false;
    }
    
    public static long getNextValidID(){
        return EntityManager.getInstance().getNewEntityId();
    }
    public int getID(){
        return ID;
    }
    public void update(){

    };

    public boolean handleMessage(Telegram msg){
        return false;
    }
 
    public Vector2D getPos(){
        return position;
    }
    public void setPos(Vector2D new_pos){
        position.cloneVec(new_pos);
    }

    public void setPosUpdate(Vector2D new_pos){
        this.setPos(new_pos);
        update();
    }
    
    public double getBRadius(){
        return boundingRadius;
    }
    public void setBRadius(double r){
        boundingRadius = r;
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

    public String getEntityType(){
        return type;
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
