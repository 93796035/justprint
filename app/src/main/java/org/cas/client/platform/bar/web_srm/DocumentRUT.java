package org.cas.client.platform.bar.web_srm;

// DOCUMENT Request - User report structure
public class DocumentRUT {

	// GST registration number
	public String RT;

	// QST registration number
	public String TQ;

	// User name
	public String UT;

	// Transaction number of the last document
	public String NO;

	// After-tax amount of the last document
	public String MT;

	// Date and time the last document was produced

	public String DF;

	// Year (current or preceding) the transactions in the sales summary were recorded
	public String AN;

	// Total number of tranactions
	public String SN;

	// Number of transaction payments
	public String SV;

	// Before-tax amount from the sales summary
	public String SS;

	// GST amount from the sales summary
	public String SF;
	
	// QST amount from the sales summary
	public String SP;

	// After-tax amount from the sales summary
	public String ST;

	// Adjusted sales summary amount
	public String SA;

	// Amount due from sales summary

	public String SD;

	// Source used to calculate the sales summary
	public String TS;

	// Identifier of the device for which the report is requested, if the sales
	// summary is calculated for a single SRS,
	// OR Mandatory billing file number, if the sales summary is calculated for all
	// SRS in the establishment

	public String SR;

	// Indication of whether sales correspond to a single user account or to all
	// user accounts in the establishment
	public String CM;

	// Unique device identifier
	public String IA;

	// SRS identifier
	public String IS;

	// SRS version number assigned by the developer
	public String VR;

	// Date and time the user logged into his or her account
	public String DC;

	// Date and time the user report was produced
	public String DR;

	// Digital signature of the user report

	public String SI;

	// Thumbprint of the digital certificate linked with the private key that
	// generated the digital signature

	public String EM;

	// Establishment’s address

	public String AD;

	// Constructeurs

	public DocumentRUT() {
	}

	// Concatenation of information to calculate the signature or to transmit the
	// document to WEB-SRM

	// <returns> Concatenated data</returns>
	// Obsolete( "GetDocumentConcatene( ) is deprecated, please use
	// GetDocumentConcateneSignature( ) or GetDocumentConcateneQRCode( ) instead."
	// )]
	public String GetDocumentConcatene() {
		StringBuilder s = new StringBuilder();
		s.append("RT=").append(RT).append(";");
		s.append("TQ=").append(TQ).append(";");
		s.append("UT=").append(UT).append(";");
		s.append("NO=").append(NO).append(";");
		s.append("MT=").append(MT).append(";");
		s.append("DF=").append(DF).append(";");
		s.append("AN=").append(AN).append(";");
		s.append("SN=").append(SN).append(";");
		s.append("SV=").append(SV).append(";");
		s.append("SS=").append(SS).append(";");
		s.append("SF=").append(SF).append(";");
		s.append("SP=").append(SP).append(";");
		s.append("ST=").append(ST).append(";");
		s.append("SA=").append(SA).append(";");
		s.append("SD=").append(SD).append(";");
		s.append("IA=").append(IA).append(";");
		s.append("IS=").append(IS).append(";");
		s.append("VR=").append(VR).append(";");
		s.append("DC=").append(DC).append(";");

		if (SI == "")
			// Concatenation for signature calculation
			s.append("DR=").append(DR);
		else {
			// Concatenation to transmit the document to WEB-SRM
			s.append("DR=").append(DR).append(";");
			s.append("SI=").append(SI).append(";");
			s.append("EM=").append(EM).append(";");
			s.append("AD=").append(AD);
		}
		return s.toString();
	}

	// Concatenation of information to calculate the signature or to transmit the
	// document to WEB-SRM
	// <returns>Informations Concatenated data</returns>
	public String GetDocumentConcateneSignature() {
		StringBuilder s = new StringBuilder();
		s.append(RT);
		s.append(TQ);
		s.append(UT);
		s.append(NO);
		s.append(MT);
		s.append(DF);
		s.append(AN);
		s.append(SN);
		s.append(SV);
		s.append(SS);
		s.append(SF);
		s.append(SP);
		s.append(ST);
		s.append(IA);
		s.append(IS);
		s.append(VR);
		s.append(DR);
		return s.toString();
	}

	// Concatenation of information to create the json document and QR code

