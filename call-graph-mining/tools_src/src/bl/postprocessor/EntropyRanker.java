package bl.postprocessor;

import weka.attributeSelection.*;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.*;

/**
 * Wrapper for accessing the weka API
 * @author Christopher Oßner
 *
 */
public class EntropyRanker {
	
	/**
	 * Search option flag and search option class
	 */
	private static final String[] SEARCH_METHOD = {"-s", "weka.attributeSelection.Ranker"};
	
	/**
	 * The evaluation class
	 */
	private ASEvaluation evaluator = new InfoGainAttributeEval();
	
	/**
	 * All options needed later.
	 */
	final private String[] options = new String[4];
	
	/**
	 * Initializes the options
	 * @param inFile the ARFF to analyze
	 */
	public EntropyRanker(String inFile){
		{
			options[0] = "-i";
			options[1] = inFile;
			options[2] = SEARCH_METHOD[0];
			options[3] = SEARCH_METHOD[1];
		}
	}
	
	/**
	 * Used to write weka`s output to a file, as Windows knows no pipes... same as invoking:
	 * <code>
	 * weka.attributeSelection.InfoGainAttributeEval \
	 * -i args[0] \
	 * -s weka.attributeSelection.Ranker \
	 * > args[1]
	 * </code>
	 * on bash.
	 * @param args 1. input file (arff); 2. output file
	 */
	public static void main(String[] args){
		
		if(args.length != 2){
			System.out.println("Read the doc.");
			System.exit(1);
		}
		
		EntropyRanker er = new EntropyRanker(args[0]);
		
		try {
			er.exec(args[1]);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		System.out.println(er.toShortString());
	}
	
	/**
	 * Executes the ranking based on information gain. Writes the standart output
	 * of weka to the passed file.
	 * @param outFile output file
	 * @throws IOException
	 */
	public void exec(String outFile) throws IOException{
		String attrSel = null; 
		
		FileWriter outFw = null;
		outFw = new FileWriter(outFile);
		BufferedWriter outBw = new BufferedWriter(outFw);
		
		// c&p -ed from weka.ASEvaluation
		try {
			attrSel = AttributeSelection.SelectAttributes(evaluator, options.clone());
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
		
		outBw.write(attrSel);
		outBw.close();
		
		System.out.println("Wrote output of weka to '" + outFile + "'.");
	}
	
	/**
	 * Very Simple class to represent pairs of a score + it`s attribute name.
	 * @author Christopher Oßner
	 *
	 */
	public static class Result{
		final String attributeName;
		final double value;
		
		public Result(String name, double v){
			attributeName = name;
			this.value = v;
		}
		
		/**
		 * @return name of the attribute
		 */
		public String getAttributeName(){ return attributeName; }
		
		/**
		 * @return outcome of the analysis
		 */
		public double getScore(){ return value; }
		
		public String toString(){ return value + " " + attributeName; }
	}
	
	/**
	 * For more individual access to the result.
	 * @return all selected attributes sorted by their outcome. Index 0 (highest information gain)
	 * to lowest information gain.
	 */
	public EntropyRanker.Result[] getSelectedAttributes(){
	    Instances train = null;
	    ASSearch searchMethod = null;
	    AttributeSelection selector = new AttributeSelection();
	    double[][] attributes = null;
	    // strange thing but needed - weka modifies the options?
	    String[] newOpt = options.clone();
	    try {
			Utils.getOption('i', newOpt);
		} catch (Exception e1) {
			System.err.println(e1);
		}

	    try {
	    	DataSource source = new DataSource(options[1]);
		    train = source.getDataSet();
			AttributeSelection.SelectAttributes(evaluator, newOpt, train);
			searchMethod = ASSearch.forName(options[3], null );
		} catch (Exception e) {
			System.err.println(e);
		}
		selector.setEvaluator(evaluator);
	    selector.setSearch(searchMethod);
		
	    try {
			selector.SelectAttributes(train);
			attributes = selector.rankedAttributes();
		} catch (Exception e) {
			System.err.println(e);
		}
	    
		int l = attributes.length;
		EntropyRanker.Result[] res =  new EntropyRanker.Result[l];
		
		for(int i = 0; i < l; i++){
			res[i] = new EntropyRanker.Result( train.attribute((int) attributes[i][0] ).name(),
					 attributes[i][1]);
		}
		return res;
	}
	
	/**
	 * Short version of the results. Created by invoking {@link EntropyRanker.Result#toString()}
	 * on {@link #getSelectedAttributes()}.
	 * @return outcome, attribute name pairs
	 */
	public String toShortString(){
		EntropyRanker.Result[] results = getSelectedAttributes();
		String out = "";
		for(int i = 0; i < results.length; i++){
			out += results[i].toString() + "\n";
		}
		return out;
	}
	
	/**
	 * @return like weka prints, if invoked from command line
	 */
	public String toFullString(){
		String attrSel = null;
		
		// c&p -ed from weka.ASEvaluation
		try {
			attrSel = AttributeSelection.SelectAttributes(evaluator, options);
		} catch (Exception e) {
			String msg = e.toString().toLowerCase();
		    if ( (msg.indexOf("help requested") == -1)
		          && (msg.indexOf("no training file given") == -1) )
		    	e.printStackTrace();
		    System.err.println(e.getMessage());
		}
		
		return attrSel;
	}

}
