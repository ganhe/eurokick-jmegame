package football.ui.view;

import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.controls.chatcontrol.ChatEntryModelClass;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import football.ui.model.EmailMessageModel;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class EmailViewConvertor  implements ListBoxViewConverter<EmailMessageModel> {
    private static final String LINE_ICON = "#message-line-icon";
    private static final String LINE_TEXT = "#message-line-text";

    /**
     * Default constructor.
     */
    public EmailViewConvertor() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void display(final Element listBoxItem, final EmailMessageModel item) {
        final Element text = listBoxItem.findElementByName(LINE_TEXT);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementByName(LINE_ICON);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        if (item != null) {
            textRenderer.setText(item.getLabel());
            iconRenderer.setImage(item.getIcon());
        } else {
            textRenderer.setText("");
            iconRenderer.setImage(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getWidth(final Element listBoxItem, final EmailMessageModel item) {
        final Element text = listBoxItem.findElementByName(LINE_TEXT);
        final TextRenderer textRenderer = text.getRenderer(TextRenderer.class);
        final Element icon = listBoxItem.findElementByName(LINE_ICON);
        final ImageRenderer iconRenderer = icon.getRenderer(ImageRenderer.class);
        return ((textRenderer.getFont() == null) ? 0 : textRenderer.getFont().getWidth(item.getLabel()))
                + ((item.getIcon() == null) ? 0 : item.getIcon().getWidth());
    }

}
