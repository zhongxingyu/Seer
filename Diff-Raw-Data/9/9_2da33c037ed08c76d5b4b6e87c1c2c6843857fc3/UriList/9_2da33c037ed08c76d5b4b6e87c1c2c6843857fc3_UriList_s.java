 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.http;
 
 import java.net.URI;
 import java.util.TreeSet;
 import org.apache.commons.lang3.Validate;
 
 /**
 * Represents a set of URIs used for responses of {@link MediaType#TEXT_UR_ILIST type URI-list}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class UriList extends TreeSet<URI> implements Typeable {
 
     /**
      * Optional comment.
      */
     private String comment = "";
 
     /**
      * Set optional comment.
      *
      * Will be placed before the URI list preceeded with "# ".
      *
      * @param comment must not be {@code null}.
      */
     public void setComment(final String comment) {
         Validate.notNull(comment, "Comment must not be null!");
         this.comment = comment;
     }
 
     @Override
     public String getMediaType() {
         return MediaType.TEXT_URI_LIST;
     }
 
     @Override
     public String toString() {
         final StringBuilder buffer = new StringBuilder();
 
         if (!comment.isEmpty()) {
             buffer.append("# ").append(comment).append(Constants.NL);
         }
 
         for (final URI uri : this) {
             buffer.append(uri).append(Constants.NL);
         }
 
         return buffer.toString();
     }
 
 }
