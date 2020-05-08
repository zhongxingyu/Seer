 import java.io.*;
 import java.util.*;
 
 class DoomsBakery
 {
   String str;
   int K;
 
   public DoomsBakery(String s, int k)
   {
     str = s;
     K = k;
   }
   //To check if two adjacent characters are the same
   public boolean isValidString()
   {
     //Base case to check for string of length 1
     if(str.length() == 1)
     {
       if(str.charAt(0) != '?' && Integer.parseInt(str) >= K)
       {
         return false;
       }
       return true;
     }
 
     for(int i=0; i<str.length(); ++i)
     {
       if(str.charAt(i) == str.charAt((i+1) % str.length())
           && str.charAt(i) != '?')
       {
         return false;
       }
       else if(charToInt(str.charAt(i)) >= K)
       {
         return false;
       }
     }
     return true;
   }
 
   static int charToInt(char ch)
   {
     if(ch == '?') return -1;
     else return ch - '0';
   }
 
   //returs a suitable color to fill, -1 indicates a failure
   //that might occur when we have less than 2 colors
   public int getViableColor(int currentIndex, char [] array)
   {
     int previousIndex = (currentIndex == 0)? str.length() - 1: currentIndex - 1;
     int nextIndex = (currentIndex + 1) % str.length();
     for(int i=0; i<K; ++i)
     {
      if(charToInt(array[previousIndex]) != i &&
           charToInt(array[nextIndex]) != i)
       {
         return i;
       }
     }
     return -1;
   }
 
   //the method that actually deals with the checks for the question
   public void process()
   {
     if(!isValidString())
     {
       System.out.println("NO");
     }
     else
     {
       char [] tmp = str.toCharArray();
       //handles the trickiest of all cases ;)
       //K=2 and str = "??10??"
      if(K % 2 == 0 && str.length() % 2 == 0 && str.length() > 3)
       {
         if(tmp[0] == '?' && tmp[1] == '?' && tmp[tmp.length - 1] != '1')
         {
           tmp[0] = '1';
         }
       }
 
       for(int i=0; i<tmp.length; ++i)
       {
         if(tmp[i] == '?')
         {
           int replacement = getViableColor(i, tmp);
           if(replacement == -1)
           {
             System.out.println("NO");
             return;
           }
           tmp[i] = (char)('0' + replacement);
         }
       }
       System.out.println(new String(tmp));
     }
   }
 }
 
 class Main
 {
   public static void main(String [] args) throws IOException
   {
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     int numTests = Integer.parseInt( br.readLine() );
     while(numTests-- > 0)
     {
       int k = Integer.parseInt(br.readLine());
       new DoomsBakery(br.readLine(),k).process();
     }
   }
 }
