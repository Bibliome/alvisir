package fr.inra.maiage.bibliome.alvisir.core.expand.explanation;

import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAnyQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;

public class AnyMatchExplanation extends MatchExplanation {
	public AnyMatchExplanation(String fieldName) {
		super(fieldName);
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MatchExplanationPayloadDecoder();
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		return AlvisIRAnyQueryNode.INSTANCE;
	}
}
