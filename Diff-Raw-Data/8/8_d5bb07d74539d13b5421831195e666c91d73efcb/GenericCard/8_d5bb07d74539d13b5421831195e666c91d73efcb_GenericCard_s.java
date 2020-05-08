 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tcg.generator.cards;
 
 import com.tcg.generator.cards.reflect.Card;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.tcg.generator.config.ConfigHolder;
 import com.tcg.generator.layouts.CardFont;
 import com.tcg.generator.layouts.CardLayout;
 import com.tcg.generator.layouts.ElementLayout;
 import com.tcg.generator.layouts.ElementMapping;
 import com.tcg.generator.layouts.Resource;
 import com.text.formatted.elements.BoldTextInsert;
 import com.text.formatted.elements.ImageInsert;
 import com.text.formatted.elements.ItalicTextInsert;
 import com.text.formatted.elements.MarkupElement;
 import com.text.formatted.elements.MixedMediaText;
 import com.text.formatted.elements.TextInsert;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Michael
  */
 public class GenericCard {
     private LinkedHashMap<String, Object> cardData;
     public String cardName = "Unknown";
     protected CardLayout cardLayout;
     protected BufferedImage artwork;
     protected ObjectMapper mapper = new ObjectMapper();
     
     public GenericCard() {}
     
     public GenericCard(String name, String artworkFile, CardLayout layout, LinkedHashMap<String, Object> cardData) {
         this.cardData = cardData;
         
         setCardName(name);
         setLayout(layout);
         setArtwork(new File(artworkFile));
     }
     
     public GenericCard(String name, String artworkFile, String layoutFile, LinkedHashMap<String, Object> cardData) {
         this.cardData = cardData;
         
         setCardName(name);
         setLayout(layoutFile);
         setArtwork(new File(artworkFile));
     }
     
     public final GenericCard setCardName(String cardName) {
         this.cardName = cardName;
         return this;
     }
     
     public final GenericCard setLayout(CardLayout layout) {
         this.cardLayout = layout;
         return this;
     }
     
     public final GenericCard setLayout(File layoutFile) {
         try {
             mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
             this.cardLayout = mapper.readValue(layoutFile, CardLayout.class);
         } catch (IOException ex) {
             Logger.getLogger(Card.class.getName()).log(Level.SEVERE, null, ex);
         }
         return this;
     }
     
     public final GenericCard setLayout(String jsonData) {
         try {
             mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
             this.cardLayout = mapper.readValue(jsonData, CardLayout.class);
         } catch (IOException ex) {
             Logger.getLogger(Card.class.getName()).log(Level.SEVERE, null, ex);
         }
         return this;
     }
     
     public final GenericCard setArtwork(File artworkFile) {
         try {
             this.artwork = ImageIO.read(artworkFile);
            System.out.println("Successfully opened: " + artworkFile.getAbsolutePath());
         } catch (IOException ex) {
             System.out.println("Failed to open:      " + artworkFile.getAbsolutePath());
         }
         return this;
     }
     
     public final GenericCard setCardData(String jsonData) {
         try {
             this.cardData = mapper.readValue(jsonData, LinkedHashMap.class);
         } catch (IOException ex) {
             Logger.getLogger(GenericCard.class.getName()).log(Level.SEVERE, null, ex);
         }
         return this;
     }
     
     public final GenericCard setCardData(LinkedHashMap<String, Object> cardData) {
         this.cardData = cardData;
         return this;
     }
 
     public void draw(String outputDirectory) {
         String name = (String)((cardData.get("cardName") != null ? cardData.get("cardName") : cardName));
         draw(new File(outputDirectory + name + ".png"));
     }
     
     public void draw(File outputFile) {
         try {
             draw(new FileOutputStream(outputFile));
         } catch (FileNotFoundException ex) {
             System.out.println("Unable to find file!");
         }
     }
     
     public void draw(OutputStream outputStream) {
         BufferedImage bi = new BufferedImage(cardLayout.getWidth(), cardLayout.getHeight(), BufferedImage.TYPE_INT_ARGB);
         drawLayer(artwork, bi);
         
         ArrayList<MixedMediaText> mmtl;
         for (ElementLayout element: cardLayout.getElements()) {
             
             // Test to see if layer should be drawn
             if (!element.shouldDraw(cardData)) {
                 continue;
             }
             
             System.out.println("Drawing element: " + element.getName());
             
             drawLayer(element, bi);
             if (element.getMappings() != null) {
                 ArrayList<String> lines = new ArrayList<>();
                 for (Entry<String, ElementMapping> entry : element.getMappings().entrySet()) {
                     ElementMapping mapping =  entry.getValue();
                     String field = entry.getKey();
                     Object value;
                     if (entry.getKey().equals("cardName") && cardData.get(field) == null) {
                         value = cardName;
                     }
                     else {
                         value = cardData.get(field);
                     } 
 
                     if (value != null) {
                         System.out.println("\tFIELD: " +  field + " (" + value.getClass().getSimpleName() + ")");
 
                         if (mapping.getLabel() != null) {
                             System.out.println("\t\tLABEL: " + mapping.getLabel());
                         }
                         if (mapping.getType() != null) {
                             System.out.println("\t\tTYPE:  " + mapping.getType());
                         }
                         
                         ArrayList<String> list;
                         Integer intValue;
                         Float floatValue;
                         String stringValue;
 
                         String line = "";
                         
                         if (mapping.getLabel() != null) {
                             line += mapping.getLabel() + ": ";
                         }
                         
                         switch (value.getClass().getSimpleName()) {
                             case "String":
                                 stringValue = (String)value;
                                 line += stringValue.toString();
                                 lines.add(line);
                                 break;
                             case "Integer":
                                 intValue = (Integer)value;
                                 line += intValue.toString();
                                 lines.add(line);
                                 break;
                             case "Float":
                                 floatValue = (Float)value;
                                 line += floatValue.toString();
                                 lines.add(line);
                                 break;
                             case "ArrayList":
                                 list = (ArrayList<String>)value;
                                 for (String aLine : list) {
                                     lines.add(aLine);
                                 }
                                 break;
                             default:
                                 System.out.println("\t\tInvalid field type!");
                                 break;
                         }
                         
 
                     }
                 }
                 
                 System.out.println("\tLINES: " + lines);
                 
                 switch(element.getType()) {
                     case "text-box":
                         if (element.hasWordWrap()) {
                             mmtl = splitAndFitMixedText(bi, lines, element);
                             drawMixedMediaText(bi, mmtl, element);
                         } else {
                             drawText(bi, lines, element);
                         }
                         break;
                     case "table":
                         drawTableCols(bi, lines, 2, element);
                         break;
                     case "image":
                     case "layer":
                         break;
                     default:
                         break;
                 }
             }
         }
         
         try {
             ImageIO.write(bi, "png", outputStream);
         } catch (IOException ex) {
             System.out.println("Unable to write image!");
         }
     }
     
     // ImageWriter stuff
     protected ArrayList<String> splitAndFitText(BufferedImage target, ArrayList<String> lines, ElementLayout elementLayout) {
         ArrayList<String> returnLines = new ArrayList<>();
         
         for (String line : lines) {
             ArrayList<String> moreLines = splitAndFitText(target, line, elementLayout);
             returnLines.addAll(moreLines);
         }
         
         return returnLines;
     }
     
     protected ArrayList<String> splitAndFitText(BufferedImage target, String text, ElementLayout elementLayout) {
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setFont(elementLayout.getFont());
         
         FontMetrics fm = g.getFontMetrics();
         
         String[] words = text.split(" ");
         
         String line = "";
         String lastLine;
         String lastWord;
         ArrayList<String> lines = new ArrayList<>();
         
         boolean first = true;
         for (String word : words) {
             lastLine = line;
             lastWord = word;
             if (first) {
                 line += word;
                 first = false;
             } else {
                 line += " " + word;
             }
             
             int width = fm.getStringBounds(line, g).getBounds().width;
             
             if (width > elementLayout.getWidth() - elementLayout.getMarginX() * 2) {
                 lines.add(lastLine);
                 line = lastWord;
             }
         }
         
         lines.add(line);
         
         for (String element : lines) {
             System.out.println("LINE: " + element);
         }
         
         return lines;
     }
     
     protected ArrayList<MixedMediaText> splitAndFitMixedText(BufferedImage target, ArrayList<String> lines, ElementLayout elementLayout) {
         ArrayList<MixedMediaText> returnLines = new ArrayList<>();
         
         for (String line : lines) {
             ArrayList<MixedMediaText> moreLines = splitAndFitMixedText(target, new MixedMediaText(line), elementLayout);
             returnLines.addAll(moreLines);
         }
         
         return returnLines;
     }
     
     protected ArrayList<MixedMediaText> splitAndFitMixedText(BufferedImage target, MixedMediaText text, ElementLayout elementLayout) {
         Graphics2D g = target.createGraphics();
         g.setFont(elementLayout.getFont());
         
         FontMetrics fm = g.getFontMetrics();
         
         MixedMediaText line = new MixedMediaText();
         MarkupElement lastElement;
         MarkupElement lastIcon = null;
         String alteredLine = "";
         int iconWidth = 0;
         int width = 0;
         int lastWidth = 0;
         int formattedTextWidth = 0;
         
         ArrayList<MixedMediaText> lines = new ArrayList<>();
         
         MarkupElement element;
         int maxWidth = (elementLayout.getWidth() - elementLayout.getMarginX() * 2);
         
         while((element = text.next()) != null) {
             if (element instanceof ImageInsert) {
                 iconWidth += cardLayout.getResource(element.getText()).getWidth();
             } else if (element instanceof BoldTextInsert) {
                 formattedTextWidth += g.getFontMetrics(new Font(elementLayout.getCardFont().getFamily(), Font.BOLD, elementLayout.getCardFont().getSize())).getStringBounds(" " + element.getText(), g).getBounds().width;
             } else if (element instanceof ItalicTextInsert) {
                 formattedTextWidth += g.getFontMetrics(new Font(elementLayout.getCardFont().getFamily(), Font.ITALIC, elementLayout.getCardFont().getSize())).getStringBounds(" " + element.getText(), g).getBounds().width;
             }else {
                 alteredLine += " " + element.getText();
             }
             
             line.addElement(element);
             
             int textWidth = fm.getStringBounds(alteredLine, g).getBounds().width;
             lastWidth = width;
             width = textWidth + iconWidth + formattedTextWidth;
             
             if (width >= maxWidth) {
                 lastElement = line.popElement();
                 
                 if (line.peekElement() instanceof ImageInsert) {
                     lastIcon = line.popElement();
                 }
                 
                 System.out.println("LINE: " + line);
                 
                 line.setWidth(lastWidth);
                 lines.add(line);
                 
                 line = new MixedMediaText();
                 iconWidth = 0;
                 formattedTextWidth = 0;
                 alteredLine = "";
                                 
                 if (lastIcon != null) {
                     iconWidth += cardLayout.getResource(lastIcon.getText()).getWidth();
                     line.addElement(lastIcon);
                     lastIcon = null;
                 }
                 line.addElement(lastElement);
                 
                 if (lastElement instanceof ImageInsert) {
                     iconWidth += cardLayout.getResource(lastElement.getText()).getWidth();
                 } else if (lastElement instanceof BoldTextInsert) {
                     formattedTextWidth = g.getFontMetrics(new Font(elementLayout.getCardFont().getFamily(), Font.BOLD, elementLayout.getCardFont().getSize())).getStringBounds(" " + lastElement.getText(), g).getBounds().width;
                 } else if (lastElement instanceof ItalicTextInsert) {
                     formattedTextWidth = g.getFontMetrics(new Font(elementLayout.getCardFont().getFamily(), Font.ITALIC, elementLayout.getCardFont().getSize())).getStringBounds(" " + lastElement.getText(), g).getBounds().width;
                 } else {
                     alteredLine = lastElement.getText();
                 }
             }
         }
         System.out.println("LINE: " + line);
         lines.add(line);
         
         return lines;
     }
     
     protected void drawLayer(ElementLayout elementLayout, BufferedImage target) {
         if (elementLayout.getLayer() == null) {
             return;
         }
         
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         Float alpha = elementLayout.getTransparency().floatValue();
         
         int rule = AlphaComposite.SRC_OVER;
         Composite comp = AlphaComposite.getInstance(rule, alpha);
         g.setComposite(comp);
         
         g.drawImage(elementLayout.getLayer(), null, elementLayout.getX(), elementLayout.getY());
     }
     
     protected void drawLayer(BufferedImage layerImage, BufferedImage target) {
         if (layerImage == null) {
             return;
         }
         
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         g.drawImage(layerImage, null, 0, 0);
     }
     
     protected void drawText(BufferedImage target, ArrayList<String> lines, ElementLayout elementLayout) {
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setFont(elementLayout.getFont());
         g.setColor(elementLayout.getCardFont().getColor());
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         FontMetrics fm = g.getFontMetrics();
         
         int index = 0;
         int actualHeight = fm.getAscent() - fm.getDescent();
         int lineHeight = fm.getHeight();
         int totalHeight = lines.size() * actualHeight;
         for (String line: lines) {
             int lineWidth = (int)fm.getStringBounds(line, g).getBounds().getWidth();
             int startX, startY;
             
             switch (elementLayout.getAlign()) {
             case "right":
                 startX = elementLayout.getWidth() - elementLayout.getMarginX() - lineWidth;
                 break;
             case "center":
                 startX = (elementLayout.getWidth() - lineWidth)/2;
                 break;
             case "left":
             default:
                 startX = elementLayout.getMarginX();
                 break;
             }
             
             switch (elementLayout.getVAlign()) {
             case "center":
                 startY = (elementLayout.getHeight() - totalHeight)/2;
                 break;
             case "bottom":
                 startY = elementLayout.getHeight() - elementLayout.getMarginY() - totalHeight;
                 break;
             case "top":
             default:
                 startY = elementLayout.getMarginY();
                 break;
             }
 
             g.drawString(line, elementLayout.getX() + startX, (elementLayout.getY() + startY + actualHeight ) + (index * lineHeight));
             
             index++;
         }
     }
     
     protected void drawText(BufferedImage target, String text, ElementLayout elementLayout) {
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setFont(elementLayout.getFont());
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         FontMetrics fm = g.getFontMetrics();
         
         int actualHeight = fm.getAscent() - fm.getDescent();
         int lineWidth = (int)fm.getStringBounds(text, g).getBounds().getWidth();
         int startX, startY;
 
         switch (elementLayout.getAlign()) {
         case "right":
             startX = (elementLayout.getWidth() - elementLayout.getMarginX() - lineWidth);
             break;
         case "center":
             startX = elementLayout.getMarginX() + (elementLayout.getWidth() - lineWidth)/2;
             break;
         case "left":
         default:
             startX = elementLayout.getMarginX();
             break;
         }
         
         switch (elementLayout.getVAlign()) {
         case "center":
             startY = (elementLayout.getHeight() - actualHeight)/2;
             break;
         case "bottom":
             startY = elementLayout.getHeight() - elementLayout.getMarginY() - actualHeight;
             break;
         case "top":
         default:
             startY = elementLayout.getMarginY();
             break;
         }
         
         g.setColor(elementLayout.getCardFont().getColor());
         g.drawString(text, elementLayout.getX() + startX, elementLayout.getY() + startY + actualHeight);
     }
     
     protected void drawMixedMediaText(BufferedImage target, ArrayList<MixedMediaText> lines, ElementLayout elementLayout) {
         Graphics2D g = (Graphics2D) target.getGraphics();
         g.setFont(elementLayout.getFont());
         g.setColor(elementLayout.getCardFont().getColor());
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         FontMetrics fm = g.getFontMetrics();
         int actualHeight = fm.getAscent() - fm.getDescent();
         int lineHeight = fm.getHeight();
         int index = 0;
         
         for (MixedMediaText mmt: lines) {
             MarkupElement me;
         
             int offset = 0;
            int spaceWidth = 0;
             int textBottom = (elementLayout.getY() + elementLayout.getMarginY() + actualHeight) + (index * lineHeight);
             
             // Debugging lines
             if (ConfigHolder.getConfig("debug") != null) {
                 g.setColor(Color.GREEN);
                 g.drawLine(elementLayout.getX(), textBottom, elementLayout.getWidth(), textBottom);
                 g.setColor(Color.BLUE);
                 g.drawLine(elementLayout.getX(), textBottom - fm.getAscent(), elementLayout.getWidth(), textBottom - fm.getAscent());
                 g.setColor(Color.RED);
                 g.drawLine(elementLayout.getX(), textBottom - (fm.getAscent() - fm.getDescent()), elementLayout.getWidth(), textBottom - (fm.getAscent() - fm.getDescent()));
             }
             
             g.setColor(elementLayout.getCardFont().getColor());
             
             while ((me = mmt.next()) != null) {
                 int startX;
 
                 switch (elementLayout.getAlign()) {
                     case "right":
                         startX = (elementLayout.getWidth() - elementLayout.getMarginX() - mmt.getWidth());
                         break;
                     case "center":
                         startX = elementLayout.getMarginX() + (elementLayout.getWidth() - mmt.getWidth())/2;
                         break;
                     case "left":
                     default:
                         startX = elementLayout.getMarginX();
                         break;
                 }
                 
                 if (me instanceof TextInsert) {
                     g.setFont(elementLayout.getFont());
                     fm = g.getFontMetrics();
                     spaceWidth = fm.getStringBounds(" ", g).getBounds().width;
                     
                     g.drawString(me.getText(), elementLayout.getX() + startX + offset, textBottom);
                     offset += fm.getStringBounds(me.getText(), g).getBounds().width + spaceWidth;
                 } else if (me instanceof ImageInsert) {
                     Resource r = cardLayout.getResource(me.getText());
                     Integer larger = Math.max(r.getHeight(), actualHeight);
                     Integer diff = Math.abs(r.getHeight() - actualHeight);
                     Integer correction = (int)Math.round((double)diff/2.0);
                     Integer imageDelta = larger - correction;
                     g.drawImage(r.getImage(), null, elementLayout.getX() + startX + offset, (textBottom - imageDelta));
                     offset += r.getWidth() + spaceWidth;
                 } else if (me instanceof BoldTextInsert) {
                     CardFont cf = elementLayout.getCardFont();
                     Font f = new Font(cf.getFamily(), Font.BOLD, cf.getSize());
                    
                     g.setFont(f);
                     fm = g.getFontMetrics();
                    spaceWidth = 0;
                     
                     g.drawString(me.getText(), elementLayout.getX() + startX + offset, textBottom);
                     offset += fm.getStringBounds(me.getText(), g).getBounds().width + spaceWidth;
                 } else if (me instanceof ItalicTextInsert) {
                     CardFont cf = elementLayout.getCardFont();
                     Font f = new Font(cf.getFamily(), Font.ITALIC, cf.getSize());
                     
                     g.setFont(f);
                     fm = g.getFontMetrics();
                    spaceWidth = 0;
                     
                     g.drawString(me.getText(), elementLayout.getX() + startX + offset, textBottom);
                     offset += fm.getStringBounds(me.getText(), g).getBounds().width + spaceWidth;
                 }
             }
             index++;
         }
     }
     
     protected void drawTableCols(BufferedImage target, ArrayList<String> cells, int nCols, ElementLayout elementLayout) {
         Graphics2D g = target.createGraphics();
         g.setFont(elementLayout.getFont());
         g.setColor(elementLayout.getCardFont().getColor());
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
         g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         
         FontMetrics fm = g.getFontMetrics();
         
         //Get max width
         int maxWidth = 0;
         for (String cell: cells) {
             int width = fm.getStringBounds(cell, g).getBounds().width;
             if (width > maxWidth) {
                 maxWidth = width;
             }
         }
         
         int nRows = (int)Math.ceil((double)cells.size()/(double)nCols);
         for (int row = 0; row < nRows; row++) {
             for (int col = 0; col < nCols; col++) {
                 String cell = cells.get((row * nCols) + col);
                 
                 int lineHeight = fm.getHeight();
 
                 g.drawString(cell, (elementLayout.getX() + elementLayout.getMarginX()) + ((maxWidth + elementLayout.getMarginX()) * col), (elementLayout.getY() + elementLayout.getMarginY() + lineHeight) + (row * lineHeight));
             }
         }
     }
 }
