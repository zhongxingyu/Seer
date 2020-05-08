 /**
  * 
  */
 
 package com.konakartadmin.modules.payment.gestpay;
 
 import java.util.Date;
 
 import com.konakart.bl.modules.payment.gestpay.GestpayConstants;
 import com.konakart.util.Utils;
 import com.konakartadmin.app.KKConfiguration;
 import com.konakartadmin.bl.KKAdminBase;
 import com.konakartadmin.modules.PaymentModule;
 
 /**
  * @author lrkwz
  */
 
 public class Gestpay extends PaymentModule {
 	/**
 	 * @return the config key stub
 	 */
 	public String getConfigKeyStub() {
 		if (configKeyStub == null) {
 			setConfigKeyStub(super.getConfigKeyStub() + "_GESTPAY");
 		}
 		return configKeyStub;
 	}
 
 	public String getModuleTitle() {
 		return getMsgs().getString("MODULE_PAYMENT_GESTPAY_TEXT_TITLE");
 	}
 
 	/**
 	 * @return the implementation filename
 	 */
 	public String getImplementationFileName() {
 		return "Gestpay";
 	}
 
 	/**
 	 * @return an array of configuration values for this payment module
 	 */
 	public KKConfiguration[] getConfigs() {
 		if (configs == null) {
 			configs = new KKConfiguration[8];
 		}
 
 		if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey())) {
 			return configs;
 		}
 
 		Date now = KKAdminBase.getKonakartTimeStampDate();
 
 		int i = 0;
 		int groupId = 6;
 
 		configs[i] = new KKConfiguration(
 		/* title */"Gestpay Status", GestpayConstants.MODULE_PAYMENT_GESTPAY_STATUS,
 		/* value */"true",
 		/* description */"If set to false, the Gestpay module will be unavailable",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Sort order of display", GestpayConstants.MODULE_PAYMENT_GESTPAY_SORT_ORDER,
 		/* value */"0",
 		/* description */"Sort Order of Gestpay module on the UI. Lowest is displayed first.",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Gestpay Payment Zone", GestpayConstants.MODULE_PAYMENT_GESTPAY_ZONE,
 		/* value */"0",
 		/* description */"Zone where Gestpay module can be used. Otherwise it is disabled.",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"tep_get_zone_class_title",
 		/* setFun */"tep_cfg_pull_down_zone_classes(",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Callback username (?)", GestpayConstants.MODULE_PAYMENT_GESTPAY_CALLBACK_USERNAME,
 		/* value */"customer@test.com",
 		/* description */"Valid username for KonaKart. Used by the callback"
 				+ " code to log into KonaKart in order to make an engine call",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Callback Password", GestpayConstants.MODULE_PAYMENT_GESTPAY_CALLBACK_PASSWORD,
 		/* value */"customerpassword",
 		/* description */"Valid password for KonaKart. Used by the callback"
 				+ " code to log into KonaKart in order to make an engine call",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Request URL", GestpayConstants.MODULE_PAYMENT_GESTPAY_REQUEST_URL,
 		/* value */"https://ecomm.sella.it/gestpay/pagam.asp",
 		/* description */"URL used by KonaKart to send the transaction details (usually https://ecomm.sella.it/gestpay/pagam.asp for production use, https://testecomm.sella.it/gestpay/pagam.asp for test mode)",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Send buyer name", GestpayConstants.MODULE_PAYMENT_GESTPAY_SEND_BUYER_NAME,
 		/* value */ "true",
 		/* description */"Depending on sella contract the payment server can accept the buyer name",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"Send buyer email", GestpayConstants.MODULE_PAYMENT_GESTPAY_SEND_BUYER_EMAIL,
 		/* value */ "true",
 		/* description */"Depending on sella contract the payment server can accept the buyer email",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
		/* title */"Is the shop in test mode ?", GestpayConstants.MODULE_PAYMENT_GESTPAY_SEND_BUYER_LANGUAGE,
 		/* value */ "false",
 		/* description */"True for multi-language sella account",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
		/* title */"Is the shop in test mode ?", GestpayConstants.MODULE_PAYMENT_GESTPAY_SEND_CUSTOMINFO,
 		/* value */ "false",
 		/* description */"Depending on sella contract the payment server can accept custom info (i.e the billing country)",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
		/* title */"Is the shop in test mode ?", GestpayConstants.MODULE_PAYMENT_GESTPAY_CURRENCY,
 		/* value */ "false",
 		/* description */"True for multi currency sella account",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"tep_cfg_select_option(array('true', 'false'), ",
 		/* dateAdd */now);
 
 		configs[i] = new KKConfiguration(
 		/* title */"GESTPAY Shop Id", GestpayConstants.MODULE_PAYMENT_GESTPAY_SHOP_ID,
 		/* value */"GESPAY47826",
 		/* description */"The GESTPAY shop ID for this installation",
 		/* groupId */groupId,
 		/* sortO */i++,
 		/* useFun */"",
 		/* setFun */"",
 		/* dateAdd */now);
 
 		return configs;
 	}
 }
