 /*
  * Copyright (c) 2013, the authors.
  *
  *   This file is part of 'DXFS'.
  *
  *   DXFS is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   DXFS is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with DXFS.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Copyright (c) 2009, 2011, Oracle and/or its affiliates. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *
  *   - Neither the name of Oracle nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /*
  * This source code is provided to illustrate the usage of a given feature
  * or technique and has been deliberately simplified. Additional steps
  * required for a production-quality application, such as security checks,
  * input validation and proper error handling, might not be present in
  * this sample code.
  */
 
 
 package nextflow.fs.dx;
 
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 import java.nio.charset.CoderResult;
 import java.nio.charset.CodingErrorAction;
 import java.nio.file.InvalidPathException;
 import java.nio.file.LinkOption;
 import java.nio.file.NoSuchFileException;
 import java.nio.file.Path;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.regex.Pattern;
 
 import com.sun.corba.se.impl.io.TypeMismatchException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Implements the {@code Path} for DnaNexus cloud storage
  *
  * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
  * @author  Xueming Shen, Rajendra Gutupalli,Jaya Hangal
  */
 
 public class DxPath extends AbstractPath {
 
     private static Logger log = LoggerFactory.getLogger(DxPath.class);
 
     enum PathType {
 
         FILE(1),
         DIRECTORY(2),
         UNKNOWN(3);
 
         final int value;
 
         PathType(int value) { this.value=value; }
 
     }
 
     private static ThreadLocal<SoftReference<CharsetEncoder>> encoder = new ThreadLocal<SoftReference<CharsetEncoder>>();
 
     private final DxFileSystem fs;
 
     // internal representation
     private final byte[] path;
 
     // array of offsets of elements in path (created lazily)
     private volatile int[] offsets;
 
     // package-private
     volatile String fileId;
 
     // package-private
     volatile PathType type = PathType.UNKNOWN;
 
     // package-private
     volatile Map<String,Object> attributes;
 
     /**
      * Create a {@code DxPath} having the file path and its unique id
      *
      * @param fs The underlying {@code DxFileSystem} instance
      * @param path The file path as a string
      * @param fileId This file unique identifier provider by the cloud storage
      * @return A {@code DxPath} instance
      */
     static public DxPath file( DxFileSystem fs, String path, String fileId ) {
         DxPath result = new DxPath(fs, path);
         result.type = PathType.FILE;
         result.fileId = fileId;
         return result;
     }
 
     /**
      * Create a {@code DxPath} having the file path and its unique id and the object attributes
      *
      * @param fs The underlying {@code DxFileSystem} instance
      * @param path The file path as a string
      * @param fileId This file unique identifier provider by the cloud storage
      * @param attr The file attributes
      * @return A {@code DxPath} instance
      */
     static public DxPath file( DxFileSystem fs, String path, String fileId, Map<String,Object> attr ) {
         DxPath result = new DxPath(fs, path);
         result.type = PathType.FILE;
         result.fileId = fileId;
         result.attributes = attr;
         return result;
     }
 
     static public DxPath directory( DxFileSystem fs, String path ) {
         DxPath result = new DxPath(fs, path);
         result.type = PathType.DIRECTORY;
         return result;
     }
 
     DxPath(DxFileSystem fs, byte[] path) {
         this.fs = fs;
         this.path = path;
     }
 
     DxPath(DxFileSystem fs, byte[] path, PathType type, String fileId, Map<String,Object> attr) {
         this.fs = fs;
         this.path = path;
         this.type = type;
         this.fileId = fileId;
         this.attributes = attr;
     }
 
     DxPath(DxFileSystem fs, String input) {
         // removes redundant slashes and checks for invalid characters
         this(fs, encode(normalizeAndCheck(input)));
 
         // here check if match file-xxxx pattern, if so declare it as a FILE
         // and set the object id
     }
 
     public String getFileId() throws IOException {
 
         if( fileId != null ) {
             return fileId;
         }
 
         if( type == PathType.UNKNOWN ) {
             guessType(PathType.FILE);
         }
 
         if( type == PathType.FILE && fileId != null ) {
             return fileId;
         }
 
         throw new NoSuchFileException( this.toString() );
     }
 
 
     public boolean isRoot() {
         return "/".equals(normalize().toString());
     }
 
     private static Pattern FILE_ID_PATTERN = Pattern.compile("file-[a-zA-Z0-9]{24}");
 
 
     static boolean isFileId( String str ) {
         if( str == null ) return false;
         return FILE_ID_PATTERN.matcher(str).matches();
     }
 
 
     private void guessType() throws IOException {
         guessType( PathType.UNKNOWN );
     }
 
     private void guessType( PathType requestType ) throws IOException {
         if( fileId == null && attributes != null ) {
             fileId = (String)attributes.get("id");
         }
         // when a *fileId* attribute is defined it is a file by definition
         if( fileId != null ) {
             type = PathType.FILE;
         }
 
         // still unknown?
         if( type == PathType.UNKNOWN ) {
 
            DxPath normalized = normalize();
             if( normalized.isRoot() ) {
                 type = PathType.DIRECTORY;
             }
             else {
                 DxPath parent = normalized.getParent();
                 if( parent == null ) parent = fs.getPath("/");
                 Iterator<DxPath> it = fs.folderIterator(parent);
                 List<DxPath> matching = new ArrayList<>();
 
                 while( it.hasNext() ) {
                     DxPath item = it.next();
                     boolean sameName = normalized.compareTo(item) == 0;
                     if( sameName && (item.type.value & requestType.value) != 0 ) {
                         matching.add(item);
                     }
                 }
 
                 // just one match, get it
                 if( matching.size() == 1 ) {
                     this.type = matching.get(0).type;
                     this.fileId = matching.get(0).fileId;
                     this.attributes = matching.get(0).attributes;
                 }
                 else if( matching.size()>1 ) {
                     throw new IllegalStateException(String.format("Ambiguous file name: '%s' -- multiple objects with this name in path: '%s'", parent.toString(), normalized.getFileName()));
                 }
             }
         }
     }
 
     // TODO refactor 'readAttributes' method moving it to DxFileAttributeView
 
     DxFileAttributes readAttributes() throws IOException {
 
         if( type == PathType.UNKNOWN ) {
             guessType();
         }
 
         if( type == PathType.DIRECTORY ) {
             return DxFileAttributes.directory(this.toString());
         }
         else if( type == PathType.FILE && fileId != null ) {
             DxFileAttributes result;
             if( attributes == null ) {
                 attributes = fs.remote().describeFile(fileId);
             }
             result = DxFileAttributes.file(attributes);
             return result;
         }
 
         else {
             throw new NoSuchFileException( this.toString() );
         }
     }
 
 
     public String toString() {
         return new String(path);
     }
 
 
     // package-private
     // removes redundant slashes and check input for invalid characters
     static String normalizeAndCheck(String input) {
         int n = input.length();
         char prevChar = 0;
         for (int i=0; i < n; i++) {
             char c = input.charAt(i);
             if ((c == '/') && (prevChar == '/'))
                 return normalize(input, n, i - 1);
             checkNotNul(input, c);
             prevChar = c;
         }
         if (prevChar == '/')
             return normalize(input, n, n - 1);
         return input;
     }
 
     private static void checkNotNul(String input, char c) {
         if (c == '\u0000')
             throw new InvalidPathException(input, "Nul character not allowed");
     }
 
     private static String normalize(String input, int len, int off) {
         if (len == 0)
             return input;
         int n = len;
         while ((n > 0) && (input.charAt(n - 1) == '/')) n--;
         if (n == 0)
             return "/";
         StringBuilder sb = new StringBuilder(input.length());
         if (off > 0)
             sb.append(input.substring(0, off));
         char prevChar = 0;
         for (int i=off; i < n; i++) {
             char c = input.charAt(i);
             if ((c == '/') && (prevChar == '/'))
                 continue;
             checkNotNul(input, c);
             sb.append(c);
             prevChar = c;
         }
         return sb.toString();
     }
 
     // encodes the given path-string into a sequence of bytes
     private static byte[] encode(String input) {
         SoftReference<CharsetEncoder> ref = encoder.get();
         CharsetEncoder ce = (ref != null) ? ref.get() : null;
         if (ce == null) {
             ce = Charset.defaultCharset().newEncoder()
                     .onMalformedInput(CodingErrorAction.REPORT)
                     .onUnmappableCharacter(CodingErrorAction.REPORT);
             encoder.set(new SoftReference<CharsetEncoder>(ce));
         }
 
         char[] ca = input.toCharArray();
 
         // size output buffer for worse-case size
         byte[] ba = new byte[(int)(ca.length * (double)ce.maxBytesPerChar())];
 
         // encode
         ByteBuffer bb = ByteBuffer.wrap(ba);
         CharBuffer cb = CharBuffer.wrap(ca);
         ce.reset();
         CoderResult cr = ce.encode(cb, bb, true);
         boolean error;
         if (!cr.isUnderflow()) {
             error = true;
         } else {
             cr = ce.flush(bb);
             error = !cr.isUnderflow();
         }
         if (error) {
             throw new InvalidPathException(input, "Malformed input or input contains unmappable chacraters");
         }
 
         // trim result to actual length if required
         int len = bb.position();
         if (len != ba.length)
             ba = Arrays.copyOf(ba, len);
 
         return ba;
     }
 
     // create offset list if not already created
     private void initOffsets() {
         if (offsets == null) {
             int count, index;
 
             // count names
             count = 0;
             index = 0;
             if (isEmpty()) {
                 // empty path has one name
                 count = 1;
             } else {
                 while (index < path.length) {
                     byte c = path[index++];
                     if (c != '/') {
                         count++;
                         while (index < path.length && path[index] != '/')
                             index++;
                     }
                 }
             }
 
             // populate offsets
             int[] result = new int[count];
             count = 0;
             index = 0;
             while (index < path.length) {
                 byte c = path[index];
                 if (c == '/') {
                     index++;
                 } else {
                     result[count++] = index++;
                     while (index < path.length && path[index] != '/')
                         index++;
                 }
             }
             synchronized (this) {
                 if (offsets == null)
                     offsets = result;
             }
         }
     }
 
     // package-private
     byte[] asByteArray() {
         return path;
     }
 
     // returns {@code true} if this path is an empty path
     private boolean isEmpty() {
         return path.length == 0;
     }
 
     // returns an empty path
     private DxPath emptyPath() {
         return new DxPath(getFileSystem(), new byte[0]);
     }
 
     @Override
     public DxFileSystem getFileSystem() {
         return fs;
     }
 
     @Override
     public boolean isAbsolute() {
         return (path.length > 0 && path[0] == '/');
     }
 
     @Override
     public DxPath getRoot() {
         if (path.length > 0 && path[0] == '/') {
             return getFileSystem().rootDirectory();
         } else {
             return null;
         }
     }
 
     @Override
     public DxPath getFileName() {
         initOffsets();
 
         int count = offsets.length;
 
         // no elements so no name
         if (count == 0)
             return null;
 
         // one name element and no root component
         if (count == 1 && path.length > 0 && path[0] != '/')
             return this;
 
         int lastOffset = offsets[count-1];
         int len = path.length - lastOffset;
         byte[] result = new byte[len];
         System.arraycopy(path, lastOffset, result, 0, len);
        return new DxPath(getFileSystem(), result);
     }
 
 
     @Override
     public DxPath getParent() {
         initOffsets();
 
         int count = offsets.length;
         if (count == 0) {
             // no elements so no parent
             return null;
         }
         int len = offsets[count-1] - 1;
         if (len <= 0) {
             // parent is root only (may be null)
             return getRoot();
         }
         byte[] result = new byte[len];
         System.arraycopy(path, 0, result, 0, len);
 
         // A parent object is a directory by definition
         return new DxPath(getFileSystem(), result, PathType.DIRECTORY, null, null);
     }
 
 
     @Override
     public int getNameCount() {
         initOffsets();
         return offsets.length;
     }
 
 
     @Override
     public DxPath getName(int index) {
         initOffsets();
         if (index < 0)
             throw new IllegalArgumentException();
         if (index >= offsets.length)
             throw new IllegalArgumentException();
 
         int begin = offsets[index];
         int len;
         if (index == (offsets.length-1)) {
             len = path.length - begin;
         } else {
             len = offsets[index+1] - begin - 1;
         }
 
         // construct result
         byte[] result = new byte[len];
         System.arraycopy(path, begin, result, 0, len);
         return new DxPath(getFileSystem(), result);
     }
 
 
     @Override
     public DxPath subpath(int beginIndex, int endIndex) {
         initOffsets();
 
         if (beginIndex < 0)
             throw new IllegalArgumentException();
         if (beginIndex >= offsets.length)
             throw new IllegalArgumentException();
         if (endIndex > offsets.length)
             throw new IllegalArgumentException();
         if (beginIndex >= endIndex) {
             throw new IllegalArgumentException();
         }
 
         // starting offset and length
         int begin = offsets[beginIndex];
         int len;
         if (endIndex == offsets.length) {
             len = path.length - begin;
         } else {
             len = offsets[endIndex] - begin - 1;
         }
 
         // construct result
         byte[] result = new byte[len];
         System.arraycopy(path, begin, result, 0, len);
         return new DxPath(getFileSystem(), result);
     }
 
     @Override
     public boolean startsWith(Path other) {
         if (!(Objects.requireNonNull(other) instanceof DxPath))
             return false;
         DxPath that = (DxPath)other;
 
         // other path is longer
         if (that.path.length > path.length)
             return false;
 
         int thisOffsetCount = getNameCount();
         int thatOffsetCount = that.getNameCount();
 
         // other path has no name elements
         if (thatOffsetCount == 0 && this.isAbsolute()) {
             return that.isEmpty() ? false : true;
         }
 
         // given path has more elements that this path
         if (thatOffsetCount > thisOffsetCount)
             return false;
 
         // same number of elements so must be exact match
         if ((thatOffsetCount == thisOffsetCount) &&
                 (path.length != that.path.length)) {
             return false;
         }
 
         // check offsets of elements match
         for (int i=0; i<thatOffsetCount; i++) {
             Integer o1 = offsets[i];
             Integer o2 = that.offsets[i];
             if (!o1.equals(o2))
                 return false;
         }
 
         // offsets match so need to compare bytes
         int i=0;
         while (i < that.path.length) {
             if (this.path[i] != that.path[i])
                 return false;
             i++;
         }
 
         // final check that match is on name boundary
         if (i < path.length && this.path[i] != '/')
             return false;
 
         return true;
     }
 
 
     @Override
     public boolean endsWith(Path other) {
         if (!(Objects.requireNonNull(other) instanceof DxPath))
             return false;
         DxPath that = (DxPath)other;
 
         int thisLen = path.length;
         int thatLen = that.path.length;
 
         // other path is longer
         if (thatLen > thisLen)
             return false;
 
         // other path is the empty path
         if (thisLen > 0 && thatLen == 0)
             return false;
 
         // other path is absolute so this path must be absolute
         if (that.isAbsolute() && !this.isAbsolute())
             return false;
 
         int thisOffsetCount = getNameCount();
         int thatOffsetCount = that.getNameCount();
 
         // given path has more elements that this path
         if (thatOffsetCount > thisOffsetCount) {
             return false;
         } else {
             // same number of elements
             if (thatOffsetCount == thisOffsetCount) {
                 if (thisOffsetCount == 0)
                     return true;
                 int expectedLen = thisLen;
                 if (this.isAbsolute() && !that.isAbsolute())
                     expectedLen--;
                 if (thatLen != expectedLen)
                     return false;
             } else {
                 // this path has more elements so given path must be relative
                 if (that.isAbsolute())
                     return false;
             }
         }
 
         // compare bytes
         int thisPos = offsets[thisOffsetCount - thatOffsetCount];
         int thatPos = that.offsets[0];
         if ((thatLen - thatPos) != (thisLen - thisPos))
             return false;
         while (thatPos < thatLen) {
             if (this.path[thisPos++] != that.path[thatPos++])
                 return false;
         }
 
         return true;
     }
 
     @Override
     public DxPath normalize() {
         final int count = getNameCount();
         if (count == 0)
             return this;
 
         boolean[] ignore = new boolean[count];      // true => ignore name
         int[] size = new int[count];                // length of name
         int remaining = count;                      // number of names remaining
         boolean hasDotDot = false;                  // has at least one ..
         boolean isAbsolute = isAbsolute();
 
         // first pass:
         //   1. compute length of names
         //   2. mark all occurences of "." to ignore
         //   3. and look for any occurences of ".."
         for (int i=0; i<count; i++) {
             int begin = offsets[i];
             int len;
             if (i == (offsets.length-1)) {
                 len = path.length - begin;
             } else {
                 len = offsets[i+1] - begin - 1;
             }
             size[i] = len;
 
             if (path[begin] == '.') {
                 if (len == 1) {
                     ignore[i] = true;  // ignore  "."
                     remaining--;
                 }
                 else {
                     if (path[begin+1] == '.')   // ".." found
                         hasDotDot = true;
                 }
             }
         }
 
         // multiple passes to eliminate all occurences of name/..
         if (hasDotDot) {
             int prevRemaining;
             do {
                 prevRemaining = remaining;
                 int prevName = -1;
                 for (int i=0; i<count; i++) {
                     if (ignore[i])
                         continue;
 
                     // not a ".."
                     if (size[i] != 2) {
                         prevName = i;
                         continue;
                     }
 
                     int begin = offsets[i];
                     if (path[begin] != '.' || path[begin+1] != '.') {
                         prevName = i;
                         continue;
                     }
 
                     // ".." found
                     if (prevName >= 0) {
                         // name/<ignored>/.. found so mark name and ".." to be
                         // ignored
                         ignore[prevName] = true;
                         ignore[i] = true;
                         remaining = remaining - 2;
                         prevName = -1;
                     } else {
                         // Case: /<ignored>/.. so mark ".." as ignored
                         if (isAbsolute) {
                             boolean hasPrevious = false;
                             for (int j=0; j<i; j++) {
                                 if (!ignore[j]) {
                                     hasPrevious = true;
                                     break;
                                 }
                             }
                             if (!hasPrevious) {
                                 // all proceeding names are ignored
                                 ignore[i] = true;
                                 remaining--;
                             }
                         }
                     }
                 }
             } while (prevRemaining > remaining);
         }
 
         // no redundant names
         if (remaining == count)
             return this;
 
         // corner case - all names removed
         if (remaining == 0) {
             return isAbsolute ? getFileSystem().rootDirectory() : emptyPath();
         }
 
         // compute length of result
         int len = remaining - 1;
         if (isAbsolute)
             len++;
 
         for (int i=0; i<count; i++) {
             if (!ignore[i])
                 len += size[i];
         }
         byte[] result = new byte[len];
 
         // copy names into result
         int pos = 0;
         if (isAbsolute)
             result[pos++] = '/';
         for (int i=0; i<count; i++) {
             if (!ignore[i]) {
                 System.arraycopy(path, offsets[i], result, pos, size[i]);
                 pos += size[i];
                 if (--remaining > 0) {
                     result[pos++] = '/';
                 }
             }
         }
 
         return new DxPath(getFileSystem(), result, type, fileId, attributes);
     }
 
 
     @Override
     public DxPath resolve(Path obj) {
         byte[] other = toDxPath(obj).path;
         if (other.length > 0 && other[0] == '/')
             return ((DxPath)obj);
         byte[] result = resolve(path, other);
         return new DxPath(getFileSystem(), result);
     }
 
     DxPath resolve(byte[] other) {
         return resolve(new DxPath(getFileSystem(), other));
     }
 
     // Resolve child against given base
     private static byte[] resolve(byte[] base, byte[] child) {
         int baseLength = base.length;
         int childLength = child.length;
         if (childLength == 0)
             return base;
         if (baseLength == 0 || child[0] == '/')
             return child;
         byte[] result;
         if (baseLength == 1 && base[0] == '/') {
             result = new byte[childLength + 1];
             result[0] = '/';
             System.arraycopy(child, 0, result, 1, childLength);
         } else {
             result = new byte[baseLength + 1 + childLength];
             System.arraycopy(base, 0, result, 0, baseLength);
             result[base.length] = '/';
             System.arraycopy(child, 0, result, baseLength+1, childLength);
         }
         return result;
     }
 
     @Override
     public DxPath relativize(Path obj) {
         DxPath other = toDxPath(obj);
         if (other.equals(this))
             return emptyPath();
 
         // can only relativize paths of the same type
         if (this.isAbsolute() != other.isAbsolute())
             throw new IllegalArgumentException("'other' is different type of Path");
 
         // this path is the empty path
         if (this.isEmpty())
             return other;
 
         int bn = this.getNameCount();
         int cn = other.getNameCount();
 
         // skip matching names
         int n = (bn > cn) ? cn : bn;
         int i = 0;
         while (i < n) {
             if (!this.getName(i).equals(other.getName(i)))
                 break;
             i++;
         }
 
         int dotdots = bn - i;
         if (i < cn) {
             // remaining name components in other
             DxPath remainder = other.subpath(i, cn);
             if (dotdots == 0)
                 return remainder;
 
             // other is the empty path
             boolean isOtherEmpty = other.isEmpty();
 
             // result is a  "../" for each remaining name in base
             // followed by the remaining names in other. If the remainder is
             // the empty path then we don't add the final trailing slash.
             int len = dotdots*3 + remainder.path.length;
             if (isOtherEmpty) {
                 assert remainder.isEmpty();
                 len--;
             }
             byte[] result = new byte[len];
             int pos = 0;
             while (dotdots > 0) {
                 result[pos++] = (byte)'.';
                 result[pos++] = (byte)'.';
                 if (isOtherEmpty) {
                     if (dotdots > 1) result[pos++] = (byte)'/';
                 } else {
                     result[pos++] = (byte)'/';
                 }
                 dotdots--;
             }
             System.arraycopy(remainder.path, 0, result, pos, remainder.path.length);
             return new DxPath(getFileSystem(), result, type, fileId, attributes);
         } else {
             // no remaining names in other so result is simply a sequence of ".."
             byte[] result = new byte[dotdots*3 - 1];
             int pos = 0;
             while (dotdots > 0) {
                 result[pos++] = (byte)'.';
                 result[pos++] = (byte)'.';
                 // no tailing slash at the end
                 if (dotdots > 1)
                     result[pos++] = (byte)'/';
                 dotdots--;
             }
             return new DxPath(getFileSystem(), result, type, fileId, attributes);
         }
     }
 
 
     // Checks that the given file is a UnixPath
     static DxPath toDxPath(Path obj) {
         if (obj == null)
             throw new NullPointerException();
         if (!(obj instanceof DxPath))
             throw new TypeMismatchException(); //ProviderMismatchException();
         return (DxPath)obj;
     }
 
     @Override
     public URI toUri() {
         return DxUriUtils.toUri(this);
     }
 
 
     // use this message when throwing exceptions
     String getPathForExceptionMessage() {
         return toString();
     }
 
     @Override
     public DxPath toAbsolutePath() {
 
         if (isAbsolute()) {
             return this;
         }
 
         return getFileSystem().defaultDirectory().resolve(path);
     }
 
 
     @Override
     public  Path toRealPath(LinkOption... options) throws IOException {
 
         checkRead();
 
         DxPath absolute = toAbsolutePath();
 
         // if not resolving links then eliminate "." and also ".."
         // where the previous element is not a link.
         DxPath result = fs.rootDirectory();
         for (int i=0; i<absolute.getNameCount(); i++) {
             DxPath element = absolute.getName(i);
 
             // eliminate "."
             if ((element.asByteArray().length == 1) && (element.asByteArray()[0] == '.'))
                 continue;
 
             // cannot eliminate ".." if previous element is a link
             if ((element.asByteArray().length == 2) && (element.asByteArray()[0] == '.') && (element.asByteArray()[1] == '.'))
             {
                 continue;
             }
             result = result.resolve(element);
         }
 
         // check file exists (without following links)
         // TODO
 
 //        try {
 //            UnixFileAttributes.get(result, false);
 //        } catch (UnixException x) {
 //            x.rethrowAsIOException(result);
 //        }
         return result;
     }
 
     // TODO checkRead
     void checkRead() {
 
 //        SecurityManager sm = System.getSecurityManager();
 //        if (sm != null)
 //            sm.checkRead(getPathForPermissionCheck());
     }
 
 //    // use this path for permission checks
 //    String getPathForPermissionCheck() {
 //        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
 //            return new String(getByteArrayForSysCalls());
 //        } else {
 //            return toString();
 //        }
 //    }
 
 //    // use this path when making system/library calls
 //    byte[] getByteArrayForSysCalls() {
 //        // resolve against default directory if required (chdir allowed or
 //        // file system default directory is not working directory)
 //        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
 //            return resolve(getFileSystem().defaultDirectory(), path);
 //        } else {
 //            if (!isEmpty()) {
 //                return path;
 //            } else {
 //                // empty path case will access current directory
 //                byte[] here = { '.' };
 //                return here;
 //            }
 //        }
 //    }
 
 
 
     @Override
     public int compareTo(Path other) {
         int len1 = path.length;
         int len2 = ((DxPath) other).path.length;
 
         int n = Math.min(len1, len2);
         byte v1[] = path;
         byte v2[] = ((DxPath) other).path;
 
         int k = 0;
         while (k < n) {
             int c1 = v1[k] & 0xff;
             int c2 = v2[k] & 0xff;
             if (c1 != c2) {
                 return c1 - c2;
             }
             k++;
         }
         return len1 - len2;
     }
 
     @Override
     public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
         throw new UnsupportedOperationException();
     }
 
 }
 
