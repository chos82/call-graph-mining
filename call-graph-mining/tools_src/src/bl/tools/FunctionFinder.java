package bl.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Gather evaluation information by executing diff and looking up
 * hunks in post-fix file. The regular expression used to find a method signature is
 * "^(\s*\w+\s+)+\w+\s*\((\s*(\w+\s+)+\w+.*)*\)" (actually it is slightly different, as we evaluate
 * line wise).
 * @author CO
 * 
 */
// TODO make it work... well evaluation by hand would be still better...
public class FunctionFinder {
	
	class Match{
		private int lineNumber, hunkPos;
		private String hunk, signature;
		Match(String signature, int lineNumber){
			this.lineNumber = lineNumber; this.signature = signature;
		}
		public int getLineNumber(){ return lineNumber; }
		public String getHunk(){ return hunk; }
		public String getSignature(){ return signature; }
		public int getHunkPos(){ return hunkPos; }
		public int setHunkPos(int hunkPos){
			int old = this.hunkPos;
			this.hunkPos = hunkPos;
			return old;
		}
		public String setHunk(String hunk){
			String old = this.hunk;
			this.hunk = hunk;
			return old;
		}
		public String toString(){
			return "ENCLOSING FUNCTION:\n"+
			signature+" (line: "+lineNumber+")"+
			"\nFOR HUNK (starting at line "+hunkPos+"):"+
			"\n"+hunk+"\n";
		}
	}
	
	private final String cmd, file2;
	private static final String DIFF_CMD = "cmd /c diff "; // -c --show-function-line=^(\\s*\\w+\\s+)+\\w+\\s*\\((\\s*(\\w+\\s+)+\\w+.*)*\\).*\\{.*
	ArrayList<Match> encFunc = new ArrayList<Match>();
	ArrayList<ArrayList<String>> hunks = new ArrayList<ArrayList<String>>();
	
	/**
	 * @param file1 the 'old' (pre-fix) file
	 * @param file2 the 'new' (post-fix) file
	 */
	FunctionFinder(String file1, String file2){
		this.file2 = file2;
		cmd = DIFF_CMD + file1 + " " + file2 + " | gclip | pclip"; //" > c:\\diplo\\java_diff_test.txt";
	}

	/**
	 * @param args 1. file1 the 'old' (pre-fix) file;
	 * 2. file2 the 'new' (post-fix) file
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FunctionFinder ff = new FunctionFinder(args[0], args[1]);
		ff.exec();
	}
	
	public void exec() throws IOException{
		System.out.println("COMMAND:\n"+cmd);
		ArrayList<String> diffLines = execute(cmd);
		ArrayList<String> hunk = new ArrayList<String>();
		// gather diff output
		for( String line : diffLines ){
			line = line.trim();
			if( line.startsWith(">") ) hunk.add( line.substring(1) );
			else if( hunk.size() > 0 ){
				hunks.add(hunk);
				hunk = new ArrayList<String>();
			}
		}
		FileReader fr = null;
		try {
			fr = new FileReader(file2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String fileLine = null, hunkString = null, sig= null, currentHunkLine = null;
		int hunk_i = 0, hunkLine_i = 0, fileLine_i = 0, encFuncSize = 0;
		// lookup hunks in file
		while( true ){
			fileLine = br.readLine();
			if( fileLine == null || hunk_i >= hunks.size() ) break;
			fileLine_i++;
			// got a function
			if( fileLine.matches("^(\\s*\\w+\\s+)+\\w+\\s*\\((\\s*(\\w+\\s+)+\\w+.*)*\\)") ){
				sig = fileLine; hunkString = null;
				while( true ){
					if( fileLine.matches(".*\\{.*") ) break;
					fileLine = br.readLine();
					sig += fileLine;
					fileLine_i++;
				}
				sig = sig.substring( 0, sig.length() -1 );
				// found a hunk that does not belong to the last hunk`s enclosing function
				if( hunk_i >= encFuncSize ){
					encFunc.add( new Match(sig, fileLine_i) );
					encFuncSize++; }
				// had a function that contains no hunk - replace by new signature
				else
					encFunc.set(  hunk_i, new Match(sig, fileLine_i)  );
			}
			ArrayList<String> currentHunk = hunks.get(hunk_i);
			int currentHunkSize = currentHunk.size();
			currentHunkLine = currentHunk.get(hunkLine_i);
			// NO MATCH !?
			if( fileLine.indexOf( currentHunkLine.trim() ) != -1 ){
				hunkLine_i++; int hunkMatchStart = fileLine_i; hunkString = currentHunkLine;
				// check if potential match is really the hunk
				for( ; hunkLine_i < currentHunkSize; hunkLine_i++ ){
					fileLine = br.readLine();
					fileLine_i++;
					currentHunkLine = currentHunk.get(hunkLine_i);
					if( ! fileLine.equals( currentHunkLine ) )
						break;
					else{
						// got a hunk that is within the same function as the last hunk
						if( hunk_i >= encFuncSize )
							hunkString += "\n\t...\n";
						hunkString += currentHunkLine +"\n";
					}
				}
				hunkLine_i = 0;
				// really found the hunk
				if( fileLine_i - hunkMatchStart == currentHunkSize ){
					Match currentMatch = encFunc.get(hunk_i);
					currentMatch.setHunk(hunkString);
					currentMatch.setHunkPos(hunkMatchStart);
					encFunc.set( encFuncSize, currentMatch );
					hunk_i++;
				}
			}
		}
		for( Match m : encFunc )
			System.out.println(m);
	}
	
	/**
	 * Executes a command.
	 * @param cmd the command
	 * @return the stdout produced by the passed command
	 */
	private static ArrayList<String> execute(String cmd){
		Process p = null;
		ArrayList<String> out = null;
		try {
			out = new ArrayList<String>();
			String line;
			p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader
		          (new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null){ out.add(line); System.out.println(line); }
			input.close();
			p.waitFor();
		}
		catch (Exception e) {
			System.err.println("IOException while executing '" +cmd+"'. " +e);
			System.exit(1);
		}
		return out;
	}

}
