package com.frontend;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.FontMetrics;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.Arrays;
import java.util.List;

public class Response extends JEditorPane {

    public void createMarkdownComponent(String markdown) {
        // Convert Markdown → HTML (enable GFM tables)
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        String html = renderer.render(parser.parse(markdown));

        // Basic HTML wrapper (inherits background from Swing)
        String font = "Verdana";
        int size = 18;
        int maxWidth = 800;
        String color = "#ffffff"; // or "red", "rgb(0,128,0)", etc.

        String styledHtml = """
                <html>
                <head>
                <style>
                    table {border-collapse: collapse;}
                    table, th, td {border:1px solid #999; padding:0px;}
                    body { max-width: %dpx; word-wrap: break-word; color: %s; }
                </style>
                </head>
                <body style='font-family:%s; font-size:%dpx; margin:8px; color: %s;'>
                """.formatted(maxWidth, color, font, size, color)
                + html +
                """
                        </body>
                        </html>
                        """;
        setContentType("text/html");
        setText(styledHtml);
        setEditable(false);

        // enforce max width when determining preferred size
        setSize(maxWidth, Short.MAX_VALUE);
        Dimension d = getPreferredSize();
        if (d.width > maxWidth) {
            d.width = maxWidth;
            setPreferredSize(d);
        }

        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setCaretPosition(0);

    }

    private int width, height, x = 1, y;

    public Response(String response) {
        super();
        createMarkdownComponent(response);

        // keep width within bounds and update layout
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;
        setBounds(x, Bubble.statY, width, height);
        setBackground(new Color(0, 0, 0, 0));
        y = Bubble.statY;
        Bubble.statY += height;

    }

    // private void resizeToFit() {
    // x = 400;
    // y = Bubble.statY;
    // int maxWidth = 700;
    // FontMetrics fm = getFontMetrics(getFont());
    // String text = getText().isEmpty() ? " " : getText();

    // int textWidth = fm.stringWidth(text);

    // width = Math.min(textWidth + 10, maxWidth);

    // setBounds(x, y, width, Short.MAX_VALUE);
    // height = getPreferredSize().height;

    // setBounds(x, y, width, height);

    // setPreferredSize(new Dimension(width, height));

    // revalidate();
    // Bubble.statY += height;

    // }

    public void update() {

        setLocation(x, y + Bubble.scrollY);
        // if (y + height > 700) {

        // setBounds(x, y+ Bubble.scrollY, getWidth(), height - ((y + height) - 700));
        // revalidate();

        // } else {
        // setBounds(x, y+ Bubble.scrollY, getWidth(), height);
        // revalidate();

        // }
    }

}
