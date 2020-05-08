 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 
 package org.geoserver.wps;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import net.opengis.ows11.CodeType;
 import net.opengis.wps10.ComplexDataType;
 import net.opengis.wps10.DataType;
 import net.opengis.wps10.DocumentOutputDefinitionType;
 import net.opengis.wps10.ExecuteResponseType;
 import net.opengis.wps10.ExecuteType;
 import net.opengis.wps10.InputReferenceType;
 import net.opengis.wps10.InputType;
 import net.opengis.wps10.LiteralDataType;
 import net.opengis.wps10.MethodType;
 import net.opengis.wps10.OutputDataType;
 import net.opengis.wps10.OutputDefinitionsType;
 import net.opengis.wps10.OutputReferenceType;
 import net.opengis.wps10.ProcessBriefType;
 import net.opengis.wps10.ProcessFailedType;
 import net.opengis.wps10.ProcessOutputsType1;
 import net.opengis.wps10.Wps10Factory;
 
 import org.geoserver.config.GeoServerInfo;
 import org.geoserver.ows.Ows11Util;
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wps.ppio.ComplexPPIO;
 import org.geoserver.wps.ppio.LiteralPPIO;
 import org.geoserver.wps.ppio.ProcessParameterIO;
 import org.geoserver.wps.ppio.ReferencePPIO;
 import org.geoserver.wps.ppio.XMLPPIO;
 import org.geotools.data.Parameter;
 import org.geotools.feature.NameImpl;
 import org.geotools.process.Process;
 import org.geotools.process.ProcessFactory;
 import org.geotools.process.Processors;
 import org.geotools.util.Converters;
 import org.geotools.xml.EMFUtils;
 import org.opengis.feature.type.Name;
 import org.springframework.context.ApplicationContext;
 
 /**
  * Main class used to handle Execute requests
  *
  * @author Lucas Reed, Refractions Research Inc
  */
 public class Execute {
     WPSInfo             wps;
     GeoServerInfo gs;
     ApplicationContext  context;
 
     public Execute(WPSInfo wps, GeoServerInfo gs, ApplicationContext context) {
         this.wps      = wps;
         this.gs = gs;
         this.context = context;
     }
 
     /**
      * Main method for performing decoding, execution, and response
      *
      * @param object
      * @param output
      * @throws IllegalArgumentException
      */
     public ExecuteResponseType run(ExecuteType request) {
         //note the current time
         Date started = Calendar.getInstance().getTime();
         
         //load the process factory
         CodeType ct = request.getIdentifier();
         Name processName = Ows11Util.name(ct);
         ProcessFactory pf = Processors.createProcessFactory(processName);
         if ( pf == null ) {
             throw new WPSException( "No such process: " + processName );
         }
         
         //parse the inputs for the request
         Map<String, Object> inputs = new HashMap();
         
         for ( Iterator i = request.getDataInputs().getInput().iterator(); i.hasNext(); ) {
             InputType input = (InputType) i.next();
             
             //locate the parameter for this request
             Parameter p = pf.getParameterInfo(processName).get( input.getIdentifier().getValue() );
             if ( p == null ) {
                 throw new WPSException( "No such parameter: " + input.getIdentifier().getValue() );
             }
             
             //find the ppio
             ProcessParameterIO ppio = ProcessParameterIO.find( p, context );
             if ( ppio == null ) {
                 throw new WPSException( "Unable to decode input: " + input.getIdentifier().getValue() );
             }
             
             //read the data
             Object decoded = null;
             if ( input.getReference() != null ) {
                 //this is a reference
                 InputReferenceType ref = input.getReference();
                 
                 //grab the location and method
                 String href = ref.getHref();
                 MethodType meth = ref.getMethod() != null ? ref.getMethod() : MethodType.GET_LITERAL; 
                 
                 //handle get vs post
                 if ( meth == MethodType.POST_LITERAL ) {
                     //post, handle the body
                 }
                 else {
                     //get, parse kvp
                 }
             }
             else {
                 //actual data, figure out which type 
                 DataType data = input.getData();
                
                 if ( data.getLiteralData() != null ) {
                     LiteralDataType literal = data.getLiteralData();
                     decoded = ((LiteralPPIO)ppio).decode( literal.getValue() );
                 }
                 else if ( data.getComplexData() != null ) {
                     ComplexDataType complex = data.getComplexData();
                     decoded = complex.getData().get( 0 );
                     try {
                         decoded = ((ComplexPPIO)ppio).decode( decoded );
                     } 
                     catch (Exception e) {
                         throw new WPSException( "Unable to decode input: " + input.getIdentifier().getValue() );
                     }
                 }
                 
             }
             
             //decode the input
             inputs.put( p.key, decoded );
         }
         
         //execute the process
         Map<String,Object> result = null;
         Throwable error = null;
         try {
             Process p = pf.create(processName);
             result = p.execute( inputs, null );    
         }
         catch( Throwable t ) {
             //save the error to report back
             error = t;
         }
         
         //build the response
         Wps10Factory f = Wps10Factory.eINSTANCE;
         ExecuteResponseType response = f.createExecuteResponseType();
         response.setLang("en");
        String proxifiedBaseUrl = RequestUtils.proxifiedBaseURL(request.getBaseUrl(), gs.getProxyBaseUrl());
        response.setServiceInstance(proxifiedBaseUrl + "ows?");
        
         //process 
         final ProcessBriefType process = f.createProcessBriefType();
         response.setProcess( process );
         process.setIdentifier(ct);
         process.setProcessVersion(pf.getVersion(processName));
         process.setTitle( Ows11Util.languageString( pf.getTitle(processName).toString() ) );
         process.setAbstract( Ows11Util.languageString( pf.getDescription(processName).toString() ) );
        
         //status
         response.setStatus( f.createStatusType() );
         response.getStatus().setCreationTime( Converters.convert( started, XMLGregorianCalendar.class ));
         
         if ( error != null ) {
             ProcessFailedType failure = f.createProcessFailedType();
             response.getStatus().setProcessFailed( failure );
             
             failure.setExceptionReport( Ows11Util.exceptionReport( new ServiceException( error ), wps.getGeoServer().getGlobal().isVerboseExceptions()) );
         }
         else {
             response.getStatus().setProcessSucceeded( "Process succeeded.");
         }
       
         //inputs
         response.setDataInputs( f.createDataInputsType1() );
         for ( Iterator i = request.getDataInputs().getInput().iterator(); i.hasNext(); ) {
             InputType input = (InputType) i.next();
             response.getDataInputs().getInput().add( EMFUtils.clone( input, f, true ) );
         }
         
         //output definitions
         OutputDefinitionsType outputs = f.createOutputDefinitionsType();
         response.setOutputDefinitions( outputs );
         
         Map<String,Parameter<?>> outs = pf.getResultInfo(processName, null);
         Map<String,ProcessParameterIO> ppios = new HashMap();
         
         for ( String key : result.keySet() ) {
             Parameter p = pf.getResultInfo(processName, null).get( key );
             if ( p == null ) {
                 throw new WPSException( "No such output: " + key );
             }
             
             //find the ppio
             ProcessParameterIO ppio = ProcessParameterIO.find( p, context );
             if ( ppio == null ) {
                 throw new WPSException( "Unable to encode output: " + p.key );
             }
             ppios.put( p.key, ppio );
             
             DocumentOutputDefinitionType output = f.createDocumentOutputDefinitionType();
             outputs.getOutput().add( output );
             
             output.setIdentifier( Ows11Util.code( p.key ) );
             if ( ppio instanceof ComplexPPIO ) {
                 output.setMimeType( ((ComplexPPIO) ppio).getMimeType() );
             }
             
             //TODO: encoding + schema
         }
         
         //process outputs
         ProcessOutputsType1 processOutputs = f.createProcessOutputsType1();
         response.setProcessOutputs( processOutputs );
         
         for ( String key : result.keySet() ) {
             OutputDataType output = f.createOutputDataType();
             output.setIdentifier(Ows11Util.code(key));
             output.setTitle(Ows11Util.languageString(pf.getResultInfo(processName, null).get( key ).description));
             processOutputs.getOutput().add( output );
             
             final Object o = result.get( key );
             ProcessParameterIO ppio = ppios.get( key );
             
             if ( ppio instanceof ReferencePPIO ) {
                 //encode as a reference
                 OutputReferenceType ref = f.createOutputReferenceType();
                 output.setReference( ref );
                 
                 //TODO: mime type
                 ref.setHref( ((ReferencePPIO) ppio).encode(o).toString() );
             }
             else {
                 //encode as data
                 DataType data = f.createDataType();
                 output.setData( data );
            
                 if ( ppio instanceof LiteralPPIO ) {
                     LiteralDataType literal = f.createLiteralDataType();
                     data.setLiteralData( literal );
                     
                     literal.setValue( ((LiteralPPIO) ppio).encode( o ) );
                 }
                 else if ( ppio instanceof ComplexPPIO ) {
                     ComplexDataType complex = f.createComplexDataType();
                     data.setComplexData( complex );
                     
                     ComplexPPIO cppio = (ComplexPPIO) ppio;
                     complex.setMimeType( cppio.getMimeType() );
                     
                     if ( cppio instanceof XMLPPIO ) {
                         //encode directly
                         complex.getData().add( 
                             new ComplexDataEncoderDelegate( (XMLPPIO) cppio, o )
                         );
                     }
                     else {
                        //TODO: handle other content types, perhaps as CDATA
                     }
                 }
             }
         }
         
         return response;
     }
 
 //    @SuppressWarnings("unchecked")
 //    private void outputs(Map<String, Object> outputs) {
 //        ProcessFactory      pf             = this.executor.getProcessFactory();
 //        ProcessOutputsType1 processOutputs = WpsFactory.eINSTANCE.createProcessOutputsType1();
 //
 //        for(String outputName : outputs.keySet()) {
 //            Parameter<?> param = (pf.getResultInfo(null)).get(outputName);
 //
 //            OutputDataType output = WpsFactory.eINSTANCE.createOutputDataType();
 //
 //            CodeType identifier = Ows11Factory.eINSTANCE.createCodeType();
 //            identifier.setValue(param.key);
 //            output.setIdentifier(identifier);
 //
 //            LanguageStringType title = Ows11Factory.eINSTANCE.createLanguageStringType();
 //            title.setValue(param.title.toString(this.locale));
 //            output.setTitle(title);
 //
 //            DataType data = WpsFactory.eINSTANCE.createDataType();
 //
 //            // Determine the output type, Complex or Literal
 //            Object outputParam = outputs.get(outputName);
 //
 //            final Transmuter transmuter = this.dataTransformer.getDefaultTransmuter(outputParam.getClass());
 //
 //            // Create appropriate response document node for given type
 //            if (transmuter instanceof ComplexTransmuter) {
 //                data.setComplexData(this.complexData((ComplexTransmuter)transmuter, outputParam));
 //            } else {
 //                if (transmuter instanceof LiteralTransmuter) {
 //                    data.setLiteralData(this.literalData((LiteralTransmuter)transmuter, outputParam));
 //                } else {
 //                    throw new WPSException("NoApplicableCode", "Could not find transmuter for output " + outputName);
 //                }
 //            }
 //
 //            output.setData(data);
 //
 //            processOutputs.getOutput().add(output);
 //        }
 //
 //        this.response.setProcessOutputs(processOutputs);
 //    }
 //
 //    private LiteralDataType literalData(LiteralTransmuter transmuter, Object value) {
 //        LiteralDataType data = WpsFactory.eINSTANCE.createLiteralDataType();
 //
 //        data.setValue(   transmuter.encode(value));
 //        data.setDataType(transmuter.getEncodedType());
 //
 //        return data;
 //    }
 //
 //    @SuppressWarnings("unchecked")
 //    private ComplexDataType complexData(ComplexTransmuter transmuter, Object value) {
 //        ComplexDataType data = WpsFactory.eINSTANCE.createComplexDataType();
 //
 //        data.setSchema(  transmuter.getSchema(this.request.getBaseUrl()));
 //        data.setMimeType(transmuter.getMimeType());
 //        data.getData().add(value);
 //
 //        return data;
 //    }
 //
 //    private void processBrief() {
 //        ProcessFactory     pf    = this.executor.getProcessFactory();
 //        ProcessBriefType   brief = WpsFactory.eINSTANCE.createProcessBriefType();
 //        LanguageStringType title = Ows11Factory.eINSTANCE.createLanguageStringType();
 //
 //        brief.setProcessVersion(pf.getVersion());
 //        brief.setIdentifier(this.request.getIdentifier());
 //        title.setValue(pf.getTitle().toString(this.locale));
 //        brief.setTitle(title);
 //
 //        this.response.setProcess(brief);
 //    }
 //
 //    private void status() {
 //        StatusType status = WpsFactory.eINSTANCE.createStatusType();
 //
 //        status.setProcessSucceeded("Process completed successfully.");
 //
 //        XMLGregorianCalendar calendar;
 //
 //        try {
 //            calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(
 //                new GregorianCalendar(TimeZone.getTimeZone("UTC")));
 //        } catch(Exception e) {
 //            throw new WPSException("NoApplicableCode", e.getMessage());
 //        }
 //
 //        status.setCreationTime(calendar);
 //
 //        this.response.setStatus(status);
 //    }
 }
