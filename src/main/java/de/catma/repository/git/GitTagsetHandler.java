package de.catma.repository.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff.StageState;
import org.eclipse.jgit.transport.CredentialsProvider;

import de.catma.project.TagsetConflict;
import de.catma.project.conflict.TagConflict;
import de.catma.rbac.RBACPermission;
import de.catma.rbac.RBACRole;
import de.catma.repository.git.interfaces.ILocalGitRepositoryManager;
import de.catma.repository.git.interfaces.IRemoteGitManagerRestricted;
import de.catma.repository.git.serialization.SerializationHelper;
import de.catma.repository.git.serialization.model_wrappers.GitTagDefinition;
import de.catma.repository.git.serialization.models.GitTagsetHeader;
import de.catma.tag.PropertyDefinition;
import de.catma.tag.TagDefinition;
import de.catma.tag.TagsetDefinition;
import de.catma.user.User;

public class GitTagsetHandler {
	private final ILocalGitRepositoryManager localGitRepositoryManager;
	private final IRemoteGitManagerRestricted remoteGitServerManager;
	private final CredentialsProvider credentialsProvider;

	private static final String TAGSET_REPOSITORY_NAME_FORMAT = "%s_tagset";

	public static String getTagsetRepositoryName(String tagsetId) {
		return String.format(TAGSET_REPOSITORY_NAME_FORMAT, tagsetId);
	}

	public GitTagsetHandler(ILocalGitRepositoryManager localGitRepositoryManager,
			IRemoteGitManagerRestricted remoteGitServerManager, CredentialsProvider credentialsProvider) {
		this.localGitRepositoryManager = localGitRepositoryManager;
		this.remoteGitServerManager = remoteGitServerManager;
		this.credentialsProvider = credentialsProvider;
	}

