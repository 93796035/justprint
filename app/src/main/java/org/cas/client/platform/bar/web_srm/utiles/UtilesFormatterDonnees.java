package org.cas.client.platform.bar.web_srm.utiles;

import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.BarUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class UtilesFormatterDonnees {
    /*
    Exemple d'utilisation des fonctions de cette classe :

    Console.WriteLine( UtilesFormatterDonnees.formatterQte( " 109.55    " ) );
    +00109.55

    Console.WriteLine( UtilesFormatterDonnees.formatterQte( ( decimal )-9.55 ) );
    -00009.55

    Console.WriteLine( UtilesFormatterDonnees.formatterMontant( "      +09.55               " ) );
    +000000009.55

    Console.WriteLine( UtilesFormatterDonnees.formatterMontant( ( decimal )-0009.56 ) );
    -000000009.56

     */

    
    // Format the noTabl field as XXXXX padded left with =
    // <param name="noTabl">Champ noTabl</param>
    // <returns>Champ noTabl au format XXXXX</returns>
	public static String formatterNoTabl(String noTabl) throws Exception {
		if(noTabl.startsWith("#")) {
			noTabl = noTabl.substring(1);
		}
	    if (noTabl.length() > 5) {
	        throw new Exception("noTabl doit avoir 5 caractères max");
	    }

	    StringBuilder strFR = new StringBuilder();
	    for (int i = 0; i < 5 - noTabl.length(); i++) {
	    	strFR.append("=");
	    }
	    strFR.append(noTabl);
	    
	    return strFR.toString();
	}
    
    // Format the NbClint field as 000
    // <param name="NbClint">Champ NbClint</param>
    // <returns>Champ qte au format 000</returns>
	public static String formatterNbClint(String nbClint) throws Exception {
	    try {
	        NumberFormat numberFormat = new DecimalFormat("000");
	        Number number = numberFormat.parse(nbClint);
	        return numberFormat.format(number);
	    } catch (ParseException e) {
	        throw new Exception(e.getMessage());
	    }
	}

    // Format the qte field as +/-00000.00
    // <param name="Qte">Champ qte</param>
    // <returns>Champ qte au format +/-00000.00</returns>
	public static String formatterQte(String qte) throws Exception {
	    try {
	        qte = qte.trim();
	        double qteValue = Double.parseDouble(qte.replace(",", ".")); // Replace , with . for decimal parsing

	        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
	        DecimalFormat decimalFormat = new DecimalFormat("+#00000.00;-#00000.00", symbols);
	        
	        return decimalFormat.format(qteValue);
	    } catch (NumberFormatException e) {
	        throw new Exception(e.getMessage());
	    }
	}

    // Formatter les champs qte, unitr selon le format +/-00000.00<br/>
    // Format the champs qte, unitr fields as +/-00000.00

    // <param name="Qte">Champ qte</param>
    // <returns>Champ qte au format +/-00000.00</returns>
	public static String formatterQte(double qte) throws Exception {
	    try {
	        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
	        DecimalFormat decimalFormat = new DecimalFormat("+#00000.00;-#00000.00", symbols);

	        return decimalFormat.format(qte).replace(",", ".");
	    } catch (Exception e) {
	        throw new Exception(e.getMessage());
	    }
	}
    
    // Format the fields (unitr, price, beforeTax, etc.) containing a $ amount as +/-000000000.00
    // <param name="Montant">Champ prix</param>
    // <returns>Champ au format +/-000000000.00</returns>
	public static String formatterMontant(String montant) {
	    try {
	        montant = montant.trim();
	        double montantValue = Double.parseDouble(montant.replace(",", ".")); // Replace , with . for decimal parsing
	        
	        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
	        DecimalFormat decimalFormat = new DecimalFormat("+#000000000.00;-#000000000.00", symbols);
	        
	        return decimalFormat.format(montantValue).replace(",", ".");
	    } catch (NumberFormatException e) {
	        L.e("UtilesForm", "Exception when formatting montnt:" + montant, e);
	    }
		return null;
	}
    
    // Format the fields (unitr, price, beforeTax, etc.) containing a $ amount as +/-000000000.00
    // <param name="Montant">Champ prix</param>
    // <returns>Champ au format +/-000000000.00</returns>
	public static String formatterMontant(float montant) {
	    try {
	        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
	        DecimalFormat decimalFormat = new DecimalFormat("+#000000000.00;-#000000000.00", symbols);

	        String formattedMontant = decimalFormat.format(montant);
	        return formattedMontant.replace(",", ".");
	    } catch (Exception e) {
	        // 在 Java 中，Format 异常通常不是 FormatException，而是 IllegalArgumentException
	        System.err.println("UtilesFormatterDonnees: " + e.getMessage());
	        return null;
	    }
	}

    // Format the fields Date and Time (datTrans) as AAAAMMDDhhmmss
    // String FormattedDateTime = BarUtil.df.format(new Date())
    // <param name="dateTime">Champ à formatter </param>
    // <returns>Date et heure formattées au format AAAAMMJJhhmmss</returns>
	public static String GetDateHeureFormatté(Date dateTime) {
		try {
//			return DateFormat.getInstance().format(dateTime.toLocaleString("yyyyMMddHHmmss");
			return BarUtil.sdf.format(dateTime);
		} catch (Exception e) {
			L.e("UtilesFormatterDonnees", e.getMessage(), e);
			return null;
		}
	}

}