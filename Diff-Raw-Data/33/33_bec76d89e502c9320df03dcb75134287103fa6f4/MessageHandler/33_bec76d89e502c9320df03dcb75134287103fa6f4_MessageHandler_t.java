 package at.ac.tuwien.swag.util;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 public abstract class MessageHandler implements MessageListener {
 	
 	protected void initialize( ConnectionFactory factory ) throws JMSException {
 		connection = factory.createConnection();
 		session    = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
 
 		connection.start();
 	}
 	
 	@Override
 	public final void onMessage( Message msg ) {
 		try {
			Object payload = getPayload( msg );
 			
			Method handler = getClass().getMethod( "handle", payload.getClass() );
 			
			this.replyTo = msg.getJMSReplyTo();
 
 			handler.invoke( this, payload );
 		} catch ( SecurityException e ) {
 			e.printStackTrace();
 		} catch ( NoSuchMethodException e ) {
 			e.printStackTrace();
 		} catch ( IllegalArgumentException e ) {
 			e.printStackTrace();
 		} catch ( IllegalAccessException e ) {
 			e.printStackTrace();
 		} catch ( InvocationTargetException e ) {
 			e.printStackTrace();
 		} catch ( JMSException e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void reply( String msg ) throws JMSException {
 		reply( session.createTextMessage( msg ) );
 	}
 
 	protected void reply( Serializable msg ) throws JMSException {
 		reply( session.createObjectMessage( msg ) );
 	}
 	
 	protected void reply( Message msg ) throws JMSException {
 		MessageProducer producer = session.createProducer( replyTo );
 
 		producer.send( msg );
 		
 		producer.close();		
 	}
 	
 	protected Connection  connection() { return connection; }
 	protected Session     session()    { return session;    }
 	protected Destination replyTo()    { return replyTo;    }
 	
 	//***** PRIVATE PARTS
 	
 	private Object getPayload( Message msg ) throws JMSException {
 		if ( msg instanceof ObjectMessage ) {
 			return ((ObjectMessage) msg).getObject();
 		} else if ( msg instanceof TextMessage ) {
 			return ((TextMessage) msg).getText();
 		} else {
 			return msg;
 		}
 	}
 	
 	private Connection  connection;
 	private Session     session;
 	private Destination replyTo;
 }
