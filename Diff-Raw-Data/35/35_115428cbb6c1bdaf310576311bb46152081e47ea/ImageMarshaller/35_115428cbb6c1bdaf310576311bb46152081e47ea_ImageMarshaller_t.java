 package com.ouchadam.fang.persistance.database.marshaller;
 
 import com.ouchadam.fang.domain.channel.Image;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 import com.ouchadam.fang.persistance.database.bridge.ContentProviderOperationValues;
 import com.ouchadam.fang.persistance.database.bridge.OperationWrapper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ImageMarshaller extends BaseMarshaller<Image> {
 
     private final String channelTitle;
     private List<ContentProviderOperationValues> operations;
 
     public ImageMarshaller(OperationWrapper operationWrapper, String channelTitle) {
         super(operationWrapper);
         this.channelTitle = channelTitle;
     }
 
     @Override
     public List<ContentProviderOperationValues> marshall(Image what) {
         operations = new ArrayList<ContentProviderOperationValues>();
         insertItem(what);
         return operations;
     }
 
     private void insertItem(Image image) {
         ContentProviderOperationValues itemBuilder = newInsertFor(Uris.IMAGE);
         itemBuilder.withValue(Tables.ChannelImage.CHANNEL.name(), channelTitle);
         itemBuilder.withValue(Tables.ChannelImage.TITLE.name(), image.getTitle());
        itemBuilder.withValue(Tables.ChannelImage.IMAGE_URL.name(), image.getUrl());
         itemBuilder.withValue(Tables.ChannelImage.LINK.name(), image.getLink());
         itemBuilder.withValue(Tables.ChannelImage.WIDTH.name(), image.getWidth());
         itemBuilder.withValue(Tables.ChannelImage.HEIGHT.name(), image.getHeight());
         operations.add(itemBuilder);
     }
 }
