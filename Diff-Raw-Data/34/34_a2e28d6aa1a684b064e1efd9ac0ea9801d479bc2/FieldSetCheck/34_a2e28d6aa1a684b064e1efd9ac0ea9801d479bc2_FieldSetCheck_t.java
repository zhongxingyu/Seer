 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.checks;
 
 import java.util.Arrays;
 import java.util.Comparator;
 
 import net.sf.clirr.framework.ClassChangeCheck;
 import net.sf.clirr.framework.AbstractDiffReporter;
 import net.sf.clirr.framework.ApiDiffDispatcher;
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.Severity;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.classfile.Field;
 
 /**
  * Checks the fields of a class.
  *
  * @author lkuehne
  */
 public class FieldSetCheck
         extends AbstractDiffReporter
         implements ClassChangeCheck
 {
     private static final class FieldNameComparator implements Comparator
     {
         public int compare(Object o1, Object o2)
         {
             Field f1 = (Field) o1;
             Field f2 = (Field) o2;
 
             final String name1 = f1.getName();
             final String name2 = f2.getName();
 
             return name1.compareTo(name2);
         }
     }
 
     private Comparator comparator = new FieldNameComparator();
 
     public FieldSetCheck(ApiDiffDispatcher dispatcher)
     {
         super(dispatcher);
     }
 
     public final void check(JavaClass compatBaseline, JavaClass currentVersion)
     {
         final Field[] baselineFields = compatBaseline.getFields();
         final Field[] currentFields = currentVersion.getFields();
 
         // Sigh... BCEL 5.1 hands out it's internal datastructure,
         // so we have to make a copy here to make sure we don't mess up BCEL by sorting
 
         final Field[] bFields = createSortedCopy(baselineFields);
         final Field[] cFields = createSortedCopy(currentFields);
 
         checkForChanges(bFields, cFields, compatBaseline, currentVersion);
     }
 
     private void checkForChanges(
             Field[] bFields, Field[] cFields, JavaClass baseLineClass, JavaClass currentClass)
     {
         boolean[] newInCurrent = new boolean[cFields.length];
         Arrays.fill(newInCurrent, true);
 
         for (int i = 0; i < bFields.length; i++)
         {
             Field bField = bFields[i];
             if (!bField.isPublic() && !bField.isProtected())
             {
                 continue;
             }
             int cIdx = Arrays.binarySearch(cFields, bField, comparator);
             if (cIdx < 0)
             {
                 final String name = bField.getName();
                 fireDiff("Field " + name + " has been removed", Severity.ERROR, baseLineClass, bField);
             }
             else
             {
                 Field cField = cFields[cIdx];
                 newInCurrent[cIdx] = false;
                 checkForModifierChange(bField, cField, currentClass);
                 checkForVisibilityChange(bField, cField, currentClass);
             }
         }
 
         for (int i = 0; i < newInCurrent.length; i++)
         {
             Field field = cFields[i];
             if (newInCurrent[i] && (field.isPublic() || field.isProtected()))
             {
                 String scope = field.isPublic() ? "public" : "protected";
                 final String fieldName = field.getName();
                 fireDiff("Added " + scope + " field " + fieldName, Severity.INFO, currentClass, field);
             }
         }
 
         // TODO: Check field types
 
         // TODO: warn about constant value changes (see JLS, section 13.4.8)
     }
 
     private void checkForModifierChange(Field bField, Field cField, JavaClass clazz)
     {
         if (bField.isFinal() && !cField.isFinal())
         {
             fireDiff("Field " + bField.getName() + " is now non-final", Severity.INFO, clazz, cField);
         }
 
         if (!bField.isFinal() && cField.isFinal())
         {
             fireDiff("Field " + bField.getName() + " is now final", Severity.ERROR, clazz, cField);
         }
 
        if (bField.isStatic() && !cField.isStatic())
         {
             fireDiff("Field " + bField.getName() + " is now non-static", Severity.ERROR, clazz, cField);
         }
 
        if (!bField.isStatic() && cField.isStatic())
         {
             fireDiff("Field " + bField.getName() + " is now static", Severity.ERROR, clazz, cField);
         }
 
         // JLS, 13.4.10: Adding or deleting a transient modifier of a field
         // does not break compatibility with pre-existing binaries
 
         // TODO: What about volatile?
     }
 
     private void checkForVisibilityChange(Field bField, Field cField, JavaClass clazz)
     {
         if (bField.isProtected() && cField.isPublic())
         {
             fireDiff("Field " + bField.getName() + " is now public", Severity.INFO, clazz, cField);
         }
         else if (bField.isProtected() && !(cField.isProtected() || cField.isPublic())
                 || bField.isPublic() && !cField.isPublic())
         {
             fireDiff("Accessibility of field " + bField.getName() + " has been weakened",
                     Severity.ERROR, clazz, cField);
         }
     }
 
     private void fireDiff(String report, Severity severity, JavaClass clazz, Field field)
     {
         final String className = clazz.getClassName();
         final ApiDifference diff =
                 new ApiDifference(report + " in " + className,
                         severity, className, null, field.getName());
         getApiDiffDispatcher().fireDiff(diff);
 
     }
 
     private Field[] createSortedCopy(final Field[] orig)
     {
         final Field[] fields = new Field[orig.length];
         System.arraycopy(orig, 0, fields, 0, orig.length);
         Arrays.sort(fields, comparator);
         return fields;
     }
 }
