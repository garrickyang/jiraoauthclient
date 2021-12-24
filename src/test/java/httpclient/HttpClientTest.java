package httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.GeneralSecurityException;

import org.junit.Ignore;
import org.junit.Test;

public class HttpClientTest {
	static String tempTokenString="";

	@Test
	public void getTempTokenTest() throws GeneralSecurityException {

		String consumer_key = "OauthKey";
		String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKl2JcD3GoEe3uHackeeciaddvReMHC41kiUQLrJZxsAAsjFTncljZKSACtujkJhVEBc3aUYyTz0noP0Vpgy8DneAXSDc6EXMunFH9Dzr9ne61x+ZimagkHfXBLECzbb+Zu1SokDYDxjb6Oit6T08hCZrigq1DA1VHFxn25cdiGDAgMBAAECgYASszb3VE1Yck+mHLT/sjxmhnnZ/Yra5Yq/95wtAIygLiAgh6VhAIPe6L2cDVclfBgQAi9zSIjscRIM/amQog5gJk4c2wqFdLv6NhnxBpkdhf3s1ByS30JQHg8e1EeS9DoMg/L1DEWIlQec51AEszYHLcHSgxIcP2gXh+FgUZUwAQJBAN5qK7Bg/ymVAj+PnKP27XX175USkchFy2GVKsvY25sdFT13je+AAVMOYgwplIShgVHLzWBmi73YqmMZxftTDGMCQQDDDPpXV7ZYC0BeVi6g68BDvUE6jR2h6TpxWAHm+piR9WtaZDhF6I3yWvUqfKXYtrSG/8vkUVkV6fl2/E44utBhAkAKBK5DG5tivBuF0Wo02IKJtbI8/MEkTECE/LsYw4Pg0MaMJj52c0WcACHaemT+NGgmzw9JMFVLD99c52RLlcoRAkBOwMWvUFXqVJinvkpTZPybHSXiGyoUvpN/QhZ6iUHi5OF0fLSP3Wa6rOkCP5PC3Xoka9GKHSJIC9FSrmpy01LhAkBHKtkElHQA6G1viODWyQzKLhDGZp9YiSmZlW2DmIufgWqpTGHJxTRPl87N8hHnRoqeJ6g/wUGyWIbKSTqymA2Y";
		String jiraBaseUrl = "https://jira-gcs-hz.atlassian.net";

		JiraOAuthGetTemporaryToken jiraOAuthGetTemporaryToken = new JiraOAuthGetTemporaryToken(jiraBaseUrl,
				consumer_key, private_key,null,null);

		jiraOAuthGetTemporaryToken.executeRequest();
		
		assertTrue(!jiraOAuthGetTemporaryToken.getRequest_token().equalsIgnoreCase(""));
		String urlString=jiraOAuthGetTemporaryToken.getAuthroizationUrl();
		System.out.println(urlString);
		tempTokenString=jiraOAuthGetTemporaryToken.getRequest_token();
		assertTrue(urlString.contains(HTTPRequestConstant.oauth_token));
		

	}
	//@Ignore
	@Test
	public void getAccessTokenTest() throws GeneralSecurityException {
		
		String consumer_key = "OauthKey";
		String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKl2JcD3GoEe3uHackeeciaddvReMHC41kiUQLrJZxsAAsjFTncljZKSACtujkJhVEBc3aUYyTz0noP0Vpgy8DneAXSDc6EXMunFH9Dzr9ne61x+ZimagkHfXBLECzbb+Zu1SokDYDxjb6Oit6T08hCZrigq1DA1VHFxn25cdiGDAgMBAAECgYASszb3VE1Yck+mHLT/sjxmhnnZ/Yra5Yq/95wtAIygLiAgh6VhAIPe6L2cDVclfBgQAi9zSIjscRIM/amQog5gJk4c2wqFdLv6NhnxBpkdhf3s1ByS30JQHg8e1EeS9DoMg/L1DEWIlQec51AEszYHLcHSgxIcP2gXh+FgUZUwAQJBAN5qK7Bg/ymVAj+PnKP27XX175USkchFy2GVKsvY25sdFT13je+AAVMOYgwplIShgVHLzWBmi73YqmMZxftTDGMCQQDDDPpXV7ZYC0BeVi6g68BDvUE6jR2h6TpxWAHm+piR9WtaZDhF6I3yWvUqfKXYtrSG/8vkUVkV6fl2/E44utBhAkAKBK5DG5tivBuF0Wo02IKJtbI8/MEkTECE/LsYw4Pg0MaMJj52c0WcACHaemT+NGgmzw9JMFVLD99c52RLlcoRAkBOwMWvUFXqVJinvkpTZPybHSXiGyoUvpN/QhZ6iUHi5OF0fLSP3Wa6rOkCP5PC3Xoka9GKHSJIC9FSrmpy01LhAkBHKtkElHQA6G1viODWyQzKLhDGZp9YiSmZlW2DmIufgWqpTGHJxTRPl87N8hHnRoqeJ6g/wUGyWIbKSTqymA2Y";
		String jiraBaseUrl = "https://jira-gcs-hz.atlassian.net";
		
		JiraOAuthGetAccessToken jiraOAuthGetAccessToken = new JiraOAuthGetAccessToken(jiraBaseUrl,
				consumer_key, private_key,"QpaStC","gmygg9Gdw9je5ZapBKXrEwukSesyLnjz");
		
		jiraOAuthGetAccessToken.executeRequest();
		
		assertTrue(!jiraOAuthGetAccessToken.getRequest_token().equalsIgnoreCase(""));
		//String urlString=jiraOAuthGetTemporaryToken.getAuthroizationUrl();
		System.out.println(jiraOAuthGetAccessToken.getRequest_token());
		System.out.println(jiraOAuthGetAccessToken.getReponseString());
		//assertTrue(urlString.contains(HTTPRequestConstant.oauth_token));
		
		
	}

	
	@Test
	public void encodingRequestHeader() throws GeneralSecurityException {
		String consumer_key = "OauthKey";
		String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKl2JcD3GoEe3uHackeeciaddvReMHC41kiUQLrJZxsAAsjFTncljZKSACtujkJhVEBc3aUYyTz0noP0Vpgy8DneAXSDc6EXMunFH9Dzr9ne61x+ZimagkHfXBLECzbb+Zu1SokDYDxjb6Oit6T08hCZrigq1DA1VHFxn25cdiGDAgMBAAECgYASszb3VE1Yck+mHLT/sjxmhnnZ/Yra5Yq/95wtAIygLiAgh6VhAIPe6L2cDVclfBgQAi9zSIjscRIM/amQog5gJk4c2wqFdLv6NhnxBpkdhf3s1ByS30JQHg8e1EeS9DoMg/L1DEWIlQec51AEszYHLcHSgxIcP2gXh+FgUZUwAQJBAN5qK7Bg/ymVAj+PnKP27XX175USkchFy2GVKsvY25sdFT13je+AAVMOYgwplIShgVHLzWBmi73YqmMZxftTDGMCQQDDDPpXV7ZYC0BeVi6g68BDvUE6jR2h6TpxWAHm+piR9WtaZDhF6I3yWvUqfKXYtrSG/8vkUVkV6fl2/E44utBhAkAKBK5DG5tivBuF0Wo02IKJtbI8/MEkTECE/LsYw4Pg0MaMJj52c0WcACHaemT+NGgmzw9JMFVLD99c52RLlcoRAkBOwMWvUFXqVJinvkpTZPybHSXiGyoUvpN/QhZ6iUHi5OF0fLSP3Wa6rOkCP5PC3Xoka9GKHSJIC9FSrmpy01LhAkBHKtkElHQA6G1viODWyQzKLhDGZp9YiSmZlW2DmIufgWqpTGHJxTRPl87N8hHnRoqeJ6g/wUGyWIbKSTqymA2Y";
		String host_name = "https://jira-gcs-hz.atlassian.net";
		JiraOAuthRequest jiraOAuthGetTemporaryToken = new JiraOAuthGetTemporaryToken(host_name, consumer_key,
				private_key,null,null);

		jiraOAuthGetTemporaryToken
				.setSigner(OAuthParameterHelper.getOAuthRsaSigner(jiraOAuthGetTemporaryToken.getPrivateKey()));
		jiraOAuthGetTemporaryToken.setCallback("oob");
		jiraOAuthGetTemporaryToken.setNonce("14a812ac4204e817");
		jiraOAuthGetTemporaryToken.setTimestamp("1639813086");
		jiraOAuthGetTemporaryToken.computeSignature();
		String signatureString = "k3Maz7Jcq5a3vI3mQ8f2zktDxWCE+Zpw2BYHDVX7Ao3NkSANt9cst5xIVCUFGtRCF/NBbYe/hOkrt57AxZX8TDMN+wHGnm3aHoA59kugPNlymh4G0DOmiwiXSnwHsAwk7t+cPAXEaxDEYb/r55+tBR5rCQ1WmOAOq0+le/iMj8U=";
		assertEquals(signatureString, jiraOAuthGetTemporaryToken.getSignature());

		assertEquals(
				"OAuth oauth_callback=\"oob\", oauth_consumer_key=\"OauthKey\", oauth_nonce=\"14a812ac4204e817\", oauth_signature=\"k3Maz7Jcq5a3vI3mQ8f2zktDxWCE%2BZpw2BYHDVX7Ao3NkSANt9cst5xIVCUFGtRCF%2FNBbYe%2FhOkrt57AxZX8TDMN%2BwHGnm3aHoA59kugPNlymh4G0DOmiwiXSnwHsAwk7t%2BcPAXEaxDEYb%2Fr55%2BtBR5rCQ1WmOAOq0%2Ble%2FiMj8U%3D\", oauth_signature_method=\"RSA-SHA1\", oauth_timestamp=\"1639813086\"",
				jiraOAuthGetTemporaryToken.getAuthorizationHeader());
	}
	
	

}
