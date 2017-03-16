package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collection;

import org.apache.lucene.search.spans.Spans;

import fr.inra.mig_bibliome.alvisir.core.snippet.DocumentSnippet;

/**
 * Decodes payloads containing the field instance and fragment offsets.
 * @author rbossy
 *
 */
public abstract class PayloadDecoder {
	private DocumentSnippet docSnippet;
	private String fieldName;
	
	/**
	 * Returns the current document snippet.
	 * @return the current document snippet.
	 */
	public DocumentSnippet getDocSnippet() {
		return docSnippet;
	}

	/**
	 * Returns the current field name.
	 * @return the current field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Sets the current field name.
	 * @param fieldName
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Sets the current document snippet.
	 * @param docSnippet
	 */
	public void setDocSnippet(DocumentSnippet docSnippet) {
		this.docSnippet = docSnippet;
	}

	/**
	 * Decodes the specified payloads.
	 * @param payloads
	 */
	public void decode(Collection<byte[]> payloads) {
		handleMatch();
		for (byte[] payload : payloads) {
			decode(payload);
		}
	}

	/**
	 * Decodes the payloads available in the current state of the specified spans object.
	 * @param spans
	 * @throws IOException
	 */
	public void decode(Spans spans) throws IOException {
		if (spans.isPayloadAvailable()) {
			Collection<byte[]> payloads = spans.getPayload();
			decode(payloads);
		}
	}
	
	/**
	 * Decodes the payloads available in the specified spans object, iterating through the specified document snippets.
	 * @param docSnippets
	 * @param spans
	 * @throws IOException
	 */
	public void decode(DocumentSnippet[] docSnippets, Spans spans) throws IOException {
		if (docSnippets.length == 0) {
			return;
		}
		int i = 0;
		while (spans.next()) {
			int docId = docSnippets[i].getDocId();
			while (spans.doc() != docId) {
				if (spans.doc() < docId) {
					if (!spans.skipTo(docId)) {
						return;
					}
				}
				else {
					++i;
					if (i == docSnippets.length) {
						return;
					}
					docId = docSnippets[i].getDocId();
				}
			}
			setDocSnippet(docSnippets[i]);
			decode(spans);
		}
	}
	
	public void decode(byte[] payload) {
		ByteBuffer buf = ByteBuffer.wrap(payload);
		int id = buf.getInt();
		int fieldInstance = buf.get();
		handleToken(id, fieldInstance);
		int nFrags = buf.getInt();
		for (int i = 0; i < nFrags; ++i) {
			int start = buf.getInt();
			int end = buf.getInt();
			handleFragment(fieldInstance, start, end);
		}
		int nArgs = buf.getInt();
		for (int i = 0; i < nArgs; ++i) {
			int role = buf.getInt();
			int arg = buf.getInt();
			handleArgument(role, arg);
		}
		if (buf.hasRemaining()) {
			int nProps = buf.getInt();
			for (int i = 0; i < nProps; ++i) {
				int key = buf.getInt();
				int valueLength = buf.getInt();
				CharBuffer value = CharBuffer.allocate(valueLength);
				for (int j = 0; j < valueLength; ++j) {
					char c = buf.getChar();
					value.put(c);
				}
				handleProperty(key, value.rewind().toString());
			}
		}
	}

	protected abstract void handleProperty(int key, String value);

	protected abstract void handleArgument(int role, int argId);
	
	protected abstract void handleMatch();

	/**
	 * Do something with the decoded field instance.
	 * @param id 
	 * @param fieldInstance field instance decoded from a payload.
	 */
	protected abstract void handleToken(int id, int fieldInstance);
	
	/**
	 * Do something with a decoded fragment.
	 * @param fieldInstance field instance.
	 * @param start start offset of the decoded fragment.
	 * @param end end offset of the decoded fragment.
	 */
	protected abstract void handleFragment(int fieldInstance, int start, int end);
}
