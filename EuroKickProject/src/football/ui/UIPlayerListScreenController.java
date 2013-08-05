package football.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import football.ui.model.EmailMessageModel;
import java.util.List;
import sg.atom.core.GameGUIManager;
import football.gameplay.info.*;
import football.gameplay.*;
import java.util.ArrayList;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class UIPlayerListScreenController implements ScreenController {

    private final GameGUIManager gameGUIManager;
    private ArrayList selectedList;

    public UIPlayerListScreenController(GameGUIManager gameGUIManager) {
        this.gameGUIManager = gameGUIManager;
    }

    public void bind(Nifty nifty, Screen screen) {
        if (screen.getScreenId().equals("PlayerListScreen")) {
            selectedList = new ArrayList();
            fillPlayersTable(screen);
        }
    }

    public void onStartScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Fill the listbox with items. In this case with Strings.
     */
    public void fillPlayersTable(Screen screen) {
        ListBox listBox = screen.findNiftyControl("player-table", ListBox.class);

        FootballGamePlayManager gamePlayManager = FootballGamePlayManager.getDefault();
        for (FootballPlayerInfo player : getPlayerList()) {
            listBox.addItem(player);
            System.out.println(" Add " + player.toString());
        }
    }

    public ArrayList<FootballPlayerInfo> getPlayerList() {
        FootballGamePlayManager gamePlayManager = FootballGamePlayManager.getDefault();
        return gamePlayManager.getLeague().getParticipants().get(0).getPlayersList();
    }

    public ArrayList getSelectedList() {
        return selectedList;
    }

    /**
     * When the selection of the ListBox changes this method is called.
     */
    @NiftyEventSubscriber(id = "player-table")
    public void onPlayerListSelectionChanged(final String id, final ListBoxSelectionChangedEvent<EmailMessageModel> event) {
        List<EmailMessageModel> selection = event.getSelection();
        selectedList.clear();
        selectedList.addAll(selection);
        System.out.println(" Selection changed");
        /*
        for (EmailMessageModel m : selection) {
            if (!selectedList.contains(m)) {
                this.selectedList.add(m);
            }
        }
        */
    }
}
