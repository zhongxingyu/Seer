 package multiplexer.jmx.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
import java.util.List;
 import java.util.Random;
 import java.util.zip.CRC32;
 
import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.Output;

 import multiplexer.Multiplexer.MultiplexerMessage;
 import multiplexer.Multiplexer.WelcomeMessage;
 
 public class SimpleConnection {
 	
 	private final Socket socket;
 	private final long instance_id;
 	private final InputStream input_stream;
 	private final OutputStream output_stream;
 	private static final int header_length = 8;
	private long peer_id;
	
 	public long getInstanceId() {
 		return instance_id;
 	}
 
 	public SimpleConnection(SocketAddress address) throws IOException {
 		 instance_id = new Random().nextLong();
 		 socket = new Socket();
 		 socket.connect(address);
 		 output_stream = socket.getOutputStream();
 		 input_stream = socket.getInputStream();
 	 }
 	
 	public long send_message(ByteString message, int type) throws IOException {
 		MultiplexerMessage mxmsg = MultiplexerMessage.newBuilder()
 		.setId(new Random().nextLong())
 		.setFrom(instance_id)
         .setType(type)
         .setMessage(message)
         .build();
 
         byte[] body = mxmsg.toByteArray();
         System.out.println("sending "+body.length+" bytes ("+message.size()+" meaningful)");
         ByteBuffer buffer = ByteBuffer.allocate(header_length+body.length);
         buffer.order(ByteOrder.LITTLE_ENDIAN);
 		CRC32 body_sum = new CRC32();
 		body_sum.update(body);
 		buffer.putInt(body.length).putInt((int)body_sum.getValue()).put(body);	
 		output_stream.write(buffer.array());
         return mxmsg.getId();
 	}
 	
 	private byte[] read_all(int len) throws IOException {
 		byte[] buffer = new byte[len];
 		int off = 0;
 	    while (off < len) {
 	    	off += input_stream.read(buffer, off, len - off);
 	    }
 	    return buffer;
 	}
 	
 	public MultiplexerMessage receive_message() throws IOException {
 		ByteBuffer buffer = ByteBuffer.wrap(read_all(header_length));
 		buffer.order(ByteOrder.LITTLE_ENDIAN);	
 		int length = buffer.getInt();
 		int crc32 = buffer.getInt();
 		byte[] body = read_all(length);
 		CRC32 body_sum = new CRC32();
 		body_sum.update(body);
 		assert (int)body_sum.getValue() == crc32;
 		MultiplexerMessage mxmsg = MultiplexerMessage.newBuilder().mergeFrom(body).build();
 		System.out.println("received "+body.length+" bytes ("+mxmsg.getMessage().size()+" meaningful)");
         return mxmsg;
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		final int PYTHON_TEST_SERVER = 106;
 		final int CONNECTION_WELCOME = 2;
 		final int MULTIPLEXER = 1;
 		final int PYTHON_TEST_REQUEST = 110;
 		
 		SimpleConnection c = new SimpleConnection(new InetSocketAddress("localhost", 1980));
 		
 		// send out invitation
 	    System.out.println("sending welcome message");
 	    ByteString message = WelcomeMessage.newBuilder().setType(PYTHON_TEST_SERVER).setId(c.getInstanceId()).build().toByteString();
 	    c.send_message(message, CONNECTION_WELCOME);
 		
 	    // receive the invitation
 	    System.out.println("waiting for welcome message");
 	    MultiplexerMessage mxmsg = c.receive_message();
 	    System.out.println("validating welcome message");
 	    assert mxmsg.getType() == CONNECTION_WELCOME;
 	    WelcomeMessage peer = WelcomeMessage.newBuilder().mergeFrom(mxmsg.getMessage()).build();
 	    assert peer.getType() == MULTIPLEXER;
	    c.peer_id = peer.getId();
 
 	    // send a stupid search_query
 	    ArrayList<Byte> sq = new ArrayList<Byte>(); 
 	    for (byte d: "this is a search query with null (\\x00) bytes and other ".getBytes())
 	    	sq.add(d);    
 	    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
 	    	sq.add((byte)i);
 	    
 	    Output sqo = ByteString.newOutput();
 	    for (byte d: sq)
 	    	sqo.write(d);
 	    
 	    System.out.println("sending sample search query");
 	    long id = c.send_message(sqo.toByteString(), PYTHON_TEST_REQUEST);
 	    System.out.println("waiting for sample search query");
 	    mxmsg = c.receive_message();
 	    System.out.println("validating sample search query");
 	    assert mxmsg.getId() == id;
 	    assert mxmsg.getType() == PYTHON_TEST_REQUEST;
 	    assert mxmsg.getMessage().equals(sqo.toByteString());
 	    
 //	    # send a large search_query
 //	    query = open("/dev/urandom", "r").read(1024 * 1024)
 //	    print "sending large search query"
 //	    id = c.send_message(query, types.SEARCH_QUERY)
 //	    print "waiting for large search query"
 //	    msg = c.receive_message()
 //	    print "validating large search query"
 //	    assert msg.id == id
 //	    assert msg.type == types.SEARCH_QUERY
 //	    assert msg.message == query
 		
 	}
 
 }
 /*
 
 import socket, struct, sys, random, zlib, time
 from build.multiplexer.Multiplexer_pb2 import *
 from multiplexer_constants import peers, types
 
 def randint():
     return random.randint(0, 2**30)
 
 class Connection(object):
 
     socket = None
     instance_id = None
 
     def __init__(self, address):
 
         self.socket = socket.socket()
         self.socket.connect(address)
         self.instance_id = randint()
     
     def send_message(self, message, type):
         message = message.SerializeToString() if not isinstance(message, str) else message
         mxmsg = MultiplexerMessage()
         mxmsg.id = randint()
         setattr(mxmsg, 'from', self.instance_id)
         # mxmsg.set_to
         mxmsg.type = type
         mxmsg.message = message
 
         body = mxmsg.SerializeToString()
         print "sending %d bytes (%d meaningful)" % (len(body), len(message))
         header = struct.pack("Ii", len(body), zlib.crc32(body))
         self.socket.sendall(header + body)
         return mxmsg.id
 
     def _read_all(self, l):
         s = ""
         while len(s) < l:
             ch = self.socket.recv(l - len(s))
             if not ch:
                 print >> sys.stderr, "Connection died unexpectedly"
                 break
             s += ch
         assert not s or len(s) == l, (s, len(s), l)
         return s
 
     def receive_message(self):
         header = self._read_all(struct.calcsize("Ii"))
         if not header: return None
         length, crc = struct.unpack("Ii", header)
         body = self._read_all(length)
         assert body
         assert zlib.crc32(body) == crc, "Crc doesn't match"
         
         mxmsg = MultiplexerMessage()
         mxmsg.ParseFromString(body)
         assert mxmsg.IsInitialized()
         print "received %d bytes (%d meaningful)" % (len(body), len(mxmsg.message))
         return mxmsg
 
 def parse(type, serialized):
     t = type()
     t.ParseFromString(serialized)
     assert t.IsInitialized()
     return t
 
 def test():
     c = Connection(("localhost", 1980))
 
     # receive the invitation
     print "waiting for welcome message"
     msg = c.receive_message()
     print "validating welcome message"
     assert msg.type == types.CONNECTION_WELCOME
     welcome = parse(WelcomeMessage, msg.message)
     assert welcome.type == peers.MULTIPLEXER, "we have connected to Multiplexer"
     c.peer_id = welcome.id
 
     # send out invitation
     print "sending welcome message"
     welcome = WelcomeMessage()
     welcome.type = peers.SEARCH # FIXME
     welcome.id = c.instance_id
     c.send_message(welcome, types.CONNECTION_WELCOME)
 
     # send a stupid search_query
     query = "this is a search query with null (\x00) bytes and other " + "".join(chr(i) for i in range(256)) + " bytes"
     print "sending sample search query"
     id = c.send_message(query, types.SEARCH_QUERY)
     print "waiting for sample search query"
     msg = c.receive_message()
     print "validating sample search query"
     assert msg.id == id
     assert msg.type == types.SEARCH_QUERY
     assert msg.message == query
     
     # send a large search_query
     query = open("/dev/urandom", "r").read(1024 * 1024)
     print "sending large search query"
     id = c.send_message(query, types.SEARCH_QUERY)
     print "waiting for large search query"
     msg = c.receive_message()
     print "validating large search query"
     assert msg.id == id
     assert msg.type == types.SEARCH_QUERY
     assert msg.message == query
 
 if __name__ == "__main__":
     test()
 */
