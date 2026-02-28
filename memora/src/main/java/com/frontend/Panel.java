package com.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networking.client.Client;
import com.util.SimpleButton;

public class Panel extends JPanel implements ActionListener, MouseWheelListener {
    // public static ArrayList<Response> responses = new ArrayList<>();
    // public static ArrayList<Bubble> bubbles = new ArrayList<>();

    public class Pan {
        // public ArrayList<Response> responses = new ArrayList<>();
        // public ArrayList<Bubble> bubbles = new ArrayList<>();
        public ArrayList<JComponent> components = new ArrayList<>();
        public int scrollY = 0;
        public int statY = 100;
        public String chatID;

        public Pan(String c) {
            chatID = c;
        }


        public void add(JComponent comp){
            components.add(comp);
        }
        // public ArrayList<Response> getResponses() {
        //     return responses;
        // }
    }

    private ArrayList<SimpleButton> buttons = new ArrayList<>();
    private JTextArea promptArea = new JTextArea();
    private AffineTransform spec = new AffineTransform();

    static Font customFont;

    private Timer repaintTimer;

    public void addButton(SimpleButton button) {
        buttons.add(button);
        addMouseListener(button);
        addMouseMotionListener(button);

    }

    // public void addBubble(Bubble bubble) {
    // bubbles.add(bubble);
    // add(bubble);
    // }

    private double scaleX, scaleY;
    private double iscaleX, iscaleY;

    private static BufferedImage mainImage;
    private static BufferedImage topImage;

    public static int panIndex = 0;
    public static HashMap<Integer, String> convMap = new HashMap<>();
    public static JPanel pa = new JPanel();
    public static ArrayList<Pan> pans = new ArrayList<>();

    public static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public void changeChatTo(int index) {
        // bubbles = new ArrayList<>();
        // responses = new ArrayList<>();
        pa.removeAll();
        pa.setLayout(null);

        if (index < pans.size()) {

            panIndex = index;
            Pan p = pans.get(index);

            for (JComponent b : p.components) {
                pa.add(b);

            }
  
           // pa.revalidate();
            requestFocus();

            // for (Bubble b : bubbles) {
            // pa.add(b);

            // }
            // for (Response r : responses) {
            // pa.add(r);
            // }

        }
    }

    public void setNewChat() {
        // bubbles = new ArrayList<>();
        // responses = new ArrayList<>();
        pa.removeAll();
        pa.setLayout(null);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        String newChatID = sb.toString();

        pans.add(new Pan(newChatID));

        panIndex = pans.size() - 1;
        final int i = panIndex;
        SimpleButton button = new SimpleButton(() -> {
            changeChatTo(i);
        });
        button.setText("Chat: " + (panIndex+1));
        button.setBounds(20, 35 + 60 + (60 * panIndex), 200, 50);

        addButton(button);
        // revalidate();
        pa.revalidate();
        requestFocus();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        spec = new AffineTransform();

        spec.translate(-10, -12);

        spec.scale(iscaleX+0.01, iscaleY+0.01);

        g2.drawImage(mainImage, spec, null);

        //g2.drawRect((int) (700 * scaleX), (int) (1350 * scaleY), (int) (1300 * scaleX), (int) (100 * scaleY));

        g2.drawImage(topImage, spec, null);
        for (SimpleButton button : buttons) {
            button.draw(g2);
        }
        // g2.drawImage(topImage, spec, null);

        // for (SimpleButton button : buttons) {
        // button.draw(g2);
        // }

    }

    public Panel(Client client) {
        System.out.println("Panel.<init> start");
        setLayout(null);

        setNewChat();

        pa.setBounds(350, 100, 1200, 600);
        pa.setBackground(new Color(0, 0, 0, 0));

        System.out.println("Panel: setNewChat done");
        try {

            SimpleButton newChatButton = new SimpleButton(() -> {
                System.out.println("create a new chat");
                setNewChat();
            });
            newChatButton.setBounds(20, 35, 200, 50);
            newChatButton.setText("New Chat");

            addButton(newChatButton);

            // setLayout(null);
            // setBounds(0, 0, (int) Main.screenSize.getWidth(), (int)
            // Main.screenSize.getHeight());
            // setPreferredSize(Main.screenSize);
            setSize(Main.screenSize);

            customFont = Font.createFont(Font.TRUETYPE_FONT, Panel.class.getResourceAsStream("/fonts/f.ttf"))
                    .deriveFont(40f);

            mainImage = ImageIO.read(getClass().getResourceAsStream("/im/mem.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mainImage != null) {
            iscaleX = Main.screenSize.getWidth() / (double) mainImage.getWidth();
            iscaleY = Main.screenSize.getHeight() / (double) mainImage.getHeight();
        } else {
            // fallback scale if image failed to load
            iscaleX = Main.screenSize.getWidth() / 2560.0;
            iscaleY = Main.screenSize.getHeight() / 1600.0;
        }

        scaleX = Main.screenSize.getWidth() / 2560;
        scaleY = Main.screenSize.getHeight() / 1600;

        initGraphics(client);
        System.out.println("Panel: initGraphics done");
    }

    public void initGraphics(Client client) {

        promptArea.setBounds((int) (700 * scaleX), (int) (1324 * scaleY), (int) (1615 * scaleX), (int) (180 * scaleY));
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
                        // pans.get(panIndex).bubbles.add(bub);
                        // pans.get(panIndex).add(bub);
                        // bubbles.add(bub);
                        pans.get(panIndex).add(bub);
                        pa.add(bub);
                        revalidate();
                        promptArea.setText("");
                        try {
                            client.sendPrompt(text, pans.get(panIndex).chatID);
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
        add(pa);
        addMouseWheelListener(this);

        // Main.frame.setLayout(null);

        // setVisible(true);

        // start repaint timer after components and resources are initialized
        repaintTimer = new Timer(33, this);
        repaintTimer.start();
    }

    public static void drawResponse(String response) {
        Response res = new Response(response);

        //pans.get(panIndex).getResponses().add(res);
        pans.get(panIndex).add(res);

        // responses.add(res);
        pa.add(res);
        // System.out.println("res stats: y: " +res.getY()+" x: "+res.getX());
        // pa.revalidate();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();

        // for (Bubble bubble : pans.get(panIndex).bubbles) {
        //     bubble.update();
        // }
        // for (Response response : pans.get(panIndex).responses) {
        //     response.update();
        // }
        for(Component j:pa.getComponents()){

            if(j instanceof Response ){
                ((Response)j).update();
            }else if(j instanceof Bubble){
                ((Bubble)j).update();
            }

        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            Panel.pans.get(Panel.panIndex).scrollY += 20;
        } else {
            Panel.pans.get(Panel.panIndex).scrollY -= 20;
        }
    }
}
