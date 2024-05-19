package org.cas.client.platform.bar.web_srm.utiles;

import org.cas.client.platform.casutil.L;

import org.cas.client.platform.bar.web_srm.ErrorWEBSRM;
import org.cas.client.platform.bar.web_srm.MevUtils;
import org.cas.client.platform.bar.web_srm.ResponseCertificats;
import org.cas.client.platform.bar.web_srm.ResponseTransaction;
import org.cas.client.platform.bar.web_srm.ResponseUtilisateur;
import org.cas.client.platform.bar.web_srm.ValuePossible;
import org.cas.client.platform.bar.web_srm.WEBSRM_Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;


/// Creation of json documents to transmit to WEB-SRM
/// Extraction of information contained in json documents received from WEB-SRM
/// The following packages are used and can be installed via the NuGet console/tool
/// - Newtonsoft.Json
/// - RestSharp
public class UtilesJSON{

	/// Request to add a certificate
	/// <param name="HTTPHeadersDict">Headers to be sent to WEB-SRM
	/// <param name="Json">Json document to be sent to the WEB-SRM
	/// <returns>WEB-SRM's response
	public static WEBSRM_Response CertificatesRequest(Map<String, String> HTTPHeadersDict, String Json) {
		return CertificatesRequest(ValuePossible.Modif.AJO, HTTPHeadersDict, Json, null);
	}

	/// Request to add, replace or delete a certificate
	/// <param name="Modif">The field indicates whether the modification concerns an
	/// addition, replacement or deletion of a certificate
	/// <param name="HTTPHeadersDict"> Headers to be sent to the WEB-SRM</param>
	/// <param name="Json">Json document to be sent to the WEB-SRM
	/// <param name="CertificateSerialNumberSRS">The field containing the serial
	/// number of the certificate when requesting a replacement ("REM") or deletion
	/// ("SUP"). Add null for certificate addition
	/// <returns>WEB-SRM's response</returns>
	public static WEBSRM_Response CertificatesRequest(String Modif, Map<String, String> HTTPHeadersDict,
			String Json, String CertificateSerialNumberSRS) {
		WEBSRM_Response response;

		if (ValuePossible.Modif.AJO.equals(Modif))
			response = sendJson(ValuePossible.UrlMEVWEB.Enrolement, HTTPHeadersDict, Json, null);
		else
			response = sendJson(ValuePossible.UrlMEVWEB.Certificats, HTTPHeadersDict, Json,
					CertificateSerialNumberSRS);

		return response;
	}

	/// Returns the json document for a request of type Certificate

	/// <param name="Modif">The field indicates whether the modification concerns an
	/// addition, a replacement or a deletion of a certificate
	/// <param name="CSR">The field containing the certificate signing request for
	/// an add ("AJO") or replace ("REM") request
	/// <param name="CertificateSerialNumberSRS">The field containing the serial
	/// number of the certificate when requesting a replacement ("REM") or removal
	/// ("SUP")
	/// <returns>Document json</returns>
	public static String GetJsonCertificate(String Modif, String CSR, String CertificateSerialNumberSRS) {
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append("   \"reqCertif\": {");
		s.append("      \"modif\": \"").append(Modif).append("\",");

		if (Modif.toUpperCase().equals(ValuePossible.Modif.AJO))
			s.append("      \"csr\": \"{0}\"").append(CSR).append("\",");

		if (Modif.toUpperCase().equals(ValuePossible.Modif.REM))
			s.append("      \"csr\": \"").append(CSR).append("\",");

		if (CertificateSerialNumberSRS != null)
			s.append("      \"noSerie\": \"{0}\"").append(CertificateSerialNumberSRS).append("\",");

		s.append("   }");
		s.append("}");
		return s.toString();
	}

