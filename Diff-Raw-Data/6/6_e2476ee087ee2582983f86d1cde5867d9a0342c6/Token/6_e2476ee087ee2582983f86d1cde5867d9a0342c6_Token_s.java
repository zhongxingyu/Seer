 package de.skuzzle.polly.core.parser;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import de.skuzzle.polly.tools.EqualsHelper;
 import de.skuzzle.polly.tools.Equatable;
 
 
 public class Token implements Serializable, Equatable {
     
     private static final long serialVersionUID = 1L;
     
     private TokenType type;
     private Position position;
     private String stringValue;
     private double floatValue;
     private Date dateValue;
     private long longValue;
     
     
     
     public Token(TokenType type, Position position) {
         this.type = type;
         this.position = position;
         this.stringValue = "";
         this.dateValue = new Date();
     }
     
     
     
     public Token(TokenType type, Position position, String stringValue) {
         this(type, position);
         this.stringValue = stringValue;
         this.dateValue = new Date();
     }
     
     
     
     public Token(TokenType type, Position position, Date dateValue) {
         this(type, position);
         this.stringValue = dateValue.toString();
         this.dateValue = dateValue;
     }
     
     
     
     public Token(TokenType type, Position position, double floatValue) {
         this(type, position);
         this.stringValue = Double.toString(floatValue);
         this.floatValue = floatValue;
         this.dateValue = new Date();
     }
     
     
     
     public Token(TokenType type, Position position, long longValue) {
         this(type, position);
         this.longValue = longValue;
         this.dateValue = new Date();
     }
 
     
     
     public TokenType getType() {
         return this.type;
     }
     
     
     
     public String getStringValue() {
         return this.stringValue;
     }
     
     
     
     public double getFloatValue() {
         return this.floatValue;
     }
     
     
     
     public Date getDateValue() {
         return this.dateValue;
     }
    
     
     
     public long getLongValue() {
         return this.longValue;
     }
     
     
     public Position getPosition() {
         return this.position;
     }
     
     
     
     public boolean matches(Token t) {
         return this.getType() == t.getType();
     }
     
     
     
     public boolean matches(TokenType t) {
         return this.getType() == t;
     }
     
     
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
             + ((this.position == null) ? 0 : this.position.hashCode());
         result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
         return result;
     }
 
 
 
     @Override
     public final boolean equals(Object o) {
         return EqualsHelper.testEquality(this, o);
     }
     
 
 
     @Override
     public Class<?> getEquivalenceClass() {
         return Token.class;
     }
     
     
     
     @Override
     public boolean actualEquals(Equatable o) {
         final Token other = (Token) o;
         return this.type == other.type && this.position.equals(other.position);
     }
     
     
     
     public String toString(boolean printPosition, boolean printValue) {
         StringBuilder result = new StringBuilder(15);
         String value = this.stringValue;
         switch (this.type) {
            default:    
             case DATETIME:
                 value = this.dateValue.toString();
                 break;
         }
         
         result.append(this.type.toString());
         
         if (printValue) {
             result.append("(");
             result.append(value);
             result.append(")");
         }
         
         if (printPosition) {
             result.append(" @ ");
             result.append(this.position.toString());
         }
         return result.toString();
         /*return this.type.toString() + "(" + value + ") @ " 
                 + this.position.toString();*/
     }
     
     
     
     @Override
     public String toString() {
         return this.toString(true, false);
     }
 }
