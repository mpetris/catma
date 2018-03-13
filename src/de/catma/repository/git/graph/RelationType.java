package de.catma.repository.git.graph;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {
	hasRevision,
	hasDocument,
	isPartOf,
	isAdjacentTo,
	hasPosition,
	;
	
	public static String rt(RelationType relationType) {
		return relationType.name();
	}
}
