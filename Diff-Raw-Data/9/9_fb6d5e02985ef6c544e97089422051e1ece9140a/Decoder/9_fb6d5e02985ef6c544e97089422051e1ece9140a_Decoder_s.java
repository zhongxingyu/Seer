 package org.hhu.c2c.openlr.io;
 
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.AREA_FLAG_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.ATTRIBUTE_FLAG_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.FORM_OF_WAY_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.FRC_BITSHIFT;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.FUNCTIONAL_ROAD_CLASS_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.LFRCNP_BITSHIFT;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.MINIMUM_NUMBER_OF_BYTES;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.MINIMUM_NUMBER_OF_BYTES_FOR_LAST_LRP;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NEGATIVE_OFFSET_FLAG_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_ABSOLUTE_COORDINATE;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_ABSOLUTE_LRP;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_RELATIVE_ANGULAR_MEASUREMENT;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.NUMBER_OF_BYTES_FOR_RELATIVE_LRP;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.POSITIVE_OFFSET_FLAG_BITMASK;
 import static org.hhu.c2c.openlr.io.PhysicalDataFormat.VERSION_NUMBER_BITMASK;
 
 import org.hhu.c2c.openlr.core.Bearing;
 import org.hhu.c2c.openlr.core.Distance;
 import org.hhu.c2c.openlr.core.FormOfWay;
 import org.hhu.c2c.openlr.core.FunctionalRoadClass;
 import org.hhu.c2c.openlr.core.LocationReference;
 import org.hhu.c2c.openlr.core.LocationReferenceBuilder;
 import org.hhu.c2c.openlr.core.LocationReferencePoint;
 import org.hhu.c2c.openlr.core.LocationReferencePointBuilder;
 import org.hhu.c2c.openlr.geo.Coordinate;
 import org.hhu.c2c.openlr.l10n.Messages;
 import org.hhu.c2c.openlr.util.ByteArrayFiFo;
 import org.hhu.c2c.openlr.util.LocationReferenceException;
 
 /**
  * Used for unmarshalling a byte representation of a location reference.
  * 
  * @author Oliver Schrenk <oliver.schrenk@uni-duesseldorf.de>
  * 
  */
 public class Decoder {
 
 	/**
 	 * Converts the given byte array into a location reference
 	 * 
 	 * @param bytes
 	 *            the byte array representing a location reference
 	 * @return a location reference
 	 * @throws LocationReferenceException
 	 *             if the location reference trying wasn't valid
 	 * @throws IllegalArgumentException
 	 *             if the byte array is has either less or more bytes than
 	 *             required
 	 */
 	public LocationReference decode(final byte[] bytes)
 			throws LocationReferenceException {
 		if (bytes.length < MINIMUM_NUMBER_OF_BYTES) {
 			throw new LocationReferenceException(
 					Messages
 							.getString(
 									"Decoder.Exception.MINIMUM_NUMBER_OF_BYTES", MINIMUM_NUMBER_OF_BYTES)); //$NON-NLS-1$
 		}
 
 		ByteArrayFiFo fifo = new ByteArrayFiFo(bytes);
 
 		LocationReferenceBuilder lrb = new LocationReferenceBuilder();
 
 		// read header byte
 		lrb
 				.setAreaFlag((fifo.peek() & AREA_FLAG_BITMASK) == AREA_FLAG_BITMASK ? true
 						: false);
 		lrb
 				.setAttributeFlag(((fifo.peek() & ATTRIBUTE_FLAG_BITMASK) == ATTRIBUTE_FLAG_BITMASK ? true
 						: false));
 		lrb.setVersion((byte) (fifo.pop() & VERSION_NUMBER_BITMASK));
 
 		// get the first point, its an absolute one
 		LocationReferencePoint point = getAbsoluteLRP(fifo
 				.pop(NUMBER_OF_BYTES_FOR_ABSOLUTE_LRP));
 		lrb.addLocationReferencePoint(point);
 
 		// get following, relative ones, if there are any
 		while (fifo.capacity() >= NUMBER_OF_BYTES_FOR_RELATIVE_LRP
 				+ MINIMUM_NUMBER_OF_BYTES_FOR_LAST_LRP) {
 			point = getLRPfromRelative(point.getCoordinate(), fifo
 					.pop(NUMBER_OF_BYTES_FOR_RELATIVE_LRP));
 			lrb.addLocationReferencePoint(point);
 		}
 
 		// we have to prepare the last one very carefully as the last attribute
 		// has info about the last point and the complete location reference
 		LocationReferencePointBuilder lrpb = new LocationReferencePointBuilder();
 		lrpb.setCoordinate(getCoordinate(point.getCoordinate(), fifo));
 		lrpb.setFrc(FunctionalRoadClass.getFunctionalRoadClass((byte) ((fifo
 				.peek() >> FRC_BITSHIFT) & FUNCTIONAL_ROAD_CLASS_BITMASK)));
 		lrpb.setFow(FormOfWay
 				.getFormOfWay((byte) (fifo.pop() & FORM_OF_WAY_BITMASK)));
 
 		boolean positiveOffsetFlag = (fifo.peek() & POSITIVE_OFFSET_FLAG_BITMASK) == POSITIVE_OFFSET_FLAG_BITMASK;
 		boolean negativeOffsetFlag = (fifo.peek() & NEGATIVE_OFFSET_FLAG_BITMASK) == NEGATIVE_OFFSET_FLAG_BITMASK;
 
 		lrpb.setBearing(Bearing.newBearing(fifo.pop()));
 		lrb.addLocationReferencePoint(lrpb.build());
 
 		// check if poffF is set
 		if (positiveOffsetFlag) {
 			// there should be one or two byte left
 			if (fifo.capacity() > 0) {
 				lrb.setPositiveOffset(Distance.newDistance(fifo.pop()));
 			} else {
 				throw new LocationReferenceException(
 						Messages
 								.getString("Decoder.Exception.POSITIVE_OFFSET_NOT_FOUND")); //$NON-NLS-1$
 			}
 		}
 
 		// if noffF is set, the byte for the offset must be there
 		if (negativeOffsetFlag) {
 			// there should only be one byte left
 			if (fifo.capacity() > 0) {
 				lrb.setNegativeOffset(Distance.newDistance(fifo.pop()));
 			} else {
 				throw new LocationReferenceException(
 						Messages
 								.getString("Decoder.Exception.NEGATIVE_OFFSET_NOT_FOUND")); //$NON-NLS-1$
 			}
 		}
 
 		// if there are some bytes left, something went wrong
 		if (fifo.capacity() != 0) {
 			throw new LocationReferenceException(Messages
 					.getString("Decoder.Exception.BYTES_NOT_EXHAUSTED")); //$NON-NLS-1$
 		}
 
 		return lrb.build();
 	}
 
 	/**
 	 * Returns a location reference point from a given byte array of 9 bytes
 	 * representing a location reference point with an absolute coordinate and
 	 * three attribute bytes
 	 * 
 	 * @param point
 	 *            the byte representation of a location reference point with an
 	 *            absolute coordinate
 	 * @return the location reference point
 	 * @throws LocationReferenceException
 	 *             if the location reference point trying to build couldn't be
 	 *             validated
 	 */
 	private LocationReferencePoint getAbsoluteLRP(final byte[] point)
 			throws LocationReferenceException {
 		LocationReferencePointBuilder lrpb = new LocationReferencePointBuilder();
 		ByteArrayFiFo fifo = new ByteArrayFiFo(point);
 
 		lrpb.setCoordinate(CoordinateHelper.getCoordinate(fifo
 				.pop(NUMBER_OF_BYTES_FOR_ABSOLUTE_COORDINATE)));
 		lrpb.setFrc(FunctionalRoadClass.getFunctionalRoadClass((byte) ((fifo
 				.peek() >> FRC_BITSHIFT) & FUNCTIONAL_ROAD_CLASS_BITMASK)));
 		lrpb.setFow(FormOfWay
 				.getFormOfWay((byte) (fifo.pop() & FORM_OF_WAY_BITMASK)));
 		lrpb.setLfrcnp(FunctionalRoadClass.getFunctionalRoadClass((byte) ((fifo
 				.peek() >> LFRCNP_BITSHIFT) & FUNCTIONAL_ROAD_CLASS_BITMASK)));
 		lrpb.setBearing(Bearing.newBearing(fifo.pop()));
 		lrpb.setDnp(Distance.newDistance(fifo.pop()));
 		return lrpb.build();
 	}
 
 	/**
 	 * Returns a new absolute {@link Coordinate} from a given previous
 	 * coordinate, and a byte representation of the current relative location
 	 * reference point.
 	 * 
 	 * @param previous
 	 *            the coordinate of the previous location reference point
 	 * @param fifo
 	 *            the {@link ByteArrayFiFo} holding the current location
 	 *            reference point
 	 * @return the absolute coordinate of the current location reference point
 	 */
 	private Coordinate getCoordinate(final Coordinate previous,
 			final ByteArrayFiFo fifo) {
 		try {
 			return Coordinate
 					.newCoordinate(
 							CoordinateHelper
 									.getDegreeFromRelative(
 											CoordinateHelper
 													.getRelativeCoordinateIntValue(fifo
 															.pop(NUMBER_OF_BYTES_FOR_RELATIVE_ANGULAR_MEASUREMENT)),
 											previous.getLongitude()),
 							CoordinateHelper
 									.getDegreeFromRelative(
 											CoordinateHelper
 													.getRelativeCoordinateIntValue(fifo
 															.pop(NUMBER_OF_BYTES_FOR_RELATIVE_ANGULAR_MEASUREMENT)),
 											previous.getLatitude()));
 		} catch (LocationReferenceException e) {
 			// this exception should never be thrown as the decoder should
 			// always return a valid value, as the binary encoded bytes can only
 			// represent valid data
			throw new RuntimeException(Messages.getString("FATAL_ERROR")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns a {@link LocationReferencePoint} from a given previous
 	 * coordinate, and a byte representation of the current relative location
 	 * reference point.
 	 * 
 	 * @param previous
 	 *            The absolute coordinate from the previous location reference
 	 *            point
 	 * @param currentPoint
 	 *            the byte array representation of the current relative location
 	 *            reference point
 	 * @return the current location reference point with absolute values
 	 * @throws LocationReferenceException
 	 *             if the location reference point trying to build couldn't be
 	 *             validated
 	 */
 	private LocationReferencePoint getLRPfromRelative(
 			final Coordinate previous, final byte[] currentPoint)
 			throws LocationReferenceException {
 		LocationReferencePointBuilder lrpb = new LocationReferencePointBuilder();
 		ByteArrayFiFo fifo = new ByteArrayFiFo(currentPoint);
 
 		lrpb.setCoordinate(getCoordinate(previous, fifo));
 		lrpb.setFrc(FunctionalRoadClass.getFunctionalRoadClass((byte) ((fifo
 				.peek() >> FRC_BITSHIFT) & FUNCTIONAL_ROAD_CLASS_BITMASK)));
 		lrpb.setFow(FormOfWay
 				.getFormOfWay((byte) (fifo.pop() & FORM_OF_WAY_BITMASK)));
 		lrpb.setLfrcnp(FunctionalRoadClass.getFunctionalRoadClass((byte) ((fifo
 				.peek() >> LFRCNP_BITSHIFT) & FUNCTIONAL_ROAD_CLASS_BITMASK)));
 		lrpb.setBearing(Bearing.newBearing(fifo.pop()));
 		lrpb.setDnp(Distance.newDistance(fifo.pop()));
 
 		return lrpb.build();
 	}
 }
