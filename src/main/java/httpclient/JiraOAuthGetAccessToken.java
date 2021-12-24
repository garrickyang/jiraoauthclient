package httpclient;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraOAuthGetAccessToken  extends JiraOAuthRequest{
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());  
	public JiraOAuthGetAccessToken(String hostName, String consumerKeyIn, String privateKeyIn,String verifierIn,String tempTokenIn) {
		
		jiraBaseURL = hostName;
		consumerKey = consumerKeyIn;
		privateKey = privateKeyIn;
		
		verifier=verifierIn;
		tempToken=tempTokenIn;
		
		initalizeRequestArguments();
	}

	protected void initalizeRequestArguments() {
		jiraRequestURL = jiraBaseURL + ACCESS_TOKEN_PATH;
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

}
