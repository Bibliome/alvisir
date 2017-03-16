package fr.inra.mig_bibliome.alvisir.core.query;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.inra.mig_bibliome.alvisir.core.AlvisIRConstants;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Clause;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Operator;

/**
 * Converts a query node into a DOM element, with the owner document given as parameter.
 * @author rbossy
 *
 */
public class AlvisIRQueryNodeDOMConverter implements AlvisIRQueryNodeVisitor<Element,Document,RuntimeException> {
	private final String defaultField;

	/**
	 * Creates a converter from query node to DOM element.
	 * @param defaultField name of the default field
	 */
	public AlvisIRQueryNodeDOMConverter(String defaultField) {
		super();
		this.defaultField = defaultField;
	}

	private void setFieldAttribute(Element elt, String field) {
		if (!field.equals(defaultField)) {
			elt.setAttribute(AlvisIRConstants.XML_QUERY_FIELD, field);
		}
	}
	
	@Override
	public Element visit(AlvisIRTermQueryNode termQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_TERM);
		setFieldAttribute(result, termQueryNode.getField());
		result.setTextContent(termQueryNode.getText());
		return result;
	}

	@Override
	public Element visit(AlvisIRPhraseQueryNode phraseQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_PHRASE);
		setFieldAttribute(result, phraseQueryNode.getField());
		for (String text : phraseQueryNode.getTexts()) {
			Element elt = param.createElement(AlvisIRConstants.XML_QUERY_TERM);
			elt.setTextContent(text);
			result.appendChild(elt);
		}
		return result;
	}

	@Override
	public Element visit(AlvisIRPrefixQueryNode prefixQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_PREFIX);
		setFieldAttribute(result, prefixQueryNode.getField());
		result.setTextContent(prefixQueryNode.getPrefix());
		return result;
	}

	@Override
	public Element visit(AlvisIRAndQueryNode andQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_AND);
		for (Clause clause : andQueryNode.getClauses()) {
			AlvisIRQueryNode qn = clause.getQueryNode();
			Element elt = qn.accept(this, param);
			if (clause.getOperator() == Operator.BUT) {
				elt.setAttribute(AlvisIRConstants.XML_QUERY_OPERATOR, AlvisIRConstants.XML_QUERY_OPERATOR_BUT);
			}
			result.appendChild(elt);
		}
		return result;
	}

	@Override
	public Element visit(AlvisIROrQueryNode orQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_OR);
		for (AlvisIRQueryNode clause : orQueryNode.getClauses()) {
			Element elt = clause.accept(this, param);
			result.appendChild(elt);
		}
		return result;
	}

	@Override
	public Element visit(AlvisIRNearQueryNode nearQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_NEAR);
		setFieldAttribute(result, nearQueryNode.getField());
		result.setAttribute(AlvisIRConstants.XML_QUERY_SLOP, Integer.toString(nearQueryNode.getSlop()));
		Element left = nearQueryNode.getLeft().accept(this, param);
		Element right = nearQueryNode.getRight().accept(this, param);
		result.appendChild(left);
		result.appendChild(right);
		return result;
	}

	@Override
	public Element visit(AlvisIRRelationQueryNode relationQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_RELATION);
		setFieldAttribute(result, relationQueryNode.getField());
		result.setAttribute(AlvisIRConstants.XML_QUERY_RELATION, relationQueryNode.getRelation());
		Element left = relationQueryNode.getLeft().accept(this, param);
		Element right = relationQueryNode.getRight().accept(this, param);
		result.appendChild(left);
		result.appendChild(right);
		return result;
	}

	@Override
	public Element visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Document param) {
		Element result = param.createElement(AlvisIRConstants.XML_QUERY_NO_EXPANSION);
		Element child = noExpansionQueryNode.getQueryNode().accept(this, param);
		result.appendChild(child);
		return result;
	}

	@Override
	public Element visit(AlvisIRAnyQueryNode noExpansionQueryNode, Document param) {
		return param.createElement(AlvisIRConstants.XML_QUERY_ANY);
	}
}
