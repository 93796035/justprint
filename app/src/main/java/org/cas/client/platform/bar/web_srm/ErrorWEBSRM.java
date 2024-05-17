package org.cas.client.platform.bar.web_srm;

/// Error received from the WEB-SRM
public class ErrorWEBSRM{
    // Error message identifier.
    public String Id;

    // Error message returned from the WEB-SRM
    public String Mess;

    // Return code of the error message
    public String CodRetour;

    // The number of the transaction containing an error in the case of a Transaction request
    public String NoTrans;

    // Erreur reçue du MEVWEB
    // <param name="Id">Error message identifier.</param>
    // <param name="Mess">Error message returned from the WEB-SRM</param>
    // <param name="CodRetour">Return code of the error message</param>
    // <param name="NoTrans">The number of the transaction containing an error in the case of a Transaction request</param>
    public ErrorWEBSRM( String Id, String Mess, String CodRetour, String NoTrans ){
        this.Id = Id;
        this.Mess = Mess;
        this.CodRetour = CodRetour;
        this.NoTrans = NoTrans;
    }
}
