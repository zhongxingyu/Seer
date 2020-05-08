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
 package es.eucm.eadventure.common.data.chapter.book;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.Documented;
 import es.eucm.eadventure.common.data.HasId;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 
 /**
  * This class holds a "bookscene" data
  */
 public class Book implements Cloneable, Documented, HasId {
 
     /**
      * The xml tag for the background image of the bookscene
      */
     public static final String RESOURCE_TYPE_BACKGROUND = "background";
     
     /**
      * XML tags for the arrows image for the book
      */
     public static final String RESOURCE_TYPE_ARROW_LEFT_NORMAL = "arrowLeftNormal";
     public static final String RESOURCE_TYPE_ARROW_RIGHT_NORMAL = "arrowRightNormal";
     public static final String RESOURCE_TYPE_ARROW_LEFT_OVER = "arrowLeftOver";
     public static final String RESOURCE_TYPE_ARROW_RIGHT_OVER = "arrowRightOver";
     
 
     public static final int TYPE_PARAGRAPHS = 0;
 
     public static final int TYPE_PAGES = 1;
 
     /**
      * Id of the book
      */
     private String id;
 
     /**
      * Documentation of the book.
      */
     private String documentation;
 
     /**
      * Set of resources for the book
      */
     private List<Resources> resources;
 
     /**
      * Paragraphs of the book
      */
     private List<BookParagraph> paragraphs;
 
     /**
      * Pages of the book: Used in case type is {@value #TYPE_PAGES} or
      * {@value #TYPE_SCROLL_PAGE}.
      */
     private ArrayList<BookPage> pages;
     
     /**
      * Previous page point
      */
     private Point previousPagePoint;
     
     /**
      * Next page point
      */
     private Point nextPagePoint;
 
     
     public Point getPreviousPagePoint( ) {
     
         return previousPagePoint;
     }
 
     
     public void setPreviousPagePoint( Point previousPagePoint ) {
     
         this.previousPagePoint = previousPagePoint;
     }
 
     
     public Point getNextPagePoint( ) {
     
         return nextPagePoint;
     }
 
     
     public void setNextPagePoint( Point nextPagePoint ) {
     
         this.nextPagePoint = nextPagePoint;
     }
 
     /**
      * {@link #TYPE_PAGES}, {@link #TYPE_PARAGRAPHS}
      */
     private int type;
 
     /**
      * Creates a new Book
      * 
      * @param id
      *            the id of the book
      */
     public Book( String id ) {
 
         this.id = id;
         this.type = TYPE_PARAGRAPHS;
         resources = new ArrayList<Resources>( );
         paragraphs = new ArrayList<BookParagraph>( );
         pages = new ArrayList<BookPage>( );
     }
 
     /**
      * Returns the book's id
      * 
      * @return the book's id
      */
     public String getId( ) {
 
         return id;
     }
 
     /**
      * Returns the documentation of the book.
      * 
      * @return the documentation of the book
      */
     public String getDocumentation( ) {
 
         return documentation;
     }
 
     /**
      * Returns the book's list of resources
      * 
      * @return the book's list of resources
      */
     public List<Resources> getResources( ) {
 
         return resources;
     }
 
     /**
      * Adds some resources to the list of resources
      * 
      * @param resources
      *            the resources to add
      */
     public void addResources( Resources resources ) {
 
         this.resources.add( resources );
     }
 
     /**
      * Returns the book's set of paragraphs
      * 
      * @return The book's set of paragraphs
      */
     public List<BookParagraph> getParagraphs( ) {
 
         return paragraphs;
     }
 
     /**
      * Adds a page to the book with margin
      * 
      * @param page
      *            New page (url) to be added
      */
 
     public void addPage( String uri, int type, int margin, boolean scrollable ) {
 
         pages.add( new BookPage( uri, type, margin, scrollable ) );
     }
 
     /**
      * Adds a page to the book with margin
      * 
      * @param page
      *            New page (url) to be added
      */
 
     public void addPage( String uri, int type, int margin, int marginEnd, int marginTop, int marginBottom, boolean scrollable ) {
 
         pages.add( new BookPage( uri, type, margin, marginEnd, marginTop, marginBottom, scrollable ) );
     }
 
     /**
      * Adds a page to the book
      * 
      * @param page
      *            New page (url) to be added
      */
 
     public void addPage( String uri, int type ) {
 
         pages.add( new BookPage( uri, type ) );
     }
 
     /**
      * @return the type
      */
     public int getType( ) {
 
         return type;
     }
 
     /**
      * @param type
      *            the type to set
      */
     public void setType( int type ) {
 
         this.type = type;
     }
 
     /**
      * @return the pageURLs
      */
     public ArrayList<BookPage> getPageURLs( ) {
 
         return pages;
     }
 
     /**
      * Sets the a new identifier for the book.
      * 
      * @param id
      *            New identifier
      */
     public void setId( String id ) {
 
         this.id = id;
     }
 
     /**
      * Changes the documentation of this book.
      * 
      * @param documentation
      *            The new documentation
      */
     public void setDocumentation( String documentation ) {
 
         this.documentation = documentation;
     }
 
     /**
      * Adds a paragraph to the book
      * 
      * @param paragraph
      *            New paragraph to be added
      */
     public void addParagraph( BookParagraph paragraph ) {
 
         paragraphs.add( paragraph );
     }
 
     @Override
     public Object clone( ) throws CloneNotSupportedException {
 
         Book b = (Book) super.clone( );
         b.documentation = ( documentation != null ? new String( documentation ) : null );
         b.id = ( id != null ? new String( id ) : null );
         if( pages != null ) {
             b.pages = new ArrayList<BookPage>( );
             for( BookPage bp : pages )
                 b.pages.add( (BookPage) bp.clone( ) );
         }
         if( paragraphs != null ) {
             b.paragraphs = new ArrayList<BookParagraph>( );
             for( BookParagraph bp : paragraphs )
                 b.paragraphs.add( (BookParagraph) bp.clone( ) );
         }
         if( resources != null ) {
             b.resources = new ArrayList<Resources>( );
             for( Resources r : resources )
                 b.resources.add( (Resources) r.clone( ) );
         }
         b.type = type;
         return b;
     }
 }
