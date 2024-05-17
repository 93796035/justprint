package org.cas.client.platform.bar.web_srm;

/// Information received from WEB-SRM during a request of type Certificate: SEV certificate and WEB-SRM certificate.
public class ResponseCertificats{
    // WEB-SRM's Certificate ( json tag certifPSI )
    public String CertificatMEVWEB;

    // SRS/POS's Certificate ( json tag certif )
    public String Certificat;

    // Device identifier
    public String IdApprl;

    // Next test case number that you must execute.
    public String ProchainCasEssai;

    // Information received from WEB-SRM during a request of type Certificate: SEV certificate and WEB-SRM certificate.
    // <param name="ProchainCasEssai">Next test case number that you must execute. </param>
    // <param name="Certif">SRS/POS's Certificate</param>
    // <param name="CertificatMEVWEB">WEB-SRM's Certificate</param>
    // <param name="IdApprl">Device identifier</param>
    public ResponseCertificats( String ProchainCasEssai, String CertificatMEVWEB, String Certif, String IdApprl ){
        this.ProchainCasEssai = ProchainCasEssai;
        this.Certificat = Certif;
        this.CertificatMEVWEB = CertificatMEVWEB;
        this.IdApprl = IdApprl;
    }
}
