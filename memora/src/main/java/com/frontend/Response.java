package com.frontend;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.Arrays;
import java.util.List;

public class Response extends JEditorPane implements Cloneable{

    public void createMarkdownComponent(String markdown) {
        List<Extension> extensions = Arrays.asList(TablesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        String html = renderer.render(parser.parse(markdown));

        String font = "Verdana";
        int size = 18;
        int maxWidth = 800;
        String color = "#ffffff";

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

    private int y;
    private static final int x = 1;

    public Response(String response) {
        super();
        createMarkdownComponent(response);
  
        // keep width within bounds and update layout
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;
        setBounds(x, Panel.pans.get(Panel.panIndex).statY, width, height);
        setBackground(new Color(0, 0, 0, 0));
        y = Panel.pans.get(Panel.panIndex).statY;
        Panel.pans.get(Panel.panIndex).statY += height;

    }
    public Response(Response r) {
        super();
        createMarkdownComponent(r.getText());
        int width = getPreferredSize().width;
        int height = getPreferredSize().height;
        setBounds(x, Panel.pans.get(Panel.panIndex).statY, width, height);
        setBackground(new Color(0, 0, 0, 0));
        y = r.y;
        //Panel.pans.get(Panel.panIndex).statY += height;

    }
    public void update() {

        setLocation(x, y + Panel.pans.get(Panel.panIndex).scrollY);

    }

}
