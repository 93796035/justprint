package org.cas.client.platform.bar.web_srm;

/// Field identifying one or more references to preceding transactions.
public class TransactionReference{
    // Amount before taxes for the transaction to which the bill refers.
    public String AvantTax;

    // Time of the transaction to which the bill refers ( date, hour, minute and second).
    public String DatTrans;

    // Number identifying the transaction to which the document refers.
    public String NoTrans;

    // Field identifying one or more references to preceding transactions.
    // <param name="noTrans">Number identifying the transaction to which the document refers.</param>
    // <param name="datTrans">Time of the transaction to which the bill refers ( date, hour, minute and second).</param>
    // <param name="avantTax">Amount before taxes for the transaction to which the bill refers.</param>
    public TransactionReference( String noTrans, String datTrans, String avantTax ){
        this.NoTrans = noTrans;
        this.DatTrans = datTrans;
        this.AvantTax = avantTax;
    }

    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson( ){
        StringBuilder s = new StringBuilder( );
        s.append( "{" );
        s.append( "\"noTrans\": \"").append(NoTrans ).append("\",");
        s.append( "\"datTrans\": \"").append(DatTrans ).append("\",");
        s.append( "\"avantTax\": \"").append(AvantTax ).append("\",");
        s.append( "}" );

        return s.toString( );
    }

}
