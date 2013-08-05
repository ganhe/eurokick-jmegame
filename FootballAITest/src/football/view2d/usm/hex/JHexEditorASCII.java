package football.view2d.usm.hex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA. User: laullon Date: 09-abr-2003 Time: 12:47:18
 */
public class JHexEditorASCII extends JComponent implements MouseListener, KeyListener {

    private JHexEditor hexEditor;

    public JHexEditorASCII(JHexEditor he) {
        this.hexEditor = he;
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(he);
    }

    public Dimension getPreferredSize() {
        debug("getPreferredSize()");
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        debug("getMinimumSize()");

        Dimension d = new Dimension();
        FontMetrics fn = getFontMetrics(hexEditor.font);
        int h = fn.getHeight();
        int nl = hexEditor.getLines();
        d.setSize((fn.stringWidth(" ") + 1) * (16) + (hexEditor.border * 2) + 1, h * nl + (hexEditor.border * 2) + 1);
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

            //data ascii
            int ini = hexEditor.getBegin() * 16;
            int fin = ini + (hexEditor.getLines() * 16);
            if (fin > hexEditor.buff.length) {
                fin = hexEditor.buff.length;
            }

            int x = 0;
            int y = 0;
            for (int n = ini; n < fin; n++) {
                if (n == hexEditor.cursor) {
                    g.setColor(Color.blue);
                    if (hasFocus()) {
                        hexEditor.fillCharRect(g, x, y, 1);
                    } else {
                        hexEditor.drawCharRect(g, x, y, 1);
                    }
                    if (hasFocus()) {
                        g.setColor(Color.white);
                    } else {
                        g.setColor(Color.black);
                    }
                } else {
                    g.setColor(Color.black);
                }

                String s = "" + new Character((char) hexEditor.buff[n]);
                if ((hexEditor.buff[n] < 20) || (hexEditor.buff[n] > 126)) {
                    s = "" + (char) 16;
                }
                hexEditor.printString(g, s, (x++), y);
                if (x == 16) {
                    x = 0;
                    y++;
                }
            }
        }
    }

    private void debug(String s) {
        if (hexEditor.DEBUG) {
            System.out.println("JHexEditorASCII ==> " + s);
        }
    }

    // calcular la posicion del raton
    public int calcularPosicionRaton(int x, int y) {
        FontMetrics fn = getFontMetrics(hexEditor.font);
        x = x / (fn.stringWidth(" ") + 1);
        y = y / fn.getHeight();
        debug("x=" + x + " ,y=" + y);
        return x + ((y + hexEditor.getBegin()) * 16);
    }

    // mouselistener
    public void mouseClicked(MouseEvent e) {
        debug("mouseClicked(" + e + ")");
        hexEditor.setCursorPos(calcularPosicionRaton(e.getX(), e.getY()));
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
        if (hexEditor.buff != null) {
            hexEditor.buff[hexEditor.cursor] = (byte) e.getKeyChar();

            if (hexEditor.cursor != (hexEditor.buff.length - 1)) {
                hexEditor.cursor++;
            }
            hexEditor.repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        debug("keyPressed(" + e + ")");
        hexEditor.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
        debug("keyReleased(" + e + ")");
    }

    public boolean isFocusTraversable() {
        return true;
    }
}
