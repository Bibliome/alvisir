package fr.inra.mig_bibliome.alvisir.core.query;

import org.apache.lucene.search.Query;

/**
 * Base class for converter from query node to lucene query, with the field name given as parameter.
 * @author rbossy
 *
 * @param <Q>
 */
abstract class AbstractAlvisIRQueryNodeQueryConverter<Q extends Query,X extends Exception> implements AlvisIRQueryNodeVisitor<Q,String,X>  {
	private final int tokenPositionGap;
	
	/**
	 * Creates a converter from a query node to lucene query.
	 * @param tokenPositionGap
	 */
	protected AbstractAlvisIRQueryNodeQueryConverter(int tokenPositionGap) {
		super();
		this.tokenPositionGap = tokenPositionGap;
	}

	/**
	 * Converts the slop into an index slop.
	 * @param slop
	 * @return
	 */
	protected int getRealSlop(int slop) {
		return (slop * tokenPositionGap) + (tokenPositionGap / 2);
	}
}
