/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import football.FootballGUIManager;
import football.FootballGame;
import football.FootballGameStageManager;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class InGameState extends AbstractAppState implements ActionListener, SceneProcessor {

    private static final Logger logger = Logger.getLogger(ScreenshotAppState.class.getName());
    private boolean capture = false;
    private Renderer renderer;
    private RenderManager rm;
    private ByteBuffer outBuf;
    private int shotIndex = 0;
    private int width, height;
    FootballGUIManager gameGUIManager;
    FootballGameStageManager stageManager;
    private FootballGame app;
    boolean gamePause = false;
    private InputManager inputManager;
    private boolean captureLastFrame;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.app = (FootballGame) app;
        this.gameGUIManager = (FootballGUIManager) this.app.getGameGUIManager();
        this.stageManager = (FootballGameStageManager) this.app.getStageManager();

        if (gameGUIManager == null) {
        }

        List<ViewPort> vps = app.getRenderManager().getPostViews();
        ViewPort last = vps.get(vps.size() - 1);
        last.addProcessor(this);

        // setup the input
        setupInput();
        setEnabled(true);

    }

    void setupInput() {
        inputManager = app.getInputManager();
        // replace default handling of ESC key
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_ESCAPE), new KeyTrigger(KeyInput.KEY_P), new KeyTrigger(KeyInput.KEY_PAUSE));
        inputManager.addListener(this, "Pause");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            //stageManager.initStage();
            goInGame();
        } else {
            goOutGame();
        }
    }

    void goInGame() {
        gameGUIManager.goInGame();
        stageManager.goInGame();
    }

    void pauseGame() {
        gameGUIManager.pauseGame();
        stageManager.pauseGame();

    }

    void goOutGame() {
        gameGUIManager.goOutGame();
        stageManager.goOutGame();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        // units
        // building
        //miniView
        if (!gamePause) {
            gameGUIManager.simpleUpdate(tpf);
            stageManager.updateStage(tpf);
        }
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Pause") && isPressed) {
            if (!gamePause) {
                pauseGame();
            } else {
                resumeGame();
            }
            gamePause = !gamePause;
        }
    }

    private void resumeGame() {
        gameGUIManager.resumeGame();
        stageManager.resumeGame();
    }

    public void takeScreenshot() {
        capture = true;
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        renderer = rm.getRenderer();
        this.rm = rm;
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }

    public void reshape(ViewPort vp, int w, int h) {
        outBuf = BufferUtils.createByteBuffer(w * h * 4);
        width = w;
        height = h;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
    }

    public void postFrame(FrameBuffer out) {
        if (capture) {
            capture = false;
            shotIndex++;

            Camera curCamera = rm.getCurrentCamera();
            int viewX = (int) (curCamera.getViewPortLeft() * curCamera.getWidth());
            int viewY = (int) (curCamera.getViewPortBottom() * curCamera.getHeight());
            int viewWidth = (int) ((curCamera.getViewPortRight() - curCamera.getViewPortLeft()) * curCamera.getWidth());
            int viewHeight = (int) ((curCamera.getViewPortTop() - curCamera.getViewPortBottom()) * curCamera.getHeight());

            renderer.setViewPort(0, 0, width, height);
            renderer.readFrameBuffer(out, outBuf);
            renderer.setViewPort(viewX, viewY, viewWidth, viewHeight);

            captureLastFrame = true;
        }
    }
}
