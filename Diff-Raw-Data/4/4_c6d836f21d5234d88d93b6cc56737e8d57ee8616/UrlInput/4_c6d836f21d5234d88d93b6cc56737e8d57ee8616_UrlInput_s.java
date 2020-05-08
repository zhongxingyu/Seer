  /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the GNU Lesser General Public License, Version 2.1. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
  * Data Integration.  The Initial Developer is Samatar HASSAN.
  *
  * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.*/
 
 package be.ibridge.kettle.url;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 import java.util.Iterator;
 
 import org.apache.commons.httpclient.util.ParameterParser;
 import org.apache.commons.httpclient.NameValuePair;
 
 import org.pentaho.di.core.Const;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.row.RowDataUtil;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.row.ValueMetaInterface;
 import org.pentaho.di.i18n.BaseMessages;
 import org.pentaho.di.trans.Trans;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.BaseStep;
 import org.pentaho.di.trans.step.StepDataInterface;
 import org.pentaho.di.trans.step.StepInterface;
 import org.pentaho.di.trans.step.StepMeta;
 import org.pentaho.di.trans.step.StepMetaInterface;
 
 
 
 /**
  * Read Url files, parse them and convert them to rows and writes these to one or more output 
  * streams.
  */
 public class UrlInput extends BaseStep implements StepInterface  
 {
 	private static Class<?> PKG = UrlInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
 
 	private UrlInputMeta meta;
 	private UrlInputData data;
 
 	
 	public UrlInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
 	{
 		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
 	}
    
     private boolean ReadNextString()
    {
 	   
 	   try {
 		   data.readrow= getRow();  // Grab another row ...
 		   
 		   if (data.readrow==null)  {
 			   // finished processing!
            	   if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "UrlInput.Log.FinishedProcessing"));
                return false;
            }
  
 		   if(first) {
 			    first=false;
 
                 data.inputRowMeta = getInputRowMeta();
 	            data.outputRowMeta = data.inputRowMeta.clone();
 	            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
 	            
 	            // Get total previous fields
 	            data.totalpreviousfields=data.inputRowMeta.size();
 
 	            // For String to <type> conversions, we allocate a conversion meta data row as well...
 				//
 				data.convertRowMeta = data.outputRowMeta.clone();
 				for (int i=0;i<data.convertRowMeta.size();i++) {
 					data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
 				}
 				
 				// Check if source field is provided
 				if (Const.isEmpty(meta.getFieldValue())){
 					logError(BaseMessages.getString(PKG, "UrlInput.Log.NoField"));
 					throw new KettleException(BaseMessages.getString(PKG, "UrlInput.Log.NoField"));
 				}
 				// cache the position of the field			
 				if (data.indexSourceField<0) {	
 					data.indexSourceField =getInputRowMeta().indexOfValue(meta.getFieldValue());
 					if (data.indexSourceField<0)
 					{
 						// The field is unreachable !
 						logError(BaseMessages.getString(PKG, "UrlInput.Log.ErrorFindingField", meta.getFieldValue())); //$NON-NLS-1$ //$NON-NLS-2$
 						throw new KettleException(BaseMessages.getString(PKG, "UrlInput.Exception.CouldnotFindField",meta.getFieldValue())); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				}
 			
 			}
 		   
 		   // get source field value
 		   String fieldValue= getInputRowMeta().getString(data.readrow,data.indexSourceField);
 			
 		   if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "UrlInput.Log.SourceValue", meta.getFieldValue(),fieldValue));
 
            data.urlToParse = new URL(fieldValue);
 	   } catch(Exception e) {
 			logError(BaseMessages.getString(PKG, "UrlInput.Log.UnexpectedError", e.toString()));
 			stopAll();
 			logError(Const.getStackTracker(e));
 			setErrors(1);
 			return false;
 		}
 		return true;
 	   
    }
 
 	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
 		Object[] r=null;
 		try {
 			 // Grab a row
 			r=getOneRow();
 			if (r==null)  {
 				setOutputDone();  // signal end to receiver(s)
 				return false; // end of data or error.
 		    }
 			 
 			if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "UrlInput.Log.ReadRow", data.outputRowMeta.getString(r)));
 			incrementLinesInput();
 			data.rownr++;
 			 
 			putRow(data.outputRowMeta, r);  // copy row to output rowset(s);
 			  
 		} catch(Exception e) {
 			boolean sendToErrorRow=false;
 			String errorMessage=null;
             e.printStackTrace();
 			if (getStepMeta().isDoingErrorHandling()) {
                 sendToErrorRow = true;
                 errorMessage = e.toString();
 	        } else {
 				logError(BaseMessages.getString(PKG, "UrlInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
 				setErrors(1);
 				stopAll();
 				setOutputDone();  // signal end to receiver(s)
 				return false;
 			}
 			if (sendToErrorRow)  {
 				 // Simply add this row to the error row
 				putError(getInputRowMeta(), r, 1, errorMessage, null, "UrlInput001");
 	         }
 			
 		}
 		return true;
 	}
 	
 	private Object[] getOneRow()  throws KettleException {
 
 			while ((data.recordnr>=data.nrrecords || data.readrow==null)) {
 				if(!ReadNextString()) {
 					return null;
 				} else {
                     return buildRow();
                 }
 			}
 
 		return null;
 	}
 
 
 		
 	private Object[] buildRow() throws KettleException {
 
 		Object[] outputRowData = RowDataUtil.resizeArray(data.readrow, data.convertRowMeta.size());
 
 		// Read fields...
 		for (int i=0;i<data.nrInputFields;i++) {	
 			// Get field
 			UrlInputField field = meta.getInputFields()[i];
 
 			// get url array for field
             ParameterParser p = new ParameterParser();
             List l = p.parse(data.urlToParse.getQuery(), '&');
 
             String nodevalue = "";
             Iterator iter = l.iterator();
             while (iter.hasNext()) {
                 NameValuePair param = (NameValuePair) iter.next();
                 if (param.getName().equalsIgnoreCase(field.getValue())) {
                     nodevalue = param.getValue();
                 }
             }
 
 			// Do trimming
 			switch (field.getTrimType()) {
 				case UrlInputField.TYPE_TRIM_LEFT:
 					nodevalue = Const.ltrim(nodevalue);
 					break;
 				case UrlInputField.TYPE_TRIM_RIGHT:
 					nodevalue = Const.rtrim(nodevalue);
 					break;
 				case UrlInputField.TYPE_TRIM_BOTH:
 					nodevalue = Const.trim(nodevalue);
 					break;
 				default:
 					break;
 			}
 			
 			// Do conversions
 			//
 			ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(data.totalpreviousfields+i);
 			ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.totalpreviousfields+i);
 			outputRowData[data.totalpreviousfields+i] = targetValueMeta.convertData(sourceValueMeta, nodevalue);
 
 			// Do we need to repeat this field if it is null?
 			if (meta.getInputFields()[i].isRepeated())  {
 				if (data.previousRow!=null && Const.isEmpty(nodevalue)) {
 					outputRowData[data.totalpreviousfields+i] = data.previousRow[data.totalpreviousfields+i];
 				}
 			}
 		}// End of loop over fields...	
 		
 		int rowIndex = data.totalpreviousfields + data.nrInputFields;
 		
 		if (meta.getAuthorityField()!=null && meta.getAuthorityField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getAuthority();
 		}
 		if (meta.getDefaultPortField()!=null && meta.getDefaultPortField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getDefaultPort();
 		}
 		if (meta.getFileField()!=null && meta.getFileField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getFile();
 		}
 		if (meta.getHostField()!=null && meta.getHostField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getHost();
 		}
 		if (meta.getPathField()!=null && meta.getPathField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getPath();
 		}
 		if (meta.getPortField()!=null && meta.getPortField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getPort();
 		}
 		if (meta.getProtocolField()!=null && meta.getProtocolField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getProtocol();
 		}
 		if (meta.getQueryField()!=null && meta.getQueryField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getQuery();
 		}
 		if (meta.getRefField()!=null && meta.getRefField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getRef();
 		}
 		if (meta.getUserInfoField()!=null && meta.getUserInfoField().length()>0) {
 			outputRowData[rowIndex++] = data.urlToParse.getUserInfo();
 		}
 		if (meta.getUriNameField()!=null && meta.getUriNameField().length()>0) {
             try {
                 outputRowData[rowIndex++] = data.urlToParse.toURI().toString();
             } catch(URISyntaxException ex) {
                 throw new KettleException(ex);
             }
 		}
 		data.recordnr++; 
 		
 		RowMetaInterface irow = getInputRowMeta();
 		
 		data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
 		// surely the next step doesn't change it in between...
 
 		return outputRowData;
 	}
 
 	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
 		meta=(UrlInputMeta)smi;
 		data=(UrlInputData)sdi;				
 		
 		if (super.init(smi, sdi)) {
 			data.rownr = 1L;
 			data.nrInputFields=meta.getInputFields().length;
 			// Take care of variable substitution
 			for(int i =0; i<data.nrInputFields; i++) {
 				UrlInputField field = meta.getInputFields()[i];
 				field.setValue(environmentSubstitute(field.getValue()));
 			}
 			
 			return true;
 		}
 		return false;		
 	}
 	
 	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
 		meta = (UrlInputMeta) smi;
 		data = (UrlInputData) sdi;
 		super.dispose(smi, sdi);
 	}
 }
