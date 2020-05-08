 package com.handinfo.redis4j.impl.protocol.decode;
 
 import java.nio.charset.Charset;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferIndexFinder;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.frame.FrameDecoder;
 
 import com.handinfo.redis4j.api.RedisResultType;
 
 public class ByteArrayToFrameDecoder extends FrameDecoder
 {
 
 	@Override
 	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception
 	{
 		// 确保收到第一个\r\n.
 		int firstIndexCR = buffer.bytesBefore(ChannelBufferIndexFinder.CR);
 		int firstIndexLF = buffer.bytesBefore(ChannelBufferIndexFinder.LF);
 
 		if(!(firstIndexLF > 0 && firstIndexLF == firstIndexCR+1))
 		{
 			return null;
 		}
 
 		//buffer.markReaderIndex();
 
 		// Read the first byte.
 		char firstByte = (char) buffer.getByte(0);
 
 		switch (firstByte)
 		{
 		case RedisResultType.SingleLineReply:
 		{
 			// With a single line reply the first byte of the reply
 			// will be "+"
 			//第一个\r\n之前的数据(包括+号和\r\n)
 			return buffer.readBytes(firstIndexLF+1);
 			
 		}
 			//break;
 		case RedisResultType.ErrorReply:
 		{
 			// With an error message the first byte of the reply
 			// will be "-"
 			return buffer.readBytes(firstIndexLF+1);
 		}
 
 			//break;
 		case RedisResultType.IntegerReply:
 		{
 			// With an integer number the first byte of the reply
 			// will be ":"
 			return buffer.readBytes(firstIndexLF+1);
 		}
 			//break;
 		case RedisResultType.BulkReplies:
 		{
 			// With bulk reply the first byte of the reply will be
 			// "$"
 			
 			int contentLength = Integer.valueOf(buffer.toString(1, firstIndexCR-1, Charset.forName("UTF-8")));
 			if(contentLength == -1)
 			{
 				//返回的头内容为 -1，说明无后续数据
 				return buffer.readBytes(firstIndexLF+1);
 			}
 			else
 			{
 				//返回包含数据的包
 				int frameLength = firstIndexLF+1+contentLength+2;
 				if(buffer.readableBytes() < frameLength)
 				{
 					//数据不完整时
 					return null;
 				}
 				else
 				{
 					return buffer.readBytes(frameLength);
 				}
 			}
 		}
 		case RedisResultType.MultiBulkReplies:
 		{
 			// With multi-bulk reply the first byte of the reply
 			// will be "*"
 			int objectNumber = Integer.valueOf(buffer.toString(1, firstIndexCR-1, Charset.forName("UTF-8")));
 			if(objectNumber == -1 || objectNumber == 0)
 			{
 				//返回的头内容为 -1,说明服务器出错，返回头内容为0,说明没有符合查询条件的数据
 				return buffer.readBytes(firstIndexLF+1);
 			}
 			else
 			{
 				//完整的帧数据内,全部\r\n的个数
 				int allCRLF = 1 + objectNumber*2;
 				int indexOfCR = 0;
 				int indexOfLF = 0;
 				int numberOfCRLF = 0;
 				boolean isFrameFinish = false;
 				
 				//遍历数组查找最后一个\r\n的位置
 				for(int i=0; i<buffer.readableBytes(); i++)
 				{
 					if(buffer.getByte(i) == '\r')
 					{
 						indexOfCR = i;
 					}
 					if(buffer.getByte(i) == '\n')
 					{
 						indexOfLF = i;
 					}
 					if(indexOfCR+1 == indexOfLF)
 					{
 						if(++numberOfCRLF == allCRLF)
 						{
 							isFrameFinish = true;
 							break;
 						}
 					}
 				}
 
 				if(isFrameFinish)
 				{
 					return buffer.readBytes(indexOfLF + 1);
 				}
 				else
 				{
 					//数据不完整时
 					return null;
 				}
 			}
 		}
 		default:
 			return buffer.readBytes(1);
 		}
 	}
 }
