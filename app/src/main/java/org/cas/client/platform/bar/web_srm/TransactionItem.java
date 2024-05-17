package org.cas.client.platform.bar.web_srm;

import java.util.ArrayList;
import java.util.List;

/// Field identifying one or more supplies that have been paid or are payable.
public class TransactionItem{
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

    
    // L’abréviation du sous secteur d’activité de l’item.
    // Abbreviation for the activity subsector of the item.
    
    public String Acti;

    
    // Liste des précisions pour un item d'une transaction.
    // List of details for an item in a transaction.
    
    public List<TransactionPrecisionItem> LstPrecisions;

    
    // Le champ identifiant une ou plusieurs fournitures payées ou payables.
    // Field identifying one or more supplies that have been paid or are payable.
    
    // <param name="Qte">Quantity of each supply paid or payable.</param>
    // <param name="Descr">Description of each supply paid or payable.</param>
    // <param name="Unitr">The unit price paid or payable in respect of the supply.</param>
    // <param name="Prix">Value of the consderation paid or payable for the supply. The price must correspond to the quantity multiplied by the unit price.</param>
    // <param name="Tax"> Indicator that tax applies to each supply paid or payable.</param>
    // <param name="Acti">Abbreviation for the activity subsector of the item.</param>
    // <param name="LstPrecisionsRecues">List of details for an item in a transaction.</param>
    public TransactionItem( String Qte, String Descr, String Unitr, String Prix, String Tax, String Acti,
    		List<TransactionPrecisionItem> LstPrecisionsRecues ){
        this.Qte = Qte;
        this.Descr = Descr;
        this.Unitr = Unitr;
        this.Prix = Prix;
        this.Tax = Tax;
        this.Acti = Acti;
        this.LstPrecisions = new ArrayList<TransactionPrecisionItem>( LstPrecisionsRecues );
    }
    
    public TransactionItem( String Qte, String Descr, String Unitr, String Prix, String Tax, String Acti ){
        this.Qte = Qte;
        this.Descr = Descr;
        this.Unitr = Unitr;
        this.Prix = Prix;
        this.Tax = Tax;
        this.Acti = Acti;
        this.LstPrecisions = null;
    }


    // Retourne le contenu dans un document json
    // Returns the content in a json document
    // <returns>json document</returns>
    public String GetJson( )
    {
        StringBuilder s = new StringBuilder( );

        s.append( "{" );

        s.append( "\"qte\": \"").append(Qte ).append("\",");

        s.append( "\"descr\": \"").append(Descr ).append("\",");

        s.append( "\"unitr\": \"").append(Unitr ).append("\",");

        s.append( "\"prix\": \"").append(Prix ).append("\",");

        s.append( "\"tax\": \"").append(Tax ).append("\",");

        // TODO Valider gestion du null
        if(Acti != null && Acti.length() > 0 )
            if( LstPrecisions != null ){
                if( LstPrecisions.size() == 0 )
                    s.append( "\"acti\": \"").append(Acti ).append("\",");
                else
                    s.append( "\"acti\": \"").append(Acti ).append("\",");
            }else
            	s.append( "\"acti\": \"").append(Acti ).append("\",");

		// Précisions
		if (LstPrecisions != null) {
			
			if (LstPrecisions.size() > 0) {
				
				s.append("\"preci\": [");

				for (TransactionPrecisionItem PI : LstPrecisions) {
					s.append(PI.GetJson());
				}

				if (s.toString().endsWith(",")) {
					s.deleteCharAt(s.lastIndexOf(","));
				}
				s.append("],");
			}
		}

        if( s.toString( ).endsWith( "," ) && s.toString( ).lastIndexOf( "]" ) > 0)
            s.deleteCharAt( s.toString( ).lastIndexOf( "]" ) + 1);

        s.append( "}" );

        return s.toString( );
    }
}
