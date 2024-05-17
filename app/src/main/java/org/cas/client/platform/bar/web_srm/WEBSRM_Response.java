package org.cas.client.platform.bar.web_srm;

import java.io.IOException;
import java.net.SocketException;

/// Processing the response received from the WEB-SRM<br/>
public class WEBSRM_Response
{
    // HTTP response status code
    public int StatusCode;

    // Description of HTTP status returned
    public String StatusDescription;

    // Contains the json document received from the WEB-SRM<br/>
    public String ResponseJsonWEBSRM;

    // Exception caused by a communication problem with the WEB-SRM and managed by the SEV<br/>
    public Exception ResponseExceptionWEBSRM;

//    // HTTP response from WEB-SRM containing success or error codes<br/>
//    public HttpResponseMessage HttpResponseMessageWEBSRM;

    // Constructeur
    public WEBSRM_Response( ) { }

	public void setStatusCode(int responseCode) {
		this.StatusCode = responseCode;
	}

	public void setStatusDescription(String responseMessage) {
		this.StatusDescription = responseMessage;
	}

	public void setResponseExceptionWEBSRM(Exception timeoutException) {
		this.ResponseExceptionWEBSRM = timeoutException;
	}

	public int getStatusCode() {
		return StatusCode;
	}

	public String getStatusDescription() {
		return StatusDescription;
	}

	public void setResponseJsonWEBSRM(String string) {
		this.ResponseJsonWEBSRM = string;
	}

	public String getResponseJsonWEBSRM() {
		return ResponseJsonWEBSRM;
	}


}
