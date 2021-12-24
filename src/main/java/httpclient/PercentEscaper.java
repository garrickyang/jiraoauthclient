package httpclient;


public class PercentEscaper {
	private final boolean plusForSpace;
	private final boolean[] safeOctets;
	private static final int DEST_PAD = 32;
	public static final String SAFECHARS_URLENCODER = "-_.*";

	  /**
	   * A string of characters that do not need to be encoded when used in URI path segments, as
	   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
	   * other parts of the URI.
	   */
	  public static final String SAFEPATHCHARS_URLENCODER = "-_.!~*'()@:$&,;=";

	  /**
	   * Contains the save characters plus all reserved characters. This happens to be the safe path
	   * characters plus those characters which are reserved for URI segments, namely '+', '/', and
	   * '?'.
	   */
	  public static final String SAFE_PLUS_RESERVED_CHARS_URLENCODER = SAFEPATHCHARS_URLENCODER + "+/?";

	  /**
	   * A string of characters that do not need to be encoded when used in URI user info part, as
	   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
	   * other parts of the URI.
	   *
	   * @since 1.15
	   */
	  public static final String SAFEUSERINFOCHARS_URLENCODER = "-_.!~*'():$&,;=";

	  /**
	   * A string of characters that do not need to be encoded when used in URI query strings, as
	   * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
	   * other parts of the URI.
	   */
	  public static final String SAFEQUERYSTRINGCHARS_URLENCODER = "-_.!~*'()@:$,;/?:";

	  // In some uri escapers spaces are escaped to '+'
	  private static final char[] URI_ESCAPED_SPACE = {'+'};

	  private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();


	public PercentEscaper(String safeChars, boolean plusForSpace) {
    // Avoid any misunderstandings about the behavior of this escaper
    if (safeChars.matches(".*[0-9A-Za-z].*")) {
      throw new IllegalArgumentException(
          "Alphanumeric characters are always 'safe' and should not be " + "explicitly specified");
    }
    // Avoid ambiguous parameters. Safe characters are never modified so if
    // space is a safe character then setting plusForSpace is meaningless.
    if (plusForSpace && safeChars.contains(" ")) {
      throw new IllegalArgumentException(
          "plusForSpace cannot be specified when space is a 'safe' character");
    }
    if (safeChars.contains("%")) {
      throw new IllegalArgumentException("The '%' character cannot be specified as 'safe'");
    }
    this.plusForSpace = plusForSpace;
    safeOctets = createSafeOctets(safeChars);
  }
	
	private static boolean[] createSafeOctets(String safeChars) {
	    int maxChar = 'z';
	    char[] safeCharArray = safeChars.toCharArray();
	    for (char c : safeCharArray) {
	      maxChar = Math.max(c, maxChar);
	    }
	    boolean[] octets = new boolean[maxChar + 1];
	    for (int c = '0'; c <= '9'; c++) {
	      octets[c] = true;
	    }
	    for (int c = 'A'; c <= 'Z'; c++) {
	      octets[c] = true;
	    }
	    for (int c = 'a'; c <= 'z'; c++) {
	      octets[c] = true;
	    }
	    for (char c : safeCharArray) {
	      octets[c] = true;
	    }
	    return octets;
	  }
	
	 public String escape(String s) {
		    int slen = s.length();
		    for (int index = 0; index < slen; index++) {
		      char c = s.charAt(index);
		      if (c >= safeOctets.length || !safeOctets[c]) {
		        return escapeSlow(s, index);
		      }
		    }
		    return s;
		  }
	 
	 protected final String escapeSlow(String s, int index) {
		    int end = s.length();

		    // Get a destination buffer and setup some loop variables.
		    char[] dest = Platform.charBufferFromThreadLocal();
		    int destIndex = 0;
		    int unescapedChunkStart = 0;

		    while (index < end) {
		      int cp = codePointAt(s, index, end);
		      if (cp < 0) {
		        throw new IllegalArgumentException("Trailing high surrogate at end of input");
		      }
		      // It is possible for this to return null because nextEscapeIndex() may
		      // (for performance reasons) yield some false positives but it must never
		      // give false negatives.
		      char[] escaped = escape(cp);
		      int nextIndex = index + (Character.isSupplementaryCodePoint(cp) ? 2 : 1);
		      if (escaped != null) {
		        int charsSkipped = index - unescapedChunkStart;

		        // This is the size needed to add the replacement, not the full
		        // size needed by the string. We only regrow when we absolutely must.
		        int sizeNeeded = destIndex + charsSkipped + escaped.length;
		        if (dest.length < sizeNeeded) {
		          int destLength = sizeNeeded + end - index + DEST_PAD;
		          dest = growBuffer(dest, destIndex, destLength);
		        }
		        // If we have skipped any characters, we need to copy them now.
		        if (charsSkipped > 0) {
		          s.getChars(unescapedChunkStart, index, dest, destIndex);
		          destIndex += charsSkipped;
		        }
		        if (escaped.length > 0) {
		          System.arraycopy(escaped, 0, dest, destIndex, escaped.length);
		          destIndex += escaped.length;
		        }
		        // If we dealt with an escaped character, reset the unescaped range.
		        unescapedChunkStart = nextIndex;
		      }
		      index = nextEscapeIndex(s, nextIndex, end);
		    }

		    // Process trailing unescaped characters - no need to account for escaped
		    // length or padding the allocation.
		    int charsSkipped = end - unescapedChunkStart;
		    if (charsSkipped > 0) {
		      int endIndex = destIndex + charsSkipped;
		      if (dest.length < endIndex) {
		        dest = growBuffer(dest, destIndex, endIndex);
		      }
		      s.getChars(unescapedChunkStart, end, dest, destIndex);
		      destIndex = endIndex;
		    }
		    return new String(dest, 0, destIndex);
		  }
	 
