 package roguelike.rpg.sisyphean;
 
 import junit.framework.TestCase;
 
 
 /**
  * // -------------------------------------------------------------------------
 /**
  *  Tests the character class.
  *
  *  @author Pietas
  *  @version Nov 4, 2012
  */
 public class WarriorAndCharacterTest extends TestCase
 {
     private Warrior newChar;
 
     /**
      * Instantiates a warrior class character.
      */
     public void setUp()
     {
         newChar = new Warrior("Sisyphean");
     }
 
     //Test Methods-------------------------------------------------------------
 
     /**
      * Tests the name (getter and setter) methods.
      */
     public void testName()
     {
         assertEquals("Sisyphean", newChar.getName());
         newChar.setName("Ralph");
         assertEquals("Ralph", newChar.getName());
     }
 
     /**
      * Tests the level (getter and setter) methods.
      */
     public void testLevel()
     {
         assertEquals(1, newChar.getLevel());
         newChar.setLevel(2);
         assertEquals(2, newChar.getLevel());
    }

    /**
     * Tests the max health (getter and setter) methods.
     */
    public void testMaxHealth()
    {
        assertEquals(120.0f, newChar.getMaxHealth());

        newChar.setMaxHealth(220);

        assertEquals(220.0f, newChar.getMaxHealth());
        newChar.setHealth(110);
        newChar.setMaxHealth(100);

        assertEquals(50.0f, newChar.getHealth());


    }

    /**
     * Tests the current health (getter and setter) methods.
     */
    public void testCurrentHealth()
    {
        newChar.setMaxHealth(120);
        assertEquals(120.0f, newChar.getHealth());

        newChar.setHealth(150);
        assertEquals(120.0f, newChar.getHealth());




 
     }
 }
