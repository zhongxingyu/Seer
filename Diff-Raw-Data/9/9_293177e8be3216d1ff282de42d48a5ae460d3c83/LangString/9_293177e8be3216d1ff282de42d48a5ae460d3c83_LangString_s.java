 package org.gesis.rdf;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.MapKeyColumn;
 
 import org.gesis.ddi.util.LangStringKeyBuilder;
 import org.gesis.ddi.util.LocaleValueEntry;
 import org.gesis.ddi.util.Locales;
 import org.gesis.persistence.PersistableResource;
 
 /**
  * Represents a multilingual string. Stores a multilingual value only with
  * correspondence to a Locale. It may represent an arbitrary number of values
  * and their corresponding Locales. <br>
  * Please note that, this class provides some "default" Locales, which are
  * Locale.GERMANY, Locale.UNITED_KINGDOM, and Locale.FRANCE. Other Locales may
  * be added without paying attention to the default Locales, but are stored in a
  * OneToMany-relationship in a separate data structure.
  *
  * @author matthaeus
  *
  */
 @Entity
 public class LangString extends PersistableResource
 {
 
 	@Column( unique = true, name = "message_key" )
 	private String messageKey;
 
 	@Column
 	@Lob
 	private String de = null;
 
 	@Column
 	@Lob
 	private String en = null;
 
 	@Column
 	@Lob
 	private String fr = null;
 
 	@ElementCollection( fetch = FetchType.LAZY )
	@MapKeyColumn( name = "language", length = 2 )
	@Column( name = "value" )
	@CollectionTable( name = "Languages_Values", joinColumns = @JoinColumn( name = "langString_id" ) )
	@Lob
 	private final Map<String, String> values = new HashMap<String, String>();
 
 	public LangString()
 	{
 
 	}
 
 	public LangString( final Locale locale, final String value )
 	{
 		setValue( locale, value );
 	}
 
 	public LangString( final Map<Locale, String> values )
 	{
 		setValues( values );
 	}
 
 	public String getMessageKey()
 	{
 		return this.messageKey;
 	}
 
 	public LangString setMessageKey( final String messageKey )
 	{
 		this.messageKey = messageKey;
 		return this;
 	}
 
 	public String getDe()
 	{
 		return this.de;
 	}
 
 	public void setDe( final String de )
 	{
 		this.de = de;
 	}
 
 	public String getEn()
 	{
 		return this.en;
 	}
 
 	public void setEn( final String en )
 	{
 		this.en = en;
 	}
 
 	public String getFr()
 	{
 		return this.fr;
 	}
 
 	public void setFr( final String fr )
 	{
 		this.fr = fr;
 	}
 
 	/**
 	 * Returns the corresponding value to the passed <i>locale</i>.
 	 *
 	 * @param locale
 	 * @return
 	 */
 	public String getValue( final Locale locale )
 	{
 		if ( locale == null )
 			return null;
 
 		// first try to match from own fields
 		if ( Locales.isEqual( Locales.UNITED_KINGDOM, locale ) )
 			return this.en;
 		else if ( Locales.isEqual( Locales.GERMANY, locale ) )
 			return this.de;
 		else if ( Locales.isEqual( Locales.FRANCE, locale ) )
 			return this.fr;
 
 		if ( this.values == null || this.values.isEmpty() )
 			return null;
 
 		String langString = LangStringKeyBuilder.buildFrom( locale );
 
 		return this.values.get( langString );
 	}
 
 	/**
 	 * Adds a <i>value</i> to the corresponding <i>locale</i> to this
 	 * LangString-object.
 	 *
 	 * @param locale
 	 * @param value
 	 * @return
 	 */
 	public LangString setValue( final Locale locale, final String value )
 	{
 		if ( locale == null )
 			return this;
 
 		if ( value == null )
 			return this;
 
 		// first try to match to own fields
 		if ( Locales.isEqual( Locales.GERMANY, locale ) )
 		{
 			this.de = value;
 			return this;
 		} else if ( Locales.isEqual( Locales.UNITED_KINGDOM, locale ) )
 		{
 			this.en = value;
 			return this;
 
 		} else if ( Locales.isEqual( Locales.FRANCE, locale ) )
 		{
 			this.fr = value;
 			return this;
 		}
 
 		// build the Entry-object
 		Entry<String, String> entry = buildEntryFrom( locale, value );
 
 		if ( entry.getKey() == null || entry.getValue() == null )
 			return this;
 
 		this.values.put( entry.getKey(), entry.getValue() );
 
 		return this;
 	}
 
 	/**
 	 * Adds all values to their corresponding Locale in <i>values</i> to this
 	 * LangString-object.
 	 *
 	 * @param values
 	 * @return
 	 */
 	public LangString setValues( final Map<Locale, String> values )
 	{
 		if ( values == null )
 			return this;
 
 		for ( Locale locale : values.keySet() )
 			setValue( locale, values.get( locale ) );
 
 		return this;
 	}
 
 	/**
 	 * Returns the map of <i>all</i> values, including the default Locales,
 	 * added to this LangString-object.
 	 *
 	 * @param all
 	 * @return
 	 */
 	public Map<String, String> getValues()
 	{
 		Map<String, String> ret = new HashMap<String, String>( this.values );
 
 		if ( this.de != null )
 			ret.put( LangStringKeyBuilder.buildFrom( Locales.GERMANY ), this.de );
 
 		if ( this.en != null )
 			ret.put( LangStringKeyBuilder.buildFrom( Locales.UNITED_KINGDOM ), this.en );
 
 		if ( this.fr != null )
 			ret.put( LangStringKeyBuilder.buildFrom( Locales.FRANCE ), this.fr );
 
 		return ret;
 	}
 
 	/**
 	 * Returns an java.util.Map.Entry object from <i>locale</i> and
 	 * <i>value</i>. For instance, if<br>
 	 * <br>
 	 * <code>Locale locale = Locales.GERMANY;</code>,<br>
 	 * <br>
 	 * <code>buildEntryFrom(locale, "some value")</code> <br>
 	 * <br>
 	 * returns and Entry-object with key = "de_DE" and value = "some value".
 	 *
 	 * @param locale
 	 * @param value
 	 * @return
 	 */
 	protected static Entry<String, String> buildEntryFrom( final Locale locale, final String value )
 	{
 		if ( locale.getCountry() == null || locale.getCountry() == "" )
 			return null;
 
 		if ( locale.getLanguage() == null || locale.getLanguage() == "" )
 			return null;
 
 		// build the key
 		final String langString = LangStringKeyBuilder.buildFrom( locale );
 
 		if ( langString == null )
 			return null;
 
 		return new LocaleValueEntry( langString, value );
 	}
 
 	@Override
 	public String toString()
 	{
 		return "LangString [key=" + this.messageKey + ", de_DE=" + this.de + ", en_UK=" + this.en + ", fr_FR=" + this.fr + "]";
 	}
 
 	/**
 	 * Factory-method for a LangString object with UK-locale.
 	 *
 	 * @param value
 	 * @return
 	 */
 	public static LangString createUKLangString( final String value )
 	{
 		return createLangString( Locales.UNITED_KINGDOM, value );
 	}
 
 	/**
 	 * Factory-method for a LangString object with DE-locale.
 	 *
 	 * @param value
 	 * @return
 	 */
 	public static LangString createDELangString( final String value )
 	{
 		return createLangString( Locales.GERMANY, value );
 	}
 
 	/**
 	 * Factory-method for a LangString object with FR-locale.
 	 *
 	 * @param value
 	 * @return
 	 */
 	public static LangString createFRLangString( final String value )
 	{
 		return createLangString( Locales.FRANCE, value );
 	}
 
 	/**
 	 * Factory-method for a LangString object with the provided locale.
 	 *
 	 * @param locale
 	 * @param value
 	 * @return
 	 */
 	public static LangString createLangString( final Locale locale, final String value )
 	{
 		if ( locale == null || value == null )
 			return null;
 
 		return new LangString( locale, value );
 	}
 
 	/**
 	 * Factory-method for a LangString object. The locale is tried to be
 	 * resolved from the country code.
 	 *
 	 * @param country
 	 * @param value
 	 * @return
 	 */
 	public static LangString createLangString( final String country, final String value )
 	{
 		Locale loc = Locales.getLocale( country );
 
 		return createLangString( loc, value );
 	}
 
 }
