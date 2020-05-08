 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: RecordDeleteComponentCommand.java,v 1.1 2003-01-20 05:33:06 roque.hernandez Exp $
  */
 
 package com.netspective.sparx.xaf.html.command;
 
 import com.netspective.sparx.xaf.form.DialogSkin;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.form.Dialog;
 import com.netspective.sparx.xaf.sql.StatementNotFoundException;
 import com.netspective.sparx.xaf.sql.StatementDialog;
 import com.netspective.sparx.xaf.report.ReportSkin;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.xaf.html.ComponentCommandFactory;
 import com.netspective.sparx.xaf.html.ComponentCommandException;
 import com.netspective.sparx.util.value.ValueContext;
 import com.netspective.sparx.xif.db.DatabaseContext;
 import com.netspective.sparx.xif.db.DatabaseContextFactory;
 
 import javax.naming.NamingException;
 import java.util.StringTokenizer;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.SQLException;
 
 public class RecordDeleteComponentCommand extends StatementComponentCommand
 {
     static public final String COMMAND_ID = "record-edit-delete";
 
     static public final Documentation DOCUMENTATION = new Documentation(
                 "Displays results of a SQL statement and a dialog to be executed along with it in delete mode.",
                 new Documentation.Parameter[]
                     {
                         new Documentation.Parameter("statement-name", true, null, null, "The fully qualified name of the statement (package-name.statement-name)."),
                         new Documentation.Parameter("dialog-name", false, null, null, "The fully qualified name of the dialog to show next to the statement (for data-editing)."),
                     });
 
     public void setCommand(StringTokenizer params)
     {
         statementName = params.nextToken();
         reportId = null;
         rowsPerPage = UNLIMITED_ROWS;
         skinName = "record-editor";
         setUrlFormats(null);
 
         if(params.hasMoreTokens())
             dialogCommand = ComponentCommandFactory.getDialogCommand(params.nextToken() + ",delete");
         else {
             dialogCommand = ComponentCommandFactory.getDialogCommand(statementName + ",delete");
         }
     }
 }
