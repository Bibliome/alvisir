package fr.inra.maiage.bibliome.alvisir.core.expand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.inra.maiage.bibliome.alvisir.core.FieldOptions;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.AnyMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.CompositeExpansionExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.NearMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.PhraseMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.PrefixMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.RelationMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.TermMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.index.NormalizationOptions;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAndQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAnyQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRNearQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRNoExpansionQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIROrQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRPhraseQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRPrefixQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNodeVisitor;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRRelationQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRTermListQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRTermQueryNode;
import fr.inra.maiage.bibliome.util.Strings;

/**
 * Objects of this class are responsible for the expansion of AlvisIR queries.
 * @author rbossy
 *
 */
public class BasicQueryNodeExpander extends AlvisIRQueryNodeExpander {
	/**
	 * Creates a query expander.
	 * @param textExpander atomic query (terms and phrases) expander
	 * @param andQueryNodeExpander and-node expander
	 */
	public BasicQueryNodeExpander(TextExpander textExpander) {
		super(textExpander);
	}

	private final class ExpanderVisitor implements AlvisIRQueryNodeVisitor<ExpansionResult,Void,RuntimeException> {
		private final FieldOptions fieldOptions;
		private final ExpanderOptions expanderOptions;
		
		private ExpanderVisitor(FieldOptions fieldOptions, ExpanderOptions expanderOptions) {
			super();
			this.fieldOptions = fieldOptions;
			this.expanderOptions = expanderOptions;
		}

		@Override
		public ExpansionResult visit(AlvisIRTermQueryNode termQueryNode, Void param) {
			List<MatchExplanation> explanations = getTermQueryNodeExplanations(termQueryNode);
			return expandAtom(explanations);
		}

		@Override
		public ExpansionResult visit(AlvisIRPhraseQueryNode phraseQueryNode, Void param) {
			List<MatchExplanation> explanations = getPhraseQueryNodeExplanations(phraseQueryNode);
			return expandAtom(explanations);
		}

		@Override
		public ExpansionResult visit(AlvisIRPrefixQueryNode prefixQueryNode, Void param) {
			List<MatchExplanation> explanations = getPrefixNodeExplanations(prefixQueryNode);
			return expandAtom(explanations);
		}

		@Override
		public ExpansionResult visit(AlvisIRAndQueryNode andQueryNode, Void param) {
			AlvisIRAndQueryNode queryNode = new AlvisIRAndQueryNode();
			ExpansionResult result = new ExpansionResult();
			for (AlvisIRAndQueryNode.Clause clause : andQueryNode.getClauses()) {
				ExpansionResult r = expandQuery(fieldOptions, expanderOptions, clause.getQueryNode());
				result.mergeExplanations(r);
				queryNode.addClause(clause.getOperator(), r.getQueryNode());
			}
			result.setQueryNode(queryNode);
			return result;
		}

		@Override
		public ExpansionResult visit(AlvisIROrQueryNode orQueryNode, Void param) {
			AlvisIROrQueryNode queryNode = new AlvisIROrQueryNode();
			ExpansionResult result = new ExpansionResult();
			for (AlvisIRQueryNode clause : orQueryNode.getClauses()) {
				ExpansionResult r = expandQuery(fieldOptions, expanderOptions, clause);
				queryNode.addClause(r.getQueryNode());
				result.mergeExplanations(r);
			}
			result.setQueryNode(queryNode);
			return result;
		}

		@Override
		public ExpansionResult visit(AlvisIRNearQueryNode nearQueryNode, Void param) {
			ExpansionResult leftExpansion = expandQuery(fieldOptions, expanderOptions, nearQueryNode.getLeft());
			ExpansionResult rightExpansion = expandQuery(fieldOptions, expanderOptions, nearQueryNode.getRight());
			AlvisIRNearQueryNode queryNode = new AlvisIRNearQueryNode(nearQueryNode.getField(), nearQueryNode.getSlop(), leftExpansion.getQueryNode(), rightExpansion.getQueryNode());
			ExpansionResult result = new ExpansionResult();
			result.setQueryNode(queryNode);
			result.mergeExplanations(leftExpansion);
			result.addExplanation(new NearMatchExplanation(nearQueryNode.getField(), nearQueryNode.getSlop(), leftExpansion.getQueryNode(), rightExpansion.getQueryNode()));
			result.mergeExplanations(rightExpansion);
			return result;
		}

		@Override
		public ExpansionResult visit(AlvisIRRelationQueryNode relationQueryNode, Void param) {
			String fieldName = relationQueryNode.getField();
			NormalizationOptions normalizationOptions = expanderOptions.getNormalizationOptions(fieldName);
			String relation = relationQueryNode.getRelation();
			String normalizedRelation = normalizationOptions.normalize(relation);
//			System.err.println("relation = " + relation);
//			System.err.println("normalizedRelation = " + normalizedRelation);
			ExpansionResult leftExpansion = expandQuery(fieldOptions, expanderOptions, relationQueryNode.getLeft());
			ExpansionResult rightExpansion = expandQuery(fieldOptions, expanderOptions, relationQueryNode.getRight());
			AlvisIRRelationQueryNode queryNode = new AlvisIRRelationQueryNode(fieldName, relation, leftExpansion.getQueryNode(), rightExpansion.getQueryNode());
			ExpansionResult result = new ExpansionResult();
			result.setQueryNode(queryNode);
			result.mergeExplanations(leftExpansion);
			result.addExplanation(new RelationMatchExplanation(fieldName, relation, normalizedRelation, leftExpansion.getExplanations(), leftExpansion.getQueryNode(), rightExpansion.getExplanations(), rightExpansion.getQueryNode()));
			result.mergeExplanations(rightExpansion);
			return result;
		}

