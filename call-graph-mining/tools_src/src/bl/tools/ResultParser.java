package bl.tools;

import java.io.*;
import java.util.*;

/**
 * This class parses the HTML result that is written by jsDriver.pl.
 * The failed tests mentioned there are made available.
 * @author Christopher Oßner
 *
 */
public class ResultParser {
	
	/**
	 * Filename of the test results jsDriver.pl wrote.
	 */
	private String filename;
	private FileReader fr;
	private BufferedReader br;
	
	/**
	 * @param filename of the test results jsDriver.pl wrote
	 */
	public ResultParser(String filename){
		this.filename = filename;
	}
	
	/**
	 * Executes the parsing process.
	 * @return all filenames that are mentioned in jsDriver.pl`s output - the failed tests
	 */
	public HashSet<String> parseFailedTests(){
		init();
		String line = null;
		HashSet<String> res = new HashSet<String>(100);
		while(true){
			try{ line = br.readLine(); }
			catch(IOException e){ System.out.println("IOException: " + e.getMessage()); }
			if( line.matches("<h2>Retest List</h2>.*") ){
				while(true){
					try{ line = br.readLine().trim(); }
					catch(IOException e){ System.out.println("IOException: " + e.getMessage()); }
					int ind = line.indexOf("</pre>");
					if( ind != -1 ){
						line = line.substring(0, ind -1);
						res.add(line);
						try{ br.close(); }
						catch(IOException e){ System.out.println("IOException: " + e.getMessage()); }
						return res;
					}
					else if( line.charAt(0) != '#' )
						res.add(line);
				}
			}
		}
	}
	
	/**
	 * Initialize the IO stuff.
	 */
	private void init(){
		try{
			fr = new FileReader(filename);
		}
		catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}
		br = new BufferedReader(fr);
	}

}
