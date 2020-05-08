 import java.math.BigInteger;
 import java.util.Formatter;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Common
 {
    public static final int CHAR_SIZE = 5;
    private Common() {}
   
    /**
     * Gets the bits from the starting to the ending position.  Starting bit is
     * included, end bit is not.  
     * @param number - the number to get the bits from
     * @param start - the starting bit position (inclusive)
     * @param end - the ending bit position (exclusive)
     * @return a number containing the selected bits on the right
     * */
    public static long getBits(long number, int start, int end)
       throws InvalidNumberException
    {
       long result = 0;
       int shiftLength = end - start - 1;
       if (start > end || start < 1 || end > Long.SIZE + 1)
       {
          throw new InvalidNumberException("Invalid bit positions");
       }
       for(int i = start; i < end; i++)
       {
          result = result | (getBit(number, i) << shiftLength);
          shiftLength--; 
       }
       return result;
    }
 
    public static byte getBit(byte number, int position)
       throws InvalidNumberException
    {
       if (position > Byte.SIZE || position < 1)
       {
          throw new InvalidNumberException(position + " is not a valid bit " + 
             "position");
       }
       byte mask = 1;
       int value = (mask << (Byte.SIZE - position)) & number;
       if (value > 0)
       {
          return 1;
       }
       else
       {
          return 0;
       }
    }
 
    /**
     * Gets either a 0 or a 1 from the given position.  Note that the leftmost
     * bit is bit 1 (not bit 0) because that's what the DES standard wants.
     * @param number - the number to get the bit from
     * @param position - the spot in the number where the bit is
     * @return a byte that stores 0 or 1 depending on the bit
     **/
    public static byte getBit(long number, int position) 
       throws InvalidNumberException
    {
       if (position > Long.SIZE || position < 1)
       {
          throw new InvalidNumberException(position + " is not a valid bit " + 
             "position");
       }
       long mask = 1;
       long value = (mask << (Long.SIZE - position)) & number;
      if (value > 0)
       {
          return 1;
       }
       else
       {
          return 0;
       }
    }
 
    public static long makeLong(int left, int right)
    {
      System.out.println("Left: " + showBinary(left));
      System.out.println("Right: " + showBinary(right));
       long result = ((long) left) << 32;
       result = result | right;
       return result;
    }
 
    public static int[] splitLong(long num)
    {
       int result[] = new int[2];
       int mask = 0xFFFFFFFF; //4 solid bytes
       result[1] = (int) (num & mask); //left half
       result[0] = (int) ((num >> 32) & mask); //right half
       return result;
    }
    
    public static long switchBits(long original, int newPositions[]) 
       throws InvalidNumberException
    {
       long result = 0;
       for(int i = 0; i < newPositions.length; i++)
       {
          long bit = getBit(original, newPositions[i]);
          result = result | (bit << Long.SIZE - i - 1);
       }
       return result;
    }
 
    public static String showBinary(long num)
    {
       String result = "";
       while(num != 0)
       {
          result = (num & 1) + result;
          num = num >>> 1;
       }
       return result;
     
    }
 
    public static String makeNumberString(String string)
    {
       String result = "";
       char[] array = string.toCharArray();
       Formatter formatter = new Formatter();
       for(char current : array)
       {
          formatter.format("%05d", (int) current);
       }
       return formatter.toString();
    }
 
    public static String makeCharString(String string)
    {
       String result = "";
       for(int i = 0; i < string.length(); i += CHAR_SIZE)
       {
          String sub = string.substring(i, i + CHAR_SIZE);
          char c = (char) (Integer.valueOf(sub).intValue());
          result += c;
       }
       return result;
    }
    
    /*public static List<String> split(String string, int subsize)
    {
       int element_size = 5;
       ArrayList<String> result = new ArrayList<String>();
       String current = "";
       for (int i = 0; i < string.length(); i += element_size)
       {
          if (current.length() + element_size > subsize)
          {
             result.add(current);
             current = "";
          }
          if (i + element_size > string.length())
          {
             current += string.substring(i);
          }
          else
          {
             current += string.substring(i, i + element_size);
          }
       }
       return result;
    }*/
 
    public static String[] split(String string, int subsize)
    {
 	   int numElements = (int) Math.ceil((double) string.length() / subsize);
 	   String result[] = new String[numElements];
 	   for(int i = 0; i * subsize < string.length(); i++)
 	   {
 		   if(i * subsize + subsize > string.length())
 		   {
 			   result[i] = string.substring(i * subsize);
 		   }
 		   else
 		   {
 			   result[i] = string.substring(i * subsize, i * subsize + subsize);
 		   }
 	   }
 	   return result;
    }
    
    public static String addLeadingZeros(BigInteger num, int size)
    {
 	   String result = num.toString();
 	   while (result.length() % size != 0)
 	   {
 		   result = "0" + result;
 	   }
 	   return result;
    }
 }
