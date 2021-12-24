package httpclient;

import java.security.GeneralSecurityException;

public interface OAuthSigner {
	

		  /** Returns the signature method. */
		  String getSignatureMethod();

		  /**
		   * Returns the signature computed from the given signature base string.
		   *
		   * @throws GeneralSecurityException general security exception
		   */
		  String computeSignature(String signatureBaseString) throws GeneralSecurityException;

}
