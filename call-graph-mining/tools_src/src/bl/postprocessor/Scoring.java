package bl.postprocessor;

import java.text.DecimalFormat;
import java.util.*;

import bl.postprocessor.SplitVertices.EmbeddedFragment;

import ml.options.OptionSet;
import ml.options.Options;
/**
 * Produces the scoring out of an ARFF, which is passed to {@link EntropyRanker}.
 * Documentation on different scores see Eichinger et al.
 * @author Christopher Oßner
 *
 */
public class Scoring {
	
	private static final DecimalFormat df = new DecimalFormat( "0.00000" );
	
	/**
	 * To represent the output of the entropy ranking and the supp(m, sg_f) structural score:
	 */
	private ArrayList<Score> combinedScores = null;
	
	/**
	 * To represent the output of the entropy ranking and the supp(m, sg_failCorr) structural score:
	 */
	private ArrayList<Score> combinedCFScores = null;
	
	/**
	 * The entropy score before normalization
	 */
	private ArrayList<Double> entropyScores = null;
	
	/**
	 * To represent the output of the entropy ranking and M. Huber`s structural score.
	 */
	//private ArrayList<Score> combinedSimpleScores = new ArrayList<Score>();
	
	/**
	 * Each vertex without respect to it`s context is held here.
	 */
	private ArrayList<String> vertexSet = new ArrayList<String>();
	
	/**
	 * weka results as returned by {@link  #parseResults(bl.postprocessor.EntropyRanker.Result[])}
	 * TODO ensure that this var is filled before methods are executed
	 */
	private ArrayList<Result> results = new ArrayList<Result>();

	/**
	 * The output of ParSeMis.
	 */
	private String inFile;
	
	private ArffWriter arffWriter;
	
	/**
	 * Edges supposed to be follow up bugs. Retrieved from {@link FollowUpDetector#getFollowUpBugs()}.
	 */
	private ArrayList<Result> followUpBugs;
	
	/**
	 * Used as indicator, which field of {@link Score} is ordered, if Scores are retrieved
	 * by {@link Scoring#getRanking()}
	 * @author Christopher Oßner
	 *
	 */
	public enum ScoreComperator {COMBINED, ENTROPY, STRUCTURAL}
	
	private int failedTests;
	private int passedTests;
	
	/**
	 * Attributes of results provided by {@link EntropyRanker#getSelectedAttributes()}
	 * hold just an attribute String. Here information of this String will be separated.
	 * @author Christopher Oßner
	 *
	 */
	public static class Result{
		private double value;
		private String fragment;
		private String startVertex;
		private String endVertex;
		private String annotationType; 
		public Result(EntropyRanker.Result res){
			value = res.getScore();
			String name = res.getAttributeName();
			int fragEndInd = name.indexOf(":");
			fragment = name.substring(0, fragEndInd );
			int annotEndInd = name.indexOf('(');
			annotationType = name.substring(fragEndInd +1, annotEndInd );
			int edgeSplitInd;
			edgeSplitInd = name.indexOf("->");
			startVertex = name.substring(annotEndInd +1, edgeSplitInd );
			endVertex = name.substring(edgeSplitInd +2, name.length() );
		}
		// only for test purposes
		public Result(String start, String end, double v){
			this.startVertex = start; this.endVertex = end; this.value = v;
		}
		public double getValue(){ return value; }
		public String getFragment(){ return fragment ;}
		public String getStartVertex(){ return startVertex; }
		public String getEndVertex(){ return endVertex; }
		public String getAnnotationType(){ return annotationType; }
		@Override
		public boolean equals(Object o){
			if(o instanceof Result && 
				this.startVertex.equals( ( (Result) o ).getStartVertex() ) &&
				this.endVertex.equals( ( (Result) o ).getEndVertex() ) )
			return true;
			return false;
		}
	}
	
	/**
	 * Represents the final score.
	 * @author Christopher Oßner
	 *
	 */
	public static class Score{
		private double combinedScore;
		private double structuralScore;
		private double entropyScore;
		String vertex;
		
		/**
		 * 
		 * @param combined as returned from {@link Scoring#combineRankings(ArrayList, ArrayList)}
		 * @param structural as returned
		 * @param entropy
		 * @param vertex
		 */
		public Score(double combined, double structural, double entropy, String vertex){
			this.combinedScore = combined;
			this.structuralScore = structural;
			this.vertex = vertex;
			this.entropyScore = entropy;
		}
		
