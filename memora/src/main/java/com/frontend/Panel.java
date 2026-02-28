package com.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TextArea;
import java.awt.color.*;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.FontMetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networking.client.Client;
import com.util.SimpleButton;

public class Panel extends JPanel implements ActionListener {

    private ArrayList<SimpleButton> buttons = new ArrayList<>();
    private ArrayList<Bubble> bubbles = new ArrayList<>();

    private JTextArea promptArea = new JTextArea();
    private AffineTransform spec = new AffineTransform();

    static Font customFont;

    public void addButton(SimpleButton button) {
        buttons.add(button);
        addMouseListener(button);
        addMouseMotionListener(button);
    }

    public void addBubble(Bubble bubble) {
        bubbles.add(bubble);
        add(bubble);
    }

    private double scaleX, scaleY;
    private double iscaleX, iscaleY;

    private static BufferedImage mainImage;
    private static Panel me;

    public Panel(Client client){
        me = this;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, Panel.class.getResourceAsStream("/fonts/f.otf"))
                    .deriveFont(40f);
            mainImage = ImageIO.read(getClass().getResourceAsStream("/im/mem.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        iscaleX = Main.screenSize.getWidth() / mainImage.getWidth();
        iscaleY = Main.screenSize.getHeight() / mainImage.getHeight();

        scaleX = Main.screenSize.getWidth() / 2560;
        scaleY = Main.screenSize.getHeight() / 1600;

        initGraphics(client);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        spec = new AffineTransform();

        spec.translate(0, 0);

        spec.scale(iscaleX, iscaleY);

        g2.drawImage(mainImage, spec, null);

        g2.drawRect((int) (700 * scaleX), (int) (1350 * scaleY), (int) (1300 * scaleX), (int) (100 * scaleY));
        for (Bubble bubble : bubbles) {
            // bubble.scroll(true);
        }
        // for (SimpleButton button : buttons) {
        // button.draw(g2);
        // }

    }

    public void initGraphics(Client client) {

        setLayout(null);

        promptArea.setBounds((int) (700 * scaleX), (int) (1350 * scaleY), (int) (1300 * scaleX), (int) (100 * scaleY));
        promptArea.setOpaque(false);
        promptArea.setBackground(new Color(0, 0, 0, 0));
        promptArea.setForeground(Color.WHITE);
        promptArea.setCaretColor(Color.WHITE);

        promptArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    evt.consume();
                    String text = promptArea.getText().trim();
                    if (!text.isEmpty()) {
                        addBubble(new Bubble(text));
                        promptArea.setText("");
                        try {
                            client.sendPrompt(text);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

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

    public static void drawResponse(String response) {
        JTextArea temp = new JTextArea(response);

        temp.setLineWrap(true);
        temp.setBackground(new Color(0,0,0,0));
        temp.setText(response);
        temp.setFont(Panel.customFont.deriveFont(30f));
        temp.setBounds(500, Bubble.statY, 10, 10);
        temp.setEditable(false);
        temp.setWrapStyleWord(true);
        // temp.setForeground(Color.WHITE);
        // temp.setSize(500, Short.MAX_VALUE);
        resizeToFit(temp);
        
        
        // g2.setColor(Color.GRAY);

    }

    private static void resizeToFit(JTextArea area) {
         int width, height, x = 400, y = Bubble.statY, maxWidth = 700;
        FontMetrics fm = area.getFontMetrics(area.getFont());
        String text = area.getText().isEmpty() ? " " : area.getText();

        int textWidth = fm.stringWidth(text);

        width = Math.min(textWidth + 10, maxWidth);

        area.setBounds(x, y, width, Short.MAX_VALUE);
        height = area.getPreferredSize().height;

        area.setBounds(x, y, width, height);

        area.setPreferredSize(new Dimension(width, height));

        area.revalidate();
        me.add(area);
        Bubble.statY += height + 60;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
