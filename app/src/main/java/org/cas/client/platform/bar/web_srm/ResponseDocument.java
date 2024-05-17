package org.cas.client.platform.bar.web_srm;

/// Information received from WEB-SRM during a request of type Document
public class ResponseDocument {
	/// Next test case number that you must execute
	public String ProchainCasEssai;

	/// Unique document number
	public String NoDoc;

	/// Information received from WEB-SRM during a request of type Transaction
	/// <param name="ProchainCasEssai">Next test case number that you must
	/// execute</param>
	/// <param name="NoDoc">Unique document number</param>
	public ResponseDocument(String JsonVersi, String ProchainCasEssai, String NoDoc) {
		this.ProchainCasEssai = ProchainCasEssai;
		this.NoDoc = NoDoc;
	}
}
