 package org.hbird.transport.payloadcodec.codecparameters;
 
 import org.hbird.transport.payloadcodec.codecparameters.number.TwosComplementLongCodecParameter;
 import org.hbird.transport.payloadcodec.exceptions.UnexpectedParameterTypeException;
 import org.hbird.transport.payloadcodec.exceptions.UnknownParameterEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnsupportedParameterEncodingException;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter;
 import org.hbird.transport.spacesystemmodel.parameters.Parameter.Encoding;
 
public final class LongCodecFactory {
 
 	private LongCodecFactory() {
 		// Utility class
 	}
 	/**
 	 * TODO update this doc
 	 */
 	public static CodecParameter<Long> decorateParameterWithCodec(final Parameter<Long> parameter)
 			throws UnsupportedParameterEncodingException, UnknownParameterEncodingException, UnexpectedParameterTypeException {
 
 		Encoding encoding = parameter.getEncoding();
 		int sizeInBits = parameter.getSizeInBits();
 
 		switch (encoding) {
 			case onesComplement:
 				throw new UnsupportedParameterEncodingException("File a bug report :D");
 			case twosComplement:
 				if (sizeInBits > Long.SIZE) {
 					throw new UnexpectedParameterTypeException(
 							"Size of this parameter is > 32 which is too big to be a signed long. Size = " + sizeInBits);
 				}
 				else {
 					return new TwosComplementLongCodecParameter(parameter);
 				}
 			case unsigned:
 				if (sizeInBits > Long.SIZE) {
 					throw new UnexpectedParameterTypeException(
 							"Size of this parameter is > 64 which is too big to be an unsigned integer. Size = "
 									+ sizeInBits);
 				}
 				else {
 					throw new UnsupportedParameterEncodingException("File a bug report :D");
 				}
 			case signMagnitude:
 				throw new UnsupportedParameterEncodingException("File a bug report :D");
 			default:
 				throw new UnknownParameterEncodingException(encoding.toString());
 		}
 
 	}
 }
