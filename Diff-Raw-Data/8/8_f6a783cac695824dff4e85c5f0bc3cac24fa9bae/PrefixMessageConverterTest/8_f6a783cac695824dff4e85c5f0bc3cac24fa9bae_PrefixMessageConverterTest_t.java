 package de.innoaccel.wamp.server.converter;
 
 import de.innoaccel.wamp.server.Websocket;
 import de.innoaccel.wamp.server.message.Message;
 import de.innoaccel.wamp.server.message.PrefixMessage;
 import mockit.Expectations;
 import mockit.NonStrictExpectations;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PrefixMessageConverterTest
 {
     private PrefixMessageConverter converter;
 
     @Before
     public void setUp()
     {
         this.converter = new PrefixMessageConverter();
     }
 
     @Test
     public void canConvertPrefixMessages()
     {
         Assert.assertTrue(this.converter.canConvert(Message.PREFIX));
     }
 
     @Test
     public void canNotConvertOtherMessages()
     {
         Assert.assertFalse(this.converter.canConvert(Message.WELCOME));
     }
 
     @Test
     public void serializesMessagesUsesPrefixAndURIOfMessage(final PrefixMessage message, Websocket socket)
         throws InvalidMessageCode
     {
         new Expectations() {{
             message.getPrefix(); result = "myPrefix";
             message.getURI(); result = "http://innoaccel.de/myPrefix";
         }};
 
         this.converter.serialize(message, socket);
     }
 
     @Test
    public void serializeMessageReturnsValidStringRepresentation(final PrefixMessage message, Websocket socket)
         throws InvalidMessageCode
     {
         new NonStrictExpectations() {{
             message.getPrefix(); result = "myPrefix";
             message.getURI(); result = "http://innoaccel.de/myPrefix";
         }};
 
         Assert.assertEquals(
             "[1, \"myPrefix\", \"http://innoaccel.de/myPrefix\"]",
             this.converter.serialize(message, socket)
         );
     }
 
     @Test(expected = MessageParseError.class)
    public void deserializeThrowsExceptionOnInvalidMarkup(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         this.converter.deserialize("no-valid-message", socket);
     }
 
     @Test(expected = MessageParseError.class)
    public void deserializeThrowsExceptionOnEmptyPrefixField(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         this.converter.deserialize("[1, \"\", \"x\"]", socket);
     }
 
     @Test(expected = MessageParseError.class)
     public void deserialzeThrowsExceptionOnEmptyURIField(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         this.converter.deserialize("[1, \"x\", \"\"]", socket);
     }
 
     @Test(expected = InvalidMessageCode.class)
     public void deserialzeThrowsExceptionOnInvalidMessageCodeButValidMarkup(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         this.converter.deserialize("[4, \"x\", \"x\"]", socket);
     }
 
     @Test
     public void deserialzeResultingPrefixMessageHasCorrectPrefix(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         PrefixMessage message = this.converter.deserialize("[1, \"prefix\", \"x\"]", socket);
         Assert.assertEquals("prefix", message.getPrefix());
     }
 
     @Test
     public void deserialzeResultingPrefixMessageHasCorrectURI(Websocket socket) throws MessageParseError, InvalidMessageCode
     {
         PrefixMessage message = this.converter.deserialize("[1, \"x\", \"uri\"]", socket);
         Assert.assertEquals("uri", message.getURI());
     }
 }
