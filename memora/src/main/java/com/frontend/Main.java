package com.frontend;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static JFrame frame = new JFrame("Memora");

    static {
        frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

    private static void fullscreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported())
            gd.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Panel panel = new Panel();
            frame.add(panel);
            frame.setVisible(true);
            fullscreen();
        });

    }
}