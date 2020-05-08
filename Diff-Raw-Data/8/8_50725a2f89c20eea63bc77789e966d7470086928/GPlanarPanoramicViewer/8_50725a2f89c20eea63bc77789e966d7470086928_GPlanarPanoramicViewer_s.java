 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.panoramic.planar;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.pushingpixels.substance.api.SubstanceLookAndFeel;
 import org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import es.igosoftware.concurrent.GConcurrent;
 import es.igosoftware.io.GHttpLoader;
 import es.igosoftware.io.GIOUtils;
 import es.igosoftware.io.ILoader;
 import es.igosoftware.io.ILoader.IHandler;
 import es.igosoftware.util.GHolder;
 import es.igosoftware.util.GLogger;
 import es.igosoftware.util.GUtils;
 
 
 /**
  * Viewer for Gigapixel Pictures.<br/>
  * <br/>
  * To use this viewer, the picture has to be processed with {@link es.igosoftware.panoramic.planar.GPlanarPanoramicCompiler}.<br/>
  * 
  * 
  * @author dgd
  * 
  */
 public class GPlanarPanoramicViewer {
    private static final GLogger LOGGER               = GLogger.instance();
 
 
    private static final int     HORIZONTAL_INCREMENT = GPlanarPanoramicZoomLevel.TILE_WIDTH / 3;
    private static final int     VERTICAL_INCREMENT   = GPlanarPanoramicZoomLevel.TILE_HEIGHT / 3;
 
 
    private class Tile
             extends
                JPanel
             implements
                ActionListener {
       private static final long               serialVersionUID = 1L;
 
       private final GPlanarPanoramicZoomLevel _zoomLevel;
       private final int                       _x;
       private final int                       _y;
 
       private Image                           _image;
       private IHandler                        _handler;
 
       private float                           _alpha           = 0.1f;
       private Timer                           _timer;
 
 
       private Tile(final GPlanarPanoramicZoomLevel zoomLevel,
                    final int x,
                    final int y) {
          super();
 
          //         if (_debug) {
          //            setBorder(new LineBorder(Color.BLUE, 1));
          //         }
 
          _zoomLevel = zoomLevel;
          _x = x;
          _y = y;
       }
 
 
       private void positionate() {
          tryToLoadImage();
 
          setBounds(calculateXPosition(), calculateYPosition(), GPlanarPanoramicZoomLevel.TILE_WIDTH,
                   GPlanarPanoramicZoomLevel.TILE_HEIGHT);
       }
 
 
       private void tryToLoadImage() {
 
          if ((_handler != null) || (_image != null)) {
             return;
          }
 
          if ((_handler == null) && (_image == null)) {
             final String fileName = getTileFileName();
             _handler = new ILoader.IHandler() {
 
 
                @Override
                public void loaded(final File file,
                                   final long bytesLoaded,
                                   final boolean completeLoaded) {
                   if (!completeLoaded) {
                      return;
                   }
 
                   _handler = null;
                   try {
                      _image = ImageIO.read(file);
                      _timer = new Timer(20, Tile.this);
                      _timer.start();
                   }
                   catch (final IOException e) {
                      LOGGER.severe("Error loading " + fileName, e);
                   }
                }
 
 
                @Override
                public void loadError(final ILoader.ErrorType error,
                                      final Throwable e) {
                   LOGGER.severe("error " + error + " loading " + fileName, e);
                }
             };
 
             _loader.load(fileName, -1, _zoomLevel.getLevel(), _handler);
          }
       }
 
 
       @Override
       public void actionPerformed(final ActionEvent e) {
          _alpha += 0.1f;
          if (_alpha >= 1) {
             _alpha = 1;
             _timer.stop();
          }
          final Container parent = getParent();
          if (parent != null) {
             parent.repaint();
          }
       }
 
 
       @Override
       public void paint(final Graphics g) {
          if (_image != null) {
             final Graphics2D g2d = (Graphics2D) g;
 
             g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha));
             g2d.drawImage(_image, 0, 0, null);
          }
 
          if (_debug) {
             final Graphics2D g2d = (Graphics2D) g;
             g2d.setColor(Color.BLUE);
             g2d.drawRect(0, 0, GPlanarPanoramicZoomLevel.TILE_WIDTH - 1, GPlanarPanoramicZoomLevel.TILE_HEIGHT - 1);
 
             final int x = 5;
             final int y = 20;
 
             g2d.setColor(Color.WHITE);
             final String debugMsg = "Level=" + _currentLevel + ", Tile=" + _x + "x" + _y;
             g2d.drawString(debugMsg, x + 1, y + 1);
             g2d.drawString(debugMsg, x - 1, y - 1);
             g2d.drawString(debugMsg, x + 1, y - 1);
             g2d.drawString(debugMsg, x - 1, y + 1);
 
             g2d.setColor(Color.BLACK);
             g2d.drawString(debugMsg, x, y);
          }
       }
 
 
       private int calculateXPosition() {
          return _offset.x + (_x * GPlanarPanoramicZoomLevel.TILE_WIDTH);
       }
 
 
       private int calculateYPosition() {
          return _offset.y + (_y * GPlanarPanoramicZoomLevel.TILE_HEIGHT);
       }
 
 
       private String getTileFileName() {
          return _zoomLevel.getLevel() + "/tile-" + _x + "-" + _y + ".jpg";
       }
 
 
       private void remove() {
          _loader.cancelLoading(getTileFileName());
          if (_timer != null) {
             _timer.stop();
          }
       }
 
 
       private boolean touches(final Rectangle bounds) {
          return bounds.intersects(calculateXPosition(), calculateYPosition(), GPlanarPanoramicZoomLevel.TILE_WIDTH,
                   GPlanarPanoramicZoomLevel.TILE_HEIGHT);
       }
 
    }
 
 
    private final String                          _name;
    private final ILoader                         _loader;
    private final boolean                         _debug;
 
 
    private final List<GPlanarPanoramicZoomLevel> _zoomLevels;
    private final int                             _minLevel;
    private final int                             _maxLevel;
    private final List<Tile>                      _tiles  = new ArrayList<Tile>();
 
    private int                                   _currentLevel;
    private final Point                           _offset = new Point(0, 0);
 
    private JLabel                                _zoomInButton;
    private JLabel                                _zoomOutButton;
    private JSlider                               _zoomSlider;
 
    private Point                                 _dragLastPosition;
 
 
    public GPlanarPanoramicViewer(final String name,
                                  final ILoader loader) throws IOException {
       this(name, loader, false);
    }
 
 
    public GPlanarPanoramicViewer(final String name,
                                  final ILoader loader,
                                  final boolean debug) throws IOException {
       _name = name;
       _loader = loader;
       _debug = debug;
 
       _zoomLevels = readZoomLevels();
 
       int minLevel = Integer.MAX_VALUE;
       int maxLevel = Integer.MIN_VALUE;
 
       for (final GPlanarPanoramicZoomLevel zoomLevel : _zoomLevels) {
          final int currentLevel = zoomLevel.getLevel();
          minLevel = Math.min(minLevel, currentLevel);
          maxLevel = Math.max(maxLevel, currentLevel);
       }
 
       _minLevel = minLevel;
       _maxLevel = maxLevel;
       _currentLevel = minLevel;
    }
 
 
    private GPlanarPanoramicZoomLevel getCurrentZoomLevel() {
       return getZoomLevel(_currentLevel);
    }
 
 
    private GPlanarPanoramicZoomLevel getZoomLevel(final int level) {
       if (_zoomLevels == null) {
          return null;
       }
 
       for (final GPlanarPanoramicZoomLevel zoomLevel : _zoomLevels) {
          if (zoomLevel.getLevel() == level) {
             return zoomLevel;
          }
       }
       return null;
    }
 
 
    private List<GPlanarPanoramicZoomLevel> readZoomLevels() throws IOException {
 
       final GHolder<Boolean> completed = new GHolder<Boolean>(false);
       final GHolder<List<GPlanarPanoramicZoomLevel>> resultHolder = new GHolder<List<GPlanarPanoramicZoomLevel>>(null);
 
       _loader.load("info.txt", -1, Integer.MAX_VALUE, new ILoader.IHandler() {
          @Override
          public void loaded(final File file,
                             final long bytesLoaded,
                             final boolean completeLoaded) {
             if (!completeLoaded) {
                return;
             }
 
             try {
                final String infoString = GIOUtils.getContents(file);
 
                final Gson gson = new Gson();
 
                final Type type = new TypeToken<List<GPlanarPanoramicZoomLevel>>() {
                }.getType();
 
                final List<GPlanarPanoramicZoomLevel> result = gson.fromJson(infoString, type);
                resultHolder.set(result);
                completed.set(true);
             }
             catch (final IOException e) {
                LOGGER.severe("error loading " + file, e);
             }
          }
 
 
          @Override
          public void loadError(final ILoader.ErrorType error,
                                final Throwable e) {
             LOGGER.severe("Error loading 'info.txt'", e);
             completed.set(true);
          }
       });
 
       while (!completed.get()) {
          GUtils.delay(10);
       }
 
       if (resultHolder.isEmpty()) {
          throw new IOException("Can't read 'info.txt'");
       }
 
       return resultHolder.get();
    }
 
 
    public void open() {
       open(800, 600);
    }
 
 
    public void open(final int width,
                     final int height) {
       final JFrame frame = createFrame(width, height);
 
       final Container container = frame.getContentPane();
 
       container.addMouseWheelListener(new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(final MouseWheelEvent e) {
             final Point point = e.getPoint();
 
             if (e.getWheelRotation() < 0) {
                setZoomLevel(container, _currentLevel + 1, point.x, point.y);
             }
             else {
                setZoomLevel(container, _currentLevel - 1, point.x, point.y);
             }
          }
       });
 
 
       createHUD(container);
 
       frame.setVisible(true);
 
       final Dimension containerSize = container.getSize();
       _currentLevel = calculateInitialLevel(containerSize);
 
       final GPlanarPanoramicZoomLevel currentZoomLevel = getCurrentZoomLevel();
       _offset.x = (containerSize.width - currentZoomLevel.getWidth()) / 2;
       _offset.y = (containerSize.height - currentZoomLevel.getHeight()) / 2;
 
       updateZoomWidgets();
 
       recreateTiles(container);
    }
 
 
    private int calculateInitialLevel(final Dimension containerSize) {
       int result = _minLevel;
 
       final double currentWidth = containerSize.getWidth();
       final double currentHeight = containerSize.getHeight();
 
       for (int i = _minLevel + 1; i < _maxLevel; i++) {
          final GPlanarPanoramicZoomLevel currentLevel = getZoomLevel(i);
          if ((currentLevel.getWidth() <= currentWidth) && (currentLevel.getHeight() <= currentHeight)) {
             result = i;
          }
       }
 
       return result;
    }
 
 
    private JFrame createFrame(final int width,
                               final int height) {
       final JFrame frame = new JFrame(_name);
 
       frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       frame.getRootPane().registerKeyboardAction(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
             frame.dispose();
          }
       }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 
       frame.setIconImage(getImage("icons/panoramic.png"));
 
       frame.setSize(width, height);
       frame.setMinimumSize(new Dimension(320, 240));
       frame.setLocationRelativeTo(null);
 
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
             frame.requestFocus();
             frame.requestFocusInWindow();
          }
       });
 
       final Container contentPane = frame.getContentPane();
       contentPane.setFocusable(true);
       contentPane.setLayout(null);
       contentPane.setBackground(Color.WHITE);
       if (contentPane instanceof JPanel) {
          ((JPanel) contentPane).putClientProperty(SubstanceLookAndFeel.COLORIZATION_FACTOR, Double.valueOf(1));
       }
 
       contentPane.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(final KeyEvent e) {
             final int keyCode = e.getKeyCode();
 
             if ((keyCode == KeyEvent.VK_PLUS) || (keyCode == KeyEvent.VK_ADD)) {
                setZoomLevel(contentPane, _currentLevel + 1);
             }
             else if ((keyCode == KeyEvent.VK_MINUS) || (keyCode == KeyEvent.VK_SUBTRACT)) {
                setZoomLevel(contentPane, _currentLevel - 1);
             }
             else if (keyCode == KeyEvent.VK_LEFT) {
                moveLeft(contentPane);
             }
             else if (keyCode == KeyEvent.VK_RIGHT) {
                moveRight(contentPane);
             }
             else if (keyCode == KeyEvent.VK_UP) {
                moveUp(contentPane);
             }
             else if (keyCode == KeyEvent.VK_DOWN) {
                moveDown(contentPane);
             }
          }
       });
 
 
       contentPane.addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(final MouseEvent evt) {
             contentPane.setCursor(new Cursor(Cursor.MOVE_CURSOR));
             _dragLastPosition = evt.getPoint();
          }
 
 
          @Override
          public void mouseReleased(final MouseEvent evt) {
             contentPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          }
 
 
          @Override
          public void mouseExited(final MouseEvent evt) {
             contentPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          }
 
       });
 
       contentPane.addMouseMotionListener(new MouseMotionAdapter() {
          @Override
          public void mouseDragged(final MouseEvent evt) {
             final Point point = evt.getPoint();
             final Point delta = new Point(point.x - _dragLastPosition.x, point.y - _dragLastPosition.y);
 
             setOffset(contentPane, _offset.x + delta.x, _offset.y + delta.y);
 
             _dragLastPosition = point;
          }
       });
 
 
       frame.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(final ComponentEvent e) {
             recreateTiles(contentPane);
          }
       });
 
       requestFocus(contentPane);
 
       return frame;
    }
 
 
    private void requestFocus(final Container contentPane) {
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
             contentPane.requestFocus();
             contentPane.requestFocusInWindow();
          }
       });
    }
 
 
    private Image getImage(final String imageName) {
       return GUtils.getImage(imageName, getClass().getClassLoader());
    }
 
 
    private ImageIcon getImageIcon(final String iconName,
                                   final int width,
                                   final int height) {
       final ImageIcon icon = GUtils.getImageIcon(iconName, getClass().getClassLoader());
 
       final Image image = icon.getImage();
       if (image == null) {
          return icon;
       }
       final int imageWidth = image.getWidth(null);
       final int imageHeight = image.getHeight(null);
       if ((imageWidth == -1) || (imageHeight == -1)) {
          return icon;
       }
       if ((width == imageWidth) && (height == imageHeight)) {
          return icon;
       }
 
       final Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
       return new ImageIcon(resizedImage);
    }
 
 
    private void createHUD(final Container container) {
       final int buttonExtent = 20;
       final int margin = 2;
 
       createNavigationButtons(container, buttonExtent, margin);
       createZoomWidgets(container, buttonExtent, margin);
    }
 
 
    private void createNavigationButtons(final Container container,
                                         final int buttonExtent,
                                         final int margin) {
       final JLabel buttonUp = new JLabel(getImageIcon("icons/go-up.png", buttonExtent, buttonExtent));
       buttonUp.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             moveUp(container);
          }
       });
       setLook(buttonUp);
       container.add(buttonUp);
       setPosition(buttonUp, margin + buttonExtent, margin + 0);
 
 
       final JLabel buttonDown = new JLabel(getImageIcon("icons/go-down.png", buttonExtent, buttonExtent));
       buttonDown.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             moveDown(container);
          }
       });
       setLook(buttonDown);
       container.add(buttonDown);
       setPosition(buttonDown, margin + buttonExtent, margin + buttonExtent * 2);
 
 
       final JLabel buttonLeft = new JLabel(getImageIcon("icons/go-left.png", buttonExtent, buttonExtent));
       buttonLeft.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             moveLeft(container);
          }
       });
       setLook(buttonLeft);
       container.add(buttonLeft);
       setPosition(buttonLeft, margin + 0, margin + buttonExtent);
 
 
       final JLabel buttonRight = new JLabel(getImageIcon("icons/go-right.png", buttonExtent, buttonExtent));
       buttonRight.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             moveRight(container);
          }
       });
       setLook(buttonRight);
       container.add(buttonRight);
       setPosition(buttonRight, margin + buttonExtent * 2, margin + buttonExtent);
    }
 
 
    private void setLook(final JComponent widget) {
       if (_debug) {
          widget.setBorder(new LineBorder(Color.RED, 1));
       }
       widget.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
 
 
    private void createZoomWidgets(final Container container,
                                   final int buttonExtent,
                                   final int margin) {
       _zoomInButton = new JLabel(getImageIcon("icons/zoom-in.png", buttonExtent, buttonExtent));
       _zoomInButton.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             setZoomLevel(container, _currentLevel + 1);
          }
       });
       setLook(_zoomInButton);
       container.add(_zoomInButton);
       setPosition(_zoomInButton, margin + buttonExtent, margin + buttonExtent * 4 + 0);
 
 
       _zoomSlider = new JSlider(JSlider.VERTICAL, _minLevel, _maxLevel, _currentLevel);
       _zoomSlider.setMajorTickSpacing(0);
       _zoomSlider.setMinorTickSpacing(1);
       _zoomSlider.setSnapToTicks(true);
       _zoomSlider.setOpaque(false);
       _zoomSlider.setPreferredSize(new Dimension(_zoomSlider.getPreferredSize().width, _zoomLevels.size() * buttonExtent * 2 / 3));
       _zoomSlider.addChangeListener(new ChangeListener() {
          @Override
          public void stateChanged(final ChangeEvent e) {
             setZoomLevel(container, _zoomSlider.getValue());
             requestFocus(container);
          }
       });
 
       setLook(_zoomSlider);
       container.add(_zoomSlider);
       setPosition(_zoomSlider, margin + buttonExtent, margin + buttonExtent * 5);
 
 
       _zoomOutButton = new JLabel(getImageIcon("icons/zoom-out.png", buttonExtent, buttonExtent));
       _zoomOutButton.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
             setZoomLevel(container, _currentLevel - 1);
          }
       });
       setLook(_zoomOutButton);
       container.add(_zoomOutButton);
       setPosition(_zoomOutButton, margin + buttonExtent, margin + buttonExtent * 5 + _zoomSlider.getHeight());
    }
 
 
    private void setZoomLevel(final Container container,
                              final int newLevel) {
       final Rectangle containerBounds = container.getBounds();
       final int targetX = (int) containerBounds.getCenterX();
       final int targetY = (int) containerBounds.getCenterY();
       setZoomLevel(container, newLevel, targetX, targetY);
    }
 
 
    private void setZoomLevel(final Container container,
                              final int newLevel,
                              final int targetX,
                              final int targetY) {
       if (newLevel == _currentLevel) {
          return;
       }
 
       if ((newLevel < _minLevel) || (newLevel > _maxLevel)) {
          return;
       }
 
       final int oldLevel = _currentLevel;
       _currentLevel = newLevel;
 
       final double zoomFactor = Math.pow(2, _currentLevel - oldLevel);
 
       final double targetXForNewZoom = (targetX - _offset.x) * zoomFactor;
       final double targetYForNewZoom = (targetY - _offset.y) * zoomFactor;
 
       _offset.x = (int) (targetXForNewZoom - targetX) * -1;
       _offset.y = (int) (targetYForNewZoom - targetY) * -1;
 
       updateZoomWidgets();
 
       recreateTiles(container);
    }
 
 
    private void setOffset(final Container container,
                           final int offsetX,
                           final int offsetY) {
       if ((offsetX == _offset.x) && (offsetY == _offset.y)) {
          return;
       }
 
       _offset.x = offsetX;
       _offset.y = offsetY;
 
       layoutTiles();
       updateTilesGrid(container);
    }
 
 
    private void updateTilesGrid(final Container container) {
       removeNotVisibleTiles(container);
 
       final List<Tile> tilesToCreate = new ArrayList<Tile>();
 
       final GPlanarPanoramicZoomLevel currentZoomLevel = getCurrentZoomLevel();
 
       final Rectangle containerBounds = new Rectangle(0, 0, (int) container.getBounds().getWidth(),
                (int) container.getBounds().getHeight());
       for (int x = 0; x < currentZoomLevel.getWidthInTiles(); x++) {
          for (int y = 0; y < currentZoomLevel.getHeightInTiles(); y++) {
             final Tile tile = new Tile(currentZoomLevel, x, y);
             if (tile.touches(containerBounds)) {
                if (!hasTileInTheSamePosition(tile)) {
                   tilesToCreate.add(tile);
                }
             }
          }
       }
 
       for (final Tile tileToCreate : tilesToCreate) {
          _tiles.add(tileToCreate);
          container.add(tileToCreate);
          tileToCreate.positionate();
       }
 
    }
 
 
    private boolean hasTileInTheSamePosition(final Tile tile) {
       for (final Tile each : _tiles) {
          if ((each._x == tile._x) && (each._y == tile._y)) {
             return true;
          }
       }
       return false;
    }
 
 
    private void removeNotVisibleTiles(final Container container) {
       final Rectangle containerBounds = new Rectangle(0, 0, (int) container.getBounds().getWidth(),
                (int) container.getBounds().getHeight());
 
       final Iterator<Tile> iterator = _tiles.iterator();
       while (iterator.hasNext()) {
          final Tile tile = iterator.next();
          if (!tile.touches(containerBounds)) {
             tile.remove();
             iterator.remove();
             container.remove(tile);
          }
       }
    }
 
 
    private void updateZoomWidgets() {
       _zoomInButton.setEnabled(_currentLevel < _maxLevel);
       _zoomOutButton.setEnabled(_currentLevel > _minLevel);
 
       _zoomSlider.setValue(_currentLevel);
    }
 
 
    private void recreateTiles(final Container container) {
       removeTiles(container);
       createTiles(container);
       addTiles(container);
       layoutTiles();
 
       // force redraw
       container.invalidate();
       container.doLayout();
       container.repaint();
    }
 
 
    private void addTiles(final Container container) {
       for (final Tile tile : _tiles) {
          container.add(tile);
       }
    }
 
 
    private void layoutTiles() {
       for (final Tile tile : _tiles) {
          tile.positionate();
       }
    }
 
 
    private void createTiles(final Container container) {
       final GPlanarPanoramicZoomLevel currentZoomLevel = getCurrentZoomLevel();
 
       final Rectangle containerBounds = new Rectangle(0, 0, (int) container.getBounds().getWidth(),
                (int) container.getBounds().getHeight());
       for (int x = 0; x < currentZoomLevel.getWidthInTiles(); x++) {
          for (int y = 0; y < currentZoomLevel.getHeightInTiles(); y++) {
             final Tile tile = new Tile(currentZoomLevel, x, y);
             if (tile.touches(containerBounds)) {
                _tiles.add(tile);
             }
          }
       }
    }
 
 
    private void removeTiles(final Container container) {
       for (final Tile tile : _tiles) {
          tile.remove();
          container.remove(tile);
       }
       _tiles.clear();
    }
 
 
    private void setPosition(final Component component,
                             final int x,
                             final int y) {
       final Dimension size = component.getPreferredSize();
       component.setBounds(x, y, size.width, size.height);
    }
 
 
    private void moveDown(final Container container) {
       setOffset(container, _offset.x, _offset.y - VERTICAL_INCREMENT);
    }
 
 
    private void moveUp(final Container container) {
       setOffset(container, _offset.x, _offset.y + VERTICAL_INCREMENT);
    }
 
 
    private void moveLeft(final Container container) {
       setOffset(container, _offset.x + HORIZONTAL_INCREMENT, _offset.y);
    }
 
 
    private void moveRight(final Container container) {
       setOffset(container, _offset.x - HORIZONTAL_INCREMENT, _offset.y);
    }
 
 
    public static void main(final String[] args) throws IOException {
       System.out.println("GPlanarPanoramicViewer 0.1");
       System.out.println("--------------------------\n");
 
 
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
             JFrame.setDefaultLookAndFeelDecorated(true);
             JDialog.setDefaultLookAndFeelDecorated(true);
 
             try {
                UIManager.setLookAndFeel(new SubstanceMistAquaLookAndFeel());
 
                SubstanceLookAndFeel.setToUseConstantThemesOnDialogs(true);
             }
             catch (final UnsupportedLookAndFeelException e) {
                e.printStackTrace();
             }
          }
       });
 
 
      //       final ILoader loader = new GFileLoader("/home/dgd/Escritorio/PruebaPanoramicas/PLANAR/PANOS/cantabria1.jpg/");
 
 
      //       final URL url = new URL("file:///home/dgd/Escritorio/PruebaPanoramicas/PLANAR/PANOS/cantabria1.jpg/");
       //       final URL url = new URL("http://localhost/PANOS/cantabria1.jpg/");
       //       final URL url = new URL("http://localhost/PANOS/PlazaSanJorge-Caceres-Espana.jpg/");
       final URL url = new URL("http://213.165.81.201:8080/gigapixel/Cantabria1/");
       //      final URL url = new URL("http://213.165.81.201:8080/gigapixel/PlazaSanJorge-Caceres-Espana.jpg/");
 
       final int workersCount = GConcurrent.AVAILABLE_PROCESSORS * 2;
       final ILoader loader = new GHttpLoader(url, workersCount, true);
 
 
      final GPlanarPanoramicViewer viewer = new GPlanarPanoramicViewer("Panorámica de Cantabria", loader, true);
 
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
             viewer.open();
          }
       });
    }
 
 }
