 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.validation;
 
 import java.util.Locale;
 
 import javax.swing.JTextField;
 
 import org.bh.gui.IBHModelComponent;
 import org.bh.gui.swing.comp.BHTextField;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.BHTranslator;
 
 import com.jgoodies.validation.ValidationResult;
 
 /**
  * This class contains validation rules to check a textfield's content to be doubles.
  * 
  * @author Robert Vollmer, Patrick Heinz
  * @version 1.0, 22.01.2010
  * 
  */
 public class VRIsDouble extends ValidationRule {
 	public static final VRIsDouble INSTANCE = new VRIsDouble();
 	
 	private VRIsDouble() {
 	}
 
 	@Override
 	public ValidationResult validate(IBHModelComponent comp) {
 		ValidationResult validationResult = new ValidationResult();
 		if (comp instanceof JTextField || comp instanceof BHTextField) {
 			BHTextField tf_toValidate = (BHTextField) comp;
 			double value = Services.stringToDouble(tf_toValidate.getText());
			if (tf_toValidate.getText().contains(".") && BHTranslator.getLoc() == Locale.GERMAN && ! tf_toValidate.getText().matches("((-?)([1-9]\\d{0,2})(.\\d{3})*)")&& !tf_toValidate.getText().contains(","))
 				validationResult.addError(translator.translate("Efield") + " '"
 						+ translator.translate(tf_toValidate.getKey()) + "' "
 						+ translator.translate("EisDouble"));
 			
			else if (tf_toValidate.getText().contains(",") && BHTranslator.getLoc()  == Locale.ENGLISH && ! tf_toValidate.getText().matches("((-?)([1-9]\\d{0,2})(,\\d{3})*)")&& !tf_toValidate.getText().contains("."))
 			validationResult.addError(translator.translate("Efield") + " '"
 					+ translator.translate(tf_toValidate.getKey()) + "' "
 					+ translator.translate("EisDouble"));
 			else if (Double.isNaN(value)) {
 				validationResult.addError(translator.translate("Efield") + " '"
 						+ translator.translate(tf_toValidate.getKey()) + "' "
 						+ translator.translate("EisDouble"));
 			}
 		}
 		return validationResult;
 	}
 }
