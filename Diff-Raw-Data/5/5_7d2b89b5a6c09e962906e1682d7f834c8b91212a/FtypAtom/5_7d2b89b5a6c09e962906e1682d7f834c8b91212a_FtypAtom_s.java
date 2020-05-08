 package com.hotcats.mp4artextractor.data.atom;
 
 import java.util.List;
 
 public class FtypAtom extends Atom {
 
   private final byte[] majorBrand;
   private final byte[] minorVersion;
   private final List<byte[]> compatibleBrands;
 
   public FtypAtom(int size, byte[] majorBrand, byte[] minorVersion, List<byte[]> compatibleBrands) {
     super(AtomType.FTYP, size);
     this.majorBrand = majorBrand;
     this.minorVersion = minorVersion;
     this.compatibleBrands = compatibleBrands;
   }
 
   public byte[] getMajorBrand() {
     return majorBrand;
   }
 
  public byte[] getMinorVerison() {
     return minorVersion;
   }
 
   public List<byte[]> getCompatibleBrands() {
     return compatibleBrands;
   }
 
   @Override
   public String toString() {
     StringBuilder builder = new StringBuilder();
     builder.append(super.toString());
     builder.append("  major brand: ").append(bytesToString(getMajorBrand())).append('\n');
    builder.append("  minor version: ").append(bytesToString(getMajorBrand())).append('\n');
     builder.append("  compatible brands:").append('\n');
     for (byte[] compatibleBrand : getCompatibleBrands()) {
       builder.append("    ").append(bytesToString(compatibleBrand)).append('\n');
     }
     return builder.toString();
   }
 }