    // Parse the response of a request of type Certificats
    // <param name="JSon">JSon document containing the WEB-SRM response
    // <returns>Certificates received from the WEB-SRM
	public static ResponseCertificats ParseResponseCertificates(String JSon) {
		try {
			if (JSon.contains("listErr")) {
				System.out.println(indentJson(JSon));
				return null;
			}

			JSONObject json = new JSONObject(JSon);
			JSONObject retourCertif = json.getJSONObject("retourCertif");

			String idApprl = "";
			if (retourCertif.has("idApprl")) {
				idApprl = retourCertif.getString("idApprl");// BarOption.setIdApprl();
			}

			String prochainCasEssai = "";
			if (retourCertif.has("casEssai")) {
				prochainCasEssai = retourCertif.getString("casEssai");// BarOption.setCasEssai();
			}

			String certifPSI = "";
			if (retourCertif.has("certifPSI")) {
				certifPSI = retourCertif.getString("certifPSI");
			}

			String certif = "";
			if (retourCertif.has("certif")) {
				certif = retourCertif.getString("certif");
			}

			return (new ResponseCertificats(prochainCasEssai, certifPSI, certif, idApprl));
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

    // Request to add, replace or delete a User

    // <param name="HTTPHeadersDict">Headers to be sent to the WEB-SRM
    // <param name="Json">Json document to be sent to the WEB-SRM
    // <param name="CertificateSerialNumberSRS">Serial number of the SRM/POS's certificate to be used to authenticate to the WEB-SRM
    // <returns>WEB-SRM's response
	public static WEBSRM_Response UserRequest(Map<String, String> HTTPHeadersDict, String Json,
			String CertificateSerialNumberSRS) {
		return sendJson(ValuePossible.UrlMEVWEB.Utilisateurs, HTTPHeadersDict, Json, CertificateSerialNumberSRS);
	}


    // Returns the json document for a request of type Utilisateur
    // <param name="Modif"> The field indicating whether the change concerns the addition, deletion or validation of the GST and QST registration numbers assigned to the mandatory</param>
    // <param name="NomUtil">The name of the user who produces the invoice to the customer</param>
    // <param name="NoTPS">The GST registration number assigned to the mandatory</param>
    // <param name="NoTVQ">The QST registration number assigned to the mandatory</param>
    // <returns>Document json</returns>
	public static String GetJsonUser(String Modif, String NomUtil, String NoTPS, String NoTVQ) {
		StringBuilder s = new StringBuilder();

		s.append("{");
		s.append("\"reqUtil\": {");
		s.append("\"modif\": \"").append(Modif).append("\",");

		if (Modif.toUpperCase().equals(ValuePossible.Modif.AJO)
				|| Modif.toUpperCase().equals(ValuePossible.Modif.SUP))
			s.append("\"nomUtil\": \"").append(NomUtil).append("\",");

		s.append("\"noTax\": {");
		s.append("\"noTPS\": \"").append(NoTPS).append("\",");
		s.append("\"noTVQ\": \"").append(NoTVQ).append("\"");
		s.append("}");
		s.append("}");
		s.append("}");

		return s.toString();
	}


    // Parse the response of a request of type Utilisateur
    // <param name="JSon">json document received from the WEB-SRM</param>
    // <returns>Response received from the WEB-SRM</returns>
    public static ResponseUtilisateur ParseResponseUtilisateur( String JSon )
    {
        try
        {
            JSONObject json = new JSONObject( JSon );
			JSONObject retourUtil = json.getJSONObject("retourUtil");
			
            String prochainCasEssai = "";
            if(retourUtil.has("casEssai"))
                prochainCasEssai = retourUtil.getString("casEssai");

            String NoTPS = "";
            String NoTVQ = "";
            if(retourUtil.has("noTax"))
            {
                NoTPS = retourUtil.getJSONObject("noTax").getString("noTPS");
                NoTVQ = retourUtil.getJSONObject("noTax").getString("noTVQ");
            }

            return ( new ResponseUtilisateur( prochainCasEssai, NoTPS, NoTVQ ) );
        }
        catch( Exception ex )
        {
            System.out.println( ex );
            return null;
        }
    }

    // Transmission d'une transaction au MEVWEB
    // <param name="HTTPHeadersDict">Headers to be sent to the WEB-SRM
    // <param name="Json">Json document to be sent to the WEB-SRM
    // <param name="CertificateSerialNumberSRS">Serial number of the SRM/POS's certificate to be used to authenticate to the WEB-SRM
    // <returns>Response received from the WEB-SRM
    public static WEBSRM_Response TransactionRequest(Map<String, String> HTTPHeadersDict, String Json, String CertificateSerialNumberSRS )
    {
    	//TODO: check if given parameter is null, null means is calling when app exit, then only check the webmev batch.
    	//other wise, combine current with batch.
    	//I guess should do this in a loop? or shall we do in in higher level?
        return sendJson( ValuePossible.UrlMEVWEB.Transactions, HTTPHeadersDict, Json, CertificateSerialNumberSRS );
    }


    // Parses the response of a request of type Transaction
    // <param name="JSon">json document received from WEB-SRM
    // <returns>Response received from the WEB-SRM
    public static ResponseTransaction ParseResponseTransaction( String JSon )
    {
        try
        {
            String NoTrans = "";
            String PsiNoTrans = "";
            String PsiDatTrans = "";
            String datLot = null;
            String noLot = null;


            JSONObject json = new JSONObject( JSon );
			JSONObject retourTrans = json.getJSONObject("retourTrans");

            String prochainCasEssai = "null";
            if(retourTrans.has("casEssai"))
                prochainCasEssai =retourTrans.getString("casEssai");

            if(retourTrans.has("retourTransActu"))
            {
            	JSONObject retourTransActu = retourTrans.getJSONObject("retourTransActu");
                if(retourTransActu.has("noTrans"))
                    NoTrans =retourTransActu.getString("noTrans");
                else
                    NoTrans = "null";

                if(retourTransActu.has("psiNoTrans"))
                    PsiNoTrans =retourTransActu.getString("psiNoTrans");
                else
                    PsiNoTrans = "null";

                if(retourTransActu.has("psiDatTrans"))
                    PsiDatTrans =retourTransActu.getString("psiDatTrans");
                else
                    PsiDatTrans = "null";
            }

            // Batch of transactions
            if(retourTrans.has("retourTransLot"))
            {
            	JSONObject retourTransLot = retourTrans.getJSONObject("retourTransLot");
                if(retourTransLot.has("datLot"))
                    datLot =retourTransLot.getString("datLot");
                else
                    datLot = "null";

                if(retourTransLot.has("noLot"))
                    noLot =retourTransLot.getString("noLot");
                else
                    noLot = "null";
            }
            return ( new ResponseTransaction( prochainCasEssai, NoTrans, PsiNoTrans, PsiDatTrans, datLot, noLot ) );
        }
        catch( Exception ex )
        {
            System.out.println( ex );
            return null;
        }
    }

    // Sending a Document request to the WEB-SRM
    // <param name="HTTPHeadersDict">Headers to be sent to the WEB-SRM
    // <param name="Json">Json document to be sent to the WEB-SRM
    // <param name="CertificateSerialNumberSRS">Serial number of the SRM/POS's certificate to be used to authenticate to the WEB-SRM
    // <returns>Response received from the WEB-SRM</returns>
    public static WEBSRM_Response DocumentRequest(Map<String, String> HTTPHeadersDict, String Json, String CertificateSerialNumberSRS )
    {
        return sendJson( ValuePossible.UrlMEVWEB.Document, HTTPHeadersDict, Json, CertificateSerialNumberSRS );
    }


    public static String GetJsonDocument( String TypeDoc, String Doc )
    {
        StringBuilder s = new StringBuilder( );

        s.append( "{" );
        s.append( "\"reqDoc\": {" );
        s.append( "\"typDoc\": \"").append( TypeDoc ).append("\",");
        s.append( "\"doc\": \"").append( Doc ).append("\"");
        s.append( "}" );
        s.append( "}" );

        return s.toString();
    }

    // Parses the response received from WEB-SRM containing a list of errors
    // Analyse la réponse reçue du MEV-WEB contenant une liste d'erreurs

    // <param name="JSon">json document received from WEB-SRM
    //                    Document json reçu du MEV-WEB</param>
    // <returns>Errors received from the WEB-SRM
    //          Erreurs reçues du MEV-WEB</returns>
    public static List<ErrorWEBSRM> ParseErrors( String JSon )
    {
        try
        {
            List<ErrorWEBSRM> lstErr = new ArrayList<ErrorWEBSRM>( );
//            IEnumerable<JToken> ListeErreurs;
//
//            // Extraction des erreurs avec LINQ
//            JSONObject ObjErreurs = new JSONObject( JSon );
//
//            if( JSon.contains( "retourCertif" ) )
//                ListeErreurs = from p in ObjErreurs[ "retourCertif" ][ "listErr" ] select p;
//            else
//            if( JSon.contains( "retourUtil" ) )
//                ListeErreurs = from p in ObjErreurs[ "retourUtil" ][ "listErr" ] select p;
//            else
//            if( JSon.contains( "retourTransActu" ) )
//                ListeErreurs = from p in ObjErreurs[ "retourTrans" ][ "retourTransActu" ][ "listErr" ] select p;
//            else
//            if( JSon.contains( "retourDoc" ) )
//                ListeErreurs = from p in ObjErreurs[ "retourDoc" ][ "listErr" ] select p;
//            else
//                throw new FormatException( "The received json document does not contain one of the following values : retourCertif, retourUtil, retourTrans or retourDoc. Le document json reçu ne contient pas une des valeurs suivantes : retourCertif, retourUtil, retourTrans ou retourDoc." );
//
//
//            foreach( var err in ListeErreurs )
//            {
//                String Id = "";
//                if( err[ "id" ] != null )
//                    Id = err[ "id" ].toString();
//
//                String Mess = "";
//                if( err[ "mess" ] != null )
//                    Mess = err[ "mess" ].toString();
//
//                String CodRetour = "";
//                if( err[ "codRetour" ] != null )
//                    CodRetour = err[ "codRetour" ].toString();
//
//                String NoTrans = "";
//                if( err[ "noTrans" ] != null )
//                    NoTrans = err[ "noTrans" ].toString();
//
//                lstErr.Add( new ErrorWEBSRM( Id, Mess, CodRetour, NoTrans ) );
//            }
            return ( lstErr );
        }
        catch( Exception e )
        {
            System.out.println( e );
            return null;
        }
    }

	public static WEBSRM_Response sendJson(String url, Map<String, String> httpHeadersDict, String json,
			String certificateSerialNumberSRS) {
		WEBSRM_Response responseWEBSRM = new WEBSRM_Response();

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/json");
			for (Entry<String, String> entry : httpHeadersDict.entrySet()) {
				if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}
			}
			//TODO: if there's content in batch, should include them
			httpPost.setEntity(new StringEntity(json,
					ContentType.create("application/json", java.nio.charset.StandardCharsets.US_ASCII)));

			//if (certificateSerialNumberSRS != null) {
				KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
				keyStore.load(new FileInputStream(MevUtils.KEYSTORE_FILE), MevUtils.KEYSTORE_KEY.toCharArray());

				KeyManagerFactory keyManagerFactory = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, MevUtils.KEYSTORE_KEY.toCharArray());

				SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
			        
				httpClient = HttpClients.custom().setSslcontext(sslContext).build();
			//}
			
			//System.setProperty("javax.net.debug", "ssl:handshake");
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusDescription = httpResponse.getStatusLine().getReasonPhrase();
			String responseJsonWEBSRM = EntityUtils.toString(httpResponse.getEntity());

			L.i("UtilesJSON", "Status code: " + statusCode);
			L.i("UtilesJSON", "Status Description: " + statusDescription);
			L.i("UtilesJSON", responseJsonWEBSRM);

			responseWEBSRM.setStatusCode(statusCode);
			responseWEBSRM.setStatusDescription(statusDescription);
			responseWEBSRM.setResponseJsonWEBSRM(responseJsonWEBSRM);

			httpResponse.close();
			httpClient.close();
		} catch (IOException e) {
			L.e("UtilesJSON", "", e);
			responseWEBSRM.setResponseExceptionWEBSRM(e);
		} catch (KeyStoreException e) {
			L.e("UtilesJSON", "", e);
		} catch (NoSuchAlgorithmException e) {
			L.e("UtilesJSON", "", e);
		} catch (KeyManagementException e) {
			L.e("UtilesJSON", "", e);
		} catch (CertificateException e) {
			L.e("UtilesJSON", "", e);
		} catch (UnrecoverableKeyException e) {
			L.e("UtilesJSON", "", e);
		}

