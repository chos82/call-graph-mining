package bl.tools;

import java.io.File;
import java.util.*;

/**
 * Tool to remove/rename tests, that can not be definitely be associated with a bug
 * (those which fail but are not mentioned in iBUGS`s repository.xml) in the
 * directory where the serialized graphs stored.
 * @author Christopher Oßner
 *
 */
public class Cleaner {
	
	/**
	 * change to true if failed tests, that are not bound to the bug should be deleted 
	 */
	private boolean deleteFailed = true;
	
	private static Cleaner cleaner;
	
	/**
	 * Path to failing tests
	 */
	private static final String failingTests = "failing/fix/";
	
	/**
	 * @param args 1. filename of the tests results HTML created by jsDriver.pl,
	 * 2. bug id,
	 * 3. path to iBUGS`s repository.xml
	 * 4. path to serialized graph objects
	 */
	public static void main(String args[]){
		cleaner = new Cleaner();
		cleaner.exec(args[0], args[1], args[2], args[3]);
	}
	
	/**
	 * @param file filename of the tests results HTML created by jsDriver.pl
	 * @param bugid Id of the bug to work with
	 * @param location path to iBUGS`s repository.xml
	 * @param graphdir graph DB
	 */
	void exec(String file, String bugid, String location, String graphdir){
		System.out.println("Test framework - results file: " + file);
		System.out.println("Bug ID: " + bugid);
		System.out.println("Graph DB: " + graphdir);
		ResultParser rp = new ResultParser(file);
		HashSet<String> failedTests = rp.parseFailedTests();
		RepositoryParser repParser = new RepositoryParser(location, bugid);
		HashSet<String> testsForFix = repParser.getTestsForFix();
		HashSet<String> unknownStatus = subtractLists(failedTests, testsForFix);
		processSerializedAdjacenceLists(graphdir, unknownStatus, testsForFix, deleteFailed );
	}
	
	/**
	 * Helper to subtract two HashSets from each other.
	 * @param subtractor Set to subtract from
	 * @param subtrahend Set to subtract
	 * @return subtractor - subtrahend
	 * @throws RuntimeException, if subtrahend is not contained in subtractor
	 */
	private HashSet<String> subtractLists(HashSet<String> subtractor,
										  HashSet<String> subtrahend) throws RuntimeException{
		String currentTest = null;
		for( Iterator<String> iter = subtrahend.iterator(); iter.hasNext(); ){
			currentTest = iter.next();
			currentTest = currentTest.substring(currentTest.indexOf("/") +1);
	   		currentTest = currentTest.substring(currentTest.indexOf("/") +1);
			if( subtractor.remove(failingTests + currentTest) ) ;
			else
				throw new RuntimeException("TestForFix '" + currentTest + "' is not within failed Tests.");
		}
		return subtractor;
	}
	
	/**
	 * Clean up is done here.
	 * @param location directory of the serialized graphs
	 * @param changeList tests to rename/remove
	 * @param testsForFix will be renamed to name + '_failed'
	 * @param deleteFailed if set to true, graphs in changeList will be removed,
	 * otherwise renamed to name + '_xxx'
	 */
	private void processSerializedAdjacenceLists(String location,
												 HashSet<String> changeList,
												 HashSet<String> testsForFix,
												 boolean deleteFailed){
		String[] entries = new File( location ).list();
		String filename, actual, variation;
		File file;
		int failed = 0, fix = 0;
		for( int i = 0; i < entries.length; i++ ){
			filename = entries[i];
			file = new File( location + "\\" + filename );
			filename = filename.replace(".ser", "");
			for(Iterator<String> iter = testsForFix.iterator(); iter.hasNext(); ){
				actual = iter.next();
				actual = actual.substring(actual.indexOf("/") +1);
				actual = actual.substring(actual.indexOf("/") +1);
				actual = failingTests + actual;
				actual = actual.replace("/", "_");
				variation = filename.substring(0, filename.length() -9);
				variation += ".js";
				if( actual.equals(filename) || actual.equals(variation) ){
					file.renameTo( new File( location + "\\" + filename + "_failed.ser" ) );
					fix++;
				}
			}
		}
		
		entries = new File( location ).list();
		for( int i = 0; i < entries.length; i++ ){
			filename = entries[i];
			file = new File( location + "\\" + filename );
			filename = filename.replace(".ser", "");
			for(Iterator<String> iter = changeList.iterator(); iter.hasNext(); ){
				if( iter.next().replace("/", "_").equals(filename) ){
					if( deleteFailed )
						file.delete();
					else
						file.renameTo( new File( location + "\\" + filename + "_xxx.ser" ) );
					failed++;
				}
			}
		}
		
		if( deleteFailed )
			System.out.println("Deleted " + failed + " failed Test(s).");
		else
			System.out.println("Renamed " + failed + " failed Test(s).");
		System.out.println("Renamed " + fix + " Test(s) for fix.");
	}
	
}
