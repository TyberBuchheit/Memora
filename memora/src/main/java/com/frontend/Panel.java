package com.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networking.client.Client;
import com.util.SimpleButton;

public class Panel extends JPanel implements ActionListener, MouseWheelListener {

    private ArrayList<SimpleButton> buttons = new ArrayList<>();
    public static ArrayList<Bubble> bubbles = new ArrayList<>();
    private static ArrayList<Response> responses = new ArrayList<>();
    private JTextArea promptArea = new JTextArea();
    private AffineTransform spec = new AffineTransform();

    static Font customFont;

    public void addButton(SimpleButton button) {
        buttons.add(button);
        addMouseListener(button);
        addMouseMotionListener(button);

    }

    // public void addBubble(Bubble bubble) {
    //     bubbles.add(bubble);
    //     add(bubble);
    // }

    private double scaleX, scaleY;
    private double iscaleX, iscaleY;

    private static BufferedImage mainImage;
    private static BufferedImage topImage;

    private static JPanel me;


    public static JPanel pan = new JPanel();

    public Panel(Client client) {
        me = pan;
        try {

            SimpleButton newChatButton = new SimpleButton(()->{
                System.out.println("create a new chat");
            });
            newChatButton.setBounds(10, 10, 200, 50);
            newChatButton.setText("New Chat");

            addButton(newChatButton);

            setLayout(null);
            setBounds(0,0, (int) Main.screenSize.getWidth(), (int) Main.screenSize.getHeight());
            // setSize(Main.screenSize);
            pan.setBounds(350, 100, 1200, 650);
            pan.setBackground(new Color(0,0,0,0));

            customFont = Font.createFont(Font.TRUETYPE_FONT, Panel.class.getResourceAsStream("/fonts/f.otf"))
                    .deriveFont(40f);

            mainImage = ImageIO.read(getClass().getResourceAsStream("/im/mem.png"));

            add(pan);

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

        g2.drawImage(topImage, spec, null);
        // g2.drawImage(topImage, spec, null);

        // for (SimpleButton button : buttons) {
        // button.draw(g2);
        // }

    }

    public void drawFront(Graphics2D g2) {

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
                        Bubble bub = new Bubble(text);
                        bubbles.add(bub);
                        pan.add(bub);
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
        addMouseWheelListener(this);
        // start repaint timer after components and resources are initialized
        Timer t = new Timer(33, this);
        t.start();
    }

    public static void drawResponse(String response) {
        Response res = new Response(response);
        responses.add(res);

        me.add(res);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        for (Bubble bubble : bubbles) {
            bubble.update();
        }
        for (Response response : responses) {
            response.update();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            Bubble.scrollY += 20;
        } else {
            Bubble.scrollY -= 20;
        }
    }
}
