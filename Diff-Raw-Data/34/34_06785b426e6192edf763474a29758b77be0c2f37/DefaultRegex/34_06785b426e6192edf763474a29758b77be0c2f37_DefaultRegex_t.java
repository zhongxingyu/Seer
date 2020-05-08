 package de.unisiegen.gtitool.core.regex;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.DefaultRegexAlphabet;
 import de.unisiegen.gtitool.core.entities.DefaultSymbol;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.entities.regex.CharacterClassNode;
 import de.unisiegen.gtitool.core.entities.regex.ConcatenationNode;
 import de.unisiegen.gtitool.core.entities.regex.KleeneNode;
 import de.unisiegen.gtitool.core.entities.regex.LeafNode;
 import de.unisiegen.gtitool.core.entities.regex.Regex;
 import de.unisiegen.gtitool.core.entities.regex.RegexNode;
 import de.unisiegen.gtitool.core.entities.regex.TokenNode;
 import de.unisiegen.gtitool.core.exceptions.RegexEmptyException;
 import de.unisiegen.gtitool.core.exceptions.RegexException;
 import de.unisiegen.gtitool.core.exceptions.RegexSymbolNotUsedException;
 import de.unisiegen.gtitool.core.exceptions.RegexValidationException;
 import de.unisiegen.gtitool.core.exceptions.alphabet.AlphabetException;
 
 
 /**
  * 
  */
 public class DefaultRegex implements Regex
 {
 
   /**
    * Signals the regex type.
    * 
    * @author Simon Meurer
    */
   public enum RegexType implements EntityType
   {
     /**
      * Regex
      */
     REGEX;
 
     /**
      * The file ending.
      * 
      * @return The file ending.
      */
     public final String getFileEnding ()
     {
       return toString ().toLowerCase ();
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @see Enum#toString()
      */
     @Override
     public final String toString ()
     {
       switch ( this )
       {
         case REGEX :
         {
           return "REGEX"; //$NON-NLS-1$
         }
       }
       throw new IllegalArgumentException ( "unsupported regex type" ); //$NON-NLS-1$
     }
   }
 
 
   /**
    * The serial version uid
    */
   private static final long serialVersionUID = 5624201944777732080L;
 
 
   /**
    * The alphabet of this {@link Regex}
    */
   private DefaultRegexAlphabet alphabet;
 
 
   /**
    * The root node of this regex
    */
   private RegexNode regexNode;
 
 
   /**
    * The String the user has typed
    */
   private String regexString;
 
 
   /**
    * The Constructor for a {@link DefaultRegex}
    * 
    * @param a The {@link Alphabet} of this Regex
    */
   public DefaultRegex ( DefaultRegexAlphabet a )
   {
     this.alphabet = a;
     this.regexString = new String ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Object#clone()
    */
   @Override
   public DefaultRegex clone ()
   {
     DefaultRegexAlphabet a;
     try
     {
       a = new DefaultRegexAlphabet ();
       a.add ( this.alphabet.get () );
       DefaultRegex r = new DefaultRegex ( a );
       if ( this.regexNode != null && this.regexString != null )
       {
         r.setRegexNode ( getRegexNode (), getRegexString () );
       }
       return r;
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     return null;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals ( Object obj )
   {
     if ( obj == this )
     {
       return true;
     }
     if ( obj instanceof DefaultRegex )
     {
       DefaultRegex dr = ( DefaultRegex ) obj;
       if ( this.regexString == null )
       {
         if ( dr.getRegexNode () != null )
         {
           return this.alphabet.equals ( dr.alphabet );
         }
       }
       else if ( this.regexString.equals ( dr.getRegexString () )
           && this.regexNode.equals ( dr.getRegexNode () ) )
       {
         return this.alphabet.equals ( dr.alphabet );
       }
     }
     return false;
   }
 
 
   /**
    * The followPos function as defined in the dragon book
    * 
    * @param pos The position
    * @return followPos as defined in the dragon book
    */
   public HashSet < Integer > followPos ( int pos )
   {
     HashSet < Integer > followPos = new HashSet < Integer > ();
 
     HashSet < RegexNode > nodeList = getAllNodes ( this.regexNode );
 
     for ( RegexNode node : nodeList )
     {
 
       if ( node instanceof ConcatenationNode )
       {
         RegexNode n1 = node.getChildren ().get ( 0 );
         boolean foundPosInLastPosN1 = false;
         for ( RegexNode searchNode : n1.lastPos () )
         {
           if ( searchNode instanceof LeafNode )
           {
             LeafNode leafNode = ( LeafNode ) searchNode;
             if ( leafNode.getPosition () == pos )
             {
               foundPosInLastPosN1 = true;
             }
           }
         }
         if ( foundPosInLastPosN1 )
         {
           RegexNode n2 = node.getChildren ().get ( 1 );
           for ( RegexNode firstPosNode : n2.firstPos () )
           {
             followPos.add ( new Integer ( ( ( LeafNode ) firstPosNode )
                 .getPosition () ) );
           }
         }
       }
       else if ( node instanceof KleeneNode )
       {
         RegexNode n = node.getChildren ().get ( 0 );
         boolean foundInLastPosN = false;
         for ( RegexNode searchNode : n.lastPos () )
         {
           if ( searchNode instanceof LeafNode )
           {
             LeafNode leafNode = ( LeafNode ) searchNode;
             if ( leafNode.getPosition () == pos )
             {
               foundInLastPosN = true;
             }
           }
         }
         if ( foundInLastPosN )
         {
           for ( RegexNode firstPosNode : n.firstPos () )
           {
             followPos.add ( new Integer ( ( ( LeafNode ) firstPosNode )
                 .getPosition () ) );
           }
         }
       }
     }
     return followPos;
   }
 
 
   /**
    * Gets all Nodes in this {@link Regex} recursivly
    * 
    * @param root The root of the Regex
    * @return All Nodes in the regex
    */
   private HashSet < RegexNode > getAllNodes ( RegexNode root )
   {
     HashSet < RegexNode > nodeList = new HashSet < RegexNode > ();
     nodeList.add ( root );
     nodeList.addAll ( root.getAllChildren () );
     return nodeList;
   }
 
 
   /**
    * Returns the alphabet.
    * 
    * @return The alphabet.
    * @see #alphabet
    */
   public DefaultRegexAlphabet getAlphabet ()
   {
     return this.alphabet;
   }
 
 
   /**
    * Returns the Not removable symbols
    * 
    * @return The Not removable symbols
    */
   public TreeSet < Symbol > getNotRemoveableSymbolsFromAlphabet ()
   {
     TreeSet < Symbol > set = new TreeSet < Symbol > ();
    if(this.regexNode == null) {
      return set;
    }
     for ( LeafNode l : this.regexNode.getTokenNodes () )
     {
       if ( l instanceof TokenNode )
       {
         set.add ( new DefaultSymbol ( ( ( TokenNode ) l ).getName () ) );
       }
       else if ( l instanceof CharacterClassNode )
       {
         CharacterClassNode c = ( CharacterClassNode ) l;
         set.addAll ( c.getCharacters () );
       }
     }
     return set;
   }
 
 
   /**
    * Returns the regexNode.
    * 
    * @return The regexNode.
    * @see #regexNode
    */
   public RegexNode getRegexNode ()
   {
     return this.regexNode;
   }
 
 
   /**
    * Returns the regexString.
    * 
    * @return The regexString.
    * @see #regexString
    */
   public String getRegexString ()
   {
     return this.regexString;
   }
 
 
   /**
    * Returns the Used symbols
    * 
    * @return The Used symbols
    */
   private HashSet < Symbol > getUsedSymbols ()
   {
     HashSet < Symbol > symbols = new HashSet < Symbol > ();
     for ( LeafNode l : this.regexNode.getTokenNodes () )
     {
       if ( l instanceof TokenNode )
       {
         TokenNode t = ( TokenNode ) l;
         symbols.add ( new DefaultSymbol ( t.getName () ) );
       }
     }
     return symbols;
   }
 
 
   /**
    * Sets the alphabet.
    * 
    * @param alphabet The alphabet to set.
    * @see #alphabet
    */
   public void setAlphabet ( DefaultRegexAlphabet alphabet )
   {
     this.alphabet = alphabet;
   }
 
 
   /**
    * Sets the {@link RegexNode} initial.
    * 
    * @param regexNode The {@link RegexNode} to set.
    * @param regexString The new {@link String} for the regex
    */
   public void setRegexNode ( RegexNode regexNode, String regexString )
   {
     this.regexNode = regexNode;
     this.regexString = regexString;
 
     int currentPosition = 1;
     for ( LeafNode current : this.regexNode.getTokenNodes () )
     {
       current.setPosition ( currentPosition );
       currentPosition++ ;
     }
   }
 
 
   /**
    * Gets the Symbol at Position in the Regex
    * 
    * @param position The position
    * @return The Symbol at Position in the Regex or null if position is empty
    */
   public String symbolAtPosition ( int position )
   {
     String s = null;
     for ( LeafNode current : this.regexNode.getTokenNodes () )
     {
       if ( current.getPosition () == position )
       {
         s = current.getNodeString ().toString ();
       }
     }
     return s;
   }
 
 
   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString ()
   {
     return this.regexNode.toString ();
   }
 
 
   /**
    * Validates the Regex
    * 
    * @throws RegexValidationException If validation goes wrong
    */
   public void validate () throws RegexValidationException
   {
     if ( this.regexNode == null
         || ( this.regexNode instanceof TokenNode && ( ( TokenNode ) this.regexNode )
             .getName ().equals ( "" ) ) ) //$NON-NLS-1$
     {
       ArrayList < RegexException > elist = new ArrayList < RegexException > ();
       elist.add ( new RegexEmptyException () );
       throw new RegexValidationException ( elist );
     }
     HashSet < Symbol > usedSymbols = getUsedSymbols ();
     HashSet < Symbol > unusedSymbols = new HashSet < Symbol > ();
     unusedSymbols.addAll ( this.alphabet.get () );
     unusedSymbols.removeAll ( usedSymbols );
     ArrayList < RegexException > elist = new ArrayList < RegexException > ();
     if ( unusedSymbols.size () > 0 )
     {
       for ( Symbol s : unusedSymbols )
       {
         elist.add ( new RegexSymbolNotUsedException ( s ) );
       }
 
     }
     if ( !elist.isEmpty () )
     {
       throw new RegexValidationException ( elist );
     }
   }
 }
