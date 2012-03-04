package nl.tudelft.in4325.a1.normalization;

import info.bliki.wiki.model.WikiModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import nl.tudelft.in4325.a1.Constants;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class AdvancedNormalizer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AdvancedNormalizer.class);

	// Third-party library to remove Wikipedia formatting
	private WikiModel wikiModel = new WikiModel(
			"http://www.mywiki.com/wiki/${image}",
			"http://www.mywiki.com/wiki/${title}");

	// List of stop words not to process
	private List<String> stopWords = readStopWords();

	// Porter2 stemmer for English
	private SnowballStemmer stemmer = new englishStemmer();

	// Symbols allowed in tokens, apart from letters, digits and white spaces
	private String allowedSymbols = "%-_";

	String curlyBraces = "{}";
	char leftBrace = curlyBraces.charAt(0);
	char rightBrace = curlyBraces.charAt(1);

	/**
	 * Removes noise from tokens
	 * 
	 * @param text
	 *            token to process
	 * @return result
	 */
	public String removeNonAlphaNumericSymbols(String text) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)
					|| allowedSymbols.indexOf(c) > -1) {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Determines validity of a token by comparison with a list of stop words
	 * 
	 * @param token
	 *            current token
	 * @return true if the token is valid, false otherwise
	 */
	public boolean isTokenValid(String token) {

		if (token.length() == 0) {
			return false;
		}

		for (String stopWord : stopWords) {
			if (token.startsWith(stopWord)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Reads corpus-dependent stop words from a file (one word at one line) and
	 * saves them to list
	 * 
	 * @return list of stop words
	 */
	private List<String> readStopWords() {
		stopWords = new LinkedList<String>();

		try {
			FileReader fr = new FileReader(Constants.STOPWORDS_FILE);
			BufferedReader in = new BufferedReader(fr);
			String strLine;
			while ((strLine = in.readLine()) != null) {
				if (!strLine.startsWith("#") && !strLine.isEmpty()) {
					stopWords.add(strLine);
				}
			}
			in.close();
			fr.close();
		} catch (Exception e) {
			LOGGER.error("Error: ", e);
			System.exit(1);
		}

		return stopWords;
	}

	/**
	 * Removes HTML formatting from text
	 * 
	 * @param html
	 *            HTML-formatted String
	 * @return plain text String
	 */
	public String removeHtmlTags(String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * Additional wiki markup removal procedures
	 * 
	 * @param text
	 *            text with markup
	 * @return plain text
	 */
	public String removeMarkup(String text) {

		StringBuffer sb = new StringBuffer();
		int braces = 0;

		// removes any content between two or more consequent pairs of curly
		// braces and the braces themselves
		for (int i = 1; i < text.length(); i++) {
			char current = text.charAt(i);
			char previous = text.charAt(i - 1);
			if (current == leftBrace && previous == leftBrace) {
				braces++;
			} else if (current == rightBrace && previous == rightBrace) {
				braces--;
			}
			if (braces == 0 && curlyBraces.indexOf(current) == -1) {
				sb.append(current);
			}
		}

		return sb.toString();
	}

	public SnowballStemmer getStemmer() {
		return stemmer;
	}

	public WikiModel getWikiModel() {
		return wikiModel;
	}
}