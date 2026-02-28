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
    private static BufferedImage mainImage;

    public Panel() {
        initGraphics();

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT,Panel.class.getResourceAsStream("/fonts/f.otf")).deriveFont(90f);
            // customFont = new Font("Arial", Font.PLAIN, 90);
            mainImage = ImageIO.read(getClass().getResourceAsStream("/im/mem.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        scaleX = Main.screenSize.getWidth() / mainImage.getWidth();
        scaleY = Main.screenSize.getHeight() / mainImage.getHeight();


    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        spec = new AffineTransform();
        
        spec.translate(0, 0);

        spec.scale(scaleX, scaleY);
 
        g2.drawImage(mainImage, spec, null);

         //g2.drawRect(700, 1400, 1000, 100);
        // for (SimpleButton button : buttons) {
        //     button.draw(g2);
        // }

    }

    public void initGraphics() {

        setLayout(null);

        promptArea.setBounds((int)(700*scaleX), (int)(1400*scaleY), (int)(1000*scaleX), (int)(100*scaleY));
        promptArea.setBackground(new Color(0, 0, 0, 0));
    
        promptArea.setFont(customFont);
        promptArea.setCaretColor(Color.WHITE);
        // promptArea.setEditable(false);
        promptArea.setWrapStyleWord(true);
        promptArea.setLineWrap(true);        
        add(promptArea);

        Timer t = new Timer(33, this);
        t.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
