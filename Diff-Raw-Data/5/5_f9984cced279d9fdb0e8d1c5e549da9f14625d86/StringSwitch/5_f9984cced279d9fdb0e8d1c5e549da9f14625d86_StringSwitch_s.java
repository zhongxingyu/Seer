 package com.mingebag.grover.falsebook.ic.datatype.string;
 
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
 import com.grover.mingebag.ic.StringData;
 
 public class StringSwitch extends BaseDataChip {
 
     public StringSwitch() {
         this.ICName = "String Switch";
         this.ICNumber = "ic.s.switch";
         setICGroup(ICGroup.CUSTOM_0);
        this.chipState = new BaseChip(true, false, false, "Datatype", "", "");
         this.chipState.setOutputs("String", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "If int input matches line 3, output string from line 4; otherwise output int.";
     }
     
     public void checkCreation(SignChangeEvent event) {
     	try {
     		Integer.parseInt(event.getLine(2));
     	} catch (Exception e) {
     		SignUtils.cancelSignCreation(event, "Line 3 must be a valid Integer!");
     	}
     	
     	if(event.getLine(3).length() == 0) {
     		SignUtils.cancelSignCreation(event, "Line 4 must be a string!");
     	}
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if (currentInputs.isInputOneHigh() && previousInputs.isInputOneLow()) {
         	
             BaseData data = getData(signBlock);
 
             if (data.getType() == DataTypes.NUMBER) {
                 NumberData nData = (NumberData) data;
                 if (signBlock.getLine(2).equals(Integer.toString(nData.getInt()))) {
                 	this.outputData(new StringData(signBlock.getLine(3)), signBlock, 2);
                } else {
                	this.outputData(nData, signBlock, 2);
                 }
             }
 
         }
     }
 }
