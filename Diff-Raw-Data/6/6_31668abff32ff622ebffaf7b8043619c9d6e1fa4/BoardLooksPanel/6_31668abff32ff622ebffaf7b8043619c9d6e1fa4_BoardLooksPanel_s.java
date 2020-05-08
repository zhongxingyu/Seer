 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2004 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.board.prefs;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import free.chess.BoardPainter;
 import free.chess.ColoredBoardPainter;
 import free.chess.ColoredPiecePainter;
 import free.chess.PiecePainter;
 import free.jin.BadChangesException;
 import free.jin.Jin;
 import free.jin.Resource;
 import free.jin.board.BoardManager;
 import free.jin.board.BoardPattern;
 import free.jin.board.JinBoard;
 import free.jin.board.PieceSet;
 import free.util.AWTUtilities;
 import free.util.TableLayout;
 import free.util.swing.ColorChooser;
 import free.util.swing.PreferredSizedPanel;
 import free.util.swing.UrlDisplayingAction;
 
 
 /**
  * A preferences panel allowing the user to modify the looks of the board. 
  */
  
 public class BoardLooksPanel extends BoardModifyingPrefsPanel{
   
   
   
   /**
    * The list holding all the available piece set names. 
    */
    
   protected final JList pieceSets;
   
   
   
   /**
    * The color chooser for the white piece's color.
    */
    
   protected final ColorChooser whiteColor; 
   
 
 
   /**
    * The color chooser for the black piece's color.
    */
    
   protected final ColorChooser blackColor;
   
   
   
   /**
    * The color chooser for the white piece's outline color.
    */
    
   protected final ColorChooser whiteOutline;
   
   
   
   /**
    * The color chooser for the black piece's outline color.
    */
    
   protected final ColorChooser blackOutline; 
 
   
   
   /**
    * The list holding all the available board pattern names.
    */
    
   protected final JList boardPatterns;
   
   
   
   /**
    * The color chooser for the board's dark squares' color.
    */
    
   protected final ColorChooser darkSquares;
    
   
 
   /**
    * The color chooser for the board's light squares' color.
    */
    
   protected final ColorChooser lightSquares;
   
   
   
   /**
    * The panel containing all of the piece color selection controls. This is
    * needed so that they can be disabled when the selected piece painter is
    * not a ColoredPiecePainter.
    */
    
   private Container pieceColorsPanel;
   
   
   
   /**
    * The panel containing all of the board color selection controls. This is
    * needed so that they can be disabled when the selected board painter is
    * not a ColoredBoardPainter.
    */
    
   private Container boardColorsPanel;
   
   
   
   /**
    * Creates a new <code>BoardLooksPanel</code> for the specified
    * <code>BoardManager</code> and with the specified preview board.
    */
    
   public BoardLooksPanel(BoardManager boardManager, JinBoard previewBoard){
     super(boardManager, previewBoard);
     
     pieceSets = new JList(getPieceSets());
     boardPatterns = new JList(getBoardPatterns());
     whiteColor = new ColorChooser("White color:", boardManager.getWhitePieceColor());
     blackColor = new ColorChooser("Black color:", boardManager.getBlackPieceColor());
     whiteOutline = new ColorChooser("White outline:", boardManager.getWhiteOutlineColor());
     blackOutline = new ColorChooser("Black outline:", boardManager.getBlackOutlineColor());
     darkSquares = new ColorChooser("Dark squares:", boardManager.getDarkSquareColor());
     lightSquares = new ColorChooser("Light squares:", boardManager.getLightSquareColor());
     
     whiteColor.setMnemonic('W');
     blackColor.setMnemonic('B');
     whiteOutline.setMnemonic('t');
     blackOutline.setMnemonic('k');
     darkSquares.setMnemonic('D');
     lightSquares.setMnemonic('L');
     
     whiteColor.setToolTipText("The color of white's pieces");
     blackColor.setToolTipText("The color of black's pieces");
     whiteOutline.setToolTipText("The color of the outline of white's pieces");
     blackOutline.setToolTipText("The color of the outline of black's pieces");
     darkSquares.setToolTipText("The color of the dark squares");
     lightSquares.setToolTipText("The color of the light squares");
     
     JComponent piecesPanel = createPiecesUI();
     JComponent boardPanel = createBoardUI();
     
 
     pieceSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     boardPatterns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     
     pieceSets.addListSelectionListener(new ListSelectionListener(){
       public void valueChanged(ListSelectionEvent evt){
         PieceSet set = (PieceSet)pieceSets.getSelectedValue();
         PiecePainter painter = set.getPiecePainter();
         
         if (painter instanceof ColoredPiecePainter){
           ColoredPiecePainter cPainter = (ColoredPiecePainter)painter; 
           cPainter.setWhiteColor(whiteColor.getColor()); 
           cPainter.setBlackColor(blackColor.getColor()); 
           cPainter.setWhiteOutline(whiteOutline.getColor()); 
           cPainter.setBlackOutline(blackOutline.getColor()); 
         }
          
         BoardLooksPanel.this.previewBoard.setPiecePainter(painter);
         AWTUtilities.setContainerEnabled(pieceColorsPanel, painter instanceof ColoredPiecePainter);
         fireStateChanged();
       }
     });
 
     boardPatterns.addListSelectionListener(new ListSelectionListener(){
       public void valueChanged(ListSelectionEvent evt){
         BoardPattern pattern = (BoardPattern)boardPatterns.getSelectedValue();
         BoardPainter painter = pattern.getBoardPainter();
         
         if (painter instanceof ColoredBoardPainter){
           ColoredBoardPainter cPainter = (ColoredBoardPainter)painter;
           cPainter.setDarkColor(darkSquares.getColor()); 
           cPainter.setLightColor(lightSquares.getColor()); 
         }
          
         BoardLooksPanel.this.previewBoard.setBoardPainter(painter);
         AWTUtilities.setContainerEnabled(boardColorsPanel, painter instanceof ColoredBoardPainter);
         fireStateChanged();
       }
     });
     
     pieceSets.setSelectedValue(boardManager.getPieceSet(), true);
     boardPatterns.setSelectedValue(boardManager.getBoardPattern(), true);
 
     ChangeListener pieceColorChangeListener = new ChangeListener(){
       public void stateChanged(ChangeEvent evt){
         ColoredPiecePainter painter =
           (ColoredPiecePainter)(BoardLooksPanel.this.previewBoard.getPiecePainter());
         painter.setWhiteColor(whiteColor.getColor());
         painter.setBlackColor(blackColor.getColor());
         painter.setWhiteOutline(whiteOutline.getColor());
         painter.setBlackOutline(blackOutline.getColor());
 
         BoardLooksPanel.this.previewBoard.repaint();
         fireStateChanged();
       }
     };
     whiteColor.addChangeListener(pieceColorChangeListener);
     blackColor.addChangeListener(pieceColorChangeListener);
     whiteOutline.addChangeListener(pieceColorChangeListener);
     blackOutline.addChangeListener(pieceColorChangeListener);
     
     ChangeListener boardColorChangeListener = new ChangeListener(){
       public void stateChanged(ChangeEvent evt){
         // The controls should be disabled when the painter isn't colored
         ColoredBoardPainter painter =
           (ColoredBoardPainter)(BoardLooksPanel.this.previewBoard.getBoardPainter()); 
         painter.setDarkColor(darkSquares.getColor());
         painter.setLightColor(lightSquares.getColor());
         
         BoardLooksPanel.this.previewBoard.repaint();
         fireStateChanged();
       }
     };
     darkSquares.addChangeListener(boardColorChangeListener);
     lightSquares.addChangeListener(boardColorChangeListener);
     
     JPanel boardAndExtraPanel = new JPanel();
    boardAndExtraPanel.setLayout(new BoxLayout(boardAndExtraPanel, BoxLayout.Y_AXIS));
    boardPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 
     JPanel contentPanel = new JPanel(new BorderLayout());
     contentPanel.add(BorderLayout.WEST, piecesPanel);
     contentPanel.add(BorderLayout.CENTER, Box.createHorizontalStrut(10));
     contentPanel.add(BorderLayout.EAST, boardAndExtraPanel);
     
     JLabel noticeLabel = new JLabel("Color settings apply only to some of the sets and patterns");
     
     contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     noticeLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     
     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));    
     add(contentPanel);
     add(Box.createVerticalStrut(10));
     add(noticeLabel);
   }
   
   
   
   /**
    * Gets the list of piece sets from the board manager.
    */
    
   private PieceSet [] getPieceSets(){
     Resource [] resources = boardManager.getResources("pieces");
     PieceSet [] pieceSets = new PieceSet[resources.length];
     
     for (int i = 0; i < resources.length; i++)
       pieceSets[i] = (PieceSet)resources[i];
     
     // Bubble sort by name
     for (int i = 0; i < resources.length; i++){
       for (int j = 0; j < resources.length - (i + 1); j++){
         String name1 = pieceSets[j].getName();
         String name2 = pieceSets[j+1].getName();
         
         if (name1.compareTo(name2) > 0){
           PieceSet tmp = pieceSets[j];
           pieceSets[j] = pieceSets[j+1];
           pieceSets[j+1] = tmp;
         }
       }
     }
     
     return pieceSets;
   }
   
   
   
   /**
    * Gets the list of board patterns from the board manager.
    */
    
   private BoardPattern [] getBoardPatterns(){
     Resource [] resources = boardManager.getResources("boards");
     BoardPattern [] boardPatterns = new BoardPattern[resources.length];
     
     for (int i = 0; i < resources.length; i++)
       boardPatterns[i] = (BoardPattern)resources[i];
     
     // Bubble sort by name
     for (int i = 0; i < resources.length; i++){
       for (int j = 0; j < resources.length - (i + 1); j++){
         String name1 = boardPatterns[j].getName();
         String name2 = boardPatterns[j+1].getName();
         
         if (name1.compareTo(name2) > 0){
           BoardPattern tmp = boardPatterns[j];
           boardPatterns[j] = boardPatterns[j+1];
           boardPatterns[j+1] = tmp;
         }
       }
     }
     
     return boardPatterns;
   }
   
   
   
   /**
    * Sets the initial properties of the preview board.
    */
    
   public void initPreviewBoard(){
     PieceSet pieceSet = (PieceSet)pieceSets.getSelectedValue();
     if (pieceSet != null){
       PiecePainter piecePainter = pieceSet.getPiecePainter();
       if (piecePainter instanceof ColoredPiecePainter){
         ColoredPiecePainter cPainter = (ColoredPiecePainter)piecePainter; 
         cPainter.setWhiteColor(whiteColor.getColor()); 
         cPainter.setBlackColor(blackColor.getColor()); 
         cPainter.setWhiteOutline(whiteOutline.getColor()); 
         cPainter.setBlackOutline(blackOutline.getColor()); 
       }
       previewBoard.setPiecePainter(piecePainter);
     }
 
     BoardPattern boardPattern = (BoardPattern)boardPatterns.getSelectedValue();
     if (boardPattern != null){
       BoardPainter boardPainter = boardPattern.getBoardPainter();
       if (boardPainter instanceof ColoredBoardPainter){
         ColoredBoardPainter cPainter = (ColoredBoardPainter)boardPainter;
         cPainter.setDarkColor(darkSquares.getColor()); 
         cPainter.setLightColor(lightSquares.getColor()); 
       }
       previewBoard.setBoardPainter(boardPainter);
     }
   }
   
   
   
   /**
    * Creates the user interface for selecting the looks of the piece set.
    */
    
   private JComponent createPiecesUI(){
     JComponent piecesPanel = new JPanel();
     piecesPanel.setLayout(new BoxLayout(piecesPanel, BoxLayout.Y_AXIS));
     piecesPanel.setBorder(BorderFactory.createCompoundBorder(
       BorderFactory.createTitledBorder("Pieces"),
       BorderFactory.createEmptyBorder(0, 10, 10, 10)));
     
       
     JLabel pieceSetLabel = new JLabel("Set:");
     pieceSetLabel.setDisplayedMnemonic('S');
     pieceSetLabel.setLabelFor(pieceSets);
     pieceSetLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     
     
     JScrollPane scrollPane = 
       new JScrollPane(pieceSets, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
     
     JPanel colorsPanel = new PreferredSizedPanel(new TableLayout(1, 0, 5));
     colorsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     colorsPanel.add(whiteColor);
     colorsPanel.add(blackColor);
     colorsPanel.add(whiteOutline);
     colorsPanel.add(blackOutline);
     
     piecesPanel.add(pieceSetLabel);
     piecesPanel.add(Box.createVerticalStrut(5));
     piecesPanel.add(scrollPane);
     piecesPanel.add(Box.createVerticalStrut(10));
     piecesPanel.add(colorsPanel);
         
     pieceColorsPanel = colorsPanel;
     
     return piecesPanel;
   }
   
   
   
   /**
    * Creates the user interface for selecting the looks of the piece set.
    */
    
   private JComponent createBoardUI(){
     JComponent boardPanel = new JPanel();
     boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
     boardPanel.setBorder(BorderFactory.createCompoundBorder(
       BorderFactory.createTitledBorder("Board"),
       BorderFactory.createEmptyBorder(0, 10, 10, 10)));
     
     
     JLabel patternLabel = new JLabel("Pattern:");
     patternLabel.setDisplayedMnemonic('P');
     patternLabel.setLabelFor(boardPatterns);
     patternLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
 
     
     JScrollPane scrollPane = 
       new JScrollPane(boardPatterns, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
     
     JPanel colorsPanel = new PreferredSizedPanel(new TableLayout(1, 0, 5));
     colorsPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
     colorsPanel.add(darkSquares);    
     colorsPanel.add(lightSquares);
 
     boardPanel.add(patternLabel);
     boardPanel.add(Box.createVerticalStrut(5));
     boardPanel.add(scrollPane);
     boardPanel.add(Box.createVerticalStrut(10));
     boardPanel.add(colorsPanel);
     
     boardColorsPanel = colorsPanel;
     
     return boardPanel;
   }
   
   
   
   /**
    * Applies any changes made by the user.
    */
    
   public void applyChanges() throws BadChangesException{
     boardManager.setPieceSet((PieceSet)pieceSets.getSelectedValue());
     boardManager.setBoardPattern((BoardPattern)boardPatterns.getSelectedValue());
     boardManager.setWhitePieceColor(whiteColor.getColor());
     boardManager.setBlackPieceColor(blackColor.getColor());
     boardManager.setWhiteOutlineColor(whiteOutline.getColor());
     boardManager.setBlackOutlineColor(blackOutline.getColor());
     boardManager.setDarkSquareColor(darkSquares.getColor());
     boardManager.setLightSquareColor(lightSquares.getColor());
   }
   
 
   
 }
