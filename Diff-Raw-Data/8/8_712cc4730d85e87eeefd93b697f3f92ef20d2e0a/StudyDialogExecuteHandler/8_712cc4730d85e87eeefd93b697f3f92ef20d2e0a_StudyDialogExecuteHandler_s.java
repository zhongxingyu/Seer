 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Aye Thu
  */
 
 /**
 * $Id: StudyDialogExecuteHandler.java,v 1.3 2003-10-31 08:33:50 aye.thu Exp $
  */
 package app.cts.form.study;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.handler.DialogExecuteRecordEditorHandler;
 import com.netspective.axiom.ConnectionContext;
 
 import auto.dcb.study.ProfileContext;
 import auto.dal.db.DataAccessLayer;
 import auto.dal.db.vo.impl.StudyVO;
 import auto.dal.db.vo.impl.StudyOrgRelationshipVO;
 import auto.dal.db.dao.StudyTable;
 import auto.dal.db.dao.study.StudyOrgRelationshipTable;
 import auto.id.sql.schema.db.enum.RecordStatus;
 import auto.id.sql.schema.db.enum.StudyOrgRelationType;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Class for handling the execution of the Study dialog in different perspectives
  */
 public class StudyDialogExecuteHandler extends  DialogExecuteRecordEditorHandler
 {
     private static Log log = LogFactory.getLog(StudyDialogExecuteHandler.class);
     /**
      * Adds a new study record and its related children records into the database
      * @param dc
      * @param cc
      * @throws DialogExecuteException
      */
     public void addRecord(DialogContext dc, ConnectionContext cc) throws DialogExecuteException
     {
         ProfileContext pc = new ProfileContext(dc);
         try
         {
             StudyTable studyTable = DataAccessLayer.getInstance().getStudyTable();
             StudyOrgRelationshipTable soRelationTable = DataAccessLayer.getInstance().getStudyTable().getStudyOrgRelationshipTable();
             StudyTable.Record studyRecord = studyTable.createRecord();
 
             StudyVO svo = new StudyVO();
 
             // These are the required fields
             svo.setStudyName(pc.getStudyName().getTextValue());
             svo.setStudyDescr(pc.getStudyDescr().getTextValue());
             svo.setStudyCode(pc.getStudyCode().getTextValue());
             svo.setStartDate(pc.getStartDate().getDateValue());
             svo.setActualEndDate(pc.getActualEndDate().getDateValue());
             svo.setTargetEndDate(pc.getTargetEndDate().getDateValue());
             svo.setIrbName(pc.getIrbName().getTextValue());
             svo.setIrbNumber(pc.getIrbNumber().getTextValue());
             svo.setRecStatId(new Integer(RecordStatus.ACTIVE));
             svo.setStudyStatusInt(pc.getStudyStatus().getIntValue());
 
             // These are the optional fields
            if (pc.getStudyStage().getSelectedChoice() != null)
                 svo.setStudyStageInt(pc.getStudyStage().getIntValue());
 
             studyRecord.setValues(svo);
             studyRecord.insert(cc);
 
             // handle the children records
            if (pc.getSponsorOrgName().getSelectedChoice() != null)
             {
                 StudyOrgRelationshipVO sorVO = new StudyOrgRelationshipVO();
                 //sorVO.setParentIdLong(studyRecord.getStudyId().getLongValue());
                 sorVO.setRelEntityIdLong(pc.getSponsorOrgName().getLongValue());
                 sorVO.setRelTypeIdInt(StudyOrgRelationType.SPONSOR);
                 sorVO.setRecStatId(new Integer(RecordStatus.ACTIVE));
                 StudyOrgRelationshipTable.Record childRecord = soRelationTable.createChildLinkedByParentId(studyRecord);
                 childRecord.setValues(sorVO);
                 childRecord.insert(cc);
             }
 
             cc.getConnection().commit();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             throw new DialogExecuteException("Failed to insert row", e);
         }
 
     }
 
     /**
      * Updates the Study record in the database
      * @param dc
      * @param cc
      * @throws DialogExecuteException
      */
     public void editRecord(DialogContext dc, ConnectionContext cc) throws DialogExecuteException
     {
         ProfileContext pc = new ProfileContext(dc);
         try
         {
             StudyTable studyTable = DataAccessLayer.getInstance().getStudyTable();
             StudyTable.Record record = studyTable.getRecordByPrimaryKey(cc, new Long(pc.getStudyId().getTextValue()), true);
             if (record == null)
             {
                 log.error("Failed to update record due to unknown study ID");
                 throw new DialogExecuteException("Failed to update record due to unknown study ID");
             }
             StudyVO svo = new StudyVO();
             svo.setStudyName(pc.getStudyName().getTextValue());
             svo.setStudyDescr(pc.getStudyDescr().getTextValue());
             svo.setStudyCode(pc.getStudyCode().getTextValue());
             svo.setStartDate(pc.getStartDate().getDateValue());
             svo.setActualEndDate(pc.getActualEndDate().getDateValue());
             svo.setTargetEndDate(pc.getTargetEndDate().getDateValue());
             svo.setIrbName(pc.getIrbName().getTextValue());
             svo.setIrbNumber(pc.getIrbNumber().getTextValue());
             svo.setRecStatId(new Integer(RecordStatus.ACTIVE));
             svo.setStudyStatusInt(pc.getStudyStatus().getIntValue());
             svo.setStudyStageInt(pc.getStudyStage().getIntValue());
             record.setValues(svo);
             record.update(cc);
             cc.commitAndClose();
             System.out.println("Study Updated " + pc.getStudyStage().getIntValue());
         }
         catch (Exception e)
         {
             e.printStackTrace();
             log.error("Failed to edit record", e);
             throw new DialogExecuteException("Failed to edit record", e);
         }
     }
 
     /**
      * Deletes the Study record and its related children records from the database
      * @param dc
      * @param cc
      * @throws DialogExecuteException
      */
     public void deleteRecord(DialogContext dc, ConnectionContext cc) throws DialogExecuteException
     {
         ProfileContext pc = new ProfileContext(dc);
         try
         {
             StudyTable studyTable = DataAccessLayer.getInstance().getStudyTable();
             StudyTable.Record record = studyTable.getRecordByPrimaryKey(cc, new Long(pc.getStudyId().getTextValue()), true);
             record.delete(cc);
             cc.commitAndClose();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             throw new DialogExecuteException("Failed to delete record", e);
         }
     }
 
 }
