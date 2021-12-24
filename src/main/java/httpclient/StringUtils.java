package httpclient;

import java.io.UnsupportedEncodingException;

public class StringUtils {

	  /**
	   * Line separator to use for this OS, i.e. {@code "\n"} or {@code "\r\n"}.
	   *
	   * @since 1.8
	   */
	  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	  /**
	   * Encodes the given string into a sequence of bytes using the UTF-8 charset, storing the result
	   * into a new byte array.
	   *
	   * @param string the String to encode, may be <code>null</code>
	   * @return encoded bytes, or <code>null</code> if the input string was <code>null</code>
	   * @throws IllegalStateException Thrown when the charset is missing, which should be never
	   *         according the the Java specification.
	   * @see <a href="http://download.oracle.com/javase/1.5.0/docs/api/java/nio/charset/Charset.html"
	   *      >Standard charsets</a>
	   * @see org.apache.commons.codec.binary.StringUtils#getBytesUtf8(String)
	   * @since 1.8
	   */
	  public static byte[] getBytesUtf8(String string) {
	    return org.apache.commons.codec.binary.StringUtils.getBytesUtf8(string);
	  }

	  /**
	   * Constructs a new <code>String</code> by decoding the specified array of bytes using the UTF-8
	   * charset.
	   *
	   * @param bytes The bytes to be decoded into characters
	   * @return A new <code>String</code> decoded from the specified array of bytes using the UTF-8
	   *         charset, or <code>null</code> if the input byte array was <code>null</code>.
	   * @throws IllegalStateException Thrown when a {@link UnsupportedEncodingException} is caught,
	   *         which should never happen since the charset is required.
	   * @see org.apache.commons.codec.binary.StringUtils#newStringUtf8(byte[])
	   * @since 1.8
	   */
	  public static String newStringUtf8(byte[] bytes) {
	    return org.apache.commons.codec.binary.StringUtils.newStringUtf8(bytes);
	  }

	  private StringUtils() {
	  }
	}
