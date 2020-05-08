 package de.elatexam.editor.components.panels.tasks.cloze;
 
 import de.elatexam.model.ClozeSubTaskDef.Cloze.Gap;
 import de.elatexam.model.ClozeSubTaskDef.Cloze.Gap.GapCorrectItem;
 
 /**
  * @author Steffen Dienst
  *
  */
 public class ClozePreviewConverter extends ClozeConverter {
 	@Override
 	protected String createGapText(Gap gap) {
 		StringBuilder sb = new StringBuilder("");
 		sb.append("<input type=\"text\" value=\"");
		sb.append(gap.getInitialValue()).append('"');
 		int maxLen = 0;
 		for (GapCorrectItem ci : gap.getCorrectItems()) {
 			maxLen = Math.max(maxLen, ci.getItem().length());
 		}
 		removeTrailingSplitchar(sb);
 
 		if (gap.getInputLength() != null && gap.getInputLength() > maxLen) {
 			maxLen = gap.getInputLength();
 		}
 
 		sb.append("\" size=\"" + maxLen + "\"");
 		sb.append(" disabled=\"disabled\"/>");
 		return sb.toString();
 	}
 }
