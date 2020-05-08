 package com.mingebag.grover.falsebook.ic.datatype.player;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.SignChangeEvent;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.bukkit.gemo.utils.SignUtils;
 import com.grover.mingebag.ic.BaseData;
 import com.grover.mingebag.ic.BaseDataChip;
 import com.grover.mingebag.ic.DataTypes;
 import com.grover.mingebag.ic.NumberData;
 import com.grover.mingebag.ic.PlayerData;
 
 public class PlayerValueInt extends BaseDataChip {
 
     public PlayerValueInt() {
         this.ICName = "Player Value int";
         this.ICNumber = "ic.player.value";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(true, false, false, "Player", "", "");
         this.chipState.setOutputs("Int", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "This pulses an int";
     }
 
     public void checkCreation(SignChangeEvent event) {
        String value = event.getLine(2).toUpperCase();
         if (value.length() > 0) {
             if ((value.equals("HEALTH") || value.equals("XP") || value.equals("LEVEL") || value.equals("HUNGER"))) {
                 return;
             }
         }
         SignUtils.cancelSignCreation(event, "Invalided value specified, must be: health, level, hunger");
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if (currentInputs.isInputOneHigh() && previousInputs.isInputOneLow()) {
             BaseData data = getData(signBlock);
             if (data.getType() == DataTypes.PLAYER) {
                 PlayerData pdata = (PlayerData) data;
                String value = signBlock.getLine(2).toUpperCase();
 
                 if (value.length() > 0) {
 
                     if (value.equals("HEALTH")) {
                         this.outputData(new NumberData(pdata.getPlayer().getHealth()), signBlock, 2);
                         return;
                     }
 
                     if (value.equals("XP")) {
                         this.outputData(new NumberData(pdata.getPlayer().getTotalExperience()), signBlock, 2);
                         return;
                     }
 
                     if (value.equals("LEVEL")) {
                         this.outputData(new NumberData(pdata.getPlayer().getLevel()), signBlock, 2);
                         return;
                     }
 
                     if (value.equals("HUNGER")) {
                         this.outputData(new NumberData(pdata.getPlayer().getFoodLevel()), signBlock, 2);
                         return;
                     }
                 }
             }
 
         }
     }
 }
