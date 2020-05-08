 package dk.dbc.opensearch.common.fedora;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.ArrayList;
 
 import org.apache.axis.encoding.Base64;
 import org.apache.log4j.Logger;
 
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.ValidationException;
 
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.xsd.ObjectProperties;
 import dk.dbc.opensearch.xsd.Property;
 import dk.dbc.opensearch.xsd.PropertyType;
 import dk.dbc.opensearch.xsd.ContentDigest;
 import dk.dbc.opensearch.xsd.Datastream;
 import dk.dbc.opensearch.xsd.DatastreamVersion;
 import dk.dbc.opensearch.xsd.DatastreamVersionTypeChoice;
 import dk.dbc.opensearch.xsd.DigitalObject;
 import dk.dbc.opensearch.xsd.types.DatastreamTypeCONTROL_GROUPType;
 import dk.dbc.opensearch.xsd.types.DigitalObjectTypeVERSIONType;
 import dk.dbc.opensearch.xsd.types.PropertyTypeNAMEType;
 import dk.dbc.opensearch.xsd.types.StateType;
import dk.dbc.opensearch.xsd.types.ContentDigestTypeTYPEType;
 
 public class FedoraTools {
 
 	Logger log = Logger.getLogger("FedoraHandler");
 
 	public static byte[] constructFoxml(CargoContainer cargo, String nextPid,
 			String label) throws IOException, MarshalException,
 			ValidationException, ParseException {
 		Date now = new Date(System.currentTimeMillis());
 		return constructFoxml(cargo, nextPid, label, now);
 	}
 
 	public static byte[] constructFoxml(CargoContainer cargo, String nextPid,
 			                            String label, Date now) throws IOException, MarshalException,
 			                                                           ValidationException, ParseException {
 		ObjectProperties op = new ObjectProperties();
 
 		Property pState = new Property();
 		pState.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE);
 		pState.setVALUE("Active");
 
 		Property pLabel = new Property();
 		pLabel.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL);
 		pLabel.setVALUE(label);
 
 		PropertyType pOwner = new Property();
 		pOwner.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNERID);
 		/** \todo: set correct value for owner of the Digital Object*/
 		pOwner.setVALUE( "user" );
 
 		// createdDate
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
 		String timeNow = dateFormat.format(now);
 		Property pCreatedDate = new Property();
 		pCreatedDate.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CREATEDDATE);
 		pCreatedDate.setVALUE(timeNow);
 
 		// lastModifiedDate
 		Property pLastModifiedDate = new Property();
 		pLastModifiedDate.setNAME(PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_VIEW_LASTMODIFIEDDATE);
 		pLastModifiedDate.setVALUE(timeNow);
 
 		Property[] props = new Property[] { (Property) pState, (Property) pLabel, (Property) pOwner, 
 				                            (Property) pCreatedDate, (Property) pLastModifiedDate };
 		op.setProperty(props);
 
 		DigitalObject dot = new DigitalObject();
 		dot.setObjectProperties(op);
 		dot.setVERSION(DigitalObjectTypeVERSIONType.VALUE_0);
                  dot.setPID( nextPid ); //
 
 
 		ArrayList<Datastream> dsArray = new ArrayList<Datastream>();
 
 		for (CargoObject co : cargo.getData())
         {
 			dsArray.add(constructDatastream(co, dateFormat, timeNow));
 		}
                 
                 Datastream[] ds =new Datastream[dsArray.size()];
                 dsArray.toArray( ds );
                 dot.setDatastream( ds );
 
 		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
 		java.io.OutputStreamWriter outW = new java.io.OutputStreamWriter(out);
 		Marshaller m = new Marshaller(outW); // IOException
 		m.marshal(dot); // throws MarshallException, ValidationException
 
 		byte[] ret = out.toByteArray();
 		return ret;
 	}
 
 	/**
 	 * constructDatastream creates a Datastream object on the basis of the
 	 * information and data found in the Pair of CargoObjectInfo and List<Byte>.
 	 * 
 	 * @return A datastream suitable for ingestion into the DigitalObject
 	 */
 	private static Datastream constructDatastream(CargoObject co, SimpleDateFormat dateFormat, String timeNow) throws java.text.ParseException, IOException 
 	{
 		int srcLen = co.getContentLength();
 		byte[] ba = co.getBytes();
 
 		/** \todo: VERSIONABLE should be configurable in some way */
 		boolean versionable = false;
 
 		/**
 		 * \todo: if the datastream is external, dsLocation should be
 		 * configurable
 		 */
 
 		/** \todo: is this an adequate check? */
 		DatastreamTypeCONTROL_GROUPType controlGroup = null;
 		if (co.getMimeType() == "text/xml") 
 			controlGroup = DatastreamTypeCONTROL_GROUPType.X;
 		else 
 			controlGroup = DatastreamTypeCONTROL_GROUPType.M;
 
 		// datastreamElement
 		Datastream dataStreamElement = new Datastream();
 
 		/** \todo: CONTROL_GROUP should be configurable in some way */
 		dataStreamElement.setCONTROL_GROUP( controlGroup );
 
 		//dataStreamElement.setID( co.getFormat() );
 		//dataStreamElement.setID( co.getDataStreamName( "test" ) );
 		dataStreamElement.setID( co.getDataStreamName().getName() );
 		/**
 		 * \todo: State type defaults to active. Client should interact with
 		 * datastream after this method if it wants something else
 		 */
 		dataStreamElement.setSTATE( StateType.A );
 		dataStreamElement.setVERSIONABLE( versionable );
 
 		// datastreamVersionElement
 		String itemId_version = co.getDataStreamName().getName() + ".0";
 
 		DatastreamVersion dataStreamVersionElement = new DatastreamVersion();
 
 		dataStreamVersionElement.setCREATED(dateFormat.parse( timeNow ) );
 
 		dataStreamVersionElement.setID(itemId_version);
 
 		DatastreamVersionTypeChoice dVersTypeChoice = new DatastreamVersionTypeChoice();
 
 		ContentDigest binaryContent = new ContentDigest();
 
 		dVersTypeChoice.setBinaryContent(ba);
 
 		dataStreamVersionElement.setDatastreamVersionTypeChoice(dVersTypeChoice);
 
 		String mimeLabel = String.format("%s [%s]", co.getFormat(), co.getMimeType());
 		dataStreamVersionElement.setLABEL(mimeLabel);
 		String mimeFormatted = String.format("%s [%s]", co.getFormat(), co.getMimeType());
 		dataStreamVersionElement.setMIMETYPE( mimeFormatted );
 
 		long lengthFormatted = (long) srcLen;
 
 		dataStreamVersionElement.setSIZE( lengthFormatted );
 
 		binaryContent.setDIGEST( Base64.encode( ba ) );
                binaryContent.setTYPE( ContentDigestTypeTYPEType.DISABLED );

 
 		dataStreamVersionElement.setContentDigest( binaryContent );
 		DatastreamVersion[] dsvArray = new DatastreamVersion[] { dataStreamVersionElement };
 		dataStreamElement.setDatastreamVersion( dsvArray );
 
 		return dataStreamElement;
 	}
 }
