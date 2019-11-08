package de.catma.repository.git.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.SubmoduleAddCommand;
import org.eclipse.jgit.api.SubmoduleInitCommand;
import org.eclipse.jgit.api.SubmoduleStatusCommand;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.util.FS;

import de.catma.repository.git.CommitMissingException;
import de.catma.repository.git.interfaces.ILocalGitRepositoryManager;
import de.catma.repository.git.managers.jgitcommand.RelativeJGitFactory;
import de.catma.user.User;

public class JGitRepoManager implements ILocalGitRepositoryManager, AutoCloseable {
	private final String repositoryBasePath;
	private String username;

	private Git gitApi;
	private JGitFactory jGitFactory;
	

	/**
	 * Creates a new instance of this class for the given {@link User}.
	 * <p>
	 * Note that the <code>catmaUser</code> argument is NOT used for authentication to remote Git servers. It is only
	 * used to organise repositories on the local file system, based on the User's identifier. Methods of this class
	 * that support authentication have their own username and password parameters for this purpose.
	 *
	 * @param repositoryBasePath the repo base path
	 * @param catmaUser a {@link User} object
	 */
	public JGitRepoManager(String repositoryBasePath, User catmaUser) {
		this.repositoryBasePath = repositoryBasePath;
		this.username = catmaUser.getIdentifier();
		this.jGitFactory = new RelativeJGitFactory();
	}

	public Git getGitApi() {
		return this.gitApi;
	}

	/**
	 * Whether this instance is attached to a Git repository.
	 *
	 * @return true if attached, otherwise false
	 */
	@Override
	public boolean isAttached() {
		return this.gitApi != null;
	}

	/**
	 * Detach this instance from the currently attached Git repository, if any. If this instance is currently attached,
	 * this will allow you to re-use it to make calls to methods that require it to be detached, for example
	 * <code>init</code>, <code>clone</code> or <code>open</code>.
	 * <p>
	 * Whenever possible, you should use a try-with-resources statement instead, which will do this for you
	 * automatically.
	 * <p>
	 * Calling this method or using a try-with-resources statement will cause {@link #close()} to be called.
	 *
	 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">The try-with-resources Statement</a>
	 */
	@Override
	public void detach() {
		this.close();
	}

	/**
	 * Gets the repository base path for this instance, which is specific to the {@link User} supplied at instantiation.
	 *
	 * @return a {@link File} object
	 */
	@Override
	public File getRepositoryBasePath() {
		return Paths.get(new File(repositoryBasePath).toURI()).resolve(this.username).toFile();
	}

	/**
	 * Gets the current Git working tree for the repository this instance is attached to, if any.
	 *
	 * @return a {@link File} object
	 */
	@Override
	public File getRepositoryWorkTree() {
		if (!this.isAttached()) {
			return null;
		}

		return this.gitApi.getRepository().getWorkTree();
	}

	/**
	 * Gets the URL for the remote with the name <code>remoteName</code>.
	 *
	 * @param remoteName the name of the remote for which the URL should be fetched. Defaults to 'origin' if not
	 *                   supplied.
	 * @return the remote URL
	 */
	@Override
	public String getRemoteUrl(String remoteName) {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `getRemoteUrl` on a detached instance");
		}

		if (remoteName == null) {
			remoteName = "origin";
		}

