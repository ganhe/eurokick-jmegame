/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import football.gameplay.FootballGamePlayManager;
import football.stage.FootballScreenEffectManager;
import football.world.FootballGameWorldManager;
import sg.atom.core.StageManager;
import sg.atom.world.WorldSettings;
import football.stage.sound.FbSoundManager;
import sg.atom.fx.ScreenEffectManager;

/**
 *
 * @author hungcuong
 */
public class FootballGameStageManager extends StageManager {

    private FbSoundManager soundManager;
    /*
     public FootballGameStageManager(FootballGame app) {
     super(app);
     }
     */
    /**
     * Singleton reference of FootballGameStageManager.
     */
    private static FootballGameStageManager selfRef;

    /**
     * Constructs singleton instance of FootballGameStageManager.
     */
    private FootballGameStageManager(FootballGame app) {
        super(app);
        selfRef = this;
    }

    /**
     * Provides reference to singleton object of FootballGameStageManager.
     *
     * @return Singleton instance of FootballGameStageManager.
     */
    public static FootballGameStageManager getInstance() {
        if (selfRef == null) {
            selfRef = new FootballGameStageManager(FootballGame.getInstance());
        }
        return selfRef;
    }

    @Override
    public void initStage() {
        super.initStage();
    }

    @Override
    public void initStageCustom() {
        worldManager = new FootballGameWorldManager((FootballGame) app, rootNode);
        WorldSettings wSettings = new WorldSettings();
        wSettings.useDayLight = false;
        wSettings.useEnviroment = false;
        wSettings.useForestor = false;
        wSettings.useLevel = false;
        wSettings.useWater = false;
        wSettings.useWeather = false;
        wSettings.useTerrainLOD = false;
        wSettings.usePhysics = true;
        worldManager.initWorld(null, wSettings);
        this.gamePlayManager = new FootballGamePlayManager((FootballGame) app);
        this.soundManager = new FbSoundManager((FootballGame) app);
        this.screenEffectManager = new FootballScreenEffectManager(this);
    }

    @Override
    public void loadStage() {
        super.loadStage();
        soundManager.load(1);
    }

    @Override
    public void configStageCustom() {
        getGamePlayManager().configGamePlay();
        this.getScreenEffectManager().init();
    }

    @Override
    public void finishStageCustom() {
        setupCamera();
        setupKeys();
        //pauseGame();
        getGamePlayManager().startGamePlay();
    }

    public void setupKeys() {
        inputManager.addMapping("ToogleStats",
                new KeyTrigger(KeyInput.KEY_I));
        inputManager.addListener(actionListener, "ToogleStats");


    }
    boolean displayStats = true;
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean pressed, float tpf) {
            if (name.equals("ToogleStats") && pressed) {
                displayStats = !displayStats;
                app.setDisplayStatView(displayStats);
            }
        }
    };

    @Override
    public FootballGameWorldManager getWorldManager() {
        return (FootballGameWorldManager) worldManager;
    }

    @Override
    public FootballGamePlayManager getGamePlayManager() {
        return (FootballGamePlayManager) gamePlayManager;
    }

    public FbSoundManager getSoundManager() {
        return this.soundManager;
    }

    void setupCamera() {
        getApp().getFlyByCamera().setMoveSpeed(10f);
        Camera cam = getCurrentActiveCamera();
        cam.setLocation(new Vector3f(0, 20, 0));
        cam.lookAt(((FootballGameWorldManager) worldManager).getStadiumMaker().getCenter(), Vector3f.UNIT_Y);


    }

    @Override
    public void updateStageCustom(float tpf) {
        getGamePlayManager().update(tpf);
        getScreenEffectManager().simpleUpdate(tpf);
    }

    public void goInGame() {
    }

    public void pauseGame() {
        gamePaused = true;
    }

    public void goOutGame() {
    }

    public void resumeGame() {
    }

    private FootballScreenEffectManager getScreenEffectManager() {
        return (FootballScreenEffectManager) screenEffectManager;
    }
}
