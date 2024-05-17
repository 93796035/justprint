package org.cas.client.platform.bar.web_srm;

/// Information received from WEB-SRM during a request of type Transaction
public class ResponseUtilisateur {
	/// Next test case number that you must execute.
	public String ProchainCasEssai;

	/// GST registration number of the mandatary
	public String NoTPS;

	/// QST registration number of the mandatary
	public String NoTVQ;

	/// <param name="ProchainCasEssai">Next test case number that you must
	/// execute</param>
	/// <param name="NoTPS">GST registration number of the mandatary</param>
	/// <param name="NoTVQ">QST registration number of the mandatary</param>
	public ResponseUtilisateur(String ProchainCasEssai, String NoTPS, String NoTVQ) {
		this.ProchainCasEssai = ProchainCasEssai;
		this.NoTPS = NoTPS;
		this.NoTVQ = NoTVQ;
	}
}