 package de.mancino.auctioneer.aspell;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.mancino.auctioneer.exceptions.ASpellLexerException;
 import de.mancino.auctioneer.exceptions.ASpellParserException;
 
 public class ParserTest {
     private static ASpellTestArmoryItemBO armoryItemBO;
     @BeforeClass
     public static void initArmoryItemBO() {
         armoryItemBO = new ASpellTestArmoryItemBO();
         armoryItemBO.addItem(1, "Golden Lotus");
         armoryItemBO.addItem(2, "Green Tea Leaf");
         armoryItemBO.addItem(3, "Fool's Cap");
         armoryItemBO.addItem(4, "Silkweed");
         armoryItemBO.addItem(5, "Ghost Iron Ore");
         armoryItemBO.addItem(6, "Black Trillium Ore");
         armoryItemBO.addItem(7, "White Trillium Ore");
         armoryItemBO.addItem(8, "Kyparite");
         armoryItemBO.addItem(9, "Embersilk Bag");
         armoryItemBO.addItem(10, "Bolt of Embersilk Cloth");
         armoryItemBO.addItem(11, "Hypnotic Dust");
         armoryItemBO.addItem(12, "Embersilk Cloth");
         armoryItemBO.addItem(13, "Ethereal Shard");
         armoryItemBO.addItem(14, "Small Ethereal Shard");
         armoryItemBO.addItem(15, "Exotic Leather");
         armoryItemBO.addItem(16, "Sha-Touched Leather");
     }
 
     @Test
     public void smokeTestCreateFarming() throws ASpellParserException, ASpellLexerException {
         final String input = "CREATE FARMING \"Herbalism Basic Route\" ICON \"Golden Lotus\" YIELDS 44 * \"Green Tea Leaf\", 11 * \"Fool's Cap\", 95 * \"Silkweed\" ADD 112000;"
                 + "CREATE FARMING \"Mining Basic Route\" ICON \"Ghost Iron Ore\" YIELDS 256 * \"Ghost Iron Ore\", 2 * \"Black Trillium Ore\", 2 * \"White Trillium Ore\", 2 * \"Kyparite\";";
        Parser.parse(Lexer.lex(input), armoryItemBO, null, null, null, null, null);
     }
 
     @Test
     public void smokeTestCreateStrategy() throws ASpellParserException, ASpellLexerException {
         final String input = "CREATE STRATEGY \"Embersilk Bag\" FROM 15 * \"Bolt of Embersilk Cloth\", 15 * \"Hypnotic Dust\";" +
                         "CREATE STRATEGY \"Embersilk Bag\" FROM 75 * \"Embersilk Cloth\", 15 * \"Hypnotic Dust\";" +
                         "CREATE STRATEGY \"Ethereal Shard\" FROM 3 * \"Small Ethereal Shard\";" +
                         "CREATE STRATEGY \"Exotic Leather\" FROM 5 * \"Sha-Touched Leather\";";
 
        Parser.parse(Lexer.lex(input), armoryItemBO, null, null, null, null, null);
     }
 
 
 }
