 
 /**
  * Extends BTNode by adding a frequency field and also methods to help 
  *  priority queue with sorting.
  * 
  * @author Josh Gillham
  * @version 11-28-12
  */
 public class HNode extends BTNode< HuffmanData > {
     /**
      * Simple constructor - set all fields to null or 0.
      */
     public HNode() {
        super( new HuffmanData( ), null, null );
     }
     
     /**
      * Constructor that sets the symbol only.
      * 
      * @param symbol the symbol.
      */
     public HNode(Character symbol) {
         super( new HuffmanData( symbol ), null, null );
     }
     
     /**
      * Constructor that sets the symbol and frequency. 
      * 
      * @param symbol the symbol.
      * @param frequency the frequency of occurrence for the symbol.
      */
     public HNode(Character symbol, double frequency) {
         super( new HuffmanData( symbol, frequency ), null, null );
     }
     
     /**
      * Constructor that sets symbol, frequency, and code. 
      * 
      * @param symbol the symbol.
      * @param frequency the frequency of occurrence for the symbol.
      * @param code the code for the symbol.
      */
     public HNode(Character symbol, double frequency, String code) {
         super( new HuffmanData( symbol, frequency, code ), null, null );
     }
     
     /**
      * Fully parameterized constructor. 
      * 
      * @param symbol the symbol.
      * @param frequency the frequency of occurrence for the symbol.
      * @param code the code for the symbol.
      * @param left link to the left child.
      * @param right link to the right child
      */
     public HNode(Character symbol, double frequency, String code,
             HNode left, HNode right) {
         super( new HuffmanData( symbol, frequency, code ), left, right );
     }
     
     /**
      * Compares the HNodes by the frequency.
      * 
      * @param n object to be compared with this.
      * 
      * @return a negative integer, zero, or a positive integer as this 
      *  object is less than, equal to, or greater than the specified object. 
      * 
      * @throws NullPointerException if the specified object is null.
      */
     public int compareTo(HNode n) {
         if ( this.getFrequency() < n.getFrequency() ) {
             return -1;
         } else if ( this.getFrequency() == n.getFrequency() ) {
             return 0;
         }
         return 1;
     }
     
     /**
      * Equals predicate considers the symbol and frequency only. 
      * 
      * @param o the object to check for equality.
      * 
      * @return true if both the symbol and the frequency agree;
      *  false otherwise
      */
     @Override
     public boolean equals( Object o ) {
         if ( !(o instanceof HNode) )
             return false;
         HNode node = (HNode)o;
         
         if ( this.getFrequency( ) == node.getFrequency( ) &&
                 this.getSymbol( ) == node.getSymbol( ) ) {
             return true;
         }
         return false;
     }
     
     /**
      * Sets the frequency.
      * 
      * @param frequency the new frequency.
      * 
      * @throws IllegalArgumentException if frequency is less than 0.
      */
     public void setFrequency( int frequency ) {
         ( (HuffmanData)this.getValue() ).setFrequency( frequency );
     }
     
     /**
      * Sets the binary code.
      * 
      * @param code the new code.
      * 
      * @throws NullPointerException when binaryCode is null.
      */
     public void setCode( String code ) {
         ( (HuffmanData)this.getValue() ).setCode( code );
     }
     
     /**
      * Modify the symbol.
      * 
      * @param symbol the new symbol.
      */
     public void setSymbol(Character symbol) {
         ( (HuffmanData)this.getValue() ).setSymbol( symbol );
     }
     
     /**
      * Accesses the frequency.
      * 
      * @return the frequency.
      */
     public int getFrequency() {
         return (int)( (HuffmanData)this.getValue() ).getFrequency();
     }
     
     /**
      * Accesses the binary code.
      * 
      * @return the binary code.
      */
     public String getCode( ) {
         return ( (HuffmanData)this.getValue() ).getCode();
     }
     
     /**
      * Access the symbol.
      * 
      * @return the symbol.
      */
     public Character getSymbol() {
         return ( (HuffmanData)this.getValue() ).getSymbol();
     }
     
     /**
      * Access the left child.
      * 
      * @return the left child of this node.
      */
     public HNode getLeftChild() {
         return (HNode)this.getLeftChild();
     }
     
     /**
      * Access the right child.
      * 
      * @return the right child of this node.
      */
     public HNode getRightChild() {
         return (HNode)this.getRightChild();
     }
     
     /**
      * Define hashcode for HNode.
      * 
      * If two objects are equal according to the equals(Object)
      *  method, then calling the hashCode method on each of the 
      *  two objects produces the same integer result.
      * 
      * @return a hash code value for this object.
      */
     public int hashCode() {
         throw new UnsupportedOperationException();
     }
 }
