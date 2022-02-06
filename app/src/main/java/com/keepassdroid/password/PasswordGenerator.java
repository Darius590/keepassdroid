/*
 * Copyright 2010 Tolga Onbay, Brian Pellin.
 *     
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.password;

import java.security.SecureRandom;
import android.content.Context;
import com.android.keepass.R;
import com.keepassdroid.app.App;

import org.json.JSONArray;
import org.json.JSONException;

public class PasswordGenerator {
	private static final String UPPERCASE_CHARS	= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWERCASE_CHARS 	= "abcdefghijklmnopqrstuvwxyz";
	private static final String DIGIT_CHARS 	= "0123456789";
	private static final String MINUS_CHAR	 	= "-";
	private static final String UNDERLINE_CHAR 	= "_";
	private static final String SPACE_CHAR 		= " ";
	private static final String SPECIAL_CHARS 	= "!\"#$%&'*+,./:;=?@\\^`";
	private static final String BRACKET_CHARS 	= "[]{}()<>";

	private JSONArray words;
	private Context cxt;
	
	public PasswordGenerator(Context cxt) {
		this.cxt = cxt;
		this.words = App.getWords();
	}
	
	public String generatePassword(int length, boolean upperCase, boolean lowerCase, boolean digits, boolean minus, boolean underline, boolean space, boolean specials, boolean brackets) throws IllegalArgumentException{
		// Desired password length is 0 or less
		if (length <= 0) {
			throw new IllegalArgumentException(cxt.getString(R.string.error_wrong_length));
		}
		
		// No option has been checked
		if (!upperCase && !lowerCase && !digits && !minus && !underline && !space && !specials && !brackets) {
			throw new IllegalArgumentException(cxt.getString(R.string.error_pass_gen_type));
		}
		
		String characterSet = getCharacterSet(upperCase, lowerCase, digits, minus, underline, space, specials, brackets);
		
		int size = characterSet.length();
		
		StringBuffer buffer = new StringBuffer();

		SecureRandom random = new SecureRandom(); // use more secure variant of Random!
		if (size > 0) {
			for (int i = 0; i < length; i++) {
				char c = characterSet.charAt((char) random.nextInt(size));
				buffer.append(c);
			}
		}
		return buffer.toString();
	}
	
	public String getCharacterSet(boolean upperCase, boolean lowerCase, boolean digits, boolean minus, boolean underline, boolean space, boolean specials, boolean brackets) {
		StringBuffer charSet = new StringBuffer();
		
		if (upperCase) {
			charSet.append(UPPERCASE_CHARS);
		}
		
		if (lowerCase) {
			charSet.append(LOWERCASE_CHARS);
		}
		
		if (digits) {
			charSet.append(DIGIT_CHARS);
		}
		
		if (minus) {
			charSet.append(MINUS_CHAR);
		}
		
		if (underline) {
			charSet.append(UNDERLINE_CHAR);
		}
		
		if (space) {
			charSet.append(SPACE_CHAR);
		}
		
		if (specials) {
			charSet.append(SPECIAL_CHARS);
		}
		
		if (brackets) {
			charSet.append(BRACKET_CHARS);
		}
		
		return charSet.toString();
	}

	public int calculateWordEntropy(int words, boolean camelCase, boolean digits, boolean specials) {
		double permutations = this.words.length();
		if(digits) { permutations = permutations * Math.pow(DIGIT_CHARS.length(), words); }
		if(specials) { permutations = permutations * Math.pow(SPECIAL_CHARS.length(), words); }

		double entropy = Math.log10(Math.pow(permutations,words)) / Math.log10(2);
		return (int) Math.round(entropy);
	}

	public String generateWordPassword(int words, boolean camelCase, boolean digits, boolean specials) throws IllegalArgumentException{
		// Number of words is 0 or less
		if (words <= 0) {
			throw new IllegalArgumentException(cxt.getString(R.string.error_wrong_length));
		}
		SecureRandom random = new SecureRandom(); // use more secure variant of Random!

		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < words; i++) {
			String word = getRandomWord();

			StringBuilder sb = new StringBuilder(word);

			if(camelCase) {
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			}

			if(digits)
			{
				sb.append(DIGIT_CHARS.charAt((char) random.nextInt(DIGIT_CHARS.length())));
			}

			if(specials)
			{
				sb.append(SPECIAL_CHARS.charAt((char) random.nextInt(SPECIAL_CHARS.length())));
			}

			buffer.append(sb.toString());
		}

		return buffer.toString();
	}

	private String getRandomWord() {
		//get a random word from the json
		SecureRandom random = new SecureRandom(); // use more secure variant of Random!
		String randomWord = null;

		try {
			randomWord = this.words.getString(random.nextInt(this.words.length()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return randomWord;
	}
}
