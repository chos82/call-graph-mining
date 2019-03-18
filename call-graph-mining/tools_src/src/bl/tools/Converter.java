package bl.tools;

import bl.postprocessor.Edge;
import callgraph.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import ml.options.*;

/**
 * Tool to read in the serialized graph objects, and convert them
 * to a different hierarchy level and print them into a LG graph DB.
 * Internally calls {@link callgraph.GraphConverter}
 * @author Christopher Oßner
 *
 */
public class Converter {
	
	/**
	 * Indicates if weights should be written to textual
	 * graph files (LG). Set to true if extended LG is wanted.
	 */
	public boolean writeWeights = false; 
	
	/**
	 * Set true if constructors should be omitted
	 */
	private boolean skipConstructors = false;
	
	/**
	 * Read in graph object
	 */
	private AdjacenceList input = null;
	
	/**
	 * Converted graph object
	 */
	private AdjacenceList output = null;
	
	/**
	 * The directory to work with.
	 */
	private String inputDir = null;
	
	/**
	 * Name of the output file
	 */
	private String outputFile = null;
	
	private GraphReader gr = new GraphReader();
	private GraphWriter gw;
	
	/**
	 * Instance of the class doing the work...
	 */
	private GraphConverter conv;
	
	/**
	 * Indicates if dummies should be written.
	 */
	private boolean includeDummies = false;

	private boolean includeJre = false;
	
	/**
	 * Signatures to include in not package-level graphs (those
	 * mining on parent-level assumes to be interesting)
	 */
	private static HashSet<Signature> includedParents = null;
	
	/**
	 * For mining on class level we pick out specific
	 * classes (as 78 calsses in org.mozilla.javascript are too much
	 * for graph-mining)
	 */
	private static HashSet<Signature> includedClasses = null;
	
	/**
	 * The level we want.
	 * @author CO
	 */
	public static enum Level {PACKAGE, CLASS, METHOD, ALL};
	
	/**
	 * Default is all: All the information we wrote during execution of Rhino.
	 */
	private static Level level = Level.ALL;;
	
	/**
	 * 
	 * @param inputDir DB of serialized graphs
	 * @param outputFile the LG
	 * @param writeWeights include weights to the LG (no 'real' LG anymore)
	 * @param includeDummies write dummy vertices to LG?
	 * @param skipConstructors do not write constructor signatures to LG
	 */
	public Converter(String inputDir, String outputFile,
			boolean writeWeights, boolean includeDummies,
			boolean skipConstructors, boolean includeJre){
		this.inputDir = inputDir;
		this.outputFile = outputFile;
		this.writeWeights = writeWeights;
		this.includeDummies = includeDummies;
		this.skipConstructors = skipConstructors;
		this.includeJre  = includeJre;
	}
	
