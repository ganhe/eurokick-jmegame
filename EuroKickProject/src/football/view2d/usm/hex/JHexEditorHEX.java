package football.view2d.usm.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA. User: laullon Date: 09-abr-2003 Time: 12:47:32
 */
public class JHexEditorHEX extends JComponent implements MouseListener, KeyListener {

    private JHexEditor hexEditor;
    private int cursor = 0;

    public JHexEditorHEX(JHexEditor he) {
        this.hexEditor = he;
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(he);
    }

    public Dimension getPreferredSize() {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMaximumSize() {
        debug("getMaximumSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        debug("getMinimumSize()");

        Dimension d = new Dimension();
        FontMetrics fn = getFontMetrics(hexEditor.font);
        int h = fn.getHeight();
        int nl = hexEditor.getLines();
        d.setSize(((fn.stringWidth(" ") + 1) * +((16 * 3) - 1)) + (hexEditor.border * 2) + 1, h * nl + (hexEditor.border * 2) + 1);
        return d;
    }

    public void paint(Graphics g) {
        debug("paint(" + g + ")");
        //debug("cursor=" + hexEditor.cursor + " buff.length=" + hexEditor.buff.length);

        if (hexEditor.buff != null) {
            Dimension d = getMinimumSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(Color.black);

            g.setFont(hexEditor.font);

            int ini = hexEditor.getBegin() * 16;
            int fin = ini + (hexEditor.getLines() * 16);
            if (fin > hexEditor.buff.length) {
                fin = hexEditor.buff.length;
            }

            //datos hex
            int x = 0;
            int y = 0;
            for (int n = ini; n < fin; n++) {
                if (n == hexEditor.cursor) {
                    if (hasFocus()) {
                        g.setColor(Color.black);
                        hexEditor.fillCharRect(g, (x * 3), y, 2);
                        g.setColor(Color.blue);
                        hexEditor.fillCharRect(g, (x * 3) + cursor, y, 1);
                    } else {
                        g.setColor(Color.blue);
                        hexEditor.drawCharRect(g, (x * 3), y, 2);
                    }

                    if (hasFocus()) {
                        g.setColor(Color.white);
                    } else {
                        g.setColor(Color.black);
                    }
                } else {
                    g.setColor(Color.black);
                }

                String s = ("0" + Integer.toHexString(hexEditor.buff[n]));
                s = s.substring(s.length() - 2);
                hexEditor.printString(g, s, ((x++) * 3), y);
                if (x == 16) {
                    x = 0;
                    y++;
                }
            }
        }
    }

    private void debug(String s) {
        if (hexEditor.DEBUG) {
            //System.out.println("JHexEditorHEX ==> " + s);
        }
    }

    // calculate position
    public int calculateCursorPosition(int x, int y) {
        FontMetrics fn = getFontMetrics(hexEditor.font);
        x = x / ((fn.stringWidth(" ") + 1) * 3);
        y = y / fn.getHeight();
        //debug("x=" + x + " ,y=" + y);
        return x + ((y + hexEditor.getBegin()) * 16);
    }

    // mouselistener
    public void mouseClicked(MouseEvent e) {
        debug("mouseClicked(" + e + ")");
        hexEditor.setCursorPos(calculateCursorPosition(e.getX(), e.getY()));
        this.requestFocus();
        hexEditor.repaint();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    //KeyListener
    public void keyTyped(KeyEvent e) {
        debug("keyTyped(" + e + ")");

        char c = e.getKeyChar();
        if (hexEditor.buff != null) {
            if (((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F')) || ((c >= 'a') && (c <= 'f'))) {
                char[] str = new char[2];
                String typeInStr = "00" + Integer.toHexString((int) hexEditor.buff[hexEditor.cursor]);
                if (typeInStr.length() > 2) {
                    typeInStr = typeInStr.substring(typeInStr.length() - 2);
                }
                str[1 - cursor] = typeInStr.charAt(1 - cursor);
                str[cursor] = e.getKeyChar();
                hexEditor.buff[hexEditor.cursor] = (byte) Integer.parseInt(new String(str), 16);

                if (cursor != 1) {
                    cursor = 1;
                } else if (hexEditor.cursor != (hexEditor.buff.length - 1)) {
                    hexEditor.cursor++;
                    cursor = 0;
                }
                hexEditor.updateCursor();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        debug("keyPressed(" + e + ")");
        hexEditor.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        debug("keyReleased(" + e + ")");
    }

    @Override
    public boolean isFocusTraversable() {
        return true;
    }
}
