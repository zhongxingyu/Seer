 package com.mingebag.grover.falsebook.ic.datatype.data;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.*;
 import org.bukkit.block.Sign;
 
 public class MCDEBUG extends BaseDataChip {
     public MCDEBUG() {
         this.ICName = "DEBUG VALUE";
         this.ICNumber = "[MCDEBUG]";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(true, false, false, "Probe", "", "");
         this.chipState.setOutputs("", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "Shows value.";
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if (currentInputs.isInputOneHigh() != previousInputs.isInputOneHigh()) {
             BaseData data = this.getData(signBlock);
             
             if (data == null) {
                 signBlock.setLine(2, currentInputs.isInputOneHigh() ? "true" : "false");
             }
             else if(data.getType() == DataTypes.NUMBER) {
                 NumberData nData = (NumberData)data;
                 signBlock.setLine(2, "n=" + nData.getInt());
             }
             else if(data.getType() == DataTypes.STRING) {
                 StringData sData = (StringData)data;
                 String str = "s=" + sData.getString();
                int len = str.length();
                
                signBlock.setLine(2, str.substring(0, len));
                if(len > 15) {
                    signBlock.setLine(3, str.substring(15, len));
                 }
                 else {
                     signBlock.setLine(3, "");
                 }
             }
             else if(data.getType() == DataTypes.PLAYER) {
                 PlayerData nData = (PlayerData)data;
                 signBlock.setLine(2, "player");
             }
             else if(data.getType() == DataTypes.ITEM) {
                 //ItemData nData = (ItemData)data;
                 signBlock.setLine(2, "item");
             }
             
             signBlock.update(true);
         }
     }
 }
