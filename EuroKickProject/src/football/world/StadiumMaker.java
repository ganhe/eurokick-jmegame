/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.world;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import java.util.ArrayList;

/**
 *
 * @author hungcuong
 */
public class StadiumMaker {

    Node rootNode;
    AssetManager assetManager;
    Geometry field;
    Node startNode;
    Node stadium;
    private Vector3f startPos;
    float boxX = 60;
    float boxY = 40;
    float startY = 7f;
    float endY = 10f;
    Vector3f center = new Vector3f(3, 0, 0);
    float angle = 0;
    float radiusX = 82;
    float radiusY = 53;
    float angleStep = 128;
    Node gizmo = new Node("gizmo");
    Node triggerNode;
    SpriteHelper spriteHelper;
    private WayPointHelper wayPointHelper;
    private Node fieldNode;
    // Football field constant
    float fieldWidth = 90;
    float circle = 9.15f;
    float penaltyDis = 11;
    float fieldLength = 120;
    // Real field meassure
    float realLengthUnit;
    float realWidthUnit;
    float realLength;
    float realWidth;

    public StadiumMaker(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
    }

    public Geometry putShape(Node node, Mesh shape, ColorRGBA color) {
        Geometry g = new Geometry("shape", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        node.attachChild(g);
        return g;
    }

    private void makeGoalGhostObject(String goalName, PhysicsSpace physicsSpace) {
        Geometry goal = (Geometry) (triggerNode).getChild(goalName);
        Vector3f halfExtents = new Vector3f(3, 4.2f, 1);
        GhostControl ghostControl = new GhostControl(new BoxCollisionShape(halfExtents));
        goal.addControl(ghostControl);
        //rootNode.attachChild(goal);
        physicsSpace.add(ghostControl);
    }

    public void putArrow(Vector3f pos, Vector3f dir, ColorRGBA color) {
        Arrow arrow = new Arrow(dir);
        arrow.setLineWidth(4); // make arrow thicker

        putShape(gizmo, arrow, color).setLocalTranslation(pos);
        rootNode.attachChild(gizmo);
        gizmo.scale(2);
    }

    void makeGizmo() {

        putArrow(Vector3f.ZERO, Vector3f.UNIT_X, ColorRGBA.Red);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Y, ColorRGBA.Green);
        putArrow(Vector3f.ZERO, Vector3f.UNIT_Z, ColorRGBA.Blue);
    }

    void makeSprite4(int currentIndex, Vector3f pos) {
        int ranSheet = FastMath.rand.nextInt(3);
        int startFrame = (spriteHelper.RANDOM_FACING_DIRECTION) ? currentIndex : 0;
        Vector3f pos1, pos2, pos3, pos4;
        /*
        pos1 = pos.clone().add(new Vector3f(0, 0, -radiusY)).add(center);
        pos2 = pos.clone().mult(new Vector3f(-1, 1, 1)).add(new Vector3f(0, 0, -radiusY)).add(center);
        pos3 = pos.clone().mult(new Vector3f(-1, 1, -1)).add(new Vector3f(0, 0, radiusY)).add(center);
        pos4 = pos.clone().mult(new Vector3f(1, 1, -1)).add(new Vector3f(0, 0, radiusY)).add(center);
         * 
         */
        Vector3f delta = pos.subtract(center);

        pos1 = center.clone().add(delta);
        pos2 = center.clone().add(delta.mult(new Vector3f(-1, 1, 1)));
        pos3 = center.clone().add(delta.mult(new Vector3f(-1, 1, -1)));
        pos4 = center.clone().add(delta.mult(new Vector3f(1, 1, -1)));
        spriteHelper.makeSprite(spriteHelper.animArray.get(ranSheet), startFrame, pos1);
        spriteHelper.makeSprite(spriteHelper.animArray.get(ranSheet), startFrame, pos2);
        spriteHelper.makeSprite(spriteHelper.animArray.get(ranSheet), startFrame, pos3);
        spriteHelper.makeSprite(spriteHelper.animArray.get(ranSheet), startFrame, pos4);
    }

    void makeLane(Vector3f pos1, Vector3f pos2, int numOfLayer, int currentIndex, boolean make4) {
        Vector3f pos = new Vector3f();
        for (int j = 0; j < numOfLayer; j++) {

            pos.interpolate(pos1, pos2, 1f / numOfLayer * j);
            if (make4) {
                makeSprite4(currentIndex + j, pos);
            } else {
                int ranSheet = FastMath.rand.nextInt(6);
                int startFrame = (spriteHelper.RANDOM_FACING_DIRECTION) ? currentIndex : 0;
                spriteHelper.makeSprite(spriteHelper.animArray.get(ranSheet), startFrame, pos);
            }
        }
    }

    public void loadStadium() {
        //stadium = (Node) assetManager.loadModel("Scenes/Football/stadium1/stadium_pitch.j3o");
        stadium = (Node) assetManager.loadModel("Scenes/Football/stadium1/albany_football_stadium.j3o");
        spriteHelper = new SpriteHelper(stadium, assetManager);
        wayPointHelper = new WayPointHelper(stadium, assetManager);

        // Make the field
        field = (Geometry) ((Node) stadium).getChild("mat san co");
        fieldNode = new Node("FieldNode");
        fieldNode.attachChild(field);
        stadium.attachChild(fieldNode);

        makeCrowd();
    }

