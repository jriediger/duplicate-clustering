package ideaPortalRetrieval.model.indexer;

import java.io.IOException;

public interface IndexerInterface {

	public void addDocument(String ideaTitle, String ideaCategory, String ideaDesc) throws IOException;

	public void closeIndex() throws IOException;
	
	public void closeDirectory() throws IOException;
	
}
