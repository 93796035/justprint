package org.cas.client.platform.bar.web_srm;

/// DOCUMENT Request - Frequent third party report structure
class DocumentHAB{
    // Frequent third party’s address
    public String AD;

    // Description of the property and services being provided or the subject of the contract amendment
    public String BS;

    // Frequent third party’s postal code
    public String CP;

    // Date of conclusion or modification of the contract
    public String DC;

    // Description of the property and services being provided or the subject of the contract amendment
    public String DE;

    // Date of entry into force of the contract or of the modification of the contract
    public String DV;

    // Frequency with which property and services are provided
    public String FR;

    //  Time the goods and services are provided (choice: 1 = Day, 2 = Evening, 3 = Night)
    public String MO;

    // Frequent third party’s Québec entreprise number (NEQ)
    public String NE;

    // Last name and first name of the individual or name of the entity
    public String NM;

    // <summary>
    // Numéro du poste (téléphone) du tiers habituel
    // Frequent third party’s extension (phone)
    // </summary>
    public String PO;

    // <summary>
    // Raison de la déclaration relatif au contrat (choix : 1 = Nouveau, 2 = Modification, 3 = Expiration)
    // Reason for reporting the contract (choice: 1 = New, 2 = Amendment, 3 = Expiration)
    // </summary>
    public String RA;

    // Operator’s digital signature
    public String SI;

    // Frequent third party’s area code and phone number
    public String TE;

    // Frequent third party’s QST registration number
    public String TQ;

    public DocumentHAB( ) { }

    // Concatenation of information to calculate the signature or to transmit the document to WEB-SRM
    // <returns>Informations concaténées
    //          Concatenated data</returns>
    public String GetDocumentConcatene( ){
        StringBuilder s = new StringBuilder( );
        s.append( "TQ=").append(TQ ).append(";");
        s.append( "NM=").append(NM ).append(";");
        s.append( "AD=").append(AD ).append(";");
        s.append( "CP=").append(CP ).append(";");
        s.append( "TE=").append(TE ).append(";");
        s.append( "PO=").append(PO ).append(";");
        s.append( "NE=").append(NE ).append(";");
        s.append( "RA=").append(RA ).append(";");
        s.append( "DC=").append(DC ).append(";");
        s.append( "DV=").append(DV ).append(";");
        s.append( "DE=").append(DE ).append(";");
        s.append( "BS=").append(BS ).append(";");
        s.append( "FR=").append(FR ).append(";");

        if ( SI == "" )
            // Concatenation for signature calculation
            s.append( "MO=").append(MO );
        else {
            // Concatenation to transmit the document to WEB-SRM
            s.append( "MO=").append(MO ).append(";");
            s.append( "SI=").append(SI );
        }

        return s.toString( );
    }

}