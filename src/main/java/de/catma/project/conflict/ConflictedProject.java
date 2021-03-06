package de.catma.project.conflict;

import java.util.Collection;
import java.util.List;

import de.catma.document.source.SourceDocument;
import de.catma.project.ProjectReference;
import de.catma.tag.TagLibrary;
import de.catma.tag.TagsetDefinition;

public interface ConflictedProject {
	public List<TagsetConflict> getTagsetConflicts() throws Exception;
	public List<CollectionConflict> getCollectionConflicts() throws Exception;
	public Collection<TagsetDefinition> getTagsets() throws Exception;
	public Collection<SourceDocument> getDocuments() throws Exception;
	public void resolveCollectionConflict(
		List<CollectionConflict> conflictedCollections, 
		TagLibrary tagLibrary) throws Exception;
	ProjectReference getProjectReference();
	public Collection<DeletedResourceConflict> resolveRootConflicts() throws Exception;
	public void resolveTagsetConflicts(List<TagsetConflict> tagsetConflicts) throws Exception;
	public void resolveDeletedResourceConflicts(Collection<DeletedResourceConflict> deletedReourceConflicts) throws Exception;
	
	
}
