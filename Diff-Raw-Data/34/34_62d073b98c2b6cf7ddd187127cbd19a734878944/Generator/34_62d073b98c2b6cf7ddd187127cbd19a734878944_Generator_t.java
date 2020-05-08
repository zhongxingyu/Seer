 /* ====================================================================
  *   Copyright 2003-2005 Fabrizio Giustina.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * ====================================================================
  */
 package net.sf.commonclipse;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IBuffer;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.ISourceReference;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.ToolFactory;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 
 
 /**
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public abstract class Generator
 {
 
     /**
      * line separator.
      */
     private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
 
     /**
      * Generates the appropriate method in <code>type</code>.
      * @param type IType
      * @param shell Shell
      */
     public void generate(IType type, Shell shell)
     {
 
         ICompilationUnit cu = (ICompilationUnit) type.getAncestor(IJavaElement.COMPILATION_UNIT);
 
         // first check if file is writable
         IResource resource;
         try
         {
             resource = cu.getCorrespondingResource();
         }
         catch (JavaModelException e)
         {
             MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), e.getMessage()); //$NON-NLS-1$
             return;
         }
         if (resource != null && resource.getResourceAttributes().isReadOnly())
         {
             IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{(IFile) resource}, shell);
 
             if (!status.isOK())
             {
                 MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), CCMessages //$NON-NLS-1$
                     .getString("Generator.readonly")); //$NON-NLS-1$
                 return;
             }
         }
 
         resource = null;
 
         if (!validate(type, shell))
         {
             return;
         }
 
         ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
         progressDialog.open();
         try
         {
 
             IProgressMonitor monitor = progressDialog.getProgressMonitor();
             generateMethod(type, cu, shell, monitor);
         }
         catch (JavaModelException ex)
         {
             MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), ex.getMessage()); //$NON-NLS-1$
         }
 
         progressDialog.close();
     }
 
     /**
      * Checks if a corresponding method already exists and prompt the user for replacing it.
      * @param type IType
      * @param shell Shell
      * @return <code>true</code> if the method doesn't exists or the user has choosen to overwrite it
      */
     protected boolean validate(IType type, Shell shell)
     {
 
         IMethod method = getExistingMethod(type);
 
         if (method != null && method.exists())
         {
             boolean dontAsk = CCPluginPreferences.getPreferences().dontAskOnOverwrite();
 
             if (dontAsk
                 || MessageDialog.openConfirm(shell, CCPlugin.PLUGIN_NAME, MessageFormat.format(CCMessages
                     .getString("Generator.methodexists"), //$NON-NLS-1$
                     new Object[]{getMethodName(), type.getElementName()})))
             {
                 try
                 {
                     method.delete(true, null);
                     return true;
                 }
                 catch (JavaModelException e)
                 {
                     MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), //$NON-NLS-1$
                         MessageFormat.format(CCMessages.getString("Generator.unabletodelete"), //$NON-NLS-1$
                             new Object[]{getMethodName(), e.getMessage()}));
                 }
             }
             return false;
         }
 
         return true;
     }
 
     /**
      * Generates the method by:
      * <ul>
      * <li>call createMethod</li>
      * <li>format the given method</li>
      * <li>add it to type</li>
      * <li>call addImports</li>
      * </ul>.
      * @param type IType
      * @param cu compilation unit
      * @param shell Shell for messages
      * @param monitor progress monitor, updated during processing
      * @throws JavaModelException any exception in method generation
      */
     public void generateMethod(IType type, ICompilationUnit cu, Shell shell, IProgressMonitor monitor)
         throws JavaModelException
     {
         String className = type.getElementName();
 
         String title = MessageFormat.format(CCMessages.getString("Generator.generating"), //$NON-NLS-1$
             new Object[]{className});
         monitor.beginTask(title, 100);
         monitor.worked(10);
 
         monitor.setTaskName(title + CCMessages.getString("Generator.parsing")); //$NON-NLS-1$
         String src = createMethod(type);
         monitor.worked(30);
 
         monitor.setTaskName(title + CCMessages.getString("Generator.formatting")); //$NON-NLS-1$
         Document document = new Document(src);
 
         TextEdit text = ToolFactory.createCodeFormatter(null).format(
             CodeFormatter.K_UNKNOWN,
             src,
             0,
             src.length(),
             getIndentUsed(type, cu) + 1,
             null);
 
         try
         {
             text.apply(document);
         }
         catch (MalformedTreeException ex)
         {
             MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), ex.getMessage()); //$NON-NLS-1$
         }
         catch (BadLocationException ex)
         {
             MessageDialog.openError(shell, CCMessages.getString("Generator.errortitle"), ex.getMessage()); //$NON-NLS-1$
         }
 
         monitor.worked(20);
 
         monitor.setTaskName(title + CCMessages.getString("Generator.adding")); //$NON-NLS-1$
         type.createMethod(document.get() + LINE_SEPARATOR, null, false, null);
 
         monitor.worked(20);
 
         monitor.setTaskName(title + CCMessages.getString("Generator.imports")); //$NON-NLS-1$
         addImports(type);
         monitor.worked(20);
 
         monitor.done();
     }
 
     /**
      * Evaluates the indention used by a Java element.
      * @param elem Java element
      * @param cu compilation unit
      * @return indentation level
      * @throws JavaModelException model exception when trying to access source
      */
     public int getIndentUsed(IJavaElement elem, ICompilationUnit cu) throws JavaModelException
     {
         if (elem instanceof ISourceReference)
         {
 
             if (cu != null)
             {
                 IBuffer buf = cu.getBuffer();
                 int offset = ((ISourceReference) elem).getSourceRange().getOffset();
                 int i = offset;
                 // find beginning of line
                 while (i > 0 && !isLineDelimiterChar(buf.getChar(i - 1)))
                 {
                     i--;
                 }
 
                 return computeIndent(buf.getText(i, offset - i));
             }
         }
         return 0;
     }
 
     /**
      * Line delimiter chars are '\n' and '\r'.
      * @param ch char
      * @return <code>true</code> if ch is '\n' or '\r'
      */
     private boolean isLineDelimiterChar(char ch)
     {
         return ch == '\n' || ch == '\r';
     }
 
     /**
      * Returns the indent of the given string.
      * @param line the text line
      * @return indent level
      */
     public int computeIndent(String line)
     {
         IPreferenceStore preferences = CCPlugin.getDefault().getPreferenceStore();
         int tabWidth = preferences.getInt(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
 
         int result = 0;
         int blanks = 0;
         int size = line.length();
         for (int i = 0; i < size; i++)
         {
             char c = line.charAt(i);
             if (c == '\t')
             {
                 result++;
                 blanks = 0;
             }
             else if (Character.isWhitespace(c) && !(c == '\n' || c == '\r'))
             {
                 blanks++;
                 if (blanks == tabWidth)
                 {
                     result++;
                     blanks = 0;
                 }
             }
             else
             {
                 return result;
             }
         }
         return result;
     }
 
     /**
      * Returns the generated method name.
      * @return String method name
      */
     protected abstract String getMethodName();
 
     /**
      * Creates the method for the ITYPE type.
      * @param type Itype
      * @return Method String
      * @throws JavaModelException exception in creating method
      */
     protected abstract String createMethod(IType type) throws JavaModelException;
 
     /**
      * Returns the existing method.
      * @param type IType
      * @return IMethod
      */
     protected abstract IMethod getExistingMethod(IType type);
 
     /**
      * Adds required imports to type.
      * @param type IType
      * @throws JavaModelException exception in adding imports
      */
     protected abstract void addImports(IType type) throws JavaModelException;
 
     /**
      * Iterates on fields and call getFieldString() on any match not in the configurable excluded list.
      * @param type IType
      * @return String
      * @throws JavaModelException exception in analyzing fields
      */
     protected String buildAppenderList(IType type) throws JavaModelException
     {
         // temporary map for caching type and supertypes fields and avoid duplicated fields
         Map<String, IField> fields = buildFieldMap(type);
 
         // start building method body
         StringBuffer buffer = new StringBuffer();
         Iterator<String> fieldsIterator = fields.keySet().iterator();
 
         while (fieldsIterator.hasNext())
         {
            String fieldName = fieldsIterator.next();
 
             // only add field if not excluded by user preferences
             if (!isExcluded(fieldName))
             {
                 buffer.append(getFieldAppender(fieldName, fieldName));
             }
         }
 
         return buffer.toString();
 
     }
 
     /**
      * Returns a Set containing all the names of fields visible by this type.
      * @param type IType
      * @return Map containing field names - IField objects
      * @throws JavaModelException exception in analyzing type
      */
     protected Map<String, IField> buildFieldMap(IType type) throws JavaModelException
     {
         Map<String, IField> fieldNames = new HashMap<String, IField>();
 
         IField[] fields = type.getFields();
 
         for (int j = 0; j < fields.length; j++)
         {
             IField field = fields[j];
             int flags = field.getFlags();
 
             if (!Flags.isStatic(flags))
             {
                 fieldNames.put(field.getElementName(), field);
             }
         }
 
         // get all the supertypes to look for public and protected fields
         ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
         IType[] types = hierarchy.getSupertypes(type);
 
         for (int j = 0; j < types.length; j++)
         {
             IField[] superFields = types[j].getFields();
 
             for (int x = 0; x < superFields.length; x++)
             {
                 IField field = superFields[x];
                 int flags = field.getFlags();
 
                 // no static and private
                 if (!Flags.isStatic(flags) && !Flags.isPrivate(flags))
                 {
                     fieldNames.put(field.getElementName(), field);
                 }
             }
         }
         return fieldNames;
     }
 
     /**
      * Checks if a given field should be excluded from generated method.
      * @param fieldName field/property name
      * @return <code>true</code> if the field should not be included in generated method
      */
     protected boolean isExcluded(String fieldName)
     {
         Pattern exclusion = CCPluginPreferences.getPreferences().getExcludedFielsPattern();
         return exclusion.matcher(fieldName).matches();
     }
 
     /**
      * get the "append" statement for a field.
      * @param fieldName name of the field
      * @param accessor can be different for fieldname
      * @return String
      */
     protected abstract String getFieldAppender(String fieldName, String accessor);
 
 }
