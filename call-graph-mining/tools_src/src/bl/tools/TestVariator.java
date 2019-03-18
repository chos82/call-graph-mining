package bl.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Retrieves all files with in a directory (test variations) (n). The content of each
 * file is copied and inserted at each file within another directory (tests supposed to fail) (m)
 * and stored there, so that n*m files will be created. Ensure that tests fail just for the part that is
 * known to be buggy (the m part) and not for the stuff we added (the n part)!
 * @author Christopher Oßner
 *
 */
public class TestVariator {

	private String testsDir = null;
	
	private static final String FP = "failing\\fix\\";
	
	private String varDir = null;
	
	public TestVariator(String testsDir, String varDir){
		this.testsDir = testsDir;
		this.varDir = varDir;
	}
	
	/**
	 * 
	 * @param args 1. path to tests (of a iBUGS Rhino version);
	 * 2. path to variations (javascript used to modify testForFix)
	 */
	public static void main(String[] args){
		TestVariator tv = new TestVariator(args[0], args[1]);
		try {
			tv.exec();
		} catch (IOException e) {
			System.err.println("Error genarating test variations: " +e);
			System.exit(1);
		} catch (RuntimeException e) {
			System.err.println("Error genarating test variations: " +e);
			System.exit(1);
		}
	}
	
	public void exec() throws IOException, RuntimeException{
		File inDir = new File(varDir);
		String[] variations = inDir.list();
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null; 
		File file = new File(testsDir + "\\" + FP);
		String[] ex = file.list();
		for(int i = 0; i < ex.length; i++){
			if(ex[i].matches(".*var.*")){
				System.out.println("Found variation in: " + testsDir + "\\" + FP + 
						"\nTest variation is skipped, as it is assumed that it has been done before.");
				return;
			}
		}
		String[] baseTest = file.list();
		DecimalFormat df = new DecimalFormat( "00" );
		for(int j = 0; j < baseTest.length; j++){
			System.out.println("Base test is: " + baseTest[j]);
			file = new File(testsDir + "\\" + FP + baseTest[j]);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String base = "";
			String line = null;
			while(true){
				line = br.readLine();
				if(line == null)
					break;
				base += line + "\n";
			}
			br.close();
			int insertPos = base.indexOf("test();") + 7;
			int i = 0;
			for( ; i < variations.length; i++){
				file = new File(varDir + "\\" + variations[i]);
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				String out = null, var = "";
				while(true){
					line = br.readLine();
					if(line == null)
						break;
					var += line + "\n";
				}
				br.close();
				out = base.substring(0, insertPos) + "\n" + var + 
									base.substring(insertPos +1, base.length());
				file = new File(testsDir + "\\" + FP + "\\" + 
								baseTest[j].substring(0, baseTest[j].length() -3) +
								"_var" + df.format(i +1) + ".js");
				fw = new FileWriter(file);
				bw = new BufferedWriter(fw);
				bw.write(out);
				bw.close();
			}
			System.out.println("Created " + (i ) + " variations of that test.");
		}
	}
	
}
