 package com.psddev.dari.util;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.imgscalr.Scalr;
 
 public class ImageResizeStorageItemListener implements StorageItemListener {
 
     @SuppressWarnings("unchecked")
     public static boolean overridePathWithNearestSize(StorageItem item, Integer width, Integer height) {
         Map<String, Object> metadata = item.getMetadata();
         if (metadata == null) {
             return false;
         }
 
         List<Map<String, Object>> items = (List<Map<String, Object>>) metadata.get("resizes");
         if (items == null) {
             return false;
         }
 
         for (Map<String, Object> map : items) {
             String storage = ObjectUtils.to(String.class, map.get("storage"));
             StorageItem resizedItem = StorageItem.Static.createIn(storage);
             new ObjectMap(resizedItem).putAll(map);
 
             metadata = resizedItem.getMetadata();
 
             if (metadata != null && metadata.size() != 0) {
                 int w = ObjectUtils.to(Integer.class, metadata.get("width"));
                 int h = ObjectUtils.to(Integer.class, metadata.get("height"));
 
                 if ((width != null && width < w) && (height != null && height < h)) {
                     item.setPath((String) resizedItem.getPath());
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public void afterSave(StorageItem item) throws IOException {
         if (item.getPublicUrl().startsWith("file://")) {
             return;
         }
 
         String contentType = item.getContentType();
         InputStream data = item.getData();
         try {
             if (contentType != null && contentType.startsWith("image/")) {
                 BufferedImage original = ImageIO.read(data);

                if (original == null) {
                    return;
                }

                 String imageType = contentType.substring(6);
 
                 List<StorageItem> dimsItems = new ArrayList<StorageItem>();
                 item.getMetadata().put("resizes", dimsItems);
 
                 processSize(item, 500, original, imageType, dimsItems);
                 processSize(item, 1500, original, imageType, dimsItems);
             }
         } finally {
             if (data != null) {
                 data.close();
             }
 
             item.setData(null);
         }
     }
 
     private void processSize(StorageItem item, int newSize, BufferedImage original, String imageType, List<StorageItem> items) throws IOException {
         int width = original.getWidth();
         int height = original.getHeight();
         float aspect = (float) width / (float) height;
         if (width > newSize || height > newSize) {
             if (aspect > 1.0) {
                 width = newSize;
                 height = Math.round(width / aspect);
             } else {
                 height = newSize;
                 width = Math.round(height * aspect);
             }
 
             BufferedImage resizedImage = Scalr.resize(original, width, height);
             String url = item.getPath();
             List<String> parts = Arrays.asList(url.split("/"));
 
             StringBuilder pathBuilder = new StringBuilder();
             pathBuilder.append(StringUtils.join(parts.subList(0, parts.size() - 1), "/"));
             pathBuilder.append("/resizes/");
             pathBuilder.append(newSize);
             pathBuilder.append('/');
             pathBuilder.append(parts.get(parts.size() - 1));
 
             StorageItem dimsItem = StorageItem.Static.create();
             StorageItem.Static.resetListeners(dimsItem);
 
             dimsItem.setPath(pathBuilder.toString());
             dimsItem.setContentType(item.getContentType());
 
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             ImageIO.write(resizedImage, imageType, os);
             InputStream is = new ByteArrayInputStream(os.toByteArray());
 
             dimsItem.getMetadata().put("width", width);
             dimsItem.getMetadata().put("height", height);
             dimsItem.setData(is);
             dimsItem.save();
 
             items.add(dimsItem);
         }
     }
 }
