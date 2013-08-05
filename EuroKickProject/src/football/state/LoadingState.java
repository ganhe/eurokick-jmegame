/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.state;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import de.lessvoid.nifty.screen.Screen;
import football.FootballGame;
import football.FootballGameStageManager;
import sg.atom.state.LoadingAppState;

import sg.atom.ui.common.UILoadingScreenController;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class LoadingState extends LoadingAppState {

    private FootballGame app;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private InputManager inputManager;
    private ViewPort viewPort;
    private UILoadingScreenController guiController;
    private FootballGameStateManager gameStateManager;
    private float oldPercent = -1f;
    private Screen screen;
    private Screen loadingScreen;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (FootballGame) app; // can cast Application to something more specific

        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();
        this.gameStateManager = (FootballGameStateManager) this.app.getGameStateManager();
        this.viewPort = this.app.getViewPort();

        if (this.app.getGameGUIManager() == null) {
            this.app.initGUI();
        }
        this.gameGUIManager = this.app.getGameGUIManager();
        if (this.app.getStageManager() == null) {
            this.app.initStage();
        }
        this.stageManager = (FootballGameStageManager) this.app.getStageManager();
        setEnabled(true);

    }

    @Override
    public void setEnabled(boolean enabled) {
        // Pause and unpause
        super.setEnabled(enabled);
        if (enabled) {
            initPhase();
        } else {
            if (loadComplete) {
                //nextState();
            }

        }
    }

    @Override
    protected void initPhase() {
        gameGUIManager.goToScreen("loadingScreen");
        loadingScreen = gameGUIManager.getNifty().getScreen("loadingScreen");
        if (loadingScreen == null) {
            throw new RuntimeException("May be: You didn't add the LoadingScreen in XML yet!");
        } else {
            guiController = (UILoadingScreenController) loadingScreen.getScreenController();
        }
    }

    @Override
    protected void loadPhase() {
        stageManager.loadStage();
        System.out.println("Finish Loading !");
        stageManager.configStage();
        System.out.println("Finish Config !");
    }

    @Override
    protected void finishPhase() {
        stageManager.attachStage();
        stageManager.finishStage();
    }

    @Override
    protected void nextState() {
        gameStateManager.goInGame();
    }

    @Override
    protected void watchTask() {
        // Wait for the GUI controller to finish screen changing
        if (guiController != null && loadingScreen.isRunning()) {
            if (stageManager.getProcessInfo().getCurrentProgressName() != null) {
                float currentProcess = stageManager.getProcessInfo().getCurrentProgressPercent();
                if (oldPercent != currentProcess) {
                    guiController.setProgress(currentProcess, stageManager.getProcessInfo().getCurrentProgressName());
                    oldPercent = currentProcess;


                }
            }
        }
    }

    public void updateProgressBar(boolean hasError, String errorMsg) {
        // Wait for the GUI controller to finish screen changing
        if (guiController != null && gameGUIManager.getNifty().getCurrentScreen().getScreenId().equals("loadingScreen")) {
            if (hasError) {
                //
                guiController.setProgress(0, "Error ! Press Esc to back to main menu :" + errorMsg);
            } else {
                if (stageManager.getProcessInfo().getCurrentProgressName() != null) {
                    float currentProcess = stageManager.getProcessInfo().getCurrentProgressPercent();
                    if (oldPercent != currentProcess) {
                        guiController.setProgress(currentProcess, stageManager.getProcessInfo().getCurrentProgressName());
                        oldPercent = currentProcess;

                        System.out.println("Load :" + oldPercent);
                    }
                }
            }
        }
    }
}
