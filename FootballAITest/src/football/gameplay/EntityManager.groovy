package football.gameplay

import groovy.swing.j2d.*
import groovy.transform.*

@CompileStatic
class EntityManager {
    static EntityManager instance;
    //to facilitate quick lookup the entities are stored in a std::map, in which
    //pointers to entities are cross referenced by their identifying number
    private HashMap<Integer,BaseGameEntity> EntityMap = new HashMap<Integer,BaseGameEntity>();
  
    public static EntityManager Instance(){
        if (instance==null){
            instance = new EntityManager();
        }
        return instance;
    }
    
    public void registerEntity(def entity){
        
    }
    
    //------------------------- GetEntityFromID -----------------------------------
    public BaseGameEntity getEntityFromID(int id)
    {
        //find the entity
        return EntityMap.get(id);;
    }

    //--------------------------- RemoveEntity ------------------------------------
    //-----------------------------------------------------------------------------
    void removeEntity(BaseGameEntity entity)
    {    
        EntityMap.remove(entity.getID());
    } 

    //---------------------------- RegisterEntity ---------------------------------
    //-----------------------------------------------------------------------------
    void registerEntity(BaseGameEntity newEntity)
    {
        EntityMap.put(newEntity.getID(), newEntity);
    }
    
    public ArrayList<BaseGameEntity> getAllEntities(){
        return new ArrayList(EntityMap.values());
    }
    
    public <T> ArrayList<T> getAllEntitiesByClass(Class<T> clazz){
        ArrayList<T> result = new ArrayList<T>();
        EntityMap.values().each{
            if (clazz.isInstance(it)){
                result << it;
            }
        }
        return result
    }
}

