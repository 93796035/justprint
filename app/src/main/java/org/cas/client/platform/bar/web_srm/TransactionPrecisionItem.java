package org.cas.client.platform.bar.web_srm;



/// Details of a transaction item for the creation of the JSon document
public class TransactionPrecisionItem{
    // Quantity of each supply paid or payable.
    public String Qte;

    // Description of each supply paid or payable.
    public String Descr;
    
    // The unit price paid or payable in respect of the supply.
    public String Unitr;

    // Value of the consderation paid or payable for the supply. The price must correspond to the quantity multiplied by the unit price.
    public String Prix;

    // Indicator that tax applies to each supply paid or payable.
    public String Tax;

    // Abbreviation for the activity subsector of the item.
    public String Acti;

    // Field identifying the details of a supply paid or payable.
    
    // <param name="Qte">Quantity of each supply paid or payable.</param>
    // <param name="Descr">Description of each supply paid or payable.</param>
    // <param name="Unitr">The unit price paid or payable in respect of the supply.</param>
    // <param name="Prix">Value of the consderation paid or payable for the supply.</param>
    // <param name="Tax">Indicator that tax applies to each supply paid or payable.</param>
    // <param name="Acti">Abbreviation for the activity subsector of the item.</param>
    public TransactionPrecisionItem( String Qte, String Descr, String Unitr, String Prix, String Tax, String Acti ){
        this.Qte = Qte;
        this.Descr = Descr;
        this.Unitr = Unitr;
        this.Prix = Prix;
        this.Tax = Tax;
        this.Acti = Acti;
    }

    
    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson( ){
        StringBuilder s = new StringBuilder( );
        s.append( "{" );
        s.append( "\"qte\": \"").append(Qte ).append("\",");
        s.append( "\"descr\": \"").append(Descr ).append("\",");
        s.append( "\"unitr\": \"").append(Unitr ).append("\",");
        s.append( "\"prix\": \"").append(Prix ).append("\",");
        s.append( "\"tax\": \"").append(Tax ).append("\",");
        s.append( "\"acti\": \"").append(Acti ).append("\"");
        s.append( "}," );

        return s.toString( );
    }
}