package httpclient;

import static org.junit.Assert.*;

import org.junit.Test;

public class JiraOAuthRestClientTest {

	@Test
	public void test() {
		String consumer_key = "OauthKey";
		String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKl2JcD3GoEe3uHackeeciaddvReMHC41kiUQLrJZxsAAsjFTncljZKSACtujkJhVEBc3aUYyTz0noP0Vpgy8DneAXSDc6EXMunFH9Dzr9ne61x+ZimagkHfXBLECzbb+Zu1SokDYDxjb6Oit6T08hCZrigq1DA1VHFxn25cdiGDAgMBAAECgYASszb3VE1Yck+mHLT/sjxmhnnZ/Yra5Yq/95wtAIygLiAgh6VhAIPe6L2cDVclfBgQAi9zSIjscRIM/amQog5gJk4c2wqFdLv6NhnxBpkdhf3s1ByS30JQHg8e1EeS9DoMg/L1DEWIlQec51AEszYHLcHSgxIcP2gXh+FgUZUwAQJBAN5qK7Bg/ymVAj+PnKP27XX175USkchFy2GVKsvY25sdFT13je+AAVMOYgwplIShgVHLzWBmi73YqmMZxftTDGMCQQDDDPpXV7ZYC0BeVi6g68BDvUE6jR2h6TpxWAHm+piR9WtaZDhF6I3yWvUqfKXYtrSG/8vkUVkV6fl2/E44utBhAkAKBK5DG5tivBuF0Wo02IKJtbI8/MEkTECE/LsYw4Pg0MaMJj52c0WcACHaemT+NGgmzw9JMFVLD99c52RLlcoRAkBOwMWvUFXqVJinvkpTZPybHSXiGyoUvpN/QhZ6iUHi5OF0fLSP3Wa6rOkCP5PC3Xoka9GKHSJIC9FSrmpy01LhAkBHKtkElHQA6G1viODWyQzKLhDGZp9YiSmZlW2DmIufgWqpTGHJxTRPl87N8hHnRoqeJ6g/wUGyWIbKSTqymA2Y";
		String jiraBaseUrl = "https://jira-gcs-hz.atlassian.net/rest/api/2/issue/JG-84";
		
		JiraOAuthRestClient jiraOAuthGetAccessToken = new JiraOAuthRestClient(jiraBaseUrl,
				consumer_key, private_key,"bDsCdO","D1Cf9ORcmMCr1kiuq9W3C4OKHJa0Qc38");
		
		jiraOAuthGetAccessToken.executeRequest();
		
		//assertTrue(!jiraOAuthGetAccessToken.getRequest_token().equalsIgnoreCase(""));
		//String urlString=jiraOAuthGetTemporaryToken.getAuthroizationUrl();
		System.out.println(jiraOAuthGetAccessToken.getReponseString());
		assertTrue(jiraOAuthGetAccessToken.getReponseString().contains("expand"));
	}

}
