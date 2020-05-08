 package de.weltraumschaf.caythe.intermediate;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public interface TypeSpec {
    public TypeForm getForm();
     public void setIdentifer(SymbolTableEntry identifer);
     public SymbolTableEntry getIdentifier();
    public void setAttribute(TypeKey key, Object value);
    public Object getAttribute(TypeKey key);
     public boolean isPascalString();
     public TypeSpec baseType();
 }
