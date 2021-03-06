 package ru.finam.bustard.codegen;
 
 import java.io.IOException;
 import java.util.List;
 
 public class ChannelsFinder implements ChannelsConsts {
 
     public static final String FILE_PATH = BUSTARD_PACKAGE_NAME.replace('.', '/') +
        "/" + CHANNELS_FILE_NAME;
 
     public static List<String> retrieveChannelKeys() throws IOException {
        return FileLinesParser.retrieveResource(Thread.currentThread().getContextClassLoader(), FILE_PATH);
     }
 }
