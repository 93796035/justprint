package org.cas.client.platform.bar.web_srm;

import java.util.ArrayList;
import java.util.List;

/// Information regarding the custumer that acquires a paid or payable supply.
public class TransactionClint{
    // Customer’s name.
    public String NomClint;

    // Customerst’s QST registration number.
    public String NoTvqClint;

    // Customer’s main telephone number.
    public String Tel1;

    // Customer’s secondary telephone number.
    public String Tel2;
    
    // Customer’s email address.
    public String Courl;
    
    // List of customer's addresses.
    public List<TransactionAdrClint> lstAdrClients = new ArrayList<TransactionAdrClint>( );

    // Information regarding the custumer that acquires a paid or payable supply.
    // <param name="NomClint">Customer’s name.</param>
    // <param name="NoTvqClint">Customerst’s QST registration number.</param>
    // <param name="Tel1">Customer’s main telephone number.</param>
    // <param name="Tel2">Customer’s secondary telephone number.</param>
    // <param name="Courl">Customer’s email address.</param>
    // <param name="lstAdrClients">List of customer's addresses.</param>
    public TransactionClint( String NomClint, String NoTvqClint, String Tel1, String Tel2, String Courl, List<TransactionAdrClint> lstAdrClients ){
        this.NomClint = NomClint;
        this.NoTvqClint = NoTvqClint;
        this.Tel1 = Tel1;
        this.Tel2 = Tel2;
        this.Courl = Courl;
        this.lstAdrClients = new ArrayList<TransactionAdrClint>( lstAdrClients );
    }
    
    public TransactionClint(){}
    
    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson(){
        StringBuilder s = new StringBuilder( );

        s.append( "{");

        s.append( "\"nomClint\": \"").append(NomClint ).append("\",");

        s.append( "\"noTvqClint\": \"").append(NoTvqClint).append("\",");

        s.append( "\"tel1\": \"").append(Tel1).append("\",");

        s.append( "\"tel2\": \"").append(Tel2).append("\",");

        s.append( "\"courl\": \"").append(Courl).append("\",");

        // balise adr (Adresse Client)
        if( lstAdrClients.size() > 0 ){
            s.append( "\"adr\": [" );

            for( TransactionAdrClint TAC : lstAdrClients ){
                s.append( TAC.GetJson( ) );
            }

            if( s.toString( ).endsWith( "," ) )
                s.deleteCharAt(s.lastIndexOf( "," ));

            s.append( "]," );
        }

        // Enlève la virgule après le dernier
        if( s.toString( ).endsWith( "," ) )
            s.deleteCharAt(s.lastIndexOf( "," ));

        s.append( "}" );

        return s.toString( );

    }
}
