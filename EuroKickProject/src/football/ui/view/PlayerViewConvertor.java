package football.ui.view;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.ScreenController;
import football.gameplay.info.FootballPlayerInfo;
import football.ui.UIPlayerListScreenController;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class PlayerViewConvertor implements ListBox.ListBoxViewConverter<FootballPlayerInfo> {

    /**
     * Default constructor.
     */
    NiftyImage fbAttackerIcon;

    public PlayerViewConvertor() {
        //fbAttackerIcon
    }

    @Override
    public void display(final Element listBoxItem, final FootballPlayerInfo item) {
        Nifty nifty = listBoxItem.getNifty();
        if (item != null) {
            setDisplay(listBoxItem, "#player-line-name", "Text", item.getName().substring(0, 20));
            setDisplay(listBoxItem, "#player-line-pos", "Text", item.getRole().toString());
            setDisplay(listBoxItem, "#player-line-shot", "Text", item.skillBallControl);
            setDisplay(listBoxItem, "#player-line-pass", "Text", item.skillPass);
            setDisplay(listBoxItem, "#player-line-keep", "Text", item.skillBallKeep);
            setDisplay(listBoxItem, "#player-line-take", "Text", item.skillBallTake);
            //setDisplay(listBoxItem,"player-line-name","Text",listBoxItem.getNifty().createImage(item., true));
            setDisplay(listBoxItem, "#player-line-icon", "Image", null);
        } else {
            setDisplay(listBoxItem, "#player-line-name", "Text", "");
            setDisplay(listBoxItem, "#player-line-icon", "Image", null);
        }

        int playerIndex = getScreenController(nifty).getPlayerList().indexOf(item);
        if (getScreenController(nifty).getSelectedList().contains(item)){
            listBoxItem.setStyle("selectedLine");
        } else if (playerIndex % 2 == 0) {
            listBoxItem.setStyle("oddLine");
        } else {
            listBoxItem.setStyle("evenLine");
        }

    }

    UIPlayerListScreenController getScreenController(Nifty nifty) {
        return (UIPlayerListScreenController) nifty.getCurrentScreen().getScreenController();
    }

    void setDisplay(Element listBoxItem, String name, String type, Object data) {

        Element el = listBoxItem.findElementByName(name);

        if (type.equals("Text")) {
            TextRenderer textRenderer = el.getRenderer(TextRenderer.class);
            if (data instanceof String) {
                textRenderer.setText((String) data);
            } else {
                textRenderer.setText(data.toString());
            }
        } else if (type.equals("Image")) {
            ImageRenderer iconRenderer = el.getRenderer(ImageRenderer.class);
            iconRenderer.setImage((NiftyImage) data);
        }
    }

    @Override
    public int getWidth(final Element listBoxItem, final FootballPlayerInfo item) {
        return 400;
    }
}
