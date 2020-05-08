 package nobugs.nolife.mw.util;
 
 import java.util.logging.Logger;
 
 import nobugs.nolife.mw.entities.Material;
 import nobugs.nolife.mw.entities.TaggedMaterial;
 
 /**
  * Material[eBeB
  * @author kazyury
  *
  */
 public class MaterialUtil {
 	private static Logger logger = Logger.getGlobal();
 
 	/**
 	 * fނ̎BeNԋp
 	 * @param m
 	 * @return
 	 */
 	public static String getMaterialYearMonthDate(Material m){ return m.getMaterialId().substring(0, 8); }
 	public static String getMaterialYearMonth(Material m){ return m.getMaterialId().substring(0, 6); }
 	public static String getMaterialYear(Material m) { return m.getMaterialId().substring(0, 4); }
 	public static String getMaterialMonth(Material m){ return m.getMaterialId().substring(4, 6); }
 	public static String getMaterialDate(Material m){ return m.getMaterialId().substring(6, 8); }
 	public static String getMaterialHour(Material m){ return m.getMaterialId().substring(8, 10); }
 	public static String getMaterialMinute(Material m){ return m.getMaterialId().substring(10, 12); }
 	public static String getMaterialSecond(Material m){ return m.getMaterialId().substring(12, 14); }
 
 	
 	/**
 	 * ֘ATaggedMaterial̏ԂɊÂMaterial̏ԂXVB
 	 * @param m
 	 */
 	public static void updateMaterialState(Material m) {
 		// 1TaggedMaterial݂ȂInstalledɐݒ肵ďI
 		if(m.getTaggedMaterials().isEmpty()) {
 			m.setMaterialState(Constants.MATERIAL_STATE_INSTALLED);
 			logger.info("MaterialMATERIAL_STATE_INSTALLEDɐݒ肳܂(TaggedMaterial:0)");
 			return;
 		}
 
 		// 1łTaggedMaterialStagedStagedɐݒ肵ďI
 		for(TaggedMaterial tm:m.getTaggedMaterials()){
 			if(tm.getTagState().equals(Constants.TAG_STATE_STAGED)){
 				m.setMaterialState(Constants.MATERIAL_STATE_STAGED);
 				logger.info("MaterialMATERIAL_STATE_STAGEDɐݒ肳܂(TAG_STATE_STAGED:݂)");
 				return;
 			}
 		}
 
 		// 1łTaggedMaterialNotInUseInstalledɐݒ肵ďI(Staged^O݂͑Ȃ̂)
 		for(TaggedMaterial tm:m.getTaggedMaterials()){
 			if(tm.getTagState().equals(Constants.TAG_STATE_NOT_IN_USE)){
 				m.setMaterialState(Constants.MATERIAL_STATE_INSTALLED);
 				logger.info("MaterialMATERIAL_STATE_INSTALLEDɐݒ肳܂(TAG_STATE_NOT_IN_USE:݂)");
 				return;
 			}
 		}
 		// L̉ɂȂꍇ͑SPublishedȂ̂IN-USEɐݒ
 		m.setMaterialState(Constants.MATERIAL_STATE_IN_USE);
		logger.info("MaterialMATERIAL_STATE_INSTALLEDɐݒ肳܂(TAG_STATE_NOT_IN_USE:݂)");
 		return;
 	}

 }
