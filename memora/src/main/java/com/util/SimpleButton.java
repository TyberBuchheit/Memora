package com.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimpleButton extends MouseAdapter {

    private Rectangle bounds;
    private boolean hover;
    private Runnable target;
    private String text;


    public void setText(String text) {
        this.text = text;
    }

    public SimpleButton(Runnable run) {
        this.target = run;
        hover = false;
    }

    public void setBounds(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2) {

        if (hover) {
            g2.setColor(java.awt.Color.GRAY);
        } else {
            g2.setColor(java.awt.Color.LIGHT_GRAY);
        }
        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.drawString(text, bounds.x + 10, bounds.y + 20);

    }

    public void mouseMoved(MouseEvent e) {
        if (bounds.contains(e.getPoint())) {
            hover = true;
        } else {
            hover = false;
        }
    }

    public void mouseClicked(MouseEvent e) {
        System.out.println("Button clicked!");

        if (hover) {
            target.run();
        }

    }
}
