package org.cas.client.platform.bar.web_srm;

/// The field giving the address of the business named on the document.
public class TransactionDocAdr{
    // The street number of the business named on the document.
    public String DocCodePostal;

    // The postal code of the business named on the document.
    public String DocNoCiviq;

    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson( ){
        StringBuilder s = new StringBuilder( );

        s.append( "{" );

        if(DocNoCiviq != null && DocNoCiviq.length() > 0 )
            s.append( "\"docNoCiviq\": \"").append(DocNoCiviq).append("\",");

        if( DocCodePostal != null && DocCodePostal.length() > 0)
            s.append( "\"docCp\": \"").append(DocCodePostal).append("\",");
        
        s.append( "}" );

        return s.toString();
    }
}