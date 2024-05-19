package org.cas.client.platform.bar.web_srm;

//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.asn1.x500.X500NameBuilder;
//import org.bouncycastle.asn1.x500.style.BCStyle;
//import org.bouncycastle.asn1.x509.*;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
//import org.bouncycastle.pkcs.PKCS10CertificationRequest;
//import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
//import org.cas.client.platform.bar.BarOption;
//import org.cas.client.platform.bar.BarUtil;
//import org.cas.client.platform.bar.pannel.BarFrame;
//import org.cas.client.platform.bar.print.PrintService;
//import org.cas.client.platform.bar.print.PrintUtil;
//import org.cas.client.platform.bar.web_srm.ValuePossible.Acti;
//import org.cas.client.platform.bar.web_srm.utiles.KeyStoreUtils;
//import org.cas.client.platform.bar.web_srm.utiles.UtilesECDSA;
//import org.cas.client.platform.bar.web_srm.utiles.UtilesFormatterDonnees;
//import org.cas.client.platform.bar.web_srm.utiles.UtilesJSON;
//import org.cas.client.platform.cascontrol.dialog.logindlg.LoginDlg;
//import org.cas.client.platform.cascustomize.CustOpts;
//import org.cas.client.platform.casutil.L;
//import org.cas.client.platform.pimmodel.PIMDBModel;
//import org.json.JSONObject;
//
//import javax.swing.*;
//import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.security.interfaces.ECPrivateKey;
//import java.security.interfaces.ECPublicKey;
//import java.security.spec.ECGenParameterSpec;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//import java.util.concurrent.*;

import com.just.print.app.BarFrame;
import com.just.print.ui.fragment.LoginDlg;
import com.just.print.util.JOptionPane;

