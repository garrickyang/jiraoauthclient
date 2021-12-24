package httpclient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraOAuthRestClient extends JiraOAuthRequest{
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	public JiraOAuthRestClient(String hostName, String consumerKeyIn, String privateKeyIn,String verifierIn,String tempTokenIn) {
		
		jiraBaseURL = hostName;
		consumerKey = consumerKeyIn;
		privateKey = privateKeyIn;
		verifier=verifierIn;
		tempToken=tempTokenIn;
		initalizeRequestArguments();
	}

	@Override
	protected void initalizeRequestArguments() {
		jiraRequestURL = jiraBaseURL;
		requestMethod="GET";
		try {
			setSigner(OAuthParameterHelper.getOAuthRsaSigner(getPrivateKey()));
		} catch (NoSuchAlgorithmException e1) {
			log.info("error",e1); 
		} catch (InvalidKeySpecException e1) {
			log.info("error",e1); 
		}
		
		setNonce();
		setTimestamp();
		try {
			computeSignature();
		} catch (GeneralSecurityException e) {
			log.info("error",e); 
		}
	}
	
	@Override
	public void executeRequest() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("authorization", getAuthorizationHeader());
		reponseString = executeRequest(jiraRequestURL, headerMap);
		if (!reponseString.isEmpty()) {
			parseResponse(reponseString);
		}
	}

	@Override
	public String executeRequest(String RquestUrl, Map<String, String> headerMap) {
		String resultString = "";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		//HttpPost httpPost = new HttpPost(RquestUrl);
		HttpGet httpGet = new HttpGet(RquestUrl);

		headerMap.forEach((key, value) -> {
			httpGet.setHeader(key, value);
		});

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			HttpEntity responseEntity = response.getEntity();
	
			log.info("request:" + httpGet.getURI());
			log.info("request:" + httpGet.getFirstHeader("Authorization"));
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
	

	@Override
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

}
