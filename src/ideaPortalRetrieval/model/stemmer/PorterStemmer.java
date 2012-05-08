package ideaPortalRetrieval.model.stemmer;

import org.apache.lucene.analysis.*;

public class PorterStemmer implements StemmerInterface{
	
	/*
	 * 
	 * This class is a wrapper class for PorterAnalyzer
	 * 
	 */
	
	private PorterAnalyzer analyzer; 
	
	public PorterStemmer(){
		this.analyzer = new PorterAnalyzer();
	}
	
	public Analyzer getAnalyzer(){
		return this.analyzer;
	}

}
