/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.state;

import java.util.logging.Level;
import java.util.logging.Logger;
import football.state.InGameState;
import football.state.LoadingState;
import football.state.MainMenuState;
import sg.atom.core.AtomMain;
import sg.atom.core.GameStateManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class FootballGameStateManager extends GameStateManager {

    public FootballGameStateManager(AtomMain app) {
        super(app);
    }

    public void goInGame() {
        LoadingState loadingState = stateManager.getState(LoadingState.class);
        boolean detached = stateManager.detach(loadingState);
        stateManager.attach(new InGameState());
        Logger.getLogger(FootballGameStateManager.class.getName()).log(Level.INFO, "Detach Loading State");
    }

    @Override
    public void loadGame() {
        MainMenuState menuState = stateManager.getState(MainMenuState.class);
        boolean detached = stateManager.detach(menuState);
        stateManager.attach(new LoadingState());
    }

}
