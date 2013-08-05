/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import football.FootballGameStageManager;
import football.gameplay.FootballGamePlayManager;
import football.gameplay.info.FootballStragegy;
import java.util.List;
import sg.atom.core.GameGUIManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class UIIngameController implements ScreenController {

    private final GameGUIManager gameGUIManager;

    public UIIngameController(GameGUIManager gameGUIManager) {
        this.gameGUIManager = gameGUIManager;
    }

    public void bind(Nifty nifty, Screen screen) {
        if (screen.getScreenId().equals("Stragegy")) {
            //fillMyListBox(screen);
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
    public void fillMyListBox(Screen screen) {
        ListBox listBox = screen.findNiftyControl("myListBox", ListBox.class);
        FootballGamePlayManager gameplay = FootballGameStageManager.getInstance().getGamePlayManager();

        for (FootballStragegy st : gameplay.getCurrentPlayerAsCoach().getStragegies()) {
            listBox.addItem(st.getPosTitle());
        }
    }

    /**
     * When the selection of the ListBox changes this method is called.
     */
    @NiftyEventSubscriber(id = "myListBox")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        FootballGamePlayManager gamePlayManager = (FootballGamePlayManager) gameGUIManager.getStageManager().getGamePlayManager();
        List<String> selection = event.getSelection();
        for (String selectedItem : selection) {
            gamePlayManager.changeStragegy(selectedItem);
            System.out.println("listbox selection [" + selectedItem + "]");
        }
    }
}
