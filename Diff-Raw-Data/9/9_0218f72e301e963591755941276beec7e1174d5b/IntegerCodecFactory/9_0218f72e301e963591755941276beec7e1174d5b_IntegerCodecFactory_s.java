 package org.hbird.transport.payloadcodec.codecparameters;
 
 import org.hbird.transport.payloadcodec.codecparameters.number.TwosComplementIntegerCodecParameter;
 import org.hbird.transport.payloadcodec.codecparameters.number.UnsignedIntegerCodecParameter;
 import org.hbird.transport.payloadcodec.exceptions.UnexpectedParameterTypeException;
 import org.hbird.transport.payloadcodec.exceptions.UnknownParameterEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnsupportedParameterEncodingException;
 import org.hbird.transport.spacesystemmodel.exceptions.InvalidParameterTypeException;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter.Encoding;
 
public class IntegerCodecFactory {
 
 	private IntegerCodecFactory() {
 		// Utility class
 	}
 
 
 	/**
 	 * TODO update this doc
 	 *
 	 * For example, the values for unsigned, signMagnitude, twosComplement, and onesComplement are all stored in the
 	 * Java Integer type. The codec deals with calculating the correct parameter value as an Integer from the binary input
 	 * and also calculating the binary output version of the java Integer value. Obviously, for twos complement we can
 	 * simply use the toByteArray methods provided with the Java libs since Java Integers are stored as big endian twos
 	 * complement.
 	 * FIXME This is "javadocing" (too briefly) the entire process, not what this class does.
 	 *
 	 * @author Mark Doyle
 	 * @throws UnknownParameterEncodingException
 	 * @throws UnexpectedParameterTypeException
 	 * @throws InvalidParameterTypeException
 	 *
 	 */
 	public static CodecParameter<Integer> decorateParameterWithCodec(final Parameter<Integer> parameter)
 			throws UnsupportedParameterEncodingException, UnknownParameterEncodingException, UnexpectedParameterTypeException {
 
 		Encoding encoding = parameter.getEncoding();
 		int sizeInBits = parameter.getSizeInBits();
 
 		switch (encoding) {
 			case onesComplement:
 				throw new UnsupportedParameterEncodingException("File a bug report :D");
 			case twosComplement:
 				if (sizeInBits > Integer.SIZE) {
 					throw new UnexpectedParameterTypeException(
 							"Size of this parameter is > " + Integer.SIZE + " which is too big to be an integer. Size = " + sizeInBits);
 				}
 				else {
 					return new TwosComplementIntegerCodecParameter(parameter);
 				}
 			case unsigned:
 				if (sizeInBits > Integer.SIZE) {
 					throw new UnexpectedParameterTypeException(
 							"Size of this parameter is > " + Integer.SIZE + " 32 which is too big to be an integer. Size = " + sizeInBits);
 				}
 				else {
 					return new UnsignedIntegerCodecParameter(parameter);
 				}
 			case signMagnitude:
 				throw new UnsupportedParameterEncodingException("File a bug report :D", Encoding.signMagnitude);
 			default:
 				throw new UnknownParameterEncodingException(encoding.toString());
 		}
 
 	}
 }
