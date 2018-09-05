/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.ui.repository.wizard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;

import de.catma.document.Range;
import de.catma.document.repository.Repository;
import de.catma.document.source.ContentInfoSet;
import de.catma.document.source.FileOSType;
import de.catma.document.source.FileType;
import de.catma.document.source.SourceDocument;
import de.catma.document.source.SourceDocumentHandler;
import de.catma.document.source.SourceDocumentInfo;
import de.catma.document.source.TechInfoSet;
import de.catma.document.source.contenthandler.DefaultProtocolHandler;
import de.catma.document.source.contenthandler.HttpProtocolHandler;
import de.catma.document.source.contenthandler.ProtocolHandler;
import de.catma.ui.CatmaApplication;
import de.catma.ui.dialog.wizard.DynamicWizardStep;
import de.catma.ui.dialog.wizard.WizardStepListener;
import de.catma.util.IDGenerator;

class FileTypePanel extends GridLayout implements DynamicWizardStep {
	
	class FileTypeCharsetValueChangeListener implements ValueChangeListener {
		@Override
		public void valueChange(ValueChangeEvent event) {
			SourceDocumentResult sdr = (SourceDocumentResult) table.getValue();
			if (sdr != null) {
				FileType fileType = sdr.getSourceDocumentInfo().getTechInfoSet().getFileType();
				
				if(!fileType.isCharsetSupported()){
					sdr.getSourceDocumentInfo().getTechInfoSet().setCharset(null);
				}					

				if (fileType.isCharsetSupported() 
						&& sdr.getSourceDocumentInfo().getTechInfoSet().getCharset() == null) {
					return;
				}
				
				if (loadSourceDocumentAndContent(sdr)) {
					onAdvance = canAdvance();
					
					showSourceDocumentPreview(sdr);
				}
				else {
					taPreview.setValue(""); //$NON-NLS-1$
				}
			}
		}
	}

	private Table table;
	private boolean onAdvance = false;
	private Label taPreview;
	private Panel previewPanel;
	private WizardStepListener wizardStepListener;
	private AddSourceDocWizardResult wizardResult;
	private Repository repository;
	private SourceDocument currentPreviewDocument = null;
	
	public FileTypePanel(
			WizardStepListener wizardStepListener, AddSourceDocWizardResult wizardResult, 
			Repository repository) {
		super(2,4);
		this.wizardResult = wizardResult;
		this.wizardStepListener = wizardStepListener;
		this.repository = repository;
		initComponents();
		initActions();
	}
	
	public boolean canAdvance(){
		boolean canAdvance = true;
		for(SourceDocumentResult sdr : wizardResult.getSourceDocumentResults()){
			TechInfoSet techInfoSet = sdr.getSourceDocumentInfo().getTechInfoSet();
			boolean needsEncoding = techInfoSet.getFileType().isCharsetSupported();
			if(needsEncoding && techInfoSet.getCharset() == null){
				canAdvance = false;
				break;
			}
		}
		
		return canAdvance;
	}
	
	public void stepActivated(boolean forward) {
		if (!forward) {
			return;
		}
		
		wizardResult.clearAllSourceDocumentResults();
		table.removeAllItems();
		
		try {
			
			final TechInfoSet inputTechInfoSet = wizardResult.getInputTechInfoSet();

			ArrayList<SourceDocumentResult> sourceDocumentResults = makeSourceDocumentResultsFromInputFile(inputTechInfoSet);
			
			@SuppressWarnings("unchecked")
			BeanItemContainer<SourceDocumentResult> container = 
				(BeanItemContainer<SourceDocumentResult>)table.getContainerDataSource();
			container.addAll(sourceDocumentResults);
			
			wizardResult.addSourceDocumentResults(sourceDocumentResults);
			
			if(sourceDocumentResults.size() > 0){
				table.select(sourceDocumentResults.get(0));
			}
			
			onAdvance = canAdvance();
		}
		catch (Exception exc) {
			((CatmaApplication)UI.getCurrent()).showAndLogError(
				Messages.getString("FileTypePanel.errorDetectingFileType"), exc); //$NON-NLS-1$
		}
	}
	
