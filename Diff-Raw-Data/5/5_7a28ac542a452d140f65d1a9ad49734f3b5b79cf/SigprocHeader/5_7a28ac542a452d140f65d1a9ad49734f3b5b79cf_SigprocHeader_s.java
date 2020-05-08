 /*
 Copyright (C) 2005-2007 Michael Keith, University Of Manchester
 
 email: mkeith@pulsarastronomy.net
 www  : www.pulsarastronomy.net/wiki/Software/PulsarHunter
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
 */
 /*
  * SigprocHeader.java
  *
  * Created on 28 September 2006, 10:33
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package pulsarhunter.datatypes.sigproc;
 
 import coordlib.Coordinate;
 import coordlib.Dec;
 import coordlib.RA;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.FileChannel.MapMode;
 import coordlib.Telescope;
 
 /**
  *
  * @author mkeith
  */
 
 public class  SigprocHeader{
     public enum HeaderID {
         rawdatafile,source_name,FREQUENCY_START,FREQUENCY_END,az_start,za_start,src_raj,src_dej
                 ,tstart,tsamp,period,fch1,fchannel,foff,nchans,telescope_id,machine_id,data_type,ibeam,
                 nbeams,nbits,barycentric,pulsarcentric,nbins,nsamples,nifs,npuls,refdm
     }
     private String rawdatafile;
     private String source_name;
     private int machine_id;
     private int telescope_id;
     private int data_type;
     private int nchans;
     private int nbits;
     private int nifs;
     private int scan_number;
     private int barycentric;
     private int pulsarcentric;
     private double tstart;
     private double mjdobs;
     private double tsamp;
     private double fch1;
     private double foff;
     private double refdm;
     private double az_start;
     private double za_start;
     private double src_raj;
     private double src_dej;
     private double gal_l;
     private double gal_b;
     private double header_tobs;
     private double raw_fch1;
     private double raw_foff;
     private int nbeams;
     private int ibeam;
     private double srcl;
     private double srcb;
     private double ast0;
     private double lst0;
     private long wapp_scan_number;
     private String project;
     private String culprits;
     private double[] analog_power = new double [2];
     
     private double period;
     private int nbins;
     
     /* added frequency table for use with non-contiguous data */
     private double[] frequency_table = new double[4096]; /* note limited number of channels */
     private long npuls; /* added for binary pulse profile format */
     
     private int headerLength;
     
     private Coordinate coordinate = null;
     
     private boolean written = false;
     
     public SigprocHeader(File file) throws IOException{
         if(file.exists())this.read(file);
     }
     
     public void write(File file) throws IOException{
         file.delete();
         RandomAccessFile out = new RandomAccessFile(file,"rw");
         ByteBuffer bb = out.getChannel().map(MapMode.READ_WRITE,0,1024*64);
         bb.order(ByteOrder.nativeOrder());
         
         this.writeASCII(bb,"HEADER_START");
         
         for(HeaderID headerID : HeaderID.values()){
             switch(headerID){
                 case rawdatafile:
                     if(this.rawdatafile != null){
                         this.writeASCII(bb,headerID.toString());
                         this.writeASCII(bb,this.rawdatafile);
                     }
                     break;
                 case source_name:
                     if(this.source_name != null){
                         this.writeASCII(bb,headerID.toString());
                         this.writeASCII(bb,this.source_name);
                     }
                     break;
                     // freq start/end
                     
                 case az_start:
                     if(!Double.isNaN(this.az_start)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.az_start);
                     }
                     break;
                 case za_start:
                     if(!Double.isNaN(this.za_start)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.za_start);
                     }
                     break;
                     
                 case src_raj:
                     if(!Double.isNaN(this.src_raj)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.src_raj);
                     }
                     break;
                 case src_dej:
                     if(!Double.isNaN(this.src_dej)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.src_dej);
                     }
                     break;
                 case tstart:
                     if(!Double.isNaN(this.tstart)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.tstart);
                     }
                     break;
                 case tsamp:
                     if(!Double.isNaN(this.tsamp)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.tsamp);
                     }
                     break;
                 case period:
                     if(!Double.isNaN(this.period)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.period);
                     }
                     break;
                 case fch1:
                     if(!Double.isNaN(this.fch1)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.fch1);
                     }
                     break;
                     // fchanel
                 case foff:
                     if(!Double.isNaN(this.foff)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.foff);
                     }
                     break;
                     
                 case nchans:
                     if(this.nchans != -1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.nchans);
                     }
                     break;
                 case telescope_id:
                     if(this.telescope_id!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.telescope_id);
                     }
                     break;
                 case machine_id:
                     if(this.machine_id!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.machine_id);
                     }
                     break;
                     
                 case data_type:
                     if(this.data_type!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.data_type);
                     }
                     break;
 //                case ibeam:
 //                    if(this.ibeam!=-1){
 //                        this.writeASCII(bb,headerID.toString());
 //                        bb.putInt(this.ibeam);
 //                    }
 //                    break;
                 case nbeams:
                     if(this.nbeams!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.nbeams);
                     }
                     break;
                 case nbits:
                     if(this.machine_id!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.nbits);
                     }
                     break;
                     
                 case barycentric:
                     if(this.barycentric!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.barycentric);
                     }
                     break;
                 case pulsarcentric:
                     if(this.pulsarcentric!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.pulsarcentric);
                     }
                     break;
                 case nbins:
                     if(this.nbins!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.nbins);
                     }
                     break;
                 case nsamples:
                     
                     break;
                 case nifs:
                     if(this.nifs!=-1){
                         this.writeASCII(bb,headerID.toString());
                         bb.putInt(this.nifs);
                     }
                     break;
                 case refdm:
                     if(!Double.isNaN(this.refdm)){
                         this.writeASCII(bb,headerID.toString());
                         bb.putDouble(this.refdm);
                     }
                     break;
                     
             }
             
             
             
             
         }
         this.writeASCII(bb,"HEADER_END");
 
         out.getChannel().truncate(bb.position());
         this.setHeaderLength(bb.position());
         out.close();
         
         this.written = true;
     }
     
     
     public void read(File file) throws IOException{
         //if(file.length() < 1024*64) throw new IOException("File too small to be a sigproc data file");
         
         FileInputStream in = new FileInputStream(file);
         
         ByteBuffer bb = in.getChannel().map(MapMode.READ_ONLY,0,1024*64);
         bb.order(ByteOrder.nativeOrder());
         int nchars = bb.getInt();
         byte[] byteString = new byte[nchars];
         bb.get(byteString);
         String headerString = new String(byteString);
         
         while(true){
             nchars = bb.getInt();
             byteString = new byte[nchars];
             bb.get(byteString);
             headerString = new String(byteString);
            // System.out.println(headerString);
             if(headerString.equals("HEADER_END"))break;
             HeaderID headerID = null;
             try {
                 headerID = HeaderID.valueOf(headerString);
             } catch (IllegalArgumentException e) {
                 System.err.println("Warning: Unknown Sigproc Header Element: "+headerString);
                 continue;
             }
             
             
             this.parseHeaderElem(headerID,bb);
             
             // System.out.println("done");
             
         }
         this.setHeaderLength(bb.position());
         in.close();
         this.written = true;
     }
     
     private int channelIndex = 0;
     private void parseHeaderElem(HeaderID headerID,ByteBuffer bb){
         switch(headerID){
             case rawdatafile:
                 int nchars = bb.getInt();
                 byte[] byteString = new byte[nchars];
                 bb.get(byteString);
                 String string = new String(byteString);
                 rawdatafile = string;
                 break;
                 
             case source_name:
                 nchars = bb.getInt();
                 byteString = new byte[nchars];
                 bb.get(byteString);
                 string = new String(byteString);
                 this.source_name = string;
                 break;
             case FREQUENCY_START:
                 channelIndex = 0;
                 break;
             case az_start:
                 this.az_start = bb.getDouble();
                 break;
             case za_start:
                 za_start = bb.getDouble();
                 break;
             case src_raj:
                 this.src_raj = bb.getDouble();
                 break;
             case src_dej:
                 this.src_dej = bb.getDouble();
                 break;
             case tstart:
                 this.tstart = bb.getDouble();
                 break;
             case tsamp:
                 this.tsamp = bb.getDouble();
                 break;
             case period:
                 this.period = bb.getDouble();
                 break;
             case fch1:
                 this.fch1 = bb.getDouble();
                 break;
             case fchannel:
                 this.fch1 = this.foff = 0.0;
                 this.frequency_table[channelIndex++] = bb.getDouble();
                 break;
             case foff:
                 this.foff = bb.getDouble();
                 break;
             case nchans:
                 
                 this.nchans = bb.getInt();
 
                 break;
             case telescope_id:
                 this.telescope_id = bb.getInt();
                 break;
             case machine_id:
                 this.machine_id = bb.getInt();
                 break;
             case data_type:
                 this.data_type = bb.getInt();
                 break;
             case nbeams:
                 this.nbeams = bb.getInt();
                 break;
 //            case ibeam:
 //                this.ibeam = bb.getInt();
 //                break;
             case nbits:
                 this.nbits = bb.getInt();
                 break;
             case barycentric:
                 this.barycentric = bb.getInt();
                 break;
             case pulsarcentric:
                 this.pulsarcentric = bb.getInt();
                 break;
             case nbins:
                 this.setNbins(bb.getInt());
                 break;
             case nsamples:
                 bb.getInt();
                 break;
             case nifs:
                 this.nifs = bb.getInt();
                 break;
             case npuls:
                 this.npuls = bb.getInt();
                 break;
             case refdm:
                 this.refdm = bb.getDouble();
                 break;
                 
             default:
                 return;
         }
         
         
         
     }
     
     public Telescope getTelescope(){
         int tid = this.getTelescope_id();
         switch(tid){
 	    case 0:
 		System.err.println("Simulating test telescope as PARKES...");
 		return Telescope.PARKES;
             case 3:
                 return Telescope.NANCAY;
             case 4:
                 return Telescope.PARKES;
             case 5:
                 return Telescope.JODRELL;
             case 8:
                 return Telescope.EFFELSBERG;
             default:
                 System.out.println("Sigproc - Unknown telescope: "+tid);
                 return Telescope.UNKNOWN;
         }
         
     }
     
     public void setTelescope(Telescope t){
         int tid = 0;
         switch(t){
             case PARKES:
                 tid = 4;
                 break;
             case JODRELL:
                 tid = 5;
                 break;
             case EFFELSBERG:
                 tid = 8;
                 break;
         }
         this.setTelescope_id(tid);
     }
     
     
     public String getRawdatafile() {
         return rawdatafile;
     }
     
     public void setRawdatafile(String rawdatafile) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.rawdatafile = rawdatafile;
     }
     
     public String getSource_name() {
         return source_name;
     }
     
     public void setSource_name(String source_name) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.source_name = source_name;
     }
     
     public int getMachine_id() {
         return machine_id;
     }
     
     public void setMachine_id(int machine_id) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.machine_id = machine_id;
     }
     
     public int getTelescope_id() {
         return telescope_id;
     }
     
     public void setTelescope_id(int telescope_id) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.telescope_id = telescope_id;
     }
     
     public int getData_type() {
         return data_type;
     }
     
     public void setData_type(int data_type) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.data_type = data_type;
     }
     
     public int getNchans() {
         return nchans;
     }
     
     public void setNchans(int nchans) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.nchans = nchans;
     }
     
     public int getNbits() {
         return nbits;
     }
     
     public void setNbits(int nbits) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.nbits = nbits;
     }
     
     public int getNifs() {
         return nifs;
     }
     
     public void setNifs(int nifs) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.nifs = nifs;
     }
     
     public int getScan_number() {
         return scan_number;
     }
     
     public void setScan_number(int scan_number) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.scan_number = scan_number;
     }
     
     public int getBarycentric() {
         return barycentric;
     }
     
     public void setBarycentric(int barycentric) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.barycentric = barycentric;
     }
     
     public int getPulsarcentric() {
         return pulsarcentric;
     }
     
     public void setPulsarcentric(int pulsarcentric) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.pulsarcentric = pulsarcentric;
     }
     
     public double getTstart() {
         return tstart;
     }
     
     public void setTstart(double tstart) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.tstart = tstart;
     }
     
     public double getMjdobs() {
         return mjdobs;
     }
     
     public void setMjdobs(double mjdobs) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.mjdobs = mjdobs;
     }
     
     public double getTsamp() {
         return tsamp;
     }
     
     public void setTsamp(double tsamp) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.tsamp = tsamp;
     }
     
     public double getFch1() {
         return fch1;
     }
     
     public void setFch1(double fch1) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.fch1 = fch1;
     }
     
     public double getFoff() {
         return foff;
     }
     
     public void setFoff(double foff) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.foff = foff;
     }
     
     public double getRefdm() {
         return refdm;
     }
     
     public void setRefdm(double refdm) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.refdm = refdm;
     }
     
     public double getAz_start() {
         return az_start;
     }
     
     public void setAz_start(double az_start) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.az_start = az_start;
     }
     
     public double getZa_start() {
         return za_start;
     }
     
     public void setZa_start(double za_start) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.za_start = za_start;
     }
     
     public double getSrc_raj() {
         return src_raj;
     }
     
     public void setSrc_raj(double src_raj) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.src_raj = src_raj;
     }
     
     public double getSrc_dej() {
         return src_dej;
     }
     
     public void setSrc_dej(double src_dej) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.src_dej = src_dej;
     }
     
     public double getGal_l() {
         return gal_l;
     }
     
     public void setGal_l(double gal_l) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.gal_l = gal_l;
     }
     
     public double getGal_b() {
         return gal_b;
     }
     
     public void setGal_b(double gal_b) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.gal_b = gal_b;
     }
     
     public double getHeader_tobs() {
         return header_tobs;
     }
     
     public void setHeader_tobs(double header_tobs) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.header_tobs = header_tobs;
     }
     
     public double getRaw_fch1() {
         return raw_fch1;
     }
     
     public void setRaw_fch1(double raw_fch1) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.raw_fch1 = raw_fch1;
     }
     
     public double getRaw_foff() {
         return raw_foff;
     }
     
     public void setRaw_foff(double raw_foff) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.raw_foff = raw_foff;
     }
     
     public int getNbeams() {
         return nbeams;
     }
     
     public void setNbeams(int nbeams) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.nbeams = nbeams;
     }
     
     public int getIbeam() {
         return ibeam;
     }
     
     public void setIbeam(int ibeam) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.ibeam = ibeam;
     }
     
     public double getSrcl() {
         return srcl;
     }
     
     public void setSrcl(double srcl) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.srcl = srcl;
     }
     
     public double getSrcb() {
         return srcb;
     }
     
     public void setSrcb(double srcb) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.srcb = srcb;
     }
     
     public double getAst0() {
         return ast0;
     }
     
     public void setAst0(double ast0) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.ast0 = ast0;
     }
     
     public double getLst0() {
         
         return lst0;
     }
     
     public void setLst0(double lst0) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.lst0 = lst0;
     }
     
     public long getWapp_scan_number() {
         return wapp_scan_number;
     }
     
     public void setWapp_scan_number(long wapp_scan_number) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.wapp_scan_number = wapp_scan_number;
     }
     
     public String getProject() {
         return project;
     }
     
     public void setProject(String project) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.project = project;
     }
     
     public String getCulprits() {
         return culprits;
     }
     
     public void setCulprits(String culprits) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.culprits = culprits;
     }
     
     public double[] getAnalog_power() {
         return analog_power;
     }
     
     public void setAnalog_power(double[] analog_power) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.analog_power = analog_power;
     }
     
     public double[] getFrequency_table() {
         return frequency_table;
     }
     
     public void setFrequency_table(double[] frequency_table) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.frequency_table = frequency_table;
     }
     
     public long getNpuls() {
         return npuls;
     }
     
     public void setNpuls(long npuls) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.npuls = npuls;
     }
     
     public int getNbins() {
         return nbins;
     }
     
     public void setNbins(int nbins) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.nbins = nbins;
     }
     
     public int getHeaderLength() {
         return headerLength;
     }
     
     public void setHeaderLength(int headerLength) {
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         this.headerLength = headerLength;
     }
     
     public Coordinate getCoordinate(){
         if(coordinate == null){
             int rahr = (int)(getSrc_raj()/10000);
             int ramin = (int)(getSrc_raj()/100) - rahr*100;
             double rasec = getSrc_raj() - rahr*10000 - ramin*100;
             
             int decdeg = (int)Math.abs(getSrc_dej()/10000);
            int decamin = Math.abs((int)(getSrc_dej()/100) - decdeg*100);
             double decasec = Math.abs(Math.abs(getSrc_dej()) - Math.abs(decdeg*10000.0
                     ) - decamin*100.0);
            
             
             RA ra = new RA(rahr,ramin,rasec);
             Dec dec= new Dec(decdeg,decamin,decasec,getSrc_dej()<0);
             
             coordinate = new Coordinate(ra,dec);
             
         }
         
         return coordinate;
     }
     
     
     public void setCoordinate(Coordinate coord){
         if(written)throw new UnsupportedOperationException("Parameters cannot be changed once the header has been written to a file!");
         double ra = (coord.getRA().getHours()*10000)
         +(coord.getRA().getMinutes()*100)
         +(coord.getRA().getSeconds());
         
         
         
         double dec= coord.getDec().getDegrees()*10000
                 +coord.getDec().getArcmins()*100
                 + coord.getDec().getArcseconds();
         
         this.setSrc_raj(ra);
         this.setSrc_dej(dec);
         
     }
     
     public static void writeASCII(ByteBuffer out, String text) throws IOException{
         out.putInt(text.length());
         for(char c : text.toCharArray()){
             int i = (int)c;
             if(i > 255)i = 0;
             out.put((byte)i);
         }
         
     }
     public static String readASCII(ByteBuffer in, int npoints) throws IOException{
         
         char[] carr = new char[npoints];
         for(int i = 0 ; i < npoints; i++){
             carr[i] = (char)in.get();
         }
         
         return new String(carr);
         
     }
     
     public boolean isWritten() {
         return written;
     }
     
 }
