package org.cas.client.platform.bar.web_srm;

import org.cas.client.platform.bar.print.PrintService;
import org.cas.client.platform.bar.print.PrintUtil;

import java.util.List;

public interface ValuePossible {
	
    public static final String RFER = "RFER";
    public static final String R_RFER = "R_RFER";
    public static final String S_RFER = "S_RFER";
    public static final String V_RFER = "V_RFER";
    public static final String PAID_FROM = "Paid from";
    public static final String METHOD = "Method:";
    public static final String CASH = "ARGENT";
	public static final String REFUND = "Refund : ";
	public static final String VOID = "*Voided*";
	//according to web-mev requiement, following fench text must be show up on bill for specific case.
	public static final String RE_PRINTED_INTERNAL_USE = "\nCOPIE DU COMMERÇANT, NE PAS REMETTRE AU \nCLIENT\n";
	public static final String RE_PRINTED = "*REPRODUCTION*\n";
	public static final String RE_FUND = "\n*NOTE DE CRÉDIT*";
	public static final String NOT_PAID = "*** COPIE DU COMMERÇANT ***\nNE PAS REMETTRE AU CLIENT, PARTI SANS PAYER";

    interface UrlMEVWEB {
        String QRCode = "https://qr.mev-web.ca?f=";
        String UrlBaseMEVWEB = "https://cnfr.api.rq-fo.ca";
        public String Enrolement = "https://certificats.cnfr.api.rq-fo.ca/enrolement";
        public String Certificats = UrlBaseMEVWEB + "/certificats";
        public String Transactions = UrlBaseMEVWEB + "/transaction";
        String Utilisateurs = UrlBaseMEVWEB + "/utilisateur";
        String Document = UrlBaseMEVWEB + "/document";
        String Document_PROD = "https://api.rq-fo.ca/document";
    }

    interface Abrvt {
        String TRP = "TRP";
        String RBC = "RBC";
    }

    interface Acti {
        String TRP = "TRP";
        String RES = "RES";
        String BAR = "BAR";
        String CDR = "CDR";
        String HAB = "HAB";
        String NON = "NON";
        String SOB = "SOB";
    }

    interface ApprlInit {
        String SEV = "SEV";		//SRS single host
        String SRV = "SRV";		//Server-Client mode.
    }

    interface Envirn {
        String DEV = "DEV";		//dev environment
        String ESSAI = "ESSAI";	//test environment
        String PROD = "PROD";	//prod environment
    }

    interface FormImpr {
        String PAP = "PAP";		//paper
        String ELE = "ELE";		//email
        String PEL = "PEL";		//mix paper and email (Each electronic document must be marked "Copie électronique" in addition to any other  required notes. 

        String NON = "NON";		//not printed. used when a customer fails to pay or when a closing  receipt is revised (value used for the "negative transaction"before the production of the new closing receipt).
        String SOB = "SOB";		//
    }

    interface Modif {
        String AJO = "AJO";		//add new
        String REM = "REM";		//update existing
        String SUP = "SUP";		//delete
        String VAL = "VAL";		
    }

    interface ModImpr {
        String ANN = "ANN";		//Cancellation.---- This is an incomplete transaction that allows the user to cancel the entered information for various reasons  (e.g. input error). In this situation, the transaction must be sent to the WEB-SRM with the information  already entered, even though the customer does not receive the bill. If some mandatory information has not  yet been entered, the values for this information must be "SOB" and the amounts must be "+000000000.00".  If no item is entered, your SRS must add the "SOB" item. acA
        String RPR = "RPR";		//reprint for customer
        String DUP = "DUP";		//duplicated for mandatory
        String PSP = "PSP"; 	//Failure to pay ("PSP") – the customer left without paying the bill.
        String FAC = "FAC";		//bill
        String SOB = "SOB";
    }

    interface ModPai {
        String ARG = "ARG";
        String CHQ = "CHQ";
        String COU = "COU";
        String CRE = "CRE";
        String DEB = "DEB";
        String CPR = "CPR";
        String FID = "FID";
        String CRY = "CRY";
        String MVO = "MVO";
        String TFD = "TFD";
        String MIX = "MIX";
        String PAC = "PAC";
        String INC = "INC";
        String AUC = "AUC";
        String SOB = "SOB";
    }

    interface ModTrans {
        String OPE = "OPE";
        String FOR = "FOR";
    }

