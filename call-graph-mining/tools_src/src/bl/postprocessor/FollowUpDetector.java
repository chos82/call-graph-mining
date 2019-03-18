package bl.postprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import bl.postprocessor.Scoring.Result;

/**
 * For each edge, edges recursively succeeding this edge and having the same InfoGain, can be removed
 * by using this class. We expect those edges to be so called follow-up bugs.
 * @author CO
 *
 */
public class FollowUpDetector {
	
	/**
	 * Results of WEKA
	 */
	private ArrayList<Result> in;
	
	/**
	 * Here we store vertices we believe to be follow up bugs
	 */
	private ArrayList<Result> followUpBugs = new ArrayList<Result>();
	
	/**
	 * @param in Results of WEKA InformationGain scoring
	 */
	public FollowUpDetector(ArrayList<Result> in){
		this.in = in;
	}
	
	public ArrayList<Result> getFollowUpBugs(){
		return followUpBugs;
	}
	
	/**
	 * 
	 * @return {@link #in} but without follow-up bugs
	 */
	public ArrayList<Result> remove(){
		ArrayList<Result> out = in;
		ArrayList<HashMap<Result, String>> sameEntropy = new ArrayList<HashMap<Result, String>>();
		for( int i = 0; i < in.size(); i++ ){
			HashMap<Result, String> entropyClass = new HashMap<Result, String>(); 
			boolean foundPotential = false;
			for( int j = i; j < in.size(); j++ ){
				Result item1 = in.get(i);
				Result item2 = in.get(j);
				if(item1.getValue() == item2.getValue()){
					entropyClass.put(item1, null);
					entropyClass.put(item2, null);
					foundPotential = true;
				}
			}
			if(foundPotential)
				sameEntropy.add(entropyClass);
		}
		ensureSucceeding(sameEntropy);
		for( Result followUp : followUpBugs ){
			for(int i = 0; i < out.size(); i++){
				if(  followUp.equals( out.get(i) )  )
					out.remove(i);
			}
		}
		return out;
	}
	
	/**
	 * Ensures that possible matches (same InfoGain) are connected.
	 * @param in each HashMap contains edges with the same InfoGain
	 */
	private void ensureSucceeding(ArrayList<HashMap<Result, String>> in){
		for( int i = 0; i < in.size(); i++ ){
			HashMap<Result, String> entropyClass = in.get(i);
			for( Iterator<Result> iter = entropyClass.keySet().iterator();
				  iter.hasNext(); ){
				Result item  = iter.next();
				searchFollowUp(entropyClass, item);
			}
		}
	}

	/**
	 * Recursively checks all edges in @param entropyClass for edges succeeding @param item 
	 * @param entropyClass edges having the same InfoGain
	 * @param item the edge we want successors for
	 */
	private void searchFollowUp(HashMap<Result, String> entropyClass,
			Result item) {
		for(Iterator<Result> iter = entropyClass.keySet().iterator(); iter.hasNext(); ){
			Result item2 = iter.next();
			if(  item.getEndVertex().equals( item2.getStartVertex() ) 
				  && ! item.getStartVertex().equals( item2.getEndVertex() )  ){
				followUpBugs.add(item2);
				searchFollowUp(entropyClass, item2);
			}
		}
	}

}
