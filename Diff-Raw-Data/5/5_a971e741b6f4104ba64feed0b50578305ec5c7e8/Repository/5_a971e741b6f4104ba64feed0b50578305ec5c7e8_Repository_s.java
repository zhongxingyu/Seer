 package no.runsafe.framework.database;
 
 import org.joda.time.DateTime;
 
 import java.sql.Timestamp;
 
 /**
  * Base class for database repositories, providing methods for converting SQL Timestamp to and from Joda DateTime
  */
 public abstract class Repository implements ISchemaChanges
 {
 	/**
 	 * Converts an SQL {@link Timestamp} into a joda time {@link DateTime}
 	 *
 	 * @param timestamp The SQL timestamp object
 	 * @return A joda time DateTime
 	 */
	protected static DateTime convert(Timestamp timestamp)
 	{
 		if (timestamp == null)
 			return null;
 		return new DateTime(timestamp);
 	}
 
 	/**
	 * Converts an SQL {@link Timestamp} into a joda time {@link DateTime}
 	 *
 	 * @param dateTime A joda time DateTime
 	 * @return The SQL timestamp object
 	 */
 	protected static Timestamp convert(DateTime dateTime)
 	{
 		if (dateTime == null)
 			return null;
 		return new Timestamp(dateTime.getMillis());
 	}
 }
