package football.gameplay.info

import java.util.Random;
/**
 *
 * @author hungcuong
 * Make random from a set of exits values like Enum or a List
 * Groovy style!
 */
public class URandom {
    private static final Random RND = new Random();
    
    public static def random(def values){
        if (values instanceof Enum){
            def list = values.getEnumConstants()
            return random(list)
        } else if (values instanceof List){
            return values[RND.nextInt(values.size())];
        }else {
         
        }
    }
}
