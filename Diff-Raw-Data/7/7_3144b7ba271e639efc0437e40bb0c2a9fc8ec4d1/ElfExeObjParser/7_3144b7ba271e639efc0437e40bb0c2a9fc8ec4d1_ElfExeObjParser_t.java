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
import edu.syr.bytecast.interfaces.fsys.*;
 import java.io.*;
 import java.util.*;
 
 public class ElfExeObjParser implements IBytecastFsys {
 
     //Constructor
     public ElfExeObjParser()
     {
         m_preferSections = false;
     }
     
     //Constructor
     public ElfExeObjParser(boolean prefer_sections)
     {
         m_preferSections = prefer_sections;
     }
     
     //Sets the file path to parse
     @Override
     public void setFilepath(String file_path) {
         m_filePath = file_path;
     }
     
     //Gets the file path to parse
     @Override
     public String getFilepath() {
         return m_filePath;
     }
 
     //Determines whether sections or segments are desired fro an OBJ file
     //based on what's available and what view is preferred.
     private boolean wantSections(int num_sections, int num_segments) throws IOException
     {
         boolean ret = false;
         //return true if we prefer sections and we have them
         if(num_sections > 0 && m_preferSections)
             ret = true;
         //or if we have no segments
         if(num_sections > 0 && num_segments == 0)
             ret = true;
         
         if(num_sections <= 0 && num_segments <= 0)
             throw new IOException("Elf file contains no executable code"
                     + "(section header and segment header sizes are zero).");
         
         return ret;
     }
   
     
     //Public "parse" function.. parses the specified ELF file.
     @Override
     public ExeObj parse() throws Exception {
         ElfFileParser elf_file_parser = new ElfFileParser();
         elf_file_parser.setFilePath(m_filePath);
         ExeObj ret = new ExeObj();
         
         //try to open the ELF file
         if(elf_file_parser.attach())
         {
             //Pull the ELF header
             ElfElfHeaderStruct elf_header = elf_file_parser.getElfHeader();
             ret.setEntryPointIndex(elf_header.e_entry);
             //Pull either the sections or segments
             if(wantSections(elf_header.e_shnum, elf_header.e_phnum))
             {
                 
                 //Get the section header and sections
                 ElfSectionHeaderStruct sect_header = elf_file_parser.getSectionHeader();
                 
                 //NOTE: We can probably save memory by reading the sections as
                 //we need them rather than all at once.
                 List<List<Byte>> sections = elf_file_parser.getSections();
                 
                 //Generate the ExeObj segments from the sections
                 ret.setSegments(generateSegments(sect_header,elf_file_parser,elf_header.e_shstrndx));
                 
             }
             else
             {
                 //Get the segment header and sections.
                 ElfProgramHeaderStruct prog_header = elf_file_parser.getProgramHeader();
                 
                 
                 //NOTE: We can probably save memory by reading the segments as
                 //we need them rather than all at once.
                 List<List<Byte>> segments = elf_file_parser.getSegments();
                 
                 //Generate the ExeObj segments from the ELF segments
                 ret.setSegments(generateSegments(prog_header,elf_file_parser));
 
                 //ret.setDependencies(resolveDependencies(prog_header, elf_file_parser, ret, elf_header.e_ident[4]));
             }
             
         }
         else
         {
             throw new FileNotFoundException(m_filePath + " could not be opened.");
         }
        
         return ret;
     }
 
     //Determines if a section is executable base on the eh_flags.
     private boolean isExecutableSection(long flags)
     {
         boolean ret = false;
         long mask = ElfSectionHeaderEntryStruct.SHF_ALLOC;
         
         if((flags & mask) != 0)
             ret = true;
         
         return ret;
     }
     
     //Determines if a segment is executable base on the p_type
     private boolean isExecutableSegment(int type)
     {
         boolean ret = false;
         if(type == ElfProgramHeaderEntryStruct.PT_LOAD)
             ret = true;
         
         return ret;
     }
     
     //Generates a label for the segment.
     private String getLabel(List<Byte> string_table, int string_id, int sh_index)
     {
         
         String ret = m_filePath + "_" + 
                      sh_index;
         
         if(string_id != -1 && string_table != null)
             ret += "_" + 
                    BytecastFsysUtil.parseStringFromBytes(string_table, string_id);
         
         return ret;
             
     }
     
     //Generates Segments from Section Headers.
     private List<ExeObjSegment> generateSegments(ElfSectionHeaderStruct sect_header,
                                                  ElfFileParser file_parser,
                                                  int sh_strtab_idx) throws IOException
     {
         List<ExeObjSegment> ret = new ArrayList<ExeObjSegment>(); 
         
         List<Byte> sh_string_table = file_parser.getSection(sh_strtab_idx);
         
         
         //Now find executable sections.
         for(int i = 0; i < sect_header.m_headerEntries.size(); i++)
         {
             ElfSectionHeaderEntryStruct entry = sect_header.m_headerEntries.get(i);
             if(isExecutableSection(entry.sh_flags))
             {
                 ExeObjSegment exe_seg = new ExeObjSegment();
                 exe_seg.setLabel(getLabel(sh_string_table, entry.sh_name, i));
                 exe_seg.setBytes(file_parser.getSection(i));
                 exe_seg.setStartAddress(entry.sh_addr);
                 ret.add(exe_seg);
             }
         }
         return ret;
     }
     
     //Generate the segments from the program header and segments.
     private List<ExeObjSegment> generateSegments(ElfProgramHeaderStruct prog_header,
                                                  ElfFileParser file_parser) throws IOException
     {
         List<ExeObjSegment> ret = new ArrayList<ExeObjSegment>();
         
         for(int i = 0; i < prog_header.m_headerEntries.size(); i++)
         {
             ElfProgramHeaderEntryStruct entry = prog_header.m_headerEntries.get(i);
             if(isExecutableSegment(entry.p_type))
             {
                 ExeObjSegment exe_seg = new ExeObjSegment();
                 exe_seg.setLabel(getLabel(null, -1, i));
                 exe_seg.setBytes(file_parser.getSegment(i));
                 exe_seg.setStartAddress(entry.p_vaddr);
                 ret.add(exe_seg);               
             }
         }
         
         return ret;
     }
 
 //    
 //    public List<ExeObjDependency> resolveDependencies(ElfProgramHeaderStruct prog_header, 
 //                                                      ElfFileParser file_parser, 
 //                                                      ExeObj exe_obj,
 //                                                      int arch) throws Exception
 //    {
 //        for(int i = 0; i < prog_header.m_headerEntries.size(); i++)
 //        {
 //            ElfProgramHeaderEntryStruct entry = prog_header.m_headerEntries.get(i);
 //            
 //            if(entry.p_type ==  entry.PT_DYNAMIC)
 //            {
 //                ElfDynamicTableParser dt_parser = new ElfDynamicTableParser(arch);
 //                ElfDynamicTableStruct dt = dt_parser.parse(file_parser.getSegment(i));
 //                return resolveDependencies(dt, exe_obj);
 //            }
 //        }
 //        
 //        return new ArrayList<ExeObjDependency>();
 //    }
 //    
 //    public List<ExeObjDependency> resolveDependencies(ElfDynamicTableStruct dt, ExeObj exe_obj) throws Exception
 //    {
 //        long str_tab_offset = 0;
 //        long str_tab_size = 0;
 //        List<ExeObjDependency> ret = new ArrayList<ExeObjDependency>();
 //        for(int i = 0; i < dt.m_headerEntries.size(); i++)
 //        {
 //            ElfDynamicTableEntryStruct entry = dt.m_headerEntries.get(i);
 //            if(entry.d_tag == entry.DT_STRTAB)
 //            {
 //                str_tab_offset = entry.d_ptr;
 //            }
 //            
 //            if(entry.d_tag == entry.DT_STRSZ)
 //            {
 //                str_tab_size = entry.d_val;
 //            }
 //        }
 //        
 //        List<Byte> string_table = exe_obj.getBytesFromAddr(str_tab_offset, (int)str_tab_size);
 //        
 //        for(int i = 0; i < dt.m_headerEntries.size(); i++)
 //        {
 //            ElfDynamicTableEntryStruct entry = dt.m_headerEntries.get(i);
 //            if(entry.d_tag == entry.DT_NEEDED)
 //            {
 //                ExeObjDependency dep = new ExeObjDependency();
 //                String depName = BytecastFsysUtil.parseStringFromBytes(string_table, (int)entry.d_ptr);
 //                dep.setDependencyName(depName);
 //                ret.add(dep);
 //            }           
 //        }
 //        
 //        return ret;
 //    }
     
 
     public static void main(String args[]) {
         ElfExeObjParser elf_parser = new ElfExeObjParser(false);
         //elf_parser.setFilepath("/home/adodds/code/bytecast-fsys/documents/testcase1_input_files/libc.so.6");
         //elf_parser.setFilepath("/lib32/libc.so.6");
 
         //elf_parser.setFilepath("../../documents/testcase1_input_files/a.out.static");
         elf_parser.setFilepath("../../documents/testcase1_input_files/a.out");
         try {
             ExeObj exeObj = elf_parser.parse();
            ExeObjIOUtils.printExeObj(exeObj);
         } catch (FileNotFoundException e) {
             System.out.println("Could not parse file.");
         } catch (Exception e) {
             System.out.println(e.getLocalizedMessage());
         }
     }
 
     private String m_filePath;
     private boolean m_preferSections;
 }
