package bl.tools;

import java.util.*;

import bl.postprocessor.Edge;
import callgraph.Signature;
import java.io.*;


/**
 * Simple file reader.
 * @author Christopher Oßner
 *
 */
public class ListReader {
	
	/**
	 * List files to a HashSet of {@link callgraph.Signature}.
	 * @param file the input file
	 * @return each line of the input becomes a Signature
	 * @throws IOException
	 */
	public static HashSet<Signature> readList2SigantureSet(String file) throws IOException{
		HashSet<Signature> out = new HashSet<Signature>();
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while(true){
			line = br.readLine();
			if(line == null)
				break;
			out.add( new Signature(Edge.detectSignatureType(line), line) );
		}
		
		return out;
	}

}