	// <returns>Informations concaténées
	// Concatenated data</returns>
	public String GetDocumentConcateneQRCode() {
		StringBuilder s = new StringBuilder();
		s.append("RT=").append(RT).append(";");
		s.append("TQ=").append(TQ).append(";");
		s.append("UT=").append(UT).append(";");
		s.append("NO=").append(NO).append(";");
		s.append("MT=").append(MT).append(";");
		s.append("DF=").append(DF).append(";");
		s.append("AN=").append(AN).append(";");
		s.append("SN=").append(SN).append(";");
		s.append("SV=").append(SV).append(";");
		s.append("SS=").append(SS).append(";");
		s.append("SF=").append(SF).append(";");
		s.append("SP=").append(SP).append(";");
		s.append("ST=").append(ST).append(";");
		s.append("SA=").append(SA).append(";");
		s.append("SD=").append(SD).append(";");
		s.append("TS=").append(TS).append(";");
		s.append("SR=").append(SR).append(";");
		s.append("CM=").append(CM).append(";");
		s.append("IA=").append(IA).append(";");
		s.append("IS=").append(IS).append(";");
		s.append("VR=").append(VR).append(";");
		s.append("DC=").append(DC).append(";");
		s.append("DR=").append(DR).append(";");
		s.append("SI=").append(SI).append(";");
		s.append("EM=").append(EM).append(";");
		s.append("AD=").append(AD);
		return s.toString();
	}

	public void setSI(String encodeToString) {
		this.SI = encodeToString;
	}

	public void setRT(String rt) {
		this.RT = rt;
	}

	public void setTQ(String qstAccount) {
		this.TQ = qstAccount;
	}

	public void setUT(String name) {
		this.UT = name;
	}

	public String getDocumentConcatenatedQRCode() {
	    StringBuilder s = new StringBuilder();
	    s.append("RT=").append(RT).append(";");
	    s.append("TQ=").append(TQ).append(";");
	    s.append("UT=").append(UT).append(";");
	    s.append("NO=").append(NO).append(";");
	    s.append("MT=").append(MT).append(";");
	    s.append("DF=").append(DF).append(";");
	    s.append("AN=").append(AN).append(";");
	    s.append("SN=").append(SN).append(";");
	    s.append("SV=").append(SV).append(";");
	    s.append("SS=").append(SS).append(";");
	    s.append("SF=").append(SF).append(";");
	    s.append("SP=").append(SP).append(";");
	    s.append("ST=").append(ST).append(";");
	    s.append("SA=").append(SA).append(";");
	    s.append("SD=").append(SD).append(";");
	    s.append("TS=").append(TS).append(";");
	    s.append("SR=").append(SR).append(";");
	    s.append("CM=").append(CM).append(";");
	    s.append("IA=").append(IA).append(";");
	    s.append("IS=").append(IS).append(";");
	    s.append("VR=").append(VR).append(";");
	    s.append("DC=").append(DC).append(";");
	    s.append("DR=").append(DR).append(";");
	    s.append("SI=").append(SI).append(";");
	    s.append("EM=").append(EM).append(";");
	    s.append("AD=").append(AD);
	    return s.toString();
	}


	public void setNO(String string) {
		this.NO = string;
	}

	public void setMT(String formatterMontant) {
		this.MT = formatterMontant;
	}

	public void setDF(String string) {
		this.DF = string;
	}

	public void setAN(String string) {
		this.AN = string;
	}

	public void setSN(String formatterMontant) {
		this.SN = formatterMontant;
	}

	public void setSV(String formatterMontant) {
		this.SV = formatterMontant;
	}

	public void setSS(String formatterMontant) {
		this.SS = formatterMontant;
	}

	public void setSF(String formatterMontant) {
		this.SF = formatterMontant;
	}

	public void setSP(String formatterMontant) {
		this.SP = formatterMontant;
	}

	public void setST(String formatterMontant) {
		this.ST = formatterMontant;
	}

	public void setSD(String formatterMontant) {
		this.SD = formatterMontant;
	}

	public void setSA(String formatterMontant) {
		this.SA = formatterMontant;
	}

	public void setIA(String string) {
		this.IA = string;
	}

	public void setIS(String string) {
		this.IS = string;
	}

	public void setVR(String string) {
		this.VR = string;
	}

	public void setAD(String string) {
		this.AD = string;
	}

	public void setEM(String getCertificateThumbprint) {
		this.EM = getCertificateThumbprint;
	}

	public void setDC(String getDateHeureFormatté) {
		this.DC = getDateHeureFormatté;
	}

	public void setDR(String getDateHeureFormatté) {
		this.DR = getDateHeureFormatté;
	}


}