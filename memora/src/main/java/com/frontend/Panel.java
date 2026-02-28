package com.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.*;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import com.util.SimpleButton;

public class Panel extends JPanel implements ActionListener {

    private ArrayList<SimpleButton> buttons = new ArrayList<>();

    private JTextArea promptArea = new JTextArea();
    private AffineTransform spec = new AffineTransform();

    private static Font customFont;

    public void addButton(SimpleButton button) {
        buttons.add(button);
        addMouseListener(button);
        addMouseMotionListener(button);
    }

    private double scaleX, scaleY;
    private double iscaleX, iscaleY;

    private static BufferedImage mainImage;

    public Panel() {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT,Panel.class.getResourceAsStream("/fonts/f.otf")).deriveFont(40f);
            // customFont = new Font("Arial", Font.PLAIN, 90);
            mainImage = ImageIO.read(getClass().getResourceAsStream("/im/mem.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        iscaleX = Main.screenSize.getWidth()  / mainImage.getWidth();
        iscaleY = Main.screenSize.getHeight() / mainImage.getHeight();

        scaleX = Main.screenSize.getWidth() /2560;
        scaleY = Main.screenSize.getHeight() /1600;

        initGraphics();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        spec = new AffineTransform();
        
        spec.translate(0, 0);

        spec.scale(iscaleX, iscaleY);
 
        g2.drawImage(mainImage, spec, null);

        g2.drawRect((int)(700*scaleX), (int)(1350*scaleY), (int)(1300*scaleX), (int)(100*scaleY));
        // for (SimpleButton button : buttons) {
        //     button.draw(g2);
        // }

    }

    public void initGraphics() {

        setLayout(null);

        promptArea.setBounds((int)(700*scaleX), (int)(1350*scaleY), (int)(1300*scaleX), (int)(100*scaleY));
        promptArea.setOpaque(false);
        promptArea.setBackground(new Color(0, 0, 0, 0));
        promptArea.setForeground(Color.WHITE);
        promptArea.setCaretColor(Color.WHITE);

        if (customFont != null) {
            promptArea.setFont(customFont);
        }
        // promptArea.setEditable(false);
        promptArea.setWrapStyleWord(true);
        promptArea.setLineWrap(true);        
        add(promptArea);

        // start repaint timer after components and resources are initialized
        Timer t = new Timer(33, this);
        t.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
