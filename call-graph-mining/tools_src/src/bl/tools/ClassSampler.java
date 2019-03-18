package bl.tools;

import java.util.*;
import java.io.*;

import ml.options.OptionSet;
import ml.options.Options;

/**
 * Generates a list file of sampled classes.
 * @author CO
 *
 */
public class ClassSampler {
	
	/**
	 * 
	 * @param args See usage printed on stdout
	 */
	public static void main(String[] args){
		Options opt = new Options(args);
		opt.getSet().addOption("ibugs", 
				Options.Separator.BLANK, Options.Multiplicity.ONCE).addOption("o",
				Options.Separator.BLANK, Options.Multiplicity.ONCE).addOption("Id",
				Options.Separator.EQUALS, Options.Multiplicity.ONCE).addOption("prefix",
				Options.Separator.EQUALS, Options.Multiplicity.ONCE).addOption("n", 
				Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE).addOption("v",
				Options.Multiplicity.ZERO_OR_ONE).addOption("all",
				Options.Multiplicity.ZERO_OR_ONE);
		
		OptionSet set = opt.getMatchingSet(false, false);
		
		if (set == null) {
			System.out.println("Usage:\n---------------------\n" +
					"\t-ibugs \t path of the iBUGS suite (and it`s repository.xml)\n"+
					"\t-o \t output file\n"+
					"\t-Id= \t Id of the bug\n"+
					"\t-prefix= \t the package name\n"+
					"\t-n= \t number of files (classes) to be included\n"+
					"\t[-v] \t verbose mode (print selected classes)");
			System.out.println("\nThe options library says:\n" + opt.getCheckErrors());
			System.exit(1);
		}
		
		//evaluate options
		String ibugs = set.getOption("ibugs").getResultValue(0);
		System.out.println("Input directory: " + ibugs);
		String id = set.getOption("Id").getResultValue(0);
		System.out.println("Bug Id: " + id);
		String prefix = set.getOption("prefix").getResultValue(0);
		System.out.println("prefix: " + prefix);
		int n = 0;
		if(set.isSet("n"))
			n = Integer.parseInt( set.getOption("n").getResultValue(0) );
		System.out.println("numer of .java files: " + n);
		String of = set.getOption("o").getResultValue(0);
		System.out.println("Output file: " + of);
		RepositoryParser rp = new RepositoryParser(ibugs, id);
		HashSet<String> fixedFiles = rp.getFixedFiles();
		String dir = ibugs + "\\output\\" + id +
		 			 "\\pre-fix\\mozilla\\js\\rhino\\src\\" +
		 			 prefix.replace('.', '\\');
		System.out.println("Scanning for .java files in: " + dir);
		boolean sample = true;
		if(set.isSet("all"))
			sample = false;
		ArrayList<String> files = javaFilesInDir(dir, n - fixedFiles.size(), sample );
		boolean v = false;
		if(set.isSet("v")){
			System.out.println("Verbose mode!");
			v = true;
		}
		
		FileWriter fw = null;
		File file = new File(of);
		if(file.exists()){
			System.out.println("File exists: " +of +"\nTerminated as writing to this file could cause data loss!");
			System.exit(1);
		}
		try {
			file.createNewFile();
			fw = new FileWriter(file);
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			System.exit(1);
		}
		BufferedWriter bw = new BufferedWriter(fw);
		
		if(v)
			System.out.println("\nSelected classes:\n-----------------------\n");
		
		String line = null;
		//write fixedFiles
		if(! set.isSet("all")){
			for( Iterator<String> iter = fixedFiles.iterator(); iter.hasNext(); ){
				line = iter.next();
				line = line.substring(0, line.length() - 5);
				if(v)
					System.out.println(line + "\t<fixedFiles>");
				try {
					bw.write(line + "\n");
				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
					System.exit(1);
				}
			}
		}
		
		//write sampled files
		for(Iterator<String> iter = files.iterator(); iter.hasNext(); ){
			line = iter.next();
			line = line.substring(0, line.length() - 5);
			if(v)
				System.out.println(line);
			try {
				bw.write(prefix + "." + line + "\n");
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				System.exit(1);
			}
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			System.exit(1);
		}
		
	}
	
	public static ArrayList<String> javaFilesInDir(String dir, int no, boolean sample){
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		String[] entries = new File(dir).list();
		for(int i = 0; i < entries.length; i++){
			if( entries[i].endsWith(".java"))
				temp.add(entries[i]);
		}
		int s = temp.size();
		System.out.println("Found "+s+" .java files.");
		Random r = new Random();
		double x = (double) no / s;
		for(int i = 0; i < s; i++){
			double rnd = r.nextDouble();
			if( ! sample || rnd <= x )
				list.add(temp.get(i));
		}
		System.out.println("Wrote "+list.size()+" .java files to list.");
		return list;
	}

}