    interface RelaCommer {
        String B2B = "B2B";
        String B2C = "B2C";
        String B2G = "B2G";
    }

    interface Tax {
        String F = "F";
        String P = "P";
        String FP = "FP";
        String NON = "NON";
        String SOB = "SOB";
    }

    interface TypAdr {
        String DOM = "DOM";
        String FAC = "FAC";
        String LVR = "LVR";
        String SRV = "SRV";
    }

    interface TypDoc {
        String RUT = "RUT";
        String HAB = "HAB";
    }

    interface TypServ {
        String CMP = "CMP";
        String LVR = "LVR";
        String LVT = "LVT";
        String TBL = "TBL";
    }

    interface TypTrans {
        String SOUM = "SOUM";	//Quote 
        String ESTM = "ESTM";	//Estimate 
        String ADDI = "ADDI"; 	//Temporary bill
        String RFER = "RFER";	//Closing receipt
        String TIER = "TIER";	//Occasional third party 
        String SOB = "SOB";		//This value must be used when a transaction is cancelled and the mandatary has not
        						//indicated the type of  transaction. No document must be produced and given to the 
        						//customer, but the transaction must always be  sent to the WEB-SRM. 
    }

    interface TS {
        String A = "A";
        String E = "E";
    }

    interface CM {
        String UNIQUE = "Unique";
        String TOUS = "Tous";
    }

    public static String getModeImpr(String string) {
		if(string.contains(RE_PRINTED)) {
			return ModImpr.RPR;
		}else if(string.contains(RE_PRINTED_INTERNAL_USE)) {
			return ModImpr.DUP;
		}else if(string.contains(VOID)) {
			return ModImpr.ANN;
		}else if(string.contains(NOT_PAID)) {
			return ModImpr.PSP; 	//Failure to pay ("PSP") – the customer left without paying the bill.
		}else if(string.contains(PrintService.PAYMENT_FAIL)) {
			return ModImpr.ANN;
		}
		return ModImpr.FAC;
	}

	public static String getModPai(List<String> sndMsg) {
		if(sndMsg.get(3).startsWith(REFUND)){	//if it's refund.
			int p = sndMsg.get(3).indexOf(METHOD);
			if(p > 0) {
				return getMatchedPaytrans(sndMsg.get(3).substring(p + METHOD.length()).trim().split("\n"));
			}
		}else {
			String tText = sndMsg.get(4).trim(); // find out the payment.
			if(tText.contains(VOID)) {
				return ModPai.AUC;
			}
			if(!tText.contains(PrintUtil.REF_TO)) { //the element at this position might be a ref(including old moneys) not for sure a paid methods.
				int p = tText.indexOf("\n\n");		//so if this line does not contain "#ref to:", then it's receipt.
				if(p > 0) {
					tText = tText.substring(0, p);
				}
				return getMatchedPaytrans(tText.split("\n"));
			}
		}
		return ModPai.SOB;		//for bill, not receipt, give SOB.
	}

	public static String getMatchedPaytrans(String[] lines) {
		String paiementTrans = ModPai.SOB;
		int payMethodQT = 0;
		for (String string : lines) {
			if(string == null || string.trim().length() == 0) {
				continue;
			}else if(string.indexOf(":") < 0) {
				if( string.length() == 3){ //if it's directly the payment method.
					payMethodQT++;
					paiementTrans = string;
				}else {
					continue;	//if it's like foot message, ignore.
				}
			}else {
				string = string.substring(0, string.indexOf(":")).trim();
				switch (string) {
				case CASH:
					payMethodQT++;
					paiementTrans = ModPai.ARG;
					break;
				case "DEBIT":
					payMethodQT++;
					paiementTrans = ModPai.DEB;
					break;
				case "VISA":
					payMethodQT++;
					paiementTrans = ModPai.CRE;
					break;
				case "MASTER":
					payMethodQT++;
					paiementTrans = ModPai.CRE;
					break;
				case "PLATFORM":
					payMethodQT++;
					paiementTrans = ModPai.TFD;
					break;
				default: //including OTHER and other string we didn't predefined.
					if(!PAID_FROM.equals(string)) {
						payMethodQT++;
					}
					paiementTrans = ModPai.SOB; //before for UBER/SKIP we log as cash, but now we use AUT. base on Becky's understanding of CRA's doc.
				}
			}
		}
		if(payMethodQT > 1) {
			return ModPai.MIX;
		}else {
			return paiementTrans;
		}
	}
}
