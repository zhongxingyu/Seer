 package plugins.adufour.ezplug;
 
 import plugins.adufour.vars.lang.VarEnum;
 
 /**
  * Class defining a enumeration type variable. <br>
  * The graphical component is a combo box containing some (or all) values of the enumeration
  * 
  * @author Alexandre Dufour
  * 
  * @param <E>
  *            The enumeration type
  */
 public class EzVarEnum<E extends Enum<E>> extends EzVar<E>
 {
     /**
      * Constructs a new input VarEnum variable
      * 
      * @param varName
      *            the variable name
      * @param values
      *            the values to choose from. The full list of enumeration values can be obtained with the E.values()
      *            method, where E is the enumeration type
      */
     public EzVarEnum(String varName, E[] values)
     {
         this(varName, values, 0);
     }
 
     /**
      * Constructs a new input VarEnum variable
      * 
      * @param varName
      *            the variable name
      * @param values
      *            the values to choose from. The full list of enumeration values can be obtained with the E.values()
      *            method, where E is the enumeration type
      * @param defaultValue
      *            the enumeration value to select by default
      */
     public EzVarEnum(String varName, E[] values, E defaultValue)
     {
        this(varName, values, defaultValue.ordinal());
     }
 
     /**
      * Constructs a new input VarEnum variable
      * 
      * @param varName
      *            the variable name
      * @param values
      *            the values to choose from. The full list of enumeration values can be obtained with the E.values()
      *            method, where E is the enumeration type
      * @param defaultValueIndex
      *            the zero-based index of the enumeration value to select by default
      */
     public EzVarEnum(String varName, E[] values, int defaultValueIndex)
     {
         super(new VarEnum<E>(varName, values[defaultValueIndex]), values, defaultValueIndex, false);
     }
 }
