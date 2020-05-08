 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.QQMeishiRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 public class QQMeishiBodyMessageCodec implements ICommandCodec {
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public ICommand decode(IoBuffer in, Charset charset)
 			throws PackageException {
 		log.debug("qqmeishi request message decode");
 		
 		QQMeishiRequestMessage message = new QQMeishiRequestMessage();
 		
 		if (in.remaining() < ProtocolLengths.COMMAND + ProtocolLengths.AMOUNT
 				+ 4) {
 			throw new PackageException(
 					"qqmeishi packge message body error, body message is :"
 							+ in);
 		}
 		
 		// 指令ID
 		long cmdId = in.getUnsignedInt();
 
 		// user_token len
 		int userTokenLen = in.getUnsignedShort();
 		if (userTokenLen > ProtocolLengths.USERTOKEN) {
 			throw new PackageException(
 					"qqmeishi packge message body error, user token len :"
 							+ userTokenLen);
 		}
 
 		String userToken = "";
 		if (userTokenLen > 0) {
 			byte[] userTokenByte = new byte[userTokenLen];
 			in.get(userTokenByte);
 			userToken = new String(userTokenByte, charset);
 		}
 
 		// amount   TODO 不想蛋疼字节序了
 //		byte[] amountBytes = new byte[ProtocolLengths.AMOUNT];
 //		in.get(amountBytes);
 //		double amount = Tools.getDouble(amountBytes);
 		int amountLen = in.getUnsignedShort();
 		String amountStr = "";
 		if (amountLen > 0) {
 			byte[] amountByte = new byte[amountLen];
 			in.get(amountByte);
 			amountStr = new String(amountByte, charset);
 		}
 		
 		//保留0.xx, 格式化
 		DecimalFormat decimalFormat = new DecimalFormat("0.##");
		double amount;
		try {
			amount = (Double) decimalFormat.parse(amountStr);
		} catch (ParseException e) {
			throw new PackageException(e);
		}
 		
 		// password
 		int passwordLen = in.getUnsignedShort();
 		String password = "";
 		if (passwordLen > 0) {
 			byte[] passwordByte = new byte[passwordLen];
 			in.get(passwordByte);
 			password = new String(passwordByte, charset);
 		}
 
 		message.setCmdId(cmdId);
 		message.setAmount(amount);
 		message.setPassword(password);
 		message.setUserToken(userToken);
 
 		log.trace("QQmeshiRequestMessage:{}", message);
 		return message;
 	}
 
 	/**
 	 * description：mock pos test use it!
 	 * 
 	 * @param bodyMessage
 	 * @param charset
 	 * @return
 	 * @time 2012-3-2
 	 * @author harry
 	 */
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 		
 		log.debug("QQmeishi request message encode");
 		QQMeishiRequestMessage requestMessage = (QQMeishiRequestMessage) bodyMessage;
 
 		long cmdId = requestMessage.getCmdId();
 		String userToken = requestMessage.getUserToken();
 		String password = requestMessage.getPassword();
 		double amount = requestMessage.getAmount();
 
 		int byteLen = ProtocolLengths.COMMAND + ProtocolLengths.POSNETSTRLEN
 				+ ProtocolLengths.POSNETSTRLEN + ProtocolLengths.POSNETSTRLEN;
 		
 		byte[] userTokenByte = null;
 		int userTokenLen = (userToken == null) ? 0 :userToken.length();
 		if(userTokenLen > 0){
 			userTokenByte = userToken.getBytes(charset);
 			userTokenLen = userTokenByte.length;
 		}
 		byteLen += userTokenLen;
 		
 		byte[] amountByte = null;
 		String amountStr = String.valueOf(amount);
 		int amountLen = (amountStr == null) ? 0 :amountStr.length();
 		if(amountLen > 0){
 			amountByte = amountStr.getBytes(charset);
 			amountLen = amountByte.length;
 		}
 		byteLen += amountLen;
 		
 		byte[] passwordByte = null;
 		int passwordLen = (password == null) ? 0 :password.length();
 		if(passwordLen > 0){
 			passwordByte = password.getBytes(charset);
 			passwordLen = passwordByte.length;
 		}
 		byteLen += passwordLen;
 		
 		
 		byte[] resultByte = new byte[byteLen];
 		int index = 0;
 		//指令ID
 		Tools.putUnsignedInt(resultByte, cmdId, index);
 		index += ProtocolLengths.COMMAND;
 		
 		//usertoken
 		Tools.putUnsignedShort(resultByte, userTokenLen, index);
 		index += ProtocolLengths.POSNETSTRLEN;
 		if(userTokenLen > 0){
 			Tools.putBytes(resultByte, userTokenByte, index);
 			index += userTokenByte.length;
 		}
 		
 		//amount double
 //		Tools.putDouble(resultByte, amount, index);
 //		index += ProtocolLengths.AMOUNT;
 		Tools.putUnsignedShort(resultByte, amountLen, index);
 		index += ProtocolLengths.POSNETSTRLEN;
 		if(amountLen > 0){
 			Tools.putBytes(resultByte, amountByte, index);
 			index += amountByte.length;
 		}
 		
 		
 		//password
 		Tools.putUnsignedShort(resultByte, passwordLen, index);
 		index += ProtocolLengths.POSNETSTRLEN;
 		if(passwordLen > 0){
 			Tools.putBytes(resultByte, passwordByte, index);
 			index += passwordByte.length;
 		}
 		
 		log.trace("QQmeishiRequestMessage:{}", requestMessage);
 
 		return resultByte;
 	}
 
 }
