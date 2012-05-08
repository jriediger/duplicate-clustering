package ideaPortalRetrieval.model.indexer;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Julian Riediger
 *
 */

public class XMLParser implements ParserInterface {
	
	/*
	 * 
	 * This class parses XML documents with title, description (body) and category
	 * into a Lucene index structure. 
	 * 
	 */
	
	private List ideasList;
	private Iterator ideasIter;
	private int numIdeas;
	
	private Indexer indexer;
	private ArrayList<File> xmlFileList = new ArrayList<File>();

	public XMLParser(Indexer indexer) throws IOException{
		this.indexer = indexer;
		this.numIdeas = 0;
	}
	
	public void parse(String dataLocation) throws IOException{
		File fileLocation = new File(dataLocation);
		addXMLFiles(fileLocation);
		
		/*
		 * Parse all XML files from Directory
		 */
		for (File xmlF : xmlFileList)
			parseXMLFile(xmlF);
		
		System.out.println("\nXMLParser: "+xmlFileList.size()+" XML files with "+numIdeas+" ideas successfully parsed.");
		
		indexer.closeIndex();
		
	}
	
	public void addXMLFiles(File fileLocation){
		
		 if (!fileLocation.exists()) {
		      System.out.println("XMLParser: "+fileLocation + " does not exist.");
		 }
		  if (fileLocation.isDirectory()) {
		      for (File f : fileLocation.listFiles())
		    	  addXMLFiles(f);
		  } else{
		    	  String filename = fileLocation.getName().toLowerCase();
		    	 
		    	  /*
		    	  * Add XML files only
		    	  */
		 		 if (filename.endsWith(".xml"))
		 		     xmlFileList.add(fileLocation);
		  }
	}
	
	public void parseXMLFile(File xmlFile){
		
		System.out.println("XMLParser: Start Parsing XML file to indexer...");
		
		org.jdom.Document xmlDoc = null;	
		try {
			SAXBuilder b = new SAXBuilder(false);
			xmlDoc = b.build(xmlFile);
		} catch (JDOMException e) {
			e.printStackTrace();
		}catch (IOException io){
		}

		Element root = xmlDoc.getRootElement();
		try {
			ideasList = root.getChildren("idea");
			ideasIter = ideasList.iterator();
			
			Element ideaNode;
			String ideaTitle;
			String ideaCategory;
			String ideaDesc;
			
			/*
			 * Extract TITLE, CATEGORY and DESC from idea node
			 */
			while (ideasIter.hasNext()){
				numIdeas++;
				ideaNode = (Element) ideasIter.next();
				ideaTitle = ideaNode.getChild("title").getText();
				ideaCategory = ideaNode.getChild("category").getText();
				ideaDesc = ideaNode.getChild("description").getText();
				
				System.out.println("XMLParser: Parsing idea \""+ ideaTitle + "\" ...");
				
				/*
				 * Hand over idea to indexer
				 */
				indexer.addDocument(ideaTitle, ideaDesc);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
