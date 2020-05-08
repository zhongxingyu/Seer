 package daniel.viewheaders;
 
 import daniel.data.dictionary.KeyValuePair;
 import daniel.data.multidictionary.sequential.SequentialMultidictionary;
 import daniel.data.option.Option;
 import daniel.web.html.Attribute;
 import daniel.web.html.Element;
 import daniel.web.html.TextNode;
 import daniel.web.html.table.TableBuilder;
 import daniel.web.http.HttpRequest;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.HttpStatus;
 import daniel.web.http.server.HttpResponseFactory;
 import daniel.web.http.server.PartialHandler;
 
 final class HomeHandler implements PartialHandler {
   public static final HomeHandler singleton = new HomeHandler();
 
   private HomeHandler() {}
 
   @Override
   public Option<HttpResponse> tryHandle(HttpRequest request) {
     if (!request.getResource().equals("/"))
       return Option.none();
 
    Element html = Layout.createDocument(getHeaderTable(request.getHeaders()));
    return Option.some(HttpResponseFactory.xhtmlResponse(HttpStatus.OK, html));
   }
 
   private Element getHeaderTable(SequentialMultidictionary<String, String> headers) {
     TableBuilder tableBuilder = new TableBuilder()
         .setTableAttribute(Attribute.CLASS, "hairlined");
 
     tableBuilder.beginTableHead();
     tableBuilder.beginRow();
     tableBuilder.addHeaderEntry(TextNode.escapedText("Name"));
     tableBuilder.addHeaderEntry(TextNode.escapedText("Value"));
     tableBuilder.endRow();
     tableBuilder.endTableHead();
 
     tableBuilder.beginTableBody();
     for (KeyValuePair<String, String> header : headers) {
       tableBuilder.beginRow();
       tableBuilder.addNormalEntry(TextNode.escapedText(header.getKey()));
       tableBuilder.addNormalEntry(TextNode.escapedText(header.getValue()));
       tableBuilder.endRow();
     }
     tableBuilder.endTableBody();
 
     return tableBuilder.build();
   }
 }
