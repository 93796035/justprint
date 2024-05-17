package org.cas.client.platform.bar.web_srm;

/// Transaction's field identifying one or more customer addresses
public class TransactionAdrClint{
    // Customer’s address type
    public String TypAdr;

    // The customer’s street number
    public String NoCiviq;

    // The customer’s street name
    public String Rue;

    // The customer’s city 
    public String Vil;

    // The customer’s postal code
    public String CP;

    // Transaction's field identifying one or more customer addresses
    // <param name="TypAdr">Customer’s address type.</param>
    // <param name="NoCiviq">The customer’s street number.</param>
    // <param name="Rue">The customer’s street name.</param>
    // <param name="Vil">The customer’s city.</param>
    // <param name="CP">The customer’s postal code</param>
    public TransactionAdrClint( String TypAdr, String NoCiviq, String Rue, String Vil, String CP ){
        this.TypAdr = TypAdr;
        this.NoCiviq = NoCiviq;
        this.Rue = Rue;
        this.Vil = Vil;
        this.CP = CP;
    }

    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson( ){
        StringBuilder s = new StringBuilder( );

        s.append( "{" );

        s.append( "\"typAdr\": \"").append(TypAdr).append("\",");

        s.append( "\"noCiviq\": \"").append(NoCiviq).append("\",");

        s.append( "\"rue\": \"").append(Rue).append("\",");

        s.append( "\"vil\": \"").append(Vil).append("\",");

        s.append( "\"cp\": \"").append(CP).append("\",");

        if( s.toString( ).endsWith( "," ) )
            s.deleteCharAt(s.lastIndexOf( "," ));

        s.append( "}" );

        return s.toString( );
    }
}
