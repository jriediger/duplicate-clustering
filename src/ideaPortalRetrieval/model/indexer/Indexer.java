package ideaPortalRetrieval.model.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.store.*;

import ideaPortalRetrieval.model.stemmer.*;
import org.apache.lucene.analysis.Analyzer;


import java.io.*;
import java.util.StringTokenizer;

/**
 * @author Julian Riediger
 *
 */

public class Indexer implements IndexerInterface {
	
	/*
	 * 
	 * This class creates and adds documents to a Lucene index
	 * 
	 */
	
	private IndexWriter indexWriter;
	private Directory indexDirectory;
	private Document indexDoc;
	private Analyzer stemmer;
	
	public Indexer() throws IOException{
		indexDirectory = new RAMDirectory(); //Lucene index will be saved in RAM
		stemmer = new PorterAnalyzer();
		indexWriter = new IndexWriter(indexDirectory, stemmer, true, IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public Indexer(Analyzer stemmer) throws IOException{
		indexDirectory = new RAMDirectory();
		this.stemmer = stemmer; //stemmer is language-dependent
		indexWriter = new IndexWriter(indexDirectory, stemmer, true, IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public Indexer(String indexFileLocation) throws IOException{
		indexDirectory = FSDirectory.getDirectory(new File(indexFileLocation));
		this.stemmer = new PorterAnalyzer();
		indexWriter = new IndexWriter(indexDirectory, stemmer, true, IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public void addDocument(String ideaTitle, String ideaDesc) throws IOException{
		indexDoc = new Document();
		indexDoc.add(new Field("title", ideaTitle, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexDoc.add(new Field("description", ideaDesc, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexWriter.addDocument(indexDoc);
		System.out.println("Indexer: Idea added: " + "\"" + ideaTitle + "\"");
	}
	
	public void addDocument(String ideaTitle, String ideaCategory, String ideaDesc) throws IOException{
		indexDoc = new Document();
		indexDoc.add(new Field("title", ideaTitle, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexDoc.add(new Field("category", ideaCategory, Field.Store.YES, Field.Index.NO));
		indexDoc.add(new Field("description", ideaDesc, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexWriter.addDocument(indexDoc);
		System.out.println("Indexer: Idea added: " + "\"" + ideaTitle + "\"");
	}
	
	public void addDocument(String ideaTitle, String ideaDesc, Long ideaID) throws IOException{
		indexDoc = new Document();
		indexDoc.add(new Field("title", ideaTitle, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexDoc.add(new Field("ideaID", ideaID.toString(), Field.Store.YES, Field.Index.NO));
		indexDoc.add(new Field("description", ideaDesc, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexWriter.addDocument(indexDoc);
		System.out.println("Indexer: Idea added: " + "\"" + ideaTitle + "\"");
	}
	
	public void updateDocument(String ideaTitle, String ideaDesc, Long ideaID) throws IOException{
		indexDoc = new Document();
		indexDoc.add(new Field("title", ideaTitle, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		indexDoc.add(new Field("ideaID", ideaID.toString(), Field.Store.YES, Field.Index.NO));
		indexDoc.add(new Field("description", ideaDesc, Field.Store.YES, Field.Index.ANALYZED,  Field.TermVector.YES));
		StringTokenizer strTokenizer = new StringTokenizer(ideaTitle);
		PhraseQuery query = new PhraseQuery();
		while (strTokenizer.hasMoreTokens()){
			query.add((new Term("title",strTokenizer.nextToken())));
		}
		//indexWriter.deleteDocuments(query);
		//indexWriter.addDocument(indexDoc);
		indexWriter.updateDocument((new Term("title",ideaTitle)), indexDoc);
		System.out.println("Indexer: Idea updated: " + "\"" + ideaTitle + "\"");
	}
	
	public void openIndex() throws IOException{
		indexWriter = new IndexWriter(indexDirectory, stemmer, false, IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public void closeIndex() throws IOException{
		indexWriter.optimize();
	    indexWriter.close();
	}
	
	public void closeDirectory() throws IOException{
		indexDirectory.close();
	}
	
	public Directory getDirectory(){
		return this.indexDirectory;
	}

}
