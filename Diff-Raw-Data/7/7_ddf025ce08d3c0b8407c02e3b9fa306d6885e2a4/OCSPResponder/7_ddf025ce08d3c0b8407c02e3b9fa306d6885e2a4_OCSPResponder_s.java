 package com.reschly.ocsplatency;
 
 
 public class OCSPResponder
 {
 	private byte[] request;
 	private String host;
 	private int port;
 	private String displayName;
 	
 	private static final String addtrustRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a050004147cb166549cabdb44ee622616adf4657bf77ad5940414adbd987a34b426f7fac42654ef03bde024cb541a02101690c329b6780607511f05b0344846cb";
 	private static final String addtrustHost = "ocsp.usertrust.com";
 	private static final int addtrustPort = 80;
 	private static final String addtrustDisplayName = "AddTrust External CA Root";
 	
 	/* Host not responding */
 	private static final String anceRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a050004141a0a547b1228847e185cf7aca14728f7c784dc0704149ec10d334979abb3b193066033a96a44f4b08333021011191109081309061679e4394efdf50d";
 	private static final String anceHost = "ocsp.certification.tn";
 	private static final int ancePort = 80;
 	private static final String anceDisplayName = "Agence Nationale de Certification Electronique";
 	
 	/* Can't find issuing certificate */
 	/*private static final String ancertRequestString = "";
 	private static final String ancertHost = "ocsp.ac.ancert.com"; // URL=http://ocsp.ac.ancert.com/ocsp.xuda
 	private static final int ancertPort = 80;
 	private static final String ancertDisplayname = "ANCERT Certificados Notariales de Sistemas V2";
 	*/
 	
 	private static final String buypassRequestString = "504f5354202f6f6373702f4250436c6173733343413120485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037380d0a0d0a304c304a304830463044300906052b0e03021a05000414160239efe47f84caa8b7901b07d2368f22214ec604143814e6c8f0a9a403f44e3e22a35bf2d6e0ad4074020b113faa1ea936a50d310fdb";
 	private static final String buypassHost = "ocsp.buypass.no";
 	private static final int buypassPort = 80;
 	private static final String buypassDisplayName = "Buypass Class 3 CA 1";
 	
 	/* ocvs.gov.in doesn't have a dns entry */
 	private static final String ccaIndia2011RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2036390d0a0d0a30433041303f303d303b300906052b0e03021a050004148899f5fd3ec020d075e5a3bd52b2af4167036c0d041417e6174b7d9c61c9d28dab309bbabe23f1601e7302022792";
 	private static final String ccaIndia2011Host = "ocvs.gov.in";
 	private static final int ccaIndia2011Port = 80;
 	private static final String ccaIndia2011DisplayName = "CCA India 2011";
 	
 	private static final String certignaRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a486f73743a2073736c2e6f6373702e6365727469676e612e66720d0a0d0a30523050304e304c304a300906052b0e03021a05000414ec366e18830d1ac5229e30ef2525305ed4a6656004147d637802ca4f56301a0b03cb915f0e3bc8ae4cb8021100b0b89d28e2d9aaecacbac39d80b1733a";
 	private static final String certignaHost = "ssl.ocsp.certigna.fr";
 	private static final int certignaPort = 80;
 	private static final String certignaDisplayName = "Certigna SSL";
 	
 	private static final String certumEVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041453c989243fc25295a68b31bfb44898a7e7506a520414c03b0afa0fd9fe0f4e029865140c8a1e2e008c030210412ba83e8c06681ecd279e17b0c66eeb";
 	private static final String certumEVHost = "evca.ocsp.certum.pl";
 	private static final int certumEVPort = 80;
 	private static final String certumEVDisplayName = "Certum Extended Validation CA";
 	
 	private static final String cihazRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037340d0a0d0a30483046304430423040300906052b0e03021a050004147c45cd6e9a1fd5e92a9f8d4f292b911896e0e9610414962a85767fb58317e0e12e34860b4b3fd86e294e02070081e18dd20a5b";
 	private static final String cihazHost = "ocspsslv3.kamusm.gov.tr";
 	private static final int cihazPort = 80;
 	private static final String cihazDisplayName = "Cihaz Sertifikasi Hizmet Saglayicisi - Surum 3";
 	
 	private static final String comodoRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a05000414fde74a84a2cc6dd61ec4743bfbbf8abe4a38a45804143fd5b5d0d64479504a17a39b8c4adcb8b022646b021100db55d2aab81bd50b36233ba3aca83005";
 	private static final String comodoHost = "ocsp.comodoca.com";
 	private static final int comodoPort = 80;
 	private static final String comodoDisplayName = "COMODO High-Assurance Secure Server CA";
 	
 	private static final String cybertrustRequestString = "504f5354202f4f63737053657276657220485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2036390d0a0d0a30433041303f303d303b300906052b0e03021a050004143ab21890618f227875dfa019187ba77c819904160414914305ecb46a154fdce1ee86565c11d02a2b8d5f020224e0";
 	private static final String cybertrustHost = "sureseries-ocsp.cybertrust.ne.jp";
 	private static final int cybertrustPort = 80;
 	private static final String cybertrustDisplayName = "Cybertrust Japan EV CA G2";
 	
 	private static final String digicertEVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414b8a299f09d061dd5c1588f76cc89ff57092b94dd04144c58cb25f0414f52f428c881439ba6a8a0e692e50210044dffee75e453ec29bfe58a767ac1c7";
 	private static final String digicertEVHost = "ocsp.digicert.com";
 	private static final int digicertEVPort = 80;
 	private static final String digicertEVDisplayName = "DigiCert High Assurance EV CA-1";
 	
 	private static final String dtrustClass3RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a05000414b2bebf94f5e30a2a426d154558448950fc6e38570414f76156f2bf5d5f8eb83aacb7b9ddf526af14b0ca02030e6767";
 	private static final String dtrustClass3Host = "ssl-c3-ca1-2009.ocsp.d-trust.net";
 	private static final int dtrustClass3Port = 80;
 	private static final String dtrustClass3DisplayName = "D-TRUST SSL Class 3 CA 1 2009";
 	
 	private static final String dtrustClass3EVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a05000414f2df2c4e376bc7c65576d2d882b6436bfae1fc560414d3bdf808a8e8641c389169963c74aec48d1a88e202030e4d3b";
 	private static final String dtrustClass3EVHost = "ssl-c3-ca1-ev-2009.ocsp.d-trust.net";
 	private static final int dtrustClass3EVPort = 80;
 	private static final String dtrustClass3EVDisplayName = "D-TRUST SSL Class 3 CA 1 EV 2009";
 
 	private static final String dtrustRootClass3EVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a050004140f4efcf05175b60e4914344f85fcf18b3f826fd8041462e8b0fa3a3c8eb99a2dc867bd5ff477306ade2a0203099064";
 	private static final String dtrustRootClass3EVHost = "root-c3-ca2-ev-2009.ocsp.d-trust.net";
 	private static final int dtrustRootClass3EVPort = 80;
 	private static final String dtrustRootClass3EVDisplayName = "D-TRUST Root Class 3 CA 2 EV 2009";
 	
 	private static final String dtrustRootClass3RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a050004144a7cdaf39dfbf67800700df49cd84af1531b09690414a737b46280e401211faff74eeccd1c05eb8947ce0203099063";
 	private static final String dtrustRootClass3Host = "root-c3-ca2-2009.ocsp.d-trust.net";
 	private static final int dtrustRootClass3Port = 80;
 	private static final String dtrustRootClass3DisplayName = "D-TRUST Root Class 3 CA 2 2009";
 	
 	private static final String eaekoRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2036390d0a0d0a30433041303f303d303b300906052b0e03021a05000414e66948616f0e1a9e3a6d682393e4bb929b533e4c0414c0a94af7472587ffbcb5a689ce82d246a889eba302020842";
 	private static final String eaekoHost = "ocsp.izenpe.com";
 	private static final int eaekoPort = 8094;
 	private static final String eaekoDisplayName = "EAEko Herri Administrazioen CA - CA AAPP Vascas (2)";
 	
 	private static final String entrustL1ERequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037310d0a0d0a304530433041303f303d300906052b0e03021a05000414a26dbff1bfd96987ba020bdf578e9a965eee1c8e04145b418ab2c443c1bdbfc85441559de096adffb9a102044c202995";
 	private static final String entrustL1EHost = "ocsp.entrust.net";
 	private static final int entrustL1EPort = 80;
 	private static final String entrustL1EDisplayName = "Entrust Certification Authority - L1E";
 	
 	/* returning internal error (2) */
 	private static final String etugraRequestString = "504f5354202f7374617475732f6f63737020485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037350d0a0d0a30493047304530433041300906052b0e03021a050004144c9d226c0809549ae952e0dc889157460aaafd2c04148f4c1c054b3b49b7fe87d797c4976eac90a5f4e2020877f489903d402371";
 	private static final String etugraHost = "ocsp.e-tugra.com";
 	private static final int etugraPort = 80;
 	private static final String etugraDisplayName = "E-Tugra EBG Web Sunucu Sertifika Hizmet Saglayicisi";
 	
 	private static final String gandiRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a050004143c482caa7d028bacb016cf642bb22b236a62c3800414b6a8ffa2a82fd0a6cd4bb168f3e7501031a7792102110084937c7f9d1c27230b25da42bae443ed";
 	private static final String gandiHost = "ocsp.gandi.net";
 	private static final int gandiPort = 80;
 	private static final String gandiDisplayName = "Gandi Standard SSL CA";
 
 	private static final String geotrustGlobalCARequstString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a05000414b1b439179016b797795011f160b9d4a23cdbedee041400f92ac34191b6c9c2b83e55f2c0971113a0072002030236d0";
 	private static final String geotrustGlobalCAHost = "ocsp.geotrust.com";
 	private static final int geotrustGlobalCAPort = 80;
 	private static final String geotrustGlobalCADisplayName = "GeoTrust Global CA";
 
 	private static final String getronicsRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414c51981765f32f9855b9b1ded6a395556f7ded267041438b285e6adf8a6d041585b786fdcd5b84476c57b021003fb40b97269534605edfa88f50db2f0";
 	private static final String getronicsHost = "ocsp2.managedpki.com";
 	private static final int getronicsPort = 80;
 	private static final String getronicsDisplayName = "Getronics CSP Organisatie CA - G2";
 	
 	private static final String globalsignRequestString = "504f5354202f6773657874656e6476616c673220485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038350d0a0d0a30533051304f304d304b300906052b0e03021a05000414a0720ea06a7c620254f2a8f59dd27ba4f3b72fa40414b0b04afd1c7528f81c61aa13f6fac1903d6b16a3021211210629afba98660632feca36b6d5425671";
 	private static final String globalsignHost = "ocsp2.globalsign.com";
 	private static final int globalsignPort = 80;
 	private static final String globalsignDisplayName = "GlobalSign Extended Validation CA - G2";
 	
 	private static final String globesslRequsetString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414be85dee184955af85dfad8c1bd448a3e5b1c10d4041445c7405b66caf4662338d465ce6d76e6ade7935e02101204b4e4fe1d8b0a4c43fa082cf1cc01";
 	private static final String globesslHost = "ocsp.globessl.com";
 	private static final int globesslPort = 80;
 	private static final String globesslDisplayName = "GlobeSSL CA";
 	
 	private static final String godaddyRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037340d0a0d0a30483046304430423040300906052b0e03021a0500041470292276537f1abc8fd53c9484e914cb762a052a0414fdac6132936c45d6e2ee855f9abae7769968cce702072b23e711fe41fd";
 	private static final String godaddyHost = "ocsp.godaddy.com";
 	private static final int godaddyPort = 80;
 	private static final String godaddyDisplayName = "Go Daddy Secure Certification Authority";
 	
 	private static final String hpPrivateClass2RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041488ed7aacfe2e7aba18009e14d4b20a36e9ec1c2a041437edf715792d30a5989a75b65c37e388ea116ad502106bcef4877ab5138891ae7402871ddfb8";
 	private static final String hpPrivateClass2Host = "hp-ocsp.symauth.com";
 	private static final int hpPrivateClass2Port = 80;
 	private static final String hpPrivateClass2DisplayName = "Hewlett-Packard Private Class 2 Certification Authority";
 	
 	private static final String keynectisEVRequestString = "504f5354202f657673736c2d6f6373702f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038350d0a0d0a30533051304f304d304b300906052b0e03021a05000414d5c0fddba0d0505d14f9d4411d4e853d43a3abfa041413c1322e925caecd6d8fa2f3e0b449b486f4a25c02121127bc78605dbdb4ce62ffce9b65cb0351da";
 	private static final String keynectisEVHost = "kvalid.keynectis.com";
 	private static final int keynectisEVPort = 80;
 	private static final String keynectisEVDisplayName = "KEYNECTIS Extended Validation CA";
 	
 	private static final String ipscaGlobalRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038370d0a486f73743a206f6373706c6576656c3130312e69707363612e636f6d0d0a0d0a305530533051304f304d300906052b0e03021a05000414b2e49703197bb1d21f1c1ee1190fb962a1c72eec041415a69680b1154b31c3c29cf6e7130b4bf318cd8602141000000000000000000000000000000000000023";
 	private static final String ipscaGlobalHost = "ocsplevel101.ipsca.com";
 	private static final int ipscaGlobalPort = 80;
 	private static final String ipscaGlobalDisplayName = "ipsCA Global CA Root";
 	
 	private static final String ipscaLevel1RequestString = "504f5354202f6f63737020485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038370d0a486f73743a206f6373706c6576656c3130312e69707363612e636f6d0d0a0d0a305530533051304f304d300906052b0e03021a05000414cd42693e2b2d678bc47946d89df7cfb3317b6bf204147d9e6b0efc13e1c48ba77f61ae6021cfcda0d306021410ec36f0b61ae143332a1875bd751a942dbce3f6";
 	private static final String ipscaLevel1Host = "ocsplevel101.ipsca.com";
 	private static final int ipscaLevel1Port = 80;
 	private static final String ipscaLevel1DisplayName = "ipsCA Level 1 CA";
 	
 	private static final String networksolutionsRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a050004143cee0edaa1afab60e60c8837c1ef38dd303e797004143c41e28f0808a94c25898d6dc538d0fc858c6217021100cbc2e684b647dfdfc376c49280e37eb7";
 	private static final String networksolutionsHost = "ocsp.netsolssl.com";
 	private static final int networksolutionsPort = 80;
 	private static final String networksolutionsDisplayName = "Network Solutions Certificate Authority";
 	
 	private static final String ocioRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037310d0a0d0a304530433041303f303d300906052b0e03021a05000414f7452a008060152772e4a135e76e9e52fde0f15804147ae1daa9f0d473746de9841eeaa1218aa189a84c02044b6ae2bb";
 	private static final String ocioHost = "ocsp.treas.gov";
 	private static final int ocioPort = 80;
 	private static final String ocioDisplayName = "OCIO CA";
 	
 	private static final String quovadisRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038370d0a0d0a305530533051304f304d300906052b0e03021a0500041479b0f2901f6a429a8117a420ac45a0272421ce6e0414324da14feaf0ae99b6ee9b072c840811508be27e0214417aa7a0ab5dd495c8318b91fa7eec0fd0214b95";
 	private static final String quovadisHost = "ocsp.quovadisglobal.com";
 	private static final int quovadisPort = 80;
 	private static final String quovadisDisplayname = "QuoVadis Global SSL ICA";
 
 	private static final String secomRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414a1a5f1887a3eddb8fae4aedf09710b6fe446d420041496f19244e1497b21fe506a3a61b70c61b809d73402104b0d482933bc46430000000046670a01";
 	private static final String secomHost = "ev.ocsp.secomtrust.net";
 	private static final int secomPort = 80;
 	private static final String secomDisplayName = "SECOM Passport for Web EV CA";
 	
 	private static final String securetrustRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041435988e16c384792d9890dbde698f8385ff8e32c80414ca4edd5b273529d9f6eec3e553efa4c019961daf02105a9bec7e31a6a607e9e7761b4f8f306e";
 	private static final String securetrustHost = "ocsp.trustwave.com";
 	private static final int securetrustPort = 80;
 	private static final String securetrustDisplayName = "SecureTrust CA";
 
 	private static final String shecaRequestString = "504f5354202f476c6f62616c2f676c6f62616c2e6f63737020485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414430bb44ca10deb51ffadd53169ba185531f990270414f500c78d5258c4966922854d5e7e23f16bc2b071021057bf64e6ab94870b6298910a6ce1f09e";
 	private static final String shecaHost = "ocsp3.sheca.com";
 	private static final int shecaPort = 80;
 	private static final String shecaDisplayName = "SHECA";
 	
 	private static final String swisscomRequestString = "504f5354202f727562696e20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a0500041405e4639e5b4ab593f2690dd371f9fd91aadb5db604142dc2a7a3633e3f8347ab4833368185f7d4e9acc0021100ad20c4998c9c0d59bbb4639e7d46f5cf";
 	private static final String swisscomHost = "ocsp.swissdigicert.ch";
 	private static final int swisscomPort = 80;
 	private static final String swisscomDisplayName = "Swisscom Rubin CA 1";
 	
 	private static final String swisssignEVRequestString = "504f5354202f3838373434363644433737434235464137323731313944454246323735453044363845304637323720485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038320d0a0d0a3050304e304c304a3048300906052b0e03021a05000414e5b6d6d9bff84d8a85340c1e2211d54d132069b504148874466dc77cb5fa727119debf275e0d68e0f727020f2d7478016a69f7e3ba267e2b669920";
 	private static final String swisssignEVHost = "ocsp.swisssign.net";
 	private static final int swisssignEVPort = 80;
 	private static final String swisssignEVDisplayName = "SwissSign EV Gold CA 2009 - G2";
 	
 	private static final String sscClass2CAIIRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2036390d0a0d0a30433041303f303d303b300906052b0e03021a05000414032b3b4e1c31c8bffad0af443ef8ffbdb1f378d7041462df33e64a4b5f7bb811954c856381de4b0f2bdd02020086";
 	private static final String sscClass2CAIIHost = "ocsp2.ssc.lt";
 	private static final int sscClass2CAIIPort = 2560;
 	private static final String sscClass2CAIIDisplayName = "SSC Class 2 CA II";
 	
 	private static final String starfieldRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037340d0a0d0a30483046304430423040300906052b0e03021a0500041493c2b5268c1af3d22a991b62b27ec0040b73760e0414494b5227d11bbcf2a1216a627b51427a8ad7d556020707ae54f60e3928";
 	private static final String starfieldHost = "ocsp.starfieldtech.com";
 	private static final int starfieldPort = 80;
 	private static final String starfieldDisplayName = "Starfield Secure Certification Authority";
 	
 	private static final String startcomClass1RequestString = "504f5354202f7375622f636c617373312f7365727665722f636120485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a050004146568874f40750f016a3475625e1f5c93e5a26d580414eb4234d098b0ab9ff41b6b08f7cc642eef0e2c45020305facc";
 	private static final String startcomClass1Host = "ocsp.startssl.com";
 	private static final int startcomClass1Port = 80;
 	private static final String startcomClass1DisplayName = "StartCom Class 1 Primary Intermediate Server CA";
 	
 	private static final String startcomClass2RequestString = "504f5354202f7375622f636c617373322f7365727665722f636120485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037300d0a0d0a304430423040303e303c300906052b0e03021a05000414b9b2d56db021b36e42f627245806c4a9a6979aeb04144e0bef1aa4405ba517698730ca346843d041aef202030092a9";
 	private static final String startcomClass2Host = "ocsp.startssl.com";
 	private static final int startcomClass2Port = 80;
 	private static final String startcomClass2DisplayName = "StartCom Class 2 Primary Intermediate Server CA";
 	
 	private static final String stebuRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037360d0a0d0a304a3048304630443042300906052b0e03021a05000414ef19cab429f57b92d6dbbafafde9e6eab42235040414953699c97c7b44fb4ec244462988e151377ebcad020900c7e26c0790e24975";
 	private static final String stebuHost = "ocsp.eschreiben.de";
 	private static final int stebuPort = 8082;
 	private static final String stebuDisplayString = "Stebu-Server-CA";
 	
 	private static final String telesecRequestString = "504f5354202f6f6373707220485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037350d0a0d0a30493047304530433041300906052b0e03021a05000414d64a10d3feb06205656e0c286e9a9e42fc8f062b041433dc9e96ecd8e8351f6d901b0b38a4af741bc658020833169215dbc5f2f5";
 	private static final String telesecHost = "ocsp.serverpass.telesec.de";
 	private static final int telesecPort = 80;
 	private static final String telesecDisplayName = "TeleSec ServerPass CA 1";
 	
 	private static final String terenaRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a05000414268b3b24381d0d382cab39138aad04cb959be97104140cbd93680cf3deaba3496b2b375747ea90e3b9ed021100c36c6144a9cb9b42e072c8e63a9ce4bd";
 	private static final String terenaHost = "ocsp.tcs.terena.org";
 	private static final int terenaPort = 80;
 	private static final String terenaDisplayName = "TERENA SSL CA";
 	
 	private static final String thawteDVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414f5b8a8188e90a62689dedc0bf3b3925042c69e0a0414ab44e45dec83c7d9c0859ff7e1c69790b08c3f98021020a7003a65c1e8fb3754e5b4ce42d92d";
 	private static final String thawteDVHost = "ocsp.thawte.com";
 	private static final int thawteDVPort = 80;
 	private static final String thawteDVDisplayName = "Thawte DV SSL CA";
 	
 	private static final String thawteSGCRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a050004141e9209aa713c794bca1e931a0a61ad3fd0ba608304143b349a709173b28a1b0cf4e937cdb370329e185402104f9d96d966b0992b54c2957cb4157d4d";
 	private static final String thawteSGCHost = "ocsp.thawte.com";
 	private static final int thawteSGCPort = 80;
 	private static final String thawteSGCDisplayName = "Thawte SGC CA";
 	
 	private static final String trendmicroRequestString = "504f5354202f746d636120485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037350d0a0d0a30493047304530433041300906052b0e03021a05000414f1588a1d2d785fb6a040029a35a0f94799cc3f4a0414ad31c7fa02ce67f7651cfbba5fc0bbc5504c67c802085fb103d910d1edc5";
 	private static final String trendmicroHost = "ocsp.trendmicro.com";
 	private static final int trendmicroPort = 80;
 	private static final String trendmicroDisplayName = "Trend Micro CA";
 	
 	private static final String trustcenterClass3RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038320d0a0d0a3050304e304c304a3048300906052b0e03021a0500041429c3cf298dc08f013869c351bc858de120be6bca0414f5cfb7e7133b6cf8393b3265d24759f4e82471a9020f0086da00010002f90b5a0f1b0f2da1";
 	private static final String trustcenterClass3Host = "ocsp.ix.tcclass3.tcuniversal-i.trustcenter.de";
 	private static final int trustcenterClass3Port = 80;
 	private static final String trustcenterClass3DisplayName = "TC TrustCenter Class 3 L1 CA IX";
 	
 	private static final String trustcenterUniversalRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038310d0a0d0a304f304d304b30493047300906052b0e03021a050004143a68fe07b388d4e0492640323911268752f47ecb041492a4752ca49ebe8144eb79fc8ac595a5eb107573020e3d43000100025af05432d1a20b3a";
 	private static final String trustcenterUniversalHost = "ocsp.tcuniversal-I.trustcenter.de";
 	private static final int trustcenterUniversalPort = 80;
 	private static final String trustcenterUniversalDisplayName = "TC TrustCenter Universal CA I";
 	
 	private static final String trustedSecureRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038340d0a0d0a30523050304e304c304a300906052b0e03021a050004147fe501a98a328cfc5b1cdcbbc70768768a8cde700414cc035b965a9e16cc261ebda370fbe3cb7919fc4d021100f869434add6d336286b6a31525a981cf";
 	private static final String trustedSecureHost = "ocsp.csctrustedsecure.com";
 	private static final int trustedSecurePort = 80;
 	private static final String trustedSecureDisplayString = "Trusted Secure Certificate Authority";
 	
 	private static final String turktrustRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2036390d0a0d0a30433041303f303d303b300906052b0e03021a05000414cb5d5c1d40dc32668d3add121ff2215fe24b1f4b0414ab4e360330d2dbd50a68be87a5506cfcf670a52502020ca3";
 	private static final String turktrustHost = "socsp.turktrust.com.tr";
 	private static final int turktrustPort = 80;
 	private static final String turktrustDisplayName = "TURKTRUST Elektronik Sunucu Sertifikasi Hizmetleri";
 	
 	private static final String twcaEVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2037340d0a0d0a30483046304430423040300906052b0e03021a050004146ef2bf0ef3823e809c17226720f5507de39a25220414b92c09b5342af9fe5c0dfd6f768bd5921ae461560207064f2c98f69733";
 	private static final String twcaEVHost = "evssl_ocsp.twca.com.tw";
 	private static final int twcaEVPort = 80;
 	private static final String twcaEVDisplayName = "TWCA EVSSL Certification Authority";
 	
 	private static final String usertrustRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041414a7e219f46b93e141258f08bc85764671f136b00414eedd79c0d379b04d7e47bc70a6e7c62aaebadec902104bc814032f07fa6aa4f0da29df6179ba";
 	private static final String usertrustHost = "ocsp.usertrust.com";
 	private static final int usertrustPort = 80;
 	private static final String usertrustDisplayName = "UTN-USERFirst-Hardware";
 	
 	private static final String verisignClass3EVRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041445a7d4d407751a5fbded1e191d4a8dec52d241550414fc8a50ba9eb9255a7b55854f9500638fe9586b4302101727dd643e4236f87f79bb046bc19802";
 	private static final String verisignClass3EVHost = "EVSecure-ocsp.verisign.com";
 	private static final int verisignClass3EVPort = 80;
 	private static final String verisignClass3EVDisplayName = "VeriSign Class 3 Extended Validation SSL CA";
 	
 	private static final String verisignClass3EVSGCRequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a0500041439af18b41c021f39109656fdc6d358ef74858b9904144e43c81d76ef37537a4ff2586f94f338e2d5bddf02106c738181516e7e6c4fb57c01aabe8788";
 	private static final String verisignClass3EVSGCHost = "EVIntl-ocsp.verisign.com";
 	private static final int verisignClass3EVSGCPort = 80;
 	private static final String verisignClass3EVSGCDisplayName = "VeriSign Class 3 Extended Validation SSL SGC CA";
 
 	private static final String verisignClass3G2RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a050004146c2bc55aaf8d96bf60adf81d023f23b48a0059c20414a5ef0b11cec04103a34a659048b21ce0572d7d47021025f5d12d5e6f0bd4eaf2a2c966f3b4ce";
 	private static final String verisignClass3G2Host = "ocsp.verisign.com";
 	private static final int verisignClass3G2Port = 80;
 	private static final String verisignClass3G2DisplayName = "VeriSign Class 3 Secure Server CA - G2";
 	
 	private static final String verisignInternationalClass3RequestString = "504f5354202f20485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038330d0a0d0a3051304f304d304b3049300906052b0e03021a05000414c0fe0278fc99188891b3f212e9c7e1b21ab7bfc004140dfc1df0a9e0f01ce7f2b213177e6f8d157cd4f602103c08cfeebe9febc42bb13ee03d620bdf";
 	private static final String verisignInternationalClass3Host = "ocsp.verisign.com";
 	private static final int verisignInternationalClass3Port = 80;
 	private static final String verisignInternationalClass3DisplayName = "VeriSign International Server CA - Class 3";
 	
 	private static final String vodafoneRequestString = "504f5354202f6f63737020485454502f312e300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f6f6373702d726571756573740d0a436f6e74656e742d4c656e6774683a2038360d0a486f73743a206f6373702e63612e766f6461666f6e652e636f6d0d0a0d0a305430523050304e304c300906052b0e03021a05000414964c2c346aebe52296c8f356623d304f422fe9b404146833ac36fb87eacbe25442018ef09cf73043407d02137700007eb3e4369c88d931d367000000007eb3";
 	private static final String vodafoneHost = "ocsp.ca.vodafone.com";
 	private static final int vodafonePort = 80;
 	private static final String vodafoneDisplayName = "Vodafone (Corporate Services 2009)";
 	
 	/* 
 	 * Wells Fargo would go here, if I had an example EE certificate.
 	 */
 	
 	private static final OCSPResponder[] responders = new OCSPResponder[]
 	{
 		new OCSPResponder(hexStringToByteArray(addtrustRequestString), addtrustHost, addtrustPort, addtrustDisplayName),
 		new OCSPResponder(hexStringToByteArray(anceRequestString), anceHost, ancePort, anceDisplayName),
 		new OCSPResponder(hexStringToByteArray(buypassRequestString), buypassHost, buypassPort, buypassDisplayName),
 		new OCSPResponder(hexStringToByteArray(ccaIndia2011RequestString), ccaIndia2011Host, ccaIndia2011Port, ccaIndia2011DisplayName),
 		new OCSPResponder(hexStringToByteArray(certignaRequestString), certignaHost, certignaPort, certignaDisplayName),
 		new OCSPResponder(hexStringToByteArray(certumEVRequestString), certumEVHost, certumEVPort, certumEVDisplayName),
 		new OCSPResponder(hexStringToByteArray(cihazRequestString), cihazHost, cihazPort, cihazDisplayName),
 		new OCSPResponder(hexStringToByteArray(comodoRequestString), comodoHost, comodoPort, comodoDisplayName),
 		new OCSPResponder(hexStringToByteArray(cybertrustRequestString), cybertrustHost, cybertrustPort, cybertrustDisplayName),
 		new OCSPResponder(hexStringToByteArray(digicertEVRequestString), digicertEVHost, digicertEVPort, digicertEVDisplayName),
 		new OCSPResponder(hexStringToByteArray(dtrustClass3RequestString), dtrustClass3Host, dtrustClass3Port, dtrustClass3DisplayName),
 		new OCSPResponder(hexStringToByteArray(dtrustClass3EVRequestString), dtrustClass3EVHost, dtrustClass3EVPort, dtrustClass3EVDisplayName),
 		new OCSPResponder(hexStringToByteArray(dtrustRootClass3RequestString), dtrustRootClass3Host, dtrustRootClass3Port, dtrustRootClass3DisplayName),
 		new OCSPResponder(hexStringToByteArray(dtrustRootClass3EVRequestString), dtrustRootClass3EVHost, dtrustRootClass3EVPort, dtrustRootClass3EVDisplayName),
 		new OCSPResponder(hexStringToByteArray(eaekoRequestString), eaekoHost, eaekoPort, eaekoDisplayName),
 		new OCSPResponder(hexStringToByteArray(entrustL1ERequestString), entrustL1EHost, entrustL1EPort, entrustL1EDisplayName),
 		new OCSPResponder(hexStringToByteArray(etugraRequestString), etugraHost, etugraPort, etugraDisplayName),
 		new OCSPResponder(hexStringToByteArray(gandiRequestString), gandiHost, gandiPort, gandiDisplayName),
 		new OCSPResponder(hexStringToByteArray(geotrustGlobalCARequstString), geotrustGlobalCAHost, geotrustGlobalCAPort, geotrustGlobalCADisplayName),
 		new OCSPResponder(hexStringToByteArray(getronicsRequestString), getronicsHost, getronicsPort, getronicsDisplayName),
 		new OCSPResponder(hexStringToByteArray(globalsignRequestString), globalsignHost, globalsignPort, globalsignDisplayName),
 		new OCSPResponder(hexStringToByteArray(globesslRequsetString), globesslHost, globesslPort, globesslDisplayName),
 		new OCSPResponder(hexStringToByteArray(godaddyRequestString), godaddyHost, godaddyPort, godaddyDisplayName),
 		new OCSPResponder(hexStringToByteArray(hpPrivateClass2RequestString), hpPrivateClass2Host, hpPrivateClass2Port, hpPrivateClass2DisplayName),
 		new OCSPResponder(hexStringToByteArray(ipscaGlobalRequestString), ipscaGlobalHost, ipscaGlobalPort, ipscaGlobalDisplayName),
 		new OCSPResponder(hexStringToByteArray(ipscaLevel1RequestString), ipscaLevel1Host, ipscaLevel1Port, ipscaLevel1DisplayName),
 		new OCSPResponder(hexStringToByteArray(keynectisEVRequestString), keynectisEVHost, keynectisEVPort, keynectisEVDisplayName),
 		new OCSPResponder(hexStringToByteArray(networksolutionsRequestString), networksolutionsHost, networksolutionsPort, networksolutionsDisplayName),
 		new OCSPResponder(hexStringToByteArray(ocioRequestString), ocioHost, ocioPort, ocioDisplayName),
 		new OCSPResponder(hexStringToByteArray(quovadisRequestString), quovadisHost, quovadisPort, quovadisDisplayname),
 		new OCSPResponder(hexStringToByteArray(secomRequestString), secomHost, secomPort, secomDisplayName),
 		new OCSPResponder(hexStringToByteArray(securetrustRequestString), securetrustHost, securetrustPort, securetrustDisplayName),
 		new OCSPResponder(hexStringToByteArray(shecaRequestString), shecaHost, shecaPort, shecaDisplayName),
 		new OCSPResponder(hexStringToByteArray(sscClass2CAIIRequestString), sscClass2CAIIHost, sscClass2CAIIPort, sscClass2CAIIDisplayName),
 		new OCSPResponder(hexStringToByteArray(starfieldRequestString), starfieldHost, starfieldPort, starfieldDisplayName),
 		new OCSPResponder(hexStringToByteArray(startcomClass1RequestString), startcomClass1Host, startcomClass1Port, startcomClass1DisplayName),
 		new OCSPResponder(hexStringToByteArray(startcomClass2RequestString), startcomClass2Host, startcomClass2Port, startcomClass2DisplayName),
 		new OCSPResponder(hexStringToByteArray(stebuRequestString), stebuHost, stebuPort, stebuDisplayString),
 		new OCSPResponder(hexStringToByteArray(swisscomRequestString), swisscomHost, swisscomPort, swisscomDisplayName),
 		new OCSPResponder(hexStringToByteArray(swisssignEVRequestString), swisssignEVHost, swisssignEVPort, swisssignEVDisplayName),
 		new OCSPResponder(hexStringToByteArray(telesecRequestString), telesecHost, telesecPort, telesecDisplayName),
 		new OCSPResponder(hexStringToByteArray(terenaRequestString), terenaHost, terenaPort, terenaDisplayName),
 		new OCSPResponder(hexStringToByteArray(thawteDVRequestString), thawteDVHost, thawteDVPort, thawteDVDisplayName),
 		new OCSPResponder(hexStringToByteArray(thawteSGCRequestString), thawteSGCHost, thawteSGCPort, thawteSGCDisplayName),
 		new OCSPResponder(hexStringToByteArray(trendmicroRequestString), trendmicroHost, trendmicroPort, trendmicroDisplayName),
 		new OCSPResponder(hexStringToByteArray(trustcenterClass3RequestString), trustcenterClass3Host, trustcenterClass3Port, trustcenterClass3DisplayName),
 		new OCSPResponder(hexStringToByteArray(trustcenterUniversalRequestString), trustcenterUniversalHost, trustcenterUniversalPort, trustcenterUniversalDisplayName),
 		new OCSPResponder(hexStringToByteArray(trustedSecureRequestString), trustedSecureHost, trustedSecurePort, trustedSecureDisplayString),
 		new OCSPResponder(hexStringToByteArray(turktrustRequestString), turktrustHost, turktrustPort, turktrustDisplayName),
 		new OCSPResponder(hexStringToByteArray(twcaEVRequestString), twcaEVHost, twcaEVPort, twcaEVDisplayName),
 		new OCSPResponder(hexStringToByteArray(usertrustRequestString), usertrustHost, usertrustPort, usertrustDisplayName),
 		new OCSPResponder(hexStringToByteArray(verisignClass3EVRequestString), verisignClass3EVHost, verisignClass3EVPort, verisignClass3EVDisplayName),
 		new OCSPResponder(hexStringToByteArray(verisignClass3EVSGCRequestString), verisignClass3EVSGCHost, verisignClass3EVSGCPort, verisignClass3EVSGCDisplayName),
 		new OCSPResponder(hexStringToByteArray(verisignClass3G2RequestString), verisignClass3G2Host, verisignClass3G2Port, verisignClass3G2DisplayName),
 		new OCSPResponder(hexStringToByteArray(verisignInternationalClass3RequestString), verisignInternationalClass3Host, verisignInternationalClass3Port, verisignInternationalClass3DisplayName),
 		new OCSPResponder(hexStringToByteArray(vodafoneRequestString), vodafoneHost, vodafonePort, vodafoneDisplayName)
 	};
 	
 
 	
 	public OCSPResponder(byte[] request, String host, int port, String displayName)
 	{
 		this.request = request;
 		this.host = host;
 		this.port = port;
 		this.displayName = displayName;
 	}
 
 	
 	public byte[] getRequest()
 	{
 		return request;
 	}
 
 
 	public String getHost()
 	{
 		return host;
 	}
 
 
 	public int getPort()
 	{
 		return port;
 	}
 
 
 	public String getDisplayName()
 	{
 		return displayName;
 	}
 
 
 	public static OCSPResponder[] getResponders()
 	{
 		return responders;
 	}
 	
 	public static CharSequence[] getDisplayNames()
 	{
 		CharSequence result[] = new CharSequence[responders.length];
 		
 		for (int i = 0; i < responders.length; i++)
 			result[i] = responders[i].getDisplayName();
 		
 		return result;
 	}
 	
 	
 	public static byte[] hexStringToByteArray(String s) 
 	{
 	    int len = s.length();
 	    byte[] data = new byte[len / 2];
 	    for (int i = 0; i < len; i += 2) {
 	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
 	                             + Character.digit(s.charAt(i+1), 16));
 	    }
 	    return data;
 	}
 
 
 	public static OCSPResponder findByDisplayname(String displayName)
 	{
 		for (OCSPResponder responder : responders)
 		{
 			if (responder.displayName.equals(displayName))
 				return responder;
 		}
 		return null;
 	}
 
 }
