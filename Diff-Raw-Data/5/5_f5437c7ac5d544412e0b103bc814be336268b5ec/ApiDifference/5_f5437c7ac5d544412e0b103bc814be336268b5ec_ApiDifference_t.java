 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003  Lars Khne
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
 
 package net.sf.clirr.event;
 
 
 /**
  * Describes an API change.
  *
  * @author Lars
  */
 public final class ApiDifference
 {
     private static final int HASHCODE_MAGIC = 29;
 
     /** human readable change report. */
     private String report;
 
     /** severity of the change, as determined by clirr. */
     private Severity severity;
 
     /** The fully qualified class name that is affected by the API change. */
     private String affectedClass;
 
     /**
      * The method that is affected, if any.
      * <p/>
      * The content is the method name plus the fully qualified
      * parameter types separated by comma and space and enclosed in
      * brackets, e.g. "doStuff(java.lang.String, int)".
      * <p/>
      * This value is <code>null</code> if no single method is
      * affected, i.e. if the
      * api change affects a field or is global
      * (like "class is now final").
      */
     private String affectedMethod;
 
     /**
      * The field that is affected, if any.
      * <p/>
      * The content is the field name, e.g. "someValue".
      * Type information for the field is not available.
      * <p/>
      * This value is <code>null</code> if no single field is
      * affected, i.e. if the
      * api change affects a method or is global
      * (like "class is now final").
      */
     private String affectedField;
 
     /**
      * Create a new API differnce representation.
      *
     * @param report a human readable string describing the change that was made.
      * @param severity the severity in terms of binary API compatibility.
      */
     public ApiDifference(String report, Severity severity, String clazz, String method, String field)
     {
         this.report = report;
         this.severity = severity;
         this.affectedClass = clazz;
         this.affectedField = field;
         this.affectedMethod = method;
     }
 
     /**
      * The Severity of the API difference. ERROR means that clients will
      * definately break, WARNING means that clients may break, depending
      * on how they use the library. See the eclipse paper for further
      * explanation.
      *
      * @return the severity of the API difference.
      */
     public Severity getSeverity()
     {
         return severity;
     }
 
     /**
      * Human readable api change description.
      *
      * @return a human readable description of this API difference.
      */
     public String getReport()
     {
         return report;
     }
 
     public String getAffectedClass()
     {
         return affectedClass;
     }
 
     public String getAffectedMethod()
     {
         return affectedMethod;
     }
 
     public String getAffectedField()
     {
         return affectedField;
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
         return report + " (" + severity + ") - "
                + getAffectedClass() + '[' + getAffectedField() + '/' + getAffectedMethod() + ']';
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean equals(Object o)
     {
         if (this == o)
         {
             return true;
         }
 
         if (!(o instanceof ApiDifference))
         {
             return false;
         }
 
         final ApiDifference other = (ApiDifference) o;
 
         if (report != null ? !report.equals(other.report) : other.report != null)
         {
             return false;
         }
 
         if (severity != null ? !severity.equals(other.severity) : other.severity != null)
         {
             return false;
         }
 
         final String otherClass = other.affectedClass;
         if (affectedClass != null ? !affectedClass.equals(otherClass) : otherClass != null)
         {
             return false;
         }
 
         final String otherMethod = other.affectedMethod;
         if (affectedMethod != null ? !affectedMethod.equals(otherMethod) : otherMethod != null)
         {
             return false;
         }
 
         final String otherField = other.affectedField;
         if (affectedField != null ? !affectedField.equals(otherField) : otherField != null)
         {
             return false;
         }
 
         return true;
     }
 
     public int hashCode()
     {
         int result;
         result = report != null ? report.hashCode() : 0;
         result = HASHCODE_MAGIC * result + (severity != null ? severity.hashCode() : 0);
         result = HASHCODE_MAGIC * result + (affectedClass != null ? affectedClass.hashCode() : 0);
         result = HASHCODE_MAGIC * result + (affectedMethod != null ? affectedMethod.hashCode() : 0);
         result = HASHCODE_MAGIC * result + (affectedField != null ? affectedField.hashCode() : 0);
         return result;
     }
 }
