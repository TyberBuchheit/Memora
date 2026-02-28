package com.frontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.networking.client.Client;

public class Main {

    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static JFrame frame = new JFrame("Memora");
    private static Client c;

    static {
        frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(null);
    }

    private static void fullscreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported())
            gd.setFullScreenWindow(frame);
    }

    public static void main(String[] args) {
        System.out.println("connecting to server...");
        try {
           c = new Client();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            frame.add(new Panel(c));
            frame.setVisible(true);
            fullscreen();
        });

    }
}