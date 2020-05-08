 /*
   Copyright (c) 2009 Philipp Carpus  <random234@gmx.net>
   Copyright (c) 2009 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 
 package core;
 
 import com.sun.jna.*;
 import gtnative.*;
 
 public class Str {
   private Pointer str_ptr;
   private Boolean disposed;
 
   public Str(String cstr) {
     if (cstr.equals(null)) {
       str_ptr = GT.INSTANCE.gt_str_new();
     } else {
       str_ptr = GT.INSTANCE.gt_str_new_cstr(cstr);
     }
     disposed = false;
   }
 
   public Str(Pointer str) {
     str_ptr = GT.INSTANCE.gt_str_ref(str);
     disposed = false;
   }
 
   public synchronized void dispose() {
    GT.INSTANCE.gt_str_delete(str_ptr);
   }
 
   protected void finalize() {
     if (!disposed) {
       dispose();
     }
   }
 
   public void append_str(Str str) {
     GT.INSTANCE.gt_str_append_str(str_ptr, str.to_ptr());
   }
 
   public void append_str(String str) {
     GT.INSTANCE.gt_str_append_cstr(str_ptr, str);
   }
 
   public String to_s() {
     return GT.INSTANCE.gt_str_get(str_ptr);
   }
 
   public long length() {
     return GT.INSTANCE.gt_str_length(str_ptr).longValue();
   }
 
   public Pointer to_ptr() {
     return str_ptr;
   }
 
 }
