package football;

import com.jme3.system.AppSettings;
import football.state.FootballGameStateManager;
import football.state.LoadingState;
import football.state.TestUIState;
import sg.atom.core.AtomMain;

/**
 *
 * @author hungcuong
 */
public class FootballGame extends AtomMain {

    public static void main(String[] args) {
        FootballGame app = FootballGame.getInstance();
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1024);
        settings.setHeight(768);
        app.setSettings(settings);
        app.setShowSettings(false);
        //app.setDisplayStatView(false);
        app.start();

    }
    /**
     * Singleton reference of Object.
     */
    private static FootballGame selfRef;

    /**
     * Constructs singleton instance of Object.
     */
    private FootballGame() {
        selfRef = this;
    }

    /**
     * Provides reference to singleton object of Object.
     *
     * @return Singleton instance of Object.
     */
    public static final FootballGame getInstance() {
        if (selfRef == null) {
            selfRef = new FootballGame();
        }
        return selfRef;
    }

    @Override
    public void startup() {
        //gameStateManager.setStartupState(new TestUIState("PlayerListScreen"));
        gameStateManager.setStartupState(new LoadingState());
        gameStateManager.startUp();
    }

    @Override
    public void initGUI() {
        gameGUIManager = new FootballGUIManager(this);
        gameGUIManager.initGUI();
    }

    @Override
    public void initStage() {
        stageManager = FootballGameStageManager.getInstance();
        stageManager.initStage();
    }

    @Override
    public void initGameStateManager() {
        this.gameStateManager = new FootballGameStateManager(this);
        this.gameStateManager.initState();
    }

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();

    }

}
