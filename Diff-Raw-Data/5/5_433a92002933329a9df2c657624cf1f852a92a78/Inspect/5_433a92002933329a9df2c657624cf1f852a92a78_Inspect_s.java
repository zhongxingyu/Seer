 package water.api;
 
 import hex.DGLM.GLMModel;
 import hex.*;
 import hex.rf.RFModel;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.zip.*;
 import water.*;
 import water.ValueArray.Column;
 import water.api.GLMProgressPage.GLMBuilder;
 import water.fvec.*;
 import water.parser.*;
 
 import water.util.Log;
 import water.util.Utils;
 
 import com.google.gson.*;
 
 public class Inspect extends Request {
   private static final HashMap<String, String> _displayNames = new HashMap<String, String>();
   private static final long                    INFO_PAGE     = -1;
   private final H2OExistingKey                 _key          = new H2OExistingKey(KEY);
   private final LongInt                        _offset       = new LongInt(OFFSET, 0L, INFO_PAGE, Long.MAX_VALUE, "");
   private final Int                            _view         = new Int(VIEW, 100, 0, 10000);
   private final Str                            _producer     = new Str(JOB, null);
 
   static final int MAX_COLUMNS_TO_DISPLAY = 1000;
 
   static {
     _displayNames.put(ENUM_DOMAIN_SIZE, "Enum Domain");
     _displayNames.put(MEAN, "&mu;");
     _displayNames.put(NUM_MISSING_VALUES, "Missing");
     _displayNames.put(VARIANCE, "&sigma;");
   }
 
   // Constructor called from 'Exec' query instead of the direct view links
   Inspect(Key k) {
     _key.reset();
     _key.check(this, k.toString());
     _offset.reset();
     _offset.check(this, "");
     _view.reset();
     _view.check(this, "");
   }
 
   // Default no-args constructor
   Inspect() {
   }
 
   public static Response redirect(JsonObject resp, Job keyProducer, Key dest) {
     JsonObject redir = new JsonObject();
     if (keyProducer!=null) redir.addProperty(JOB, keyProducer.job_key.toString());
     redir.addProperty(KEY, dest.toString());
     return Response.redirect(resp, Inspect.class, redir);
   }
 
   public static Response redirect(JsonObject resp, Key dest) {
     return redirect(resp, null, dest);
   }
 
   @Override protected boolean log() {
     return false;
   }
 
   @Override
   protected Response serve() {
     // Key might not be the same as Value._key, e.g. a user key
     Key key = Key.make(_key.record()._originalValue);
     Value val = _key.value();
     if(val == null) {
       // Some requests redirect before creating dest
       return RequestServer._http404.serve();
     }
     if( val.type() == TypeMap.PRIM_B )
       return serveUnparsedValue(key, val);
     Freezable f = val.getFreezable();
     if( f instanceof ValueArray ) {
       ValueArray ary = (ValueArray)f;
       if( ary._cols.length==1 && ary._cols[0]._name==null )
         return serveUnparsedValue(key, val);
       return serveValueArray(ary);
     }
     if( f instanceof Vec ) {
       return serveUnparsedValue(key, ((Vec) f).chunkIdx(0));
     }
     if( f instanceof Frame ) {
       return serveFrame(key, (Frame) f);
     }
     if( f instanceof GLMModel ) {
       GLMModel m = (GLMModel)f;
       JsonObject res = new JsonObject();
       res.add(GLMModel.NAME, m.toJson());
       Response r = Response.done(res);
       r.setBuilder(ROOT_OBJECT, new GLMBuilder(m, null));
       return r;
     }
     if( f instanceof hex.GLMGrid.GLMModels ) {
       JsonObject resp = new JsonObject();
       resp.addProperty(Constants.DEST_KEY, val._key.toString());
       return GLMGridProgress.redirect(resp,null,val._key);
     }
     if( f instanceof KMeansModel ) {
       KMeansModel m = (KMeansModel)f;
       JsonObject res = new JsonObject();
       res.add(KMeansModel.NAME, m.toJson());
       Response r = Response.done(res);
       r.setBuilder(KMeansModel.NAME, new KMeans.Builder(m));
       return r;
     }
     if( f instanceof RFModel ) {
       RFModel rfModel = (RFModel)f;
       JsonObject response = new JsonObject();
       return RFView.redirect(response, rfModel._selfKey, rfModel._dataKey, true);
     }
     if( f instanceof Job.Fail ) {
       UKV.remove(val._key);   // Not sure if this is a good place to do this
       return Response.error(((Job.Fail)f)._message);
     }
     return Response.error("No idea how to display a "+f.getClass());
   }
 
   public static byte [] getFirstBytes(Value v){
     byte[] bs = v.getFirstBytes();
     int off = 0;
     // First decrypt compression
     InputStream is = null;
     try {
       switch( water.parser.ParseDataset.guessCompressionMethod(v) ) {
       case NONE: // No compression
         off = bs.length; // All bytes ready already
         break;
       case ZIP: {
         ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bs));
         ZipEntry ze = zis.getNextEntry(); // Get the *FIRST* entry
         // There is at least one entry in zip file and it is not a directory.
         if( ze != null && !ze.isDirectory() )
           is = zis;
         else
           zis.close();
         break;
       }
       case GZIP:
         is = new GZIPInputStream(new ByteArrayInputStream(bs));
         break;
       }
       // If reading from a compressed stream, estimate we can read 2x uncompressed
       if( is != null )
         bs = new byte[bs.length * 2];
       // Now read from the (possibly compressed) stream
       while( off < bs.length ) {
         int len = is.read(bs, off, bs.length - off);
         if( len < 0 )
           break;
         off += len;
         if( off == bs.length ) { // Dataset is uncompressing alot! Need more space...
           if( bs.length >= ValueArray.CHUNK_SZ )
             break; // Already got enough
           bs = Arrays.copyOf(bs, bs.length * 2);
         }
       }
     } catch( IOException ioe ) { // Stop at any io error
       Log.err(ioe);
     } finally {
       Utils.close(is);
     }
     if( off < bs.length )
       bs = Arrays.copyOf(bs, off); // Trim array to length read
     return bs;
   }
 
   // Build a response JSON
   private final Response serveUnparsedValue(Key key, Value v) {
     JsonObject result = new JsonObject();
     result.addProperty(VALUE_TYPE, "unparsed");
     CustomParser.ParserSetup setup = ParseDataset.guessSetup(v);
     if( setup._data != null && setup._data[1].length > 0 ) { // Able to parse sanely?
       int zipped_len = v.getFirstBytes().length;
       double bytes_per_row = (double) zipped_len / setup._data.length;
       long rows = (long) (v.length() / bytes_per_row);
       result.addProperty(NUM_ROWS, "~" + rows); // approx rows
       result.addProperty(NUM_COLS, setup._data[1].length);
 
       result.add(ROWS, new Gson().toJsonTree(setup._data));
     } else {
       result.addProperty(NUM_ROWS, "unknown");
       result.addProperty(NUM_COLS, "unknown");
     }
     result.addProperty(VALUE_SIZE, v.length());
 
     // The builder Response
     Response r = Response.done(result);
     // Some nice links in the response
     r.addHeader("<div class='alert'>" //
         + Parse.link(key, "Parse into hex format") + " or " //
         + RReader.link(key, "from R data") + " </div>");
     // Set the builder for showing the rows
     r.setBuilder(ROWS, new ArrayBuilder() {
       public String caption(JsonArray array, String name) {
         return "<h4>First few sample rows</h4>";
       }
     });
     return r;
   }
 
   public Response serveValueArray(final ValueArray va) {
     if( _offset.value() > va._numrows )
       return Response.error("Value only has " + va._numrows + " rows");
 
     JsonObject result = new JsonObject();
     result.addProperty(VALUE_TYPE, "parsed");
     result.addProperty(KEY, va._key.toString());
     result.addProperty(NUM_ROWS, va._numrows);
     result.addProperty(NUM_COLS, va._cols.length);
     result.addProperty(ROW_SIZE, va._rowsize);
     result.addProperty(VALUE_SIZE, va.length());
 
     JsonArray cols = new JsonArray();
     JsonArray rows = new JsonArray();
 
     for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY, va._cols.length); i++ ) {
       Column c = va._cols[i];
       JsonObject json = new JsonObject();
       json.addProperty(NAME, c._name);
      json.addProperty(OFFSET, (int) c._off);
       json.addProperty(SIZE, Math.abs(c._size));
       json.addProperty(BASE, c._base);
       json.addProperty(SCALE, (int) c._scale);
       json.addProperty(MIN, c._min);
       json.addProperty(MAX, c._max);
       json.addProperty(MEAN, c._mean);
       json.addProperty(VARIANCE, c._sigma);
       json.addProperty(NUM_MISSING_VALUES, va._numrows - c._n);
       json.addProperty(TYPE, c._domain != null ? "enum" : (c.isFloat() ? "float" : "int"));
       json.addProperty(ENUM_DOMAIN_SIZE, c._domain != null ? c._domain.length : 0);
       cols.add(json);
     }
 
     if( _offset.value() != INFO_PAGE ) {
       long endRow = Math.min(_offset.value() + _view.value(), va._numrows);
       long startRow = Math.min(_offset.value(), va._numrows - _view.value());
       for( long row = Math.max(0, startRow); row < endRow; ++row ) {
         JsonObject obj = new JsonObject();
         obj.addProperty(ROW, row);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY, va._cols.length); ++i )
           format(obj, va, row, i);
         rows.add(obj);
       }
     }
 
     result.add(COLS, cols);
     result.add(ROWS, rows);
 
     Response r = Response.done(result);
     r.setBuilder(ROOT_OBJECT, new ObjectBuilder() {
       @Override
       public String build(Response response, JsonObject object, String contextName) {
         String s = html(va._key, va._numrows, va._cols.length, va._rowsize, va.length());
         Table t = new Table(argumentsToJson(), _offset.value(), _view.value(), va);
         s += t.build(response, object.get(ROWS), ROWS);
         return s;
       }
     });
     r.setBuilder(ROWS + "." + ROW, new ArrayRowElementBuilder() {
       @Override
       public String elementToString(JsonElement elm, String contextName) {
         String json = elm.getAsString();
         String html = _displayNames.get(json);
         return html != null ? html : RequestStatics.JSON2HTML(json);
       }
     });
     return r;
   }
 
   private static void format(JsonObject obj, ValueArray va, long rowIdx, int colIdx) {
     if( rowIdx < 0 || rowIdx >= va._numrows )
       return;
     if( colIdx >= va._cols.length )
       return;
     ValueArray.Column c = va._cols[colIdx];
     String name = c._name != null ? c._name : "" + colIdx;
     if( va.isNA(rowIdx, colIdx) ) {
       obj.addProperty(name, "NA");
     } else if( c._domain != null ) {
       obj.addProperty(name, c._domain[(int) va.data(rowIdx, colIdx)]);
     } else if( (c._size > 0) && (c._scale == 1) ) {
       obj.addProperty(name, va.data(rowIdx, colIdx));
     } else {
       obj.addProperty(name, va.datad(rowIdx, colIdx));
     }
   }
 
   private static void format(JsonObject obj, Frame f, long rowIdx, int colIdx) {
     Vec v = f._vecs[colIdx];
     if( rowIdx < 0 || rowIdx >= v.length() )
       return;
     String name = f._names[colIdx] != null ? f._names[colIdx] : "" + colIdx;
     switch(v.dtype()) {
       case U:
         obj.addProperty(name, "Unknown");
         break;
       case NA:
         obj.addProperty(name, "NA");
         break;
       case S:
         // TODO enums
         break;
       case I:
         obj.addProperty(name, v.at8(rowIdx));
         break;
       case F:
         obj.addProperty(name, v.at(rowIdx));
         break;
     }
   }
 
   private final String html(Key key, long rows, int cols, int bytesPerRow, long bytes) {
     String keyParam = KEY + "=" + key.toString();
     StringBuilder sb = new StringBuilder();
     // @formatter:off
     sb.append(""
         + "<h3>"
           + "<a href='RemoveAck.html?" + keyParam + "'>"
           + "<button class='btn btn-danger btn-mini'>X</button></a>"
           + "&nbsp;&nbsp;" + key.toString()
         + "</h3>");
     if (_producer.valid() && _producer.value()!=null) {
       Job job = Job.findJob(Key.make(_producer.value()));
       if (job!= null)
         sb.append("<div class='alert alert-success'>"
         		+ "<b>Produced in ").append(PrettyPrint.msecs(job.executionTime(),true)).append(".</b></div>");
     }
     sb.append("<div class='alert'>" +"View " + SummaryPage.link(key, "Summary") +  "<br/>Build models using "
           + RF.link(key, "Random Forest") + ", "
           + GLM.link(key, "GLM") + ", " + GLMGrid.link(key, "GLM Grid Search") + ", "
           + KMeans.link(key, "KMeans") + ", or "
           + KMeansGrid.link(key, "KMeansGrid") + "<br />"
           + "Score data using "
           + RFScore.link(key, "Random Forest") + ", "
           + GLMScore.link(KEY, key, 0.0, "GLM") + "</br><b>Download as</b> " + DownloadDataset.link(key, "CSV")
         + "</div>"
         + "<p><b><font size=+1>"
           + cols + " columns"
           + (bytesPerRow != 0 ? (", " + bytesPerRow + " bytes-per-row * " + rows + " rows = " + PrettyPrint.bytes(bytes)) : "")
         + "</font></b></p>");
     // @formatter:on
     return sb.toString();
   }
 
   // Frame
 
   public Response serveFrame(final Key key, final Frame f) {
     if( _offset.value() > f._vecs[0].length() )
       return Response.error("Value only has " + f._vecs[0].length() + " rows");
 
     JsonObject result = new JsonObject();
     result.addProperty(VALUE_TYPE, "parsed");
     result.addProperty(KEY, key.toString());
     result.addProperty(NUM_ROWS, f._vecs[0].length());
     result.addProperty(NUM_COLS, f._vecs.length);
 
     JsonArray cols = new JsonArray();
     JsonArray rows = new JsonArray();
 
     for( int i = 0; i < f._vecs.length; i++ ) {
       Vec v = f._vecs[i];
       JsonObject json = new JsonObject();
       json.addProperty(NAME, f._names[i]);
       json.addProperty(MIN, v.min());
       json.addProperty(MAX, v.max());
       cols.add(json);
     }
 
     if( _offset.value() != INFO_PAGE ) {
       long endRow = Math.min(_offset.value() + _view.value(), f._vecs[0].length());
       long startRow = Math.min(_offset.value(), f._vecs[0].length() - _view.value());
       for( long row = Math.max(0, startRow); row < endRow; ++row ) {
         JsonObject obj = new JsonObject();
         obj.addProperty(ROW, row);
         for( int i = 0; i < f._vecs.length; ++i )
           format(obj, f, row, i);
         rows.add(obj);
       }
     }
 
     result.add(COLS, cols);
     result.add(ROWS, rows);
 
     Response r = Response.done(result);
     r.setBuilder(ROOT_OBJECT, new ObjectBuilder() {
       @Override
       public String build(Response response, JsonObject object, String contextName) {
         String s = html(key, f._vecs[0].length(), f._vecs.length, 0, 0);
         Table2 t = new Table2(argumentsToJson(), _offset.value(), _view.value(), f);
         s += t.build(response, object.get(ROWS), ROWS);
         return s;
       }
     });
     r.setBuilder(ROWS + "." + ROW, new ArrayRowElementBuilder() {
       @Override
       public String elementToString(JsonElement elm, String contextName) {
         String json = elm.getAsString();
         String html = _displayNames.get(json);
         return html != null ? html : RequestStatics.JSON2HTML(json);
       }
     });
     return r;
   }
 
   private static final class Table extends PaginatedTable {
     private final ValueArray _va;
 
     public Table(JsonObject query, long offset, int view, ValueArray va) {
       super(query, offset, view, va._numrows, true);
       _va = va;
     }
 
     @Override
     public String build(Response response, JsonArray array, String contextName) {
       StringBuilder sb = new StringBuilder();
       if (_va._cols.length > MAX_COLUMNS_TO_DISPLAY)
         sb.append("<p style='text-align:center;'><center><h5 style='font-weight:800; color:red;'>Columns trimmed to " + MAX_COLUMNS_TO_DISPLAY + "</h5></center></p>");
       if( array.size() == 0 ) { // Fake row, needed by builder
         array = new JsonArray();
         JsonObject fake = new JsonObject();
         fake.addProperty(ROW, 0);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY, _va._cols.length); ++i )
           format(fake, _va, 0, i);
         array.add(fake);
       }
       sb.append(header(array));
 
       JsonObject row = new JsonObject();
 
       row.addProperty(ROW, MIN);
       for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
         row.addProperty(_va._cols[i]._name, _va._cols[i]._min);
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, MAX);
       for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
         row.addProperty(_va._cols[i]._name, _va._cols[i]._max);
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, MEAN);
       for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
         row.addProperty(_va._cols[i]._name, _va._cols[i]._mean);
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, VARIANCE);
       for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
         row.addProperty(_va._cols[i]._name, _va._cols[i]._sigma);
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, NUM_MISSING_VALUES);
       for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
         row.addProperty(_va._cols[i]._name, _va._numrows - _va._cols[i]._n);
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       if( _offset == INFO_PAGE ) {
         row.addProperty(ROW, OFFSET);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
          row.addProperty(_va._cols[i]._name, (int) _va._cols[i]._off);
         sb.append(defaultBuilder(row).build(response, row, contextName));
 
         row.addProperty(ROW, SIZE);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
           row.addProperty(_va._cols[i]._name, Math.abs(_va._cols[i]._size));
         sb.append(defaultBuilder(row).build(response, row, contextName));
 
         row.addProperty(ROW, BASE);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
           row.addProperty(_va._cols[i]._name, _va._cols[i]._base);
         sb.append(defaultBuilder(row).build(response, row, contextName));
 
         row.addProperty(ROW, SCALE);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
           row.addProperty(_va._cols[i]._name, (int) _va._cols[i]._scale);
         sb.append(defaultBuilder(row).build(response, row, contextName));
 
         row.addProperty(ROW, ENUM_DOMAIN_SIZE);
         for( int i = 0; i < Math.min(MAX_COLUMNS_TO_DISPLAY,_va._cols.length); i++ )
           row.addProperty(_va._cols[i]._name, _va._cols[i]._domain != null ? _va._cols[i]._domain.length : 0);
         sb.append(defaultBuilder(row).build(response, row, contextName));
       } else {
         for( JsonElement e : array ) {
           Builder builder = response.getBuilderFor(contextName + "_ROW");
           if( builder == null )
             builder = defaultBuilder(e);
           sb.append(builder.build(response, e, contextName));
         }
       }
 
       sb.append(footer(array));
       if (_va._cols.length > MAX_COLUMNS_TO_DISPLAY)
         sb.append("<p style='text-align:center;'><center><h5 style='font-weight:800; color:red;'>Columns trimmed to " + MAX_COLUMNS_TO_DISPLAY + "</h5></center></p>");
       return sb.toString();
     }
   }
 
   private static final class Table2 extends PaginatedTable {
     private final Frame _f;
 
     public Table2(JsonObject query, long offset, int view, Frame f) {
       super(query, offset, view, f._vecs[0].length(), true);
       _f = f;
     }
 
     @Override
     public String build(Response response, JsonArray array, String contextName) {
       StringBuilder sb = new StringBuilder();
       if( array.size() == 0 ) { // Fake row, needed by builder
         array = new JsonArray();
         JsonObject fake = new JsonObject();
         fake.addProperty(ROW, 0);
         for( int i = 0; i < _f._vecs.length; ++i )
           format(fake, _f, 0, i);
         array.add(fake);
       }
       sb.append(header(array));
 
       JsonObject row = new JsonObject();
 
       row.addProperty(ROW, MIN);
       for( int i = 0; i < _f._vecs.length; i++ )
         row.addProperty(_f._names[i], _f._vecs[i].min());
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, MAX);
       for( int i = 0; i < _f._vecs.length; i++ )
         row.addProperty(_f._names[i], _f._vecs[i].max());
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       row.addProperty(ROW, FIRST_CHUNK);
       for( int i = 0; i < _f._vecs.length; i++ )
         row.addProperty(_f._names[i], _f._vecs[i].chunk(0).getClass().getSimpleName());
       sb.append(ARRAY_HEADER_ROW_BUILDER.build(response, row, contextName));
 
       if( _offset == INFO_PAGE ) {
         for( int ci = 0; ci < _f._vecs[0].nChunks(); ci++ ) {
           Chunk chunk = _f._vecs[ci].elem2BV(ci);
           String prefix = CHUNK + " " + ci + " ";
 
           row.addProperty(ROW, prefix + TYPE);
           for( int i = 0; i < _f._vecs.length; i++ )
             row.addProperty(_f._names[i], chunk.getClass().getSimpleName());
           sb.append(defaultBuilder(row).build(response, row, contextName));
 
           row.addProperty(ROW, prefix + SIZE);
           for( int i = 0; i < _f._vecs.length; i++ )
             row.addProperty(_f._names[i], chunk.byteSize());
           sb.append(defaultBuilder(row).build(response, row, contextName));
         }
       } else {
         for( JsonElement e : array ) {
           Builder builder = response.getBuilderFor(contextName + "_ROW");
           if( builder == null )
             builder = defaultBuilder(e);
           sb.append(builder.build(response, e, contextName));
         }
       }
 
       sb.append(footer(array));
       return sb.toString();
     }
   }
 }
