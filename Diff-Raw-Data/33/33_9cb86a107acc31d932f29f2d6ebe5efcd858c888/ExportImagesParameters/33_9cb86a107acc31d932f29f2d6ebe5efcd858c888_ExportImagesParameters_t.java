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
    private URI fileServerUrl;
    {
         try {
            fileServerUrl = new URI("http://localhost:8080/fileserver/file/");
         } catch (URISyntaxException e) {
             throw new RuntimeException(e);
         }
     }
 
    public URI getFileServerUrl() {
        return fileServerUrl;
     }
 
    public void setFileServerUrl(URI fileServerUrl) {
        this.fileServerUrl = fileServerUrl;
     }
 
     @Override
     public void parseParameters(Arguments args, PolopolyContext context) throws ArgumentException
     {
        String url = args.getOptionString("fileServerUrl", null);
         try {
             if (url != null) {
                fileServerUrl = new URI(url);
             }
         } catch (URISyntaxException e) {
             throw new ArgumentException(url + " is not a valid URI", e);
         }
         super.parseParameters(args, context);
     }
 
     @Override
     public void getHelp(ParameterHelp help)
     {
        help.addOption("fileServerUrl", null, "Fileserver url for uploading images");
         super.getHelp(help);
     }
 }
