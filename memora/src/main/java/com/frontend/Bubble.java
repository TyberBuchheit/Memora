package com.frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JTextArea;
import java.awt.FontMetrics;

public class Bubble extends JTextArea {
    static int statY = 200;
    private int width, height, x = 1200, y;
    private static final int maxWidth = 500;

    public Bubble(String text) {
        super(text);

        setLineWrap(true);
        setBackground(Color.GRAY);
        setText(text);
        setFont(Panel.customFont.deriveFont(30f));
        // setBounds(x, y=statY, 10, 10);
        setEditable(false);
        setWrapStyleWord(true);
        y=statY;
        resizeToFit();
        statY += height + 60;

    }

    public void addStatY(int delta) {
        statY += delta;
        
    }

    public void scroll(boolean up) {
        if (up) {
            y -= 5;
        } else {
            y += 5;
        }

    }

    private void resizeToFit() {
        FontMetrics fm = getFontMetrics(getFont());
        String text = getText().isEmpty() ? " " : getText();

        int textWidth = fm.stringWidth(text);

        width = Math.min(textWidth + 10, maxWidth);

        setBounds(x, y, width, Short.MAX_VALUE);
        height = getPreferredSize().height;

        setBounds(x, y, width, height);

        setPreferredSize(new Dimension(width, height));

        revalidate();
    }

}