	private ProtocolHandler getProtocolHandlerForUri(
			URI inputFileURI, String fileID, String inputFileMimeType)
					throws MalformedURLException, IOException{
		if (inputFileURI.toURL().getProtocol().toLowerCase().equals("http") //$NON-NLS-1$
				|| inputFileURI.toURL().getProtocol().toLowerCase().equals("https")) { //$NON-NLS-1$
			
			final String destinationFileUri = repository.getFileURL(
					fileID, ((CatmaApplication)UI.getCurrent()).getTempDirectory() + "/"); //$NON-NLS-1$
			
			return new HttpProtocolHandler(
					inputFileURI, destinationFileUri);
		}
		else {
			return new DefaultProtocolHandler(
					inputFileURI, inputFileMimeType);
		}
	}
	
	private ArrayList<SourceDocumentResult> makeSourceDocumentResultsFromInputFile(
			TechInfoSet inputTechInfoSet) throws MalformedURLException, IOException {
		
		ArrayList<SourceDocumentResult> output = new ArrayList<SourceDocumentResult>();
		
		FileType inputFileType = FileType.getFileType(inputTechInfoSet.getMimeType());
		
		if (inputFileType != FileType.ZIP) {
			SourceDocumentResult outputSourceDocumentResult = new SourceDocumentResult();
			
			String sourceDocumentID = repository.getIdFromURI(inputTechInfoSet.getURI());
			outputSourceDocumentResult.setSourceDocumentID(sourceDocumentID);
			
			SourceDocumentInfo outputSourceDocumentInfo = outputSourceDocumentResult.getSourceDocumentInfo();			
			outputSourceDocumentInfo.setTechInfoSet(new TechInfoSet(inputTechInfoSet));
			outputSourceDocumentInfo.setContentInfoSet(new ContentInfoSet());
			
			outputSourceDocumentInfo.getTechInfoSet().setFileType(inputFileType);
			
			output.add(outputSourceDocumentResult);
		}
		else { //TODO: put this somewhere sensible
			URI uri = inputTechInfoSet.getURI();
			ZipFile zipFile = new ZipFile(uri.getPath());
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
			
			String tempDir = ((CatmaApplication)UI.getCurrent()).getTempDirectory();
			IDGenerator idGenerator = new IDGenerator();
			
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				String fileName = FilenameUtils.getName(entry.getName());
				if (fileName.startsWith(".")) {
					continue; // we treat them as hidden files, that's probably what most users would expect
				}
				String fileId = idGenerator.generate();
				
				File entryDestination = new File(tempDir, fileId);
				if (entryDestination.exists()) {
					entryDestination.delete();
				}
				
				entryDestination.getParentFile().mkdirs();
				if(entry.isDirectory()){
					entryDestination.mkdirs();
				}
				else {
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryDestination));
					IOUtils.copy(bis, bos);
					IOUtils.closeQuietly(bis);
					IOUtils.closeQuietly(bos);
					
					SourceDocumentResult outputSourceDocumentResult = new SourceDocumentResult();
					URI newURI = entryDestination.toURI();
					
					String repositoryId = repository.getIdFromURI(newURI); // we need to do this as a catma:// is appended
					
					outputSourceDocumentResult.setSourceDocumentID(repositoryId);
					
					SourceDocumentInfo outputSourceDocumentInfo = outputSourceDocumentResult.getSourceDocumentInfo();
					FileType newFileType = FileType.getFileTypeFromName(fileName);
					TechInfoSet newTechInfoSet = new TechInfoSet(fileName, newFileType.getMimeType(), newURI);
					newTechInfoSet.setFileType(newFileType);
					
					outputSourceDocumentInfo.setTechInfoSet(newTechInfoSet);
					outputSourceDocumentInfo.setContentInfoSet(new ContentInfoSet());
					
					output.add(outputSourceDocumentResult);
				}
			}
			
			ZipFile.closeQuietly(zipFile);
		}

		
		for (SourceDocumentResult sdr : output) {
			TechInfoSet sdrTechInfoSet = sdr.getSourceDocumentInfo().getTechInfoSet();
			String sdrSourceDocumentId = sdr.getSourceDocumentID();
			
			ProtocolHandler protocolHandler = getProtocolHandlerForUri(sdrTechInfoSet.getURI(), sdrSourceDocumentId, sdrTechInfoSet.getMimeType());
			String mimeType = protocolHandler.getMimeType();
			
			sdrTechInfoSet.setMimeType(mimeType);			
			FileType sdrFileType = FileType.getFileType(mimeType);
			sdrTechInfoSet.setFileType(sdrFileType);
			
			if (sdrFileType.isCharsetSupported()) {
				Charset charset = Charset.forName(protocolHandler.getEncoding());
				sdrTechInfoSet.setCharset(charset);
			}
			else {
				sdrTechInfoSet.setFileOSType(FileOSType.INDEPENDENT);
			}
			
			loadSourceDocumentAndContent(sdr);
		}
		
		return output;
	}

	private void showSourceDocumentPreview(SourceDocumentResult sdr){
		if (currentPreviewDocument != null) {
			currentPreviewDocument.unload();
		}

		currentPreviewDocument = sdr.getSourceDocument();
		try{
			taPreview.setValue(
					"<pre>" + currentPreviewDocument.getContent(new Range(0, 2000)) + "</pre>");			 //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			((CatmaApplication)UI.getCurrent()).showAndLogError(
				Messages.getString("FileTypePanel.errorLoadingPreview"), e); //$NON-NLS-1$
		}		
	}
	
	private boolean loadSourceDocumentAndContent(SourceDocumentResult sdr) {
		try {
			SourceDocumentHandler sourceDocumentHandler = new SourceDocumentHandler();
			SourceDocument sourceDocument =
					sourceDocumentHandler.loadSourceDocument(
							sdr.getSourceDocumentID(),
							sdr.getSourceDocumentInfo());
			sdr.setSourceDocument(sourceDocument);
			try {
				TechInfoSet techInfoSet = sdr.getSourceDocumentInfo().getTechInfoSet();
				String documentId = sdr.getSourceDocumentID();
				
				ProtocolHandler protocolHandler = getProtocolHandlerForUri(techInfoSet.getURI(), documentId, techInfoSet.getMimeType());
				
				byte[] currentByteContent = protocolHandler.getByteContent();
	
				sourceDocument.getSourceContentHandler().load(
						new ByteArrayInputStream(currentByteContent));
	
				FileOSType fileOSType = FileOSType.getFileOSType(sourceDocument.getContent());
				
				sdr.getSourceDocumentInfo().getTechInfoSet().setFileOSType(fileOSType);
				CRC32 checksum = new CRC32();
				checksum.update(currentByteContent);
				sdr.getSourceDocumentInfo().getTechInfoSet().setChecksum(checksum.getValue());
				return true;
			}
			finally {
				sourceDocument.unload();
			}
		} catch (Exception e) {
			TechInfoSet techInfoSet = sdr.getSourceDocumentInfo().getTechInfoSet();
			if (techInfoSet.getFileType().equals(FileType.DOC)) {
				Logger.getLogger(FileTypePanel.class.getName()).log(
					Level.WARNING, 
					"error loading DOC file, retrying load as DOCX: " //$NON-NLS-1$
							+ techInfoSet.getFileName(), 
					e);
				techInfoSet.setFileType(FileType.DOCX);
				return loadSourceDocumentAndContent(sdr);
			}
			else {
				Notification.show(
					Messages.getString("FileTypePanel.infoTitle"),  //$NON-NLS-1$
					MessageFormat.format(Messages.getString("FileTypePanel.unableToProcessfile"),   //$NON-NLS-1$
							techInfoSet.getFileType(), 
							(techInfoSet.getFileType().isCharsetSupported()?
									((techInfoSet.getCharset()==null)?Messages.getString("FileTypePanel.unknown"):techInfoSet.getCharset()) //$NON-NLS-1$
									:""), //$NON-NLS-1$
							e.getLocalizedMessage()),
					Notification.Type.WARNING_MESSAGE);
				return false;
			}
		}
	}
	
	private void initComponents() {
		setSpacing(true);
		setSizeFull();
		setMargin(true);
		
		BeanItemContainer<SourceDocumentResult> container = new BeanItemContainer<SourceDocumentResult>(SourceDocumentResult.class);
		container.addNestedContainerProperty("sourceDocumentInfo.techInfoSet.fileName"); //$NON-NLS-1$
		container.addNestedContainerProperty("sourceDocumentInfo.techInfoSet.fileType"); //$NON-NLS-1$
		container.addNestedContainerProperty("sourceDocumentInfo.techInfoSet.charset"); //$NON-NLS-1$
		
		table = new Table(Messages.getString("FileTypePanel.documents"), container); //$NON-NLS-1$

		table.setTableFieldFactory(new FileTypeFieldFactory(new FileTypeCharsetValueChangeListener(), new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object itemId = event.getProperty().getValue();
				
				if (itemId != null) {
					loadSourceDocumentAndContent((SourceDocumentResult)itemId);
				}
			}
		}));
		
		table.setVisibleColumns(new Object[]{
				"sourceDocumentInfo.techInfoSet.fileName", //$NON-NLS-1$
				"sourceDocumentInfo.techInfoSet.fileType", //$NON-NLS-1$
				"sourceDocumentInfo.techInfoSet.charset" //$NON-NLS-1$
		});
		table.setColumnHeaders(new String[]{Messages.getString("FileTypePanel.Filename"), Messages.getString("FileTypePanel.Filetype"), Messages.getString("FileTypePanel.Encoding")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		table.setSelectable(true);
		table.setNullSelectionAllowed(false);
		table.setImmediate(true);
		table.setEditable(true);
		
		addComponent(table, 0, 0);
		
		VerticalLayout previewContent = new VerticalLayout();
		previewContent.setMargin(true);
		previewPanel = new Panel(Messages.getString("FileTypePanel.preview"), previewContent); //$NON-NLS-1$
		previewPanel.getContent().setSizeUndefined();
		previewPanel.setHeight("100%"); //$NON-NLS-1$
		previewPanel.setStyleName("preview-panel"); //$NON-NLS-1$
		
		this.taPreview = new Label();
		this.taPreview.setContentMode(ContentMode.HTML);
		previewContent.addComponent(taPreview);
		
		addComponent(previewPanel, 1, 0);
		
		setColumnExpandRatio(1, 1);
	}
	
	private void initActions() {
		table.addValueChangeListener(new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				if(table.getValue() == null){
					return;
				}
				
				SourceDocumentResult sdr = (SourceDocumentResult)table.getValue();
				
				handleFileType(sdr);
			}
		});
		
	}
	
	private void handleFileType(SourceDocumentResult sdr) {
		switch(sdr.getSourceDocumentInfo().getTechInfoSet().getFileType()) {
			case TEXT : {
				showSourceDocumentPreview(sdr);
				onAdvance = true;
				break;
			}
			default : {
				showSourceDocumentPreview(sdr);
				onAdvance = true;
			}
		}
		wizardStepListener.stepChanged(FileTypePanel.this);
	}
	
	public Component getContent() {
		return this;
	}
	
	public boolean onAdvance() {
		return onAdvance;
	}
	
	public boolean onBack() {
		return true;
	}
	
	@Override
	public String getCaption() {
		return Messages.getString("FileTypePanel.sourceDocfiletype"); //$NON-NLS-1$
	}
	
	public boolean onFinish() {
		return false;
	}
	
	public boolean onFinishOnly() {
		return false;
	}
	
	public void stepDeactivated(boolean forward){
		if (!forward) {
			wizardResult.clearAllSourceDocumentResults();
			table.removeAllItems();
		}
	}

	public void stepAdded() {/* noop */}

}