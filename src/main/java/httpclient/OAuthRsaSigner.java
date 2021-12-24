package httpclient;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;

import org.apache.commons.codec.binary.Base64;

public final class OAuthRsaSigner implements OAuthSigner {

	  /** Private key. */
	  public PrivateKey privateKey;

	  public String getSignatureMethod() {
	    return "RSA-SHA1";
	  }

	  public String computeSignature(String signatureBaseString) throws GeneralSecurityException {
	    Signature signer = SecurityUtils.getSha1WithRsaSignatureAlgorithm();
	    byte[] data = StringUtils.getBytesUtf8(signatureBaseString);
	    return Base64.encodeBase64String(SecurityUtils.sign(signer, privateKey, data));
	  }
	}

