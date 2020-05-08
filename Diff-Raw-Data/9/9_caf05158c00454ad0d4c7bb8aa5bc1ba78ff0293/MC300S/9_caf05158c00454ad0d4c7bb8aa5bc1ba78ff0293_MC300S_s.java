 package com.mingebag.grover.falsebook.ic.datatype.string;
 
 import org.bukkit.block.Sign;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.grover.mingebag.ic.*;
 
 public class MC300S extends BaseDataChip {
    MC300S() {
         this.ICName = "SUB STRING";
         this.ICNumber = "MC300S";
         setICGroup(ICGroup.CUSTOM_0);
         this.chipState = new BaseChip(true, false, false, "String", "Index", "Count");
         this.chipState.setOutputs("String", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "This pulses a substring when clocked.";
     }
     
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         boolean hasChanged = previousInputs.isInputOneLow() || previousInputs.isInputTwoLow() || previousInputs.isInputThreeLow();
         hasChanged = hasChanged && currentInputs.isInputOneHigh() && currentInputs.isInputTwoHigh() && currentInputs.isInputThreeHigh();
         
         if(hasChanged) {
             
             BaseData baseCenter = this.getData(signBlock);
             BaseData baseLeft = this.getDataLeft(signBlock);
             BaseData baseRight = this.getDataRight(signBlock);
             
             // Check types.
             if(baseCenter.getType() == DataTypes.STRING.ordinal() &&
                 baseLeft.getType() == DataTypes.NUMBER.ordinal() &&
                 baseRight.getType() == DataTypes.NUMBER.ordinal()) {
                 
                 StringData center = (StringData)baseCenter;
                 NumberData left = (NumberData)baseLeft;
                 NumberData right = (NumberData)baseRight;
                 
                 String str = center.getString();
                 
                 // Sanity checks
                 if(str == null || str.length() == 0)
                     return;
                 
                 int offset = left.getInt();
                 if(offset < 0)
                     offset = 0;
                 
                 if(offset > str.length())
                     offset = str.length();
                 
                 int count = right.getInt();
                 if(count < 0)
                     count = 0;
                 
                 // Substr
                 str = str.substring(offset, offset + count);
                 
                 StringData data = new StringData(str);
                 this.outputData(data, data.getType(), signBlock, 2, 20);
             }
         }
     }
 }
