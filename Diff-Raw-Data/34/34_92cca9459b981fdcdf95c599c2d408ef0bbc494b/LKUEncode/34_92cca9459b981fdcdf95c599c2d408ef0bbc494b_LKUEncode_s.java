 package com.cs.fwk.core.config.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cs.fwk.api.AttrSysType;
 import com.cs.fwk.api.Attribute;
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.gridattr.GridAttribute;
 import com.cs.fwk.api.gridattr.GridRowData;
 import com.cs.fwk.api.util.AttrConfigGenerator;
 import com.cs.fwk.api.util.ConfigItem;
 
 /**
  * Encode config for a grid based Synthetic Instrument list
  * 
  * Format of grid is 0:target 1:function, 2:arg, 3:type, 4:event, 5:channel
  * Configs created are EL_LKU_C_CHN_SPEC.
  * 
  *--				C_CHN_SPEC	= CB.FN.ARG.[TY.EV.IP]
  *--					CB  = id of CB to address, or FF (8 bit)
  *--					FN  = id of LKU or TD fn (8 bit)
  *--					ARG = arg to provide to LKU fn (8 bit) or Tickdata bitselect (0:low 32 bits, 1: high 32 bits)
  *--					TY  = 00:Tickdata, 01:LKU (2 bit)
  *--					EV  = TF - sens to ip=True,False, or both. (2 bit)
  *--					IP  = which input to apply fn to (4 bit = 0,1,2,3)
  * @author pjf
  *
  */
 public class LKUEncode implements AttrConfigGenerator {
 
 	
 	@Override
 	public List<ConfigItem> generate(Attribute attr) throws Exception {
 		List<ConfigItem> configs = new ArrayList<ConfigItem>();
 		GridAttribute ga = (GridAttribute) attr;
 		int elId = attr.getParent().getId();
 		
 		// build config item for each row in the grid
 		
 		int rows = ga.getNumRows();
 		for (int row=0; row<rows; row++) {			
 			GridRowData rd = ga.getRow(row);
 			// get data from the row
 			int target = rd.getInt(0);
 			int function = rd.getInt(1);
 			int arg = rd.getInt(2);
 			int type = rd.getInt(3);
 			int event = rd.getInt(4);
 			int channel = rd.getInt(5);
 			// encode into the 32 bit config value
 			if (target == 0) {
				target = 0xff;
 			}
 			channel--;
 			int cfgData = (target << 24) + (function << 16) + (arg << 8) + (type << 6) + (event << 4) + channel;
 			ConfigItem cfg = new ConfigItem(elId,AttrSysType.NORMAL,
 						MatElementDefs.EL_LKU_C_CHN_SPEC, 0, cfgData);
 			configs.add(cfg);
 			row++;
 		}
 
 		return configs;
 	}
 
 }
