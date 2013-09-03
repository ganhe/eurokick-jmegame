package football.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import football.ui.model.EmailMessageModel;
import java.util.List;
import sg.atom.ui.GameGUIManager;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class UIManagerScreenController implements ScreenController {

    private final GameGUIManager gameGUIManager;

    public UIManagerScreenController(GameGUIManager gameGUIManager) {
        this.gameGUIManager = gameGUIManager;
    }

    public void bind(Nifty nifty, Screen screen) {
        if (screen.getScreenId().equals("ManagerScreen")) {
            fillEmailTable(screen);
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
    public void fillEmailTable(Screen screen) {
        ListBox listBox = screen.findNiftyControl("message-table", ListBox.class);


        for (int i = 0; i < 10; i++) {
            EmailMessageModel email = new EmailMessageModel(" Email " + i, null);
            listBox.addItem(email);
        }
    }

    /**
     * When the selection of the ListBox changes this method is called.
     */
    @NiftyEventSubscriber(id = "message-table")
    public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<EmailMessageModel> event) {

        List<EmailMessageModel> selection = event.getSelection();

    }
}
