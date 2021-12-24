package httpclient;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraOAuthGetTemporaryToken extends JiraOAuthRequest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());  
	public JiraOAuthGetTemporaryToken(String hostName, String consumerKeyIn, String privateKeyIn,String verifier,String tempToken) {
		
		jiraBaseURL = hostName;
		consumerKey = consumerKeyIn;
		privateKey = privateKeyIn;
		
		initalizeRequestArguments();
	}
	
	protected void initalizeRequestArguments() {
		jiraRequestURL = jiraBaseURL + TEM_REQUEST_TOKEN_PATH;
		authorizationUrl = jiraBaseURL + OAUTH_AUTHORIZE_PATH;
		callback="oob";//need default "oob" if no true callback server URL
		requestMethod="POST";
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




}
