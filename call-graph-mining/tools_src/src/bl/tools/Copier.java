package bl.tools;

import java.util.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Tool to identify associated tests within iBUGS repository.xml
 * (<code>testsforfix</code>). This test(s) is copied from the
 * post-fix version to the pre-fix version of one bug.
 * @author Christopher Oßner
 *
 */
public class Copier {
	
	/**
	 * Path to the failing tests - those we need to generate
	 */
	private static final String FP = "failing\\fix\\";
	
	/**
	 * @param args 1. path to Rhino`s post-fix tests
	 * (e.g. <iBUGS path>\output\<bug id>\post-fix\mozilla\js\tests),
	 * 2. bug id
	 * 3. path to iBUGS repository.xml
	 */
	public static void main(String args[]){
		Copier copier = new Copier();
		try {
			copier.exec(args[0], args[1], args[2]);
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
	}
	
	/**
	 * @param testdir path to Rhino`s post-fix tests
	 * (e.g. <iBUGS path>\output\<bug id>\post-fix\mozilla\js\tests)
	 * @param bugId Id of the bug to work with
	 * @param repositorypath path to iBUGS repository.xml
	 * @throws IOException
	 */
	public void exec(String testdir, String bugId, String repositorypath) throws IOException{
		RepositoryParser repParser = new RepositoryParser(repositorypath, bugId);
		HashSet<String> testsForFix = repParser.getTestsForFix();
		String filename;
		String outfilename;
		for( Iterator<String> iter = testsForFix.iterator(); iter.hasNext(); ){
			String test = iter.next();
			String test_temp = test.replace("/","\\");
			filename = testdir + "\\" + test_temp;
			test = test.substring(test.indexOf('/') +1);
			test = test.substring(test.indexOf('/') +1);
			test = test.replace("/","\\");
			outfilename = testdir.replace("post-fix", "pre-fix") + "\\" + FP;
			File outputFile = new File(outfilename);
			outputFile.mkdirs();
			outputFile = new File(outfilename + test);
			if(outputFile.createNewFile()){
				System.out.println("Copying...\n"
						   + "\t" + filename + "\n"
						   + "\t-> " + outfilename + test);
				File inputFile = new File(filename);
				if(!inputFile.exists())
					throw new IOException("File '" + inputFile + "' not found.");
				copyFile(inputFile, outputFile);
				File inParent = inputFile.getParentFile().getParentFile();
				File[] utilFiles = inParent.listFiles();
				int j = 0;
				for( int i = 0; i < utilFiles.length; i++){
					if(utilFiles[i].isDirectory())
						continue;
					String fileName = utilFiles[i].getAbsolutePath();
					File outUtil = ( new File( testdir.replace("post-fix",
											"pre-fix") + "\\" + FP ) ).getParentFile();
					outUtil = new File( outUtil.getAbsolutePath() +"\\"+ utilFiles[i].getName() );
					copyFile(  utilFiles[i], outUtil );
					System.out.println("\tCopied lib file: " + fileName +
										"\n\t-> " + outUtil.getAbsolutePath());
					j++;
				}
				System.out.println("Copied " +j+ " util file(s)");
			} else{
				System.out.println("\\"+FP+" already exists for the given bug.\n"+
				"Copying is skipped, as it is assumed, that it has beed done before!");
				return;
			}
		}
	}
	
	/**
	 * Helper to copy a file.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyFile(File in, File out) 
    throws IOException 
{
    FileChannel inChannel = new
        FileInputStream(in).getChannel();
    FileChannel outChannel = new
        FileOutputStream(out).getChannel();
    try {
        inChannel.transferTo(0, inChannel.size(),
                outChannel);
    } 
    catch (IOException e) {
        throw e;
    }
    finally {
        if (inChannel != null) inChannel.close();
        if (outChannel != null) outChannel.close();
    }
}

	
}
