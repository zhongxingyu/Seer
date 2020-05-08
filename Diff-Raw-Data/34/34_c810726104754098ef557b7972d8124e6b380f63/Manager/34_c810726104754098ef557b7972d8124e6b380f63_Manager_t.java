 package mediastore;
 
 import java.io.IOException;
import java.sql.SQLException;
 import java.util.Scanner;
 
 /**
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Manager {
 
     private String password; //managers password
     private Database db; //the database this instance is a member of
 
     /**
      * Constructor with password and db parameters.
      *
      * @param password The manager password
      * @param db The database this instance belongs to
      */
     Manager( String password, Database db ) {
 
         this.password = password;
         this.db = db;
 
     }
 
     /**
      * Creates a new Media object with a CLI interface.
      *
      * @throws java.io.IOException
      */
    public void addCLI() throws java.io.IOException, SQLException {
 
         //Command Line prompt for manager to add desired item to Media store
         System.out.println( "What media type do you want to add? (0 to exit)" );
         System.out.println( "1. Album" );
         System.out.println( "2. Movie" );
         System.out.println( "3. Audiobook" );
         Scanner input = new Scanner( System.in ); //Scanner oject
 
         int choice = Integer.parseInt( input.nextLine() );
         switch ( choice ) {
             case 1:
                 System.out.print( "Enter the album artist: " );
                 String artist = input.nextLine();
                 System.out.print( "Enter the album title: " );
                 String albumTitle = input.nextLine();
                 System.out.print( "Enter the album duration: " );
                 int albumDuration = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the album's genre: " );
                 String albumGenre = input.nextLine();
                 System.out.print( "Enter the album's rating (out of 5 stars): " );
                 int albumRating = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the album's total number of reviews: " );
                 int totalAlbumReviews = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the album's price: " );
                 double albumPrice = Double.parseDouble( input.nextLine() );
                 Media newAlbum = new Album( 0, artist, albumTitle, albumDuration, albumGenre, albumRating, totalAlbumReviews, albumPrice, 0 );
                 db.media.add( newAlbum );
                 db.writeNewMediaItem( newAlbum );
 
                 break;
             case 2:
                 System.out.print( "Enter the movie director: " );
                 String director = input.nextLine();
                 System.out.print( "Enter the movie title: " );
                 String movieTitle = input.nextLine();
                 System.out.print( "Enter the movie duration: " );
                 int movieDuration = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the movie genre: " );
                 String movieGenre = input.nextLine();
                 System.out.print( "Enter the movie's rating (out of 5 stars): " );
                 int movieRating = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the movie's total number of reviews: " );
                 int totalMovieReviews = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the movie price: " );
                 double moviePrice = Double.parseDouble( input.nextLine() );
                 System.out.print( "Enter the movie release year: " );
                 int movieYear = Integer.parseInt( input.nextLine() );
                 Media newMovie = new Movie( 0, director, movieTitle, movieDuration, movieGenre, movieRating, totalMovieReviews, moviePrice, movieYear, 0 );
                 db.media.add( newMovie );
                 db.writeNewMediaItem( newMovie );
 
                 break;
             case 3:
                 System.out.print( "Enter the book's author: " );
                 String author = input.nextLine();
                 System.out.print( "Enter the book's title: " );
                 String bookTitle = input.nextLine();
                 System.out.print( "Enter the book's duration: " );
                 int bookDuration = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the book's genre: " );
                 String bookGenre = input.nextLine();
                 System.out.print( "Enter the book's rating (out of 5 stars): " );
                 int bookRating = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the book's total number of reviews: " );
                 int totalBookReviews = Integer.parseInt( input.nextLine() );
                 System.out.print( "Enter the book's price: " );
                 double bookPrice = Double.parseDouble( input.nextLine() );
                 Media newAudiobook = new Audiobook( 0, author, bookTitle, bookDuration, bookGenre, bookRating, totalBookReviews, bookPrice, 0 );
                 db.media.add( newAudiobook );
                 db.writeNewMediaItem( newAudiobook );
 
                 break;
         }
 
     }
 
     /**
      * Adds the specified Media object to the database, generating a new id in
      * the process.
      *
      * @param m The Media object to add to the database
      * @throws java.io.IOException
      */
    public void add( Media m ) throws java.io.IOException, SQLException {
 
         db.media.add( m );
         db.writeNewMediaItem( m );
     }
 
     /**
      * Adds the specified Media object to the database.
      *
      * @param m The Media object to add to the database
      * @param id The id of the new object
      * @throws java.io.IOException
      */
    public void add( Media m, int id ) throws java.io.IOException, SQLException {
         m.id = id;
         db.media.add( m );
         db.writeNewMediaItem( m, id );
     }
 
     /**
      * Removes the specified id from the database.
      *
      * @param id The id of the item to remove
      * @throws java.io.IOException
      */
    public void remove( int id ) throws java.io.IOException, SQLException {
         Media object = db.getMediaFromID( id ); //stores desired media item in temporary object
         db.media.remove( object );              //deletes media object from RAM
         db.deleteMediaItem( object );           //deletes media object from disk
 
     }
 
     /**
      * Gets the number sold for the given id.
      *
      * @param id The id to check
      * @return The number sold for the given id
      */
     public int getNumSales( int id ) {
 
         Media object = db.getMediaFromID( id );
         return object.numSold;
     }
 
     /**
      * Gets the total sales numbers for the entire music store.
      *
      * @return The total sales for the entire music store
      */
     public int getTotalNumSales() {
 
         int sum = 0;
         for ( Media m : db.media ) {
             sum += m.numSold;
         }
         return sum;
     }
 
     /**
      * Returns information about a particular Customer by id
      *
      * @param id The id to check
      * @return A string representation of the customer
      */
     public String getCustomerInfo( int id ) {
         Customer person = db.getCustomerFromID( id );
         return person.toString();
     }
 
     public boolean checkPassword( String password ) {
         return this.password.equals( password );
     }
 }
