/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.state;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import football.FootballGame;
import football.FootballGUIManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class MainMenuState extends AbstractAppState {

    private FootballGame app;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private InputManager inputManager;
    //private ViewPort viewPort;
    private FootballGUIManager gameGUIManager;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (FootballGame) app; // can cast Application to something more specific
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();


        if (this.app.getGameGUIManager() == null) {
            this.app.initGUI();
        }

        this.gameGUIManager = (FootballGUIManager) this.app.getGameGUIManager();
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Pause and unpause
        super.setEnabled(enabled);
        if (enabled) {
            // pause the whole client game and tell server to pause
            // show nifty main menu screen
            // let user config if they want
            gameGUIManager.goToScreen("MainMenuScreen");
        } else {
            // take away everything not needed while this state is PAUSED
            //...
        }
    }
}
