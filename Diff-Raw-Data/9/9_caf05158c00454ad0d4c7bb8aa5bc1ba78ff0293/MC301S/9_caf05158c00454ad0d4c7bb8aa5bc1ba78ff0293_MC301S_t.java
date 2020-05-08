 package com.mingebag.grover.falsebook.ic.datatype.string;
 
 import org.bukkit.block.Sign;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.*;
 import org.bukkit.Instrument;
 import org.bukkit.Material;
 import org.bukkit.Note;
 import org.bukkit.Note.Tone;
 import org.bukkit.block.Block;
 import org.bukkit.block.NoteBlock;
 
 public class MC301S extends BaseDataChip {
    public MC301S() {
         this.ICName = "PLAY NOTE";
         this.ICNumber = "MC301S";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(true, false, false, "String", "", "");
         this.chipState.setOutputs("", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "When clocked, plays the first note in the string.";
     }
     
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if(currentInputs.isInputOneHigh() && previousInputs.isInputOneLow()) {
             BaseData baseCenter = this.getData(signBlock);
             Block block = getICBlock(signBlock).add(0, 1, 0).getBlock();
             Material blockMat = block.getType();
             
             if(baseCenter.getType() == DataTypes.STRING.ordinal() &&
                     blockMat.equals(Material.NOTE_BLOCK)) {
                 
                 StringData center = (StringData)baseCenter;
                 
                 String str = center.getString();
                 
                 if(str.length() < 3)
                     return;
                 
                 str = str.toLowerCase();
                 str = str.trim();
                 
                 String instrumentStr = signBlock.getLine(2);
                 Instrument instrument = Instrument.PIANO;
                 try {
                     instrument = Instrument.valueOf(instrumentStr.toUpperCase());
                 }
                 catch(Exception e) {
                     
                 }
                 
                 char octave = str.charAt(0);
                 char tone = str.charAt(1);
                 
                 boolean isSharp = false;
                 boolean isFlat = false;
                 if(str.length() > 2)
                 {
                     char sharpFlat = str.charAt(2);
                     isSharp = sharpFlat == '#';
                     isFlat = sharpFlat == '-';
                 }
                 
                 int octaveValue = 0;
                 try {
                     octaveValue = Integer.parseInt(String.valueOf(octave));
                 }
                 catch(Exception e) {
                     
                 }
                 
                 if(octaveValue < 0)
                     octaveValue = 0;
                 if(octaveValue > 2)
                     octaveValue = 2;
 
                 Tone toneValue = Tone.C;
                 try {
                     toneValue = Tone.valueOf(String.valueOf(tone).toUpperCase());
                 }
                 catch(Exception e) {
                     
                 }
                 
                 Note note = null;
                 if(isSharp)
                     note = Note.sharp(octaveValue, toneValue);
                 else if(isFlat)
                     note = Note.flat(octaveValue, toneValue);
                 else
                     note = Note.natural(octaveValue, toneValue);
                 
                 NoteBlock noteBlock = (NoteBlock)block;
                 noteBlock.play(instrument, note);
             }
         }
     }
 }
