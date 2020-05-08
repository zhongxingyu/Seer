 package com.socialcomputing.wps.server.plandictionary;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import com.socialcomputing.wps.client.applet.ColorX;
 import com.socialcomputing.wps.client.applet.Env;
 import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
 import com.socialcomputing.wps.server.planDictionnary.connectors.utils.ConnectorHelper;
 import com.socialcomputing.wps.server.planDictionnary.connectors.utils.NameValuePair;
 import com.socialcomputing.wps.server.plandictionary.connectors.iEntityConnector;
 import com.socialcomputing.wps.server.webservices.RequestingClassifyId;
 
 /**
   * Model is determined by connected user segmentation */
 
 public class Model implements java.io.Serializable
 {
 	static final long serialVersionUID = 7094708832902600585L;
 
 	public static final int MAX_SELECTION  = 32;
 
 	public String m_Name = null;
 	public String m_Description = null;
 
 	// Param�tres de g�n�ration du plan
 	public boolean m_DisplayEntities = true;
 	public boolean m_DisplayEmptyLinks = true;
 	public boolean m_DisplayFakeLinks = true;
 
 	public		int         m_flags         = 0;
 	public  	ColorX      m_inCol         = null;
 	public  	ColorX      m_outCol        = null;
 	public  	ColorX      m_filterCol        = null;
 
 	// A voir
 	public String m_Type;
 
 	// S�lections d'entit�s et/ou d'attributs (WPSSelection)
 	public WPSSelection m_EntitiesSelections[] = new WPSSelection[ Model.MAX_SELECTION];
 	public WPSSelection m_AttributesSelections[] = new WPSSelection[ Model.MAX_SELECTION];
 
 	private transient iEntityConnector m_EntitiesConnector = null;
 
 	// Matching RequestingEntity / Entity Swatch (normal, ref, norm cur, ref cur)
 	public ClassifierMapper m_EntityMapper[] = new ClassifierMapper[2];
 
 	// Matching RequestingEntity / Attribute Swatch (normal, ref, norm cur, ref cur)
 	public ClassifierMapper m_AttributeMapper[] = new ClassifierMapper[4];
 
 	// Matching RequestingEntity / Link Swatch (normal, ref)
 	public ClassifierMapper m_LinkMapper[] = new ClassifierMapper[4];
 
 	// Matching RequestingEntity / Cluster Swatch (normal, ref)
 	public ClassifierMapper m_ClusterMapper[] = new ClassifierMapper[2];
 
 	// Matching RequestingEntity / Reference Swatch (normal, cur)
 	public ClassifierMapper m_ReferenceMapper[] = new ClassifierMapper[2];
 
 	// Matching RequestingEntity / Advertising Swatch (normal, cur)
 	public ClassifierMapper m_AdvertisingMapper[] = new ClassifierMapper[2];
 
 	static public Model readObject( org.jdom.Element element) throws org.jdom.JDOMException
 	{
 		Model model = new Model( element.getAttributeValue( "name"));
 		model.m_Description = element.getChildText( "comment");
 		model.m_DisplayEntities = element.getAttributeValue( "display-entities").equalsIgnoreCase( "yes");
 		if( element.getAttributeValue( "display-empty-links") != null)
 			model.m_DisplayEmptyLinks = element.getAttributeValue( "display-empty-links").equalsIgnoreCase( "yes");
 		else
 		    model.m_DisplayEmptyLinks = false;
 		if( element.getAttributeValue( "display-fake-links") != null)
 			model.m_DisplayFakeLinks = element.getAttributeValue( "display-fake-links").equalsIgnoreCase( "yes");
         else
             model.m_DisplayFakeLinks = false;
 		if( element.getAttributeValue( "in-color") != null)
 			model.m_inCol = Model.readColorX(element.getAttributeValue( "in-color"));
         else
             model.m_inCol = new ColorX( 0xFFFFFF);
 		if( element.getAttributeValue( "out-color") != null)
 			model.m_outCol = Model.readColorX(element.getAttributeValue( "out-color"));
         else
             model.m_outCol = new ColorX( 0xFFFFFF);
 		if( element.getAttributeValue( "filter-color") != null)
 			model.m_filterCol = Model.readColorX(element.getAttributeValue( "filter-color"));
 		else
 		    model.m_filterCol = new ColorX( 0xFFFFFF);
 		{   // Swatchs
 			org.jdom.Element props = element.getChild( "swatch-segmentation");
 			org.jdom.Element subprops = props.getChild( "attribute-swatch");
 			// TODO (ON) Simplifier les swatch ICI : on peut n'avoir qu'un swatch
 			model.m_AttributeMapper[0] = ClassifierMapper.readObject( subprops.getChild( "norm-swatch"));
 			model.m_AttributeMapper[1] = ClassifierMapper.readObject( subprops.getChild( "ref-swatch"));
 			model.m_AttributeMapper[2] = ClassifierMapper.readObject( subprops.getChild( "active-norm-swatch"));
 			model.m_AttributeMapper[3] = ClassifierMapper.readObject( subprops.getChild( "active-ref-swatch"));
 			subprops = props.getChild( "link-swatch");
 			model.m_LinkMapper[0] = ClassifierMapper.readObject( subprops.getChild( "norm-swatch"));
 			model.m_LinkMapper[1] = ClassifierMapper.readObject( subprops.getChild( "ref-swatch"));
 			org.jdom.Element activeLinkElm   = subprops.getChild( "active-norm-swatch");
 			if ( activeLinkElm != null )
 			{
 				model.m_LinkMapper[2] = ClassifierMapper.readObject( activeLinkElm );
 				model.m_LinkMapper[3] = ClassifierMapper.readObject( subprops.getChild( "active-ref-swatch"));
 			}
 		}
 		{   // S�lections
 			java.util.List lst = element.getChildren( "selection-swatch");
 			int size = lst.size();
 			if( size >= 32)
 				throw new org.jdom.JDOMException( "Display Profile '" + model.m_Name + "' : too many selections!");
 			for( int i = 0; i < size; ++i)
 			{
 				org.jdom.Element node = ( org.jdom.Element)lst.get( i);
 				//String name = node.getAttributeValue( "name");
 				model.m_AttributesSelections[ i] = WPSSelection.readObject( node, i);
 			}
 		}
 		return model;
 	}
 
 	static protected ColorX readColorX( String value) {
 	    try {
 	        return new ColorX( Integer.parseInt( value.startsWith("#") ? value.substring(1): value, 16));
 	    }
 	    catch (Exception e) {
         }
 	    return new ColorX( value);
 	}
 	
 	// Constructor
 	public Model( String name)
 	{
 		m_Name = name;
 		for( int i = 0; i < 2; ++i)
 			 m_EntityMapper[ i] = new ClassifierMapper();
 		for( int i = 0; i < 4; ++i)
 			 m_AttributeMapper[ i] = new ClassifierMapper();
 		for( int i = 0; i < 4; ++i)
 			 m_LinkMapper[ i] = new ClassifierMapper();
 		for( int i = 0; i < 2; ++i)
 			 m_ClusterMapper[ i] = new ClassifierMapper();
 		for( int i = 0; i < 2; ++i)
 			 m_ReferenceMapper[ i] = new ClassifierMapper();
 		for( int i = 0; i < 2; ++i)
 			 m_AdvertisingMapper[ i] = new ClassifierMapper();
 
 		m_inCol         = new ColorX( 127, 175, 31 );
 		m_outCol        = new ColorX( 0, 0, 0 );
 	}
 
 	public void setEntitiesConnector( iEntityConnector connector)
 	{
 		m_EntitiesConnector = connector;
 	}
 
 	// Param�tres de g�n�ration du plan
 	public String getEntitySwatch( RequestingClassifyId classifyId, String entityId, int swatchType) throws JMIException
 	{   // La segmentation sur entityId n'est pas trait�e
 		return m_EntityMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 
 	public String getAttributeSwatch( RequestingClassifyId classifyId, int swatchType) throws JMIException
 	{
 		return m_AttributeMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 	public String getLinkSwatch( RequestingClassifyId classifyId, int swatchType) throws JMIException
 	{
 		return m_LinkMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 	public String getClusterSwatch( RequestingClassifyId classifyId, int swatchType) throws JMIException
 	{
 		return m_ClusterMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 	public String getReferenceSwatch( RequestingClassifyId classifyId, int swatchType) throws JMIException
 	{
 		return m_ReferenceMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 	public String getAdvertisingSwatch( RequestingClassifyId classifyId, int swatchType) throws JMIException
 	{
 		return m_AdvertisingMapper[ swatchType].getAssociatedName( m_EntitiesConnector, classifyId);
 	}
 
 	public void initClientEnv( WPSDictionary dico, Hashtable<String, Object> wpsparams, Env env)
 	{
 	/*
 	public static final int AUDIO_BIT       = 0x01;
 	public static final int JSCRIPT_BIT     = 0x04;
 	public static final int EXTERN_SEL      = 0x20;
 	*/
 		env.m_flags = 0;
 		if( m_DisplayEntities)
 			env.m_flags = Env.GROUP_BIT;
 
 		env.m_inCol     = m_inCol;
 		env.m_outCol    = m_outCol;
 		env.m_filterCol = m_filterCol;
 		env.m_transfo   = null;
 
 		// Propriétés globales du plan
 		for( NameValuePair value : dico.m_EnvProperties) {
			String name = value.getName().startsWith( "$") ? value.getName() : "$" + value.getName();
			if( !wpsparams.containsKey(name)) 
 			    env.m_props.put( name, ConnectorHelper.ReplaceParameter(value, wpsparams));
 		}
 
 		// S�lection Attributes
 		env.m_selections    = new Hashtable();
 		WPSSelection        selection;
 		int                 i, n        = m_AttributesSelections.length;
 		for ( i = 0; i < n; i ++ )
 		{
 			selection = m_AttributesSelections[i];
 			if ( selection != null )
 			{
 				env.m_selections.put( selection.m_SelectionName, new Integer( i ));
 			}
 		}
 	}
 
 	// Check classifiers integrity
 	public void checkIntegrity( String m, iEntityConnector entities, String attributes) throws org.jdom.JDOMException, JMIException
 	{
 		for( int i = 0; i < Model.MAX_SELECTION; ++i)
 		{
 			if( m_EntitiesSelections[i] != null)
 				if( !m_EntitiesSelections[i].m_FreeSelection && entities.getSelection( m_EntitiesSelections[i].m_SelectionRef) == null)
 					  throw new org.jdom.JDOMException( m + " : Unknown Entities Selection '" + m_EntitiesSelections[i].m_SelectionRef + "'");
 		}
 		for( int i = 0; i < Model.MAX_SELECTION; ++i)
 		{
 			if( m_AttributesSelections[i] != null)
 				if( !m_AttributesSelections[i].m_FreeSelection && entities.getProfile( attributes).getSelection( m_AttributesSelections[i].m_SelectionRef) == null)
 					  throw new org.jdom.JDOMException( m + " : Unknown Attributes Selection '" + m_AttributesSelections[i].m_SelectionRef + "'");
 		}
 	}
 
 }
