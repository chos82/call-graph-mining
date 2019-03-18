package tests;


import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import bl.postprocessor.ArffWriter;

import callgraph.AdjacenceElement;
import callgraph.AdjacenceList;
import callgraph.GraphWriter;
import callgraph.Signature;
import callgraph.SuccessorList;

public class ArffWriterTest {
	
	/**
	 * Actually there is no real test in the moment, we just write an ARFF, which can be
	 * hand checked. Serves as draft for future in depth testing (e.g. using WEKA`s ARFF API) 
	 */
	@Test
	public void main(){
		ArffWriter arffWriter = new ArffWriter("testIO\\postprocessor_data\\fragments.txt",
				"testIO\\postprocessor_data\\entropyScoring.arff",
				"testIO\\postprocessor_data", true, true);
		arffWriter.exec();
	}

	/**
	 * Generate some (non sens) AdjacenceList.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		GraphWriter gw = new GraphWriter( makeClique(), "testIO\\postprocessor_data", "run1" );
		gw.writeAdjacence();
		
		gw = new GraphWriter( makeClique(), "testIO\\postprocessor_data", "run2" );
		gw.writeAdjacence();
		
		gw = new GraphWriter( makeClique(), "testIO\\postprocessor_data", "run3" );
		gw.writeAdjacence();
		
		gw = new GraphWriter( makeClique(), "testIO\\postprocessor_data", "run4_failed" );
		gw.writeAdjacence();
		
		gw = new GraphWriter( makeClique(), "testIO\\postprocessor_data", "run5_failed" );
		gw.writeAdjacence();
	}
	
	/**
	 * Generates a package AdjacenceList clique with 4 vertices (and self calls for each vertex).
	 * Call-frequency is constantly 1, annotations are in [1, 100].
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private AdjacenceList makeClique(){
		AdjacenceList g = new AdjacenceList();
		SuccessorList succ;
		AdjacenceElement ae;
		Random rand = new Random();
		for(int i = 0; i < 4; i++){
			succ = new SuccessorList();
			for(int j = 0; j < 4; j++){
				ae = new AdjacenceElement( new Signature( "package", "v"+(j+1) ) );
				ae.setNoPreClasses( rand.nextInt(100) +1 );
				ae.setNoPreMethods( rand.nextInt(100) +1 );
				ae.setNoSuccClasses( rand.nextInt(100) +1 );
				ae.setNoSuccMethods( rand.nextInt(100) +1 );
				succ.add(ae);
			}
			g.put( new Signature( "package", "v"+(i+1) ), succ );
		}
		return g;
	}

}
