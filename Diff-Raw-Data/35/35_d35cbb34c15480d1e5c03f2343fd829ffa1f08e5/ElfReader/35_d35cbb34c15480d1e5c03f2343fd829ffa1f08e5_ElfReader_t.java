 
 package com.github.erasmux.AndLibUtils;
 
 import java.io.*;
 import java.util.*;
 
 public class ElfReader {
 
     private BufferedRandomAccessFile raf_;
     private boolean valid_ = false;
     private int elfClass_ = 0;
     private int elfDataEncoding_ = 0;
     private int elfType_ = 0;
     private int elfMachine_ = 0;
     private long elfVersion_ = 0;
     private long progHdrOfs_ = 0;
     private int progHdrEntrySize_ = 0;
     private int progHdrEntries_ = 0;
     private long sectHdrOfs_ = 0;
     private int sectHdrEntrySize_ = 0;
     private int sectHdrEntries_ = 0;
     private int sectHdrStringTableIndex_ = 0;
 
     static private class SectionHeader {
         public long nameIndex_;
         public long type_;
         public long addr_;
         public long ofs_;
         public long size_;
         public long effSize_;
         public String name_;
     }
 
     private List<SectionHeader> sections_;
     private SectionHeader currentSection_;
 
     public ElfReader(File file, String mode) throws IOException, FileNotFoundException {
         sections_ = new ArrayList<SectionHeader>();
         currentSection_ = null;
         raf_ = new BufferedRandomAccessFile(file, mode);
         readElfHeader();
     }
 
     public boolean valid() {
         return valid_;
     }
 
     public int readUByte() throws IOException {
         return raf_.readUnsignedByte();
     }
 
     public int readUShort() throws IOException {
         return raf_.readUnsignedShort();
     }
 
     public long readUInt() throws IOException {
         return raf_.readUnsignedInt();
     }
 
     public void writeUByte(int v) throws IOException {
         raf_.writeUnsignedByte(v);
     }
 
     public void writeUShort(int v) throws IOException {
         raf_.writeUnsignedShort(v);
     }
 
     public void writeUInt(long v) throws IOException {
         raf_.writeUnsignedInt(v);
     }
 
     public String readString() throws IOException {
         StringBuilder builder = new StringBuilder();
         int next;
         while ( (next=readUByte()) != 0 )
             builder.append((char)next);
         return builder.toString();
     }
 
     public void skip(int bytes) throws IOException {
         raf_.skipBytes(bytes);
     }
 
     public void seek(long ofs) throws IOException {
         raf_.seek(ofs);
         if (currentSection_==null || !offsetInSection(ofs,currentSection_))
             findCurrentSection(ofs);
     }
 
     /// like seek but offset given relative to current location
     public void reseek(long relativeOfs) throws IOException {
         seek(raf_.getFilePointer() + relativeOfs);
     }
 
     public boolean hasSection(String name) {
         return findSection(name)!=null;
     }
 
     public String currentSection() {
         return currentSection_==null ? null : currentSection_.name_;
     }
 
     public void seekSection(String name, long ofs) throws IOException {
         currentSection_ = findSection(name);
         if (currentSection_ != null) 
             seek(currentSection_.ofs_+ofs);
     }
 
     public boolean finishedSection() throws IOException {
         return currentSection_ != null &&
             currentOffsetInSection() >= currentSection_.effSize_;
     }
 
     public long currentOffsetInSection() throws IOException {
         return currentSection_==null ? -1
             : raf_.getFilePointer() - currentSection_.ofs_;
     }
 
     public long sectionAddr(String name) {
         SectionHeader sh = findSection(name);
         return sh==null ? -1 : sh.addr_;
     }
 
     public long sectionOfs2FileOfs(String name, long ofs) {
         SectionHeader sh = findSection(name);
         return sh==null ? -1 : sh.ofs_+ofs;
     }
 
     /// searches the remain of the current section for a null terminated string
     /// which matches the given string.
     /// if it is found, its offset is returned and the file pointer is left right
     /// after it to allow further searches.
     /// if it is not found -1 is return the section is finished.
     public long seekString(String str) throws IOException {
         if (currentSection_ == null)
             return -1;
         // the implementation relies on that we are searching for a null terminate string
         int n = str.length();
         char[] cbuf = new char[n]; // circular buffer
         int length = 0;
         int ind = 0;
         long remainingInSection = currentSection_.effSize_ - currentOffsetInSection();
         while (remainingInSection-- > 0) {
             char ch = (char)readUByte();
             if (ch == 0) { // found null terminator
                 // compare string:
                 if (length >= n) {
                     boolean found=true;
                     int ii=ind; // because cbuf size is n and its circular cbuf[ind]
                                 // is actually the n-th character we last read.
                     for(int jj=0; jj<n; ++jj) {
                         if (str.charAt(jj) != cbuf[ii]) {
                             found=false;
                             break;
                         }
                         ii = (ii+1) % n;
                     }
                     if (found)
                         return currentOffsetInSection()-n-1;
                 }
                 length = 0;
             } else {
                 cbuf[ind] = ch;
                 ind = (ind+1) % n;
                 ++length;
             }
         }
         return -1;
     }
 
     public void readSections() throws IOException {
         seek(sectHdrOfs_);
         for(int ii=0; ii<sectHdrEntries_; ++ii) {
             SectionHeader sh = new SectionHeader();
             sh.nameIndex_ = readUInt();
             sh.type_ = readUInt();
             skip(4);
             sh.addr_ = readUInt();
             sh.ofs_ = readUInt();
             sh.size_ = readUInt();
             skip(sectHdrEntrySize_ - 6*4);
 
             sh.effSize_ = sh.type_==8 ? 0 : sh.size_; // if type==NOBITS effective size=0
             sh.name_ = ""; // just in case...
             sections_.add(sh);
         }
 
         if (sectHdrStringTableIndex_ < sections_.size()) {
             long strTabOfs = sections_.get(sectHdrStringTableIndex_).ofs_;
             for(int ii=0; ii < sections_.size(); ++ii) {
                 SectionHeader sh = sections_.get(ii);
                 seek(strTabOfs + sh.nameIndex_);
                 sh.name_ = readString();
             }
         }
     }
 
     private void readElfHeader() throws IOException {
         // check magic:
         valid_ = readUByte()==0x7F &&
             readUByte()=='E' &&
             readUByte()=='L' &&
             readUByte()=='F';
         if (valid_) {
             elfClass_ = readUByte();
             elfDataEncoding_ = readUByte();
             skip(10);
             elfType_ = readUShort();
             elfMachine_ = readUShort();
             elfVersion_ = readUInt();
             skip(4);
             progHdrOfs_ = readUInt();
             sectHdrOfs_ = readUInt();
             skip(6);
             progHdrEntrySize_ = readUShort();
             progHdrEntries_ = readUShort();
             sectHdrEntrySize_ = readUShort();
             sectHdrEntries_ = readUShort();
             sectHdrStringTableIndex_ = readUShort();
         }
     }
 
     private SectionHeader findSection(String name) {
         for(int ii=0; ii < sections_.size(); ++ii) {
             SectionHeader sh = sections_.get(ii);
             if (sh.name_.equals(name))
                 return sh;
         }
         return null;
     }
 
     private static boolean offsetInSection(long ofs, SectionHeader sh) {
         return ofs >= sh.ofs_ && ofs < sh.ofs_+sh.size_;
     }
 
     private void findCurrentSection(long ofs) {
         for(int ii=0; ii < sections_.size(); ++ii) {
             SectionHeader sh = sections_.get(ii);
             if (offsetInSection(ofs,sh)) {
                 currentSection_ = sh;
                 return;
             }
         }
         currentSection_ = null;
     }

    public void close() throws IOException {
        raf_.close();
    }

 }
