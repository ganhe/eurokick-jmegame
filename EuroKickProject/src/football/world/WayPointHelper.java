/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.world;

import com.jme3.asset.AssetManager;
import com.jme3.cinematic.MotionPath;
import com.jme3.math.Spline;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 *
 * @author hungcuong
 */
public class WayPointHelper {

    Node rootNode;
    AssetManager assetManager;
    ArrayList<ArrayList<Vector3f>> pointListArray = new ArrayList<ArrayList<Vector3f>>();

    public WayPointHelper(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
    }

    Vector3f getPathPos(int pathIndex, float stepValue) {
        Vector3f pos1 = new Vector3f();
        MotionPath path1 = pathArray.get(pathIndex - 1);
        Spline sp1 = path1.getSpline();
        float totalLength1 = sp1.getTotalLength();
        float dis1 = stepValue * totalLength1;
        //getting waypoint index and current value from new traveled distance
        Vector2f v1 = getWayPointIndexForDistance(sp1, dis1);
        int currentIndex = (int) v1.x;
        float currentValue = v1.y;
        //interpolating new position
        sp1.interpolate(currentValue, currentIndex, pos1);
        /*
        if (currentIndex % 2 == 1) {
        return null;
        }
         * 
         */
        return pos1;
    }

    MotionPath loadPath(int pathIndex, String customName, boolean debugShape) {
        Node way1 = (Node) rootNode.getChild("Way" + pathIndex);
        MotionPath path1 = new MotionPath();
        for (Spatial sp : way1.getChildren()) {
            path1.addWayPoint(sp.getWorldTranslation().clone());
        }
        pathArray.add(path1);
        return path1;
    }
    ArrayList<MotionPath> pathArray = new ArrayList<MotionPath>();

    public Vector2f getWayPointIndexForDistance(Spline spline, float distance) {
        float sum = 0;
        distance = distance % spline.getTotalLength();
        int i = 0;
        for (Float len : spline.getSegmentsLength()) {
            if (sum + len >= distance) {
                return new Vector2f((float) i, (distance - sum) / len);
            }
            sum += len;
            i++;
        }
        return new Vector2f((float) spline.getControlPoints().size() - 1, 1.0f);
    }

    ArrayList<Vector3f> getPointList(int i) {
        return pointListArray.get(i);
    }

    public void enableDebug() {
        for (MotionPath path1 : pathArray) {
            path1.enableDebugShape(assetManager, rootNode);
        }

    }
}