		public String toString(){
			DecimalFormat df = new DecimalFormat( "0.00000" );
			return df.format(combinedScore) + "    " + 
			   df.format(entropyScore) + "         " + 
			   df.format(structuralScore) + "                 " + 
			   vertex + "\n";
		}

	}
	
	/**
	 * @param fragments the output of ParSeMis
	 * @param serData the serialized graphs
	 * @param arff where to store the ARFF (for WEKA)
	 * @param reincludeDummies if dummies are omitted before, they can be re-included by passing true
	 */
	private Scoring(String fragments, String serData, String arff, boolean reincludeDummies, boolean reincludeJre){
		inFile = fragments;
		arffWriter = new ArffWriter(fragments, arff, serData, reincludeDummies, reincludeJre);
		arffWriter.exec();
		EntropyRanker er = new EntropyRanker(arff);
		EntropyRanker.Result[] tempRes = er.getSelectedAttributes();
		FollowUpDetector fud = new FollowUpDetector(results);
		followUpBugs = fud.getFollowUpBugs();
		results = fud.remove();
		results = parseResults( tempRes );
		vertexSet = unifyVertices(results);
		GraphCounter gc = new GraphCounter(serData);
		failedTests = gc.getNoFailed();
		passedTests = gc.getNoCorr();
	}
	
	/**
	 * @param args is parsed with Options lib.
	 */
	public static void main(String[] args){
		Scoring scoring = Scoring.getInstance(args);
		scoring.exec();
		System.out.println( scoring.toString() );
	}
	
	/**
	 * Factory Class
	 * @param args parsed with Options lib. See usage on stdout...
	 * @return an instance
	 */
	public static Scoring getInstance(String[] args){
		Options opt = new Options(args);
		opt.getSet().addOption("i", Options.Separator.BLANK,
							   Options.Multiplicity.ONCE).addOption("arff", Options.Separator.BLANK,
							   Options.Multiplicity.ONCE).addOption("ser", Options.Separator.BLANK,
							   Options.Multiplicity.ONCE).addOption("reincludeDummies",
							   Options.Multiplicity.ZERO_OR_ONE).addOption("reincludeJre",
							   Options.Multiplicity.ZERO_OR_ONE);
		OptionSet set = opt.getMatchingSet(false, false);
		
		if (set == null) {
			System.out.println("Usage:\n---------------------\n" +
					"\t-i \t the fragments file (ParSeMiS)\n"+
					"\t-arff \t output ARFF file (needed for entropy based scoring in WEKA)\n"+
					"\t-ser \t graphDB path to the serialized graph objects that were "+
					"used to create the fragments file\n"+
					"\t[-reincludeDummies] \t if dummy vertices were ommited before, the can be re-included"+
					"\t[-reincludeJre] \t inclcude vertices representing JRE calls");
			System.out.println("\nThe options library says:\n" + opt.getCheckErrors());
			System.exit(1);
		}
		
		//evaluate options
		String inf = set.getOption("i").getResultValue(0);
		System.out.println("Input fragments: " + inf);
		String arff = set.getOption("arff").getResultValue(0);
		System.out.println("Output file: " + arff);
		String ser = set.getOption("ser").getResultValue(0);
		System.out.println("Used DB: " + ser);
		boolean rid = false, rij = false;
		if(set.isSet("reincludeDummies")){
			System.out.println("Dummies will be re-included.");
			rid = true;
		}
		if(set.isSet("reincludeJre")){
			System.out.println("JRE calls will be re-included.");
			rij = true;
		}
		
		return new Scoring(inf, ser, arff, rid, rij);
	}
	
	public void exec(){
		entropyScores = entropyRanking();
		//ArrayList<Double> simpleStructuralScores = scoring.simpleStructuralRanking();
		SplitVertices sv = new SplitVertices(inFile);
		HashMap<String, HashSet<String>> failing = sv.getFailing();
		ArrayList<Double> structuralScores = structuralScoring(failing);
		//scoring.combinedSimpleScores = scoring.combineRankings(simpleStructuralScores, entropyScores);
		ArrayList<Double> structuralCFScores = structuralCFRanking(sv);
		combinedCFScores = combineRankings(structuralCFScores, entropyScores);
		combinedScores = combineRankings(structuralScores, entropyScores);
	}
	
