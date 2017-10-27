package de.catma.repository.git;

import de.catma.repository.git.exceptions.LocalGitRepositoryManagerException;
import de.catma.repository.git.exceptions.RemoteGitServerManagerException;
import de.catma.repository.git.exceptions.TagsetHandlerException;
import de.catma.repository.git.interfaces.ILocalGitRepositoryManager;
import de.catma.repository.git.interfaces.IRemoteGitServerManager;
import de.catma.repository.git.interfaces.ITagsetHandler;
import de.catma.repository.git.managers.RemoteGitServerManager;
import de.catma.repository.git.serialization.SerializationHelper;
import de.catma.repository.git.serialization.model_wrappers.GitTagDefinition;
import de.catma.repository.git.serialization.models.HeaderBase;
import de.catma.repository.git.serialization.models.TagsetDefinitionHeader;
import de.catma.tag.TagDefinition;
import de.catma.tag.TagsetDefinition;
import de.catma.tag.Version;
import de.catma.util.IDGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.models.User;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagsetHandler implements ITagsetHandler {
	static final String TAGSET_ROOT_REPOSITORY_NAME_FORMAT = "%s_tagset";

	private final ILocalGitRepositoryManager localGitRepositoryManager;
	private final IRemoteGitServerManager remoteGitServerManager;

	private final IDGenerator idGenerator;

	public TagsetHandler(ILocalGitRepositoryManager localGitRepositoryManager,
							 IRemoteGitServerManager remoteGitServerManager) {
		this.localGitRepositoryManager = localGitRepositoryManager;
		this.remoteGitServerManager = remoteGitServerManager;

		this.idGenerator = new IDGenerator();
	}

	@Override
	public String create(String name, String description, Version version, String projectId) throws TagsetHandlerException {
		String tagsetId = idGenerator.generate();

		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {
			// create the tagset repository
			IRemoteGitServerManager.CreateRepositoryResponse response;

			// TODO: nice name for gitlab
			String tagsetRepoName = this.getTagsetRepoName(tagsetId);
			response = this.remoteGitServerManager.createRepository(
					tagsetRepoName, tagsetRepoName, projectId
			);

			// clone the repository locally
			RemoteGitServerManager remoteGitServerManagerImpl =
					(RemoteGitServerManager)this.remoteGitServerManager;

			User gitLabUser = remoteGitServerManagerImpl.getGitLabUser();
			String gitLabUserImpersonationToken = remoteGitServerManagerImpl
					.getGitLabUserImpersonationToken();

			localGitRepoManager.clone(
					response.repositoryHttpUrl,
					null,
					gitLabUser.getUsername(),
					gitLabUserImpersonationToken
			);

			// write header.json into the local repo
			File targetHeaderFile = new File(
					localGitRepoManager.getRepositoryWorkTree(), "header.json"
			);

			TagsetDefinitionHeader header = new TagsetDefinitionHeader(name, description, version);
			String serializedHeader = new SerializationHelper<HeaderBase>().serialize(header);
			byte[] headerBytes = serializedHeader.getBytes(StandardCharsets.UTF_8);

			localGitRepoManager.add(
					targetHeaderFile, headerBytes
			);

			// commit newly added files
			String commitMessage = String.format("Adding %s", targetHeaderFile.getName());
			String committerName = StringUtils.isNotBlank(gitLabUser.getName()) ? gitLabUser.getName() : gitLabUser.getUsername();
			localGitRepoManager.commit(commitMessage, committerName, gitLabUser.getEmail());
		}
		catch (RemoteGitServerManagerException|LocalGitRepositoryManagerException e) {
			throw new TagsetHandlerException("Failed to create Tagset repo", e);
		}

		return tagsetId;
	}

	@Override
	public void delete(String tagsetId) throws TagsetHandlerException {
		throw new TagsetHandlerException("Not implemented");
	}

	String getTagsetRepoName(String tagsetId){
		return String.format(TAGSET_ROOT_REPOSITORY_NAME_FORMAT, tagsetId);
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

	@Override
	public TagsetDefinition open(String tagsetId, String projectId) throws TagsetHandlerException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {

			localGitRepoManager.open(this.getTagsetRepoName(tagsetId));

			File repositoryWorkTreeFile = localGitRepoManager.getRepositoryWorkTree();
			File targetHeaderFile = new File(
					repositoryWorkTreeFile, "header.json"
			);

			String serialized = FileUtils.readFileToString(targetHeaderFile, StandardCharsets.UTF_8);
			TagsetDefinitionHeader tagsetDefinitionHeader = new SerializationHelper<TagsetDefinitionHeader>()
					.deserialize(
							serialized,
							TagsetDefinitionHeader.class
					);

			//Integer id, String uuid, String tagsetName, Version version
			TagsetDefinition tagsetdefinition = new TagsetDefinition(null, tagsetId, tagsetDefinitionHeader.getName(), tagsetDefinitionHeader.version());

			ArrayList<TagDefinition> tagDefinitions = this.openTagDefinitions(repositoryWorkTreeFile);

			for(TagDefinition tagdefinition : tagDefinitions){
				tagsetdefinition.addTagDefinition(tagdefinition);
			}

			return tagsetdefinition;
		}
		catch (LocalGitRepositoryManagerException | IOException e) {
			throw new TagsetHandlerException("Failed to open the Tagset repo", e);
		}

	}

	@Override
	public String addTagDefinition(String tagsetId, TagDefinition tagDefinition) throws TagsetHandlerException {
		try (ILocalGitRepositoryManager localGitRepoManager = this.localGitRepositoryManager) {

			localGitRepoManager.open(this.getTagsetRepoName(tagsetId));

			String propertyDefinitionPath = String.format("%s/%s", tagDefinition.getUuid(), "propertydefs.json");

			if(StringUtils.isNotEmpty(tagDefinition.getParentUuid())){
				propertyDefinitionPath = String.format("%s/%s", tagDefinition.getParentUuid(), propertyDefinitionPath);
			}

			// write header.json into the local repo
			File propertyDefFile = new File(
					localGitRepoManager.getRepositoryWorkTree(), propertyDefinitionPath
			);
			propertyDefFile.getParentFile().mkdirs();

			GitTagDefinition getTagDefinition = new GitTagDefinition(tagDefinition);
			String serializedTagDefinition = new SerializationHelper<GitTagDefinition>().serialize(getTagDefinition);
			byte[] propertyDefBytes = serializedTagDefinition.getBytes(StandardCharsets.UTF_8);

			// commit newly added files
			RemoteGitServerManager remoteGitServerManagerImpl =
					(RemoteGitServerManager)this.remoteGitServerManager;

			User gitLabUser = remoteGitServerManagerImpl.getGitLabUser();

			String committerName = StringUtils.isNotBlank(gitLabUser.getName()) ? gitLabUser.getName() : gitLabUser.getUsername();
			localGitRepoManager.addAndCommit(
					propertyDefFile, propertyDefBytes, committerName, gitLabUser.getEmail()
			);

			return tagDefinition.getUuid();
		}
		catch (LocalGitRepositoryManagerException e) {
			throw new TagsetHandlerException("Failed to create add the TagDefinition to the repo", e);
		}
	}
}
