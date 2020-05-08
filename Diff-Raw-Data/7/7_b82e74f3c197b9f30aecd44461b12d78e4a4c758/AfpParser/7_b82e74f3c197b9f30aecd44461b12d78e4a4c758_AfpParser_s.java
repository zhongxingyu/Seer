 package org.afpparser.parser;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.afpparser.afp.modca.StructuredFieldFactory;
 import org.afpparser.afp.modca.StructuredFieldFactoryImpl;
 
 /**
  * This class performs a full parse of an AFP document and produces the object model.
  */
 public class AfpParser {
 
     private final StructuredFieldIntroducerParser parser;
 
     private AfpParser(FileInputStream afpFileInputStream,
             StructuredFieldIntroducerHandler structuredFieldIntroducerHandler,
             StructuredFieldHandler structuredFieldHandler) throws FileNotFoundException {
 
         StructuredFieldFactory structuredFieldFactory = new StructuredFieldFactoryImpl(
                 afpFileInputStream.getChannel());
 
         StructuredFieldIntroducerHandler structuredFieldCreator = new StructuredFieldCreator(
                 structuredFieldFactory, structuredFieldHandler);
 
         StructuredFieldIntroducerHandler sFIntroducerHandler = structuredFieldIntroducerHandler == null ?
                StructuredFieldIntroducerHandlers.chain(
                structuredFieldIntroducerHandler, structuredFieldCreator) : structuredFieldCreator;
 
         parser = new StructuredFieldIntroducerParser(afpFileInputStream, sFIntroducerHandler);
     }
 
     /**
      * Creates an AFPParser builder that will parse the AFP document stream.
      * @param afpFileInputStream The AFP document stream
      * @return The builder
      */
     public static Builder forInput(FileInputStream afpFileInputStream) {
         return new Builder(afpFileInputStream);
     }
 
     /**
      * The Builder provides a fluent API for building an AFPParser.
      *
      */
     public static class Builder {
         private final FileInputStream afpFileInputStream;
 
         private List<StructuredFieldHandler> sfHandlers = new ArrayList<StructuredFieldHandler>();
 
         private List<StructuredFieldIntroducerHandler> sfiHandlers = new ArrayList<StructuredFieldIntroducerHandler>();
 
         private Builder(FileInputStream afpFileInputStream) {
             this.afpFileInputStream = afpFileInputStream;
         }
 
         /**
          * Adds handler to the chain of {@link StructuredFieldHandler}s.
          *
          * @param handler handler to add
          * @return the builder
          */
         public Builder withHandler(StructuredFieldHandler handler) {
             sfHandlers.add(handler);
             return this;
         }
 
         /**
          * Adds handler to the chain of {@link StructuredFieldIntroducerHandler}s.
          *
          * @param handler handler to add
          * @return the builder
          */
         public Builder withHandler(StructuredFieldIntroducerHandler handler) {
             sfiHandlers.add(handler);
             return this;
         }
 
         /**
          * Constructs an AFPParser
          *
          * @return the AFPParser
          * @throws FileNotFoundException
          */
         public AfpParser build() throws FileNotFoundException {
             if (sfHandlers.size() == 0) {
                 throw new IllegalArgumentException("No StructuredFieldHandler configured");
             }
             StructuredFieldIntroducerHandler sfiHandler = sfiHandlers.size() == 0 ? null :
                    StructuredFieldIntroducerHandlers.chain(sfiHandlers);
             return new AfpParser(afpFileInputStream, sfiHandler,
                     StructuredFieldHandlers.chain(sfHandlers));
         }
 
         /**
          * Constructs an AFPParser and parses the AFP document
          * @throws IOException
          */
         public void parse() throws IOException {
             build().parse();
         }
     }
 
     /**
      * Parses the AFP document.
      * @throws IOException
      */
     public void parse() throws IOException {
         parser.parse();
     }
 }
