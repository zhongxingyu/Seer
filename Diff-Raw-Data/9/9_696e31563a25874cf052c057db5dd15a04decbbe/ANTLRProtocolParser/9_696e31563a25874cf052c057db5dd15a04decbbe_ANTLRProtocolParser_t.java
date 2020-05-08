 /*
  * Copyright 2009 Scribble.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.scribble.protocol.parser.antlr;
 
 import java.io.InputStream;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.scribble.protocol.model.*;
 import org.scribble.protocol.parser.ProtocolParser;
 import org.scribble.core.logger.ScribbleLogger;
 import org.scribble.core.model.*;
 
 public class ANTLRProtocolParser implements ProtocolParser {
 
 	public Model<Protocol> parse(InputStream is, ScribbleLogger logger) {
 		Model<Protocol> ret=null;
 		
         try {
             ScribbleProtocolLexer lex = new ScribbleProtocolLexer(new ANTLRInputStream(is));
            	CommonTokenStream tokens = new CommonTokenStream(lex);
 
     		ScribbleProtocolParser parser = new ScribbleProtocolParser(tokens);
 
     		ProtocolTreeAdaptor adaptor=new ProtocolTreeAdaptor();
     		adaptor.setParser(parser);
     		
     		parser.setTreeAdaptor(adaptor);
    		
    		parser.setLogger(logger);
 
     		parser.description();
     		
     		ret = adaptor.getProtocolModel();
             
         } catch (Exception e)  {
             e.printStackTrace();
         }
 		
 		return(ret);
 	}
 
 }
