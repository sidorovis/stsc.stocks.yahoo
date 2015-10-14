package stsc.yahoo;

/**
 * {@link String} utils that I believe was written billion times before, but I
 * failed to find them, may be because leak of knowledge. Anyway currently it
 * only two methods that are not very complicated to read and they have tests so
 * it is pretty good.
 */
public final class StringUtils {

	/**
	 * Not real {@link String} comparator. Compare length of the strings only.
	 * 
	 * @return integer value (if left value length is bigger then right value
	 *         length, return difference; otherwise return
	 *         {@link String#compareTo(String)}.
	 */
	public static int comparePatterns(String l, String r) {
		if (l.length() > r.length())
			return l.length() - r.length();

		return l.compareTo(r);
	}

	/**
	 * Create next permutation based on provided. a -> b <br/>
	 * z -> aa <br/>
	 * aa -> ab <br/>
	 * az -> ba <br/>
	 * bz -> ca <br/>
	 * zz -> aaa <br/>
	 * 
	 * @return next permutation.
	 */
	public static String nextPermutation(String f) {
		boolean onlyZ = true;
		for (int i = 0; i < f.length(); ++i)
			if (f.charAt(i) != 'z')
				onlyZ = false;
		if (onlyZ) {
			String s = "";
			for (int i = 0; i <= f.length(); ++i)
				s = s + 'a';
			return s;
		}
		for (int i = f.length() - 1; i > -1; --i)
			if (f.charAt(i) != 'z') {
				if (i == f.length() - 1) {
					char last = (char) (f.charAt(i) + 1);
					return f.substring(0, f.length() - 1) + new Character(last).toString();
				} else {
					char symb = (char) (f.charAt(i) + 1);
					String postfix = "";
					for (int u = i + 1; u < f.length(); ++u) {
						postfix += "a";
					}
					return f.substring(0, i) + new Character(symb).toString() + postfix;
				}
			}
		return f;
	}
}