	 protected static int codePointAt(CharSequence seq, int index, int end) {
		    if (index < end) {
		      char c1 = seq.charAt(index++);
		      if (c1 < Character.MIN_HIGH_SURROGATE || c1 > Character.MAX_LOW_SURROGATE) {
		        // Fast path (first test is probably all we need to do)
		        return c1;
		      } else if (c1 <= Character.MAX_HIGH_SURROGATE) {
		        // If the high surrogate was the last character, return its inverse
		        if (index == end) {
		          return -c1;
		        }
		        // Otherwise look for the low surrogate following it
		        char c2 = seq.charAt(index);
		        if (Character.isLowSurrogate(c2)) {
		          return Character.toCodePoint(c1, c2);
		        }
		        throw new IllegalArgumentException(
		            "Expected low surrogate but got char '" + c2 + "' with value " + (int) c2 + " at index "
		                + index);
		      } else {
		        throw new IllegalArgumentException(
		            "Unexpected low surrogate character '" + c1 + "' with value " + (int) c1 + " at index "
		                + (index - 1));
		      }
		    }
		    throw new IndexOutOfBoundsException("Index exceeds specified range");
		  }
	 protected char[] escape(int cp) {
		    // We should never get negative values here but if we do it will throw an
		    // IndexOutOfBoundsException, so at least it will get spotted.
		    if (cp < safeOctets.length && safeOctets[cp]) {
		      return null;
		    } else if (cp == ' ' && plusForSpace) {
		      return URI_ESCAPED_SPACE;
		    } else if (cp <= 0x7F) {
		      // Single byte UTF-8 characters
		      // Start with "%--" and fill in the blanks
		      char[] dest = new char[3];
		      dest[0] = '%';
		      dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
		      dest[1] = UPPER_HEX_DIGITS[cp >>> 4];
		      return dest;
		    } else if (cp <= 0x7ff) {
		      // Two byte UTF-8 characters [cp >= 0x80 && cp <= 0x7ff]
		      // Start with "%--%--" and fill in the blanks
		      char[] dest = new char[6];
		      dest[0] = '%';
		      dest[3] = '%';
		      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[2] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[1] = UPPER_HEX_DIGITS[0xC | cp];
		      return dest;
		    } else if (cp <= 0xffff) {
		      // Three byte UTF-8 characters [cp >= 0x800 && cp <= 0xffff]
		      // Start with "%E-%--%--" and fill in the blanks
		      char[] dest = new char[9];
		      dest[0] = '%';
		      dest[1] = 'E';
		      dest[3] = '%';
		      dest[6] = '%';
		      dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[7] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[2] = UPPER_HEX_DIGITS[cp];
		      return dest;
		    } else if (cp <= 0x10ffff) {
		      char[] dest = new char[12];
		      // Four byte UTF-8 characters [cp >= 0xffff && cp <= 0x10ffff]
		      // Start with "%F-%--%--%--" and fill in the blanks
		      dest[0] = '%';
		      dest[1] = 'F';
		      dest[3] = '%';
		      dest[6] = '%';
		      dest[9] = '%';
		      dest[11] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[10] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[8] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[7] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[5] = UPPER_HEX_DIGITS[cp & 0xF];
		      cp >>>= 4;
		      dest[4] = UPPER_HEX_DIGITS[0x8 | cp & 0x3];
		      cp >>>= 2;
		      dest[2] = UPPER_HEX_DIGITS[cp & 0x7];
		      return dest;
		    } else {
		      // If this ever happens it is due to bug in UnicodeEscaper, not bad input.
		      throw new IllegalArgumentException("Invalid unicode character value " + cp);
		    }
		  }
	 
	  private static char[] growBuffer(char[] dest, int index, int size) {
		    char[] copy = new char[size];
		    if (index > 0) {
		      System.arraycopy(dest, 0, copy, 0, index);
		    }
		    return copy;
		  }
	  
	  protected int nextEscapeIndex(CharSequence csq, int index, int end) {
		    for (; index < end; index++) {
		      char c = csq.charAt(index);
		      if (c >= safeOctets.length || !safeOctets[c]) {
		        break;
		      }
		    }
		    return index;
		  }
}
final class Platform {
	  private Platform() {
	  }

	  /** Returns a thread-local 1024-char array. */
	  static char[] charBufferFromThreadLocal() {
	    return DEST_TL.get();
	  }

	  /**
	   * A thread-local destination buffer to keep us from creating new buffers. The starting size is
	   * 1024 characters. If we grow past this we don't put it back in the threadlocal, we just keep
	   * going and grow as needed.
	   */
	  private static final ThreadLocal<char[]> DEST_TL = new ThreadLocal<char[]>() {
	    @Override
	    protected char[] initialValue() {
	      return new char[1024];
	    }
	  };
	}
