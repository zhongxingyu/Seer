 package de.weltraumschaf.caythe.intermediate;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public interface TypeSpec {
    public TypForm getForm();
     public void setIdentifer(SymbolTableEntry identifer);
     public SymbolTableEntry getIdentifier();
    public void setAtrribute(TypeKey key, Object value);
    public Object getAttribute();
     public boolean isPascalString();
     public TypeSpec baseType();
 }
