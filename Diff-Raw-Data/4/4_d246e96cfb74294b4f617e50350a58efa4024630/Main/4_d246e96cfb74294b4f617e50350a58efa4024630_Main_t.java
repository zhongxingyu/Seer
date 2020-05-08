 package nz.gen.geek_central.screencalc;
 /*
     Calculator for parameters for a display screen: given any of height,
     width or diagonal in distance units, aspect ratio, pixel density,
     or height or width in pixels, try to calculate the rest.
 
     Copyright 2013 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.
 
     This program is free software: you can redistribute it and/or
     modify it under the terms of the GNU General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see
     <http://www.gnu.org/licenses/>.
 */
 
 import java.util.HashMap;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.EditText;
 
 public class Main extends android.app.Activity
   {
   /* worth comparing the relative complexity of setting up the calculation rules
     here in Java versus the Python version at <https://github.com/ldo/screencalc> */
 
     static final double cm_per_in = 2.54;
 
     static enum Units
       {
         UNITS_CM,
         UNITS_IN,
       };
     Units CurUnits = Units.UNITS_CM; /* no relevant locale setting? */
 
     static double AspectDiag
       (
         double Aspect
       )
       /* returns the ratio of the diagonal to the width. */
       {
         return
             Math.hypot(1.0, Aspect);
       } /*AspectDiag*/
 
     interface Parser
       {
 
         public double Parse
           (
             String s
           );
 
       } /*Parser*/;
 
     static class ParseInt implements Parser
       {
 
         public double Parse
           (
             String s
           )
           {
             return
                 Integer.parseInt(s);
           } /*Parse*/
 
       } /*ParseInt*/;
 
     class ParseDensity implements Parser
       {
 
         public double Parse
           (
             String s
           )
           /* always returns dots per cm. */
           {
             boolean IsDPI = CurUnits == Units.UNITS_IN; /* default */
             s = s.toLowerCase();
             if (s.endsWith("dpcm"))
               {
                 IsDPI = false;
                 s = s.substring(0, s.length() - 4);
               }
             else if (s.endsWith("dpi"))
               {
                 IsDPI = true;
                 s = s.substring(0, s.length() - 3);
               } /*if*/
             return
                 Double.parseDouble(s) / (IsDPI ? cm_per_in : 1.0);
           } /*Parse*/
 
       } /*ParseDensity*/;
 
     class ParseMeasure implements Parser
       {
         private class Unit
           {
             public final String Name;
             public final double Multiplier;
 
             public Unit
               (
                 String Name,
                 double Multiplier
               )
               {
                 this.Name = Name;
                 this.Multiplier = Multiplier;
               } /*Unit*/
 
           } /*Unit*/;
 
         public double Parse
           (
             String s
           )
           {
             double Multiplier = CurUnits == Units.UNITS_CM ? 1.0 : cm_per_in;
             s = s.toLowerCase();
             for
               (
                 Unit This :
                     new Unit[]
                         {
                             new Unit("cm", 1.0),
                             new Unit("mm", 0.1),
                             new Unit("in", cm_per_in),
                         }
               )
               {
                 if (s.endsWith(This.Name))
                   {
                     s = s.substring(0, s.length() - This.Name.length());
                     Multiplier = This.Multiplier;
                     break;
                   } /*if*/
               } /*for*/
             return
                 Double.parseDouble(s) * Multiplier;
           } /*Parse*/
 
       } /*ParseMeasure*/;
 
     static class ParseRatio implements Parser
       {
 
         public double Parse
           (
             String s
           )
           {
             final int SepPos = s.indexOf(":");
             final double Result;
             if (SepPos >= 0)
               {
                 final double Numer = Integer.parseInt(s.substring(0, SepPos));
                 final double Denom = Integer.parseInt(s.substring(SepPos + 1, s.length()));
                 Result = Numer / Denom;
               }
             else
               {
                 Result = Double.parseDouble(s);
               } /*if*/
             return
                 Result;
           } /*Parse*/
 
       } /*ParseRatio*/;
 
     interface CalcFunction
       {
 
         public double Calculate
           (
             double[] Args
           );
 
       } /*CalcFunction*/;
 
     static class ParamDef
       {
         public static enum ParamTypes
           {
             TYPE_RATIO,
             TYPE_MEASURE,
             TYPE_PIXELS,
             TYPE_DENSITY,
           };
         public final ParamTypes Type;
         public final Parser Parse;
         public final HashMap<int[], CalcFunction> Calculate = new HashMap<int[], CalcFunction>();
 
         public static class Entry
           {
             public final int[] ArgNames;
             public final CalcFunction Calc;
 
             public Entry
               (
                 int[] ArgNames,
                 CalcFunction Calc
               )
               {
                 this.ArgNames = ArgNames;
                 this.Calc = Calc;
               } /*Entry*/
 
           } /*Entry*/;
 
         public ParamDef
           (
             ParamTypes Type,
             Parser Parse,
             Entry[] Calculate
           )
           {
             this.Type = Type;
             this.Parse = Parse;
             for (Entry ThisEntry : Calculate)
               {
                 this.Calculate.put(ThisEntry.ArgNames, ThisEntry.Calc);
               } /*for*/
           } /*ParamDef*/
 
       } /*ParamDef*/;
 
     final HashMap<Integer, ParamDef> ParamDefs = new HashMap<Integer, ParamDef>();
       {
         ParamDefs.put
           (
             R.id.height_measure,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_MEASURE,
                 /*Parse =*/ new ParseMeasure(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.aspect_ratio, R.id.diag_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / AspectDiag(Args[0]) * Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.pixel_density, R.id.height_pixels},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.diag_measure, R.id.width_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Math.sqrt(Args[0] * Args[0] - Args[1] * Args[1]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.width_measure,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_MEASURE,
                 /*Parse =*/ new ParseMeasure(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.aspect_ratio, R.id.diag_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / AspectDiag(Args[0]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.pixel_density, R.id.width_pixels},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.diag_measure, R.id.height_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Math.sqrt(Args[0] * Args[0] - Args[1] * Args[1]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.diag_measure,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_MEASURE,
                 /*Parse =*/ new ParseMeasure(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.aspect_ratio, R.id.height_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / Args[0] * AspectDiag(Args[0]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.aspect_ratio, R.id.width_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] * AspectDiag(Args[0]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.height_measure, R.id.width_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Math.hypot(Args[0], Args[1]);
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.height_pixels,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_PIXELS,
                 /*Parse =*/ new ParseInt(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.pixel_density, R.id.height_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] * Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.width_pixels,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_PIXELS,
                 /*Parse =*/ new ParseInt(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.pixel_density, R.id.width_measure},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] * Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.pixel_density,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_DENSITY,
                 /*Parse =*/ new ParseDensity(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.height_measure, R.id.height_pixels},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.width_measure, R.id.width_pixels},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[1] / Args[0];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
         ParamDefs.put
           (
             R.id.aspect_ratio,
             new ParamDef
               (
                 /*Type =*/ ParamDef.ParamTypes.TYPE_RATIO,
                 /*Parse =*/ new ParseRatio(),
                 /*Calculate =*/ new ParamDef.Entry[]
                     {
                         new ParamDef.Entry
                           (
                             /*ArgNames =*/ new int[] {R.id.height_pixels, R.id.width_pixels},
                             /*Calc =*/
                                 new CalcFunction()
                                   {
                                     public double Calculate
                                       (
                                         double[] Args
                                       )
                                       {
                                         return
                                             Args[0] / Args[1];
                                       } /*Calculate*/
                                   } /*CalcFunction*/
                           ),
                     }
               )
           );
       }
 
     final int[] UnitsButtons = new int[] {R.id.units_cm, R.id.units_in};
 
     static final int[] Fields =
         {
             R.id.height_measure,
             R.id.width_measure,
             R.id.diag_measure,
             R.id.pixel_density,
             R.id.aspect_ratio,
             R.id.height_pixels,
             R.id.width_pixels,
         };
 
     static final HashMap<Integer, String> FieldNames = new HashMap<Integer, String>(); /* debug */
       {
         FieldNames.put(R.id.height_measure, "height");
         FieldNames.put(R.id.width_measure, "width");
         FieldNames.put(R.id.diag_measure, "diagonal");
         FieldNames.put(R.id.pixel_density, "density");
         FieldNames.put(R.id.aspect_ratio, "aspect");
         FieldNames.put(R.id.height_pixels, "heightpx");
         FieldNames.put(R.id.width_pixels, "widthpx");
       } /* debug */
 
     private static class IDPair
       {
         public final int ID1;
         public final int ID2;
 
         public IDPair
           (
             int ID1,
             int ID2
           )
           {
             this.ID1 = ID1;
             this.ID2 = ID2;
           } /*IDPair*/
 
       } /*IDPair*/;
 
     private int ColorValidValue, ColorUnknownValue, ColorErrorValue;
 
     private void SetUnknown
       (
         int FieldID
       )
       {
         final EditText TheField = (EditText)findViewById(FieldID);
         TheField.setText("", TextView.BufferType.EDITABLE);
         TheField.setFocusable(true);
         TheField.setFocusableInTouchMode(true);
         TheField.setBackgroundColor(ColorUnknownValue);
       } /*SetUnknown*/
 
     private void SetValid
       (
         int FieldID,
         double NewValue
       )
       {
         final EditText TheField = (EditText)findViewById(FieldID);
         double Multiplier = CurUnits == Units.UNITS_CM ? 1.0 : 1.0 / cm_per_in;
         String Suffix = "";
         String Format = "%.3f";
         switch (ParamDefs.get(FieldID).Type)
           {
         case TYPE_RATIO:
            Multiplier = 1.0;
         break;
         case TYPE_MEASURE:
             Suffix = CurUnits == Units.UNITS_CM ? "cm" : "in";
         break;
         case TYPE_PIXELS:
             Format = "%.0f";
            Multiplier = 1.0;
         break;
         case TYPE_DENSITY:
             switch (CurUnits)
               {
             case UNITS_CM:
                 Suffix = "dpcm";
             break;
             case UNITS_IN:
                 Multiplier = cm_per_in;
                 Suffix = "dpi";
             break;
               } /*switch*/
         break;
           } /*switch*/
         TheField.setText
           (
             String.format(Format, NewValue * Multiplier) + Suffix,
             TextView.BufferType.NORMAL
           );
         TheField.setFocusable(false);
         TheField.setBackgroundColor(ColorValidValue);
       } /*SetValid*/
 
     private void SetError
       (
         int FieldID
       )
       {
         final EditText TheField = (EditText)findViewById(FieldID);
         TheField.setFocusable(true);
         TheField.setFocusableInTouchMode(true);
         TheField.setBackgroundColor(ColorErrorValue);
       } /*SetError*/
 
     private class FieldClearAction implements View.OnClickListener
       {
         final int FieldID;
 
         public FieldClearAction
           (
             int FieldID
           )
           {
             this.FieldID = FieldID;
           } /*FieldClearAction*/
 
         public void onClick
           (
             View ClearButton
           )
           {
             SetUnknown(FieldID);
           } /*onClick*/
 
       } /*FieldClearAction*/;
 
     private void ClearAll()
       {
         for (int FieldID : Fields)
           {
             SetUnknown(FieldID);
           } /*for*/
       } /*ClearAll*/
 
     @Override
     public void onCreate
       (
         android.os.Bundle SavedInstanceState
       )
       {
         super.onCreate(SavedInstanceState);
         setContentView(R.layout.main);
           {
             final android.content.res.Resources Res = getResources();
             ColorValidValue = Res.getColor(R.color.valid_value);
             ColorUnknownValue = Res.getColor(R.color.unknown_value);
             ColorErrorValue = Res.getColor(R.color.error_value);
           }
         for
           (
             IDPair This :
                 new IDPair[]
                     {
                         new IDPair(R.id.height_measure, R.id.clear_height_measure),
                         new IDPair(R.id.width_measure, R.id.clear_width_measure),
                         new IDPair(R.id.diag_measure, R.id.clear_diag_measure),
                         new IDPair(R.id.pixel_density, R.id.clear_pixel_density),
                         new IDPair(R.id.aspect_ratio, R.id.clear_aspect_ratio),
                         new IDPair(R.id.height_pixels, R.id.clear_height_pixels),
                         new IDPair(R.id.width_pixels, R.id.clear_width_pixels),
                     }
           )
           {
             findViewById(This.ID2).setOnClickListener(new FieldClearAction(This.ID1));
           } /*for*/
         for (final int UnitsID : UnitsButtons)
           {
             final android.widget.RadioButton ThisButton =
                 (android.widget.RadioButton)findViewById(UnitsID);
             ThisButton.setOnClickListener
               (
                 new View.OnClickListener()
                   {
                     public void onClick
                       (
                         View TheButton
                       )
                       {
                         CurUnits = UnitsID == R.id.units_cm ? Units.UNITS_CM : Units.UNITS_IN;
                       } /*onClick*/
                   } /*View.OnClickListener*/
               );
             ThisButton.setChecked
               (
                 UnitsID == (CurUnits == Units.UNITS_CM ? R.id.units_cm : R.id.units_in)
               );
           } /*for*/
         findViewById(R.id.clear_all).setOnClickListener
           (
             new View.OnClickListener()
               {
                 public void onClick
                   (
                     View TheButton
                   )
                   {
                     ClearAll();
                   } /*onClick*/
               } /*View.OnClickListener*/
           );
         findViewById(R.id.calculate).setOnClickListener
           (
             new View.OnClickListener()
               {
                 public void onClick
                   (
                     View TheButton
                   )
                   {
                     final HashMap<Integer, Double> Known = new HashMap<Integer, Double>();
                     for (int FieldID : Fields)
                       {
                         Double FieldValue = null;
                         final String FieldStr =
                             ((TextView)findViewById(FieldID)).getText().toString();
                         if (FieldStr.length() != 0)
                           {
                             try
                               {
                                 FieldValue = ParamDefs.get(FieldID).Parse.Parse(FieldStr);
                               }
                             catch (NumberFormatException Bad)
                               {
                                 SetError(FieldID);
                               } /*try*/
                           }
                         else
                           {
                             System.err.printf("Field “%s” initially unknown\n", FieldNames.get(FieldID)); /* debug */
                             SetUnknown(FieldID);
                           } /*if*/
                         if (FieldValue != null)
                           {
                             System.err.printf("Field “%s” initially known\n", FieldNames.get(FieldID)); /* debug */
                             SetValid(FieldID, FieldValue);
                             Known.put(FieldID, FieldValue);
                           } /*if*/
                       } /*for*/
                     for (;;)
                       {
                         boolean DidSomething = false;
                         boolean LeftUndone = false;
                         for (int FieldID : Fields)
                           {
                             if (!Known.containsKey(FieldID))
                               {
                                 System.err.printf("Field “%s” not yet known\n", FieldNames.get(FieldID)); /* debug */
                                 final ParamDef ThisParam = ParamDefs.get(FieldID);
                                 boolean DidThis = false;
                                 for (int[] ArgNames : ThisParam.Calculate.keySet())
                                   {
                                     boolean GotAll = true; /* to begin with */
                                     for (int ArgName : ArgNames)
                                       {
                                         if (!Known.containsKey(ArgName))
                                           {
                                             GotAll = false;
                                             break;
                                           } /*if*/
                                       } /*for*/
                                     if (GotAll)
                                       {
                                           { /* debug */
                                             System.err.printf("Field “%s” can be determined from ", FieldNames.get(FieldID)); /* debug */
                                             for (int i = 0; i < ArgNames.length; ++i)
                                               {
                                                 if (i != 0)
                                                   {
                                                     System.err.print(",");
                                                   } /*if*/
                                                 System.err.printf("“%s”", FieldNames.get(ArgNames[i]));
                                               } /*for*/
                                             System.err.println();
                                           } /* debug */
                                         final double[] Args = new double[ArgNames.length];
                                         for (int i = 0; i < ArgNames.length; ++i)
                                           {
                                             Args[i] = Known.get(ArgNames[i]);
                                           } /*for*/
                                         final double FieldValue =
                                             ThisParam.Calculate.get(ArgNames).Calculate(Args);
                                         Known.put(FieldID, FieldValue);
                                         SetValid(FieldID, FieldValue);
                                         DidThis = true;
                                         break;
                                       } /*if*/
                                   } /*for*/
                                 if (DidThis)
                                   {
                                     DidSomething = true;
                                   }
                                 else
                                   {
                                     LeftUndone = true;
                                   } /*if*/
                               } /*if*/
                           } /*for*/
                         if (!LeftUndone)
                             break;
                         if (!DidSomething)
                           {
                             android.widget.Toast.makeText
                               (
                                 /*context =*/ Main.this,
                                 /*text =*/ R.string.calc_incomplete,
                                 /*duration =*/ android.widget.Toast.LENGTH_SHORT
                               ).show();
                             break;
                           } /*if*/
                       } /*for*/
                   } /*onClick*/
               } /*View.OnClickListener*/
           );
         ClearAll();
       } /*onCreate*/
 
     @Override
     public void onRestoreInstanceState
       (
         android.os.Bundle SavedInstanceState
       )
       {
       /* need to repeat setChecked calls to avoid getting out of sync with UI display */
         for (final int UnitsID : UnitsButtons)
           {
             ((android.widget.RadioButton)findViewById(UnitsID)).setChecked
               (
                 UnitsID == (CurUnits == Units.UNITS_CM ? R.id.units_cm : R.id.units_in)
               );
           } /*for*/
       } /*onRestoreInstanceState*/
 
   } /*Main*/;
