 package com.github.signed.mp3;
 
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.tag.KeyNotFoundException;
 import org.jaudiotagger.tag.TagField;
 import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
 import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
 import org.jaudiotagger.tag.id3.ID3v24Tag;
 import org.jaudiotagger.tag.id3.framebody.AbstractFrameBodyTextInfo;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyCOMM;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyMCDI;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyPRIV;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyTALB;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
 
 import java.io.PrintStream;
 import java.util.Iterator;
 
 public class Tags {
 
     public static Tags ExtractFrom(MP3File mp3File) {
         if (!mp3File.hasID3v2Tag()) {
             mp3File.setID3v2Tag(new ID3v24Tag());
         }
 
         if (!(mp3File.getID3v2Tag() instanceof ID3v24Tag)) {
             ID3v24Tag tags = mp3File.getID3v2TagAsv24();
             mp3File.setID3v2Tag(tags);
         }
         ID3v24Tag id3v2Tag = (ID3v24Tag) mp3File.getID3v2Tag();
         return new Tags(id3v2Tag);
     }
 
     private final ID3v24Tag tags;
 
     public Tags(ID3v24Tag tags) {
         this.tags = tags;
     }
 
     public boolean hasFrameFor(Tag tag) {
         return hasFrameFor(tag.frameId());
     }
 
     public boolean hasFrameFor(String id3v24FrameKey) {
         return tags.hasFrameAndBody(id3v24FrameKey);
     }
 
     public void dropFrameFor(Tag tag) {
         tags.deleteField(tag.frameId());
     }
 
     public AbstractTagFrameBody createFrameFor( Tag tag) {
         String id3v24FrameKey = tag.frameId();
         try {
             AbstractID3v2Frame theFrame = tags.getFirstField(id3v24FrameKey);
             if (null == theFrame) {
                AbstractID3v2Frame frame = tags.createFrame(tag.frameId());
                 tags.setFrame(frame);
             }
             theFrame = tags.getFirstField(id3v24FrameKey);
             return theFrame.getBody();
        } catch (KeyNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void dumpTagsTo(PrintStream out) {
         Iterator<TagField> iterator = tags.getFields();
         while (iterator.hasNext()) {
             TagField next = iterator.next();
 
             if (next instanceof AbstractID3v2Frame) {
                 AbstractID3v2Frame frame = (AbstractID3v2Frame) next;
                 String value;
                 AbstractTagFrameBody abstractBody = frame.getBody();
                 if (abstractBody instanceof FrameBodyTALB) {
                     FrameBodyTALB body = (FrameBodyTALB) abstractBody;
                     value = body.getUserFriendlyValue();
                 } else if (abstractBody instanceof AbstractFrameBodyTextInfo) {
                     AbstractFrameBodyTextInfo textBody = (AbstractFrameBodyTextInfo) abstractBody;
                     value = textBody.getText();
                 } else if (abstractBody instanceof FrameBodyTRCK) {
                     FrameBodyTRCK body = (FrameBodyTRCK) abstractBody;
                     value = body.getUserFriendlyValue();
                 } else if (abstractBody instanceof FrameBodyCOMM) {
                     FrameBodyCOMM body = (FrameBodyCOMM) abstractBody;
                     value = "\n\tdescription: " + body.getDescription();
                     value += "\n\tlanguage   :" + body.getLanguage();
                     value += "\n\ttext       :" + body.getText();
                     value += "\n\tfriendly   :" + body.getUserFriendlyValue();
                 } else if (abstractBody instanceof FrameBodyAPIC) {
                     FrameBodyAPIC body = (FrameBodyAPIC) abstractBody;
                     value = body.getUserFriendlyValue();
                 } else if (abstractBody instanceof FrameBodyMCDI) {
                     FrameBodyMCDI body = (FrameBodyMCDI) abstractBody;
                     value = body.getUserFriendlyValue();
                 }else if (abstractBody instanceof FrameBodyPRIV) {
                     FrameBodyPRIV body = (FrameBodyPRIV) abstractBody;
                     value = body.getUserFriendlyValue();
                 } else {
                     value = abstractBody.getClass().getCanonicalName();
                 }
                 String id = frame.getId();
                 out.println(id + " : " + value);
             } else {
                 throw new RuntimeException();
             }
         }
     }
 }
