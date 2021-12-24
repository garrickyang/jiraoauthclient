package httpclient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraOAuthRequest {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());  


	protected static final String OAUTH_AUTHORIZE_PATH = "/plugins/servlet/oauth/authorize";
	protected static final String TEM_REQUEST_TOKEN_PATH = "/plugins/servlet/oauth/request-token";
	protected static final String ACCESS_TOKEN_PATH = "/plugins/servlet/oauth/access-token";
	protected String consumerKey;//set in jira->product->link
	protected String privateKey;//ssl command created private key
	protected String request_token;//get tem token from oauth_token in response
	protected String oauth_token_secret;//get value from oauth_token_secret in response
	protected OAuthSigner signer;//used to signature request string using RSA
	protected String callback;//oauth_callback in request header
	protected String nonce;//oauth_nonce in request header
	protected String timestamp;//oauth_timestamp in request header
	protected String signatureMethod;//oauth_signature_method in request header
	protected String tempToken;//oauth_token in request header. used to call service API
	protected String verifier;//oauth_verifier in request header. used in access token request.
	protected String version;// oauth_version in request header, usually 1.0 or null.
	protected String signature;//oauth_signature in request header
	
	protected String reponseString;
	protected String oauth_callback_confirmed;
	protected String jiraBaseURL;
	protected String requestMethod;//used to in signature string.
	protected String jiraRequestURL;
	protected String authorizationUrl;
	/** Secure random number generator to sign requests. */
	protected static final SecureRandom RANDOM = new SecureRandom();
	protected static final PercentEscaper ESCAPER = new PercentEscaper("-_.~", false);

	public JiraOAuthRequest() {
		super();
	}

	protected void initalizeRequestArguments() {
		
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
		buf.append(escape(requestMethod)).append('&');
		buf.append(escape(jiraRequestURL)).append('&');
		buf.append(escape(normalizedParameters));
		String signatureBaseString = buf.toString();
		signature = signer.computeSignature(signatureBaseString);
		log.debug("signatureBaseString:",signatureBaseString);
		log.debug("signature:",signature);
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
		log.debug("authorizationHeader:",buf.substring(0, buf.length() - 1));
		return buf.substring(0, buf.length() - 1);
		
	}

	public void executeRequest() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("authorization", getAuthorizationHeader());
		reponseString = executeRequest(jiraRequestURL, headerMap);
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
	
			log.info("request:" + httpPost.getURI());
			log.info("request:" + httpPost.getFirstHeader("Authorization"));
			log.info("response status:" + response.getStatusLine());
			if (responseEntity != null) {
				resultString = EntityUtils.toString(responseEntity);
				log.info("response lenth:" + responseEntity.getContentLength());
				log.info("response content:" + resultString);
			}
		} catch (ClientProtocolException e) {
			log.info("error£º" + e);
		} catch (ParseException e) {
			log.info("error£º" + e);
		} catch (IOException e) {
			log.info("error£º" + e);
		} finally {
			try {
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				log.info("error£º" + e);
			}
		}
		return resultString;
	}

	protected void parseResponse(String reponseString2) {
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

	protected Map<String, String> parseResponse2Map(String reponseString) {
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

	public void setNonce() {
		nonce = Long.toHexString(Math.abs(RANDOM.nextLong()));
	}

	public void setNonce(String s) {
		nonce = s;
	}

	public void setTimestamp(String s) {
		timestamp = s;
	}

	public void setTimestamp() {
		timestamp = Long.toString(System.currentTimeMillis() / 1000);
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public void setSigner(OAuthSigner signer) {
		this.signer = signer;
	}

	public String getConsumerkey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String protected_key) {
		this.privateKey = protected_key;
	}

	public String getSecret() {
		return oauth_token_secret;
	}

	public void setSecret(String secret) {
		this.oauth_token_secret = secret;
	}

	public String getReqeustHome() {
		return jiraRequestURL;
	}

	public void setReqeustHome(String reqeustHome) {
		this.jiraRequestURL = reqeustHome;
	}

	public String getRequest_token() {
		return request_token;
	}

	public void setRequest_token(String request_token) {
		this.request_token = request_token;
	}
	
	public String getReponseString(){
		return reponseString;
	}

	protected void putParameterIfValueNotNull(TreeMap<String, String> parameters, String key, String value) {
		if (value != null) {
			putParameter(parameters, key, value);
		}
	}

	protected void putParameter(TreeMap<String, String> parameters, String key, Object value) {
		parameters.put(escape(key), value == null ? null : escape(value.toString()));
	}

	protected void appendParameter(StringBuilder buf, String name, String value) {
		if (value != null) {
			buf.append(' ').append(escape(name)).append("=\"").append(escape(value)).append("\",");
		}
	}

	/** Returns the escaped form of the given value using OAuth escaping rules. */
	protected String escape(String value) {
		return ESCAPER.escape(value);
	}

}