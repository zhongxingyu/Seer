 package mediastore;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 /**
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Manager {
 
     private String password; //managers password
     private Database db; //the database this instance is a member of
 
     Manager( String password, Database db ) {
 
         this.password = password;
         this.db = db;
 
     }
 
     public void addContent() throws java.io.IOException {
 
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
                 db.writeNewMediaItem( newAudiobook );
 
                 break;
         }
 
     }
 
     public void removeContent( int id ) throws java.io.IOException {
         Media object = db.getMediaFromID( id ); //stores desired media item in temporary object
         db.media.remove( object );              //deletes media object from RAM
         db.deleteMediaItem( object );           //deletes media object from disc
 
     }
 
     public int checkItemSales( int id ) {
 
         Media object = db.getMediaFromID( id );
         return object.numSold;
     }
 
     public int checkTotalSales() {
 
         int sum = 0;
         for ( Media m : db.media ) {
             sum += m.numSold;
         }
         return sum;
     }
 
     public String getCustomerInfo( int id ) {
         Customer person = db.getCustomerFromID( id );
         return person.toString();
     }
 }
