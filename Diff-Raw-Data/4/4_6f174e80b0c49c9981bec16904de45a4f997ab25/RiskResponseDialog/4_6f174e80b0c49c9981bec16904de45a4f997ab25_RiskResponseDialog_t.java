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
  * 4. The names "Netspective", "Sparx", and "Junxion" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Netspective where possible. We suggest using
  *    the "powered by" button or creating a "powered by" link to
  *    http://www.netspective.com for each application using this code.
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
 * $Id: RiskResponseDialog.java,v 1.6 2003-09-01 03:24:44 shahid.shah Exp $
  */
 
 package app;
 
 import java.io.Writer;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.sql.SQLException;
 
 import javax.naming.NamingException;
 
 import auto.dal.db.dao.RiskResponseTable;
 import auto.dal.db.DataAccessLayer;
 import auto.dal.db.vo.RiskResponse;
 import auto.dal.db.vo.impl.RiskResponseVO;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.ResultSetUtils;
 import com.netspective.axiom.ConnectionContext;
 
 public class RiskResponseDialog extends com.netspective.sparx.form.Dialog
 {
     public final static String FIELDPREFIX_RISK_ID = "risk_name_";
     public static final String FIELDPREFIXID_RISK_RESPONSE = "risk_response_";
     public static final String FIELDPREFIXID_RISK_GROUP = "risk_group_";
     public static final String FIELDPREFIXID_RISK_ROW_SYSTEM_ID = "risk_row_system_id_";
     public static final String FIELDNAME_IMMBUSUNIT_SIGNIFICANCE = ".immediate_bus_unit.significance";
     public static final String FIELDNAME_IMMBUSUNIT_EFFECTIVENESS = ".immediate_bus_unit.effectiveness";
     public static final String FIELDNAME_LARBUSGROUP_SIGNIFICANCE = ".larger_bus_group.significance";
     public static final String FIELDNAME_LARBUSGROUP_EFFECTIVENESS = ".larger_bus_group.effectiveness";
     public static final String FIELDNAME_ENTERPRISE_SIGNIFICANCE = ".enterprise.significance";
     public static final String FIELDNAME_ENTERPRISE_EFFECTIVENESS = ".enterprise.effectiveness";
 
     public List getRiskIdentifiers(DialogContext dc)
     {
         // get the names of all the risks so that we can add them appropriately
         List riskIdentifiers = new ArrayList();
         DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
         for(Iterator i = fieldStates.getStatesByQualifiedName().keySet().iterator(); i.hasNext(); )
         {
             String fieldName = (String) i.next();
             if(fieldName != null && fieldName.startsWith(FIELDPREFIX_RISK_ID))
                 riskIdentifiers.add(fieldName.substring(FIELDPREFIX_RISK_ID.length()));
         }
         return riskIdentifiers;
     }
 
     public void populateValues(DialogContext dc, int formatType)
     {
         super.populateValues(dc, formatType);
         if(dc.getAuthenticatedUser() == null)
             return;
 
         if(dc.isInitialEntry())
         {
             DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
 
             List riskIdentifiers = getRiskIdentifiers(dc);
             for(int i = 0; i < riskIdentifiers.size(); i++)
             {
                 String riskIdentifier = (String) riskIdentifiers.get(i);
                 Integer pin = ((AuthenticatedRespondent) dc.getAuthenticatedUser()).getRespondent().getPin();
                 String riskGroup = fieldStates.getState(FIELDPREFIXID_RISK_GROUP + riskIdentifier).getValue().getTextValue();
 
                 Query query = dc.getSqlManager().getQuery(auto.id.sql.query.Responses.BY_PIN_GROUP_RISK_ID);
                 try
                 {
                     QueryResultSet qrs = query.execute(dc, new Object[] { pin, riskGroup, riskIdentifier }, false);
                     Map responses = ResultSetUtils.getInstance().getResultSetSingleRowAsMap(qrs.getResultSet());
                     qrs.close(true);
                     qrs = null;
 
                     RiskResponseTable rrTable = DataAccessLayer.getInstance().getRiskResponseTable();
                     if(responses != null)
                     {
                         if(riskIdentifier.startsWith("additional"))
                         {
                             Object addlRiskNameValue = responses.get(rrTable.getRiskColumn().getName());
                             if(addlRiskNameValue != null)
                                 fieldStates.getState(FIELDPREFIX_RISK_ID + riskIdentifier).getValue().setTextValue(addlRiskNameValue.toString());
                         }
 
                         fieldStates.getState(FIELDPREFIXID_RISK_ROW_SYSTEM_ID + riskIdentifier).getValue().setTextValue(responses.get(rrTable.getSystemIdColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_IMMBUSUNIT_SIGNIFICANCE).getValue().setTextValue(responses.get(rrTable.getIbuSigColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_IMMBUSUNIT_EFFECTIVENESS).getValue().setTextValue(responses.get(rrTable.getIbuEffColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_LARBUSGROUP_SIGNIFICANCE).getValue().setTextValue(responses.get(rrTable.getLbgSigColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_LARBUSGROUP_EFFECTIVENESS).getValue().setTextValue(responses.get(rrTable.getLbgEffColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_ENTERPRISE_SIGNIFICANCE).getValue().setTextValue(responses.get(rrTable.getFirmSigColumn().getName()).toString());
                         fieldStates.getState(FIELDPREFIXID_RISK_RESPONSE + riskIdentifier + FIELDNAME_ENTERPRISE_EFFECTIVENESS).getValue().setTextValue(responses.get(rrTable.getFirmEffColumn().getName()).toString());
                     }
                 }
                 catch (Exception e)
                 {
                     getLog().error("Error populating values", e);
                     dc.getValidationContext().addError(e.toString());
                 }
             }
         }
     }
 
     public void execute(Writer writer, DialogContext dc) throws IOException
     {
         NavigationPage page = dc.getNavigationContext().getActivePage();
         AuthenticatedRespondent user = (AuthenticatedRespondent) dc.getAuthenticatedUser();
         if(user == null)
         {
             handlePostExecute(writer, dc, page.getNextPath().getUrl(dc));
             return;
         }
 
         List riskIdentifiers = getRiskIdentifiers(dc);
         ConnectionContext cc = null;
         try
         {
             RiskResponseTable riskResponseTable = DataAccessLayer.getInstance().getRiskResponseTable();
             cc = dc.getConnection(null, true);
             DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
 
             for(int i = 0; i < riskIdentifiers.size(); i++)
             {
                 String riskIdentifier = (String) riskIdentifiers.get(i);
                 String riskResponseGridName = FIELDPREFIXID_RISK_RESPONSE + riskIdentifier;
 
                 Integer pin = ((AuthenticatedRespondent) dc.getAuthenticatedUser()).getRespondent().getPin();
                 String riskGroup = fieldStates.getState(FIELDPREFIXID_RISK_GROUP + riskIdentifier).getValue().getTextValue();
                 String risk = fieldStates.getState(FIELDPREFIX_RISK_ID + riskIdentifier).getValue().getTextValue();
 
                 // if "additional" risks are presenent, they can be empty so filter them out
                 if(risk != null && risk.length() > 0)
                 {
                     String existingRowSystemId = fieldStates.getState(FIELDPREFIXID_RISK_ROW_SYSTEM_ID + riskIdentifier).getValue().getTextValue();
                     boolean haveExistingRow = existingRowSystemId != null && existingRowSystemId.length() > 0;
 
                     RiskResponse riskResponse = new RiskResponseVO();
                     if(haveExistingRow) riskResponse.setSystemId(Long.valueOf(existingRowSystemId));
                     riskResponse.setPin(pin);
                     riskResponse.setRiskGroup(riskGroup);
                     riskResponse.setRiskId(riskIdentifier);
                     riskResponse.setRisk(risk);
                     riskResponse.setIbuSig(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_IMMBUSUNIT_SIGNIFICANCE).getValue().getTextValue()));
                     riskResponse.setIbuEff(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_IMMBUSUNIT_EFFECTIVENESS).getValue().getTextValue()));
                     riskResponse.setLbgSig(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_LARBUSGROUP_SIGNIFICANCE).getValue().getTextValue()));
                     riskResponse.setLbgEff(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_LARBUSGROUP_EFFECTIVENESS).getValue().getTextValue()));
                     riskResponse.setFirmSig(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_ENTERPRISE_SIGNIFICANCE).getValue().getTextValue()));
                     riskResponse.setFirmEff(Integer.valueOf(fieldStates.getState(riskResponseGridName + FIELDNAME_ENTERPRISE_EFFECTIVENESS).getValue().getTextValue()));
 
                     RiskResponseTable.Record riskResponseRecord = riskResponseTable.createRecord();
                     riskResponseRecord.setValues(riskResponse);
                     if(haveExistingRow)
                         riskResponseRecord.update(cc);
                     else
                         riskResponseRecord.insert(cc);
                 }
             }
             cc.commitAndClose();
         }
         catch (NamingException e)
         {
             e.printStackTrace();  //To change body of catch statement use Options | File Templates.
         }
         catch (SQLException e)
         {
             try
             {
                 e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                 cc.rollbackAndClose();
             }
             catch (SQLException e1)
             {
                 e1.printStackTrace();  //To change body of catch statement use Options | File Templates.
             }
         }
         catch (NumberFormatException e)
         {
             e.printStackTrace();  //To change body of catch statement use Options | File Templates.
         }
 
         user.setVisitedPage(dc.getNavigationContext(), page);
         handlePostExecute(writer, dc, dc.getNavigationContext().getActivePage().getNextPath().getUrl(dc));
     }
 }
