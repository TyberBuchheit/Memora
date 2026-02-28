package com.frontend;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextArea;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Bubble extends JTextArea implements Cloneable{
    // static int statY = 200-100;
   // static int actY = 200;
    private static final int x = 1100-300;

    private int width, height, y;
    private static final int maxWidth = 200;

    // public static int scrollY = 0;

    public Bubble(String text) {
        super(text);

        setLineWrap(true);
        setBackground(new Color(0,0,0,0));
        setOpaque(false);
        setMargin(new Insets(8,8,8,8));
        setForeground(Color.WHITE);
        setText(text);
        setFont(Panel.customFont.deriveFont(35f));
        setEditable(false);
        setWrapStyleWord(true);

        y = Panel.pans.get(Panel.panIndex).statY;

        resizeToFit();
        Panel.pans.get(Panel.panIndex).statY += height;

        System.out.println("x: "+x+" y: "+y+" w: "+width+" height: "+height);

    }

        public Bubble(Bubble b) {
        super(b.getText());


        setLineWrap(true);
        setBackground(new Color(0,0,0,0));
        setOpaque(false);
        setMargin(new Insets(8,8,8,8));
        setForeground(Color.WHITE);
        setText(b.getText());
        setFont(Panel.customFont.deriveFont(35f));
        setEditable(false);
        setWrapStyleWord(true);

        // y = b.newy+(b.y-newy);
        // y=b.y+Panel.pans.get(Panel.panIndex).scrollY+Panel.pans.get(Panel.panIndex).scrollY;
        y = Panel.pans.get(Panel.panIndex).statY;

        width = b.width;
        height = b.height;
       
        System.out.println("x: "+x+" y: "+y+" w: "+width+" height: "+height);

        // resizeToFit();
        // Panel.pans.get(Panel.panIndex).statY += height;


    }
    private int newy;
    public void update() {
        // always use our stored x value; other components shouldn't be able to shift us
        newy = y + Panel.pans.get(Panel.panIndex).scrollY;
        setLocation(x, newy);
    }

    public void scroll(boolean up) {

    }

    private void resizeToFit() {

        FontMetrics fm = getFontMetrics(getFont());
        String text = getText().isEmpty() ? " " : getText();

        int textWidth = fm.stringWidth(text);

        Insets m = getMargin();
        int hPad = m.left + m.right + 6;
        width = Math.min(textWidth + hPad, maxWidth);

        setBounds(x, y, width, Short.MAX_VALUE);
        height = getPreferredSize().height;

        setBounds(x, y, width, height);

        setPreferredSize(new Dimension(width, height));

        revalidate();
    }



    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        } finally {
            g2.dispose();
        }
        // setBounds(getX()+5, getY(), getWidth(), getHeight());
        super.paintComponent(g);
        // setBounds(getX()-5, getY(), getWidth(), getHeight());


    }

}
