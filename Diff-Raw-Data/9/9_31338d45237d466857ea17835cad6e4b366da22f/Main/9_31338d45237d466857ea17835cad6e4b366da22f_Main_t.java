 package arcade.database;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import arcade.games.Comment;
 import arcade.games.Score;
 
 
 public class Main {
     
     public static void main(String[] args) throws SQLException {
         
         Database myDatabase = new Database();
         
         
 //        myDatabase.createUser("natx13", "password", "Natalia", "Carvalho", "04/26/1991");
 //        myDatabase.createUser("kayzooo", "mypassword", "Kevin", "Zhu", "11/12/1990");
         
       System.out.println(myDatabase.getAverageRating("test"));
         
 //        List<Comment> myComments = myDatabase.retrieveCommentsForGame("test");
 //        System.out.println(myComments.size());
 //        for (Comment c : myComments) {
 //            System.out.println(c.getComment());
 //        }
         
 //        myDatabase.updateRating("natx13", "test", 4.0);
         //myDatabase.insertComment("natx13", "test", "this game is a piece of crap");
 
 //        List<String> myComments = myDatabase.retrieveCommentsForGame("test");
 //        for (String s : myComments) {
 //            System.out.println(s);
 //        }
 
         //myDatabase.addNewHighScore("test", "test", 100);
 //        List<Score> myScores = myDatabase.getScoresForGame("test");
 //        for (Score s : myScores) {
 //            System.out.println(s.getScore());
 //        }
         //myDatabase.addNewHighScore("natx13", "example", 157);
         
         //System.out.println(System.getProperty("user.dir"));
 
         //myDatabase.printUserTable();
         //myDatabase.printGameTable();
         
         //myDatabase.insertAvatar("natx13", "/Users/nataliacarvalho/Desktop/testing2.png");
         // myDatabase.getAvatar("natx13");
         
      //   myDatabase.createUser("natx13", "mypassword", "Natalia", "Carvalho", "04/26/1991");
      //   myDatabase.authenticateUsernameAndPassword("natx13", "password");
         
    //     myDatabase.createUser("doesthiswork", "hiiii", "hiii", "hiii", "11/12/1990");
    //     myDatabase.createUser("bob", "mypassword", "Joe", "Smith", "11/11/1911");   
   //      myDatabase.createUser("test", "test", "test", "test", "01/01/1901");
         
        // myDatabase.deleteGame("test");
   //      myDatabase.deleteGame("example");
         
 //       myDatabase.addAvatarToUser("natx13", "C:/blahbalhblah");
         
         //myDatabase.createGame("example", "examplegenre");
         
 //        myDatabase.authenticateUsernameAndPassword("kayzooo", "mypassword");
         
         //myDatabase.printUserGameDataTable();
         
   //      myDatabase.userPlaysGameFirst("kayzooo", "Pacman", "10");
         //myDatabase.updateHighScore("kayzooo", "Pacman", "1000");
         //System.out.println(myDatabase.retrieveDOB("natx13"));
         //System.out.println(myDatabase.retrieveAvatar("natx13"));
 
         //myDatabase.deleteUser("kayzooo");
         //myDatabase.deleteUser("natx13");
         
         //myGameDatabase.registerGame("Pacman");
         
         
 //        List<String> myGameNames = myDatabase.retrieveListOfGames();
 //        
 //        for (String game : myGameNames) {
 //            System.out.println(game);
 //        }
 //                
         myDatabase.printUserTable();
         myDatabase.printGameTable();
         myDatabase.printScoreTable();
         myDatabase.printCommentTable();
         
         myDatabase.closeDatabaseConnection();
         
 
     }
 
 }
