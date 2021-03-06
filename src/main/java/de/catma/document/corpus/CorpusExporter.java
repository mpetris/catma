package de.catma.document.corpus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import de.catma.document.annotation.AnnotationCollection;
import de.catma.document.annotation.AnnotationCollectionReference;
import de.catma.document.source.SourceDocument;
import de.catma.document.source.contenthandler.SourceContentHandler;
import de.catma.project.Project;
import de.catma.serialization.tei.TeiUserMarkupCollectionSerializationHandler;

public class CorpusExporter {
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyMMddhhmm");

	private Project repo;

	private String date;

	private boolean simpleEntryStyle;
	
	public CorpusExporter(Project repo, boolean simpleEntryStyle) {
		this.repo = repo;
		this.simpleEntryStyle = simpleEntryStyle;
		this.date = FORMATTER.format(new Date());
	}

	public void export(
		String exportName, Corpus corpus,  OutputStream os) throws IOException {
		
		OutputStream tarFileOs = new GZIPOutputStream(os);
		
		TarArchiveOutputStream taOut = new TarArchiveOutputStream(tarFileOs, "UTF-8");
		try {
			
			taOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			taOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
			
			for (SourceDocument sd : corpus.getSourceDocuments()) {
				
				TarArchiveEntry sdEntry = 
					new TarArchiveEntry(getSourceDocEntryName(exportName, sd));
			
				byte[] sdContent = 
					sd.getContent().getBytes(Charset.forName("UTF8"));
				
				sdEntry.setSize(sdContent.length);
				
				taOut.putArchiveEntry(sdEntry);
				
				taOut.write(sdContent);
				
				taOut.closeArchiveEntry();
				
				for (AnnotationCollectionReference umcRef 
						: corpus.getUserMarkupCollectionRefs(sd)) {
					
					AnnotationCollection umc = 
							repo.getUserMarkupCollection(umcRef);

					TeiUserMarkupCollectionSerializationHandler handler =
							new TeiUserMarkupCollectionSerializationHandler(
									repo.getTagManager(), false);
					ByteArrayOutputStream teiDocOut = new ByteArrayOutputStream();
					handler.serialize(
						repo.getUserMarkupCollection(umcRef), sd, teiDocOut);

					byte[] umcContent = teiDocOut.toByteArray();
					
					String umcEntryName = getUmcEntryName(exportName, umc, sd);
					
					TarArchiveEntry umcEntry = 
						new TarArchiveEntry(umcEntryName);
					
					umcEntry.setSize(umcContent.length);
					
					taOut.putArchiveEntry(umcEntry);
					taOut.write(umcContent);
					
					taOut.closeArchiveEntry();
				}
			}
		}
		finally {
			taOut.finish();
			taOut.close();
		}
		
	}
	
	private String getUmcEntryName(String exportName, AnnotationCollection umc, SourceDocument sd) {
		if (simpleEntryStyle) {
			return cleanupName(getFilename(sd, false))
					+ "/annotationcollections/" 
					+ cleanupName(umc.getName())
					+ ".xml";
		}
		
		return exportName 
				+ "_" 
				+ date 
				+ "/" + cleanupName(sd.getUuid()) 
				+ "/annotationcollections/" 
				+ cleanupName(umc.getName())
				+ ".xml";

	}

	private String getSourceDocEntryName(String exportName, SourceDocument sd) {
		if (simpleEntryStyle) {
			return cleanupName(getFilename(sd, false)) 
					+ "/" 
					+ cleanupName(getFilename(sd, true)); 
		}
		return exportName 
				+ "_" 
				+ date 
				+ "/" 
				+ cleanupName(sd.getUuid()) 
				+ "/" 
				+ cleanupName(getFilename(sd, true));
	}

	public String getDate() {
		return date;
	}
	
	public String cleanupName(String name) {
		return name.replaceAll("[/:]|\\s", "_");
	}
	
	private String getFilename(SourceDocument sourceDocument, boolean withFileExtension) {
		SourceContentHandler sourceContentHandler = 
				sourceDocument.getSourceContentHandler();
		String title = 
				sourceContentHandler.getSourceDocumentInfo()
					.getContentInfoSet().getTitle();
		if (simpleEntryStyle) {
			return sourceDocument.toString() + (withFileExtension?".txt":"");
		}
		return sourceDocument.getUuid() 
			+ (((title==null)||title.isEmpty())?"":("_"+title)) 
			+ (withFileExtension?".txt":"");
	};

}
