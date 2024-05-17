package org.cas.client.platform.bar.web_srm;

/// Sector of activity
public class TransactionSectActi{
    // Abbreviation of the business’s sector of activity.
    public String Abrvt;

    // Number of customers.
    public String NbClint;

    // Table number.
    public String NoTabl;

    // Service type.
    public String TypServ;

    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson(){
        StringBuilder s = new StringBuilder( );

        s.append( "{" );

        if( Abrvt != null && Abrvt.length() > 0)
            s.append("\"abrvt\": \"").append(Abrvt).append("\",");

        if(TypServ != null && TypServ.length() > 0 )
            s.append( "\"typServ\": \"").append(TypServ).append("\",");

        if(NoTabl != null && NoTabl.length() > 0)
            s.append( "\"noTabl\": \"").append(NoTabl).append("\",");

        if( NbClint != null && NbClint.length() > 0 )
            s.append( "\"nbClint\": \"").append(NbClint).append("\",");

        s.append( "}" );

        return s.toString( );
    }
}
