package fr.inra.maiage.bibliome.alvisir.core.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Payload;

final class AlvisIRTokenStream<T,R> extends TokenStream {
	private final CharTermAttribute attrTermForm = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute attrPositionIncrement = addAttribute(PositionIncrementAttribute.class);
	private final PayloadAttribute attrPayload = addAttribute(PayloadAttribute.class);
	private final int tokenPositionGap;
	private final byte fieldInstance;
	private final AnnotationsSlot tokensSlot;
	private final List<AnnotationsSlot> annotationsSlots;
	
	private int tokensOverload;
	
	private final class AnnotationsSlot {
		private final AlvisIRIndexedTokens<T,R> indexedTokens;
		private final Iterator<T> annotations;
		private T current;
		private int currentStart = Integer.MAX_VALUE;
		private int currentEnd = Integer.MIN_VALUE;
		private Payload payload;
		
		private AnnotationsSlot(AlvisIRIndexedTokens<T,R> indexedTokens) {
			this.indexedTokens = indexedTokens;
			this.annotations = indexedTokens.getTokenInstances();
		}
		
		private void setAttributes(boolean annotation) {
			String text = indexedTokens.getTokenText(current);
			attrTermForm.setLength(0);
			attrTermForm.append(text);
			int posIncr;
			if (annotation) {
				posIncr = 1;
				tokensOverload += 1;
			}
			else {
				posIncr = tokenPositionGap - tokensOverload;
				tokensOverload = 1;
			}
			attrPositionIncrement.setPositionIncrement(posIncr);
			attrPayload.setPayload(payload);
//			System.err.println("text = " + text);
//			System.err.println("posIncr = " + posIncr);
		}

		private boolean next() {
			if (annotations.hasNext()) {
				current = annotations.next();
				AlvisIRTokenFragments<R> tokenFragments = indexedTokens.getTokenFragments(current);
				Collection<R> fragments = tokenFragments.getTokenFragments();
				Map<Integer,Integer> args = indexedTokens.getRelationArguments(current);
				Map<Integer,String> props = indexedTokens.getProperties(current);
				byte[] payload = new byte[17 + 8 * fragments.size() + 8 * args.size() + getPropsSize(props)];
				ByteBuffer buf = ByteBuffer.wrap(payload);
				int id = indexedTokens.getTokenIdentifier(current);
				buf.putInt(id);
				buf.put(fieldInstance);
				buf.putInt(fragments.size());
				boolean first = true;
				for (R frag : fragments) {
					int start = tokenFragments.getStart(frag);
					int end = tokenFragments.getEnd(frag);
					if (first) {
						currentStart = start;
						currentEnd = end;
						first = false;
					}
					buf.putInt(start);
					buf.putInt(end);
//					System.err.println("start-end = " + start + "-" + end);
				}
				buf.putInt(args.size());
				for (Map.Entry<Integer,Integer> e : args.entrySet()) {
					int role = e.getKey();
					int argId = e.getValue();
					buf.putInt(role);
					buf.putInt(argId);
				}
				buf.putInt(props.size());
				for (Map.Entry<Integer,String> e : props.entrySet()) {
					int key = e.getKey();
					String value = e.getValue();
					buf.putInt(key);
					buf.putInt(value.length());
					for (int i = 0; i < value.length(); ++i) {
						buf.putChar(value.charAt(i));
					}
				}
				this.payload = new Payload(payload);
				return true;
			}
			currentStart = Integer.MAX_VALUE;
			return false;
		}

		private int getPropsSize(Map<Integer,String> props) {
			int result = 8 * props.size();
			for (String v : props.values()) {
				result += v.length() * 2;
			}
			return result;
		}
	}

	<F> AlvisIRTokenStream(AlvisIRIndexedFields<F,T,R> indexedFields, F field, int tokenPositionGap, byte fieldInstance) {
		this.tokenPositionGap = tokenPositionGap;
		this.fieldInstance = fieldInstance;
		AlvisIRIndexedTokens<T,R> indexedTokens = indexedFields.getIndexedTokens(field);
		tokensSlot = new AnnotationsSlot(indexedTokens);
		List<AlvisIRIndexedTokens<T,R>> indexedAnnotations = indexedFields.getIndexedAnnotations(field);
		annotationsSlots = new ArrayList<AnnotationsSlot>(indexedAnnotations.size());
		for (AlvisIRIndexedTokens<T,R> ia : indexedAnnotations) {
			AnnotationsSlot as = new AnnotationsSlot(ia);
			as.next();
			annotationsSlots.add(as);
		}
	}

	@Override
	public boolean incrementToken() throws IOException {
		int reach = tokensSlot.currentEnd;
		for (AnnotationsSlot as : annotationsSlots) {
			if (as.currentStart < reach) {
				as.setAttributes(true);
				as.next();
				return true;
			}
		}
		if (tokensSlot.next()) {
			tokensSlot.setAttributes(false);
			return true;
		}
		return false;
	}
}
