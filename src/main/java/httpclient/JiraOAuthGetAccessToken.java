package httpclient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class JiraOAuthGetAccessToken {
	private static final String OAUTH_AUTHORIZE_PATH = "/plugins/servlet/oauth/authorize";
	private static final String TEM_REQUEST_TOKEN_PATH = "/plugins/servlet/oauth/request-token";
	private String consumerKey;//set in jira->product->link
	private String privateKey;//ssl command created private key
	//private String accessToken;

	private String request_token; //get tem token from oauth_token in response 1
	private String oauth_token_secret;//get value from oauth_token_secret in response

	private OAuthSigner signer; //used to signature request string using RSA
	private String callback; //oauth_callback in request header
	private String nonce;//oauth_nonce in request header
	private String timestamp;//oauth_timestamp in request header
	private String REQUESTMETHOD = "POST";//used to in signature string.
	private String signatureMethod;//oauth_signature_method in request header
	private String tempToken;//oauth_token in request header. used to call service API
	private String verifier;//oauth_verifier in request header. used in access token request.
	private String version;// oauth_version in request header, usually 1.0 or null.
	private String signature;//oauth_signature in request header

	private String reponseString;

	private String oauth_token;//get access token from oauth_token in response 2
	private String oauth_callback_confirmed;

	private String jiraBaseURL;
	private String jiraRequestTokenURL;
	private String authorizationUrl;

	/** Secure random number generator to sign requests. */
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final PercentEscaper ESCAPER = new PercentEscaper("-_.~", false);

	public JiraOAuthGetAccessToken(String hostName, String consumerKeyIn, String privateKeyIn,String verifierIn,String tempToken) {
		
		jiraBaseURL = hostName;
		consumerKey = consumerKeyIn;
		privateKey = privateKeyIn;
		verifier=verifierIn;
		initalizeRequestArguments();
	}

	private void initalizeRequestArguments() {
		jiraRequestTokenURL = jiraBaseURL + TEM_REQUEST_TOKEN_PATH;
		authorizationUrl = jiraBaseURL + OAUTH_AUTHORIZE_PATH;
		callback="oob";//need default "oob" if no true callback server URL
		
		try {
			setSigner(OAuthParameterHelper.getOAuthRsaSigner(getPrivateKey()));
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		setNonce();
		setTimestamp();
		try {
			computeSignature();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getAuthroizationUrl() {
		String authrizationUrl="";
		if(!request_token.isEmpty()) {
			authrizationUrl= authorizationUrl+"?"+HTTPRequestConstant.oauth_token+"="+request_token;
		}
		if(callback!="" && "oob".equalsIgnoreCase(callback)) {
			authrizationUrl=authrizationUrl+"&oauth_callback="+callback;
		}
		return authrizationUrl;
	}

	public void computeSignature() throws GeneralSecurityException {
		OAuthSigner signer = this.signer;
		String signatureMethod = this.signatureMethod = signer.getSignatureMethod();
		// oauth_* parameters (except oauth_signature)
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		putParameterIfValueNotNull(parameters, "oauth_callback", callback);
		putParameterIfValueNotNull(parameters, "oauth_consumer_key", consumerKey);
		putParameterIfValueNotNull(parameters, "oauth_nonce", nonce);
		putParameterIfValueNotNull(parameters, "oauth_signature_method", signatureMethod);
		putParameterIfValueNotNull(parameters, "oauth_timestamp", timestamp);
		putParameterIfValueNotNull(parameters, "oauth_token", tempToken);
		putParameterIfValueNotNull(parameters, "oauth_verifier", verifier);
		putParameterIfValueNotNull(parameters, "oauth_version", version);

		// normalize parameters
		StringBuilder parametersBuf = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (first) {
				first = false;
			} else {
				parametersBuf.append('&');
			}
			parametersBuf.append(entry.getKey());
			String value = entry.getValue();
			if (value != null) {
				parametersBuf.append('=').append(value);
			}
		}
		String normalizedParameters = parametersBuf.toString();
		StringBuilder buf = new StringBuilder();
		buf.append(escape(REQUESTMETHOD)).append('&');
		buf.append(escape(jiraRequestTokenURL)).append('&');
		buf.append(escape(normalizedParameters));
		String signatureBaseString = buf.toString();
		signature = signer.computeSignature(signatureBaseString);
	}

	



	public String getAuthorizationHeader() {
		StringBuilder buf = new StringBuilder("OAuth");
		// appendParameter(buf, "realm", realm);
		appendParameter(buf, "oauth_callback", callback);
		appendParameter(buf, "oauth_consumer_key", consumerKey);
		appendParameter(buf, "oauth_nonce", nonce);
		appendParameter(buf, "oauth_signature", signature);
		appendParameter(buf, "oauth_signature_method", signatureMethod);
		appendParameter(buf, "oauth_timestamp", timestamp);
		appendParameter(buf, "oauth_token", tempToken);
		appendParameter(buf, "oauth_verifier", verifier);
		appendParameter(buf, "oauth_version", version);
		// hack: we have to remove the extra ',' at the end
		return buf.substring(0, buf.length() - 1);
	}

	

	public void executeRequest() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("authorization", getAuthorizationHeader());
		reponseString = executeRequest(jiraRequestTokenURL, headerMap);
		if (!reponseString.isEmpty()) {
			parseResponse(reponseString);
		}
	}

	public String executeRequest(String RquestUrl, Map<String, String> headerMap) {
		String resultString = "";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(RquestUrl);

		headerMap.forEach((key, value) -> {
			httpPost.setHeader(key, value);
		});

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();

			System.out.println("响应状态为:" + response.getStatusLine());
			if (responseEntity != null) {
				resultString = EntityUtils.toString(responseEntity);
				System.out.println("响应内容长度为:" + responseEntity.getContentLength());
				System.out.println("响应内容为:" + resultString);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}
	


	private void parseResponse(String reponseString2) {
		Map<String, String> resultMap = parseResponse2Map(reponseString2);
		if (resultMap.containsKey(HTTPRequestConstant.oauth_token)) {
			this.request_token = resultMap.get(HTTPRequestConstant.oauth_token);
		}
		if (resultMap.containsKey(HTTPRequestConstant.oauth_token_secret)) {
			this.oauth_token_secret = resultMap.get(HTTPRequestConstant.oauth_token_secret);
		}
		if (resultMap.containsKey(HTTPRequestConstant.oauth_callback_confirmed)) {
			this.oauth_callback_confirmed = resultMap.get(HTTPRequestConstant.oauth_callback_confirmed);
		}

	}

	public String getOauth_callback_confirmed() {
		return oauth_callback_confirmed;
	}

//	public void setOauth_callback_confirmed(String oauth_callback_confirmed) {
//		this.oauth_callback_confirmed = oauth_callback_confirmed;
//	}

	private Map<String, String> parseResponse2Map(String reponseString) {
		String[] keyValuePairs = reponseString.split("&");
		Map<String, String> responseMap = new HashMap<String, String>();
		for (String subString : keyValuePairs) {
			String[] keyValueStrings = subString.split("=");
			responseMap.put(keyValueStrings[0], keyValueStrings[1]);
		}

		return responseMap;
	}
	
	public String getSignature() {
		return signature;
	}

//	public String getNonce() {
//		return nonce;
//	}

	public void setNonce() {
		nonce = Long.toHexString(Math.abs(RANDOM.nextLong()));
	}

	public void setNonce(String s) {
		nonce = s;
	}

//	public String getTimestamp() {
//		return timestamp;
//	}

	public void setTimestamp(String s) {
		timestamp = s;
	}

	public void setTimestamp() {
		timestamp = Long.toString(System.currentTimeMillis() / 1000);
	}

//	public String getCallback() {
//		return callback;
//	};

	public void setCallback(String callback) {
		this.callback = callback;
	}

//	public OAuthSigner getSigner() {
//		return signer;
//	}

	public void setSigner(OAuthSigner signer) {
		this.signer = signer;
	}

	public String getConsumerkey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

//	public String getAccessToken() {
//		return accessToken;
//	}
//
//	public void setAccessToken(String access_token) {
//		this.accessToken = access_token;
//	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivate_key(String private_key) {
		this.privateKey = private_key;
	}

	public String getSecret() {
		return oauth_token_secret;
	}

	public void setSecret(String secret) {
		this.oauth_token_secret = secret;
	}

	public String getReqeustHome() {
		return jiraRequestTokenURL;
	}

	public void setReqeustHome(String reqeustHome) {
		this.jiraRequestTokenURL = reqeustHome;
	}

	public String getRequest_token() {
		return request_token;
	}

	public void setRequest_token(String request_token) {
		this.request_token = request_token;
	}

	
	private void putParameterIfValueNotNull(TreeMap<String, String> parameters, String key, String value) {
		if (value != null) {
			putParameter(parameters, key, value);
		}
	}

	private void putParameter(TreeMap<String, String> parameters, String key, Object value) {
		parameters.put(escape(key), value == null ? null : escape(value.toString()));
	}
	
	private void appendParameter(StringBuilder buf, String name, String value) {
		if (value != null) {
			buf.append(' ').append(escape(name)).append("=\"").append(escape(value)).append("\",");
		}
	}
	
	/** Returns the escaped form of the given value using OAuth escaping rules. */
	private String escape(String value) {
		return ESCAPER.escape(value);
	}


}
