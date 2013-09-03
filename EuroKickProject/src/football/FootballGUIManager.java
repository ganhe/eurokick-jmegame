package football;

import football.ui.MainMenuScreenUI;
import football.ui.UIEditPlayer;
import football.ui.UIIngameController;
import football.ui.UIManagerScreenController;
import football.ui.UIPlayerListScreenController;
import sg.atom.ui.GameGUIManager;
import sg.atom.ui.common.UILoadingScreenController;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class FootballGUIManager extends GameGUIManager {

    public FootballGUIManager(FootballGame app) {
        super(app);
    }

    @Override
    public void initGUI() {
        super.initGUI();
    }

    public void setupCommonScreens() {
        nifty.registerScreenController(new UIIngameController(this),
                new UILoadingScreenController(this),
                new UIEditPlayer(this),
                new MainMenuScreenUI(this),
                new UIManagerScreenController(this),
                new UIPlayerListScreenController(this));

        nifty.addXml("Interface/InGame/Ingame2.xml");
        nifty.addXml("Interface/InGame/ManagerScreen.xml");
        nifty.addXml("Interface/InGame/PlayerListScreen.xml");
        nifty.addXml("Interface/MainMenu/Loading.xml");
        nifty.addXml("Interface/EditPlayer.xml");
        nifty.addXml("Interface/MainMenu/MainMenu.xml");
        nifty.addXml("Interface/MainMenu/Options/Options.xml");
        //nifty.addXml("Interface/MainMenu/CreateHost.xml");
    }

    public void goInGame() {
        goToScreen("InGameScreen");
        
    }

    public void pauseGame() {
    }

    public void goOutGame() {
    }

    public void simpleUpdate(float tpf) {
    }

    public void resumeGame() {
    }

    public void testNextScreens() {
    }
}
