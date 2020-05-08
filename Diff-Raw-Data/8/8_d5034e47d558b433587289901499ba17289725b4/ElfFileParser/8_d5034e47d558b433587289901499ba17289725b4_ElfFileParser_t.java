 /*
  * This file is part of Bytecast.
  *
  * Bytecast is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Bytecast is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Bytecast.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package edu.syr.bytecast.fsys.elf;
 
 import edu.syr.bytecast.fsys.*;
 import java.io.IOException;
 import java.util.*;
 
 public class ElfFileParser {
 
     private ElfElfHeaderStruct m_elfHeader;
     private BytecastFileReader m_bytecastFileReader;
     private ElfProgramHeaderStruct m_programHeader;
     private ElfSectionHeaderStruct m_sectionHeader;
     //constructor
 
     ElfFileParser() {
         m_bytecastFileReader = new BytecastFileReader();
         m_elfHeader = null;
         m_programHeader = null;
         m_sectionHeader = null;
     }
 
     //set file path of binary data
     public void setFilePath(String filePath) {
         m_bytecastFileReader.setFilepath(filePath);
     }
 
     //get file path of binary data
     public String getFilePath() {
         return m_bytecastFileReader.getFilepath();
     }
 
     //open file 
     public boolean attach() throws IOException {
         if (m_bytecastFileReader.openFile()) {
             return true;
         } else {
             throw new IOException("Error opening file.");
         }
 
     }
     //close the file.
 
     public boolean detach() throws IOException {
         if (m_bytecastFileReader.closeFile()) {
             return true;
         } else {
             throw new IOException("Error closing file.");
         }
     }
 
     public ElfElfHeaderStruct getElfHeader() throws IOException {
         List<Byte> bin_elf_file_header;
         //read in the first 64 bytes and put it in a byte array
         bin_elf_file_header = m_bytecastFileReader.getContents(0, 64);
         ElfElfHeaderParser file_header_parser = new ElfElfHeaderParser();
         //parse the elf header.
         file_header_parser.setBinaryData(bin_elf_file_header);
         file_header_parser.parse();
         m_elfHeader = file_header_parser.getElfMainHeader();
 
         return m_elfHeader;
     }
 
     public ElfProgramHeaderStruct getProgramHeader() throws IOException {
         //Parse the program header
         long ph_offset = m_elfHeader.e_phoff; //Program header offset
         //Size of program headers and Number of Program headers
         int ph_read_amount = m_elfHeader.e_phentsize * m_elfHeader.e_phnum;
 
         List<Byte> bin_elf_prog_header;
         bin_elf_prog_header = m_bytecastFileReader.getContents(ph_offset, ph_read_amount);
         //use e_ident field to find out if its 64 or 32 bit elf file. 
         ElfProgramHeaderParser phparser = new ElfProgramHeaderParser(m_elfHeader.e_ident[4]);
         m_programHeader = phparser.parse(bin_elf_prog_header);
         
         return m_programHeader;
     }
 
     public ElfSectionHeaderStruct getSectionHeader() throws IOException {
         //Parse the section headers.
         long sh_offset = m_elfHeader.e_shoff; //Address of the section headers
         //size of the section headers and the number of section headers.
         int read_amount = m_elfHeader.e_shentsize * m_elfHeader.e_shnum;
 
         List<Byte> bin_elf_sec_header;
         bin_elf_sec_header = m_bytecastFileReader.getContents(sh_offset, read_amount);
         //use e_ident field to find out if its 64 or 32 bit elf file.
         ElfSectionHeaderParser shparser = new ElfSectionHeaderParser(m_elfHeader.e_ident[4]);
         //parse the section headers.
         m_sectionHeader = shparser.parseHeader(bin_elf_sec_header);
 
         return m_sectionHeader;
     }
 
     public List<List<Byte>> getSections() throws IOException {
         List<List<Byte>> ret = new ArrayList();
         //pull out the section data defined by the program headers.
         for (int i = 0; i < m_sectionHeader.m_headerEntries.size(); i++) {
 
             ret.add(getSection(i));
 
         }
         return ret;
     }
 
     public List<Byte> getSection(int index) throws IOException {
         if (index < 0 || index > m_sectionHeader.m_headerEntries.size()) {
             throw new IOException("Segment index out of bounds.");
         } else {
             List<Byte> ret = new ArrayList<Byte>();
 
             //segment address in file
             long section_offset = m_sectionHeader.m_headerEntries.get(index).sh_offset;
             //segment size in file
             long section_size = m_sectionHeader.m_headerEntries.get(index).sh_size;
 
             //there is no section data for this section header so add null.
             if (section_size != 0) {
                 ret = m_bytecastFileReader.getContents(section_offset, (int) section_size);
             }
             return ret;
         }
     }
 
     public List<List<Byte>> getSegments() throws IOException {
         List<List<Byte>> ret = new ArrayList();
         //pull out the segment data defined by the program headers.
         for (int i = 0; i < m_programHeader.m_headerEntries.size(); i++) {
             ret.add(getSegment(i));
         }
 
         return ret;
 
     }
 
     public List<Byte> getSegment(int index) throws IOException {
         List<Byte> ret = new ArrayList<Byte>();
 
         //segment address in file
         long segment_offset = m_programHeader.m_headerEntries.get(index).p_offset;
         //segment size in file
         long segment_size = m_programHeader.m_headerEntries.get(index).p_filesz;
 
         //there is no segment data for this segment header so add null.
         if (segment_size != 0) {
             ret = m_bytecastFileReader.getContents(segment_offset, (int) segment_size);
         }
 
         return ret;
     }
 
     public List<Byte> getBytes(long offset, int size) throws IOException
     {
         return m_bytecastFileReader.getContents(offset,size);
     }
     
     public List<Byte> getBytesVAddr(long vAddr,int size) throws IOException
     {
         //convert a virtual address from a program header to a file offset.
         for(int i = 0; i < m_programHeader.m_headerEntries.size(); i++)
         {
             ElfProgramHeaderEntryStruct phs = m_programHeader.m_headerEntries.get(i);
             
             if(vAddr >= phs.p_vaddr && vAddr < phs.p_vaddr + phs.p_memsz)
             {
                 return m_bytecastFileReader.getContents(vAddr - phs.p_vaddr + phs.p_offset,size);
             }
         }
        return null; 
     }
     
     public List<Byte> getMainStringTable() throws IOException
     {
         //get the section headers if it is null. This will
         //allow us to pull out the string table when the program
         //headers are trying to be used.
         if(m_sectionHeader == null)
         {
             getSectionHeader();
         }
         
         List <Byte> sh_str_tab = getSectionStringTable();
         
         //find the main string table. This is done by finding the string section that 
         //doesn't have the elf header string table index. 
         for(int i = 0; i < m_sectionHeader.m_headerEntries.size();i++)
         {
             ElfSectionHeaderEntryStruct entry = m_sectionHeader.m_headerEntries.get(i);
             if(BytecastFsysUtil.parseStringFromBytes(sh_str_tab, entry.sh_name).equals(".strtab"))
             {
                 return getBytes(entry.sh_offset, (int)entry.sh_size);
             }
         }
         return null;
     }   
     
     public List<Byte> getSectionStringTable() throws IOException
     { 
         //get the section headers if it is null. This will
         //allow us to pull out the string table when the program
         //headers are trying to be used.
         if(m_sectionHeader == null)
         {
             getSectionHeader();
         }
         // pull out the section string table.
         return getBytes(m_sectionHeader.m_headerEntries.get(m_elfHeader.e_shstrndx).sh_offset,
                         (int)m_sectionHeader.m_headerEntries.get(m_elfHeader.e_shstrndx).sh_size);
     }
         
 
     public ElfSymbolTableStruct getSymTable() throws IOException
     {
         //get the section headers if it is null. This will
         //allow us to pull out the string table when the program
         //headers are trying to be used.
         if(m_sectionHeader == null)
         {
             getSectionHeader();
         }
         
         //find the with the symbol table type and parse the that section
         for(int i = 0; i < m_sectionHeader.m_headerEntries.size(); i++)
         {
            ElfSectionHeaderEntryStruct she = m_sectionHeader.m_headerEntries.get(i);
            if(she.sh_type == ElfSectionHeaderEntryStruct.SHT_SYMTAB) 
            {
                ElfSymbolTableParser pst = new ElfSymbolTableParser(m_elfHeader.e_ident[4]);
                return pst.parse(getBytes(she.sh_offset,(int)she.sh_size));
            }
        }
         
         return null;
     }
     
     public ElfSectionHeaderEntryStruct getSectionHeaderEntry(int index)
     {
         return m_sectionHeader.m_headerEntries.get(index);
     }
     
     public ElfProgramHeaderEntryStruct getProgramHeaderEntry(int index)
     {
         return m_programHeader.m_headerEntries.get(index); 
     }
     
     public static void main(String args[]) {
         ElfFileParser elf_parser = new ElfFileParser();
         //elf_parser.setFilepath("/home/adodds/code/bytecast-fsys/documents/testcase1_input_files/libc.so.6");
         //elf_parser.setFilepath("/lib32/libc.so.6");
         elf_parser.setFilePath("../../documents/testcase1_input_files/a.out");
 
         try {
             elf_parser.attach();
         } catch (IOException e) {
             System.out.println("Could not open file.");
         }
 
         try {
             elf_parser.getElfHeader();
         } catch (IOException e) {
            System.out.println("Could not Parse Elf header.");
         }
 
         try {
             elf_parser.getProgramHeader();
         } catch (IOException e) {
            System.out.println("Could not Parse Program header.");
         }
 
         try {
             elf_parser.getSectionHeader();
         } catch (IOException e) {
            System.out.println("Could not Parse Elf header.");
         }
 
         try {
             elf_parser.getSections();
         } catch (IOException e) {
             System.out.println("No section data.");
         }
 
         try {
             elf_parser.getSegments();
         } catch (IOException e) {
             System.out.println("No segment data.");
         }
     }
 }
