package bl.tools;

import java.io.*;
import java.util.*;

import javax.xml.stream.*;

import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;

/**
 * Some XML analyzing to get the right <code>testforfix</code> out of repository.xml
 * @author Christopher Oßner
 *
 */
public class RepositoryParser {
	
	private XMLInputFactory factory;
	private XMLStreamReader parser;
	
	/**
	 * Path to the failing tests - those we need to generate
	 */
	//private static final String failingPath = "failing/fix/";
	
	/**
	 * Associated tests are stored here
	 */
	private HashSet<String> testsForFix = new HashSet<String>();
	
	/**
	 * Associated fixed files are stored here.
	 */
	private HashSet<String> fixedFiles = new HashSet<String>();
	
	/**
	 * Id of the bug to work with
	 */
	private String bugno;
	
	/**
	 * @param location directory where iBUGS`s repository.xml is
	 * @param bugno Id of the bug to work with
	 */
	public RepositoryParser(String location, String bugno){
		this.bugno = bugno; 
		factory = XMLInputFactory.newInstance();
		try{
			parser = factory.createXMLStreamReader( new FileInputStream( location + "\\repository.xml" ) );
		} catch(IOException e){ System.out.println("IOException: " + e.getMessage()); }
		  catch(XMLStreamException xe){ System.out.println("XMLStreamException: " + xe.getMessage()); }
	}
	
	/**
	 * @return the filenames within <code>testsforfix</code>
	 */
	public HashSet<String> getTestsForFix(){
		parseTestsForFix();
		return testsForFix;
	}
	
	/**
	 * Currently not in active use - but still useful.
	 * @return the name of (in the post-fix version) changed files
	 */
	public HashSet<String> getFixedFiles(){
		try {
			parseFixedFiles();
		} catch (XMLStreamException e) {
			System.out.println("XMLStramException: " + e.getMessage());
		} catch (RuntimeException re) {
			System.out.println("RuntimeException: " + re.getMessage());
		}
		return fixedFiles;
	}
	
	/**
	 * Helper to catch all possible exceptions
	 */
	private void parseTestsForFix(){
		try {
			parse();
		} catch (XMLStreamException e) {
			System.out.println("XMLStramException: " + e.getMessage());
		} catch (XMLParseException pe) {
			System.out.println("XML Parse Exception: " + pe.getMessage());
		} catch (RuntimeException re) {
			System.out.println("RuntimeException: " + re.getMessage());
		}
	}
	
	/**
	 * Helper to extract the filenames of tests associated with the bug. Adds them to {@link #testsForFix}.
	 * @throws XMLStreamException
	 * @throws RuntimeException
	 * @throws XMLParseException
	 */
	private void parse() throws XMLStreamException, RuntimeException, XMLParseException{
		for( ; parser.hasNext(); parser.next()){
			if( foundTestForFix() ){
				for( ; parser.hasNext(); parser.next() ){
					if( parser.getEventType() == XMLStreamConstants.END_ELEMENT
						&& parser.getLocalName().equals("testsforfix") )
						return;
					else{
						if( parser.getEventType() == XMLStreamConstants.START_ELEMENT
							&& parser.getLocalName() == "test" ){
							for ( int i = 0; i < parser.getAttributeCount(); i++ ){
							   	if( parser.getAttributeLocalName(i) == "file" ){
							   		String test = parser.getAttributeValue(i).replace("mozilla/js/tests/", "");
							   		//test = test.substring(test.indexOf("/") +1);
							   		//test = test.substring(test.indexOf("/") +1);
							   		testsForFix.add( test );
							   	}
							}
						}
					}
				}
			}
		}
		throw new RuntimeException("BugId '" + bugno + "' has no associated Tests.");
	}
	
	/**
	 * Helper to localize the <code>testsforfix</code> tag
	 * @return true ifi the proper testForFix was found
	 */
	private boolean foundTestForFix(){
		if( parser.getEventType() == XMLStreamConstants.START_ELEMENT
			&& parser.getLocalName().equals("testsforfix") ){
			for ( int i = 0; i < parser.getAttributeCount(); i++ ){
		    	if( parser.getAttributeLocalName(i).equals("id")
		    		&& parser.getAttributeValue(i).equals(bugno.toString()) ){
		    		return true;}
			}
		}
		return false;
	}
	
	private boolean foundBug(){
		if( parser.getEventType() == XMLStreamConstants.START_ELEMENT
			&& parser.getLocalName().equals("bug") ){
			for ( int i = 0; i < parser.getAttributeCount(); i++ ){
		    	if( parser.getAttributeLocalName(i).equals("id")
		    		&& parser.getAttributeValue(i).equals(bugno) ){
		    		return true;}
			}
		}
		return false;
	}
	
	/**
	 * Parses for <code>fixedFiles</code> and adds them to {@link #fixedFiles}
	 * @throws XMLStreamException
	 * @throws RuntimeException
	 */
	private void parseFixedFiles() throws XMLStreamException, RuntimeException{
		for( ; parser.hasNext(); parser.next()){
			if( foundBug() ){
				for( ; parser.hasNext(); parser.next() ){
					if( parser.getEventType() == XMLStreamConstants.END_ELEMENT
						&& parser.getLocalName().equals("bug") )
						return;
					else{
						if( parser.getEventType() == XMLStreamConstants.START_ELEMENT
							&& parser.getLocalName() == "fixedFiles" ){
							for(; parser.hasNext(); parser.next() ){
								if(parser.getEventType() == XMLStreamConstants.START_ELEMENT
								   && parser.getLocalName() == "file"){
									for ( int i = 0; i < parser.getAttributeCount(); i++ ){
									   	if( parser.getAttributeLocalName(i) == "name" ){
									   		//System.out.println("attr name = " + parser.getAttributeValue(i));
									   		String test = parser.getAttributeValue(i).replace("mozilla/js/rhino/src/", "");
									   		test = test.replace('/', '.');
									   		fixedFiles.add( test );
									   	}
									}
								}
								if( parser.getEventType() == XMLStreamConstants.END_ELEMENT										&& parser.getLocalName() == "fixedFiles" )
									break;
							}
						}
					}
				}
			}
		}
		throw new RuntimeException("BugId '" + bugno + "' has no associated fixed file(s).");
	}
	
}