		StoredConfig config = this.gitApi.getRepository().getConfig();
		return config.getString("remote", remoteName, "url");
	}

	/**
	 * Initialises a new Git repository with the directory name <code>name</code> and stores the
	 * <code>description</code> in '.git/description'.
	 *
	 * @param name the directory name of the Git repository to initialise
	 * @param description the description of the Git repository to initialise
	 * @throws IOException if the Git repository already exists or couldn't
	 *         be initialised for some other reason
	 */
	@Override
	public void init(String group, String name, String description)
			throws IOException {
		if (isAttached()) {
			throw new IllegalStateException("Can't call `init` on an attached instance");
		}

		File repositoryPath = 
			Paths.get(this.getRepositoryBasePath().toURI()).resolve(group).resolve(name).toFile();

		// if the directory exists we assume it's a Git repo, could also check for a child .git
		// directory
		if (repositoryPath.exists() && repositoryPath.isDirectory()) {
			throw new IOException(
				String.format(
					"A Git repository with the name '%s' already exists at base path '%s'. " +
					"Did you mean to call `open`?",
						name,
					this.getRepositoryBasePath()
				)
			);
		}

		try {
			this.gitApi = Git.init().setDirectory(repositoryPath).call();
			this.gitApi.getRepository().setGitwebDescription(description);
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to init Git repository", e);
		}
	}

	public String clone(String group, String uri, File path, CredentialsProvider credentialsProvider)
			throws IOException {
		return clone(group, uri, path, credentialsProvider, false);
	}
	
	/**
	 * Clones a remote repository whose address is specified via the <code>uri</code> parameter.
	 *
	 * @param uri the URI of the remote repository to clone
	 * @param path the destination path of the clone operation
	 * @param username the username to authenticate with
	 * @param password the password to authenticate with
	 * @param initAndUpdateSubmodules init and update submodules
	 * @return the name of the cloned repository
	 */
	@Override
	public String clone(String group, String uri, File path, CredentialsProvider credentialsProvider, boolean initAndUpdateSubmodules)
			throws IOException {
		if (isAttached()) {
			throw new IllegalStateException("Can't call `clone` on an attached instance");
		}

		if (path == null) {
			String repositoryName = uri.substring(uri.lastIndexOf("/") + 1);
			if (repositoryName.endsWith(".git")) {
				repositoryName = repositoryName.substring(0, repositoryName.length() - 4);
			}
			path = Paths.get(this.getRepositoryBasePath().toURI()).resolve(group).resolve(repositoryName).toFile();
		}

		try {
			CloneCommand cloneCommand = 
					jGitFactory.newCloneCommand()
					.setURI(uri).setDirectory(path);

			cloneCommand.setCredentialsProvider(credentialsProvider);
			
			this.gitApi = cloneCommand.call();
			
			if (initAndUpdateSubmodules) {
				this.gitApi.submoduleInit().call();
				
//				SubmoduleUpdateCommand submoduleUpdateCommand = 
//					jGitFactory.newSubmoduleUpdateCommand(gitApi.getRepository());
//				
//				submoduleUpdateCommand.setCredentialsProvider(credentialsProvider);
//				submoduleUpdateCommand.call();
				
			}
		}
		catch (GitAPIException e) {
			throw new IOException(
				"Failed to clone remote Git repository", e
			);
		}

		return path.getName();
	}

	/**
	 * Opens an existing Git repository with the directory name <code>name</code>.
	 *
	 * @param name the directory name of the Git repository to open
	 * @throws IOException if the Git repository couldn't be found or
	 *         couldn't be opened for some other reason
	 */
	@Override
	public void open(String group, String name) throws IOException {
		if (isAttached()) {
			throw new IllegalStateException("Can't call `open` on an attached instance");
		}

		File repositoryPath = Paths.get(getRepositoryBasePath().toURI()).resolve(group).resolve(name).toFile();

		// could also check for the absence of a child .git directory
		if (!repositoryPath.exists() || !repositoryPath.isDirectory()) {
			throw new IOException(
				String.format(
					"Couldn't find a Git repository with the name '%s' at base path '%s'. " +
					"Did you mean to call `init`?",
						name,
					this.getRepositoryBasePath()
				)
			);
		}

		File gitDir = new File(repositoryPath, ".git");
		if (gitDir.isFile()) {
			gitDir = Paths.get(repositoryPath.toURI()).resolve(
				FileUtils.readFileToString(gitDir, StandardCharsets.UTF_8)
				.replace("gitdir:", "")
				.trim()).normalize().toFile();
		}
		this.gitApi = Git.open(gitDir);
	}

	@Override
	public String getRevisionHash() throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't determine root revision hash on a detached instance");
		}
		ObjectId headRevision = this.gitApi.getRepository().resolve(Constants.HEAD);
		if (headRevision == null) {
			return NO_COMMITS_YET;
		}
		else {
			return headRevision.getName();
		}
	}
	
	@Override
	public List<String> getSubmodulePaths() throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't determine submodules from a detached instance");
		}
		try {
			List<String> paths = new ArrayList<>();
			try (SubmoduleWalk submoduleWalk = SubmoduleWalk.forIndex(this.gitApi.getRepository())) {
				while (submoduleWalk.next()) {
					paths.add(submoduleWalk.getModulesPath());
				}
				
				return paths;
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}			
	}
	
	@Override
	public String getRevisionHash(String submodule) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't determine root revision hash on a detached instance");
		}
		
		ObjectId headRevision = 
			SubmoduleWalk
			.getSubmoduleRepository(this.gitApi.getRepository(), submodule)
			.resolve(Constants.HEAD);
		if (headRevision == null) {
			return "no_commits_yet";
		}
		else {
			return headRevision.getName();
		}
	}

	/**
	 * Writes a new file with contents <code>bytes</code> to disk at path <code>targetFile</code>
	 * and adds it to the attached Git repository.
	 * <p>
	 * It's the caller's responsibility to call {@link #commit(String, String, String)}.
	 *
	 * @param targetFile a {@link File} object representing the target path
	 * @param bytes the file contents
	 * @throws IOException if the file contents couldn't be written to disk
	 *         or the file couldn't be added for some other reason
	 */
	@Override
	public void add(File targetFile, byte[] bytes) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `add` on a detached instance");
		}

		try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(targetFile)) {
			fileOutputStream.write(bytes);

			Path basePath = this.gitApi.getRepository().getWorkTree().toPath();
			Path absoluteFilePath = Paths.get(targetFile.getAbsolutePath());
			Path relativeFilePath = basePath.relativize(absoluteFilePath);

			this.gitApi
				.add()
				.addFilepattern(FilenameUtils.separatorsToUnix(relativeFilePath.toString()))
				.call();
		}
		catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	public void add(Path relativePath) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `add` on a detached instance");
		}
		try {
			this.gitApi
			.add()
			.addFilepattern(FilenameUtils.separatorsToUnix(relativePath.toString()))
			.call();
		}
		catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void remove(File targetFile) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `remove` on a detached instance");
		}

		try {

			Path basePath = this.gitApi.getRepository().getWorkTree().toPath();
			Path absoluteFilePath = Paths.get(targetFile.getAbsolutePath());
			Path relativeFilePath = basePath.relativize(absoluteFilePath);

			this.gitApi
				.rm()
				.setCached(true)
				.addFilepattern(FilenameUtils.separatorsToUnix(relativeFilePath.toString()))
				.call();
			
			if (targetFile.isDirectory()) {
				FileUtils.deleteDirectory(targetFile);
			}
			else if (!targetFile.delete()) {
				throw new IOException(String.format(
						"could not remove %s", 
						targetFile.toString()));
			}
		}
		catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public String removeSubmodule(File submodulePath, String commitMsg, String committerName, String committerEmail) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `removeSubmodule` on a detached instance");
		}
		try {
			Path basePath = this.gitApi.getRepository().getWorkTree().toPath();
			Path absoluteFilePath = Paths.get(submodulePath.getAbsolutePath());
			Path relativeFilePath = basePath.relativize(absoluteFilePath);
			String relativeUnixStyleFilePath = FilenameUtils.separatorsToUnix(relativeFilePath.toString());
			
		    File gitSubmodulesFile = 
		    	new File(
		    			this.gitApi.getRepository().getWorkTree(), 
		    			Constants.DOT_GIT_MODULES );
		    FileBasedConfig gitSubmodulesConfig = 
		    		new FileBasedConfig(null, gitSubmodulesFile, FS.DETECTED );		
		    gitSubmodulesConfig.load();
		    gitSubmodulesConfig.unsetSection(
		    		ConfigConstants.CONFIG_SUBMODULE_SECTION, relativeUnixStyleFilePath);
		    gitSubmodulesConfig.save();
		    StoredConfig repositoryConfig = this.getGitApi().getRepository().getConfig();
		    repositoryConfig.unsetSection(
		    	ConfigConstants.CONFIG_SUBMODULE_SECTION, relativeUnixStyleFilePath);
		    repositoryConfig.save();
		    
		    gitApi.add().addFilepattern(Constants.DOT_GIT_MODULES).call();
		    gitApi.rm().setCached(true).addFilepattern(relativeUnixStyleFilePath).call();

		    String projectRevisionHash = commit(commitMsg, committerName, committerEmail, false);
			
			File submoduleGitDir = 
				basePath
				.resolve(Constants.DOT_GIT)
				.resolve(Constants.MODULES)
				.resolve(relativeFilePath).toFile();
			
			FileUtils.deleteDirectory(submoduleGitDir);
			
			FileUtils.deleteDirectory(absoluteFilePath.toFile());

			return projectRevisionHash;
		}
		catch (GitAPIException | ConfigInvalidException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public String removeAndCommit(File targetFile, String commitMsg, String committerName, String committerEmail)
			throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `removeAndCommit` on a detached instance");
		}

		this.remove(targetFile);
		return this.commit(commitMsg, committerName, committerEmail, false);
	}

	/**
	 * Writes a new file with contents <code>bytes</code> to disk at path <code>targetFile</code>,
	 * adds it to the attached Git repository and commits.
	 * <p>
	 * Calls {@link #add(File, byte[])} and {@link #commit(String, String, String)} internally.
	 *
	 * @param targetFile a {@link File} object representing the target path
	 * @param bytes the file contents
	 * @return the revisionHash of the commit
	 * @throws IOException if the file contents couldn't be written to disk
	 *         or the commit operation failed
	 */
	@Override
	public String addAndCommit(
		File targetFile, byte[] bytes, String commitMsg, String committerName, String committerEmail)
			throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `addAndCommit` on a detached instance");
		}

		this.add(targetFile, bytes);
		return this.commit(commitMsg, committerName, committerEmail, false);
	}

	/**
	 * Commits pending changes to the attached Git repository.
	 *
	 * @param message the commit message
	 * @return revision hash
	 * @throws IOException if the commit operation failed
	 */
	@Override
	public String commit(String message, String committerName, String committerEmail, boolean force)
			throws IOException {
		return this.commit(message, committerName, committerEmail, false, force);
	}

	@Override
	public String commitWithSubmodules(
			String message, String committerName, String committerEmail, Set<String> submodules) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `commit` on a detached instance");
		}

		try {
			
			StatusCommand statusCommand = gitApi.status();
			for (String submodule : submodules) {
				statusCommand.addPath(submodule);
			}
			
			statusCommand.addPath(".");
			
			if (statusCommand.call().hasUncommittedChanges()) {
				AddCommand addCommand = this.gitApi.add();
				for (String submodule : submodules) {
					addCommand
					.addFilepattern(submodule);
				}
				addCommand.call();
				
				return this.gitApi
					.commit()
					.setMessage(message)
					.setCommitter(committerName, committerEmail)
					.call()
					.getName();
			}
			else {
				return getRevisionHash();
			}
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to commit", e);
		}
	}
	@Override
	public String commit(String message, String committerName, String committerEmail, boolean all, boolean force) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `commit` on a detached instance");
		}

		try {
			if (force || gitApi.status().call().hasUncommittedChanges()) {
				return this.gitApi
					.commit()
					.setMessage(message)
					.setCommitter(committerName, committerEmail)
					.setAll(all)
					.call()
					.getName();
			}
			else {
				return getRevisionHash();
			}
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to commit", e);
		}
	}
	/**
	 * Adds a new submodule to the attached Git repository.
	 *
	 * @param path a {@link File} object representing the target path of the submodule
	 * @param uri the URI of the remote repository to add as a submodule
	 * @param username the username to authenticate with
	 * @param password the password to authenticate with
	 * @throws IOException if the submodule couldn't be added
	 */
	@Override
	public void addSubmodule(File path, String uri, CredentialsProvider credentialsProvider)
			throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `addSubmodule` on a detached instance");
		}

		Path basePath = this.gitApi.getRepository().getWorkTree().toPath();
		Path relativeSubmodulePath = basePath.relativize(path.toPath());
		// NB: Git doesn't understand Windows path separators (\) in the .gitmodules file
		String unixStyleRelativeSubmodulePath = FilenameUtils.separatorsToUnix(relativeSubmodulePath.toString());

		try {
			SubmoduleAddCommand submoduleAddCommand = 
				jGitFactory.newSubmoduleAddCommand(gitApi.getRepository())
					.setURI(uri)
					.setPath(unixStyleRelativeSubmodulePath);
			//needs permissions because the submodule is cloned from remote first and then added locally
			submoduleAddCommand.setCredentialsProvider(credentialsProvider);

			Repository repository = submoduleAddCommand.call();
			repository.close();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to add submodule", e);
		}
	}

	/**
	 * Pushes commits made locally to the associated remote repository ('origin' remote).
	 *
	 * @param username the username to authenticate with
	 * @param password the password to authenticate with
	 * @throws IOException if the push operation failed
	 */
	@Override
	public void push(CredentialsProvider credentialsProvider) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `push` on a detached instance");
		}

		try {
			PushCommand pushCommand = this.gitApi.push();
			pushCommand.setCredentialsProvider(credentialsProvider);
			pushCommand.setRemote(Constants.DEFAULT_REMOTE_NAME);
			Iterable<PushResult> pushResults = pushCommand.call();
			for (PushResult pushResult : pushResults) {
				for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
					System.out.println(remoteRefUpdate);
				}
			}
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to push", e);
		}
	}

	/**
	 * Fetches refs from the associated remote repository ('origin' remote).
	 *
	 * @param username the username to authenticate with
	 * @param password the password to authenticate with
	 * @throws IOException if the fetch operation failed
	 */
	@Override
	public void fetch(CredentialsProvider credentialsProvider) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `fetch` on a detached instance");
		}

		try {
			FetchCommand fetchCommand = this.gitApi.fetch();
			fetchCommand.setCredentialsProvider(credentialsProvider);
			fetchCommand.call();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to fetch", e);
		}
	}
	
	@Override
	public boolean hasRef(String branch) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `hasRef` on a detached instance");
		}
		
		return this.gitApi.getRepository().findRef(branch) != null;
	}
	
	@Override
	public MergeResult merge(String branch) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `merge` on a detached instance");
		}

		try {
			MergeCommand mergeCommand = this.gitApi.merge();
			
			
			Ref ref = this.gitApi.getRepository().findRef(branch);
			if (ref != null) {
				mergeCommand.include(ref);
			} 
//			else {
//				mergeCommand.include(ObjectId.fromString(branch));
//			}
			
			return mergeCommand.call();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to merge", e);
		}		
	}
	
	@Override
	public void rebase(String branch) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `rebase` on a detached instance");
		}

		try {
			RebaseCommand rebaseCommand = this.gitApi.rebase();

			rebaseCommand.setUpstream(branch);

			rebaseCommand.call();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to rebase", e);
		}		
	}

	/**
	 * Checks out a branch or commit identified by <code>name</code>.
	 *
	 * @param name the name of the branch or commit to check out
	 * @throws IOException if the checkout operation failed
	 */
	@Override
	public void checkout(String name) throws IOException {
		this.checkout(name, false);
	}
	
	/**
	 * Checks out a branch or commit identified by <code>name</code>.
	 *
	 * @param name the name of the branch or commit to check out
	 * @param createBranch equals to -b CLI option
	 * @throws IOException if the checkout operation failed
	 */
	@Override
	public void checkout(String name, boolean createBranch) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `checkout` on a detached instance");
		}

		try {
			if (!this.gitApi.getRepository().getBranch().equals(name)) {
				CheckoutCommand checkoutCommand = this.gitApi.checkout();
				if (createBranch) {
					List<Ref> refs = this.gitApi.branchList().call();
					if (refs
						.stream()
						.map(rev -> rev.getName())
						.filter(revName -> revName.equals("refs/heads/"+name))
						.findFirst()
						.isPresent()) {
						createBranch = false;
					}
				}
				checkoutCommand.setCreateBranch(createBranch);
				checkoutCommand.setName(name).call();
			}
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to checkout", e);
		}
	}

	/**
	 * Gets the HEAD revision hash for the submodule with the name <code>submoduleName</code>.
	 *
	 * @param submoduleName the name of the submodule whose HEAD revision hash to get
	 * @return a revision hash
	 * @throws IOException if a submodule with the name <code>submoduleName</code> doesn't exist
	 *                                            or if the operation failed for another reason
	 */
	@Override
	public String getSubmoduleHeadRevisionHash(String submoduleName) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `getSubmoduleHeadRevisionHash` on a detached instance");
		}

		try {
			SubmoduleStatusCommand submoduleStatusCommand = this.gitApi.submoduleStatus();
			Map<String, SubmoduleStatus> results = submoduleStatusCommand.call();

			if (!results.containsKey(submoduleName)) {
				throw new IOException(
						String.format("Failed to get HEAD revision hash for submodule `%s`. " +
								"A submodule with that name does not appear to exist.", submoduleName)
				);
			}

			return results.get(submoduleName).getHeadId().getName();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to get submodule status", e);
		}
	}
	
	@Override
	public boolean hasUncommitedChanges() throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `status` on a detached instance");
		}
		
		try {
			return gitApi.status().call().hasUncommittedChanges();
		} catch (GitAPIException e) {
			throw new IOException("Failed to check for uncommited changes", e);
		}
	}

	@Override
	public boolean hasUncommitedChangesWithSubmodules(Set<String> submodules) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `status` on a detached instance");
		}
		
		try {
			StatusCommand statusCommand = gitApi.status();
			for (String submodule : submodules) {
				statusCommand.addPath(submodule);
			}
			
			statusCommand.addPath(".");
			
			return statusCommand.call().hasUncommittedChanges();
		} catch (GitAPIException e) {
			throw new IOException("Failed to check for uncommited changes", e);
		}
	}
	
	@Override
	public boolean hasUntrackedChanges() throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `status` on a detached instance");
		}
		
		try {
			Status status = gitApi.status().call();
			
			return !status.getUntracked().isEmpty() || !status.getUntrackedFolders().isEmpty();
		} catch (GitAPIException e) {
			throw new IOException("Failed to check for untracked changes", e);
		}
	}
	
	@Override
	public String addAllAndCommit(
			String message, String committerName, String committerEmail, boolean force) throws IOException {
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `addAllAndCommit` on a detached instance");
		}

		try {
			gitApi.add().addFilepattern(".").call();
			
			List<DiffEntry> diffEntries = gitApi.diff().call();
			if (!diffEntries.isEmpty()) {
				RmCommand rmCmd = gitApi.rm();
				for (DiffEntry entry : diffEntries) {
					if (entry.getChangeType().equals(ChangeType.DELETE)) {
						rmCmd.addFilepattern(entry.getOldPath());
					}
				}
				
				rmCmd.call();
			}
			
			if (force || gitApi.status().call().hasUncommittedChanges()) {
				return commit(message, committerName, committerEmail, force);
			}
			else {
				return getRevisionHash();
			}
			
		} catch (GitAPIException e) {
			throw new IOException("Failed to add and commit changes", e);
		}
	}

	@Override // AutoCloseable
	public void close() {
		if (this.gitApi != null) {
			this.gitApi.close();
			this.gitApi = null;
		}
	}
	
	@Override
	public Status getStatus() throws IOException {
		try {
			if (!isAttached()) {
				throw new IllegalStateException("Can't call `getStatus` on a detached instance");
			}
			
			return gitApi.status().call();
		}
		catch (GitAPIException e) {
			throw new IOException("Failed to retrieve the status", e);
		}
	}

	@Override
	public void resolveRootConflicts(CredentialsProvider credentialsProvider) throws IOException {
		
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `resolveRootConflicts` on a detached instance");
		}
		DirCache dirCache = gitApi.getRepository().lockDirCache();
		try {
			if (dirCache.hasUnmergedPaths()) {
				
				for (String conflictingSubmodule : gitApi.status().call().getConflicting()) {
					
					// get the base entry from where the branches diverge, the common ancestor version
					int baseIdx = dirCache.findEntry(conflictingSubmodule);
					DirCacheEntry baseEntry = dirCache.getEntry(baseIdx);

					// get their version, the being-merged in version
					DirCacheEntry theirEntry = dirCache.getEntry(baseIdx+2); // TODO: check stage
					
					// we try to make sure that their version is included (merged) in the latest version
					// of this submodule
					ensureLatestSubmoduleRevision(
						baseEntry, theirEntry, conflictingSubmodule, credentialsProvider);
					
					ObjectId subModuleHeadRevision = 
							SubmoduleWalk
							.getSubmoduleRepository(this.gitApi.getRepository(), conflictingSubmodule)
							.resolve(Constants.HEAD);
					
					baseEntry.setObjectId(subModuleHeadRevision);
				}
				dirCache.write();
				dirCache.commit();
			}
			else {
				dirCache.unlock();
			}
		}
		catch (Exception e) {
			try  {
				dirCache.unlock();
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
			throw new IOException("Failed to resolve root conflicts", e);
		}
		
	}

	private void ensureLatestSubmoduleRevision(DirCacheEntry baseEntry, 
			DirCacheEntry theirEntry, String conflictingSubmodule,
			CredentialsProvider credentialsProvider) throws Exception {
		
		try (Git submoduleGitApi = Git.wrap(
			SubmoduleWalk.getSubmoduleRepository(this.gitApi.getRepository(), conflictingSubmodule))) {
			boolean foundTheirs = false;
			int tries = 10;
			while (!foundTheirs && tries > 0) {
				// iterate over the revisions until we find their commit or until we reach
				// the the common ancestor version (base)
				Iterator<RevCommit> revIterator = submoduleGitApi
				.log()
				.call()
				.iterator();
				while (revIterator.hasNext()) {
					RevCommit revCommit = revIterator.next();
					if (revCommit.getId().equals(theirEntry.getObjectId())) {
						// we found their version
						foundTheirs = true;
						break;
					}
					else if (revCommit.getId().equals(baseEntry.getObjectId())) {
						// we reached the common ancestor
						break;
					}
				}
				
				if (!foundTheirs) {
					// we reached the common ancestor without finding
					// their commit, so we pull again to see if it comes in now
					// and then we start over
					
					submoduleGitApi.checkout()
					.setName(Constants.MASTER)
					.setCreateBranch(false)
					.call();
					
					PullResult pullResult = 
						submoduleGitApi.pull().setCredentialsProvider(credentialsProvider).call();
					if (!pullResult.isSuccessful()) {
						throw new IllegalStateException(
							String.format(
							"Trying to get the latest commits for %1$s failed!", conflictingSubmodule));
					}
					
					submoduleGitApi
						.checkout()
						.setName(ILocalGitRepositoryManager.DEFAULT_LOCAL_DEV_BRANCH)
						.setCreateBranch(false)
						.call();
					
					submoduleGitApi.rebase().setUpstream(Constants.MASTER).call();
				}
				tries --;
			}
			
			if (!foundTheirs) {
				Logger.getLogger(this.getClass().getName()).severe(
						String.format("Cannot resolve root conflict for submodule %1$s commit %2$s is missing!",
						conflictingSubmodule, theirEntry.getObjectId().toString()));
				throw new CommitMissingException(
					"Failed to synchronize the Project because of a missing commit, "
					+ "try again later or contact the system administrator!");
			}
		}
	}
	
	@Override
	public void initAndUpdateSubmodules(CredentialsProvider credentialsProvider, Set<String> submodules) throws Exception {
		
		if (!isAttached()) {
			throw new IllegalStateException("Can't call `initAndUpdateSubmodules` on a detached instance");
		}

		SubmoduleInitCommand submoduleInitCommand = this.gitApi.submoduleInit();
		submoduleInitCommand.call();
		
		SubmoduleUpdateCommand submoduleUpdateCommand = 
			jGitFactory.newSubmoduleUpdateCommand(gitApi.getRepository());
		submodules.forEach(submodule -> submoduleUpdateCommand.addPath(submodule));
		submoduleUpdateCommand.setCredentialsProvider(credentialsProvider);
		submoduleUpdateCommand.call();
		
	}
}
