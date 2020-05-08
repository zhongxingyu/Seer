 package jogoanatomia.services;
 
 import static org.junit.Assert.*;
 
 import jogoanatomia.entidades.AssociationGame;
 import jogoanatomia.entidades.HangmanGame;
 import jogoanatomia.entidades.QuizGame;
 import jogoanatomia.entidades.WordSearchesGame;
 import org.junit.Test;
 
 import java.util.List;
 
 // TODO: This is only a dummy test.
 public class GameServiceTest {
     private GameService service = new GameServiceImpl();
 
    private final String ORRGAN_ID = "5241a3234afaca7a9a000002";

     @Test
     public void listHangmanGamesByOrganIdTest() {
       List<HangmanGame> hangmanGames = service.listHangmanGamesByOrganId(ORRGAN_ID);
 
         assertNotNull(hangmanGames);
         assertFalse(hangmanGames.isEmpty());
 
         for (HangmanGame game: hangmanGames) {
             assertNotNull(game.getId());
             assertNotNull(game.getTip());
             assertNotNull(game.getAnswer());
             assertNotNull(game.getScore());
             assertNotNull(game.getOrganId());
         }
     }
 
     @Test
     public void listAssociationGamesByOrganIdTest() {
        List<AssociationGame> associationGames = service.listAssociationGamesByOrganId(ORRGAN_ID);
 
         assertNotNull(associationGames);
         assertFalse(associationGames.isEmpty());
 
         for (AssociationGame game: associationGames) {
             assertNotNull(game.getId());
             assertNotNull(game.getOrganId());
             assertNotNull(game.getScore());
             assertNotNull(game.getTipAnswers());
             assertFalse(game.getTipAnswers().isEmpty());
         }
     }
 
     @Test
     public void listQuizGamesByOrganIdTest() {
        List<QuizGame> quizGames = service.listQuizGamesByOrganId(ORRGAN_ID);
 
         assertNotNull(quizGames);
         assertFalse(quizGames.isEmpty());
 
         for (QuizGame game: quizGames) {
             assertNotNull(game.getId());
             assertNotNull(game.getOrganId());
             assertNotNull(game.getScore());
             assertNotNull(game.getQuestion());
             assertNotNull(game.getOptionA());
             assertNotNull(game.getOptionA());
             assertNotNull(game.getOptionB());
             assertNotNull(game.getOptionC());
             assertNotNull(game.getOptionD());
             assertNotNull(game.getAnswer());
         }
     }
 
     @Test
     public void listWordSearchesGamesByOrganIdTest() {
        List<WordSearchesGame> wordSearchesGames = service.listWordSearchesGamesByOrganId(ORRGAN_ID);
 
         assertNotNull(wordSearchesGames);
         assertFalse(wordSearchesGames.isEmpty());
 
         for (WordSearchesGame game: wordSearchesGames) {
             assertNotNull(game.getId());
             assertNotNull(game.getOrganId());
             assertNotNull(game.getScore());
             assertNotNull(game.getWords());
             assertFalse(game.getWords().isEmpty());
         }
     }
 }
