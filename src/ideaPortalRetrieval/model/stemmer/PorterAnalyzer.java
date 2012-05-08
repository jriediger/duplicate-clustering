package ideaPortalRetrieval.model.stemmer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import java.io.Reader;
import java.util.Set;

public class PorterAnalyzer extends Analyzer{
	
	private Set stopWords;
	
	public PorterAnalyzer(){
		stopWords = StopFilter.makeStopSet(StopAnalyzer.ENGLISH_STOP_WORDS);
	}
	
	public TokenStream tokenStream(String fieldName, Reader reader){
		return new PorterStemFilter(new StandardFilter(new StopFilter(new LowerCaseFilter(new StandardTokenizer(reader)),stopWords)));
	}

}
