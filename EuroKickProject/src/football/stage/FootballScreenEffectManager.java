package football.stage;

/**
 *
 * @author cuong.nguyenmanh2
 */
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.renderer.ViewPort;
import football.FootballGame;



import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;
import football.FootballGameStageManager;
import sg.atom.fx.ScreenEffectManager;

/**
 * test
 *
 * @author Nehon
 */
public class FootballScreenEffectManager extends ScreenEffectManager {

    DepthOfFieldFilter dofFilter;
    FootballGame app;
    private InputManager inputManager;
    private final AppSettings settings;

    /**
     *
     * @param stageManager
     */
    public FootballScreenEffectManager(FootballGameStageManager stageManager) {
        super(stageManager);
        this.app = (FootballGame) stageManager.getApp();
        this.inputManager = app.getInputManager();
        this.settings = app.getSettings();
    }

    public void init() {
        fpp = new FilterPostProcessor(assetManager);
        //     fpp.setNumSamples(4);

        dofFilter = new DepthOfFieldFilter();
        dofFilter.setFocusDistance(0);
        dofFilter.setFocusRange(50);
        dofFilter.setBlurScale(1.4f);
        fpp.addFilter(dofFilter);
        viewPort.addProcessor(fpp);

        inputManager.addListener(new ActionListener() {
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    if (name.equals("toggle")) {
                        dofFilter.setEnabled(!dofFilter.isEnabled());
                    }


                }
            }
        }, "toggle");
        inputManager.addListener(new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("blurScaleUp")) {
                    dofFilter.setBlurScale(dofFilter.getBlurScale() + 0.01f);
                    System.out.println("blurScale : " + dofFilter.getBlurScale());
                }
                if (name.equals("blurScaleDown")) {
                    dofFilter.setBlurScale(dofFilter.getBlurScale() - 0.01f);
                    System.out.println("blurScale : " + dofFilter.getBlurScale());
                }
                if (name.equals("focusRangeUp")) {
                    dofFilter.setFocusRange(dofFilter.getFocusRange() + 1f);
                    System.out.println("focusRange : " + dofFilter.getFocusRange());
                }
                if (name.equals("focusRangeDown")) {
                    dofFilter.setFocusRange(dofFilter.getFocusRange() - 1f);
                    System.out.println("focusRange : " + dofFilter.getFocusRange());
                }
                if (name.equals("focusDistanceUp")) {
                    dofFilter.setFocusDistance(dofFilter.getFocusDistance() + 1f);
                    System.out.println("focusDistance : " + dofFilter.getFocusDistance());
                }
                if (name.equals("focusDistanceDown")) {
                    dofFilter.setFocusDistance(dofFilter.getFocusDistance() - 1f);
                    System.out.println("focusDistance : " + dofFilter.getFocusDistance());
                }

            }
        }, "blurScaleUp", "blurScaleDown", "focusRangeUp", "focusRangeDown", "focusDistanceUp", "focusDistanceDown");


        inputManager.addMapping("toggle", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("blurScaleUp", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("blurScaleDown", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("focusRangeUp", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("focusRangeDown", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("focusDistanceUp", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("focusDistanceDown", new KeyTrigger(KeyInput.KEY_L));

    }

    public void simpleUpdate(float tpf) {
        Camera cam = stageManager.getCurrentActiveCamera();
        Vector3f origin = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();
        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        int numCollisions = stageManager.getWorldManager().getWorldNode().collideWith(ray, results);
        if (numCollisions > 0) {
            CollisionResult hit = results.getClosestCollision();
            dofFilter.setFocusDistance(hit.getDistance() / 10.0f);
        }
    }
}
