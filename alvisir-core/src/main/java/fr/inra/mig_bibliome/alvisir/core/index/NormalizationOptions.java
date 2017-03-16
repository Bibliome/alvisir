package fr.inra.mig_bibliome.alvisir.core.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;

/**
 * Options for text normalization.
 * @author rbossy
 *
 */
public abstract class NormalizationOptions {
	/**
	 * Returns a stream of normalized tokens.
	 * @param source raw token stream
	 * @return a stream of normalized tokens
	 */
	public abstract TokenStream getTokenStream(TokenStream source);
	
	private static final class SingleTokenStream extends TokenStream {
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
		private final String synonym;
		private boolean available = true;

		private SingleTokenStream(String synonym) {
			super();
			this.synonym = synonym;
		}

		@Override
		public boolean incrementToken() {
			if (available) {
				clearAttributes();
				termAtt.setLength(0);
				termAtt.append(synonym);
				available = false;
				return true;
			}
			return false;
		}

		@Override
		public void close() {
		}
	}
	
	/**
	 * Normalizes the specified text.
	 * @param text token to normalize
	 * @return a stream containing a single normalized token
	 */
	public final TokenStream getTokenStream(String text) {
		TokenStream source = new SingleTokenStream(text);
		return getTokenStream(source);
	}
	
	/**
	 * Normalizes the specified tokens.
	 * @param texts tokens to normalize
	 * @return a stream containing one token for each element of the specified token collection
	 */
	public final TokenStream getTokenStream(Collection<String> texts) {
		TokenStream source = new IteratorTokenStream(texts);
		return getTokenStream(source);
	}
	
	/**
	 * Normalizes the specified text.
	 * @param text token to normalize
	 * @return the normalized text
	 */
	public final String normalize(String text) {
		try {
			TokenStream tokenStream = getTokenStream(text);
			tokenStream.incrementToken();
			CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
			String result = attr.toString();
//			System.err.println("result = " + result);
			return result;
		}
		catch (IOException e) {
			// incrementToken() should not throw IOException
			throw new RuntimeException();
		}
	}
	
	private static final class IteratorTokenStream extends TokenStream {
		private final CharTermAttribute attrTermForm = addAttribute(CharTermAttribute.class);
		private final Iterator<String> iterator;
		
		private IteratorTokenStream(Iterator<String> iterator) {
			super();
			this.iterator = iterator;
		}
		
		private IteratorTokenStream(Collection<String> strings) {
			this(strings.iterator());
		}
		
		private IteratorTokenStream(String... strings) {
			this(Arrays.asList(strings));
		}

		@Override
		public boolean incrementToken() throws IOException {
			if (iterator.hasNext()) {
				String token = iterator.next();
				attrTermForm.setLength(0);
				attrTermForm.append(token);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Normalize the specified texts.
	 * @param texts tokens to normalize
	 * @return the normalized tokens
	 */
	public final String[] normalize(String[] texts) {
		try {
			TokenStream source = new IteratorTokenStream(texts);
			TokenStream tokenStream = getTokenStream(source);
			String[] result = new String[texts.length];
			for (int i = 0; i < texts.length; ++i) {
				boolean increment = tokenStream.incrementToken();
				assert increment;
				CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
				result[i] = attr.toString();
			}
			return result;
		}
		catch (IOException e) {
			// incrementToken() should not throw IOException
			throw new RuntimeException();			
		}
	}
	
	/**
	 * Normalize the specified texts.
	 * @param texts tokens to normalize
	 * @return the normalized tokens
	 */
	public final Collection<String> normalize(Collection<String> texts) {
		try {
			TokenStream source = new IteratorTokenStream(texts);
			TokenStream tokenStream = getTokenStream(source);
			Collection<String> result = new ArrayList<String>(texts.size());
			while (tokenStream.incrementToken()) {
				CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
				result.add(attr.toString());
			}
			assert result.size() == texts.size();
			return result;
		}
		catch (IOException e) {
			// incrementToken() should not throw IOException
			throw new RuntimeException();			
		}
	}

	/**
	 * Instance that does not normalize, passes tokens as is.
	 */
	public static final NormalizationOptions NONE = new NormalizationOptions() {
		@Override
		public TokenStream getTokenStream(TokenStream source) {
			return source;
		}
	};
	
	/**
	 * For multiple normalization filters.
	 * @author rbossy
	 *
	 */
	public static abstract class TokenFilterFactory extends NormalizationOptions {
		private final NormalizationOptions next;

		protected TokenFilterFactory(NormalizationOptions next) {
			super();
			this.next = next;
		}

		@Override
		public TokenStream getTokenStream(TokenStream source) {
			TokenStream next = this.next.getTokenStream(source);
			return createTokenFilter(next);
		}
		
		protected abstract TokenFilter createTokenFilter(TokenStream source);
	}
	
	private static final Version VERSION = Version.LUCENE_36;

	/**
	 * Case-folding normalization.
	 * @author rbossy
	 *
	 */
	public static final class CaseFolding extends TokenFilterFactory {
		public CaseFolding(NormalizationOptions next) {
			super(next);
		}

		@Override
		protected TokenFilter createTokenFilter(TokenStream source) {
			return new LowerCaseFilter(VERSION, source);
		}
	}
	
	/**
	 * ASCII-folding normalization.
	 * @author rbossy
	 *
	 */
	public static final class ASCIIFolding extends TokenFilterFactory {
		public ASCIIFolding(NormalizationOptions next) {
			super(next);
		}

		@Override
		protected TokenFilter createTokenFilter(TokenStream source) {
			return new ASCIIFoldingFilter(source);
		}
	}
	
	/**
	 * Stemming.
	 * @author rbossy
	 *
	 */
	public static final class Stemming extends TokenFilterFactory {
		private final SnowballProgram stemmer;

		public Stemming(NormalizationOptions next, SnowballProgram stemmer) {
			super(next);
			this.stemmer = stemmer;
		}

		@Override
		protected TokenFilter createTokenFilter(TokenStream source) {
			return new SnowballFilter(source, stemmer);
		}
	}
	
	// EnglishPossessiveFilter, ElisionFilter, 

	/**
	 * Converts the specified string as a normalization filter.
	 * @param norm normalization name
	 * @param next other normalization
	 * @return
	 */
	public static NormalizationOptions getFilter(String norm, NormalizationOptions next) {
		NormalizationOptions result = next;
		switch (norm) {
			case "case":
			case "lowercase":
			case "lower":
			case "case-folding":
				result = new CaseFolding(result);
				break;
			case "ascii":
			case "ascii-folding":
				result = new ASCIIFolding(result);
				break;
			case "english":
			case "english-stemming":
				result = new Stemming(result, new EnglishStemmer());
				break;
			case "french":
			case "french-stemming":
				result = new Stemming(result, new FrenchStemmer());
				break;
			default:
				return null;
		}
		return result;
	}

	/**
	 * Default normalization: ASCII folding, case folding and english stemmer.
	 */
	public static final NormalizationOptions DEFAULT = new Stemming(new ASCIIFolding(new CaseFolding(NONE)), new EnglishStemmer());
}
