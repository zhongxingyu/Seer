 package com.polopoly.ps.pcmd.tools;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import com.polopoly.ps.pcmd.argument.ArgumentException;
 import com.polopoly.ps.pcmd.argument.Arguments;
 import com.polopoly.ps.pcmd.argument.ContentIdListParameters;
 import com.polopoly.ps.pcmd.argument.ParameterHelp;
 import com.polopoly.util.client.PolopolyContext;
 
 public class ExportImagesParameters extends ContentIdListParameters
 {
    private URI fileService;
    { 
         try {
            fileService = new URI("http://localhost:8080/fileserver/file/");
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
    public URI getFileService() {
        return fileService;
     }
 
    public void setFileService(URI fileServerUrl) {
        this.fileService = fileServerUrl;
     }
 
     @Override
     public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException
     {
        String url = args.getOptionString("fileServer", null);
         try {
             if (url != null) {
                fileService = new URI(url);
             }
         } catch (URISyntaxException e) {
             throw new ArgumentException(url + " is not a valid URI", e);
         }
         super.parseParameters(args, context);
     }
 
     @Override
     public void getHelp(ParameterHelp help)
     {
        help.addOption("fileServerUrl", null, "The file server url");
         super.getHelp(help);
     }
 }
