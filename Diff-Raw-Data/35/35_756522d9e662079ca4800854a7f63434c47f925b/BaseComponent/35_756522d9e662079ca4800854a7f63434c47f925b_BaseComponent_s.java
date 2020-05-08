 /**
  * 
  */
 package com.isesalud.support.components;
 
 import java.io.Serializable;
 
 import org.jboss.solder.logging.Logger;
 
 import com.isesalud.support.JSFUtil;
 import com.isesalud.support.exceptions.BaseException;
 
 /**
  * @author Jesus Espinoza Hernandez
  *
  */
 @SuppressWarnings("serial")
 public abstract class BaseComponent implements Serializable
 {
 	protected final Logger log = Logger.getLogger(getClass());
 
 	/**
 	 */
 	public BaseComponent()
 	{
 		try
 		{
 			init();
 			initComponents();
 			initProperties();
 			clear();
 		}
 		catch (Throwable t)
 		{
 			JSFUtil.fatal();
 			log.fatal(t.getMessage(), t);
 		}
 	}
 
 	/**
 	 */
 	protected void init() throws BaseException
 	{
 	}
 
 	/**
 	 */
 	protected void initComponents() throws BaseException
 	{
 	}
 
 	/**
 	 */
 	protected void initProperties() throws BaseException
 	{
 	}
 
 	/**
 	 */
 	public void clear() throws BaseException
 	{
 	}
 }
