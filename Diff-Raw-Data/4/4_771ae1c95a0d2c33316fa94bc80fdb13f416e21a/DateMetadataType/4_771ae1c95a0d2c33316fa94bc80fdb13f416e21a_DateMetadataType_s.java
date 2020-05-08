 package uk.ac.ox.oucs.content.metadata.model;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * @author Colin Hebert
  */
 public class DateMetadataType extends MetadataType<Date>
 {
 	private Date minimumDateTime;
 	private Date maximumDateTime;
 	private boolean date;
 	private boolean time;
 	private boolean defaultToday;
	private final DateTimeConverter converter = new DateTimeConverter();
 
 	public Date getMinimumDateTime()
 	{
 		return minimumDateTime;
 	}
 
 	public void setMinimumDateTime(Date minimumDateTime)
 	{
 		this.minimumDateTime = minimumDateTime;
 	}
 
 	public Date getMaximumDateTime()
 	{
 		return maximumDateTime;
 	}
 
 	public void setMaximumDateTime(Date maximumDateTime)
 	{
 		this.maximumDateTime = maximumDateTime;
 	}
 
 	public boolean isDate()
 	{
 		return date;
 	}
 
 	public void setDate(boolean date)
 	{
 		this.date = date;
 	}
 
 	public boolean isTime()
 	{
 		return time;
 	}
 
 	public void setTime(boolean time)
 	{
 		this.time = time;
 	}
 
 	public boolean isDefaultToday()
 	{
 		return defaultToday;
 	}
 
 	public void setDefaultToday(boolean defaultToday)
 	{
 		this.defaultToday = defaultToday;
 	}
 
 	@Override
 	/**
 	 * {@inheritDoc}
 	 *
 	 * Returns today's date if the default has been set to today
 	 */
 	public Date getDefaultValue()
 	{
 		return defaultToday ? new Date() : super.getDefaultValue();
 	}
 
 	@Override
 	public MetadataRenderer getRenderer()
 	{
 		return null;	//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public MetadataConverter<Date> getConverter()
 	{
		return converter;
 	}
 
 	@Override
 	public MetadataValidator<Date> getValidator()
 	{
 		return new DateMetadataValidator();
 	}
 
 
 	private final class DateMetadataValidator implements MetadataValidator<Date>
 	{
 		public boolean validate(Date metadataValue)
 		{
 			if (metadataValue == null)
 				return isRequired();
 			if (minimumDateTime != null && metadataValue.before(minimumDateTime))
 				return false;
 			if (maximumDateTime != null && metadataValue.after(maximumDateTime))
 				return false;
 
 			return true;
 		}
 	}
 
 	private final class DateTimeConverter implements MetadataConverter<Date>
 	{
 		public String toString(Date metadataValue)
 		{
 			return (metadataValue != null) ? DateFormat.getDateInstance().format(metadataValue) : null;
 		}
 
 		public Date fromString(String stringValue)
 		{
 			try
 			{
 				return (stringValue != null && !stringValue.isEmpty()) ? DateFormat.getDateInstance().parse(stringValue) : null;
 			}
 			catch (ParseException e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 
 		public Map<String, ?> toProperties(Date metadataValue)
 		{
 			String stringValue = toString(metadataValue);
 			return (stringValue != null) ? Collections.singletonMap(getUniqueName(), stringValue) : Collections.<String, Object>emptyMap();
 		}
 
 		public Date fromProperties(Map<String, ?> properties)
 		{
 			return fromString((String) properties.get(getUniqueName()));
 		}
 
 		public Date fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
 		{
 			return fromString((String) parameters.get(getUniqueName() + parameterSuffix));
 		}
 	}
 }