	public ArrayList<Result> getFollowUpBugs(){
		return followUpBugs;
	}
	
	public String toStringFollowUpBugs(){
		String out = "Edges removes as supposed to belong to a follow-up bug:\n";
		if(followUpBugs.size() == 0)
			out += "NONE\n";
		for(Result fub : followUpBugs){
			out += fub.getStartVertex() + "->" + fub.getEndVertex() + "\n";
		}
		return out;
	}
	
	public String toString(){
		return		 this.toStringCombined()+"\n" +
					 this.toStringCFombined()+"\n" +
					 this.toStringEntropy()+"\n" +
					 this.toStringEntropyScoresNotNormalized()+"\n" +
					 this.toStringStructural()+"\n" +
					 this.toStringCFstructural()+"\n";
	}
	
	public String toStringCombined(){
		if(combinedScores == null)
			exec();
		String out = "P_FAIL Ranking:\n"+
				     "Combined   Entropy Score   Structural" +
			 	   "\n--------   -------------   ----------\n";
		for(int i = combinedScores.size() -1; i >= 0; i--){
			out += df.format(combinedScores.get(i).combinedScore) + "    " +
				   df.format(combinedScores.get(i).entropyScore) + "         " + 
				   df.format(combinedScores.get(i).structuralScore) + "   " + 
				   combinedScores.get(i).vertex + "\n";
			
		}
		return out;
	}
	public String toStringEntropy(){
		if(combinedScores == null)
			exec();
		String out = "Entropy Score" +
	 	           "\n-------------\n";
		ArrayList<Score> temp = this.getRanking(ScoreComperator.ENTROPY);
		for(int i = temp.size() -1; i >= 0; i--){
			out += df.format(temp.get(i).entropyScore) + "      " +
				   temp.get(i).vertex + "\n";
		}
		return out;
	}
	public String toStringEntropyScoresNotNormalized(){
		if(entropyScores == null)
			exec();
		int i = 0;
		String out = "Entropy Score NOT normalized:\n"+
					 "-----------------------------\n";
		for( Double value : entropyScores ){
			out += df.format(value) + "   " + vertexSet.get(i) +"\n";
			i++;
		}
		return out;
	}
	public String toStringStructural(){
		if(combinedScores == null)
			exec();
		String out = "P_FAIL Score" +
	 	           "\n------------\n";
		ArrayList<Score> temp = this.getRanking(ScoreComperator.STRUCTURAL);
		for(int i = temp.size() -1; i >= 0; i--){
			out += df.format(temp.get(i).structuralScore) + "     " +
				   temp.get(i).vertex + "\n";
		}
		return out;
	}
	public String toStringCFstructural(){
		if(combinedCFScores == null)
			exec();
		String out = "P_FAIL-CORR Score" +
	 	           "\n-----------------\n";
		ArrayList<Score> temp = this.getCFRanking(ScoreComperator.STRUCTURAL);
		for(int i = temp.size() -1; i >= 0; i--){
			out += df.format(temp.get(i).structuralScore) + "          " +
				   temp.get(i).vertex + "\n";
		}
		return out;
	}
	public String toStringCFombined(){
		if(combinedCFScores == null)
			exec();
		String out = "P_FAIL-CORR Ranking:\n"+
				     "Combined   Entropy Score   Structural" +
			 	   "\n--------   -------------   ----------\n";
		for(int i = combinedCFScores.size() -1; i >= 0; i--){
			out += df.format(combinedCFScores.get(i).combinedScore) + "    " +
				   df.format(combinedCFScores.get(i).entropyScore) + "         " + 
				   df.format(combinedCFScores.get(i).structuralScore) + "   " + 
				   combinedScores.get(i).vertex + "\n";
			
		}
		return out;
	}
	
	public String toStringFragmentStatistics(){
		return arffWriter.toStringStatistics();
	}
	
	/**
	 * Converts input to more convenient result representation. Only scores greater than 0 are considered.
	 * @param input information gain results as produced by {@link EntropyRanker#getSelectedAttributes()}
	 * @return converted input in same order
	 */
	private ArrayList<Result> parseResults( EntropyRanker.Result[] input ){
		ArrayList<Result> output = new ArrayList<Result>();
		for(int i = 0; i < input.length; i++){
			if(input[i].getScore() > 0.0)
				output.add(new Result(input[i]));
		}
		return output;
	}
	
