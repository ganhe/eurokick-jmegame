/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import football.FootballGUIManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class MainMenuScreenUI implements ScreenController {

    FootballGUIManager gameGUIManager;
    Screen screen;
    String[] tabNames = {"KeyboardTabPanel", "GraphicsTabPanel", "SoundsTabPanel", "GeneralTabPanel"};
    String currentTab;

    public MainMenuScreenUI(FootballGUIManager gameGUIManager) {
        this.gameGUIManager = gameGUIManager;

    }

    public void bind(Nifty nifty, Screen screen) {
        this.screen = screen;
        gameGUIManager.getInputManager().setCursorVisible(true);
    }

    public void onStartScreen() {
        if (screen.getScreenId().equals("Options")) {
            showTab("KeyboardTabPanel");
        }

    }

    public void onEndScreen() {
    }

    public void showOptions() {
        System.out.println(" Go to Options");
        gameGUIManager.goToScreen("Options");
    }

    public void backToMainMenu() {
        gameGUIManager.goToScreen("MainMenuScreen");
    }

    public void showTab(String tabName) {
        if (currentTab != tabName) {
            hideAllTabs();
            Element e = screen.findElementByName(tabName);
            e.setVisibleToMouseEvents(true);
            e.setConstraintHeight(new SizeValue("100%"));
            e.setVisible(true);
            e.setFocusable(true);

            currentTab = tabName;
            e.getParent().resetLayout();
            screen.resetLayout();
            screen.layoutLayers();

            System.out.println(" " + e.getId() + " " + e.getConstraintHeight() + " " + e.getY() + " " + e.getConstraintY());
            System.out.println("-------------------------------------------------");
        }
    }

    public void hideAllTabs() {
        for (String name : tabNames) {
            //if (name != currentTab) {
            Element e = screen.findElementByName(name);
            System.out.println(" " + e.getId() + " " + e.getConstraintHeight() + " " + e.getY() + " " + e.getConstraintY());
            e.setVisibleToMouseEvents(false);
            e.setConstraintHeight(new SizeValue("0%"));
            e.setFocusable(false);
            e.setVisible(false);
            e.getLayoutPart().getBox().setY(0);

            System.out.println(" " + e.getId() + " " + e.getConstraintHeight() + " " + e.getY() + " " + e.getConstraintY());
            //}
        }

    }

    public void createHost() {
        //gameStateManager.changeState();
        gameGUIManager.getApp().getGameStateManager().loadGame();
    }

    public void singleGame() {
        //gameGUIManager.goToScreen("CreateSingleGameScreen");
        createHost();
    }

    public void quitGame() {
        gameGUIManager.getApp().quitGame();
    }
}
