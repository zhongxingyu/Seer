 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.gui.otherpanels.bookpanels;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.font.FontRenderContext;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.book.BookDataControl;
 import es.eucm.eadventure.editor.control.controllers.book.BookParagraphDataControl;
 import es.eucm.eadventure.editor.control.controllers.book.BookParagraphsListDataControl;
 
 /**
  * Class for book paragraph view in Content tab.
  * 
  * @author ngel
  * 
  */
 public class BookParagraphPreviewPanel extends BookPreviewPanel {
 
     /**
      * Required.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * X position of the first column of text
      */
     private static final int TEXT_FIRST_COLUMN = 110;
 
     /**
      * X position for the second column of text
      */
     private static final int TEXT_SECOND_COLUMN = 445;
 
     /**
      * Y position for both columns of text
      */
     private static final int TEXT_TOP_POSITION = 75;
 
     /**
      * Width of each column of text
      */
     private static final int TEXT_WIDTH = 250;
 
     /**
      * Width of each column of the bullet text
      */
     private static final int TEXT_WIDTH_BULLET = 225;
 
     /**
      * Height of each column of text
      */
     private static final int PAGE_TEXT_HEIGHT = 400;
 
     /**
      * Height of each line of text
      */
     private static final int LINE_HEIGHT = 25;
 
     /**
      * Height of each line of a title
      */
     private static final int TITLE_HEIGHT = 50;
 
     /**
      * Contains the image that holds the entire book.
      */
     private Image bookContinousImage;
 
     /**
      * Contains the number of pages of the book.
      */
     private int pageCount;
 
     /**
      * Contains the current page being shown in the book.
      */
     private int currentPage;
 
     /**
      * Current state for arrows
      */
     protected Image currentArrowLeft, currentArrowRight;
 
     private BookParagraphsListDataControl bookParagraphsListDataControl;
 
     /**
      * Constructor.
      * 
      * @param imagePath
      *            Path to the background image
      * @param bookParagraphsListDataControl
      *            Data controller of the list of paragraphs
      */
     public BookParagraphPreviewPanel( BookDataControl bookDataControl, BookParagraphsListDataControl bookParagraphsListDataControl ) {
 
         super( bookDataControl );
         currentArrowLeft = arrowLeftNormal;
         currentArrowRight = arrowRightNormal;
         this.bookParagraphsListDataControl = bookParagraphsListDataControl;
         updatePreview( );
 
         // Add the click listener
         ClickMouseListener l = new ClickMouseListener( );
         addMouseListener( l );
         this.addMouseMotionListener( l );
     }
 
     public void updatePreview( ) {

         // First page only
         currentPage = 0;
 
         // Create the array with the images and fill it
         int totalHeight = 0;
         List<ParagraphImage> paragraphImages = new ArrayList<ParagraphImage>( );
         for( BookParagraphDataControl bookParagraphDataControl : bookParagraphsListDataControl.getBookParagraphs( ) ) {
             ParagraphImage paragraphImage = null;
 
             // Create the paragraph image depending on the paragraph type
             if( bookParagraphDataControl.getType( ) == Controller.BOOK_TEXT_PARAGRAPH )
                 paragraphImage = createTextParagraphImage( bookParagraphDataControl.getParagraphContent( ) );
             else if( bookParagraphDataControl.getType( ) == Controller.BOOK_TITLE_PARAGRAPH )
                 paragraphImage = createTitleParagraphImage( bookParagraphDataControl.getParagraphContent( ) );
             else if( bookParagraphDataControl.getType( ) == Controller.BOOK_BULLET_PARAGRAPH )
                 paragraphImage = createBulletImageParagraph( bookParagraphDataControl.getParagraphContent( ) );
             else if( bookParagraphDataControl.getType( ) == Controller.BOOK_IMAGE_PARAGRAPH )
                 paragraphImage = createImageParagraphImage( bookParagraphDataControl.getParagraphContent( ) );
 
             // If the paragraph is not null, add it to the list
             if( paragraphImage.image != null ) {
                 totalHeight += paragraphImage.image.getHeight( null );
                 paragraphImages.add( paragraphImage );
             }
 
         }
         
         // Calculate the number of pages
         pageCount = (int) Math.ceil( (double) totalHeight / (double) PAGE_TEXT_HEIGHT );
 
         // Create the image of the book and extract the graphics
         int y = 0;
         bookContinousImage = new BufferedImage( TEXT_WIDTH, totalHeight + PAGE_TEXT_HEIGHT, BufferedImage.TYPE_INT_ARGB );
 
         // Paint each paragraph image
         for( ParagraphImage paragraphImage : paragraphImages ) {
             // If the paragraph can't be split and doesn't fit in the current page, jump to the next one
             if( !paragraphImage.canBeSplitted && ( y % PAGE_TEXT_HEIGHT ) + paragraphImage.image.getHeight( null ) > PAGE_TEXT_HEIGHT ) {
                 y += ( PAGE_TEXT_HEIGHT - ( y % PAGE_TEXT_HEIGHT ) );
                 // Paint the entire paragraph
                 bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, null );
 
                 y += paragraphImage.image.getHeight( null );
             }
             // It it's a title and the next line doesn't fit in the current line
             else if( paragraphImage.isTitle && ( y % PAGE_TEXT_HEIGHT ) + paragraphImage.image.getHeight( null ) > PAGE_TEXT_HEIGHT ) {
                 // Lines in the title
                 int linesNumber = paragraphImage.image.getHeight( null ) / TITLE_HEIGHT;
                 // Lines that still fit
                 int linesRemain = (int) Math.floor( ( PAGE_TEXT_HEIGHT - ( y % PAGE_TEXT_HEIGHT ) ) / TITLE_HEIGHT );
 
                 if( linesRemain > linesNumber ) {
                     // Paint the entire title
                     bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, null );
 
                     y += paragraphImage.image.getHeight( null );
                 }
                 else {
                     int height_tal = TITLE_HEIGHT * linesRemain;
                     //bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, paragraphImage.image.getWidth( null ), height_tal, null );
                     bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, paragraphImage.image.getWidth( null ), y + height_tal, 0, 0, paragraphImage.image.getWidth( null ), linesRemain * TITLE_HEIGHT, null );
                     // Change page and draw the remaining lines
                     y += ( PAGE_TEXT_HEIGHT - ( y % PAGE_TEXT_HEIGHT ) );
                     bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, paragraphImage.image.getWidth( null ), y + TITLE_HEIGHT * ( linesNumber - linesRemain ), 0, TITLE_HEIGHT * linesRemain, paragraphImage.image.getWidth( null ), TITLE_HEIGHT * linesNumber, null );
                     y += TITLE_HEIGHT * ( linesNumber - linesRemain );
                 }
             }
             else {
                 // Paint the entire paragraph
                 bookContinousImage.getGraphics( ).drawImage( paragraphImage.image, 0, y, paragraphImage.image.getWidth( null ), paragraphImage.image.getHeight( null ), null );
 
                 y += paragraphImage.image.getHeight( null );
             }
         }
 
         updateUI( );
     }
 
     @Override
     public void paint( Graphics g ) {
 
         //super.paint( g );
 
         // If the image is loaded, draw the elements
         if( isImageLoaded( ) ) {
             paintBackground( g );
             
             if( currentPage > 1 )
                 if( currentArrowLeft != null ) {
                     g.drawImage( currentArrowLeft, getAbsoluteX( previousPagePoint.x ), getAbsoluteY( previousPagePoint.y ), getAbsoluteWidth( arrowLeftNormal.getWidth( null ) ), getAbsoluteHeight( arrowLeftNormal.getHeight( null ) ), null );
                 }
 
             if( currentPage + 1 < pageCount - 1 )
                 if( currentArrowRight != null ) {
                     g.drawImage( currentArrowRight, getAbsoluteX( nextPagePoint.x ), getAbsoluteY( nextPagePoint.y ), getAbsoluteWidth( currentArrowRight.getWidth( null ) ), getAbsoluteHeight( currentArrowRight.getHeight( null ) ), null );
                 }
             
             // Draw the first page
             g.drawImage( bookContinousImage, getAbsoluteX( TEXT_FIRST_COLUMN ), getAbsoluteY( TEXT_TOP_POSITION + 5 ), getAbsoluteX( TEXT_FIRST_COLUMN + TEXT_WIDTH ), getAbsoluteY( TEXT_TOP_POSITION + PAGE_TEXT_HEIGHT + 5 ), 0, currentPage * PAGE_TEXT_HEIGHT, TEXT_WIDTH, ( currentPage + 1 ) * PAGE_TEXT_HEIGHT, null );
             // If there is second page, draw it
             if( currentPage < pageCount - 1 ) {
                 g.drawImage( bookContinousImage, getAbsoluteX( TEXT_SECOND_COLUMN ), getAbsoluteY( TEXT_TOP_POSITION + 5 ), getAbsoluteX( TEXT_SECOND_COLUMN + TEXT_WIDTH ), getAbsoluteY( TEXT_TOP_POSITION + PAGE_TEXT_HEIGHT + 5 ), 0, ( currentPage + 1 ) * PAGE_TEXT_HEIGHT, TEXT_WIDTH, ( currentPage + 2 ) * PAGE_TEXT_HEIGHT, null );
             }
         }
     }
 
     /**
      * Returns the image for a text paragraph.
      * 
      * @param text
      *            Text of the text paragraph
      * @return Image paragraph, containing the image and aditional information
      */
     private ParagraphImage createTextParagraphImage( String text ) {
 
         // Create variables for the process
         List<String> textLines = new ArrayList<String>( );
         String word = "";
         String line = "";
 
         // Font for the measures
         Font font = new Font( "Dialog", Font.PLAIN, 18 );
 
         // While there is still text to be process
         while( !text.equals( "" ) ) {
             // Get the first char
             char c = text.charAt( 0 );
             // And the rest of the text (without that char)
             text = text.substring( 1 );
             // If the first char is a new line
             if( c == '\n' ) {
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 if( r.getWidth( ) < TEXT_WIDTH ) {
                     // Finish the line with the current word
                     line = line + word;
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // Empty line and word
                     word = "";
                     line = "";
                 }
                 else {
                     textLines.add( line );
                     textLines.add( word.substring( 1 ) );
                     line = "";
                     word = "";
                 }
             }
             // If its a white space
             else if( Character.isWhitespace( c ) ) {
                 // Get the width of the line and the word
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 // If its width size don't go out of the line text width
                 if( r.getWidth( ) < TEXT_WIDTH ) {
                     // Add the word to the line
                     line = line + word;
                     word = " ";
                 }
                 // If it goes out
                 else {
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // The line is now the word
                     line = word.substring( 1 ) + " ";
                     word = "";
                 }
             }
             // Else we do some more checking 
             else {
                 Rectangle2D r = font.getStringBounds( line + word + c, new FontRenderContext( null, false, true ) );
                 if( r.getWidth( ) < TEXT_WIDTH )
                     word = word + c;
                 else {
                     if( line != "" )
                         textLines.add( line );
                     line = "";
                     Rectangle2D r2 = font.getStringBounds( word + c, new FontRenderContext( null, false, true ) );
                     if( r2.getWidth( ) < TEXT_WIDTH )
                         word = word + c;
                     else {
                         textLines.add( word );
                         word = "" + c;
                     }
                 }
             }
         }
         // All the text has been process except the last line and last word
         Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
         if( r.getWidth( ) < TEXT_WIDTH ) {
             line = line + word;
         }
         else {
             textLines.add( line );
             line = word.substring( 1 );
         }
         textLines.add( line );
 
         // Create the image to draw
         Image paragraphImage = new BufferedImage( TEXT_WIDTH, textLines.size( ) * LINE_HEIGHT, BufferedImage.TYPE_INT_ARGB );
         Graphics g = paragraphImage.getGraphics( );
         g.setFont( font );
         g.setColor( Color.DARK_GRAY );
 
         // For each line of the text book
         int x = 0;
         int y = 0;
         for( int i = 0; i < textLines.size( ); i++ ) {
             // Draw the line string
             line = textLines.get( i );
             g.drawString( line, x, y + LINE_HEIGHT - 9 );
 
             // Add the line height to the Y coordinate for the next line
             y = y + LINE_HEIGHT;
         }
 
         return new ParagraphImage( paragraphImage, true );
     }
 
     /**
      * Returns the image for a title paragraph.
      * 
      * @param text
      *            Text of the title paragraph
      * @return Image paragraph, containing the image and aditional information
      */
     private ParagraphImage createTitleParagraphImage( String text ) {
 
         // Create variables
         List<String> textLines = new ArrayList<String>( );
         String word = "";
         String line = "";
 
         // Create the font
         Font font = new Font( "Dialog", Font.PLAIN, 32 );
 
         // While there is still text to be process
         while( !text.equals( "" ) ) {
             // Get the first char
             char c = text.charAt( 0 );
             // Add the rest of the text (without that char)
             text = text.substring( 1 );
             // If the first char is a new line
             if( c == '\n' ) {
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 if( r.getWidth( ) < TEXT_WIDTH ) {
                     // Finish the line with the current word
                     line = line + word;
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // Empy line and word
                     word = "";
                     line = "";
                 }
                 else {
                     textLines.add( line );
                     textLines.add( word.substring( 1 ) );
                     line = "";
                     word = "";
                 }
             }
             // If its a white space
             else if( Character.isWhitespace( c ) ) {
                 // Get the width of the line and the word
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 // If its width size don't go out of the line text width
                 if( r.getWidth( ) < TEXT_WIDTH ) {
                     // Add the word to the line
                     line = line + word;
                     word = " ";
                 }
 
                 // If it goes out
                 else {
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // The line is now the word
                     line = word.substring( 1 ) + " ";
                     word = "";
                 }
             }
 
             // Else we add it to the current word
             else {
                 Rectangle2D r = font.getStringBounds( line + word + c, new FontRenderContext( null, false, true ) );
                 if( r.getWidth( ) < TEXT_WIDTH )
                     word = word + c;
                 else {
                     if( line != "" )
                         textLines.add( line );
                     line = "";
                     Rectangle2D r2 = font.getStringBounds( word + c, new FontRenderContext( null, false, true ) );
                     if( r2.getWidth( ) < TEXT_WIDTH )
                         word = word + c;
                     else {
                         textLines.add( word );
                         word = "" + c;
                     }
                 }
             }
         }
 
         // All the text has been process except the last line and last word
         Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
         if( r.getWidth( ) < TEXT_WIDTH ) {
             line = line + word;
         }
         else {
             textLines.add( line );
             line = word.substring( 1 );
         }
         textLines.add( line );
 
         // Create the image to draw
         Image paragraphImage = new BufferedImage( TEXT_WIDTH, textLines.size( ) * TITLE_HEIGHT, BufferedImage.TYPE_INT_ARGB );
         Graphics g = paragraphImage.getGraphics( );
         g.setFont( font );
         g.setColor( Color.DARK_GRAY );
 
         // X and Y coordinates
         int x = 0;
         int y = 0;
 
         // For each line of the title paragraph
         for( int i = 0; i < textLines.size( ); i++ ) {
             // Draw the line string
             line = textLines.get( i );
             g.drawString( line, x, y + TITLE_HEIGHT - 15 );
 
             // Add the line height to the Y coordinate for the next line
             y = y + TITLE_HEIGHT;
         }
 
         return new ParagraphImage( paragraphImage, true, true );
     }
 
     /**
      * Returns the image for a bullet paragraph.
      * 
      * @param text
      *            Text of the bullet paragraph
      * @return Image paragraph, containing the image and aditional information
      */
     private ParagraphImage createBulletImageParagraph( String text ) {
 
         // Create the variables
         List<String> textLines = new ArrayList<String>( );
         String word = "";
         String line = "";
 
         // Font for the measures
         Font font = new Font( "Dialog", Font.PLAIN, 18 );
 
         // While there is still text to be process
         while( !text.equals( "" ) ) {
             // Get the first char
             char c = text.charAt( 0 );
             // And the rest of the text (without that char)
             text = text.substring( 1 );
             // If the first char is a new line
             if( c == '\n' ) {
                 // Get the width of the line and the word
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 // If its width size don't go out of the line text width
                 if( r.getWidth( ) < TEXT_WIDTH_BULLET ) {
                     // Finish the line with the current word
                     line = line + word;
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // Empty line and word
                     word = "";
                     line = "";
                 }
                 else {
                     textLines.add( line );
                     textLines.add( word.substring( 1 ) );
                     word = "";
                     line = "";
                 }
             }
 
             // If its a white space
             else if( Character.isWhitespace( c ) ) {
                 // Get the width of the line and the word
                 Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
                 // If its width size don't go out of the line text width
                 if( r.getWidth( ) < TEXT_WIDTH_BULLET ) {
                     // Add the word to the line
                     line = line + word;
                     word = " ";
                 }
 
                 // If it goes out
                 else {
                     // Add the line to the text of the bullet book
                     textLines.add( line );
                     // The line is now the word
                     line = word.substring( 1 ) + " ";
                     word = "";
                 }
             }
 
             // Else we add it to the current word
             else{
                 Rectangle2D r = font.getStringBounds( line + word + c, new FontRenderContext( null, false, true ) );
                 if( r.getWidth( ) < TEXT_WIDTH_BULLET )
                     word = word + c;
                 else {
                     if( line != "" )
                         textLines.add( line );
                     line = "";
                     Rectangle2D r2 = font.getStringBounds( word + c, new FontRenderContext( null, false, true ) );
                     if( r2.getWidth( ) < TEXT_WIDTH_BULLET )
                         word = word + c;
                     else {
                         textLines.add( word );
                         word = "" + c;
                     }
                 }
             }
         }
 
         // All the text has been process except the last line and last word
         Rectangle2D r = font.getStringBounds( line + " " + word, new FontRenderContext( null, false, true ) );
         if( r.getWidth( ) < TEXT_WIDTH_BULLET ) {
             line = line + word;
         }
         else {
             textLines.add( line );
             line = word.substring( 1 );
         }
         textLines.add( line );
 
         // Create the image to draw
         Image paragraphImage = new BufferedImage( TEXT_WIDTH, textLines.size( ) * LINE_HEIGHT, BufferedImage.TYPE_INT_ARGB );
         Graphics g = paragraphImage.getGraphics( );
         g.setFont( font );
         g.setColor( Color.DARK_GRAY );
 
         // For each line of the bullet book
         int x = TEXT_WIDTH - TEXT_WIDTH_BULLET;
         int y = 0;
         for( int i = 0; i < textLines.size( ); i++ ) {
             // If its the first line, we draw the bullet
             if( i == 0 )
                 g.fillOval( 5, y + 5, x - 15, x - 15 );
 
             // Paint the string
             line = textLines.get( i );
             g.drawString( line, x, y + LINE_HEIGHT - 9 );
 
             // Add the line height to the Y coordinate for the next line
             y = y + LINE_HEIGHT;
         }
 
         return new ParagraphImage( paragraphImage, true );
     }
 
     /**
      * Returns the image for an image paragraph.
      * 
      * @param text
      *            Text of the image paragraph
      * @return Image paragraph, containing the image and aditional information
      */
     private ParagraphImage createImageParagraphImage( String path ) {
         
         // Load the real image
         Image image = null;
         
         if ( path != null && path != ""  )
             image = AssetsController.getImage( path );
 
         // Create the image to draw
         Image paragraphImage = null;
         if( image != null ) {
             paragraphImage = new BufferedImage( TEXT_WIDTH, (int) Math.ceil( image.getHeight( null ) / (double) LINE_HEIGHT ) * LINE_HEIGHT, BufferedImage.TYPE_INT_ARGB );
             Graphics g = paragraphImage.getGraphics( );
             g.drawImage( image, 0, 0, null );
         }
 
         return new ParagraphImage( paragraphImage, false );
     }
 
     /**
      * Listener for the clicks in the book panel.
      */
     private class ClickMouseListener extends MouseAdapter {
 
         @Override
         public void mouseClicked( MouseEvent e ) {
 
             // Take the position of the click
             int x = e.getX( );
             int y = e.getY( );
 
             if( isInPreviousPage( x, y ) ) {
                 // If the "Previous page" button was pressed
                 if( currentPage - 2 >= 0 )
                     currentPage -= 2;
                 repaint( );
             }
 
             // If the "Next page" button was pressed
             else if( isInNextPage( x, y ) ) {
                 if( currentPage + 2 < pageCount )
                     currentPage += 2;
                 repaint( );
             }
         }
 
         @Override
         public void mouseMoved( MouseEvent e ) {
 
             // Take the position of the click
             int x = e.getX( );
             int y = e.getY( );
 
             if( isInPreviousPage( x, y ) ) {
                 // If the "Previous page" button was pressed
                 currentArrowLeft = arrowLeftOver;
             }
             else
                 currentArrowLeft = arrowLeftNormal;
             // If the "Next page" button was pressed
             if( isInNextPage( x, y ) ) {
                 currentArrowRight = arrowRightOver;
             }
             else
                 currentArrowRight = arrowRightNormal;
 
             repaint( );
         }
 
     }
 
     /**
      * Internal class holding and image, and if it can be splitted or not.
      */
     private class ParagraphImage {
 
         /**
          * Image.
          */
         public Image image;
 
         /**
          * True if the paragraph can be splitted.
          */
         public boolean canBeSplitted;
 
         /**
          * True if the paragraph is a title
          */
         public boolean isTitle;
 
         /**
          * Constructor.
          * 
          * @param image
          *            Image of the paragraph
          * @param canBeSplitted
          *            True if the paragraph can be splitted
          */
         public ParagraphImage( Image image, boolean canBeSplitted, boolean isTitle ) {
 
             this.isTitle = isTitle;
             this.image = image;
             this.canBeSplitted = canBeSplitted;
         }
 
         public ParagraphImage( Image image, boolean canBeSplitted ) {
 
             this( image, canBeSplitted, false );
         }
     }
 }
