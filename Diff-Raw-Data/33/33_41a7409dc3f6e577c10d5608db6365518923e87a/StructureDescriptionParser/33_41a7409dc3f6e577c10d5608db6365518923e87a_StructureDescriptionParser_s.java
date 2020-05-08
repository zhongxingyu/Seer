 package net.mirky.redis;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.mirky.redis.BinaryElementType.Struct.Step;
 import net.mirky.redis.ControlData.LineParseError;
 import net.mirky.redis.ParseUtil.IndentationSensitiveLexer;
 
 abstract class StructureDescriptionParser {
     private StructureDescriptionParser() {
         // not a real constructor
     }
 
     static abstract class ParameterParser {
         /**
          * Parse parameters of a field type, if any, and return the complete
          * {@link StructFieldType}. Called with {@code lexer} positioned
          * immediately after the keyword, so it may need to start by ignoring
          * horizontal whitespace.
          * 
          * @throws ControlData.LineParseError
          * @throws IOException
          */
         abstract BinaryElementType parseParameters(ParseUtil.IndentationSensitiveLexer lexer) throws ControlData.LineParseError, IOException;
     }
 
     // for field types without parameters
     static final class SimpleFieldParameterParser extends StructureDescriptionParser.ParameterParser {
         private final BinaryElementType fieldType;
     
         public SimpleFieldParameterParser(BinaryElementType fieldType) {
             this.fieldType = fieldType;
         }
     
         @Override
         final BinaryElementType parseParameters(ParseUtil.IndentationSensitiveLexer lexer) throws ControlData.LineParseError, IOException {
             lexer.passNewline();
             return fieldType;
         }
     }
 
     static final class SlicedIntegerFieldParameterParser extends StructureDescriptionParser.ParameterParser {
         private final BinaryElementType.BasicInteger integerType;
     
         public SlicedIntegerFieldParameterParser(BinaryElementType.BasicInteger integerType) {
             this.integerType = integerType;
         }
     
         @Override
         final BinaryElementType.SlicedInteger parseParameters(ParseUtil.IndentationSensitiveLexer lexer) throws ControlData.LineParseError, IOException {
             lexer.skipSpaces();
             lexer.passNewline();
             lexer.passIndent();
             ArrayList<BinaryElementType.SlicedInteger.Slice> slices = new ArrayList<BinaryElementType.SlicedInteger.Slice>();
             while (!lexer.atDedent()) {
                 lexer.noIndent();
                 slices.add(StructureDescriptionParser.parseIntegerSlice(lexer));
             }
             lexer.skipThisDedent();
             return new BinaryElementType.SlicedInteger(integerType, slices.toArray(new BinaryElementType.SlicedInteger.Slice[0]));
         }
     }
 
     // Note that there are two forms of integer slices: 'basic' and 'flags'.
     // Basic slices hold an integer, flag slices hold a single bit.
     static final BinaryElementType.SlicedInteger.Slice parseIntegerSlice(ParseUtil.IndentationSensitiveLexer lexer) throws ControlData.LineParseError, IOException {
         lexer.pass('@');
         lexer.pass('.');
         int rightShift = lexer.parseUnsignedInteger("right shift");
         lexer.skipSpaces();
         BinaryElementType.SlicedInteger.Slice slice;
         if (lexer.atUnsignedInteger()) {
             // it's a basic slice; the field width (in bits) comes next
            int fieldWidth = lexer.parseUnsignedInteger("field width");
            if (fieldWidth == 0) {
                lexer.complain("zero-bit field?");
             }
             List<String> meanings = new ArrayList<String>();
             while (true) {
                 lexer.skipSpaces();
                 if (!lexer.at('"')) {
                     break;
                 }
                 meanings.add(lexer.parseThisString());
             }
            slice = new BinaryElementType.SlicedInteger.Slice.Basic(rightShift, fieldWidth, meanings.toArray(new String[0]));
         } else {
             // it's a flag slice; the field width is implicitly one
            String setMessage;
             if (lexer.at('"')) {
                setMessage = lexer.parseThisString();
             } else {
                setMessage = null;
             }
             lexer.skipSpaces();
            String clearMessage;
             if (lexer.at('/')) {
                 lexer.skipChar();
                 lexer.skipSpaces();
                clearMessage = lexer.parseString("cleared flag meaning");
             } else {
                clearMessage = null;
             }
            if (setMessage == null && clearMessage == null) {
                 lexer.complain("expected bit meaning");
             }
            slice = new BinaryElementType.SlicedInteger.Slice.Flag(rightShift, setMessage, clearMessage);
         }
         lexer.passNewline();
         return slice;
     }
 
     public static final BinaryElementType parseStructureDescription(String name, BufferedReader reader) throws LineParseError, IOException,
             RuntimeException {
         ParseUtil.IndentationSensitiveLexer lexer = new ParseUtil.IndentationSensitiveFileLexer(reader, name,
         '#');
         try {
             return parseType(lexer);
         } finally {
             reader.close();
         }
     }
 
     public static final BinaryElementType parseType(ParseUtil.IndentationSensitiveLexer lexer) throws LineParseError,
             IOException, RuntimeException {
         String keyword = lexer.parseDashedWord("type");
         StructureDescriptionParser.ParameterParser parameterParser = getFieldTypeParameterParser(keyword);
         BinaryElementType type;
         if (parameterParser == null) {
             try {
                 type = BinaryElementType.MANAGER.get(keyword);
             } catch (ResourceManager.ResolutionError e) {
                 lexer.complain("unknown type");
                 // {@link
                 // ParseUtil.IndentationSensitiveFileLexer#complain(String)}
                 // returned?
                 throw new RuntimeException("bug detected");
             }
             lexer.passNewline();
         } else {
             type = parameterParser.parseParameters(lexer);
         }
         return type;
     }
 
     public static final Step parseThisSeek(IndentationSensitiveLexer lexer) throws LineParseError, IOException {
         assert lexer.at('@');
         lexer.skipChar();
         int sign;
         if (lexer.at('+')) {
             lexer.skipChar();
             sign = +1;
         } else if (lexer.at('-')) {
             lexer.skipChar();
             sign = -1;
         } else {
             sign = 0;
         }
         int offset = lexer.parseUnsignedInteger("offset");
         return sign == 0 ? new Step.LocalSeek(offset) : new Step.RelSeek(sign * offset);
     }
 
     private static final Map<String, StructureDescriptionParser.ParameterParser> KNOWN_FIELD_TYPES = new HashMap<String, StructureDescriptionParser.ParameterParser>();
 
     static final StructureDescriptionParser.ParameterParser getFieldTypeParameterParser(String name) {
         return KNOWN_FIELD_TYPES.get(name);
     }
 
     static {
         KNOWN_FIELD_TYPES.put("sliced-byte", new SlicedIntegerFieldParameterParser(BinaryElementType.BasicInteger.BYTE));
         KNOWN_FIELD_TYPES.put("sliced-lewyde", new SlicedIntegerFieldParameterParser(BinaryElementType.BasicInteger.LEWYDE));
         KNOWN_FIELD_TYPES.put("sliced-bewyde", new SlicedIntegerFieldParameterParser(BinaryElementType.BasicInteger.BEWYDE));
 
         KNOWN_FIELD_TYPES.put("padded-string", new ParameterParser() {
             @Override
             final BinaryElementType parseParameters(ParseUtil.IndentationSensitiveLexer lexer) throws ControlData.LineParseError, IOException {
                 lexer.skipSpaces();
                 int size = lexer.parseUnsignedInteger("string length");
                 lexer.skipSpaces();
                 int padding = lexer.parseUnsignedInteger("char code");
                 if (padding >= 0x100) {
                     lexer.complain("value too high to be a char code");
                 }
                 lexer.passNewline();
                 return new BinaryElementType.PaddedString(size, (byte) padding);
             }
         });
         
         KNOWN_FIELD_TYPES.put("struct", new ParameterParser() {
             @Override
             final BinaryElementType parseParameters(IndentationSensitiveLexer lexer) throws LineParseError, IOException {
                 ArrayList<BinaryElementType.Struct.Step> steps = new ArrayList<BinaryElementType.Struct.Step>();
                 lexer.passNewline();
                 lexer.passIndent();
                 
                 while (!lexer.atDedent()) {
                     lexer.noIndent();
                     if (lexer.at('@')) {
                         Step seek = parseThisSeek(lexer);
                         steps.add(seek);
                         lexer.skipSpaces();
                     }
                     if (!(lexer.atEndOfLine() || lexer.atCommentChar())) {
                         ArrayList<BinaryElementType.Struct.Step> lineSteps = new ArrayList<BinaryElementType.Struct.Step>();
                         while (true) {
                             String fieldName = lexer.parseDashedWord("field name");
                             lineSteps.add(new Step.Pass(fieldName, null));
                             lexer.skipSpaces();
                             if (!lexer.at(',')) {
                                 break;
                             }
                             lexer.skipChar();
                             lexer.skipSpaces();
                             if (lexer.at('@')) {
                                 Step seek = parseThisSeek(lexer);
                                 lineSteps.add(seek);
                                 lexer.skipSpaces();
                             }
                         }
                         lexer.pass(':');
                         lexer.skipSpaces();
                         BinaryElementType fieldType = parseType(lexer);
                         for (Step step : lineSteps) {
                             step.setType(fieldType);
                         }
                         steps.addAll(lineSteps);
                     } else {
                         lexer.passNewline();
                     }
                 }
                 lexer.skipThisDedent();
                 if (!lexer.atEndOfFile()) {
                     lexer.complain("garbage follows structure description");
                 }
                 return new BinaryElementType.Struct(steps.toArray(new BinaryElementType.Struct.Step[0]));
             }
         });
     }
 }
