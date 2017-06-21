package fr.inra.mig_bibliome.alvisir.core.query.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bibliome.util.Strings;

import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAnyQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRPrefixQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRTermQueryNode;

public class QueryParserUtils {
	static String unescapeText(String term) {
		StringBuilder sb = new StringBuilder();
		boolean esc = false;
		for (int i = 0; i < term.length(); ++i) {
			char c = term.charAt(i);
			if (esc) {
				esc = false;
				sb.append(c);
				continue;
			}
			if (c == '\\') {
				esc = true;
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
//	static Query getTextQuery(String field, String text) {
//		char last = text.charAt(text.length() - 1);
//		if (last == '*') {
//			Term term = new Term(field, text.substring(0, text.length() - 1));
//			return new PrefixQuery(term);
//		}
//		Term term = new Term(field, text);
//		return new TermQuery(term);
//	}
	
	static AlvisIRQueryNode getTextQueryNode(String field, String text) {
		if (text.equals("*")) {
			return AlvisIRAnyQueryNode.INSTANCE;
		}
		char last = text.charAt(text.length() - 1);
		if (last == '*') {
			return new AlvisIRPrefixQueryNode(field, text.substring(0, text.length() - 1));
		}
		return new AlvisIRTermQueryNode(field, text);
	}
	
	private static final Collection<String> RESERVED_WORDS = new HashSet<String>();
	private static final Collection<Character> SPECIAL_CHARACTERS = new HashSet<Character>();
	
	static {
		RESERVED_WORDS.addAll(Arrays.asList("and", "or", "not"));
		SPECIAL_CHARACTERS.addAll(Arrays.asList('=', '(', ')', '[', ']', '~', '"'));
	}
	
	public static String quotePhrase(String phrase) {
		List<String> terms = Strings.split(phrase, ' ', 0);
		List<String> quotedTerms = new ArrayList<String>(terms.size());
		for (String term : terms) {
			quotedTerms.add(quoteTerm(term));
		}
		return Strings.join(quotedTerms, ' ');
	}
	
	public static String quoteTerm(String term) {
		if (isReservedWord(term)) {
			return "\\" + term;
		}
		StringBuilder sb = null;
		final int n = term.length();
		for (int i = 0; i < n; ++i) {
			final char c = term.charAt(i);
			if (isSpecialCharacter(c)) {
				if (sb == null) {
					sb = new StringBuilder(term.substring(0, i));
				}
				sb.append('\\');
			}
			if (sb != null) {
				sb.append(c);
			}
		}
		if (sb == null) {
			return term;
		}
		return sb.toString();
	}

	public static boolean isReservedWord(String term) {
		return RESERVED_WORDS.contains(term.toLowerCase());
	}
	
	public static boolean isSpecialCharacter(char c) {
		return Character.isWhitespace(c) || SPECIAL_CHARACTERS.contains(c);
	}
	
	public static String normalizeRelationName(String name) {
		StringBuilder sb = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (Character.isWhitespace(c) || c == '_' || c == '-') {
				continue;
			}
			c = Character.toLowerCase(c);
			sb.append(c);
		}
		return sb.toString();
	}
}