		return responseWEBSRM;
	}
    
    // Send a request to WEB-SRM with a certificate
    // <param name="Url">URL complète du service à appeler chez le MEV-WEB</param>
    // <param name="HTTPHeadersDict">Headers to be sent to the WEB-SRM
    // <param name="Json">Json document to be sent to the WEB-SRM
    // <param name="CertificateSerialNumberSRS">Serial number of the SRM/POS's certificate to be used to authenticate to the WEB-SRM
    // <returns>Message received from the WEB-SRM
//	public static WEBSRM_Response sendJson(String url, List<Map.Entry<String, String>> httpHeadersDict, String json,
//			String certificateSerialNumberSRS) {
//		
//		WEBSRM_Response responseWEBSRM = new WEBSRM_Response();
//
//		HttpsURLConnection connection = null;
//		try {
//			URL urlObj = new URL(url);
//			connection = (HttpsURLConnection) urlObj.openConnection();
//			connection.setRequestMethod("POST");
//			connection.setConnectTimeout(5000);
//			connection.setReadTimeout(5000);
//			connection.setRequestProperty("Content-Type", "application/json");
//
//			for (Map.Entry<String, String> entry : httpHeadersDict) {
//				String key = entry.getKey();
//				String value = entry.getValue();
//				if (value != null && !value.isEmpty()) {
//					connection.setRequestProperty(key, value);
//					System.out.println(key + "=" + value);
//				}
//			}
//			System.out.println(UtilesJSON.indentJson(json));
//
//			//json = json.replace("\"", "\\\"");
//			System.out.println(json);
//			
//			if (certificateSerialNumberSRS != null) {
//				KeyStore keyStore = KeyStore.getInstance(MevUtils.KEYSTORE_TYPE);
//
//				TrustManagerFactory trustManagerFactory = TrustManagerFactory
//						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//				trustManagerFactory.init(keyStore);
//
//				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
//					public X509Certificate[] getAcceptedIssuers() {
//						return null;
//					}
//
//					public void checkClientTrusted(X509Certificate[] certs, String authType) {
//					}
//
//					public void checkServerTrusted(X509Certificate[] certs, String authType) {
//					}
//				} };
//				SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//				sslContext.init(null, trustAllCerts, new SecureRandom());
//				connection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//                  connection.setSSLSocketFactory(sslContext.getSocketFactory());
//			}
//
//			connection.setDoOutput(true);
//			try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
//				outputStream.write(json.getBytes(StandardCharsets.US_ASCII));
////			}
////		     // 获取输出流并写入请求数据
////	        try (OutputStream os = connection.getOutputStream();
////	             OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
////	            String requestData = "{\"key\": \"value\"}";
////	            writer.write(json);
////	            writer.flush();
//	        }catch(Exception e) {
//	        	System.out.println("................................");
//	        }
//			int responseCode = connection.getResponseCode();
//			responseWEBSRM.setStatusCode(responseCode);
//			responseWEBSRM.setStatusDescription(connection.getResponseMessage());
//
//			try (BufferedReader bufferedReader = new BufferedReader(
//					new InputStreamReader(connection.getInputStream()))) {
//				StringBuilder responseContent = new StringBuilder();
//				String line;
//				while ((line = bufferedReader.readLine()) != null) {
//					responseContent.append(line);
//				}
//				responseWEBSRM.setResponseJsonWEBSRM(responseContent.toString());
//			}
//
//			System.out.println("Status code : " + responseWEBSRM.getStatusCode());
//			System.out.println("Status Description : " + responseWEBSRM.getStatusDescription());
//			System.out.println(UtilesJSON.indentJson(responseWEBSRM.getResponseJsonWEBSRM()));
//		} catch (java.net.SocketException timeoutException) {
//			System.out.println(timeoutException);
//			responseWEBSRM.setResponseExceptionWEBSRM(timeoutException);
//		} catch (IOException ex) {
//			System.out.println(ex);
//			responseWEBSRM.setResponseExceptionWEBSRM(ex);
//		} catch (Exception ex) {
//			System.out.println(ex);
//			responseWEBSRM.setResponseExceptionWEBSRM(ex);
//		} finally {
//			if (connection != null) {
//				connection.disconnect();
//			}
//		}
//
//		return responseWEBSRM;
//	}
    
    // Indentation of a json document
    // <param name="json">String containing the json to indent
    // <returns>String containing the indented json
    public static String indentJson(String json) {
        String formattedJson;
        try {
            Object parsedJson = new JSONObject(json);
            if (parsedJson instanceof JSONObject) {
                formattedJson = ((JSONObject) parsedJson).toString(4); // 4 spaces for indentation
            } else if (parsedJson instanceof JSONArray) {
                formattedJson = ((JSONArray) parsedJson).toString(4);
            } else {
                formattedJson = "Invalid JSON data";
            }
        } catch (JSONException e) {
            formattedJson = e.getMessage();
        }
        return formattedJson;
    }

}