	/**
	 * Creates a new tagset.
	 * <p>
	 * NB: You probably don't want to call this method directly (it doesn't create the submodule in the project root
	 * repo). Instead call the <code>createTagset</code> method of the {@link GitProjectManager} class.
	 *
	 * @param projectId the ID of the project within which the tagset must be created
	 * @param tagsetId the ID of the tagset to create. If none is provided, a new ID will be generated.
	 * @param name the name of the new tagset
	 * @param description the description of the new tagset
	 * @return the new revisionhash
	 * @throws IOException if an error occurs while creating the tagset
	 */
	public String create(@Nonnull String projectId,
						 @Nullable String tagsetId,
						 @Nonnull String name,
						 @Nullable String description
	) throws IOException {

		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			// create the remote tagset repository
			String tagsetRepoName = GitTagsetHandler.getTagsetRepositoryName(tagsetId);

			CreateRepositoryResponse createRepositoryResponse =
					this.remoteGitServerManager.createRepository(tagsetRepoName, tagsetRepoName, projectId);

			// clone the repository locally
			localGitRepoManager.clone(
					projectId,
					createRepositoryResponse.repositoryHttpUrl,
					null, // destination path is created by the repo manager 
					credentialsProvider
			);

			// write header.json into the local repo
			File targetHeaderFile = new File(localGitRepoManager.getRepositoryWorkTree(), "header.json");

			GitTagsetHeader header = new GitTagsetHeader(name, description, new TreeSet<>());
			String serializedHeader = new SerializationHelper<GitTagsetHeader>().serialize(header);

			String revisionHash = localGitRepoManager.addAndCommit(
					targetHeaderFile,
					serializedHeader.getBytes(StandardCharsets.UTF_8),
					String.format("Added Tagset %1$s with ID %2$s", name, tagsetId),
					remoteGitServerManager.getUsername(),
					remoteGitServerManager.getEmail()
			);
			
			return revisionHash;
		}
	}

	private ArrayList<TagDefinition> openTagDefinitions(File parentDirectory) throws IOException {
		ArrayList<TagDefinition> tagDefinitions = new ArrayList<>();

		List<String> contents = Arrays.asList(parentDirectory.list());

		for(String item : contents){
			File target = new File(parentDirectory, item);

			// if it is a directory, recurse into it adding results to the current tagDefinitions list
			if(target.isDirectory() && !target.getName().equalsIgnoreCase(".git")){
				tagDefinitions.addAll(this.openTagDefinitions(target));
				continue;
			}

			// if item is propertydefs.json, read it into a TagDefinition
			if(target.isFile() && target.getName().equalsIgnoreCase("propertydefs.json")){
				String serialized = FileUtils.readFileToString(target, StandardCharsets.UTF_8);
				GitTagDefinition gitTagDefinition = new SerializationHelper<GitTagDefinition>()
						.deserialize(
								serialized,
								GitTagDefinition.class
						);

				tagDefinitions.add(gitTagDefinition.getTagDefinition());
			}
		}

		return tagDefinitions;
	}

	public void checkout(String projectId, String tagsetId, String branch, boolean createBranch) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			localGitRepoManager.checkout(branch, createBranch);
		}		
	}
	
	public MergeResult synchronizeBranchWithRemoteMaster(
			String branch, String projectId, String tagsetId, boolean canPushToRemote) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			localGitRepoManager.checkout(Constants.MASTER, false);
			
			localGitRepoManager.fetch(credentialsProvider);
			
			MergeResult mergeWithOriginMasterResult = 
				localGitRepoManager.merge(Constants.DEFAULT_REMOTE_NAME + "/" + Constants.MASTER);
			
			if (!mergeWithOriginMasterResult.getMergeStatus().isSuccessful()) {
				throw new IllegalStateException(
					String.format(
						"Merge of origin/master into master "
						+ "of Tagset with ID %1$s "
						+ "of Project with ID %2$s "
						+ "failed. "
						+ "Merge Status is %3$s",
					tagsetId,
					projectId,
					mergeWithOriginMasterResult.getMergeStatus().toString()));
			}
			
			MergeResult mergeResult = localGitRepoManager.merge(branch);
			
			if (mergeResult.getMergeStatus().isSuccessful()) {
				
				if (canPushToRemote) {
					localGitRepoManager.push(credentialsProvider);
				}
				
				localGitRepoManager.checkout(branch, false);
				
				localGitRepoManager.rebase(Constants.MASTER);
			}
			
			return mergeResult;
		}		
	}
	
	public TagsetDefinition getTagset(@Nonnull String projectId, @Nonnull String tagsetId) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			localGitRepoManager.open(projectId, projectRootRepositoryName);

			String tagsetSubmoduleName = String.format(
					"%s/%s", GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME, tagsetId
			);

			File tagsetHeaderFile = new File(
				localGitRepoManager.getRepositoryWorkTree(),
				String.format("%s/header.json", tagsetSubmoduleName, tagsetId)
			);

			String serialized = FileUtils.readFileToString(tagsetHeaderFile, StandardCharsets.UTF_8);
			GitTagsetHeader gitTagsetHeader = new SerializationHelper<GitTagsetHeader>()
					.deserialize(
							serialized,
							GitTagsetHeader.class
					);

			TagsetDefinition tagsetdefinition = new TagsetDefinition(
				tagsetId, gitTagsetHeader.getName(), null, gitTagsetHeader.getDeletedDefinitions()
			);
			String tagsetDefinitionRevisionHash = localGitRepoManager.getSubmoduleHeadRevisionHash(tagsetSubmoduleName);
			tagsetdefinition.setRevisionHash(tagsetDefinitionRevisionHash);

			ArrayList<TagDefinition> tagDefinitions = this.openTagDefinitions(tagsetHeaderFile.getParentFile());

			for(TagDefinition tagdefinition : tagDefinitions){
				tagsetdefinition.addTagDefinition(tagdefinition);
			}

			return tagsetdefinition;
		}
	}

	/**
	 * Creates a tag definition within the tagset identified by <code>tagsetId</code>.
	 *
	 * @param projectId the ID of the project that contains the tagset within which the tag definition should be created
	 * @param tagsetId the ID of the tagset within which to create the tag definition
	 * @param tagDefinition a {@link TagDefinition} object
	 * @return the tagset definition UUID
	 * @throws IOException if an error occurs while creating the tag definition
	 */
	public String createOrUpdateTagDefinition(
			String projectId,
			String tagsetId,
			TagDefinition tagDefinition,
			String commitMsg
	) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String targetPropertyDefinitionsFileRelativePath =
				(StringUtils.isEmpty(tagDefinition.getParentUuid()) ? "" : (tagDefinition.getParentUuid() + "/"))
				+ tagDefinition.getUuid()
				+ "/propertydefs.json";

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			File targetPropertyDefinitionsFileAbsolutePath = Paths.get(
					localGitRepoManager.getRepositoryWorkTree().toString(),
					targetPropertyDefinitionsFileRelativePath
			).toFile();

			GitTagDefinition gitTagDefinition = new GitTagDefinition(tagDefinition);
			String serializedGitTagDefinition = 
					new SerializationHelper<GitTagDefinition>().serialize(gitTagDefinition);

			String tagsetRevision = localGitRepoManager.addAndCommit(
				targetPropertyDefinitionsFileAbsolutePath, 
				serializedGitTagDefinition.getBytes(StandardCharsets.UTF_8), 
				commitMsg,
				remoteGitServerManager.getUsername(),
				remoteGitServerManager.getEmail());
			
			return tagsetRevision;
		}
	}
	
	public void createOrUpdateTagDefinition(
			String projectId,
			String tagsetId,
			TagDefinition tagDefinition) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String targetPropertyDefinitionsFileRelativePath =
				(StringUtils.isEmpty(tagDefinition.getParentUuid()) ? "" : (tagDefinition.getParentUuid() + "/"))
				+ tagDefinition.getUuid()
				+ "/propertydefs.json";

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			File targetPropertyDefinitionsFileAbsolutePath = Paths.get(
					localGitRepoManager.getRepositoryWorkTree().toString(),
					targetPropertyDefinitionsFileRelativePath
			).toFile();

			GitTagDefinition gitTagDefinition = new GitTagDefinition(tagDefinition);
			String serializedGitTagDefinition = 
					new SerializationHelper<GitTagDefinition>().serialize(gitTagDefinition);

			localGitRepoManager.add(
				targetPropertyDefinitionsFileAbsolutePath.getAbsoluteFile(), 
				serializedGitTagDefinition.getBytes(StandardCharsets.UTF_8));
		}
	}	

	public String removeTagDefinition(String projectId, TagsetDefinition tagsetDefinition, TagDefinition tagDefinition) 
			throws IOException {
		
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			
			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetDefinition.getUuid();

			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			// write header.json with deletion journal
			File targetHeaderFile = new File(localGitRepoManager.getRepositoryWorkTree(), "header.json");
			
			GitTagsetHeader header = 
					new GitTagsetHeader(
							tagsetDefinition.getName(), 
							"", //TODO: description
							new TreeSet<>(tagsetDefinition.getDeletedDefinitions()));
			String serializedHeader = new SerializationHelper<GitTagsetHeader>().serialize(header);
			
			localGitRepoManager.add(
					targetHeaderFile,
					serializedHeader.getBytes(StandardCharsets.UTF_8));
			
			String targetTagDefinitionsFolderRelativePath = 
				(StringUtils.isEmpty(tagDefinition.getParentUuid()) ? "" : (tagDefinition.getParentUuid()+"/"))
				+ tagDefinition.getUuid();

			File targetTagDefinitionsFolderAbsolutePath = Paths.get(
					localGitRepoManager.getRepositoryWorkTree().toString(),
					targetTagDefinitionsFolderRelativePath
			).toFile();

			String tagsetRevision = localGitRepoManager.removeAndCommit(
					targetTagDefinitionsFolderAbsolutePath, 
					String.format(
						"Removing Tag %1$s with ID %2$s", 
						tagDefinition.getName(), 
						tagDefinition.getUuid()),
					remoteGitServerManager.getUsername(),
					remoteGitServerManager.getEmail());
				
			return tagsetRevision;
		}
	}

	public String removePropertyDefinition(
			String projectId, TagsetDefinition tagsetDefinition,
			TagDefinition tagDefinition, PropertyDefinition propertyDefinition) 
		throws IOException {
		
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			
			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetDefinition.getUuid();

			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			// write header.json with deletion journal
			File targetHeaderFile = new File(localGitRepoManager.getRepositoryWorkTree(), "header.json");
			
			GitTagsetHeader header = 
					new GitTagsetHeader(
							tagsetDefinition.getName(), 
							"", //TODO: description
							new TreeSet<>(tagsetDefinition.getDeletedDefinitions()));
			String serializedHeader = new SerializationHelper<GitTagsetHeader>().serialize(header);
			
			localGitRepoManager.add(
					targetHeaderFile,
					serializedHeader.getBytes(StandardCharsets.UTF_8));
			
			String targetPropertyDefinitionsFileRelativePath =
					(StringUtils.isEmpty(tagDefinition.getParentUuid()) ? "" : (tagDefinition.getParentUuid() + "/"))
					+ tagDefinition.getUuid()
					+ "/propertydefs.json";
			
			File targetPropertyDefinitionsFileAbsolutePath = Paths.get(
					localGitRepoManager.getRepositoryWorkTree().toString(),
					targetPropertyDefinitionsFileRelativePath
			).toFile();

			GitTagDefinition gitTagDefinition = new GitTagDefinition(tagDefinition);
			String serializedGitTagDefinition = 
					new SerializationHelper<GitTagDefinition>().serialize(gitTagDefinition);

			String tagsetRevision = localGitRepoManager.addAndCommit(
				targetPropertyDefinitionsFileAbsolutePath, 
				serializedGitTagDefinition.getBytes(StandardCharsets.UTF_8), 
				String.format("Removed Property Definition %1$s with ID %2$s from Tag %3$s with ID %4$s",
					propertyDefinition.getName(), propertyDefinition.getUuid(),
					tagDefinition.getName(), tagDefinition.getUuid()),
				remoteGitServerManager.getUsername(),
				remoteGitServerManager.getEmail());
			
				
			return tagsetRevision;
		}
	}

	public String updateTagsetDefinition(String projectId, TagsetDefinition tagsetDefinition) throws Exception {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetDefinition.getUuid();
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			
			File targetHeaderFile = new File(localGitRepoManager.getRepositoryWorkTree(), "header.json");
			GitTagsetHeader header = 
					new GitTagsetHeader(
							tagsetDefinition.getName(), 
							"", //TODO: description
							new TreeSet<>(tagsetDefinition.getDeletedDefinitions()));
			String serializedHeader = new SerializationHelper<GitTagsetHeader>().serialize(header);
			
			localGitRepoManager.add(
					targetHeaderFile,
					serializedHeader.getBytes(StandardCharsets.UTF_8));

			String tagsetRevision = localGitRepoManager.addAndCommit(
					targetHeaderFile, 
					serializedHeader.getBytes(StandardCharsets.UTF_8), 
					String.format("Updated metadata of Tagset %1$s with ID %2$s", 
						tagsetDefinition.getName(), tagsetDefinition.getUuid()),
					remoteGitServerManager.getUsername(),
					remoteGitServerManager.getEmail());

			return tagsetRevision;
		}
	}

	public Status getStatus(String projectId, String tagsetId) throws IOException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);
			
			return localGitRepositoryManager.getStatus();
		}
	}

	public TagsetConflict getTagsetConflict(String projectId, String tagsetId) throws Exception {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);
			
			localGitRepoManager.open(projectId, projectRootRepositoryName);

			String tagsetSubmoduleRelDir = 
					GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME + "/" + tagsetId;
			
			File tagsetSubmoduleAbsPath = new File(
					localGitRepoManager.getRepositoryWorkTree().toString(),
					tagsetSubmoduleRelDir
			);

			localGitRepoManager.detach(); 
			//TODO: handle header conflict
			File tagsetHeaderFile = new File(
					tagsetSubmoduleAbsPath,
					"header.json"
			);
			
			String serializedTagsetHeaderFile = FileUtils.readFileToString(
					tagsetHeaderFile, StandardCharsets.UTF_8
			);
			
			GitTagsetHeader gitTagsetHeader = new SerializationHelper<GitTagsetHeader>()
					.deserialize(
							serializedTagsetHeaderFile,
							GitTagsetHeader.class
					);
			
			TagsetConflict tagsetConflict = 
				new TagsetConflict(
					projectId, 
					tagsetId, 
					gitTagsetHeader.getName(), 
					gitTagsetHeader.getDeletedDefinitions());
			
			
			String tagsetGitRepositoryName =
					projectRootRepositoryName + "/" + tagsetSubmoduleRelDir;

			localGitRepoManager.open(projectId, tagsetGitRepositoryName);

			Status status = localGitRepositoryManager.getStatus();

			for (Entry<String, StageState> entry : status.getConflictingStageState().entrySet()) {
				String relativeTagPathname = entry.getKey();
				String absTagPathname = tagsetSubmoduleAbsPath + "/" + relativeTagPathname;

				StageState stageState = entry.getValue();
				
				
				switch (stageState) {
				case BOTH_MODIFIED: {
					String serializedConflictingTag = FileUtils.readFileToString(
						new File(absTagPathname), StandardCharsets.UTF_8);
					
					TagConflict tagConflict = 
							getBothModifiedAnnotationConflict(
								projectId, tagsetId, serializedConflictingTag);
					tagsetConflict.addTagConflict(tagConflict);
					break;
				}
				default: System.out.println("not handled"); //TODO:
				}
				
			}			

			return tagsetConflict;
		}
	}

	private TagConflict getBothModifiedAnnotationConflict(
			String projectId, String tagsetId, String serializedConflictingTag) throws Exception {

		String masterVersion = serializedConflictingTag
			.replaceAll("\\Q<<<<<<< HEAD\\E(\\r\\n|\\r|\\n)", "")
			.replaceAll("\\Q=======\\E(\\r\\n|\\r|\\n|.)*?\\Q>>>>>>> \\E.+?(\\r\\n|\\r|\\n)", "");
		
		String devVersion = serializedConflictingTag
			.replaceAll("\\Q<<<<<<< HEAD\\E(\\r\\n|\\r|\\n|.)*?\\Q=======\\E(\\r\\n|\\r|\\n)", "")
			.replaceAll("\\Q>>>>>>> \\E.+?(\\r\\n|\\r|\\n)", "");

		GitTagDefinition gitMasterTagDefinition = new SerializationHelper<GitTagDefinition>()
				.deserialize(
						masterVersion,
						GitTagDefinition.class
				);

		TagDefinition masterTagDefinition = gitMasterTagDefinition.getTagDefinition();
		
		
		
		GitTagDefinition gitDevTagDefinition = new SerializationHelper<GitTagDefinition>()
				.deserialize(
						devVersion,
						GitTagDefinition.class
				);

		TagDefinition devTagDefinition = gitDevTagDefinition.getTagDefinition();
		
		TagConflict tagConflict = new TagConflict(masterTagDefinition, devTagDefinition);
		
		return tagConflict;
	}

	public void rebaseToMaster(String projectId, String tagsetId, String branch) throws Exception {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);
			localGitRepoManager.checkout(branch, false);
			localGitRepoManager.rebase(Constants.MASTER);
		}
	}

	public String addAllAndCommit(String projectId, String tagsetId, String commitMsg) throws Exception {

		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			String projectRootRepositoryName = GitProjectManager.getProjectRootRepositoryName(projectId);

			String tagsetGitRepositoryName = 
					projectRootRepositoryName 
					+ "/" + GitProjectHandler.TAGSET_SUBMODULES_DIRECTORY_NAME 
					+ "/" + tagsetId;
			
			localGitRepoManager.open(projectId, tagsetGitRepositoryName);		

			String tagsetRevision = localGitRepoManager.addAllAndCommit(
					commitMsg,
					remoteGitServerManager.getUsername(),
					remoteGitServerManager.getEmail());
				
			return tagsetRevision;
		}
	}
}