    public void configStadium() {
        // CONFIG THE TRIGGER
        triggerNode = (Node) ((Node) stadium).getChild("Trigger");
        center = triggerNode.getChild("center").getWorldTranslation();
        realLength = FastMath.abs(getConnerLoc(2).distance(getConnerLoc(3)));
        realWidth = FastMath.abs(getConnerLoc(1).distance(getConnerLoc(2)));
        realLengthUnit = realLength / fieldLength;
        realWidthUnit = realWidth / fieldWidth;
    }

    public void attachStadium(PhysicsSpace physicsSpace, AppStateManager stateManager) {
        rootNode.attachChild(stadium);
        physicsSpace.add(field);
        makeGizmo();
        gizmo.setLocalTranslation(center);
        makeGoalGhostObject("goalA", physicsSpace);
        makeGoalGhostObject("goalB", physicsSpace);
        makeWayPoint(1, 2, true);
        spriteHelper.attachSprite(stateManager);
    }

    float mu(float value, int n) {
        float result = 1;
        for (int i = 0; i < n; i++) {
            result = result * value;
        }
        return result;
    }

    public Vector3f getCenter() {
        return center;
    }

    public Node getFieldNode() {
        return this.fieldNode;
    }

    void makeWayPoint(int index1, int index2, boolean make4) {
        for (int i = 0; i < angleStep; i++) {

            float stepValue = (1f / angleStep) * i;
            Vector3f pos1 = wayPointHelper.getPathPos(1, stepValue);
            Vector3f pos2 = wayPointHelper.getPathPos(2, stepValue);
            if (pos1 != null && pos2 != null) {
                makeLane(pos1, pos2, 12, i, make4);
            }


            Vector3f pos3 = wayPointHelper.getPathPos(3, stepValue);
            Vector3f pos4 = wayPointHelper.getPathPos(4, stepValue);
            if (pos3 != null && pos4 != null) {
                makeLane(pos3, pos4, 8, i, make4);
            }

        }
    }

    void createPLBySinCos() {
        for (int i = 0; i < angleStep; i++) {


            angle = FastMath.DEG_TO_RAD * i;
            float slowDown = (FastMath.cos(angle - FastMath.HALF_PI)) * 10;
            float addX = FastMath.cos(angle) * radiusX + slowDown;
            float addZ = FastMath.sin(angle) * radiusY;


            Vector3f pos = new Vector3f(addX, startY, addZ);

            wayPointHelper.getPointList(1).add(pos);

        }
    }

    void createPLByWayPoint() {
    }

    void createPLByFalling(int n) {

        for (int i = 0; i < angleStep; i++) {

            float aX = (radiusX / angleStep);
            float aY = (radiusY * 2 / mu(angleStep, n));
            float nowX = aX * i;
            float nowY = 0.5f * aY * mu(i, n);

            Vector3f pos = new Vector3f(nowX, startY, nowY);

            wayPointHelper.getPointList(1).add(pos);

        }
    }

    void createByPointList(int incStep, boolean make4) {

        float minDis = 8;
        int startIndex = 0;
        int currentIndex = 1;
        Vector3f startLinePoint;
        Vector3f endLinePoint;
        Vector3f pos1 = new Vector3f();
        Vector3f pos2 = new Vector3f();
        ArrayList<Vector3f> pointList1 = wayPointHelper.getPointList(1);
        ArrayList<Vector3f> pointList2 = wayPointHelper.getPointList(2);

        while (currentIndex < pointList1.size()) {
            startLinePoint = pointList1.get(startIndex);

            if (startLinePoint.distance(pointList1.get(currentIndex)) > minDis) {

                endLinePoint = pointList1.get(currentIndex).clone(); // make sprite

                //sprite.setPosition(index * 2, 0, i*1.5f);
                for (int i = 0; i < incStep; i++) {
                    float value = (1f / incStep) * i;
                    pos1.interpolate(startLinePoint, endLinePoint, value);
                    pos2.interpolate(pointList2.get(startIndex), pointList2.get(currentIndex), value);
                    //System.out.println(pos1 + "    " + pos2);

                    makeLane(pos1, pos2, 12, currentIndex, make4);
                }
                startIndex = currentIndex;
            }
            currentIndex++;


        }
    }

    public Vector3f getConnerLoc(int i) {
        return triggerNode.getChild("conner" + i).getLocalTranslation();
    }

    public Vector3f getGoalLoc(int i) {
        String str = "goal" + ((i == 1) ? "A" : "B");
        Spatial goal = triggerNode.getChild(str);
        return goal.getLocalTranslation();
    }

    public float getFieldX() {
        return realLength;
    }

    public float getFieldY() {
        return realWidth;
    }

    public Vector3f getGoalKeeperPos(int i) {
        return getConnerLoc(1).clone().interpolate(getConnerLoc(2), 0.5f);
    }

    private void makeCrowd() {
                
        spriteHelper.initSprite();

        //createPLByFalling(8);

        // MAKE AUDIENCES
        wayPointHelper.loadPath(1, "", true);
        wayPointHelper.loadPath(2, "", true);
        wayPointHelper.loadPath(3, "", true);
        wayPointHelper.loadPath(4, "", true);

        //createByPointList(8, true);
        //createSpriteField();
    }
}
