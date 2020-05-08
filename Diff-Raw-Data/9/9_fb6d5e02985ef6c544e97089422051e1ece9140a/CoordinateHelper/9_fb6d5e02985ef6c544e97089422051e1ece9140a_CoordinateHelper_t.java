 package org.hhu.c2c.openlr.io;
 
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_ABSOLUTE_ANGULAR_MEASUREMENT;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_RELATIVE_COORDINATE;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.RELATIVE_FORMAT_INT_MULTIPLIER;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.RESOLUTION_PARAMETER;
 
 import org.hhu.c2c.openlr.geo.Coordinate;
 import org.hhu.c2c.openlr.l10n.Messages;
 import org.hhu.c2c.openlr.util.ByteArrayFiFo;
 import org.hhu.c2c.openlr.util.LocationReferenceException;
 
 /**
  * <b>CoordinateUtils</b> is a collection of small helper functions for
  * converting coorinates into and out of their respective representations
  * 
  * @author Oliver Schrenk <oliver.schrenk@uni-duesseldorf.de>
  * 
  */
 public class CoordinateHelper {
 
 	/**
 	 * Returns a byte array representation of a relative coordinate
 	 * 
 	 * @param current
 	 *            the current coordinate
 	 * @param previous
 	 *            the previous coordinate
 	 * @return a byte array representation of a relative coordinate
 	 */
 	protected static byte[] getByteArrayRepresentation(
 			final Coordinate current, final Coordinate previous) {
 		int longitude = Math.round((RELATIVE_FORMAT_INT_MULTIPLIER * (current
 				.getLongitude() - previous.getLongitude())));
 		int latitude = Math.round((RELATIVE_FORMAT_INT_MULTIPLIER * (current
 				.getLatitude() - previous.getLatitude())));
 
 		byte[] relativeCoordinate = new byte[NUMBER_OF_BYTES_FOR_RELATIVE_COORDINATE];
 		relativeCoordinate[0] = (byte) (longitude >> 8);
 		relativeCoordinate[1] = (byte) (longitude >> 0);
 		relativeCoordinate[2] = (byte) (latitude >> 8);
 		relativeCoordinate[3] = (byte) (latitude >> 0);
 		return relativeCoordinate;
 	}
 
 	/**
 	 * Returns a longitude or latitude encoded as a float as a byte array
 	 * representation
 	 * 
 	 * @param angularMeasurement
 	 *            longitude or latitude in degree
 	 * @return a byte array representation of a longitude or latitude
 	 */
 	protected static byte[] getByteArrayRepresentation(
 			final float angularMeasurement) {
 		byte[] longOrLat = new byte[NUMBER_OF_BYTES_FOR_ABSOLUTE_ANGULAR_MEASUREMENT];
 		long longitude = CoordinateHelper
 				.getLongRepresentation(angularMeasurement);
 		longOrLat[0] = (byte) (longitude >> 16);
 		longOrLat[1] = (byte) (longitude >> 8);
 		longOrLat[2] = (byte) (longitude >> 0);
 		return longOrLat;
 	}
 
 	/**
 	 * Returns the absolute float value of a longitude (or latitude) from a the
 	 * integer encoded value of a longitude (or latitude) and the absolute
 	 * longitude (or latitude as a float from the previous point)
 	 * 
 	 * @param relative
 	 *            the integer representation of a relative longitude (or
 	 *            latitude) from the current point
 	 * @param previousPointDegree
 	 *            the float representation of the absolute longitude (or
 	 *            latitude) from the previous point
 	 * @return the absolute value of the longitude (or latitude) of the current
 	 *         point
 	 */
 	protected static float getDegreeFromRelative(final int relative,
 			final float previousPointDegree) {
 		return relative / RELATIVE_FORMAT_INT_MULTIPLIER + previousPointDegree;
 	}
 
 	/**
 	 * Returns the float representation of longitude or latitude encoded as a
 	 * long value
 	 * 
 	 * @param degree
 	 *            the long value of the longitude (or latitude)
 	 * @return the float representation
 	 */
 	static float getFloatRepresentation(final long degree) {
 		return (float) ((degree - Math.signum(degree) / 2) * 360 * (Math.pow(2,
 				-RESOLUTION_PARAMETER)));
 	}
 
 	/**
 	 * Returns the long representation of a degree, used for converting
 	 * coordinate values like longitude or latitude
 	 * 
 	 * @param degree
 	 *            a longitude or latitude
 	 * @return the long representation of the longitude (or latitude)
 	 */
 	private static long getLongRepresentation(final float degree) {
 		return (long) (Math.signum(degree) / 2 + (degree / 360)
 				* (Math.pow(2, RESOLUTION_PARAMETER)));
 	}
 
 	/**
 	 * Returns the integer representation of a relative coordinate
 	 * 
 	 * @param coordinate
 	 *            a relative longitude or relative latitude encoded as two bytes
 	 * @return the integer representation of a relative coordinate
 	 */
 	protected static int getRelativeCoordinateIntValue(final byte[] coordinate) {
 		int relativeCoordinate = coordinate[0] << 8;
 		return relativeCoordinate | coordinate[1];
 	}
 
 	/**
 	 * Creates a new {@link Coordinate} from the given byte array
 	 * 
 	 * @param coordinate
 	 *            a byte array of the length of six
 	 * @return a new coordinate
 	 */
 	protected static Coordinate getCoordinate(final byte[] coordinate) {
 		ByteArrayFiFo fifo = new ByteArrayFiFo(coordinate);
 		try {
 			return Coordinate.newCoordinate(
 					getFloatRepresentation(getAbsoluteAngularMeasurement(fifo
 							.pop(NUMBER_OF_BYTES_FOR_ABSOLUTE_ANGULAR_MEASUREMENT))),
 					getFloatRepresentation(getAbsoluteAngularMeasurement(fifo
 							.pop(NUMBER_OF_BYTES_FOR_ABSOLUTE_ANGULAR_MEASUREMENT))));
 		} catch (LocationReferenceException e) {
 			// this exception should never be thrown as the decoder should
 			// always return a valid value, as the binary encoded bytes can only
 			// represent valid data
			throw new RuntimeException(Messages.getString("General.Error.GURU_MEDITATION_FAILURE")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns the long value of the angular measurement
 	 * 
 	 * @param angularMeasurement
 	 *            the angular measurement
 	 * 
 	 * @return the longitude value
 	 */
 	private static long getAbsoluteAngularMeasurement(
 			final byte[] angularMeasurement) {
 		return (angularMeasurement[0] << 16) + (angularMeasurement[1] << 8)
 				+ (angularMeasurement[2]);
 	}
 
 }