	/**
	 * @param args see usage information on stdout
	 */
	public static void main(String[] args){
		Options opt = new Options(args);
		opt.addSet("package").addOption("package");
		opt.addSet("class", 1, 10).addOption("class").addOption("classList", Options.Separator.EQUALS,
				Options.Multiplicity.ZERO_OR_ONE);
		opt.addSet("method", 1, 10).addOption("method");
		opt.addSet("all").addOption("all");
		opt.addOptionAllSets("i", Options.Separator.BLANK, Options.Multiplicity.ONCE);
		opt.addOptionAllSets("o", Options.Separator.BLANK, Options.Multiplicity.ONCE);
		opt.addOptionAllSets("writeWeights", Options.Multiplicity.ZERO_OR_ONE);
		opt.addOptionAllSets("includeDummies", Options.Multiplicity.ZERO_OR_ONE);
		opt.addOptionAllSets("skipConstructors", Options.Multiplicity.ZERO_OR_ONE);
		opt.addOptionAllSets("includeJre", Options.Multiplicity.ZERO_OR_ONE);
		OptionSet set = opt.getMatchingSet(true, false);
		
		if (set == null) {
			System.out.println("Usage:\n---------------------\n" +
					"Hierarchy levels:\n"+
					"\t-package"+
					"\n\t-class \t [-classList=<list file with classes to include>] package1 [package2] [..] [package10]"+
					"\n\t-method" +
					"\n\t-all \t can be used for debug purposes\n"+
					"\nNeeded for all levels:\n"+
					"\t-i \t input directory (serialized graphs on method level)\n"+
					"\t-o \t output direcotry (LG will have same name)\n"+
					"\nValid options for all levels:\n"+
					"\t-writeWeights \t if set, weights will be written to the LG file\n"+
					"\t-includeDummies \t if set the dummy signatures will be written to textual output (they are written to the objects anyway)\n"+
					"\t-includeJre \t set if JRE calls should be written to LG"+
					"\t-skipConstructors \t set if constructors should be omitted by the converter");
			System.out.println("\nThe options library says:\n" + opt.getCheckErrors());
			System.exit(1);
		}
		
		//evaluate options
		String inf = set.getOption("i").getResultValue(0);
		System.out.println("Input directory: " + inf);
		String of = set.getOption("o").getResultValue(0);
		System.out.println("Output directory: " + of);
		boolean id = false, ww = false, sc = false, jre = false;
		
		if(set.isSet("writeWeights")){
			System.out.println("Call frequency will be written to LG file.");
			ww = true;
		}
		if(set.isSet("includeDummies")){
			System.out.println("Will write dummy vertices to LG file.");
			id = true;
		}
		if(set.isSet("skipConstructors")){
			System.out.println("Constructors will be omitted.");
			sc = true;
		}
		if(set.isSet("includeJre")){
			System.out.println("Will write JRE calls to LG.");
			jre = true;
		}
		
		Converter con = new Converter(inf, of, ww, id, sc, jre);
		
		if (set.getSetName().equals("package")){
			System.out.println("Converting to package...");
			level = Level.PACKAGE;
		}
		else if (set.getSetName().equals("class")){
			level = Level.CLASS;
			System.out.println("Converting to classes...\nIncluding the following package(s):");
			ArrayList<String> data = set.getData();
			setParentLevelIncludes(data);
			for (String d : data)
				System.out.println("\t" + d);
			if (set.isSet("classList")){
				String iv = set.getOption("classList").getResultValue(0);
				System.out.println("Classes in '" + iv + "' will be included.");
				try {
					includedClasses = ListReader.readList2SigantureSet(iv);
				} catch (IOException e) {
					System.out.println("IOException: " + e.getMessage());
					System.exit(1);
				}
			}
		} else if(set.getSetName().equals("method")){
			System.out.println("Converting to methods...\nIncluding the following class(es):");
			level = Level.METHOD;
			ArrayList<String> data = set.getData();
			setParentLevelIncludes(data);
			for (String d : data)
				System.out.println("\t" + d);
		} else
			System.out.println("Writing all graphs on method level to output file...");
		con.exec(level);
	}
	
	/**
	 * E.g. set packages to consider when mining on class level.
	 * @param packages
	 */
	public static void setParentLevelIncludes(ArrayList<String> packages){
		includedParents = new HashSet<Signature>();
		for(Iterator<String> iter = packages.iterator(); iter.hasNext(); ){
			String sig = iter.next();
			includedParents.add(  new Signature( Edge.detectSignatureType(sig), sig )  );
		}
	}
	
	/**
	 * Trigger the whole thing...
	 * @param level
	 */
	public void exec(Level level){
		String graphname = null;
		String[] entries = ( new File(  inputDir  ) ).list();
		int wrote = 0;
		if( (new File(outputFile)).exists()){
			System.out.println("Output directory already exists: " +outputFile+
					"\nTerminated, since using this dir could cause data loss!");
			System.exit(1);}
		for( int i = 0; i < entries.length; i++ ){
			graphname = entries[i];
			if( !graphname.substring(graphname.length() -8).equals("_xxx.ser") ){
				input = gr.readGraph(inputDir + "\\" + graphname);
				graphname = graphname.substring(0, entries[i].length() - 4);
				conv = new GraphConverter(input);
				if(skipConstructors)
					conv.omitConstructors();
				wrote++;
				//package
				if(level == Level.PACKAGE){
					output = conv.methods2Package();
				} else if(level == Level.CLASS){ //class
					if(includedClasses != null)
						conv.includeVertices(includedClasses);
					output = conv.methods2class(includedParents);
				} else if(level == Level.METHOD){ // method
					output = conv.methods(includedParents);
				} else{ // all
					gw = new GraphWriter(input, outputFile, graphname);
					gw.writeR_w_total2lg(writeWeights, !includeDummies, !includeJre);
					continue;
				}

				gw = new GraphWriter(output, outputFile, graphname);
				gw.writeR_w_total2lg(writeWeights, !includeDummies, !includeJre);
				gw.writeAdjacence();
			}
		}
		System.out.println("Wrote " + wrote + " graph(s).");
	}

}
