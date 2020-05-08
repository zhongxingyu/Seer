 package pdflicense.gui;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Event;
 import java.awt.BorderLayout;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.KeyStroke;
 import java.awt.Point;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JFrame;
 import javax.swing.JDialog;
 import java.awt.Dimension;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import java.awt.GridBagConstraints;
 import javax.swing.JComboBox;
 import javax.swing.JButton;
 import javax.swing.BoxLayout;
 import java.awt.GridLayout;
 import java.io.IOException;
 
 import com.lowagie.text.DocumentException;
 import pdflicense.*;
 import java.awt.GridBagLayout;
 import javax.swing.JOptionPane;
 import java.lang.String;
 
 public class MainWin {
 	
 	private static PdfLicenseManager manager = null;
 	
 	private final String initialMessage = "Please select a file (use File / Open)<br><br>";
 	
 	private final String gpl =
 		"		    GNU GENERAL PUBLIC LICENSE\n" +
 		"		       Version 2, June 1991\n" +
 		"\n" +
 		" Copyright (C) 1989, 1991 Free Software Foundation, Inc.,\n" +
 		" 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA\n" +
 		" Everyone is permitted to copy and distribute verbatim copies\n" +
 		" of this license document, but changing it is not allowed.\n" +
 		"\n" +
 		"			    Preamble\n" +
 		"\n" +
 		"  The licenses for most software are designed to take away your\n" +
 		"freedom to share and change it.  By contrast, the GNU General Public\n" +
 		"License is intended to guarantee your freedom to share and change free\n" +
 		"software--to make sure the software is free for all its users.  This\n" +
 		"General Public License applies to most of the Free Software\n" +
 		"Foundation's software and to any other program whose authors commit to\n" +
 		"using it.  (Some other Free Software Foundation software is covered by\n" +
 		"the GNU Lesser General Public License instead.)  You can apply it to\n" +
 		"your programs, too.\n" +
 		"\n" +
 		"  When we speak of free software, we are referring to freedom, not\n" +
 		"price.  Our General Public Licenses are designed to make sure that you\n" +
 		"have the freedom to distribute copies of free software (and charge for\n" +
 		"this service if you wish), that you receive source code or can get it\n" +
 		"if you want it, that you can change the software or use pieces of it\n" +
 		"in new free programs; and that you know you can do these things.\n" +
 		"\n" +
 		"  To protect your rights, we need to make restrictions that forbid\n" +
 		"anyone to deny you these rights or to ask you to surrender the rights.\n" +
 		"These restrictions translate to certain responsibilities for you if you\n" +
 		"distribute copies of the software, or if you modify it.\n" +
 		"\n" +
 		"  For example, if you distribute copies of such a program, whether\n" +
 		"gratis or for a fee, you must give the recipients all the rights that\n" +
 		"you have.  You must make sure that they, too, receive or can get the\n" +
 		"source code.  And you must show them these terms so they know their\n" +
 		"rights.\n" +
 		"\n" +
 		"  We protect your rights with two steps: (1) copyright the software, and\n" +
 		"(2) offer you this license which gives you legal permission to copy,\n" +
 		"distribute and/or modify the software.\n" +
 		"\n" +
 		"  Also, for each author's protection and ours, we want to make certain\n" +
 		"that everyone understands that there is no warranty for this free\n" +
 		"software.  If the software is modified by someone else and passed on, we\n" +
 		"want its recipients to know that what they have is not the original, so\n" +
 		"that any problems introduced by others will not reflect on the original\n" +
 		"authors' reputations.\n" +
 		"\n" +
 		"  Finally, any free program is threatened constantly by software\n" +
 		"patents.  We wish to avoid the danger that redistributors of a free\n" +
 		"program will individually obtain patent licenses, in effect making the\n" +
 		"program proprietary.  To prevent this, we have made it clear that any\n" +
 		"patent must be licensed for everyone's free use or not licensed at all.\n" +
 		"\n" +
 		"  The precise terms and conditions for copying, distribution and\n" +
 		"modification follow.\n" +
 		"\n" +
 		"		    GNU GENERAL PUBLIC LICENSE\n" +
 		"   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION\n" +
 		"\n" +
 		"  0. This License applies to any program or other work which contains\n" +
 		"a notice placed by the copyright holder saying it may be distributed\n" +
 		"under the terms of this General Public License.  The \"Program\", below,\n" +
 		"refers to any such program or work, and a \"work based on the Program\"\n" +
 		"means either the Program or any derivative work under copyright law:\n" +
 		"that is to say, a work containing the Program or a portion of it,\n" +
 		"either verbatim or with modifications and/or translated into another\n" +
 		"language.  (Hereinafter, translation is included without limitation in\n" +
 		"the term \"modification\".)  Each licensee is addressed as \"you\".\n" +
 		"\n" +
 		"Activities other than copying, distribution and modification are not\n" +
 		"covered by this License; they are outside its scope.  The act of\n" +
 		"running the Program is not restricted, and the output from the Program\n" +
 		"is covered only if its contents constitute a work based on the\n" +
 		"Program (independent of having been made by running the Program).\n" +
 		"Whether that is true depends on what the Program does.\n" +
 		"\n" +
 		"  1. You may copy and distribute verbatim copies of the Program's\n" +
 		"source code as you receive it, in any medium, provided that you\n" +
 		"conspicuously and appropriately publish on each copy an appropriate\n" +
 		"copyright notice and disclaimer of warranty; keep intact all the\n" +
 		"notices that refer to this License and to the absence of any warranty;\n" +
 		"and give any other recipients of the Program a copy of this License\n" +
 		"along with the Program.\n" +
 		"\n" +
 		"You may charge a fee for the physical act of transferring a copy, and\n" +
 		"you may at your option offer warranty protection in exchange for a fee.\n" +
 		"\n" +
 		"  2. You may modify your copy or copies of the Program or any portion\n" +
 		"of it, thus forming a work based on the Program, and copy and\n" +
 		"distribute such modifications or work under the terms of Section 1\n" +
 		"above, provided that you also meet all of these conditions:\n" +
 		"\n" +
 		"    a) You must cause the modified files to carry prominent notices\n" +
 		"    stating that you changed the files and the date of any change.\n" +
 		"\n" +
 		"    b) You must cause any work that you distribute or publish, that in\n" +
 		"    whole or in part contains or is derived from the Program or any\n" +
 		"    part thereof, to be licensed as a whole at no charge to all third\n" +
 		"    parties under the terms of this License.\n" +
 		"\n" +
 		"    c) If the modified program normally reads commands interactively\n" +
 		"    when run, you must cause it, when started running for such\n" +
 		"    interactive use in the most ordinary way, to print or display an\n" +
 		"    announcement including an appropriate copyright notice and a\n" +
 		"    notice that there is no warranty (or else, saying that you provide\n" +
 		"    a warranty) and that users may redistribute the program under\n" +
 		"    these conditions, and telling the user how to view a copy of this\n" +
 		"    License.  (Exception: if the Program itself is interactive but\n" +
 		"    does not normally print such an announcement, your work based on\n" +
 		"    the Program is not required to print an announcement.)\n" +
 		"\n" +
 		"These requirements apply to the modified work as a whole.  If\n" +
 		"identifiable sections of that work are not derived from the Program,\n" +
 		"and can be reasonably considered independent and separate works in\n" +
 		"themselves, then this License, and its terms, do not apply to those\n" +
 		"sections when you distribute them as separate works.  But when you\n" +
 		"distribute the same sections as part of a whole which is a work based\n" +
 		"on the Program, the distribution of the whole must be on the terms of\n" +
 		"this License, whose permissions for other licensees extend to the\n" +
 		"entire whole, and thus to each and every part regardless of who wrote it.\n" +
 		"\n" +
 		"Thus, it is not the intent of this section to claim rights or contest\n" +
 		"your rights to work written entirely by you; rather, the intent is to\n" +
 		"exercise the right to control the distribution of derivative or\n" +
 		"collective works based on the Program.\n" +
 		"\n" +
 		"In addition, mere aggregation of another work not based on the Program\n" +
 		"with the Program (or with a work based on the Program) on a volume of\n" +
 		"a storage or distribution medium does not bring the other work under\n" +
 		"the scope of this License.\n" +
 		"\n" +
 		"  3. You may copy and distribute the Program (or a work based on it,\n" +
 		"under Section 2) in object code or executable form under the terms of\n" +
 		"Sections 1 and 2 above provided that you also do one of the following:\n" +
 		"\n" +
 		"    a) Accompany it with the complete corresponding machine-readable\n" +
 		"    source code, which must be distributed under the terms of Sections\n" +
 		"    1 and 2 above on a medium customarily used for software interchange; or,\n" +
 		"\n" +
 		"    b) Accompany it with a written offer, valid for at least three\n" +
 		"    years, to give any third party, for a charge no more than your\n" +
 		"    cost of physically performing source distribution, a complete\n" +
 		"    machine-readable copy of the corresponding source code, to be\n" +
 		"    distributed under the terms of Sections 1 and 2 above on a medium\n" +
 		"    customarily used for software interchange; or,\n" +
 		"\n" +
 		"    c) Accompany it with the information you received as to the offer\n" +
 		"    to distribute corresponding source code.  (This alternative is\n" +
 		"    allowed only for noncommercial distribution and only if you\n" +
 		"    received the program in object code or executable form with such\n" +
 		"    an offer, in accord with Subsection b above.)\n" +
 		"\n" +
 		"The source code for a work means the preferred form of the work for\n" +
 		"making modifications to it.  For an executable work, complete source\n" +
 		"code means all the source code for all modules it contains, plus any\n" +
 		"associated interface definition files, plus the scripts used to\n" +
 		"control compilation and installation of the executable.  However, as a\n" +
 		"special exception, the source code distributed need not include\n" +
 		"anything that is normally distributed (in either source or binary\n" +
 		"form) with the major components (compiler, kernel, and so on) of the\n" +
 		"operating system on which the executable runs, unless that component\n" +
 		"itself accompanies the executable.\n" +
 		"\n" +
 		"If distribution of executable or object code is made by offering\n" +
 		"access to copy from a designated place, then offering equivalent\n" +
 		"access to copy the source code from the same place counts as\n" +
 		"distribution of the source code, even though third parties are not\n" +
 		"compelled to copy the source along with the object code.\n" +
 		"\n" +
 		"  4. You may not copy, modify, sublicense, or distribute the Program\n" +
 		"except as expressly provided under this License.  Any attempt\n" +
 		"otherwise to copy, modify, sublicense or distribute the Program is\n" +
 		"void, and will automatically terminate your rights under this License.\n" +
 		"However, parties who have received copies, or rights, from you under\n" +
 		"this License will not have their licenses terminated so long as such\n" +
 		"parties remain in full compliance.\n" +
 		"\n" +
 		"  5. You are not required to accept this License, since you have not\n" +
 		"signed it.  However, nothing else grants you permission to modify or\n" +
 		"distribute the Program or its derivative works.  These actions are\n" +
 		"prohibited by law if you do not accept this License.  Therefore, by\n" +
 		"modifying or distributing the Program (or any work based on the\n" +
 		"Program), you indicate your acceptance of this License to do so, and\n" +
 		"all its terms and conditions for copying, distributing or modifying\n" +
 		"the Program or works based on it.\n" +
 		"\n" +
 		"  6. Each time you redistribute the Program (or any work based on the\n" +
 		"Program), the recipient automatically receives a license from the\n" +
 		"original licensor to copy, distribute or modify the Program subject to\n" +
 		"these terms and conditions.  You may not impose any further\n" +
 		"restrictions on the recipients' exercise of the rights granted herein.\n" +
 		"You are not responsible for enforcing compliance by third parties to\n" +
 		"this License.\n" +
 		"\n" +
 		"  7. If, as a consequence of a court judgment or allegation of patent\n" +
 		"infringement or for any other reason (not limited to patent issues),\n" +
 		"conditions are imposed on you (whether by court order, agreement or\n" +
 		"otherwise) that contradict the conditions of this License, they do not\n" +
 		"excuse you from the conditions of this License.  If you cannot\n" +
 		"distribute so as to satisfy simultaneously your obligations under this\n" +
 		"License and any other pertinent obligations, then as a consequence you\n" +
 		"may not distribute the Program at all.  For example, if a patent\n" +
 		"license would not permit royalty-free redistribution of the Program by\n" +
 		"all those who receive copies directly or indirectly through you, then\n" +
 		"the only way you could satisfy both it and this License would be to\n" +
 		"refrain entirely from distribution of the Program.\n" +
 		"\n" +
 		"If any portion of this section is held invalid or unenforceable under\n" +
 		"any particular circumstance, the balance of the section is intended to\n" +
 		"apply and the section as a whole is intended to apply in other\n" +
 		"circumstances.\n" +
 		"\n" +
 		"It is not the purpose of this section to induce you to infringe any\n" +
 		"patents or other property right claims or to contest validity of any\n" +
 		"such claims; this section has the sole purpose of protecting the\n" +
 		"integrity of the free software distribution system, which is\n" +
 		"implemented by public license practices.  Many people have made\n" +
 		"generous contributions to the wide range of software distributed\n" +
 		"through that system in reliance on consistent application of that\n" +
 		"system; it is up to the author/donor to decide if he or she is willing\n" +
 		"to distribute software through any other system and a licensee cannot\n" +
 		"impose that choice.\n" +
 		"\n" +
 		"This section is intended to make thoroughly clear what is believed to\n" +
 		"be a consequence of the rest of this License.\n" +
 		"\n" +
 		"  8. If the distribution and/or use of the Program is restricted in\n" +
 		"certain countries either by patents or by copyrighted interfaces, the\n" +
 		"original copyright holder who places the Program under this License\n" +
 		"may add an explicit geographical distribution limitation excluding\n" +
 		"those countries, so that distribution is permitted only in or among\n" +
 		"countries not thus excluded.  In such case, this License incorporates\n" +
 		"the limitation as if written in the body of this License.\n" +
 		"\n" +
 		"  9. The Free Software Foundation may publish revised and/or new versions\n" +
 		"of the General Public License from time to time.  Such new versions will\n" +
 		"be similar in spirit to the present version, but may differ in detail to\n" +
 		"address new problems or concerns.\n" +
 		"\n" +
 		"Each version is given a distinguishing version number.  If the Program\n" +
 		"specifies a version number of this License which applies to it and \"any\n" +
 		"later version\", you have the option of following the terms and conditions\n" +
 		"either of that version or of any later version published by the Free\n" +
 		"Software Foundation.  If the Program does not specify a version number of\n" +
 		"this License, you may choose any version ever published by the Free Software\n" +
 		"Foundation.\n" +
 		"\n" +
 		"  10. If you wish to incorporate parts of the Program into other free\n" +
 		"programs whose distribution conditions are different, write to the author\n" +
 		"to ask for permission.  For software which is copyrighted by the Free\n" +
 		"Software Foundation, write to the Free Software Foundation; we sometimes\n" +
 		"make exceptions for this.  Our decision will be guided by the two goals\n" +
 		"of preserving the free status of all derivatives of our free software and\n" +
 		"of promoting the sharing and reuse of software generally.\n" +
 		"\n" +
 		"			    NO WARRANTY\n" +
 		"\n" +
 		"  11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY\n" +
 		"FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN\n" +
 		"OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES\n" +
 		"PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED\n" +
 		"OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
 		"MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS\n" +
 		"TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE\n" +
 		"PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,\n" +
 		"REPAIR OR CORRECTION.\n" +
 		"\n" +
 		"  12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING\n" +
 		"WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR\n" +
 		"REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,\n" +
 		"INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING\n" +
 		"OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED\n" +
 		"TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY\n" +
 		"YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER\n" +
 		"PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE\n" +
 		"POSSIBILITY OF SUCH DAMAGES.\n" +
 		"\n" +
 		"		     END OF TERMS AND CONDITIONS\n" +
 		"\n" +
 		"	    How to Apply These Terms to Your New Programs\n" +
 		"\n" +
 		"  If you develop a new program, and you want it to be of the greatest\n" +
 		"possible use to the public, the best way to achieve this is to make it\n" +
 		"free software which everyone can redistribute and change under these terms.\n" +
 		"\n" +
 		"  To do so, attach the following notices to the program.  It is safest\n" +
 		"to attach them to the start of each source file to most effectively\n" +
 		"convey the exclusion of warranty; and each file should have at least\n" +
 		"the \"copyright\" line and a pointer to where the full notice is found.\n" +
 		"\n" +
 		"    <one line to give the program's name and a brief idea of what it does.>\n" +
 		"    Copyright (C) <year>  <name of author>\n" +
 		"\n" +
 		"    This program is free software; you can redistribute it and/or modify\n" +
 		"    it under the terms of the GNU General Public License as published by\n" +
 		"    the Free Software Foundation; either version 2 of the License, or\n" +
 		"    (at your option) any later version.\n" +
 		"\n" +
 		"    This program is distributed in the hope that it will be useful,\n" +
 		"    but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
 		"    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
 		"    GNU General Public License for more details.\n" +
 		"\n" +
 		"    You should have received a copy of the GNU General Public License along\n" +
 		"    with this program; if not, write to the Free Software Foundation, Inc.,\n" +
 		"    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.\n" +
 		"\n" +
 		"Also add information on how to contact you by electronic and paper mail.\n" +
 		"\n" +
 		"If the program is interactive, make it output a short notice like this\n" +
 		"when it starts in an interactive mode:\n" +
 		"\n" +
 		"    Gnomovision version 69, Copyright (C) year name of author\n" +
 		"    Gnomovision comes with ABSOLUTELY NO WARRANTY; for details type `show w'.\n" +
 		"    This is free software, and you are welcome to redistribute it\n" +
 		"    under certain conditions; type `show c' for details.\n" +
 		"\n" +
 		"The hypothetical commands `show w' and `show c' should show the appropriate\n" +
 		"parts of the General Public License.  Of course, the commands you use may\n" +
 		"be called something other than `show w' and `show c'; they could even be\n" +
 		"mouse-clicks or menu items--whatever suits your program.\n" +
 		"\n" +
 		"You should also get your employer (if you work as a programmer) or your\n" +
 		"school, if any, to sign a \"copyright disclaimer\" for the program, if\n" +
 		"necessary.  Here is a sample; alter the names:\n" +
 		"\n" +
 		"  Yoyodyne, Inc., hereby disclaims all copyright interest in the program\n" +
 		"  `Gnomovision' (which makes passes at compilers) written by James Hacker.\n" +
 		"\n" +
 		"  <signature of Ty Coon>, 1 April 1989\n" +
 		"  Ty Coon, President of Vice\n" +
 		"\n" +
 		"This General Public License does not permit incorporating your program into\n" +
 		"proprietary programs.  If your program is a subroutine library, you may\n" +
 		"consider it more useful to permit linking proprietary applications with the\n" +
 		"library.  If this is what you want to do, use the GNU Lesser General\n" +
 		"Public License instead of this License.\n" ;
 
 	private final byte[] iconBytes = {
 			-119,   80,   78,   71,   13,   10,   26,   10,    0,    0,    0,   13,   73,   72,   68,   82, 
 			   0,    0,    0,   94,    0,    0,    0,   72,    8,    6,    0,    0,    0,  127,   93,   66, 
 			 -24,    0,    0,    0,    6,   98,   75,   71,   68,    0,   -1,    0,   -1,    0,   -1,  -96, 
 			 -67,  -89, -109,    0,    0,    0,    9,  112,   72,   89,  115,    0,    0,   14,  -61,    0, 
 			   0,   14,  -62,    1,  -34,  116, -103,   37,    0,    0,    0,    7,  116,   73,   77,   69, 
 			   7,  -42,    7,   12,    8,   46,   51,   74, -127,   15,  -72,    0,    0,    6,   26,   73, 
 			  68,   65,   84,  120,  -38,  -19,  -99,  111,  104,   27,  101,   28,  -57,  -65,   79,   23, 
 			 103,  -59,  -46,   13,   11,   99,  -17,   58,   29,   70,  -10,   34,   72,   83,  -35,  -24, 
 			 -47,   53,  119,  -66,  -23,  -85,  -54,   96,    1,  -57,  -20,   11,  -95,   58,  -33,  -92, 
 			 -96,  102,  -32,   72,  -89,  121,  -98,   23,   41,   21,  -51,   24,  -76,   32,   29,   84, 
 			-117,  -12, -123, -123,   14, -124,  -18,  -75,   -9,  -92, -116, -117,   12, -119,  -78,   14, 
 			-124,  -62,  -24,  -70,   23,   25,  -66,   40, -126,    8,  -82,  -38,  -19,  -15,   69, -101, 
 			  51,  -55, -110,  -12,  -18, -110,  -36,  -65,  -36,   15,   30,  -24,  -35,  -27,  -62,  -45, 
 			  79,  126,   -9,   -3,  125,  -97,   95,  -18,   90,   34, -124,   64,   16,  -10,   71,   87, 
 			-128,  -64, -103,    8, -103,  121,   49,   33,  -60,   21, -105, -121,   16, -126,  116,   20, 
 			  -8,   -3,   95,  -38,  -47,    9,   19,   66,   64,    8,   17,   94, -121,  -17,   73,  -87, 
 			  -7,   -6,  -27,   99,  -82,  -71,   -6,   58,   10,  124,  -12,  -59,  110,  -49,  -61,   -9, 
 			 108,  113,  -11,   58,  124,   79,  -69,   26,   47,  -61,   -9,  -68,  -99,  -12,   42,   -4, 
 			-112,   89, -101,   72,   72,   -5,  -51, -124,   89,  -25,   84,   14,  -33,   43,  110,   39, 
 			   4,    0,  -17,   31,   59, -126,   15, -114,   29,  113,  -59, -124,  -50,  -36,  123, -120, 
 			  92,   46, -121,   88,   44,  -26,   57,   -8, -116,   49,   65,   41,  -43,  -73,   85,   85, 
 			-123,   44,  -53,  -60,   -9,   43,   87,   39,  101, -121,  115,   46,   40,  -91,   80,  -57, 
 			  87,   33,  -90,   -2, -128,   58,  -66,   10,   69,   81,  -64,   24,   19, -106,   23,   80, 
 			 -89,  -41,  -73,   42,  -74,  -17,   68,   -6,   13,   29,   51,  114,  -68,  -99,  -16,  -19, 
 			 -52,  124,  -58,   24,  -24,  -56,   21,  -56,   -3,  -61,    0,    0,  -71,  127,   24,  -22, 
 			  -8,   42,  -40,  -73,   95, -128,  115,   46,   84,   85,   37, -106,   86,  -82,  -43,  -80, 
 			 -53,  -73,   27,   29,  -77,    3,  -74,  -45,  -16,   21,   69,   17,  -40,  -36,   69,  122, 
 			  -4,  -45, -118,   -3,  114,   -1,   48,  -28,   -2,   97,   40,   75,   99,   80,   20,  -91, 
 			   2,  126,  -88, -107,   19,   32,   93,  -10,   40,  -41, -103,  123,   15,  -51,  -76,   24, 
 			 -38,   10,   63, -107,   74, -119,   68,   34, -127,  -66,   31,  -98,  -81,   -5,   26,  117, 
 			 124,   21,  -54,  -46,   24,    8,   33,  -94,  -92,   -5, -122,   73,  -99,   94,  -33,  -46, 
 			  71,  -67,   12,   22,   79,  -97,   54,   60,  -73,   85, -114,  -57,  -52,    0, -128,   92, 
 			  46,   39,  -38, -107,  -23, -125, -125, -125, -112,   36,    9,  -37,  -25,  118, -112,  -70, 
 			  63,  -35,   16,  126,  -71,  -18,   91, -110,   26,  -77,   97,  -73,  -44,  -40,   85,   76, 
 			  19, -119,    4,  -14,   -7,   60,   36,   73,  -46,   71,  -22, -109,  105,   76,  -97,   76, 
 			 -43,   60,  -89,  -92,   -5,   10,   29,  107,  -91,  -85,   33,  -24, -108,  -32, -100, -117, 
 			 -87,  -87,   41,   72, -110, -124,  100,   50, -119,  108,   54,   11,   77,  -45,    0,    0, 
 			-109,  -41,   62,   70,  -22,   -2,   52,   -8,  -42,  -19,  -70,  -16,   91,  102,   39,   -9, 
 			 -92,  -28, -127,   46,   67,  -28,  -48,   33,   95,   67,   87,   20,    5,  -46,  -87,    2, 
 			 -78,  -39,   44,    0,   32, -103,   76,    2,   64,    5,   -4,  -19,  115,   59,   53,  -31, 
 			-105,  -10,  117,   53,   43,   21,  119,   34,   -3,   21,    3,    0,  -60, -109,   39,  -66, 
 			-107,   25,  -58,   24,   46,   79,  116,   35,   57,  -47, -115,  -53,  -25,   23, -112,  -51, 
 			  92,    0,    0,   72, -110,    4,    0,   -6, -121,   81,  -46,   -3,  106,   -8,  -54,  -46, 
 			  24,   84,   85,   13,  -66,   -6,   51,   91,   76,   -1,   -7,  -13,   54, -110,   19,  -35, 
 			  -6,  -66,  -53,  -17,  -34,  -59,  -75,  -49,   94, -127,  -90,  105,   21,  -46,   83,   14, 
 			 -65,   84,  116, -107,  -91,   49,  -56,  -78,  -68,  -73, -102,  -35,  -81,   -4,  -62,   77, 
 			-125,  115,   46,   90,   21,  101,  -17, -121,  102,    6,  -91,   84,   72,  -47, -112,  120, 
 			 -92,   29,  -83,   57,  110,  -50,  -11, -120, -107, -107,   21,   81,   44,   22,   69,  -79, 
 			  88,   20,  -55,  100,   82,   -1,  121,  101,  101,   69,    0,   16,  -78,   44,  -21,  -13, 
 			  32,  -19,  -74,   92,   86,  -61,  108,  -81,  -90,   81,   83, -113,  115, -114,   88,   44, 
 			  70, -102,  -43,  -11, -101,  115,   61, -112,  -94,  -11, -115,  -96,   86,  -40, -123,   56, 
 			 -66,   88,   33,   59,   67,   67,   67, -120,  -57,  -29, -112,  101,   25,  -27,   11,   40, 
 			  98,  -90,   19,  -72,  -65,   24,  -15, -108,   60,   52,   11,  -34,   40,  -12,  114,   -8, 
 			 -38,  111,   81,   36,  -89,  -66,    7,    0,  -60,  -29,  113,  104, -102,  -10,   76,  -61, 
 			  44,   20,   40,  -73,  -79,   98,  106,    4,   58,    0,   72,  -47,   16,  -92,  -24,   93, 
 			 124, -107,  -71,    0,  116,  -65,   89,   19,  122,    0,  -34,  112,   49,  -19,   49,  125, 
 			 -82,  116,  -86, -128,  -13, -119,  -75,  -70,  -83,  -31,  -64,  -43,   28,    0,   -3,  -26, 
 			  92, -113,  -91,  -13,  -49,   39,   -2,  106,  -40, -113,   15,   50,  -66, -114,  -82,  115, 
 			 -50,  -15,   72,   59,  106,   25,  -70,  110,   27,  -21,   68, -112,  -15,   13, -118,  -87, 
 			  85,  -24, -121,  123, -121,   81,  -35,  127,   15,   50,   -2,   96, -119,   49,  -20,   96, 
 			 106,   58, -102,  -62,   46, -124,   80,   15,  116,   80,   65,  -58,   87,  -23,   -6,  -98, 
 			  43,  -79,    6,  -67,  -92,  -21,   70,   34,    0,  111,   83,   49,   13,  -64,  -41,  -10, 
 			 -22,   77,   67,   63,  -88, -104,    6,   26,   95,  -93, -104,   82,   74,  -37,   94,   76, 
 			-125, -116,  111,  -94,   29,   80,   29,  -39, -123,  -57, -106,  -96,  -37,  -46,  -85,  -79, 
 			 -29,  -50,   51,  -85,   33,  -53,   50,  -34,   56,   -7,   83,   69, -101,  -41,   74,   49, 
 			  53,   35,   49,  -74,  102,  124,  -77,   45,  -39,   86, -113,  -46,  -35,   94,  -43,  -67, 
 			 117,  -69,  -96,  119,  -76,  -44,   72,  -47, -112,  101,   93,  -49,  126,  -13,   24, -108, 
 			  82,  -53,  -48,   91,   46,   53,   78,  -56, -118,   89,  -23,  -29, -100,   55,  -75,   72, 
 			 -78,   90,   76,  -37,  -17,  106,  -26,   27, -125,    8,   17,   96,  -73,   85,   45,   -3, 
 			  15, -119,  -87,   27,   92,  -35,    2,  -35,   22,   59,   41,   46,   85,   93,   21,   55, 
 			-102,   63,  -26,  -10,  118, -128,  107,  124,  124,   53,  -48,  -14,  109,  -93,  -57,   90, 
 			   1,  -35,  -82,  118,   64,   80,   92,  -53,   36, -122,  115,   14,  105,  -64,   90,  -98, 
 			  53,  -21,   96, -100,  -77, -109, -105,   -2,   31,  102,  -78,  -72,  116,   78,  -77, -111, 
 			 -53,  -27,   64,  -81,   78,  -32,  -53,  -71,   31,   49,  -69,   28,  110,  123,   59,  -64, 
 			  53,  -32,  -55,   13,  107,  -78,   97,  -11,  -68,  -22,  -96, -108,   34,  118,  118,    0, 
 			   0, -112, -103,  -71, -114,  -39,  -27,   48,  -76,  -62,  -82,  -83,  -59,  -44,   81,  -87, 
 			  33,   55,  -10,   50,  -72,   -9,  -71,  -26,  -81,    6,   51,   50,    3,    0,  -14,   72, 
 			  84,  -33, -105, -103,  -71, -114, -105,   94,  -99,  105,    8,  -65,   84,   76,  -37,    1, 
 			 -35, -106,  -30,   90,   13,  -45,  104,  -15,  108,  -43, -121,  -64,   24,    3,  -67,   58, 
 			 -15,  108,  -69,   96,   36,   10, -114,   25,  -52,   46,  127, -121,  -55,  119,   54,  -38, 
 			  94,   76,  125,  -73, -128,  -38,  -65,  103,  -90,  -31, -100,  -60,  -33,   90,  -61,   -9, 
 			-104,  -70,  -14, -111,   14,  -65,   21,  -19,    0,  -37,   51,   94,    8, -127,   92,   46, 
 			 -25,   26,   55,  -61,   24,  -85, -112, -104,  122, -111, -103,  -71,   14, -106,   89,   64, 
 			 -28,   -8,  114,   75,  -38,    1, -114,   72,   77,  117,  -10,   17,   66,   28,   -5, -117, 
 			  31, -100,  115,  -56,   35,    3,  -58,  -26,  125,  118,    0,   44,  -13,   11,   14,   -9, 
 			 -10,   34,  -99,   78,  -73,   -3,  -46,  -75,  -91,   45,  -20,   20,  120,   35,   50,    3, 
 			   0,  -54,  104,    2,  124,  -83, -128,   19,   39,   78,   96,  115,  115,  -45,   22,  -67, 
 			 -12,  -19,    2, -118,   49, -122,  112,   56,   12, -106,   89,  -88,  127,   69,  -84,   21, 
 			  64,   94, -112, -128,  -82,   94,    0,  -64,  -30,  -30,  -94,  109,  -13,  -13,   45,   -8, 
 			 -11,  -11,  117, -124,  -61,   97,   -4,   -4,  -21,  -17, -120,   95,   -4,   -4,   25,  -32, 
 			 -54,  104,    2,  -54,  104,    2,  -86,  -86,  -74,  -43,  -67,  116,   36,   -8,   82,  -20, 
 			  -4,  -37, -115,  -41,   94,  127,   15,  124,  -83,    0, -106,   89, -128,   50, -102, -128, 
 			  -4,  -42,  -37,   16,   66,   64, -106,  101,   71,  -26,   23,  -14,  -77,  -52, -108,   71, 
 			  95,   95,   31,  -30,   23,  -45, -120,   68,   34,   37,  -85,  -24,  -24,   28,  125,    9, 
 			 126,  118,  118,   22,   67,   67,   67,   -6,  118,   62,  -97,  -57,  -10,  -10,  -74,   43, 
 			-128,   -5,   86,  106,   56,  -25,  122,  -74,  111,  108,  108,  -32,  -42,  -83,   91, -104, 
 			-100, -100,  116,   84,   86,   58,   34,  -29,   75,   11,  -72,  124,   62, -113,   72,   36, 
 			-126,   -7,   -7,  121,   87,    1,  -81,   88,  109, -102,  120,   60,  -35,  -46,  -61,   95, 
 			 118,    6,  -10,   31,   96,  -93, -108,   58,  -14, -112, -102,  -47,  -31,  -69, -116,  -89, 
 			-108,   34,  -99,   78,  -69,  126,  -98,  -66,  -45,  120,   47,   64,   -9,  -75, -113,   15, 
 			 -64,    7,   17, -128,  119,   83, -104,   46,  -82,  110,  -66,    9,  -43,  -73,  -32, -123, 
 			  16,  -60,  -20,  -93,   -9,  -82,  -12,  -48,   94,  -52,  120,   11, -113,  -90,    7,   -1, 
 			-110,   33,  -48,   -8,    0,  124,    0,   62,   64,  -32,   17, -115,  -73,   18, -127,   19, 
 			 114,    0,  -68,   21,   39,  -44,    9,  -15,   31,   57,  -12,  -61,  122,   53,   35,   29, 
 			  29,    0,    0,    0,    0,   73,   69,   78,   68,  -82,   66,   96, -126, 
 	};
 
 	
 	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="9,0"
 
 	private JPanel jContentPane = null;
 
 	private JMenuBar jJMenuBar = null;
 
 	private JMenu fileMenu = null;
 
 	private JMenu editMenu = null;
 
 	private JMenu helpMenu = null;
 
 	private JMenuItem exitMenuItem = null;
 
 	private JMenuItem aboutMenuItem = null;
 
 	private JMenuItem licenseManagerMenuItem = null;
 
 	private JMenuItem advancedMenuItem = null;
 
 	private JMenuItem saveMenuItem = null;
 
 	private JDialog licenseDialog = null;  //  @jve:decl-index=0:visual-constraint="524,146"
 
 	private JPanel aboutContentPane1 = null;
 
 	private JMenuItem licenseMenuItem = null;
 
 	private JScrollPane jScrollPane = null;
 
 	private JTextArea jTextArea = null;
 
 	private JDialog jFileSaveDialog = null;  //  @jve:decl-index=0:visual-constraint="14,344"
 
 	private JMenuItem openMenuItem = null;
 
 	private JTabbedPane jTabbedPane = null;
 
 	private JScrollPane jScrollPane1 = null;
 
 	private JTextArea advancedTextArea = null;
 
 	private JPanel jPanel = null;
 
 	private JPanel jPanel2 = null;
 
 	private JPanel jPanel1 = null;
 
 	private JLabel jLabel = null;
 
 	private JComboBox licenseComboBox = null;
 
 	private JLabel jLabel1 = null;
 
 	private JComboBox versionComboBox = null;
 
 	private JLabel emptyLabel = null;
 
 	private JButton jButton = null;
 
 	private JLabel infoLabel = null;
 
 	private JPanel jPanelFileInfo = null;
 
 	private JLabel infoFileName = null;
 
 	private String initialMessage1 = null;
 
 	private JLabel jLabel2 = null;
 
 	/**
 	 * This method initializes licenseDialog	
 	 * 	
 	 * @return javax.swing.JDialog	
 	 */
 	private JDialog getLicenseDialog() {
 		if (licenseDialog == null) {
 			licenseDialog = new JDialog(getJFrame(), true);
 			licenseDialog.setResizable(true);
 			//licenseDialog.setSize(new Dimension(274, 82));
 			licenseDialog.setContentPane(getAboutContentPane1());
 			licenseDialog.setTitle("PdfLicenseManager License");
 		}
 		return licenseDialog;
 	}
 
 	/**
 	 * This method initializes aboutContentPane1	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getAboutContentPane1() {
 		if (aboutContentPane1 == null) {
 			aboutContentPane1 = new JPanel();
 			aboutContentPane1.setLayout(new BorderLayout());
 			aboutContentPane1.add(getJScrollPane(), BorderLayout.CENTER);
 		}
 		return aboutContentPane1;
 	}
 
 	/**
 	 * This method initializes licenseMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getLicenseMenuItem() {
 		if (licenseMenuItem == null) {
 			licenseMenuItem = new JMenuItem();
 			licenseMenuItem.setText("License");
 			licenseMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					JDialog licenseDialog = getLicenseDialog();
 					licenseDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					licenseDialog.setLocation(loc);
 					licenseDialog.setVisible(true);
 					}
 			});
 		}
 		return licenseMenuItem;
 	}
 
 	/**
 	 * This method initializes jScrollPane	
 	 * 	
 	 * @return javax.swing.JScrollPane	
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setViewportView(getJTextArea());
 		}
 		return jScrollPane;
 	}
 
 	/**
 	 * This method initializes jTextArea	
 	 * 	
 	 * @return javax.swing.JTextArea	
 	 */
 	private JTextArea getJTextArea() {
 		if (jTextArea == null) {
 			jTextArea = new JTextArea();
 			jTextArea.setText(gpl);
 			//jTextArea.setPreferredSize(new Dimension (500, 400));
 			//jTextArea.setSize(new Dimension(400, 300));
 		}
 		return jTextArea;
 	}
 
 
 	/**
 	 * This method initializes openMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getOpenMenuItem() {
 		if (openMenuItem == null) {
 			openMenuItem = new JMenuItem();
 			openMenuItem.setText("Open");
 			openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true));
 			openMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					JFileChooser chooser = new JFileChooser();
 				    PdfFileFilter filter = new PdfFileFilter();
 				    chooser.setFileFilter(filter);
 				    int returnVal = chooser.showOpenDialog(getJFrame());
 				    if (returnVal == JFileChooser.APPROVE_OPTION) {
 				       //System.out.println("You chose to open this file:"+chooser.getSelectedFile().getName());
 				       manager.setFileIn(chooser.getSelectedFile());
 				       try {
 				    	   manager.readFileInfo();
 				    	   License lic = manager.getCurrLic();
 				    	   if (lic != null) {
 				    		   infoLabel.setText("<html>"+lic.toHtmlString());
 				    		   advancedTextArea.setText(manager.getXmpText());
 				    		   // Set cursor at beginning
 				    		   advancedTextArea.setCaretPosition(0);
 				    	   } else {
 				    		   infoLabel.setText("<html><font color=\"blue\">No XMP info found</font><br>");
 				    		   advancedTextArea.setText("");
 				    	   }
 				    	   infoFileName.setText("<html>"+chooser.getSelectedFile().getName());
 				       } catch (IOException e1) {
 			    		   infoLabel.setText("<html><font color=\"red\">ERROR:<br>"+e1.getMessage()+"</font>");
 				       } catch (LicenseException e1) {
 			    		   infoLabel.setText("<html><font color=\"red\">ERROR:<br>"+e1.getMessage()+"</font>");
 				       }
 				    }
 				}
 			} );
 			
 		}
 		return openMenuItem;
 	}
 
 	/**
 	 * This method initializes jTabbedPane	
 	 * 	
 	 * @return javax.swing.JTabbedPane	
 	 */
 	private JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.addTab("License Manager", null, getJPanel(), null);
 			jTabbedPane.addTab("Advanced", null, getJScrollPane1(), null);
 		}
 		return jTabbedPane;
 	}
 
 	/**
 	 * This method initializes jScrollPane1	
 	 * 	
 	 * @return javax.swing.JScrollPane	
 	 */
 	private JScrollPane getJScrollPane1() {
 		if (jScrollPane1 == null) {
 			jScrollPane1 = new JScrollPane();
 			jScrollPane1.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createTitledBorder("Raw XMP Metadata"),
                         BorderFactory.createEmptyBorder(5,5,5,5)));
 			jScrollPane1.setViewportView(getAdvancedTextArea());
 		}
 		return jScrollPane1;
 	}
 
 	/**
 	 * This method initializes advancedTextArea	
 	 * 	
 	 * @return javax.swing.JTextArea	
 	 */
 	private JTextArea getAdvancedTextArea() {
 		if (advancedTextArea == null) {
 			advancedTextArea = new JTextArea();
 			advancedTextArea.setEditable(false);
 		}
 		return advancedTextArea;
 	}
 
 	/**
 	 * This method initializes jPanel	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getJPanel() {
 		if (jPanel == null) {
 			jPanel = new JPanel();
 			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.Y_AXIS));
 			jPanel.add(getJPanelFileInfo(), null);
 			jPanel.add(getJPanel2(), null);
 			jPanel.add(getJPanel1(), null);
 		}
 		return jPanel;
 	}
 
 	/**
 	 * This method initializes jPanel2	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getJPanel2() {
 		if (jPanel2 == null) {
 			GridLayout gridLayout1 = new GridLayout();
 			gridLayout1.setRows(1);
 			gridLayout1.setColumns(1);
 			infoLabel = new JLabel();
 			infoLabel.setText("<html>"+"No info<br>"+initialMessage);
 			jPanel2 = new JPanel();
 			jPanel2.setLayout(gridLayout1);
 			jPanel2.setBorder((BorderFactory.createCompoundBorder(
                     BorderFactory.createTitledBorder("Current License Info"),
                     BorderFactory.createEmptyBorder(5,5,5,5))));
 			jPanel2.add(infoLabel, null);
 		}
 		return jPanel2;
 	}
 
 	/**
 	 * This method initializes jPanel1	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getJPanel1() {
 		if (jPanel1 == null) {
 			emptyLabel = new JLabel();
 			emptyLabel.setText("    ");
 			jLabel1 = new JLabel();
 			jLabel1.setText("License version:");
 			jLabel = new JLabel();
 			jLabel.setText("License:");
 			GridLayout gridLayout = new GridLayout();
 			gridLayout.setRows(3);
 			gridLayout.setColumns(2);
 			gridLayout.setHgap(10);
 			gridLayout.setVgap(10);
 			jPanel1 = new JPanel();
 			jPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Update License"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
 			jPanel1.setLayout(gridLayout);
 			jPanel1.add(jLabel, null);
 			jPanel1.add(getLicenseComboBox(), null);
 			jPanel1.add(jLabel1, null);
 			jPanel1.add(getVersionComboBox(), null);
 			jPanel1.add(emptyLabel, null);
 			jPanel1.add(getJButton(), null);
 		}
 		return jPanel1;
 	}
 
 	/**
 	 * This method initializes licenseComboBox	
 	 * 	
 	 * @return javax.swing.JComboBox	
 	 */
 	private JComboBox getLicenseComboBox() {
 		if (licenseComboBox == null) {
 			licenseComboBox = new JComboBox();
 			licenseComboBox.setName("licenseCombo");
 			licenseComboBox.setPreferredSize(new Dimension(200, 24));
 			String []list = CCLicense.getCCLicenseList();
 			for (int i=0; i<list.length; i++) {
 				licenseComboBox.addItem(list[i]);
 			}
 		}
 		return licenseComboBox;
 	}
 
 	/**
 	 * This method initializes versionComboBox	
 	 * 	
 	 * @return javax.swing.JComboBox	
 	 */
 	private JComboBox getVersionComboBox() {
 		if (versionComboBox == null) {
 			versionComboBox = new JComboBox();
 			versionComboBox.addItem(CCLicense.getLicenseVersionAvail());
 		}
 		return versionComboBox;
 	}
 
 	/**
 	 * This method initializes jButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getJButton() {
 		if (jButton == null) {
 			jButton = new JButton();
 			jButton.setName("Update");
 			jButton.setText("Update");
 			jButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					String licShortName = (String) licenseComboBox.getSelectedItem();
 					String licVersion = (String) versionComboBox.getSelectedItem();
 					try {
 						if (manager.getFileIn()!=null){
 							CCLicense ccLic = new CCLicense(licShortName, licVersion);
 							manager.updateLicense(ccLic);
 							License lic = manager.getCurrLic();
 							if (lic != null) {
 								infoLabel.setText("<html>"+lic.toHtmlString());
 								advancedTextArea.setText(manager.getXmpText());
 								// Set cursor at beginning
 								advancedTextArea.setCaretPosition(0);
 							} else {
 								infoLabel.setText("<html><font color=\"blue\">No XMP info found</font><br>");
 							} 
 						}else {
 							infoLabel.setText("<html><font color=\"blue\">Please load a PDF file</font><br>");
 						}
 					} catch (LicenseException e1) {
 						infoLabel.setText("<html><font color=\"red\">ERROR 1:<br>"+e1.getMessage()+"</font>");
 					} catch (IOException e1) {
 						infoLabel.setText("<html><font color=\"red\">ERROR 2:<br>"+e1.getMessage()+"</font>");
 					}
 				}
 			});
 		}
 		return jButton;
 	}
 
 
 	/**
 	 * This method initializes jPanelFileInfo	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getJPanelFileInfo() {
 		if (jPanelFileInfo == null) {
 			//ImageIcon icon = new ImageIcon("icon.png");
 			ImageIcon icon = new ImageIcon(iconBytes);
 			jLabel2 = new JLabel(icon, JLabel.RIGHT);
 			infoFileName = new JLabel();
 			infoFileName.setText("<html>NONE");
 						
 			GridLayout gridLayout11 = new GridLayout();
 			gridLayout11.setRows(1);
 			gridLayout11.setColumns(2);
 			jPanelFileInfo = new JPanel();
 			jPanelFileInfo.setBorder((BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Current File"), BorderFactory.createEmptyBorder(5, 5, 5, 5))));
 			jPanelFileInfo.setLayout(gridLayout11);
 			jPanelFileInfo.add(infoFileName, null);
 			jPanelFileInfo.add(jLabel2, null);
 		}
 		return jPanelFileInfo;
 	}
 
 
 	
 	/**
 	 * This method initializes initialMessage1	
 	 * 	
 	 * @return java.lang.String	
 	 */
 	private String getInitialMessage1() {
 		if (initialMessage1 == null) {
 			initialMessage1 = new String();
 		}
 		return initialMessage1;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length == 0) {
 			manager = new PdfLicenseManager();
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					MainWin application = new MainWin();
 					application.getJFrame().setVisible(true);
 				}
 			});
 		} else {
 			manager = new PdfLicenseManager();
 			manager.mainTextual(args);
 		}
 	}
 
 	/**
 	 * This method initializes jFrame
 	 * 
 	 * @return javax.swing.JFrame
 	 */
 	private JFrame getJFrame() {
 		if (jFrame == null) {
 			jFrame = new JFrame();
 			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			jFrame.setJMenuBar(getJJMenuBar());
 			jFrame.setSize(600, 446);
 			jFrame.setContentPane(getJContentPane());
 			jFrame.setTitle("PdfLicenseManager");
 		}
 		return jFrame;
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new BorderLayout());
 			jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
 		}
 		return jContentPane;
 	}
 
 	/**
 	 * This method initializes jJMenuBar	
 	 * 	
 	 * @return javax.swing.JMenuBar	
 	 */
 	private JMenuBar getJJMenuBar() {
 		if (jJMenuBar == null) {
 			jJMenuBar = new JMenuBar();
 			jJMenuBar.add(getFileMenu());
 			jJMenuBar.add(getEditMenu());
 			jJMenuBar.add(getHelpMenu());
 		}
 		return jJMenuBar;
 	}
 
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	private JMenu getFileMenu() {
 		if (fileMenu == null) {
 			fileMenu = new JMenu();
 			fileMenu.setText("File");
 			fileMenu.add(getOpenMenuItem());
 			fileMenu.add(getSaveMenuItem());
 			fileMenu.add(getExitMenuItem());
 		}
 		return fileMenu;
 	}
 
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	private JMenu getEditMenu() {
 		if (editMenu == null) {
 			editMenu = new JMenu();
 			editMenu.setText("Tools");
 			editMenu.add(getLicenseManagerMenuItem());
 			editMenu.add(getAdvancedMenuItem());
 		}
 		return editMenu;
 	}
 
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	private JMenu getHelpMenu() {
 		if (helpMenu == null) {
 			helpMenu = new JMenu();
 			helpMenu.setText("Help");
 			helpMenu.add(getAboutMenuItem());
 			helpMenu.add(getLicenseMenuItem());
 		}
 		return helpMenu;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getExitMenuItem() {
 		if (exitMenuItem == null) {
 			exitMenuItem = new JMenuItem();
 			exitMenuItem.setText("Exit");
 			exitMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					System.exit(0);
 				}
 			});
 		}
 		return exitMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getAboutMenuItem() {
 		if (aboutMenuItem == null) {
 			aboutMenuItem = new JMenuItem();
 			aboutMenuItem.setText("About");
 			aboutMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					JOptionPane.showMessageDialog(getJFrame(), "<html>" +
 							"PdfLicenseManager<br>" +
 							"<br>" +
							"Version: " + PdfLicenseManager.version + "<br>" +
 							"Author: Enrico Masala<br>" +
 							"License: GPL v.2",
 							
 							"PdfLicenseManager Info",
 							JOptionPane.INFORMATION_MESSAGE);
 
 /*
 					JDialog aboutDialog = getAboutDialog();
 					aboutDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					aboutDialog.setLocation(loc);
 					aboutDialog.setVisible(true);
 */
 					}
 			});
 		}
 		return aboutMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getLicenseManagerMenuItem() {
 		if (licenseManagerMenuItem == null) {
 			licenseManagerMenuItem = new JMenuItem();
 			licenseManagerMenuItem.setText("License Manager");
 			licenseManagerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
 					Event.CTRL_MASK, true));
 			licenseManagerMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					int idx = jTabbedPane.indexOfTab("License Manager");
 					if (idx >= 0) {
 						jTabbedPane.setSelectedIndex(idx);
 					}
 				}
 			});
 		}
 		return licenseManagerMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getAdvancedMenuItem() {
 		if (advancedMenuItem == null) {
 			advancedMenuItem = new JMenuItem();
 			advancedMenuItem.setText("Advanced");
 			advancedMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
 					Event.CTRL_MASK, true));
 			advancedMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					int idx = jTabbedPane.indexOfTab("Advanced");
 					if (idx >= 0) {
 						jTabbedPane.setSelectedIndex(idx);
 					}
 				}
 			});
 		}
 		return advancedMenuItem;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	private JMenuItem getSaveMenuItem() {
 		if (saveMenuItem == null) {
 			saveMenuItem = new JMenuItem();
 			saveMenuItem.setText("Save as");
 			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 					Event.CTRL_MASK, true));
 			saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if (manager.getFileIn()==null) {
 						JOptionPane.showMessageDialog(getJFrame(), "Please open a PDF file first!", "Error", JOptionPane.ERROR_MESSAGE);
 					} else {
 						JFileChooser chooser = new JFileChooser();
 						boolean repeat = true;
 						while (repeat) {
 							int returnVal = chooser.showSaveDialog(getJFrame());
 							if (returnVal == JFileChooser.APPROVE_OPTION) {
 								//System.out.println("You chose to save in this file:"+chooser.getSelectedFile().getName());
 								boolean write = true;
 								if (chooser.getSelectedFile().exists()) {
 									//JOptionPane jop = new JOptionPane();
 									//jop.setInitialSelectionValue("No");
 									int option = JOptionPane.showConfirmDialog(chooser, "Overwrite existing file?\n(Do not overwrite your original files!)", "Confirm overwrite", JOptionPane.YES_NO_OPTION);
 									//System.out.println("option="+option+" cancel="+JOptionPane.NO_OPTION);
 									if (option == JOptionPane.NO_OPTION) {
 										write = false;
 									}
 								}
 
 								if (write == true) {
 									try {
 										manager.writeFile(chooser.getSelectedFile());
 										manager.setFileIn(null);
 										advancedTextArea.setText("");
 										infoLabel.setText("<html>"+"File written!<br>"+initialMessage);
 										infoFileName.setText("<html>NONE");
 									} catch (IOException e1) {
 										infoLabel.setText("<html><font color=\"red\">ERROR 3:<br>"+e1.getMessage()+"</font>");
 									} catch (DocumentException e1) {
 										infoLabel.setText("<html><font color=\"red\">ERROR 4:<br>"+e1.getMessage()+"</font>");
 									}
 									break;
 								}
 							} else {
 								break;
 							}
 						}
 					}
 				}
 			});
 		}
 		return saveMenuItem;
 	}
 
 }