import org.cas.client.platform.bar.BarUtil;
import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.print.PrintUtil;
import org.cas.client.platform.bar.web_srm.utiles.UtilesECDSA;
import org.cas.client.platform.bar.web_srm.utiles.UtilesFormatterDonnees;
import org.cas.client.platform.casutil.L;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.cas.client.platform.bar.BarOption;
import org.cas.client.platform.bar.web_srm.utiles.KeyStoreUtils;
import org.cas.client.platform.bar.web_srm.utiles.UtilesJSON;
import org.cas.client.platform.cascustomize.CustOpts;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class MevUtils {
	
	private static final String TRANS_LOT_INDICATOR = "}, \"transLot\"";
	private static final String TRANS_LOT = "{ \"reqTrans\": { \"transLot\": [{";
	private static final String TRANS_ACTU = "{ \"reqTrans\": { \"transActu\": {";

	public static final String ALIAS_CERTIFICAT_SEV = "_ALIAS_CERTIFICAT.SEV";

	public static final String ALIAS_CERTIFICAT_PSI = "_ALIAS_CERTIFICAT.PSI";

	public static final String KEYSTORE_TYPE = "PKCS12";
	public static final String KEYSTORE_FILE = "c:/AikaPos/jre/lib/jckconfig";
	public static final String KEYSTORE_KEY = "Eric";

	private static ECPrivateKey privateKey;

	private static Map<String, String> httpHeaders = getHTTPHeaders();

	// Prepare the common information in header (company/ software and request), not including mandatory and transaction specific info.
	// partner id,single/server mode, Aikapos's two ids, hardware id, certificate pass code, content-type, environment, test case,
	//	IDPARTN=00000000000012C6
	//	APPRLINIT=SEV
	//	IDSEV=0000000000003372
	//	IDVERSI=0000000000003D96
	//	VERSI=003.030.0616
	//	VERSIPARN=0
	//	IDAPPRL=8191-3934-9563
	//	CODCERTIF=FOB201999999
	//  CONTENT-TYPE=application/json
	//	ENVIRN=DEV
	//	CASESSAI=000.000
	private static Map<String, String> getHTTPHeaders() {
		Map<String, String> keyValuePairs = new HashMap<String, String>();

		//company:
		keyValuePairs.put("IDPARTN", BarOption.getPartnerId());// partner id of my company.

		//product
		keyValuePairs.put("APPRLINIT", "SEV"); //Possible values are: SEV(our case) or SRV (server mode)
		keyValuePairs.put("IDSEV", BarOption.getIdSignedToAikaPos());//Identifiers signed to AikaPos to send the request to  the WEB-SRM
		keyValuePairs.put("IDVERSI", BarOption.getIdVersi()); //not clear why there are two identify
		keyValuePairs.put("VERSI", BarOption.getCertifiedVersion());		//003.030.0616 will be the version for certificate.
		keyValuePairs.put("VERSIPARN", BarOption.getParentVersion()); //no parent version, use 0.

		keyValuePairs.put("IDAPPRL", BarOption.getCertifiedDeviceId());//the device will get a identity after first time certificate.
		keyValuePairs.put("CODCERTIF", BarOption.getCertificationPassedCode());//TODO: when AikaPos passed certificate, will have this value.
		//request:
		keyValuePairs.put("CONTENT-TYPE", "application/json");
		keyValuePairs.put("ENVIRN", "DEV");		//Possible values are: DEV, ESSAI or  PROD
		keyValuePairs.put("CASESSAI", BarOption.getTestCaseNumber()); //case number of test case. use it only when testing in ESSAI env, for DEV and PROD, use 000.000

		// 添加 HTTP 头部  this info is used when sending document, other case Don't know if it's matter if it's added....
		keyValuePairs.put("NOTVQ", BarOption.getQSTAccount());
		keyValuePairs.put("NOTPS", BarOption.getGSTAccount());

		return keyValuePairs;
	}

	//=================================== certificate reqeust ==============================================
	// The steps required to obtain the SRS and WEB-SRM signed certificates.
	public static void askWebMevToSignTheCertificates(String modif) {
		if(ValuePossible.Modif.REM.equals(modif)) {
			if(BarOption.getCertificateSerialNumberSRS() == null || BarOption.getCertificateSerialNumberSRS().length() == 0) {
				JOptionPane.showMessageDialog(BarFrame.instance, "No local certificate to update, please restart the application to apply for a new certificate first!");
				return;
			}
		}
		// build the main part of the string asking server to sign the certificate generated by AikaPos.
		String strCertificateSignRequest = getCertificateSignRequestString();

		// build the whole part of the string. (a JASON structured string which can be send to server as request body)
		String jsonCertificatSignRequest = getCertificatSignRequestBodyInJSon(modif, strCertificateSignRequest, BarOption.getCertificateSerialNumberSRS());

		// Send out the CSR to WEB-SRM with JSON format request body and header properties
		sendOutCertificateSignRequest(modif, httpHeaders, jsonCertificatSignRequest);
	}

//	public static void askWebMevToDeleteTheCertificates() {
//		if(BarOption.getCertificateSerialNumberSRS() == null || BarOption.getCertificateSerialNumberSRS().length() == 0) {
//			JOptionPane.showMessageDialog(BarFrame.instance, "No local certificate to delete, please restart the application to apply for a new certificate first!");
//			return;
//		}
//		// build the whole part of the string. (a JASON structured string which can be send to server as request body)
//		String jsonCertificatSignRequest = getCertificatSignRequestBodyInJSon(ValuePossible.Modif.SUP, null, BarOption.getCertificateSerialNumberSRS());
//
//		// Send out the CSR to WEB-SRM with JSON format request body and header properties
//		sendOutCertificateSignRequest(ValuePossible.Modif.SUP, httpHeaders, jsonCertificatSignRequest);
//	}
//
	//generate a standard certificate request content in string format.
	private static String getCertificateSignRequestString() {
		try {
			//first get the information which will be in certificate ready.
			X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);

			nameBuilder.addRDN(BCStyle.CN, BarOption.getIdentificationNumber());//"5678912340");					//the mandatary's identification number.
			nameBuilder.addRDN(BCStyle.SURNAME, BarOption.getCertificateName());//an unique user-friendly name for the certificate (8-32 characters ascII) @NOTE: before I used ramdome SurName, it succeed, now whatever I use, it fails, guess something wrong on their server side.
			nameBuilder.addRDN(BCStyle.O, "RBC-" + BarOption.getMandatoryAuthorizationCode());//"D8T8-W8W8");	//the abbreviation for the sector of activity and the mandatay's authorization code separated by a hyphen.
			nameBuilder.addRDN(BCStyle.GIVENNAME, BarOption.getBillingNumber());//"ER0001");		//mandatary's billing file number was maile to mandatory with authorization code.
			//mandatary's QST file number
			nameBuilder.addRDN(BCStyle.OU, BarOption.getQSTAccount());
			//municipality's Coordinated Universal Time.
			nameBuilder.addRDN(BCStyle.L, "-05:00");//BarOption.getUTC());//This might be a but of web-mev serverside. for certificateSigning, must use this "-05:00", while for transaction regiest, must use "-05:00A" or "-05:00N"
			//abbreviation for the Province
			nameBuilder.addRDN(BCStyle.ST, "QC");
			//abbreviation for the country
			nameBuilder.addRDN(BCStyle.C, "CA");
			X500Name subject = nameBuilder.build();//x500Name is used to describe the subject and issuer of the certificate

			//get the key paire generated.
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
			kpg.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom()); //.initialize(256); //what's the difference?
			KeyPair keyPair = kpg.generateKeyPair();

			//public key and the X500Name subject are used to build the certificate request.
			ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
			final SubjectPublicKeyInfo clePublique = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
			//OK, this is my infomation which need to be certified, and this is my public key, please build a request for me, so I can send
			//it to CA organization, and they will give me a certificate. so, in future, I can show people the trustable certificate, and they
			//will trust me, and use the public key in certificat to verify the message I send was not modified by anyone else...
			PKCS10CertificationRequestBuilder csrBuilder = new PKCS10CertificationRequestBuilder(subject, clePublique);
			csrBuilder.addAttribute(org.bouncycastle.asn1.x509.Extension.keyUsage, new KeyUsage(
				  KeyUsage.digitalSignature
				| KeyUsage.keyEncipherment
				| KeyUsage.dataEncipherment
				| KeyUsage.keyAgreement));
			Vector<Object> keyPurposeIds = new Vector<>();
			keyPurposeIds.add(KeyPurposeId.id_kp_clientAuth);
			keyPurposeIds.add(KeyPurposeId.id_kp_serverAuth);
			csrBuilder.addAttribute(Extension.extendedKeyUsage, new ExtendedKeyUsage(keyPurposeIds));

			//private key is used to sign the content before publish it out
			privateKey = (ECPrivateKey) keyPair.getPrivate();
			JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withECDSA");
			ContentSigner signer = signerBuilder.build(privateKey);
			//here, the first usage of the signer will be sign certificate request???? -----no, it's used as parameter just to tell the
			//server what algorithm it will use to sign content in future.
			PKCS10CertificationRequest csr = csrBuilder.build(signer);
			byte[] csrBytes = csr.getEncoded();
			return "-----BEGIN CERTIFICATE REQUEST-----\\n"
			+ Base64.getEncoder().encodeToString(csrBytes)
			+ "\\n-----END CERTIFICATE REQUEST-----";

		} catch (Exception e) {
			L.e("MevUtiles", "exception when getCertificateSignRequestString", e);
			e.printStackTrace();
			return null;
		}
	}

	private static String getCertificatSignRequestBodyInJSon(String modifyType, String csrMainPart, Object certificateSerialNumberSRS) {
		StringBuilder s = new StringBuilder("{\"reqCertif\": {\"modif\": \"").append(modifyType).append("\"");	//modify type added.

		if (ValuePossible.Modif.AJO.equals(modifyType.toUpperCase())) {
			s.append(",\n       \"csr\": \"").append(csrMainPart).append("\"");
		} else if (ValuePossible.Modif.REM.equals(modifyType.toUpperCase())) {
			s.append(",\n      \"csr\": \"").append(csrMainPart).append("\"");//Why for updating there's a ","? is it a bug? it's never reached for now.
			s.append(",\n      \"noSerie\": \"").append(certificateSerialNumberSRS).append("\"");	//request main part added.
		} else if(ValuePossible.Modif.SUP.equals(modifyType.toUpperCase())) {
			s.append(",\n      \"noSerie\": \"").append(certificateSerialNumberSRS).append("\"");	//request main part added.
		}

		return s.append("}}").toString();
	}

	//Request to add, replace or delete a certificate
    //"Modif": 				The field indicates whether the modification concerns an addition, replacement or deletion of a certificate
    //"HTTPHeadersDict": 			Headers to be sent to the WEB-SRM
    //"strCertificateSignRequest": 	Json document to be sent to the WEB-SRM
    //"CertificateSerialNumberSRS": The field containing the serial number of the certificate when requesting a replacement ("REM") or deletion ("SUP"). Add null for certificate addition
    //returns 						WEB-SRM's response
	private static void sendOutCertificateSignRequest(
    		String modif,
    		Map<String, String> httpHeadersDict,
    		String reqeustContentInJASON){
		WEBSRM_Response response = UtilesJSON.CertificatesRequest(modif, httpHeadersDict, reqeustContentInJASON, BarOption.getCertificateSerialNumberSRS());
		String responseString = response.getResponseJsonWEBSRM();
		if(responseString == null) {
			L.i("MevUtils", "got a null as ResponseJson when requesting certification to be signed by server. status code is: " + response.getStatusCode());
		}else if(responseString.contains( "listErr" )){
			L.e("MevUtils", responseString, null);
			//BarOption.setCertifiedDeviceId("");//remove the user inputed known old idapprl. why? if noe deleted successful on server, in local should not change anything, right?
			JOptionPane.showMessageDialog(BarFrame.instance, BarFrame.consts.exceptionWhenSendingOutRequestForSignatureDigitalCertificate());
		} else if(responseString.toLowerCase().contains("error") || responseString.contains( "délai d'attente" ) || responseString.contains( "la connexion sous-jacente a été fermée")){
			L.e("MevUtils", responseString, null);
			BarOption.setCertifiedDeviceId("");//remove the user inputed known old idapprl.
		} else {
			L.i("MevUtils", "certificate application got signed by server" + responseString);
//-------------windows version-----------------------------------------------------
			try {
				JSONObject jsonRecuDuPSI = new JSONObject(responseString);
				JSONObject retourCertif = jsonRecuDuPSI.getJSONObject("retourCertif");
				if(retourCertif.has("certif") && ValuePossible.Modif.SUP.equals(retourCertif.get("certif"))) {
					BarOption.setCertificateSerialNumberSRS(null);
					BarOption.setCertificateSerialNumberWebSRM(null);
					deleteLocalCertificate();
					JOptionPane.showMessageDialog(BarFrame.instance, "Certificate deleted! New certificate will be appllied when restarting AikaPos.");
					return;
				}
				// save device identifier
				if(retourCertif.has("idApprl")) {
					BarOption.setCertifiedDeviceId(retourCertif.getString("idApprl"));
				}
        		//add SRS private key to digital signature.
                X509Certificate certificate_SRS = createX509Certificate(retourCertif.getString("certif"));
                X509Certificate certificate_WEBSRM = createX509Certificate(retourCertif.getString("certifPSI"));
                savePrivateKeyAndCertificates(certificate_SRS, certificate_WEBSRM);

                // save serialNumber
                String serialNum = certificate_SRS.getSerialNumber().toString(16);
                if(serialNum.length() < 40) {		//why for srs, the SN's length must be 40? or do I really means if less then 40, just make it a little longer by adding one "0" on head?
                	serialNum = "0" + serialNum;
                }
                BarOption.setCertificateSerialNumberSRS(serialNum);
                serialNum = certificate_WEBSRM.getSerialNumber().toString(16);
//                if(serialNum.length() < 40) {
//                	serialNum = "0" + serialNum;
//                }
                BarOption.setCertificateSerialNumberWebSRM(serialNum);
                JOptionPane.showMessageDialog(BarFrame.instance, ValuePossible.Modif.AJO.equals(modif) ? "Certificate added successfully!" : "Certificate updated successfully!");
            } catch (Exception exp) {
                L.e("MevUtils", "exception when processing received cetificate from server.", exp);
            }
		}
		CustOpts.custOps.saveData();
		updateHttpHeader();
    }

	public static void updateHttpHeader() {
		httpHeaders = getHTTPHeaders(); //refill the header with new values which was just got from server. and when called from other class, it can update the testcase value(CASESSAI).
	}

	private static void deleteLocalCertificate() {
		try {
            // 加载密钥库
            FileInputStream is = new FileInputStream(MevUtils.KEYSTORE_FILE);
            KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
            keystore.load(is, MevUtils.KEYSTORE_KEY.toCharArray());
            is.close();

            // 删除指定别名的证书
            if (keystore.containsAlias(ALIAS_CERTIFICAT_SEV)) {
                keystore.deleteEntry(ALIAS_CERTIFICAT_SEV);
                // 保存更改后的密钥库
                FileOutputStream os = new FileOutputStream(MevUtils.KEYSTORE_FILE);
                keystore.store(os, MevUtils.KEYSTORE_KEY.toCharArray());
                os.close();
                System.out.println("The certificate has been cleared.");
            } else {
                System.out.println("No entry found with given alias: ****");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    private static X509Certificate createX509Certificate(String pemCertificate) throws Exception {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
        		new ByteArrayInputStream(pemCertificate.getBytes()));
    }

    // save private key and certificates to KeyStore
	private static void savePrivateKeyAndCertificates(Certificate certificateOfAikaPos,
													  X509Certificate certificateOfWebMev) {

		try {
			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
			keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());

 			keyStore.deleteEntry(ALIAS_CERTIFICAT_SEV);
            keyStore.setEntry(ALIAS_CERTIFICAT_SEV, 						//certificate of Aikapos saved into keystore together with our private key.
            		new KeyStore.PrivateKeyEntry(privateKey, new Certificate[]{certificateOfAikaPos}),
            		new KeyStore.PasswordProtection(KEYSTORE_KEY.toCharArray()));

 			keyStore.deleteEntry(ALIAS_CERTIFICAT_PSI);
 			keyStore.setCertificateEntry(ALIAS_CERTIFICAT_PSI,				//certificate of webmev save into key store alone
 					certificateOfWebMev);

 			keyStore.store(new FileOutputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());
 		} catch (Exception e) {
 			L.e("MevUtils", "Exception when saving private key and certificate to keystore file", e);
 		}
 	}

    //======================================= Transaction request =========================================
	public static void buildTransaction(final List<String> sndMsg, final String transType, final Transaction transac) throws Exception{

        //=========================header info=================================
        transac.sectActi.Abrvt = ValuePossible.Abrvt.RBC;	 // Abbreviation of the business's sector of activity (RBC - Restaurants, bars and food trucks)
        transac.sectActi.TypServ = getServiceType(sndMsg); // Service typeLVR is what service?

        transac.FormImpr = ValuePossible.S_RFER.equals(transType) || (sndMsg.size() == 6 && sndMsg.get(5).endsWith(PrintService.PAYMENT_FAIL)) ?
        		ValuePossible.Acti.NON : ValuePossible.FormImpr.PAP;		 // Document print option (Paper)
        transac.RelaCommer = ValuePossible.RelaCommer.B2C;	 // Business relationship
        transac.CommerceElectronique = "N"; 					 // E-commerce? No

        //Print mode for the document, for print bill, the transType should be "FAC"
        //printing closing receipt, also use this method, then it could be also one of following:
        //•ANN: Annulation  (cancellation) •RPR: Reproduction •DUP: Duplicata •PSP: Parti sans payer •SOB: Sans objet (n/a)FAC: bill or any other document not included in this list)
        transac.ModImpr = ValuePossible.getModeImpr(sndMsg.get(sndMsg.size() - 1));		//print purpos: currently support RPR, DUP, ANN, PSP or FAC
        transac.Utc = BarOption.getUTC();	//"-05:00A", in summer time will be "-05:00N"
        transac.ModTrans = BarOption.isTrainingMode()? ValuePossible.ModTrans.FOR : ValuePossible.ModTrans.OPE; // Transaction mode (Operation)
        transac.DatTrans = BarUtil.sdf.format(new Date());

        // "Previous signature" of the first transaction
        transac.signa.Preced = "========================================================================================";
        String[] addressParts = sndMsg.get(0).split("\n");
        String address = addressParts[1].trim();

        //info of store owner
        transac.docAdr.DocNoCiviq = address.substring(0, address.indexOf(" "));	// Street number "3800";
        transac.docAdr.DocCodePostal = addressParts[2].trim();					// Postal Code
        transac.NomUtil = LoginDlg.USERNAME;	 		// The name of the user who produced the customer's bill(not matter)
        transac.NomMandt = BarOption.getShopName();		// Name of the mandatory who operates the business	(not matter)
        transac.NoTPS = BarOption.getGSTAccount();		// Mandatary’s GST and QST registration number (not the user's ones)
        transac.NoTVQ = BarOption.getQSTAccount();
        transac.EmprCertifSEV = KeyStoreUtils.GetCertificateThumbprint( BarOption.getCertificateSerialNumberSRS() );	// Thumb print of the certificate containing the
        												//public key linked to the private key used to produce the current transaction signature
        //info of aikapos
        transac.IdPartn = BarOption.getPartnerId();		//aikapos's partner id.
        transac.IdSev = BarOption.getIdSignedToAikaPos();        	//aikapos's idSev
        transac.IdVersi = BarOption.getIdVersi();		//aikapos's IdVersi
        transac.CodCertif = BarOption.getCertificationPassedCode();	//aikaPos's code certificate
        transac.Versi = BarOption.getCertifiedVersion();			//aikapos's version
        transac.VersiParn = BarOption.getParentVersion();	//aikapos's parent version which passed the certificate (if the getVersi is not the one)

        //info of bill
        transac.TypTrans = ValuePossible.TypTrans.ADDI.equals(transType) ? ValuePossible.TypTrans.ADDI : ValuePossible.TypTrans.RFER;	//the parameter(transType) can be BILL/RFER/V_RFER/R_RFER/S_RFER
        transac.ModPai = ValuePossible.getModPai(sndMsg); 	// Method of payment used for the bill (Cash) @note: seems for bill(addi) this field and be set with anything? it will be just ignored by webmev?
        transac.NoTrans = buildBillID(sndMsg.get(0));	// Number identifying the transaction ("100001");
        // Table number("====0") and Number of customers
        addressParts[addressParts.length - 1] = addressParts[addressParts.length - 1].trim();
        transac.sectActi.NoTabl = addressParts[addressParts.length - 1].substring(0, addressParts[addressParts.length - 1].indexOf(" "));
        int p = transac.sectActi.NoTabl.indexOf("-");
        if(p > 0) {
        	try {
				transac.sectActi.NoTabl = UtilesFormatterDonnees.formatterNoTabl(transac.sectActi.NoTabl.substring(0, p));
			} catch (Exception e) {
				L.e("MevUtils", "Unexpected exception when formatting table name:" + transac.sectActi.NoTabl, e);
			}
        	transac.sectActi.NbClint = addressParts[addressParts.length - 1].substring(p + 1);
        	p = transac.sectActi.NbClint.indexOf(PrintService.INVOICE);
        	if(p > 0) {
        		try {
					transac.sectActi.NbClint = UtilesFormatterDonnees.formatterNbClint(transac.sectActi.NbClint.substring(0, p).trim());
				} catch (Exception e) {
					L.e("MevUtils", "Unexpected exception when formatting customerIdx.", e);
				}
        	}
        }else {
            transac.sectActi.NbClint = "001";
        }

        // Field identifying one or more customer addresses
//        transac.clint.lstAdrClients.add( new TransactionAdrClint( ValeursPossibles.TypAdr.LVR, "2535", "Boul. Laurier", "Québec", "G1V5C6" ) );
//        transac.clint.lstAdrClients.add( new TransactionAdrClint( ValeursPossibles.TypAdr.FAC, "3800", "rue Marly", "Québec", "G1X4A5" ) );


        //============================dish part================================
        // Supplies of the transaction
        transac.lstItems = new ArrayList<TransactionItem>( );
        String dishContent = sndMsg.get(2).trim();
        String sep2 = BarUtil.getSeperatorLine(0);
        p = dishContent.indexOf(sep2);
		String subtotals = dishContent.substring(p + sep2.length()).trim();
		p = subtotals.indexOf(PrintService.SOUSTOTAL);
		dishContent = subtotals.substring(0, p);

		String[] dishes = dishContent.split("\n\n");
		for(int i = 0; i < dishes.length; i++) {
			String[] dishAndModify = dishes[i].split("\n");
			//get out the info of dish, the qt info will be used to calculate each modify.
			p = getTimesOperatorPosition(dishAndModify[0]);
			if(p < 1) {		//added this check because I met once there's two subtotal, might because some previous step process wrong?
				continue;
			}

			String descr_dish = dishAndModify[0].substring(0, p);
			String pricePart = dishAndModify[0].substring(p + 1);
			if(pricePart.endsWith("P") || pricePart.endsWith("F")) {
				pricePart = pricePart.substring(0, pricePart.lastIndexOf(" ")).trim();
			}
			p = pricePart.lastIndexOf(" ");
			float dishItemTotalPrice = Float.valueOf(pricePart.substring(p + 1).trim());
			p = descr_dish.lastIndexOf(" ");
			int Qte_dish = Integer.valueOf(descr_dish.substring(p + 1));
			descr_dish = descr_dish.substring(0, p).trim();
			//build info of each modify.
			List<TransactionPrecisionItem> lstPrecisionsItem = new ArrayList<TransactionPrecisionItem>( );
			float modifyTotal = 0.0f;
			float prix = 0.0f;
	        for(int m = 1; m < dishAndModify.length; m++) {
				p = dishAndModify[m].trim().lastIndexOf("=");
				float totalPrice = Float.valueOf(dishAndModify[m].substring(p + 1));
				String modifyAndQT = dishAndModify[m].substring(0, p);
				p = modifyAndQT.lastIndexOf(" ");
				String descr_modify = modifyAndQT.substring(0, p);
				int qt_modify = Integer.valueOf(modifyAndQT.substring(p +2));

				prix = totalPrice * Qte_dish;
				modifyTotal += prix;

				// Details for a supply of the transaction
		        lstPrecisionsItem.add(new TransactionPrecisionItem(
		        	UtilesFormatterDonnees.formatterQte((double)qt_modify * Qte_dish), //Qte e.g."+00001.00",
		        	descr_modify, 		//"Descr"
		        	UtilesFormatterDonnees.formatterMontant(totalPrice / qt_modify),	//"Unitr", "+000000001.00",
		        	UtilesFormatterDonnees.formatterMontant(prix),  // "Prix","+000000099.00"
		        	ValuePossible.Tax.FP, // "Tax",
		        	prix > 0 ? ValuePossible.Acti.RES : ValuePossible.Acti.SOB));   //"Acti"     //RBC
			}

	        prix = dishItemTotalPrice + modifyTotal;
	        transac.lstItems.add(new TransactionItem(
	        	UtilesFormatterDonnees.formatterQte((double)Qte_dish), //   "Qte", "+00001.00",
	        	descr_dish,   											//    "Descr",  "Poutine"
	        	UtilesFormatterDonnees.formatterMontant(prix / Qte_dish), //"Unitr", "+000000000.00"
	        	UtilesFormatterDonnees.formatterMontant(prix),			//"Prix", "+000000979.00"
	        	ValuePossible.Tax.FP, 								// "Tax",
	        	ValuePossible.Acti.RES,								//"Acti",
	        	lstPrecisionsItem ) ); 									// Add all the items to the transaction
		}

        //===============================amount part=============================
		//first， it service exists, add the service fee as an item of transac.lstItems.
		p = subtotals.indexOf(PrintService.SERVICE_FEE);
		if(p > -1) {
			String strServiceFee = subtotals.substring(p + PrintService.SERVICE_FEE.length() + 2, subtotals.indexOf("\n", p)).trim();
			String formattedServiceFee = UtilesFormatterDonnees.formatterMontant(strServiceFee);
			transac.lstItems.add(new TransactionItem(
					"+00001.00", //   "Qte", "+00001.00",
		        	"Service fee",   											//    "Descr",  "Poutine"
		        	formattedServiceFee, 	//"Unitr" is same with "Prix" because the qty of service fee is always 1
		        	formattedServiceFee,		//"Prix", "+000000979.00"
		        	ValuePossible.Tax.FP, 								// "Tax", SERVICE_FF should also applied both taxes?
		        	ValuePossible.Acti.RES));								//"Acti",
		}
		//according to document, if change pay method, need to secretly send a transaction to mev, with all number nagative.
		int s_rfer = ValuePossible.S_RFER.equals(transType) ? -1 : 1;

        //then, check if there's discount.
        float discount = 0.0f;
        p = subtotals.lastIndexOf(PrintService.ANNULATION);
        if(p > -1) {
	        String strDiscount = subtotals.substring(p + PrintService.ANNULATION.length() + 2, subtotals.indexOf("\n", p)).trim();
	        discount = -1 * Float.valueOf(strDiscount);	//adjusted amount is  applied to the after-tax.can be a coupon,gift card, a
			transac.Ajus = UtilesFormatterDonnees.formatterMontant(s_rfer * discount);//"+000000000.00";	//discount, rounding of the  after-tax amount, etc.
        }else {
        	transac.Ajus = "+000000000.00";
        }
        // numbers before tax
		p = subtotals.lastIndexOf(PrintService.SOUSTOTAL);
		String subTotal = subtotals.substring(p + PrintService.SOUSTOTAL.length() + 2, subtotals.indexOf("\n", p)).trim();
		p = subtotals.lastIndexOf(PrintService.TPS);
		String TPS = subtotals.substring(p + PrintService.TPS.length() + 2, subtotals.indexOf("\n", p)).trim();
		p = subtotals.lastIndexOf(PrintService.TVQ);
		String TPQ =  subtotals.substring(p + PrintService.TVQ.length() + 2).trim();

        transac.AvantTax = UtilesFormatterDonnees.formatterMontant(s_rfer * (Float.valueOf(subTotal) + discount));//"+000000500.00";
        transac.TPS = UtilesFormatterDonnees.formatterMontant(s_rfer * Float.valueOf(TPS));//"+000000025.00";
        transac.TVQ = UtilesFormatterDonnees.formatterMontant(s_rfer * Float.valueOf(TPQ));//"+000000050.00";

        //total number after tax
        float afterTaxAmount = Float.valueOf(subTotal) + discount + Float.valueOf(TPS) + Float.valueOf(TPQ);
        transac.ApresTax = UtilesFormatterDonnees.formatterMontant(s_rfer * afterTaxAmount);//"+000000575.00";

        //===============================payment part===============================
        //transac.Ajus =  //this field is cause by can be a coupon, a gift card, a discount, rounding of the  after-tax amount, etc.
        //The amount due after adjusting the after tax amount. The value must be the sum of the apresTax and ajus fields
		transac.MTDU = UtilesFormatterDonnees.formatterMontant(s_rfer * (afterTaxAmount - discount));

		//get the currently totally paid amount from all kind of payment of this time.
        Float currentlyPaid = 0.0f;
        String[] currentPays = PrintService.getCurrentPays(sndMsg, transType);
        for (String string : currentPays) {
        	p = string.indexOf(":");
        	if(p > 0) {
        		currentlyPaid += Float.valueOf(string.substring(p + 1).trim());
        	}
		}
        //get the latest paid amount from comment  TODO: there's a mistake when fetching comm.
        float latestPay = 0.0f;
        String comment = PrintService.getCommentFromSndMsg(sndMsg);
        p = comment.indexOf(PrintService.LATEST_PAY);
        if(p > -1) {
        	String paid = comment.substring(p + PrintService.LATEST_PAY.length() + 2, sndMsg.get(5).indexOf("\n", p)).trim();
        	latestPay = Float.valueOf(paid);
        }
        //get total due
      	float due = afterTaxAmount - currentlyPaid;

      	//the paied amount of this time / the total paiment of previouse / the current due / the tip.
      	//following 3 value shouldn't set, because we didn't selected relevant options yet
//		transac.VersActu = UtilesFormatterDonnees.formatterMontant(s_rfer * latestPay); //getCurrentPayAmount(subtotals);//"+000000000.00";	//must correspond to the current instalment,
//		transac.VersAnt = UtilesFormatterDonnees.formatterMontant(s_rfer * (currentlyPaid - latestPay)); //"+000000000.00";	//must correspond to the sum of all previous instalments
//		transac.Solde = UtilesFormatterDonnees.formatterMontant(s_rfer * (due < 0 ? 0.0f : due));		//"+000000000.00";	//must correspond to the balance to be paid.
		transac.Pourb = UtilesFormatterDonnees.formatterMontant(s_rfer * (due > 0 ? 0.0f : - due));	//"+000000000.00";	//The amount of the tip.
        transac.NoDossFO = BarOption.getBillingNumber();	//"ER0001";

        // Concatenation of data for digital signature calculation
        String strConcateneePourSignature = transac.NoTrans
            + transac.DatTrans
            + transac.TPS
            + transac.TVQ
            + transac.ApresTax
            + transac.NoTPS
            + transac.NoTVQ
            + transac.ModImpr
            + transac.ModTrans
            + transac.signa.Preced;
        // Calculation of the current transaction signature
        transac.signa.Actu = Base64.getEncoder().encodeToString(UtilesECDSA.getSignedText(strConcateneePourSignature));// Current transaction digital signature
        transac.signa.DatActu = transac.DatTrans;		//not affect certificate result of bill, but affect receipt.

        transac.lsttransactionReferences = new ArrayList<TransactionReference>();
        String[] comments = sndMsg.get(sndMsg.size() - 1).trim().split("\n"); //should always not null, right?
        if(comments.length >= 3 || (comments.length > 0 && comments[0].contains(PrintService.PAYMENT_FAIL.trim()))) {
        	try {
		        String noTrans = PrintService.fomatBillId(comments[0].substring(comments[0].indexOf(PrintUtil.REF_TO) + PrintUtil.REF_TO.length()));
		        String datTrans = comments[1].substring(comments[1].indexOf(PrintUtil.DATE_REF) + PrintUtil.DATE_REF.length());
		        String avantTax = comments[2].substring(comments[2].indexOf(PrintUtil.OLD_SUBTOTAL) + PrintUtil.OLD_SUBTOTAL.length());
		        TransactionReference transactionReference = new TransactionReference(noTrans, datTrans, avantTax);
		        transac.lsttransactionReferences.add(transactionReference);
        	}catch(Exception e) {
        		//There's a case which the comments.length is 3 but doesn't contains the right content, including paying directly without printing temporal bill.
        		String noTrans = PrintService.fomatBillId(transac.NoTrans);
		        String datTrans = transac.DatTrans;
		        String avantTax = transac.AvantTax;
		        TransactionReference transactionReference = new TransactionReference(noTrans, datTrans, avantTax);
		        transac.lsttransactionReferences.add(transactionReference);
        	}
        }
    }

	public static String getServiceType(List<String> sndMsg) {
		String serviceType = BarOption.isCounterMode() ? ValuePossible.TypServ.CMP : ValuePossible.TypServ.TBL;
		String[] lines = sndMsg.get(0).split("\n");
		String line = lines[lines.length - 1].trim();
		if(!line.startsWith("#")) {
			L.e("MevUtils", "Didn't found # at the beginning of last line of bill header!", null);
		}
		if(BarOption.getPlatformCharacters().contains(line.substring(1,2))) {
			serviceType = ValuePossible.TypServ.LVT;
		}
		return serviceType;
	}

	private static int getTimesOperatorPosition(String string) {
		int p = -1;
		while(true) {
			p = string.indexOf("x", p + 1);
			if(p == -1) {
				return p;
			}else if(p < 2) {
				continue;
			}else {
				char price1 = string.charAt(p + 1);
				if(Character.isDigit(string.charAt(p - 1)) &&  //after the x, it could be a negative number
						(Character.isDigit(price1) || (Character.isDigit(string.charAt(p + 2)) && (price1 == '-')))) {
					return p;
				}else {
					continue;
				}
			}
		}
	}
//
	//Figure out the bill Number
    private static String buildBillID(String content) {
		String tBillNumberStartStr = BarOption.getBillNumberStartStr();

		int tStartPos = content.lastIndexOf(tBillNumberStartStr);
		if(tStartPos >= 0) {
			String tBillID = content.substring(tStartPos + tBillNumberStartStr.length(), content.indexOf("\n", tStartPos)); //billID
			return tBillID;
		} else {
			L.e("Printer Serice", "unexpected content found when building BillID:"+content, null);
			return null;
		}
    }

    public static String getCompleteJSONStr(Transaction transActu) {
    	 String completeJSONStr = TRANS_ACTU
    			+ transActu.GetJson()
    			+ "}}}";
    	return completeJSONStr.replaceFirst("\"", "\\\"").replaceAll(",}", "}");
	}

    //Send the json document containing the transactions to the WEB-SRM
    //inside will consider if there's records in the batch.
    //it will be called when print bill, system exit, or user pressed specific button.
    //NOTE: 1/we don't check if currently it's offline mode, instead, we check the db table(websrmbatch) to see if there's records or not in batch.
    //2/to simplify the logic, we always divide the batch into as small as possible(if there's N records, we divide them to N(each group contains only 1 to avoid it's bigger than 256k)
	public static void flushBatchWithCurTransaction() {
		flushBatchWithCurTransaction(null, null, null, null, null);
	}
    public static String flushBatchWithCurTransaction( String newJsonTransaction, String signatransm, String emprCertifSEV, String strUrlQRCode,  String noTrans){
//		//load all batch records into memory.
//    	List<String[]> values = new ArrayList<>();
//    	StringBuilder sql = new StringBuilder("select * from WEBSRMBATCH");
//    	try {
//			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//			while (rs.moveToNext()) {
//				String[] record = new String[6];
//				record[0] = DBUtil.getCursorString(rs,"ID");
//				record[1] = DBUtil.getCursorString(rs,"JSONTOSEND");
//				record[2] = DBUtil.getCursorString(rs,"SIGNATRANSM");
//				record[3] = DBUtil.getCursorString(rs,"EMPRCERTIFSEV");
//				record[4] = DBUtil.getCursorString(rs,"STRURLQRCODE");
//				record[5] = DBUtil.getCursorString(rs,"EMPLOYEEID");
//				values.add(record);
//			}
//		}catch(Exception exp) {
//			L.e("MevUtils ", "unexpected error when inserting web mev reqeust into db. sql:" + sql, exp);
//		}
//
    	String strFR = null;
//		//modify the jsonToSend string with the records in batch, and send to web-mev.
//    	//the values.size
//    	for (int i = -1; i < values.size(); i++) {	//initialized to -1, even values is empty will still go in once.
//    		if(signatransm != null) {		//has new bill, not triggered by flush button or logout button, note sholdn't trigger when login, because the test case need to show the button there.
//    			httpHeaders.put("SIGNATRANSM", signatransm);
//    			httpHeaders.put("EMPRCERTIFTRANSM", emprCertifSEV);
//    		}else if(i >= 0){        		//in this case need to replace two property in header
//        		httpHeaders.put("SIGNATRANSM", values.get(i)[2]);
//        		httpHeaders.put("EMPRCERTIFTRANSM", values.get(i)[3]);
//    		}
//
//    		final String jsonToSend;
//    		if(i == -1 && values.size() > 0) {//first round, while batch is not empty.
//        		continue;					//batch is not empty, so don't send in this loop round, just go to next round to
//        	}else if(i == -1) {
//        		// check if given parameter is null, null means is calling when app exit or click the flush button.
//        		if(newJsonTransaction == null) {//there's no record in batch, and the param is null, means no need to do batch flush.
//        			BarOption.setWebMevOfflineMode(false);
//		        	BarFrame.instance.btnFlushMevBatch.setVisible(false);
//        			return null;
//        		}
//        		//if not returned, means no record in the batch, and parameter json is not null, so let's do not modify the jsonToSend;
//        		//let continue excuting to the code down there, and do only one time request.(no loop)
//        		jsonToSend = newJsonTransaction;
//        	}else if(i == 0 && newJsonTransaction != null) {	//the first request will be new record + the oldest batch
//        		jsonToSend = addElementFromBatchIntoJson(newJsonTransaction, values.get(0)[1]);
//        	}else {				//the first request will contains only the oldest batch.
//        		jsonToSend = buildOneElementBatchPartJson(values.get(i)[1]);
//        	}
//
//
//	        // Send the request to the WEB-SRM
//			ExecutorService executor = Executors.newSingleThreadExecutor();
//		    Future<WEBSRM_Response> future = executor.submit(new Callable<WEBSRM_Response>() {
//		        @Override
//		        public WEBSRM_Response call() {
//		            return UtilesJSON.TransactionRequest(httpHeaders, jsonToSend, BarOption.getCertificateSerialNumberSRS());
//		        }
//		    });
//
//		    try {
//		    	// add time limitation.
//		    	WEBSRM_Response webSRMResponse = future.get(BarOption.getWebMevWaitTime(), TimeUnit.SECONDS); // Try to get the response within maximum waiting seconds
//
//		        if(webSRMResponse.getStatusCode() >= 200 && webSRMResponse.getStatusCode() < 300) {
//		        	if(BarOption.isWebMevOfflineMode()) {
//			        	BarOption.setWebMevOfflineMode(false);
//			        	BarFrame.instance.btnFlushMevBatch.setVisible(false);
//			        	BarFrame.setStatusMes("Web srm back to normal! system exit from offline mode!");
//		        		L.i("MevUtiles", "Web srm back to normal! system exit from offline mode================================");
//		        	}
//		        	if(i > -1) {
//		        		cleanFromDB(values.get(i)[0]);	//I choosed the shorter one as the key.
//		        	}
//		        }else {
//		        	if(!BarOption.isWebMevOfflineMode()) {
//			        	BarOption.setWebMevOfflineMode(true);
//			        	BarFrame.instance.btnFlushMevBatch.setVisible(true);
//			        	BarFrame.setStatusMes("Having problem communicating with Web srm, system has turned into offline mode!");
//		        		L.i("MevUtiles", "Having problem communicating with Web srm, system entering the offline mode------------------------");
//		        	}
//		        	if(i <= 0) {	//if not success(save 5 string to DB)
//		        		addToBatch(jsonToSend, signatransm, emprCertifSEV, strUrlQRCode, noTrans);
//		        	}
//		        }
//
//		        if(webSRMResponse.ResponseJsonWEBSRM == null) {
//		        	L.e("MevUtiles", "webSRMResponse.ResponseJsonWEBSRM is null", null);
//		        	return null;
//		        }
//
//		        // Processing of error messages received from WEB-SRM, if any
//				if (webSRMResponse.ResponseJsonWEBSRM.contains("listErr")) {
//					StringBuilder errorMsg = new StringBuilder();
//					errorMsg.append("HTTP Status Code : " + webSRMResponse.getStatusCode()).append("\n");
//					errorMsg.append("HTTP Status Description : " + webSRMResponse.getResponseJsonWEBSRM().replace(",", "\n")).append("\n");
//					List<ErrorWEBSRM> lstErr = UtilesJSON.ParseErrors(webSRMResponse.getResponseJsonWEBSRM());
//					if (lstErr.size() > 0) {
//						for (ErrorWEBSRM er : lstErr) {
//							errorMsg.append("Id : " + er.Id).append(" ");
//							errorMsg.append("Message : " + er.Mess).append("\n");
//							errorMsg.append("Code de retour : " + er.CodRetour).append(" ");
//							errorMsg.append("Numéro de transaction : " + er.NoTrans).append("\n");
//
//							if (er.CodRetour.endsWith("0") || er.CodRetour.endsWith("1") || er.CodRetour.endsWith("5"))
//								errorMsg.append("La transaction numéro " + er.NoTrans
//										+ " n'a pas été transmise. Par conséquent, votre SEV devra la retransmettre dans un lot lors de la prochaine requête de type Transaction.").append("\n");
//							errorMsg.append("--------------------------");
//						}
//					}
//
//					L.e("webMev Error!!!",errorMsg.toString(), null);
//					JOptionPane.showMessageDialog(BarFrame.instance, errorMsg.toString(), "Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//				} else {
//					if (i <= 0 && newJsonTransaction != null  //the meaningful response is the one for the new transaction. for batch records, just send request, will not print again.
//							&& !webSRMResponse.ResponseJsonWEBSRM.contains("Error")
//							&& !webSRMResponse.ResponseJsonWEBSRM.contains("délai d'attente")
//							&& !webSRMResponse.ResponseJsonWEBSRM
//									.contains("Impossible de créer un canal sécurisé SSL/TLS.")) {
//						// Get the informations received from the WEB-SRM
//						ResponseTransaction reponseTransaction = UtilesJSON
//								.ParseResponseTransaction(webSRMResponse.ResponseJsonWEBSRM);
//
//						// Information received from the WEB-SRM
//						L.i("MevUtiles", webSRMResponse.ResponseJsonWEBSRM);
//						L.i("MevUtiles", "ProchainCasEssai : " + reponseTransaction.ProchainCasEssai);
//						L.i("MevUtiles", "NoTrans : " + reponseTransaction.NoTrans);
//						L.i("MevUtiles", "PsiNoTrans : " + reponseTransaction.PsiNoTrans);
//						L.i("MevUtiles", "PsiDatTrans : " + reponseTransaction.PsiDatTrans);
//						L.i("MevUtiles", "noLot : " + reponseTransaction.noLot);
//						L.i("MevUtiles", "datLot : " + reponseTransaction.datLot);
//						String date = reponseTransaction.PsiDatTrans;
//						if(date.length() < 14) {
//							date = BarUtil.sdf.format(new Date());
//						}
//						StringBuilder sb = new StringBuilder("       ").append(date.substring(0,4)).append("-")
//								.append(date.substring(4,6)).append("-")
//								.append(date.substring(6,8)).append(" ")
//								.append(date.subSequence(8, 10)).append(":")
//								.append(date.substring(10, 12)).append(":")
//								.append(date.substring(12)).append("\n")
//								.append("       ")
//								.append(reponseTransaction.PsiNoTrans).append("\n")
//								.append("          ")
//								.append(BarOption.getCertifiedDeviceId()).append("\n")
//								.append(BarUtil.getSeperatorLine(1));
//						strFR = sb.toString();
//					} else {
//						if (webSRMResponse.ResponseJsonWEBSRM.contains("Le délai d'attente de l'opération a expiré."))
//							System.out.println(
//									"Problème de communication avec le MEV-WEB. Le délai d'attente de l'opération a expiré.");
//						else {
//
//							if (webSRMResponse.ResponseJsonWEBSRM.contains("Impossible de créer un canal sécurisé SSL/TLS."))
//								System.out.println("Problème de certificat pour vous authentifer auprès du MEV-WEB");
//							else
//								System.out.println(UtilesJSON.indentJson(webSRMResponse.ResponseJsonWEBSRM));
//						}
//						//TODO: what shall we return for this case?
//					}
//				}
//		    } catch (TimeoutException e) {
//		        future.cancel(true); // Interrupt the transaction request if necessary
//		        L.e("MevUtiles", "TransactionRequest took longer than expected maximum seconds!", e);
//	        	BarOption.setWebMevOfflineMode(true);
//
//	        	if(i <= 0) {
//	        		addToBatch(newJsonTransaction, signatransm, emprCertifSEV, strUrlQRCode, noTrans);//add latest record.
//	        	}
//	        	break;
//		    } catch (InterruptedException | ExecutionException e) {
//		        // Handle other exceptions if necessary
//		        L.e("MevUtiles", "unexpected exception happend:", e);
//		    } finally {
//		        executor.shutdownNow(); // Shutdown the executor
//		    }
//    	}
	    return strFR;
    }

//    private static String buildOneElementBatchPartJson(String json) {
//    	StringBuilder completeJSONStr = new StringBuilder(TRANS_LOT)
//    			.append(json).append("}]}}");
//    	try {
//            new JSONObject(completeJSONStr.toString());
//        } catch (Exception e) {
//            L.w("MevUtils","Invalid JSON format", e);
//            completeJSONStr.append("}");
//        }
//      	 return completeJSONStr.toString();
//	}
//
//	private static String addElementFromBatchIntoJson(String jsonToSend, String jsonInBatch) {
//		StringBuilder sb = new StringBuilder(jsonToSend.substring(0, jsonToSend.length() - 2))
//				.append(", \"transLot\": [{").append(jsonInBatch).append("}]}}");
//		return sb.toString();
//	}
//
//	private static void cleanFromDB(String noTrans) {
//		StringBuilder sql = new StringBuilder("delete from WEBSRMBATCH where id = ").append(noTrans);
//		try {
//			ResultSet rs = PIMDBModel.getStatement().executeQuery(sql.toString());
//		}catch(Exception exp) {
//			//it's normal record which id = noTrans not exists.
//		}
//	}
//
//	private static void addToBatch(String jsonToSend, String signatransm, String emprCertifSEV, String strUrlQRCode, String noTrans) {
//		//if parameter not valid, return (when app exit, will flush the webmev batch, in that case, parames will be null
//		if(jsonToSend == null || signatransm == null || emprCertifSEV == null || strUrlQRCode == null || noTrans == null) {
//			return;
//		}
//		//if already exists return; otherwise, save to db.
//		int billID = Integer.valueOf(noTrans.substring(2).split("-")[0]);//noTrans is like 10000...001, remove the first two letters should be safe to get the id.
//		StringBuilder sql = new StringBuilder("select * from WEBSRMBATCH where id = ").append(billID);
//		try {
//			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//			if (!rs.moveToNext()) {
//				jsonToSend = jsonToSend.substring(TRANS_ACTU.length());
//				int pTransLot = jsonToSend.indexOf(TRANS_LOT_INDICATOR);
//				if(pTransLot > 0) {
//					jsonToSend = jsonToSend.substring(0, pTransLot);
//				}else {
//					jsonToSend = jsonToSend.substring(0, jsonToSend.length() - 3);
//				}
//				sql = new StringBuilder("INSERT INTO WEBSRMBATCH (id, jsonToSend, signatransm, emprCertifSEV, strUrlQRCode, employeeId) VALUES (")
//					.append(billID).append(", '")
//					.append(jsonToSend).append("', '")
//				    .append(signatransm).append("', '")
//				    .append(emprCertifSEV).append("', '")
//				    .append(strUrlQRCode).append("', ")
//					.append(LoginDlg.USERID).append(")") ;
//				rs = PIMDBModel.getStatement().executeQuery(sql.toString());
//			}
//		}catch(Exception exp) {
//			L.e("MevUtils ", "unexpected error when inserting web mev reqeust into db. sql:" + sql, exp);
//		}
//	}
//
//	//not used for now, because the time in sndMsg could be still a little different with the time request reach to the Web-srm server.
//	private static String getDatTrans(List<String> sndMsg) {
//		String tText = sndMsg.get(1).trim(); //find out the waiter and time and subtotal and tps tpq
//		String[] ary = tText.split(" ");
//
//		//-----------time-----------------
//		String dateTrans = null;//"00000000000000";	//20090128084800
//		String time =  ary[ary.length - 1];
//		String date =  ary[ary.length - 2];
//		if(time.indexOf("-") > 0) {	// sometimes the time part could be like 13:00-14:30, so we take te 14:30 as the dateTrans.
//			time = time.substring(time.indexOf("-") + 1);
//		}
//		String[] tAry = time.split(":");
//		String[] dAry = date.split("-");
//		dateTrans = new StringBuilder(dAry[0]).append(dAry[1]).append(dAry[2]).append(tAry[0]).append(tAry[1]).toString();
//		if(tAry.length == 3) {
//			dateTrans  = dateTrans.concat(tAry[2].endsWith(")") ? tAry[2].substring(0,  tAry[2].indexOf("(")) : tAry[2]);
//		}else {
//			dateTrans  = dateTrans.concat("00");
//		}
//		return dateTrans;
//	}
//
//	//==================================== user reqeusts ================================================
//    // Add a user on the WEB-SRM
//    //"Michel Untel"/"567891234RT0001"/"5678912340TQ0001";
//    public static void registerUser(String userName, String noGSTUser, String noQSTUser) {
//        // Preparation of the json document to send to the WEB-SRM
//        String jsonUser = UtilesJSON.GetJsonUser(ValuePossible.Modif.AJO, userName, noGSTUser, noQSTUser);
//
//        // Send the request to the WEB-SRM
//        WEBSRM_Response webSRMResponse = UtilesJSON.UserRequest(httpHeaders, jsonUser, BarOption.getCertificateSerialNumberSRS());
//
//        // Processing of error messages received from WEB-SRM, if any
//        if (!webSRMResponse.getResponseJsonWEBSRM().contains("listErr")) {
//			if (!webSRMResponse.getResponseJsonWEBSRM().contains("Error")
//					&& !webSRMResponse.getResponseJsonWEBSRM().contains("délai d'attente")
//					&& !webSRMResponse.getResponseJsonWEBSRM().contains("Impossible de créer un canal sécurisé SSL/TLS.")) {
//				//do nothing.
//			} else {
//				if (webSRMResponse.getResponseJsonWEBSRM().contains("Le délai d'attente de l'opération a expiré.")) {
//					JOptionPane.showMessageDialog(BarFrame.instance,
//							"Problème de communication avec le MEV-WEB. Le délai d'attente de l'opération a expiré.",
//							"Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//				} else if (webSRMResponse.getResponseJsonWEBSRM().contains("Impossible de créer un canal sécurisé SSL/TLS.")) {
//					JOptionPane.showMessageDialog(BarFrame.instance,
//							"Problème de certificat pour vous authentifer auprès du MEV-WEB", "Web-srm Response Error:",
//							JOptionPane.ERROR_MESSAGE);
//				} else {
//					JOptionPane.showMessageDialog(BarFrame.instance, webSRMResponse.getResponseJsonWEBSRM(),
//							"Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//        } else {
//        	StringBuilder errorMsg = new StringBuilder();
//	        errorMsg.append("HTTP Status Code : " + webSRMResponse.getStatusCode()).append("\n");
//	        errorMsg.append("HTTP Status Description : " + webSRMResponse.getResponseJsonWEBSRM().replace(",", "\n"));
//	        List<ErrorWEBSRM> errors = UtilesJSON.ParseErrors(webSRMResponse.getResponseJsonWEBSRM());
//	        for (ErrorWEBSRM error : errors) {
//	        	errorMsg.append("Id : " + error).append("\n");
//	        }
//			JOptionPane.showMessageDialog(BarFrame.instance, errorMsg.toString(), "Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//			StringBuilder sql = new StringBuilder("delete from employee where nname is '" + userName + "'");
//			try {
//				PIMDBModel.getStatement().executeUpdate(sql.toString());
//			} catch (Exception exp) {
//				L.e("MevUtils... ", "error happend when deleting an Error employee:" + sql, exp);
//			}
//
//		}
//    }
//
//    /**
//     * Delete a user on the WEB-SRM
//     * "Michel Untel"/"567891234RT0001"/"5678912340TQ0001";
//     */
//    public static void deleteUser(String userName, String noGSTUser, String noQSTUser) {
//
//        // Preparation of the json document to send to the WEB-SRM
//        String jsonUtilisateur = UtilesJSON.GetJsonUser(ValuePossible.Modif.SUP, userName, noGSTUser, noQSTUser);
//
//        // Send the request to the WEB-SRM
//        WEBSRM_Response webSRMResponse = UtilesJSON.UserRequest(httpHeaders, jsonUtilisateur, BarOption.getCertificateSerialNumberSRS());
//
//        // Processing of error messages received from WEB-SRM, if any
//		if (!webSRMResponse.getResponseJsonWEBSRM().contains("listErr")) {
//			if (!webSRMResponse.getResponseJsonWEBSRM().contains("Error")
//					&& !webSRMResponse.getResponseJsonWEBSRM().contains("délai d'attente") && !webSRMResponse
//							.getResponseJsonWEBSRM().contains("Impossible de créer un canal sécurisé SSL/TLS.")) {
//				//do nothing.
//			} else {
//				if (webSRMResponse.getResponseJsonWEBSRM().contains("Le délai d'attente de l'opération a expiré.")) {
//					JOptionPane.showMessageDialog(BarFrame.instance,
//							"Problème de communication avec le MEV-WEB. Le délai d'attente de l'opération a expiré.",
//							"Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//				} else {
//					if (webSRMResponse.getResponseJsonWEBSRM().contains("Impossible de créer un canal sécurisé SSL/TLS.")) {
//						JOptionPane.showMessageDialog(BarFrame.instance,
//								"Problème de certificat pour vous authentifer auprès du MEV-WEB",
//								"Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//					} else {
//						JOptionPane.showMessageDialog(BarFrame.instance, webSRMResponse.getResponseJsonWEBSRM(),
//								"Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//					}
//				}
//			}
//		} else {
//            StringBuilder errorMsg = new StringBuilder();
//            errorMsg.append("HTTP Status Code : " + webSRMResponse.getStatusCode()).append("\n");
//            errorMsg.append("HTTP Status Description : " + webSRMResponse.getResponseJsonWEBSRM().replace(",", "\n"));
//            List<ErrorWEBSRM> errors = UtilesJSON.ParseErrors(webSRMResponse.getResponseJsonWEBSRM());
//            for (ErrorWEBSRM error : errors) {
//            	errorMsg.append("error : " + error).append("\n");
//            }
//			JOptionPane.showMessageDialog(BarFrame.instance, errorMsg.toString(), "Web-srm Response Error:", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//	//==================================== document reqeust ==============================================
//	public static boolean sendUserDocumentRequest(DocumentRUT docRUT, int userId, String date, Float afterTextAmount) {//String name, String taxCode1, String taxCode2) {
//
//		// 数字签名
//		byte[] signature = UtilesECDSA.getSignedText(docRUT.GetDocumentConcateneSignature());
//		docRUT.setSI(Base64.getEncoder().encodeToString(signature));	//Mandatary's digital signature 88 characters
//
//		// 准备 JSON 文档
//		String json = UtilesJSON.GetJsonDocument(ValuePossible.TypDoc.RUT, docRUT.getDocumentConcatenatedQRCode());
//
//		System.out.println(UtilesJSON.indentJson(json));
//
//		// 发送请求到 WEB-SRM
//		WEBSRM_Response webSRMResponse = UtilesJSON.DocumentRequest(httpHeaders, json, BarOption.getCertificateSerialNumberSRS());
//
//		// Processing of error messages received from WEB-SRM, if any
//		if (webSRMResponse.getResponseJsonWEBSRM().contains("listErr")) {
//			StringBuilder errorMsg = new StringBuilder();
//			errorMsg.append("HTTP Status Code : " + webSRMResponse.getStatusCode()).append("\n");
//			errorMsg.append("HTTP Status Description : " + webSRMResponse.getResponseJsonWEBSRM().replace(",", "\n"));
//			List<ErrorWEBSRM> errors = UtilesJSON.ParseErrors(webSRMResponse.getResponseJsonWEBSRM());
//			for (ErrorWEBSRM error : errors) {
//				errorMsg.append("error : " + error).append("\n");
//			}
//			JOptionPane.showMessageDialog(BarFrame.instance, errorMsg.toString());
//			return false;
//		} else {
//			JSONObject jsonResponse = new JSONObject(webSRMResponse.getResponseJsonWEBSRM());
//			String noDocValue = jsonResponse.getJSONObject("retourDoc").getString("noDoc");
//			System.out.println(noDocValue);
//			StringBuilder sql = new StringBuilder("INSERT INTO customizations (key, value, type, status) VALUES (");
//			sql.append("'").append(userId).append("',")
//			.append(" '").append(date).append("',")
//			.append(Math.round(afterTextAmount * 100)).append(",")
//			.append(noDocValue).append(")");
//			try {
//				PIMDBModel.getStatement().executeUpdate(sql.toString());
//			} catch (SQLException e) {
//				L.e("MevUtiles", "Unexpected exception when insert userDocument info into customization :" + sql, e);;
//			}
//			return true;
//		}
//	}
//
	//==================================== certificate expiration check ==================================
	//when app started, will call this to check if the certificate expired.
	public static void checkCertificateDate() {
		if(BarOption.getBillingNumber() != null) {
			 X509Certificate certificate = KeyStoreUtils.getCertificate(BarOption.getCertificateSerialNumberSRS());
			 if(certificate == null) {
				L.e("MevUtils", " Can not find certificate with serialNumber:" + BarOption.getCertificateSerialNumberSRS() , null);
				JOptionPane.showMessageDialog(null, "certificate not found! Aikapos will automatically register a new one! ");
				MevUtils.askWebMevToSignTheCertificates(ValuePossible.Modif.AJO);
				return;
			 }
			 long millisecondsDifference = certificate.getNotAfter().getTime() - new Date().getTime();
			 long daysDifference = TimeUnit.MILLISECONDS.toDays(millisecondsDifference);

			 certificate = KeyStoreUtils.getCertificate(BarOption.getCertificateSerialNumberWebSRM());
			 if(certificate == null) {
					L.e("MevUtils", " Can not find certificate with serialNumberWebSRM:" + String.valueOf(BarOption.getCertificateSerialNumberSRS()) , null);
					JOptionPane.showMessageDialog(null, "WEBSRM certificate not found! Please contact Aikapos customer support! ");
					MevUtils.askWebMevToSignTheCertificates(ValuePossible.Modif.AJO);
					return;
				 }
			 millisecondsDifference = certificate.getNotAfter().getTime() - new Date().getTime();
			 int expiredWarningDays = BarOption.getExpiredWarningDays();
			 if(TimeUnit.MILLISECONDS.toDays(millisecondsDifference) < expiredWarningDays || daysDifference < expiredWarningDays){
				 //TODO: should change here into a option with two buttons.
                if(JOptionPane.showConfirmDialog(null, "certificate is about to expire! Would you like to re-activate it!",
   					 BarFrame.consts.ComboMenu())) {
		   				//if selected yes, start to do recertificate.
		   				MevUtils.askWebMevToSignTheCertificates(ValuePossible.Modif.REM);
		   		}
			 }
		}
	}
//
//	public static boolean isBillIdAlreayInMevBatch(int billID) {
//		StringBuilder sql = new StringBuilder("select * from WEBSRMBATCH where id = ").append(billID);
//		try {
//			Cursor rs = AikaPos.db.rawQuery(sql.toString(), null);
//			return rs.moveToNext();
//		}catch(Exception exp) {
//			L.e("MevUtils ", "unexpected error when inserting web mev reqeust into db. sql:" + sql, exp);
//		}
//		return false;
//	}
}
