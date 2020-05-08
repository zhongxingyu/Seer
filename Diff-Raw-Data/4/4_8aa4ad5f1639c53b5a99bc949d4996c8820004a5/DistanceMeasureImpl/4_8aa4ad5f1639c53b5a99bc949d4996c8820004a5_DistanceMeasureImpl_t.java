 package cz.cuni.mff.odcleanstore.conflictresolution.impl;
 
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.openrdf.model.BNode;
 import org.openrdf.model.Literal;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.vocabulary.XMLSchema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cz.cuni.mff.odcleanstore.conflictresolution.DistanceMeasure;
 import cz.cuni.mff.odcleanstore.conflictresolution.resolution.utils.EnumLiteralType;
 import cz.cuni.mff.odcleanstore.conflictresolution.resolution.utils.LevenshteinDistance;
 import cz.cuni.mff.odcleanstore.conflictresolution.resolution.utils.ResolutionFunctionUtils;
 import cz.cuni.mff.odcleanstore.core.ODCSUtils;
 
 /**
  * The default implementation of a distance metric between Node instances.
  * In all methods value 1 means maximum distance, value 0 means identity.
  *
  * @author Jan Michelfeit
  */
 public class DistanceMeasureImpl implements DistanceMeasure {
     private static final Logger LOG = LoggerFactory.getLogger(DistanceMeasureImpl.class);
 
     /** Minimum distance between two {@link Value Values} indicating equal nodes. */
     private static final double MIN_DISTANCE = 0;
 
     /** Maximum distance between two {@link Value Values}. */
     private static final double MAX_DISTANCE = 1;
     
     /** Distance value for URI resources with different URIs. */
     private static final double DIFFERENT_RESOURCE_DISTANCE = MAX_DISTANCE;
 
     /** Distance of {@link Value Values} of different types. */
     private static final double DIFFERENT_TYPE_DISTANCE = MAX_DISTANCE;
 
     /** Distance of {@link Value Values} when an error (e.g. a parse error) occurs. */
     private static final double ERROR_DISTANCE = MAX_DISTANCE;
 
     /** Number of seconds in a day. */
     private static final int SECONDS_IN_DAY = (int) (ODCSUtils.DAY_HOURS * ODCSUtils.TIME_UNIT_60 * ODCSUtils.TIME_UNIT_60);
 
     /** Default minimum difference between two dates to consider them completely different  in seconds.. */ 
     private static final long DEFAULT_MAX_DATE_DIFFERENCE = 366 * SECONDS_IN_DAY;
 
     /** Minimum difference between two dates to consider them completely different  in seconds. */
     private final Long maxDateDifference;
     
     /**
      * Creates a new instance.
      */
     public DistanceMeasureImpl() {
         this(DEFAULT_MAX_DATE_DIFFERENCE);
     }
     
     /**
      * Creates a new instance.
      * @param maxDateDifference minimum difference between two dates to consider them completely different in seconds 
      */
     public DistanceMeasureImpl(Long maxDateDifference) {
         this.maxDateDifference = maxDateDifference;
     }
 
     /**
      * {@inheritDoc}
      * @param value1 {@inheritDoc }
      * @param value2 {@inheritDoc }
      * @return {@inheritDoc }
      */
     @Override
     public double distance(Value value1, Value value2) {
        if (value1 instanceof URI) {
             return resourceDistance((URI) value1, (URI) value2);
         } else if (value1 instanceof BNode) {
             return blankNodeDistance((BNode) value1, (BNode) value2);
         } else if (value1 instanceof Literal) {
             return literalDistance((Literal) value1, (Literal) value2);
         } else {
             LOG.warn("Distance cannot be measured on Nodes of type {}", value1.getClass().getSimpleName());
             return ERROR_DISTANCE;
         }
     }
 
     /**
      * Calculates a distance metric between two Node_Literal instances.
      * @see #distance(Value, Value)
      * @param primaryNode first of the compared Nodes; this Node may be considered "referential",
      *        i.e. we measure distance from this value
      * @param comparedNode second of the compared Nodes
      * @return a number from interval [0,1]
      *
      */
     private double literalDistance(Literal primaryNode, Literal comparedNode) {
         EnumLiteralType primaryLiteralType = ResolutionFunctionUtils.getLiteralType(primaryNode);
         EnumLiteralType comparedLiteralType = ResolutionFunctionUtils.getLiteralType(comparedNode);
         EnumLiteralType comparisonType = primaryLiteralType == comparedLiteralType
                 ? primaryLiteralType
                 : EnumLiteralType.OTHER;
         Literal primaryLiteral = (Literal) primaryNode;
         Literal comparedLiteral = (Literal) comparedNode;
 
         double result;
         switch (comparisonType) {
         case NUMERIC:
             double primaryValueDouble = ResolutionFunctionUtils.convertToDoubleSilent(primaryLiteral);
             if (Double.isNaN(primaryValueDouble)) {
                 LOG.warn("Numeric literal {} is malformed.", primaryLiteral);
                 return ERROR_DISTANCE;
             }
             double comparedValueDouble = ResolutionFunctionUtils.convertToDoubleSilent(comparedLiteral);
             if (Double.isNaN(comparedValueDouble)) {
                 LOG.warn("Numeric literal {} is malformed.", comparedLiteral);
                 return ERROR_DISTANCE;
             }
             result = numericDistance(primaryValueDouble, comparedValueDouble);
             break;
         case TIME:
             result = timeDistance(primaryLiteral, comparedLiteral);
             break;
         case DATE_TIME:
             result = dateDistance(primaryLiteral, comparedLiteral);
             break;
         case BOOLEAN:
             boolean primaryValueBool = ResolutionFunctionUtils.convertToBoolean(primaryLiteral);
             boolean comparedValueBool = ResolutionFunctionUtils.convertToBoolean(comparedLiteral);
             result = primaryValueBool == comparedValueBool ? MIN_DISTANCE : MAX_DISTANCE;
             break;
         case STRING:
         case OTHER:
             result = LevenshteinDistance.normalizedLevenshteinDistance(
                     primaryLiteral.stringValue(),
                     comparedLiteral.stringValue());
             break;
         default:
             LOG.error("Unhandled literal type for comparison {}.", comparisonType);
             throw new RuntimeException("Unhandled literal type for comparison");
         }
 
         /*LOG.debug("Distance between numeric literals {} and {}: {}",
                 new Object[] { primaryNode, comparedNode, result });*/
         return result;
     }
 
     /**
      * Calculates a distance metric between two numbers.
      * @see #distance(Value, Value)
      * @param primaryValue first of the compared values; this v may be considered "referential",
      *        i.e. we measure distance from this value
      * @param comparedValue second of the compared values
      * @return a number from interval [0,1]
      */
     private double numericDistance(double primaryValue, double comparedValue) {
         double result = primaryValue - comparedValue;
         double average = (primaryValue + comparedValue) / 2;
         if (average != 0) {
             // TODO: document change to normalization with average
             // "Normalize" result to primaryValue;
             // for zero leave as is - the important thing is order of the value
             // which for zero is close enough to 1
             result /= average;
         }
         result = Math.abs(result);
         // result /= SQRT_OF_TWO;
         return Math.min(result, MAX_DISTANCE);
     }
 
     /**
      * Calculates a distance metric between two time values.
      * The maximum distance is reached with difference of one day.
      * If value types are incompatible or conversion fails, {@value #ERROR_DISTANCE} is returned.
      * @see #distance(Value, Value)
      * @param primaryValue first of the compared values
      * @param comparedValue second of the compared values
      * @return a number from interval [0,1]
      */
     private double timeDistance(Literal primaryValue, Literal comparedValue) {
         if (XMLSchema.TIME.equals(primaryValue.getDatatype()) && XMLSchema.TIME.equals(comparedValue.getDatatype())) {
             XMLGregorianCalendar primaryValueTime = ResolutionFunctionUtils.convertToCalendarSilent(primaryValue);
             XMLGregorianCalendar comparedValueTime = ResolutionFunctionUtils.convertToCalendarSilent(comparedValue);
             if (primaryValueTime == null || comparedValueTime == null) {
                 LOG.warn("Time value '{}' or '{}' is malformed.", primaryValue, comparedValue);
                 return ERROR_DISTANCE;
             }
             double difference = Math.abs(getTimePart(primaryValueTime) - getTimePart(comparedValueTime));
             double result = difference / SECONDS_IN_DAY;
             assert MIN_DISTANCE <= result && result <= MAX_DISTANCE;
             return result;
         } else {
             LOG.warn("Time literals '{}' and '{}' have incompatible types.", primaryValue, comparedValue);
             return ERROR_DISTANCE;
         }
     }
     
     private int getTimePart(XMLGregorianCalendar calendar) {
         int result = 0;
         if (calendar.getHour() != DatatypeConstants.FIELD_UNDEFINED) {
             result += calendar.getHour();
         }
         result *= ODCSUtils.TIME_UNIT_60_INT;
         if (calendar.getMinute() != DatatypeConstants.FIELD_UNDEFINED) {
             result += calendar.getMinute();
         }
         result *= ODCSUtils.TIME_UNIT_60_INT;
         if (calendar.getSecond() != DatatypeConstants.FIELD_UNDEFINED) {
             result += calendar.getSecond();
         }
         return result;
     }
 
     /**
      * Calculates a distance metric between two dates.
      * The maximum distance is reached with {@value #MAX_DATE_DIFFERENCE}.
      * If value types are incompatible or conversion fails, {@value #ERROR_DISTANCE} is returned.
      * @see #distance(Value, Value)
      * @param primaryValue first of the compared values
      * @param comparedValue second of the compared values
      * @return a number from interval [0,1]
      */
     private double dateDistance(Literal primaryValue, Literal comparedValue) {
         // CHECKSTYLE:OFF
         if ((XMLSchema.DATETIME.equals(primaryValue.getDatatype()) || XMLSchema.DATE.equals(primaryValue.getDatatype()))
                 && (XMLSchema.DATETIME.equals(comparedValue.getDatatype()) || XMLSchema.DATE.equals(comparedValue.getDatatype()))) {
             // CHECKSTYLE:ON
             XMLGregorianCalendar primaryValueTime = ResolutionFunctionUtils.convertToCalendarSilent(primaryValue);
             XMLGregorianCalendar comparedValueTime = ResolutionFunctionUtils.convertToCalendarSilent(comparedValue);
             if (primaryValueTime == null || comparedValueTime == null) {
                 LOG.warn("Date value '{}' or '{}' is malformed.", primaryValue, comparedValue);
                 return ERROR_DISTANCE;
             }
             double differenceInSeconds = Math.abs(primaryValueTime.toGregorianCalendar().getTimeInMillis()
                     - comparedValueTime.toGregorianCalendar().getTimeInMillis()) / ODCSUtils.MILLISECONDS;
             double result = (MAX_DISTANCE - MIN_DISTANCE)
                     * differenceInSeconds / maxDateDifference;
             result = Math.min(result, MAX_DISTANCE);
             assert MIN_DISTANCE <= result && result <= MAX_DISTANCE;
             return result;
         } else {
             LOG.warn("Date literals '{}' and '{}' have incompatible types.", primaryValue, comparedValue);
             return ERROR_DISTANCE;
         }
     }
 
     /**
      * Calculates a distance metric between two Node_URI instances.
      * @see #distance(Value, Value)
      * @param primaryValue first of the compared Nodes; this Node may be considered "referential",
      *        i.e. we measure distance from this value
      * @param comparedValue second of the compared Nodes
      * @return a number from interval [0,1]
      *
      */
     private double resourceDistance(URI primaryValue, URI comparedValue) {
         if (primaryValue.equals(comparedValue)) {
             return MIN_DISTANCE;
         } else {
             return DIFFERENT_RESOURCE_DISTANCE;
         }
     }
 
     /**
      * Calculates a distance metric between two Node_URI instances.
      * @see #distance(Value, Value)
      * @param primaryValue first of the compared Nodes; this Node may be considered "referential",
      *        i.e. we measure distance from this value
      * @param comparedValue second of the compared Nodes
      * @return a number from interval [0,1]
      */
     private double blankNodeDistance(BNode primaryValue, BNode comparedValue) {
         if (primaryValue.equals(comparedValue)) {
             return MIN_DISTANCE;
         } else {
             return DIFFERENT_RESOURCE_DISTANCE;
         }
     }
 }
