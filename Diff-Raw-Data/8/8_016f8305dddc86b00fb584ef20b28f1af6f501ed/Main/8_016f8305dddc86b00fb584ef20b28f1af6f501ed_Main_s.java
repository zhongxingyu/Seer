 package com.test.components;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 
 import com.test.entity.DataContainer;
 import com.test.entity.DataStore;
 import com.webobjects.appserver.WOActionResults;
 import com.webobjects.appserver.WOContext;
 import com.webobjects.eoaccess.EOUtilities;
 import com.webobjects.eocontrol.EOEditingContext;
 import com.webobjects.eocontrol.EOObjectStore;
 import com.webobjects.foundation.NSData;
 
 import er.extensions.components.ERXComponent;
 import er.extensions.eof.ERXEC;
 import er.extensions.eof.ERXObjectStoreCoordinator;
 
 public class Main extends ERXComponent {
 	private EOObjectStore _osc = new ERXObjectStoreCoordinator(true);
 	public Main(WOContext context) {
 		super(context);
 
 		// The memory leak doesn't happen unless there are multiple OSCs registered with 
 		// ERXObjectStoreCoordinatorSynchronizer._coordinators. Using an OSC (performing a lookup, etc,
 		// as below) causes the OSC to be added to the _coordinators array.
 		EOEditingContext ec = ERXEC.newEditingContext(_osc);
 		ec.lock();
 		try {
			DataContainer dc = (DataContainer) EOUtilities.objectMatchingKeyAndValue( ec, "DataContainer", "id", 1 );
 		}
 		finally {
 			ec.unlock();
 			ec.dispose();
 			ec = null;
 		}
 	}
 
 	public WOActionResults createDataStore() throws IOException, MessagingException {
 		File emailFile = new File("Resources/largeEmail.eml");
 		javax.mail.Message message = convertEmlToMessage( emailFile );
 		
 		EOObjectStore osc = new ERXObjectStoreCoordinator(true);
 		EOEditingContext ec = ERXEC.newEditingContext(osc);
 		ec.lock();
 		try {
 			DataContainer container = (DataContainer) EOUtilities.createAndInsertInstance(ec, DataContainer.class.getSimpleName());
			ec.insertObject(container);
 			
 			DataStore dataStore = (DataStore) EOUtilities.createAndInsertInstance(ec, DataStore.class.getSimpleName());
			ec.insertObject(dataStore);
 			dataStore.setDataContainer(container);
 			
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 			message.writeTo( byteStream );
 			NSData rawEmail = new NSData( byteStream.toByteArray() );
 			dataStore.setData(rawEmail);
 			
 			ec.saveChanges();
 		}
 		finally {
 			ec.unlock();
 			ec.dispose();
 			osc.dispose();
 			ec = null;
 			osc = null;
 		}
 		return null;
 	}
 
 	private static Session _dummyMailSession;
 	private static Session dummyMailSession() {
 		if ( _dummyMailSession == null ) {
 			Properties props = new Properties();
 			props.put("mail.host", "smtp.dummyDomain.com");
 			props.put("mail.transport.protocol", "smtp");
 			_dummyMailSession = Session.getDefaultInstance(props, null);
 		}
 		return _dummyMailSession;
 	}
 
 	private Message convertEmlToMessage(File emailFile) {
 		FileInputStream fileInputStream;
 		try {
 			fileInputStream = new FileInputStream( emailFile );
 			return new MimeMessage( dummyMailSession(), fileInputStream );
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
