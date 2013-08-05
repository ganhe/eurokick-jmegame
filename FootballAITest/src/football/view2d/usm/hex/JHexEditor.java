package football.view2d.usm.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA. User: laullon Date: 08-abr-2003 Time: 13:21:09
 */
public class JHexEditor extends JPanel implements FocusListener, AdjustmentListener, MouseWheelListener {

    byte[] buff;
    public int cursor;
    protected static Font font = new Font("Monospaced", 0, 12);
    protected int border = 2;
    public boolean DEBUG = false;
    private JPanel panel;
    private JScrollBar scrollBar;
    private int begin = 0;
    private int lines = 1;
    private Label lblStatus;

    public JHexEditor() {
        super();
        createLayout();
    }

    public JHexEditor(byte[] buff) {
        this();
        setBuff(buff);
    }

    public void setBuff(byte[] buff) {
        this.buff = buff;
        updateData();
        resetLayout();
    }

    public void resetLayout() {
        scrollBar.setMaximum(buff.length / getLines());
    }

    public void updateData() {
    }

    private void createLayout() {

        this.addMouseWheelListener(this);

        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollBar.addAdjustmentListener(this);
        scrollBar.setMinimum(0);

        JPanel p1, p2, p3;
        //centro
        p1 = new JPanel(new BorderLayout(1, 1));
        p1.add(new JHexEditorHEX(this), BorderLayout.CENTER);
        p1.add(new ColumnRuler(), BorderLayout.NORTH);

        // izq.
        p2 = new JPanel(new BorderLayout(1, 1));
        p2.add(new HRuler(), BorderLayout.CENTER);
        p2.add(new EmptyPanel(), BorderLayout.NORTH);

        // der
        p3 = new JPanel(new BorderLayout(1, 1));
        p3.add(scrollBar, BorderLayout.EAST);
        p3.add(new JHexEditorASCII(this), BorderLayout.CENTER);
        p3.add(new EmptyPanel(), BorderLayout.NORTH);

        panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 1));
        panel.add(p1, BorderLayout.CENTER);
        panel.add(p2, BorderLayout.WEST);
        panel.add(p3, BorderLayout.EAST);
        
        lblStatus = new Label();
        lblStatus.setBackground(new Color(200,200,200));
        panel.add(lblStatus, BorderLayout.SOUTH);
        this.setLayout(new BorderLayout(1, 1));
        this.add(panel, BorderLayout.CENTER);
    }
    void updateStatusText(){
        lblStatus.setText("Pos" + cursor);
    }
    public void paint(Graphics g) {
        FontMetrics fn = getFontMetrics(font);
        Rectangle rec = this.getBounds();
        if (buff != null) {
            lines = (rec.height / fn.getHeight()) - 1;
            int rows = (buff.length / 16) - 1;
            if (lines > rows) {
                lines = rows;
                begin = 0;
            }

            scrollBar.setValues(getBegin(), +getLines(), 0, buff.length / 16);
            scrollBar.setValueIsAdjusting(true);
        }
        super.paint(g);
    }

    protected void updateCursor() {
        int rows = (cursor / 16);

        //System.out.print("- " + begin + "<" + rows + "<" + (lines + begin) + "(" + lines + ")");

        if (rows < begin) {
            begin = rows;
        } else if (rows >= begin + lines) {
            begin = rows - (lines - 1);
        }

        //System.out.println(" - " + begin + "<" + rows + "<" + (lines + begin) + "(" + lines + ")");
        updateStatusText();
        repaint();
    }

    protected int getBegin() {
        return begin;
    }

    protected int getLines() {
        return lines;
    }

    public void setCursorPos(int cursor) {
        this.cursor = cursor;
        updateCursor();
    }

    
    protected void fillCharRect(Graphics g, int x, int y, int s) {
        FontMetrics fn = getFontMetrics(font);
        g.fillRect(((fn.stringWidth(" ") + 1) * x) + border, (fn.getHeight() * y) + border, ((fn.stringWidth(" ") + 1) * s), fn.getHeight() + 1);
    }

    protected void drawCharRect(Graphics g, int x, int y, int s) {
        FontMetrics fn = getFontMetrics(font);
        g.drawRect(((fn.stringWidth(" ") + 1) * x) + border, (fn.getHeight() * y) + border, ((fn.stringWidth(" ") + 1) * s), fn.getHeight() + 1);
    }

    protected void printString(Graphics g, String s, int x, int y) {
        FontMetrics fn = getFontMetrics(font);
        g.drawString(s, ((fn.stringWidth(" ") + 1) * x) + border, ((fn.getHeight() * (y + 1)) - fn.getMaxDescent()) + border);
    }

    public void focusGained(FocusEvent e) {
        this.repaint();
    }

    public void focusLost(FocusEvent e) {
        this.repaint();
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        begin = e.getValue();
        if (begin < 0) {
            begin = 0;
        }
        repaint();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        begin += (e.getUnitsToScroll());
        if ((begin + lines) >= buff.length / 16) {
            begin = (buff.length / 16) - lines;
        }
        if (begin < 0) {
            begin = 0;
        }
        repaint();
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 33:    // rep
                if (cursor >= (16 * lines)) {
                    cursor -= (16 * lines);
                }
                updateCursor();
                break;
            case 34:    // fin
                if (cursor < (buff.length - (16 * lines))) {
                    cursor += (16 * lines);
                }
                updateCursor();
                break;
            case 35:    // fin
                cursor = buff.length - 1;
                updateCursor();
                break;
            case 36:    // ini
                cursor = 0;
                updateCursor();
                break;
            case 37:    // <--
                if (cursor != 0) {
                    cursor--;
                }
                updateCursor();
                break;
            case 38:    // <--
                if (cursor > 15) {
                    cursor -= 16;
                }
                updateCursor();
                break;
            case 39:    // -->
                if (cursor != (buff.length - 1)) {
                    cursor++;
                }
                updateCursor();
                break;
            case 40:    // -->
                if (cursor < (buff.length - 16)) {
                    cursor += 16;
                }
                updateCursor();
                break;
        }
    }

    private class ColumnRuler extends JPanel {

        public ColumnRuler() {
            this.setLayout(new BorderLayout(1, 1));
        }

        @Override
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            int nl = 1;
            d.setSize(((fn.stringWidth(" ") + 1) * +((16 * 3) - 1)) + (border * 2) + 1, h * nl + (border * 2) + 1);
            return d;
        }

        @Override
        public void paint(Graphics g) {
            Dimension d = getMinimumSize();
            g.setColor(new Color(180,180,180));
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);
            g.setFont(font);

            for (int n = 0; n < 16; n++) {
                if (n == (cursor % 16)) {
                    drawCharRect(g, n * 3, 0, 2);
                }
                String s = "00" + Integer.toHexString(n);
                s = s.substring(s.length() - 2);
                printString(g, s, n * 3, 0);
            }
        }
    }

    private class EmptyPanel extends JPanel {

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            d.setSize((fn.stringWidth(" ") + 1) + (border * 2) + 1, h + (border * 2) + 1);
            return d;
        }
    }

    private class HRuler extends JPanel {

        public HRuler() {
            this.setLayout(new BorderLayout(1, 1));
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        public Dimension getMinimumSize() {
            Dimension d = new Dimension();
            FontMetrics fn = getFontMetrics(font);
            int h = fn.getHeight();
            int nl = getLines();
            d.setSize((fn.stringWidth(" ") + 1) * (8) + (border * 2) + 1, h * nl + (border * 2) + 1);
            return d;
        }

        public void paint(Graphics g) {
            Dimension d = getMinimumSize();
            g.setColor(new Color(180,180,180));
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);
            g.setFont(font);

            int ibegin = getBegin();
            int iline = ibegin + getLines();
            int y = 0;
            for (int n = ibegin; n < iline; n++) {
                if (n == (cursor / 16)) {
                    drawCharRect(g, 0, y, 8);
                }
                String s = "0000000000000" + Integer.toHexString(n);
                s = s.substring(s.length() - 8);
                printString(g, s, 0, y++);
            }
        }
    }
}
