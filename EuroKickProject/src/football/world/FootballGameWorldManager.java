/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.world;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.PssmShadowRenderer;
import football.FootballGame;
import sg.atom.gameplay.GameLevel;
import sg.atom.stage.WorldManager;
import sg.atom.world.WorldSettings;

/**
 *
 * @author hungcuong
 */
public class FootballGameWorldManager extends WorldManager {

    Geometry ballGeometry;
    StadiumMaker stadiumMaker;
    PssmShadowRenderer pssm;
    boolean toogleTerrain = false;
    boolean toogleShaddow = false;

    public FootballGameWorldManager(FootballGame app, Node rootNode) {
        super(app, rootNode);
    }

    public Spatial createBall() {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        //immovable sphere with mesh collision shape
        Sphere sphere = new Sphere(8, 8, 1);

        ballGeometry = new Geometry("Soccer ball", sphere);
        ballGeometry.setMaterial(material);
        ballGeometry.setLocalTranslation(1, 1, -4);
        ballGeometry.scale(0.2f);
        //RigidBodyControl ballPhysicsControl = new RigidBodyControl(new SphereCollisionShape(1f), 1);
        GhostControl ballPhysicsControl = new GhostControl(new SphereCollisionShape(1f));
        //ballPhysicsControl.setDamping(0.1f, 0.1f);
        ballGeometry.addControl(ballPhysicsControl);

        rootNode.attachChild(ballGeometry);
        getPhysicsSpace().add(ballGeometry);
        return ballGeometry;
    }

    @Override
    public void initWorld(GameLevel level, WorldSettings worldSettings) {
        super.initWorld(level, worldSettings);
        stadiumMaker = new StadiumMaker(rootNode, assetManager);

    }

    @Override
    public void loadWorld() {
        stadiumMaker.loadStadium();

    }

    @Override
    public void configWorld() {
        super.configWorld();
        stadiumMaker.configStadium();
        setupKeys();
    }

    @Override
    public void attachWorld() {
        super.attachWorld();
        stadiumMaker.attachStadium(getPhysicsSpace(), stateManager);
        setupLight();
    }

    @Override
    public void finishWorld() {
        super.finishWorld();
    }

    void setupKeys() {
        stageManager.getInputManager().addMapping("ToogleTerrain", new KeyTrigger(KeyInput.KEY_F6));
        stageManager.getInputManager().addListener(actionListener, "ToogleTerrain");
        stageManager.getInputManager().addMapping("ToogleShaddow", new KeyTrigger(KeyInput.KEY_F9));
        stageManager.getInputManager().addListener(actionListener, "ToogleShaddow");
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("ToogleTerrain") && pressed) {
                doToogleTerrain();
            } else if (name.equals("ToogleShaddow") && pressed) {
                doToogleShaddow();;
            }
        }
    };

    void doToogleTerrain() {
        if (terrain != null) {
            toogleTerrain = !toogleTerrain;
            if (toogleTerrain) {
                terrain.removeFromParent();
            } else {
                rootNode.attachChild(terrain);
            }
        }
    }

    void doToogleShaddow() {
        if (pssm != null) {
            toogleShaddow = !toogleShaddow;
            if (toogleShaddow) {
                app.getViewPort().removeProcessor(pssm);
            } else {
                app.getViewPort().addProcessor(pssm);
            }
        }
    }

    private void setupLight() {
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.8f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);

        DirectionalLight dl2 = new DirectionalLight();
        dl2.setDirection(new Vector3f(0.8f, -0.7f, 1).normalizeLocal());
        dl2.setColor(new ColorRGBA(0.8f, 1f, 0.8f, 1.0f));
        rootNode.addLight(dl2);
        /**
         * Advanced shadows for uneven surfaces
         */
        pssm = new PssmShadowRenderer(assetManager, 1024, 3);
        pssm.setDirection(new Vector3f(-0.8f, -0.7f, 0).normalizeLocal());
        ViewPort viewPort = stageManager.getCurrentActiveViewPort();
        viewPort.addProcessor(pssm);

    }

    public Spatial getBall() {
        return ballGeometry;
    }

    public StadiumMaker getStadiumMaker() {
        return stadiumMaker;
    }
}