	/**
	 * @return a set out of {@link #results} where each start (non-leaf) vertex, 
	 * without respect to it`s context, is included just once.
	 */
	private ArrayList<String> unifyVertices(ArrayList<Result> results){
		ArrayList<String> vertexSet = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();
		for(int i = 0; i < results.size(); i++){
			String vertex = results.get(i).getStartVertex();
			if( set.add(vertex) )
				vertexSet.add(vertex);
		}
		return vertexSet;
	}
	
	/**
	 * @return the maximum of each information gain raking for each vertex,
	 * the same order as {@link #vertexSet}. 
	 */
	private ArrayList<Double> entropyRanking(){
		ArrayList<Double> entropyScores = new ArrayList<Double>();
		for(int i = 0; i < vertexSet.size(); i++){
			String vertex = vertexSet.get(i);
			double max = 0;
			for(int j = 0; j < results.size(); j++){
				Result currentResult = results.get(j);
				String startVertex = currentResult.getStartVertex();
				double value = currentResult.getValue();
				if(value > max && startVertex.equals(vertex)){
					max = value;
				}
			}
			entropyScores.add(max);
		}
		return entropyScores;
	}
	
	/*
	 * Simply counts how often one vertex is contained (as start of an edge)
	 * in all fragments.
	 * @param results the fragments representations returned by
	 * {@link #parseResults(postprocessor.EntropyRanker.Result[])}
	 * @return the counts for each vertex - same order as  {@link #vertexSet}.
	 */
	/*private ArrayList<Double> simpleStructuralRanking() {
		ArrayList<Double> simpleStructuralScores = new ArrayList<Double>();
		for (int i = 0; i < vertexSet.size(); i++) {
			int count = 0;
			String vertex = vertexSet.get(i);
			for (int j = 0; j < results.size(); j++) {
				String startVertex = results.get(j).getStartVertex();
				if (vertex.equals(startVertex)) {
					count++;
				}
			}
			simpleStructuralScores.add( (double) count );
		}
		return simpleStructuralScores;
	}*/
	
	/**
	 * Support of vertices in fragments, that occur in failing executions only
	 * ( supp(m, sg_f) ).
	 * @param fragments as produced by {@link SplitVertices#exec()}.
	 * @return supp(m, sg_f) for each vertex - same order as  {@link #vertexSet}.
	 */
	private ArrayList<Double> structuralScoring(
			HashMap<String, HashSet<String>> fragments) {
		ArrayList<Double> structuralScoring = new ArrayList<Double>();
		int fragSize = fragments.size();
		for(int i = 0; i < vertexSet.size(); i++){
			String vertex = vertexSet.get(i);
			int count = 0;
			for(Iterator<Map.Entry<String, HashSet<String>>> iter = fragments.entrySet().iterator();
				iter.hasNext(); ){
				HashSet<String> currentVertices = iter.next().getValue();
				for(Iterator<String> vertIter = currentVertices.iterator(); vertIter.hasNext(); ){
					String currentVertex = vertIter.next();
					if(currentVertex.equals(vertex)){
						count++;
						break;
					}
				}
			}
			if(fragSize == 0)
				structuralScoring.add( 0.0 );
			else
				structuralScoring.add( ( new Double(count) ).doubleValue() / ( new Double(fragSize) ).doubleValue() );
		}
		return structuralScoring;
	}
	
