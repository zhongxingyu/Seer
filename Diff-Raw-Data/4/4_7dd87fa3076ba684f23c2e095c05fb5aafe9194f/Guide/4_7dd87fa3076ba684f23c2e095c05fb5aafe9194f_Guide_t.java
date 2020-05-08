 //        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
 //        Copyright (C) 2013  Adrián Romero Corchado
 //
 //        This program is free software: you can redistribute it and/or modify
 //        it under the terms of the GNU General Public License as published by
 //        the Free Software Foundation, either version 3 of the License, or
 //        (at your option) any later version.
 //
 //        This program is distributed in the hope that it will be useful,
 //        but WITHOUT ANY WARRANTY; without even the implied warranty of
 //        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //        GNU General Public License for more details.
 //
 //        You should have received a copy of the GNU General Public License
 //        along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.adrguides.model;
 
 import android.content.Context;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import com.adrguides.utils.HTTPUtils;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Created by adrian on 20/08/13.
  */
 public class Guide implements Parcelable {
 
     private String address;
     private String title;
     private String language;
     private String country;
     private String variant;
 
     private boolean stored;
 
     private List<Place> places = new ArrayList<Place>();
 
     public Guide() {
         address = null;
         title = "* * *";
         language = Locale.getDefault().getLanguage();
         country = Locale.getDefault().getCountry();
         variant = Locale.getDefault().getVariant();
 
         stored = false;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public String getVariant() {
         return variant;
     }
 
     public void setVariant(String variant) {
         this.variant = variant;
     }
 
     public Locale getLocale() {
         return new Locale(language, country, variant);
     }
 
     public List<Place> getPlaces() {
         return places;
     }
 
     public void setPlaces(List<Place> places) {
         this.places = places;
     }
 
     public void setStored(boolean stored) {
         this.stored = stored;
     }
 
     public boolean isStored() {
         return stored;
     }
 
     public void saveToDisk(Context context) throws IOException, JSONException {
 
         File dir = new File(context.getFilesDir(), "saved-" + sha256(getTitle()));
         dir.mkdir();
         File[] children = dir.listFiles();
         for (File c: children) {
             c.delete();
         }
 
         Map<String, String> processedimages = new HashMap<String, String>();
 
         JSONObject jsonguide = new JSONObject();
         jsonguide.put("address", getAddress());
         jsonguide.put("title", getTitle());
         jsonguide.put("language", getLanguage());
         jsonguide.put("country", getCountry());
         jsonguide.put("variant", getVariant());
 
         JSONArray chapters = new JSONArray();
         jsonguide.put("chapters", chapters);
         for (Place p: getPlaces()) {
             JSONObject chapter = new JSONObject();
             chapters.put(chapter);
             chapter.put("id", p.getId());
             chapter.put("title", p.getTitle());
 
             JSONArray paragraphs = new JSONArray();
             chapter.put("paragraphs", paragraphs);
             for (Section s : p.getSections()) {
                 JSONObject section = new JSONObject();
                paragraphs.put(section);
                 section.put("text", s.getText());
                 section.put("read", s.getRead());
                 section.put("image", saveImage(context, processedimages, dir, s.getImageURL()));
             }
         }
 
         // Save guidebook as JSON object.
         Writer fileguide  = null;
         try {
             fileguide = new OutputStreamWriter(new FileOutputStream(new File(dir, "guidebook.json")), "UTF-8");
             fileguide.append(jsonguide.toString());
         } finally {
             if (fileguide != null) {
                 try {
                     fileguide.close();
                 } catch (IOException e) {
                 }
             }
         }
 
        //
        setStored(true);
     }
 
     private String saveImage(Context context, Map<String, String> processedimages, File dir, String address) throws IOException {
 
 
         String outImage = processedimages.get(address);
         if (outImage == null) {
 
             InputStream inimage;
             try {
                 inimage = HTTPUtils.openAddress(context, new URL(address));
             } catch (IOException e) {
                 return null;
             }
 
             OutputStream outimage = null;
             try {
                 File outfile = new File(dir, "img-" + UUID.randomUUID().toString() + ".png");
                 outimage = new FileOutputStream(outfile);
 
                 byte[] buffer = new byte[1024];
                 int len;
                 while ((len = inimage.read(buffer)) != -1) {
                     outimage.write(buffer, 0, len);
                 }
 
                 outImage = outfile.toURI().toURL().toString();
                 processedimages.put(address, outImage);
             } finally {
                 if (inimage != null) {
                     try {
                         inimage.close();
                     } catch (IOException e) {
                     }
                 }
                 if (outimage != null) {
                     try {
                         outimage.close();
                     } catch (IOException e) {
                     }
                 }
             }
         }
 
         return outImage;
     }
 
     private static String sha256(String base) {
         try{
             MessageDigest digest = MessageDigest.getInstance("SHA-256");
             return bytesToHex(digest.digest(base.getBytes("UTF-8")));
         } catch(NoSuchAlgorithmException ex) {
             throw new RuntimeException(ex);
         } catch (UnsupportedEncodingException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     // private final static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
     private final static char[] hexArray = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'};
 
     private static String bytesToHex(byte[] bytes) {
         char[] chars = new char[bytes.length * 2];
         int v;
         int index = 0;
         for (byte b: bytes) {
             v = b & 0xFF;
             chars[index++] = hexArray[v >>> 4];
             chars[index++] = hexArray[v & 0x0F];
         }
         return new String(chars);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int i) {
         parcel.writeString(getAddress());
         parcel.writeString(getTitle());
         parcel.writeString(getLanguage());
         parcel.writeString(getCountry());
         parcel.writeString(getVariant());
         parcel.writeTypedList(getPlaces());
         parcel.writeInt(isStored() ? 1 : 0);
     }
 
     public static final Parcelable.Creator<Guide> CREATOR = new Parcelable.Creator<Guide>() {
         public Guide createFromParcel(Parcel in) {
             Guide guide = new Guide();
             guide.setAddress(in.readString());
             guide.setTitle(in.readString());
             guide.setLanguage(in.readString());
             guide.setCountry(in.readString());
             guide.setVariant(in.readString());
             guide.setPlaces(in.createTypedArrayList(Place.CREATOR));
             guide.setStored(in.readInt() == 1);
             return guide;
         }
 
         public Guide[] newArray(int size) {
             return new Guide[size];
         }
     };
 }
