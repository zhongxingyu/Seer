 package org.cyclopsgroup.caff.token;
 
 /**
  * A Java bean that stands for an event of token
  *
  * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
  */
 public final class TokenEvent
 {
     private final int end;
 
     private final boolean quoted;
 
     private final int start;
 
     private final boolean terminated;
 
     private final String token;
 
     /**
      * Constructor that requires token, start positoion and end position
      *
      * @param token Value of token
      * @param start Zero based start position
      * @param end Zero based last character position
     * @parma terminated True if token is terminated explicitly
      */
     TokenEvent( String token, int start, int end, boolean terminated )
     {
         this( token, start, end, terminated, false );
     }
 
     /**
      * Constructor that requires token, start positoion and end position
      *
      * @param token Value of token
      * @param start Zero based start position
      * @param end Zero based last character position
     * @parma terminated True if token is terminated explicitly
      * @param quoted True if word is quoted
      */
     TokenEvent( String token, int start, int end, boolean terminated, boolean quoted )
     {
         this.token = token;
         this.start = start;
         this.end = end;
         this.terminated = terminated;
         this.quoted = quoted;
     }
 
     /**
      * @return One based position of last character in token
      */
     public int getEnd()
     {
         return end;
     }
 
     /**
      * @return Zero based start position of token
      */
     public int getStart()
     {
         return start;
     }
 
     /**
      * @return Value of token
      */
     public String getToken()
     {
         return token;
     }
 
     /**
      * @return True if token is explicitly quoted
      */
     public boolean isQuoted()
     {
         return quoted;
     }
 
     /**
      * @return True if token is explicitly terminated
      */
     public boolean isTerminated()
     {
         return terminated;
     }
 }
