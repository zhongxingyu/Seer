 /**
  * User: ivan
  * Date: 3-Feb-2005
  * Time: 11:37:45 AM
  */
 
 package org.pcl.parser;
 
import java.util.Arrays;

 /**
  * PCL Command representation
  */
 public class PCLCommand {
 
   public static final int TYPE_UNKNOWN = -1;
   /**
    * Denote a two characters PCL command
    */
   public static final int TYPE_TWO_CHARACTERS = 0;
   /**
    * Denote parameterized PCL command
    */
   public static final int TYPE_PARAMETERIZED = 1;
 
   /**
    * Command type
    */
   protected int type = TYPE_UNKNOWN;
 
   /**
    * Second char of the sequence
    */
   protected char second_char = (char) 0;
 
   /**
    * Third character of the sequence
    */
   protected char third_char = (char) 0;
 
   /**
    * Command value
    */
   protected String value = null;
 
   /**
    * break character
    */
   protected char break_char = (char) 0;
 
   /**
    * Data
    */
   protected byte[] data = null;
 
   /**
    * Return PCL command type
    *
    * @return command type
    */
   public int getType() {
     return type;
   }
 
   /**
    * Set PCL command type
    *
    * @param type command type to set
    */
   public void setType(int type) {
     this.type = type;
   }
 
   /**
    * Return PCL command parameterized character
    *
    * @return parameterized character or or <code>(char)0</code>
    *         if command is not a parameterized command or parameterized character hasn't been set yet
    */
   public char getParameterizedChar() {
     return (type == TYPE_PARAMETERIZED ? second_char : (char) 0);
   }
 
   /**
    * Return second character from the sequence
    *
    * @return second character from the sequence
    *         or <code>(char)0</code> if character hasn't been set
    */
   public char getSecond_char() {
     return second_char;
   }
 
   /**
    * Set second character of the sequence.
    *
    * @param second_char char
    */
   public void setSecond_char(char second_char) {
     this.second_char = second_char;
   }
 
   /**
    * Return third character from the sequence
    *
    * @return group character or <code>(char)0</code> if char not set
    *         or command is not a parameterized command.
    */
   public char getGroupCharacter() {
     return (type == TYPE_PARAMETERIZED ? third_char : (char) 0);
   }
 
   /**
    * Return third character from the sequence
    *
    * @return third character or <code>(char)0</code> if char not set
    *         or command is not a parameterized command.
    */
   public char getThird_char() {
     return third_char;
   }
 
   /**
    * Set third character of the sequence
    *
    * @param third_char character to set
    */
   public void setThird_char(char third_char) {
     this.third_char = third_char;
   }
 
   /**
    * Return command value
    *
    * @return command value or null if command is not parameterized command
    *         or value not set
    */
   public String getValue() {
     return value;
   }
 
   /**
    * Set command value
    *
    * @param value value to set
    */
   public void setValue(String value) {
     this.value = value;
   }
 
   /**
    * Return break character
    *
    * @return break character or <code>(char)0</code> if break character not set.
    */
   public char getBreak_char() {
     return break_char;
   }
 
   /**
    * Set break character
    *
    * @param break_char break character to set
    */
   public void setBreak_char(char break_char) {
     this.break_char = break_char;
   }
 
   /**
    * Return data for this command
    *
    * @return data or <code>null</code> if command is not a parameterized comand
    *         or no data available
    */
   public byte[] getData() {
     return data;
   }
 
   /**
    * Set command data
    *
    * @param data data to set
    */
   public void setData(byte[] data) {
     this.data = data;
   }
 
   /**
    * Return command length in bytes.
    *
    * @return command length, -1 when command is empty
    */
   public int getLength() {
     int len = -1;
     switch (type) {
       case TYPE_TWO_CHARACTERS:
         if (second_char != (char) 0) {
           len = 2;
         }
         break;
       case TYPE_PARAMETERIZED:
         if (second_char != (char) 0 && break_char != (char) 0) {
           len = 2 + (third_char != (char) 0 ? 1 : 0) + value.length() + 1 + (null != data ? data.length : 0);
         }
     }
     return len;
   }
 
   /**
    * Return pcl command including leading escape character.
    *
    * @return PCL command
    */
   public byte[] getCommand() {
     byte[] cmd;
     switch (type) {
       case TYPE_TWO_CHARACTERS:
         cmd = new byte[2];
         cmd[0] = (byte) 0x1B;
         cmd[1] = (byte) second_char;
         return cmd;
       case TYPE_PARAMETERIZED:
         int cnt = 0;
         cmd = new byte[getLength()];
         cmd[cnt] = (byte) 0x1B;
         cnt++;
         cmd[cnt] = (byte) second_char;
         cnt++;
         if ((char) 0 != third_char) {
           cmd[cnt] = (byte) third_char;
           cnt++;
         }
         char[] valChar = value.toCharArray();
         for (char aValChar : valChar) {
           cmd[cnt] = (byte) aValChar;
           cnt++;
         }
 
         cmd[cnt] = (byte) break_char;
         cnt++;
         if (null != data) {
           System.arraycopy(data, 0, cmd, cnt, data.length);
         }
         return cmd;
     }
     return null;
   }
 
   /**
    * Compare given command with this command and return
    * <code>true</code> if commands are equals, <code>false</code> when not.<br/>
    * <i><strong>Note:</strong>Only command signature will be compared.<br/>
    * Example: *pX will be equal to *p2000X </i>
    *
    * @param cmd command to compare
    * @see #getCommandSignature()
    */
   public boolean commandEqual(String cmd) {
     return getCommandSignature().equalsIgnoreCase(cmd);
   }
 
   /**
    * Return command signature.<br/>
    * The command signature is a command witout ESC character
    * and without data.<br/>
    * Example: for command ESC*p2000X signature will be *pX
    *
    * @return command signature;
    * @see #commandEqual(String)
    */
   public String getCommandSignature() {
     switch (type) {
       case TYPE_TWO_CHARACTERS :
         return "" + second_char;
       case TYPE_PARAMETERIZED :
         return "" + second_char + ((char) 0 != third_char ? "" + third_char : "") + break_char;
       default:
         return "";
     }
   }
 
   /**
    * Clear command values.
    */
   public void clear() {
     type = TYPE_UNKNOWN;
     second_char = (char) 0;
     third_char = (char) 0;
     break_char = (char) 0;
     value = null;
     data = null;
   }
 
   /**
    * Return human readable representation of PCL command.
    *
    * @return string representation of this command
    */
   @Override
   public String toString() {
     return toCommandString(this);
   }
 
   /**
    * Return human readable representation of the given PCL Command.<br/>
    * <b>Contributed by Dave Hill</b>
    *
    * @param command PCL Command
    * @return Command string representation
    */
   public static String toCommandString(PCLCommand command) {
     StringBuilder str = new StringBuilder("ESC ");
     if (command.type == TYPE_TWO_CHARACTERS) {
       str.append(command.second_char);
       switch(command.second_char) {
         case 'E':
           str.append(" - PRINTER RESET");
           break;
         case '9':
           str.append(" - CLEAR HORIZONTAL MARGINS");
           break;
         case 'Y':
           str.append(" - ENABLE DISPLAY FUNCTIONS");
           break;
         case 'Z':
           str.append(" - DISABLE DISPLAY FUNCTIONS");
           break;
       }
     } else {
       //parameterized
       str.append(command.second_char)
         .append(command.third_char == (char) 0 ? "" : String.valueOf(command.third_char))
         .append(command.value).append(command.break_char).append(" ");
       StringBuilder keyBuilder = new StringBuilder();
       if (command.second_char != 0) keyBuilder.append(command.second_char);
       if (command.third_char != 0) keyBuilder.append(command.third_char);
       if (command.break_char != 0) keyBuilder.append(command.break_char);
       String key = keyBuilder.toString();
       String description = null;
       //JOB CONTROL
       if ("%X".equals(key)) description = "UNIVERSAL EXIT (RESET)";
       if ("%bW".equals(key)) description = "CONFIGURATION IO";
       if ("&lX".equals(key)) description = "NUMBER OF COPIES";
       if ("&lS".equals(key)) description = "SIMPLEX/DUPLEX";
       if ("&lU".equals(key)) description = "LONG EDGE OFFSET REGISTRATION";
       if ("&lZ".equals(key)) description = "SHORT EDGE OFFSET REGISTRATION";
       if ("&aG".equals(key)) description = "DUPLEX PAGE SIDE SELECTION";
       if ("&lT".equals(key)) description = "JOB SEPARATION";
       if ("&lG".equals(key)) description = "OUTPUT BIN";
       if ("&uD".equals(key)) description = "UNIT OF MEASURE";
       //PAGE CONTROL
       if ("&lA".equals(key)) description = "PAGE SIZE";
       if ("&lH".equals(key)) description = "TRAY";
       if ("&lP".equals(key)) description = "PAGE LENGTH (OBSOLETE)";
       if ("&lO".equals(key)) description = "ORIENTATION";
       if ("&aP".equals(key)) description = "PRINT DIRECTION";
       if ("&cT".equals(key)) description = "CHARACTER TEXT PATH DIRECTION";
       if ("&tP".equals(key)) description = "TEXT PARSING METHOD";
       if ("&aL".equals(key)) description = "LEFT MARGIN";
       if ("&aM".equals(key)) description = "RIGHT MARGIN";
       if ("&lE".equals(key)) description = "TOP MARGIN";
       if ("&lF".equals(key)) description = "TEXT LENGTH";
       if ("&lL".equals(key)) description = "PERFORATION SKIP";
       if ("&kH".equals(key)) description = "HORIZONTAL MOTION INDEX";
       if ("&lC".equals(key)) description = "VERTICAL MOTION INDEX";
       if ("&lD".equals(key)) description = "LINE SPACING";
       //CURSOR POSITIONING
       if ("&aC".equals(key)) description = "HORZ CURSOR TO COLUMN";
       if ("&ah".equals(key)) description = "HORZ CRUSOR TO DECIPOINT";
       if ("*pX".equals(key)) description = "HORZ CURSOR TO UNIT-OF-MEASURE";
       if ("&aR".equals(key)) description = "VERT CURSOR TO ROW";
       if ("&aV".equals(key)) description = "VERT CURSOR TO DECIPOINT";
       if ("*pY".equals(key)) description = "VERT CURSOR TO UNIT-OF-MEASURE";
       if ("&kG".equals(key)) description = "LINE TERMINATION";
       if ("&fS".equals(key)) description = "PUSH/POP CURSOR POSITION";
       //FONT SELECTION
       if ("(ID".equals(key) || ")ID".equals(key)) description = "SYMBOL SET";
       if ("(sP".equals(key) || ")sP".equals(key)) description = "FONT SPACING";
       if ("(sH".equals(key) || ")sH".equals(key)) description = "FONT PITCH";
       if ("(sV".equals(key) || ")sV".equals(key)) description = "FONT HEIGHT";
       if ("(sS".equals(key) || ")sS".equals(key)) description = "FONT STYLE";
       if ("(sB".equals(key) || ")sB".equals(key)) description = "FONT STROKE WEIGHT";
       if ("(sT".equals(key) || ")sT".equals(key)) description = "FONT TYPEFACE";
       if ("(X".equals(key) || ")X".equals(key)) description = "FONT BY ID";
       if ("(3@".equals(key) || ")3@".equals(key)) description = "DEFAULT FONT";
       if ("&pX".equals(key)) description = "TRANSPARENT PRINT DATA";
       if ("&dD".equals(key)) description = "ENABLE UNDERLINE";
       if ("&d@".equals(key)) description = "DISABLE UNDERLINE";
       if ("*cD".equals(key)) description = "FONT ID";
       if ("*cF".equals(key)) description = "FONT CONTROL";
       if ("&nW".equals(key)) description = "FONT ALPHANUMERIC ID";
       if ("*cR".equals(key)) description = "SYMBOL SET ID CODE";
       if ("(fW".equals(key)) description = "DEFINE SYMBOL SET";
       if ("*cS".equals(key)) description = "SYMBOL SET MANAGEMENT";
       //FONT CREATION
       if (")sW".equals(key)) description = "FONT CREATION DESCRIPTOR/DATA";
       if ("*cE".equals(key)) description = "FONT CREATION CHARACTER CODE";
       if ("(sW".equals(key)) description = "FONT CREATION CHARACTER DESCRIPTOR/DATA";
       //MACROS
       if ("&fY".equals(key)) description = "MACRO ID";
       if ("&fX".equals(key)) description = "MACRO CONTROL";
       //PRINT MODEL
       if ("*vN".equals(key)) description = "SOURCE TRANSPARENCY MODE";
       if ("*vO".equals(key)) description = "PATTERN TRANSPARENCY MODE";
       if ("*cG".equals(key)) description = "AREA FILL ID";
       if ("*vT".equals(key)) description = "SELECT CURRENT PATTERN";
       if ("*pR".equals(key)) description = "SET PATTERN REFERENCE POINT";
       if ("*cQ".equals(key)) description = "PATTERN CONTROL";
       if ("*lO".equals(key)) description = "PATTERN LOGICAL OPERATION";
       if ("*lR".equals(key)) description = "PATTERN PIXEL PLACEMENT";
       //RECTANGULAR AREA FILL GRAPHICS
       if ("*cH".equals(key)) description = "HORZ RECTANGLE SIZE IN DECIPOINTS";
       if ("*cA".equals(key)) description = "HORZ RECTANGLE SIZE IN UNITS-OF-MEASURE";
       if ("*cV".equals(key)) description = "VERT RECTANGLE SIZE IN DECIPOINTS";
       if ("*cB".equals(key)) description = "VERT RECTANGLE SIZE IN UNITS-OF-MEASURE";
       if ("*cP".equals(key)) description = "FILL RECTANGULAR AREA";
       //RASTER GRAPHICS
       if ("*tR".equals(key)) description = "RASTER RESOLUTION";
       if ("*rF".equals(key)) description = "RASTER PRESENTATION";
       if ("*rT".equals(key)) description = "RASTER SOURCE HEIGHT";
       if ("*rS".equals(key)) description = "RASTER SOURCE WIDTH";
       if ("*tV".equals(key)) description = "RASTER DEST WIDTH";
       if ("*tV".equals(key)) description = "RASTER DEST HEIGHT";
       if ("*tH".equals(key)) description = "RASTER DEST WIDTH";
       if ("*tK".equals(key)) description = "RASTER SCALE ALGORITHM";
       if ("*rA".equals(key)) description = "RASTER START";
       if ("*bY".equals(key)) description = "RASTER Y OFFSET";
       if ("*bM".equals(key)) description = "RASTER COMPRESSION MODE";
       if ("*bW".equals(key)) description = "RASTER DATA";
       if ("*rB".equals(key) || "*rC".equals(key)) description = "RASTER END";
       //COLOR
       if ("*rU".equals(key)) description = "SIMPLE COLOR";
       if ("*vW".equals(key)) description = "CONFIGURE IMAGE DATA";
       if ("*vA".equals(key)) description = "COLOR COMPONENT ONE";
       if ("*vB".equals(key)) description = "COLOR COMPONENT TWO";
       if ("*vC".equals(key)) description = "COLOR COMPONENT THREE";
       if ("*vI".equals(key)) description = "COLOR INDEX";
       if ("*pP".equals(key)) description = "PUSH/POP PALETTE";
       if ("&pS".equals(key)) description = "SELECT PALETTE";
       if ("&pI".equals(key)) description = "PALETTE CONTROL ID";
       if ("&pC".equals(key)) description = "PALETTE CONTROL";
       if ("*vS".equals(key)) description = "FOREGROUND COLOR";
       if ("*tJ".equals(key)) description = "RENDER ALGORITHM";
       if ("*mW".equals(key)) description = "DOWNLOAD DITHER MATRIX";
       if ("*lW".equals(key)) description = "COLOR LOOKUP TABLES";
       if ("*tI".equals(key)) description = "GAMMA CORRECTION";
       if ("*iW".equals(key)) description = "VIEWING ILLUMINANT";
       if ("&bM".equals(key)) description = "MONOCHROME PRINT MODE";
       //STATUS READBACK
       if ("*sT".equals(key)) description = "SET LOCATION TYPE";
       if ("*sU".equals(key)) description = "SET LOCATION UNIT";
       if ("*sI".equals(key)) description = "INQUIRE ENTITY";
       if ("*sM".equals(key)) description = "FREE SPACE";
       if ("&rF".equals(key)) description = "FLUSH ALL PAGES";
       if ("*sX".equals(key)) description = "ECHO";
       //PICTURE FRAME
       if ("*cX".equals(key)) description = "PICTURE FRAME HORZ SIZE";
       if ("*cy".equals(key)) description = "PICTURE FRAME VERT SIZE";
       if ("*cT".equals(key)) description = "PICTURE FRAME ANCHOR POINT";
       if ("*cK".equals(key)) description = "PICTURE FRAME HP-GL/2 PLOT HORZ SIZE";
       if ("*cL".equals(key)) description = "PICTURE FRAME HP-GL/2 PLOT VERT SIZE";
       if ("%B".equals(key)) description = "PICTURE FRAME ENTER HP-GL/2 MODE";
       if ("%A".equals(key)) description = "PICTURE FRAME ENTER PCL MODE";
       if (description == null) {
         str.append("UNKNOWN");
       } else {
         str.append(description);
       }
     }
     return str.toString();
   }
 
 }
