 package net.avh4.framework.uilayer.swing;
 
 import net.avh4.framework.uilayer.ClickReceiver;
 import net.avh4.framework.uilayer.KeyReceiver;
 import net.avh4.framework.uilayer.ResponseListener;
 import net.avh4.framework.uilayer.SceneCreator;
 import net.avh4.framework.uilayer.UILayerService;
 import net.avh4.framework.uilayer.scene.Scene;
 import net.avh4.framework.uilayer.scene.SceneRenderer;
 import net.avh4.framework.uilayer.scene.SwingGraphicsOperations;
 import net.avh4.framework.uilayer.scene.SwingSceneRenderer;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 
 public class SwingUILayerService implements UILayerService {
 
     private static final HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();
     private static final HashMap<String, Font> fontCache = new HashMap<String, Font>();
 
     @Override
     public void run(final SceneCreator game, final ClickReceiver receiver,
                     final KeyReceiver keyReceiver) {
 
         final Scene scene = game.getScene();
         final String title = scene != null ? scene.getTitle() : "(No scene)";
         final JFrame window = new JFrame(title);
         final SwingGraphicsOperations graphicsOperations = new SwingGraphicsOperations();
         final SwingSceneRenderer component = new SwingSceneRenderer(graphicsOperations, new SceneRenderer(game));
         window.add(component);
         window.pack();
         window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         final SwingInputHandler inputHandler = new SwingInputHandler(receiver,
                 keyReceiver, component);
         component.addMouseListener(inputHandler);
         component.addKeyListener(inputHandler);
 
         component.requestFocusInWindow(); // Required to get keyboard focus for
         // the KeyListener to work
 
         window.setVisible(true);
     }
 
     public static BufferedImage loadImage(final String imageName) {
         if (imageCache.containsKey(imageName)) {
             return imageCache.get(imageName);
         }
 
         final URL resource = ClassLoader.getSystemResource(imageName);
         if (resource == null) {
             throw new IllegalArgumentException(String.format(
                     "No such resource '%s'", imageName));
         }
         try {
             final BufferedImage image = ImageIO.read(resource);
             imageCache.put(imageName, image);
             return image;
         } catch (final IOException e) {
             e.printStackTrace();
             throw new IllegalArgumentException(String.format(
                     "Could not read image '%s'", imageName), e);
         }
     }
 
     @Override
     public int getImageWidth(final String image) {
         final BufferedImage i = loadImage(image);
         return i.getWidth();
     }
 
     @Override
     public int getImageHeight(final String image) {
         final BufferedImage i = loadImage(image);
         return i.getHeight();
     }
 
     @Override
     public int getPixel(String image, int x, int y) {
         return loadImage(image).getRGB(x, y);
     }
 
     @Override
     public int getFontHeight(net.avh4.framework.uilayer.Font font) {
         final Font awtFont = loadFont(font);
 
         final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
         final Graphics2D g2 = (Graphics2D) image.getGraphics();
         return g2.getFontMetrics(awtFont).getHeight();
     }
 
     @Override
     public int measureText(net.avh4.framework.uilayer.Font font, String text) {
         final Font awtFont = loadFont(font);
 
         final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
         final Graphics2D g2 = (Graphics2D) image.getGraphics();
         final FontMetrics metrics = g2.getFontMetrics(awtFont);
         final Rectangle2D bounds = metrics.getStringBounds(text, g2);
         return (int) bounds.getWidth();
     }
 
     @Override
     public void showChoices(final String title, final List<String> choices, final ResponseListener listener) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                final String response = (String) JOptionPane.showInputDialog(null, null, title, 0, null,
                        choices.toArray(), null);
                 listener.response(response);
             }
         });
     }
 
     public static Font loadFont(final net.avh4.framework.uilayer.Font font) {
         if (font == null) {
             throw new RuntimeException("No font specified.  Try Font.PFENNIG.");
         }
 
         final String customFontResource = font.getResourceName();
         return loadFont(customFontResource, font.getSize());
     }
 
     public static Font loadFont(final String customFontResource,
                                 final int fontSize) {
 
         final String key = customFontResource + ":" + fontSize;
         if (fontCache.containsKey(key)) {
             return fontCache.get(key);
         }
 
         final InputStream is = ClassLoader
                 .getSystemResourceAsStream(customFontResource);
         if (is == null) {
             throw new RuntimeException(String.format(
                     "Custom font resource not found: %s", customFontResource));
         }
         try {
             final Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is)
                     .deriveFont(Font.PLAIN, fontSize);
             GraphicsEnvironment.getLocalGraphicsEnvironment()
                     .registerFont(awtFont);
             fontCache.put(key, awtFont);
             return awtFont;
         } catch (final FontFormatException e) {
             throw new RuntimeException(String.format(
                     "Couldn't open custom font: %s", customFontResource), e);
         } catch (final IOException e) {
             throw new RuntimeException(String.format(
                     "Couldn't open custom font: %s", customFontResource), e);
         }
     }
 }