		@Override
		public ExpansionResult visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Void param) {
			BasicQueryNodeExpander exp = new BasicQueryNodeExpander(NullTextExpander.INSTANCE);
			return exp.expandQuery(fieldOptions, expanderOptions, noExpansionQueryNode.getQueryNode());
		}

		@Override
		public ExpansionResult visit(AlvisIRAnyQueryNode anyQueryNode, Void param) {
			return new ExpansionResult(anyQueryNode, new AnyMatchExplanation(""));
		}

		@Override
		public ExpansionResult visit(AlvisIRTermListQueryNode termListQueryNode, Void param) throws RuntimeException {
			return termListQueryNode.toAndQueryNode().accept(this, param);
		}

		private ExpansionResult expandAtom(List<MatchExplanation> explanations) {
			Collection<AlvisIRQueryNode> clauseNodes = new ArrayList<AlvisIRQueryNode>();
			for (MatchExplanation ex : explanations) {
				clauseNodes.add(ex.getQueryNode());
			}
			AlvisIRQueryNode queryNode = createQueryNode(clauseNodes);
			MatchExplanation explanation = createExplanation(explanations);
			return new ExpansionResult(queryNode, explanation);
		}

		private void searchExpansions(List<MatchExplanation> explanations, String fieldName, String text) {
			try {
				TextExpansionResult textExpansionResult = textExpander.searchExpansion(text);
				if (textExpansionResult.any()) {
					do {
						String type = textExpansionResult.getType();
						ExplanationFactory factory = textExpander.getExplanationFactory(type);
						MatchExplanation explanation = factory.getExplanation(expanderOptions, textExpander, fieldName, textExpansionResult);
						explanations.add(explanation);
					} while (textExpansionResult.next());
				}
			}
			catch (IOException|ExpanderException e) {
				throw new RuntimeException(e);
			}
		}
		
		private List<MatchExplanation> getTermQueryNodeExplanations(AlvisIRTermQueryNode termQueryNode) {
			List<MatchExplanation> result = new ArrayList<MatchExplanation>();
			String fieldName = termQueryNode.getField();
			String text = termQueryNode.getText();
			NormalizationOptions normalizationOptions = expanderOptions.getNormalizationOptions(fieldName);
			String normalizedText = normalizationOptions.normalize(text);
			searchExpansions(result, fieldName, normalizedText);
			if (result.isEmpty()) {
				result.add(new TermMatchExplanation(termQueryNode, normalizedText));			
			}
			return result;
		}
		
		private List<MatchExplanation> getPhraseQueryNodeExplanations(AlvisIRPhraseQueryNode phraseQueryNode) {
			List<MatchExplanation> result = new ArrayList<MatchExplanation>();
			String fieldName = phraseQueryNode.getField();
			List<String> textsList = phraseQueryNode.getTexts();
			String[] texts = textsList.toArray(new String[textsList.size()]);
			NormalizationOptions normalizationOptions = expanderOptions.getNormalizationOptions(fieldName);
			String[] normalizedTexts = normalizationOptions.normalize(texts);
			String normalizedText = Strings.join(normalizedTexts, ' ');
			searchExpansions(result, fieldName, normalizedText);
			if (result.isEmpty()) {
				result.add(new PhraseMatchExplanation(phraseQueryNode, normalizedTexts));
			}
			return result;
		}
		
		private List<MatchExplanation> getPrefixNodeExplanations(AlvisIRPrefixQueryNode prefixQueryNode) {
			String prefix = prefixQueryNode.getPrefix();
			String fieldName = prefixQueryNode.getField();
			NormalizationOptions normalizationOptions = expanderOptions.getNormalizationOptions(fieldName);
			String normalizedPrefix = normalizationOptions.normalize(prefix);
			MatchExplanation raw = new PrefixMatchExplanation(prefixQueryNode, normalizedPrefix);
			return Collections.singletonList(raw);
		}
	}
	
	private static AlvisIRQueryNode createQueryNode(Collection<AlvisIRQueryNode> clauses) {
		assert !clauses.isEmpty();
		if (clauses.size() == 1) {
			return clauses.iterator().next();
		}
		AlvisIROrQueryNode result = new AlvisIROrQueryNode();
		for (AlvisIRQueryNode qn : clauses) {
			result.addClause(qn);
		}
		return result;
	}
	
	private static MatchExplanation createExplanation(List<MatchExplanation> explanations) {
		assert !explanations.isEmpty();
		MatchExplanation first = explanations.iterator().next();
		if (explanations.size() == 1)
			return first;
		return new CompositeExpansionExplanation(first.getFieldName(), explanations);
	}

	/**
	 * Expand the specified query.
	 * @param fieldOptions field options
	 * @param expanderOptions expander options
	 * @param originalQuery query to expand
	 * @return the result of the expansion
	 */
	@Override
	public ExpansionResult expandQuery(FieldOptions fieldOptions, ExpanderOptions expanderOptions, AlvisIRQueryNode originalQuery) {
		ExpanderVisitor visitor = new ExpanderVisitor(fieldOptions, expanderOptions);
		return originalQuery.accept(visitor, null);
	}
}