	/**
	 * Calculates the P_fail-corr score.
	 * @param sv used to get informations about the fragments
	 * @return the P_fail-corr score for each vertex. Vertices are in same order
	 * as {@link #vertexSet}
	 */
	private ArrayList<Double> structuralCFRanking( SplitVertices sv ) {
		ArrayList<SplitVertices.EmbeddedFragment> embeddings = sv.getEmbeddedFragments();
		ArrayList<Double> scoringCF = new ArrayList<Double>();
		for(int i = 0; i < vertexSet.size(); i++){
			String currentVertex = vertexSet.get(i);
			Double maxEmbeddingCorr = new Double(0.0), maxEmbeddingFail = new Double(0.0);
			for(EmbeddedFragment embedding : embeddings){
				HashSet<String> fragment = embedding.getEmbeddedFragment();
				if(fragment.contains(currentVertex)){
					maxEmbeddingCorr = Math.max(maxEmbeddingCorr, embedding.getCountInCorr());
					maxEmbeddingFail = Math.max(maxEmbeddingFail, embedding.getCountInFail());
				}
			}
			scoringCF.add(  Math.abs(  maxEmbeddingFail / failedTests
									   - maxEmbeddingCorr / passedTests )  );
		}
				
		return scoringCF;
	}

	
	/**
	 * Calculates the normalized average of the passed rankings.
	 * @param structural as returned by {@link #structuralCFRanking(SplitVertices)}, or
	 * {@link #structuralScoring(HashMap)}
	 * @param entropy as returned by {@link #entropyRanking()}
	 * @return sorted list
	 */
	public ArrayList<Score> combineRankings(ArrayList<Double> structural,
													ArrayList<Double> entropy) {
		ArrayList<Score> rankings = new ArrayList<Score>();
		ArrayList<Double> structuralNorm = normalizeList(structural);
		ArrayList<Double> entropyNorm = normalizeList(entropy);
		for (int i = 0; i < structural.size(); i++) {
			Double currentStructural = structuralNorm.get(i);
			Double currentEntropy = entropyNorm.get(i);
			rankings.add( new Score( (currentStructural + currentEntropy)/2,
									  currentStructural,
									  currentEntropy,
									  vertexSet.get(i) ) );
		}
		Collections.sort( rankings, new Comparator<Score>(){
										@Override
										public int compare(Score o1, Score o2) {
											if(o1.combinedScore < o2.combinedScore) return -1;
											if(o1.combinedScore > o2.combinedScore)	return 1;
											else return 0;
										}});
		return rankings;
	}
	
	/**
	 * If scores have not been calculated before, {@link #exec()} is called.
	 * @return scores ordered by {@link Score#combinedScore} ASC.
	 */
	public ArrayList<Score> getRanking(){
		if(combinedScores == null)
			exec();
		return combinedScores;
	}
	
	/**
	 * 
	 * @return a list of vertices ordered by the combination of entropy-scoring and
	 * the P_fail-corr score.
	 */
	public ArrayList<Score> getCFRanking(){
		if(combinedCFScores == null)
			exec();
		return combinedCFScores;
	}
	
	/**
	 * Comparator to sort by entropy
	 */
	private static final Comparator<Score> entropyComparator = new Comparator<Score>(){
		@Override
		public int compare(Score o1, Score o2) {
			if(o1.entropyScore < o2.entropyScore) return -1;
			if(o1.entropyScore > o2.entropyScore)	return 1;
			else return 0;
	}};
	/**
	 * Comparatoe to sort by the structural score.
	 */
	private static final Comparator<Score> structuralComparator = new Comparator<Score>(){
		@Override
		public int compare(Score o1, Score o2) {
			if(o1.structuralScore < o2.structuralScore) return -1;
			if(o1.structuralScore > o2.structuralScore)	return 1;
			else return 0;
	}};
	/**
	 * @param sc switches the ordering
	 * @return vertices ordered by the passed comparator, ranked by P_fail-corr
	 */
	public ArrayList<Score> getCFRanking(ScoreComperator sc){
		if(combinedCFScores == null)
			exec();
		ArrayList<Score> rankings = new ArrayList<Score>(combinedCFScores);
		if(sc == ScoreComperator.ENTROPY)
			Collections.sort( rankings, entropyComparator);
		if(sc == ScoreComperator.STRUCTURAL)
			Collections.sort( rankings, structuralComparator);
		return rankings;
	}
	
	/**
	 * If scores have not been calculated before, {@link #exec()} is called.
	 * @param sc switches the ordering
	 * @return scores ordered as specified ASC.
	 */
	public ArrayList<Score> getRanking(ScoreComperator sc){
		if(combinedScores == null)
			exec();
		ArrayList<Score> rankings = new ArrayList<Score>(combinedScores);
		if(sc == ScoreComperator.ENTROPY)
			Collections.sort( rankings, entropyComparator);
		if(sc == ScoreComperator.STRUCTURAL)
			Collections.sort( rankings, structuralComparator);
		return rankings;
	}
	
	/**
	 * Normalizes the input to [0, 1].
	 * @param list to normalize
	 * @return the input but normalized
	 */
	public ArrayList<Double> normalizeList(ArrayList<Double> list) {
		ArrayList<Double> out = new ArrayList<Double>();
		double max = Collections.max(list);
		if(max == 0)
			return list;
		for (int i = 0; i < list.size(); i++)
			out.add( list.get(i) / max );
		return out;
	}

}
