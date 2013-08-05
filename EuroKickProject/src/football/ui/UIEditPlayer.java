/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import football.FootballGUIManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class UIEditPlayer implements ScreenController {

    private FootballGUIManager gameGUIManager;

    public UIEditPlayer(FootballGUIManager gameGUIManager) {
        this.gameGUIManager = gameGUIManager;
    }

    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
