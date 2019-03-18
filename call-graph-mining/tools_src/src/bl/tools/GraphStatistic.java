package bl.tools;

import java.io.*;
import callgraph.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Scan for serialized graphs and generate statistics. Lil` hacky :(
 * @author Christopher Oßner
 *
 */
public class GraphStatistic {
	
	private ArrayList<AnnotatedEdge> graphEdges = null;
	private String dbDir;
	long[] annotationSums = null;
	int noGraphs;
	long noAllVertices = 0;
	private String type;
	private AnnotatedEdge[] annotationsMax, annotationsMin;/* cfMax = null, cfMin = null, calledMethodsMax = null, calledMethodsMin = null,
			callingMethodsMax = null, callingMethodsMin = null, calledClassesMax = null,
			calledClassesMin = null, callingClassesMax = null, callingClassesMin = null;*/
	private GraphAggragate graphMaxEdges = new GraphAggragate(),
			graphMinEdges = new GraphAggragate(),
			graphMaxVertices = new GraphAggragate(),
			graphMinVertices = new GraphAggragate();
	
	static class GraphAggragate{
		private int noVertices, noEdges;
		private String name = null;
		GraphAggragate(String name, int noVertices, int noEdges){
			this.name = name; this.noEdges = noEdges; this.noVertices = noVertices;
		}
		public GraphAggragate() { }
	}
	
	/**
	 * Simple Helper.
	 * @author CO
	 */
	static class AnnotatedEdge {
		private Signature start;
		private AdjacenceElement end;
		private String graphName;
		public AnnotatedEdge(String graphName, Signature start, AdjacenceElement end) {
			this.start = start; this.end = end; this.graphName = graphName;
		}
		public Signature getStart(){ return start; }
		public AdjacenceElement getEnd(){ return end; }
		public String getGraphName(){ return graphName; }
	}
	
	/**
	 * @param dbDir directory to scan
	 */
	public GraphStatistic(String dbDir){
		this.dbDir = dbDir; 
	}
	
	public static void main(String[] args){
		GraphStatistic gs = new GraphStatistic(args[0]);
		System.out.println(gs.toString());
	}
	
	@SuppressWarnings("unchecked")
	public void exec(){
		graphEdges = new ArrayList<AnnotatedEdge>();
		File dir = new File(dbDir);
		if( !dir.exists() || !dir.isDirectory() ){
			System.out.println("NO NO NO not the right place: " + dbDir);
			System.exit(1);
		}
		System.out.println("Graph DB scanned: " + dbDir);
		// each edge in list
		GraphReader gr = new GraphReader();
		String[] sers = dir.list();
		noGraphs = sers.length;
		for(int i = 0; i < sers.length; i++){
			AdjacenceList graph = gr.readGraph(dbDir + "\\" + sers[i]);
			int noEdges = 0;
			for(Iterator<Entry<Signature, SuccessorList>> iter = graph.iterator(); iter.hasNext(); ){
				Entry<Signature, SuccessorList> entry = iter.next();
				Signature start = entry.getKey();
				SuccessorList succ = entry.getValue();
				for(Iterator<AdjacenceElement> succIter = succ.iterator(); succIter.hasNext(); ){
					AdjacenceElement ae = succIter.next();
					graphEdges.add(new AnnotatedEdge(sers[i], start, ae));
					noEdges++;
				}
			}
			int noVertices = graph.size();
			noAllVertices += noVertices;
			if(noVertices > graphMaxVertices.noVertices || graphMaxVertices.noVertices == 0 )
				graphMaxVertices = new GraphAggragate(sers[i], noVertices, noEdges);
			if(noVertices < graphMinVertices.noVertices || graphMinVertices.noVertices == 0 )
				graphMinVertices = new GraphAggragate(sers[i], noVertices, noEdges);
			if(noEdges > graphMaxEdges.noEdges || graphMaxEdges.noEdges == 0 )
				graphMaxEdges = new GraphAggragate(sers[i], noVertices, noEdges);
			if(noEdges < graphMinEdges.noEdges || graphMinEdges.noEdges == 0 )
				graphMinEdges = new GraphAggragate(sers[i], noVertices, noEdges);
		}
		// analyze
		type = graphEdges.get(0).getStart().getType();
		// hack arrgh...
		final int cmPos;
		final int callingMethodsPos;
		if(type.equals("package")){
			cmPos = 2;	callingMethodsPos = 4;
			annotationsMin = new AnnotatedEdge[5];
			annotationsMax = new AnnotatedEdge[5];
		} else if(type.equals("class")){
			cmPos = 1;	callingMethodsPos = 2;
			annotationsMin = new AnnotatedEdge[3];
			annotationsMax = new AnnotatedEdge[3];
		} else{
			cmPos = -1;	callingMethodsPos = -1;
			annotationsMin = new AnnotatedEdge[1];
			annotationsMax = new AnnotatedEdge[1];
		}
		// compare call-frequency (~weight)
		Comparator c = new Comparator<AnnotatedEdge>(){
			@Override
			public int compare(AnnotatedEdge o1, AnnotatedEdge o2) {
				if(o1.getEnd().getAnnotations()[0] < o2.getEnd().getAnnotations()[0])
					return -1;
				if(o1.getEnd().getAnnotations()[0] == o2.getEnd().getAnnotations()[0])
					return 0;
				else return 1;
			}
		};
		// all possible stats per edge
		annotationsMax[0] = (AnnotatedEdge) Collections.max(graphEdges, c);
		annotationsMin[0] = (AnnotatedEdge) Collections.min(graphEdges, c);
		if(type.equals("class") || type.equals("package")){
			c = new Comparator<AnnotatedEdge>(){
				@Override
				public int compare(AnnotatedEdge o1, AnnotatedEdge o2) {
					if(o1.getEnd().getAnnotations()[cmPos] < o2.getEnd().getAnnotations()[cmPos])
						return -1;
					if(o1.getEnd().getAnnotations()[cmPos] == o2.getEnd().getAnnotations()[cmPos])
						return 0;
					else return 1;
				}
			};
			annotationsMax[cmPos] = (AnnotatedEdge) Collections.max(graphEdges, c);
			annotationsMin[cmPos] = (AnnotatedEdge) Collections.min(graphEdges, c);
			c = new Comparator<AnnotatedEdge>(){
				@Override
				public int compare(AnnotatedEdge o1, AnnotatedEdge o2) {
					if(o1.getEnd().getAnnotations()[callingMethodsPos] < 
							o2.getEnd().getAnnotations()[callingMethodsPos])
						return -1;
					if(o1.getEnd().getAnnotations()[callingMethodsPos] == 
						o2.getEnd().getAnnotations()[callingMethodsPos])
						return 0;
					else return 1;
				}
			};
			annotationsMax[callingMethodsPos] = (AnnotatedEdge) Collections.max(graphEdges, c);
			annotationsMin[callingMethodsPos] = (AnnotatedEdge) Collections.min(graphEdges, c);
		}
		if(type.equals("package")){
			c = new Comparator<AnnotatedEdge>(){
				@Override
				public int compare(AnnotatedEdge o1, AnnotatedEdge o2) {
					if(o1.getEnd().getAnnotations()[1] < o2.getEnd().getAnnotations()[1])
						return -1;
					if(o1.getEnd().getAnnotations()[1] == o2.getEnd().getAnnotations()[1])
						return 0;
					else return 1;
				}
			};
			annotationsMax[1] = (AnnotatedEdge) Collections.max(graphEdges, c);
			annotationsMin[1] = (AnnotatedEdge) Collections.min(graphEdges, c);
			c = new Comparator<AnnotatedEdge>(){
				@Override
				public int compare(AnnotatedEdge o1, AnnotatedEdge o2) {
					if(o1.getEnd().getAnnotations()[3] < o2.getEnd().getAnnotations()[3])
						return -1;
					if(o1.getEnd().getAnnotations()[3] == o2.getEnd().getAnnotations()[3])
						return 0;
					else return 1;
				}
			};
			annotationsMax[3] = (AnnotatedEdge) Collections.max(graphEdges, c);
			annotationsMin[3] = (AnnotatedEdge) Collections.min(graphEdges, c);
		}
		// aggregations
		if(type.equals("package")) annotationSums = new long[5];
		else if(type.equals("class")) annotationSums = new long[3];
		else annotationSums = new long[1];
		for(AnnotatedEdge ae : graphEdges){
			for(int i = 0; i < annotationSums.length; i++){
				annotationSums[i] += ae.getEnd().getAnnotations()[i];
			}
		}
	}
	
	public String toString(){
		if(graphEdges == null)
			exec();
		int noEdges = graphEdges.size();
		String out = "Statistics for graphs in: " + dbDir+
					 "\nGraph hierarchy level: " + type+
					 "\n#graphs: " + noGraphs +
					 "\nAVG(#vertices): " +(noAllVertices / noGraphs) +
					 "\nAVG(#edges): " +(noEdges / noGraphs) + "\n---"+
					 "\nMAX(#edges): " + graphMaxEdges.name +
					 "\n\t#vertices: " + graphMaxEdges.noVertices +
					 "\n\t#edges: " + graphMaxEdges.noEdges +"\n-"+
					 "\nMIN(#edges): " + graphMinEdges.name +
					 "\n\t#vertices: " + graphMinEdges.noVertices +
					 "\n\t#edges: " + graphMinEdges.noEdges +"\n---"+
					 "\nMAX(#vertices): " + graphMaxVertices.name +
					 "\n\t#vertices: " + graphMaxVertices.noVertices +
					 "\n\t#edges: " + graphMaxVertices.noEdges +"\n-"+
					 "\nMIN(#vertices): " + graphMinVertices.name +
					 "\n\t#vertices: " + graphMinVertices.noVertices +
					 "\n\t#edges: " + graphMinVertices.noEdges +"\n---";
		String[] annotationNames;
		if( type.equals("package" ) ) annotationNames = AdjacenceElement.PACKAGE_ANNOTATIONS;
		else if( type.equals("class") ) annotationNames = AdjacenceElement.CLASS_ANNOTATIONS;
		else annotationNames = AdjacenceElement.METHOD_ANNOTATIONS;
		for(int i = 0; i < annotationNames.length; i++){
			AdjacenceElement end = annotationsMax[i].getEnd();
			AdjacenceElement endMin = annotationsMin[i].getEnd();
			out += "\nSUM("+ annotationNames[i] +"): " + annotationSums[i] +
			   	   "\nAVG("+ annotationNames[i] +"): " + (annotationSums[i] / noEdges)+
			   	   "\nMAX("+ annotationNames[i] +"): " + annotationsMax[i].getGraphName()+"("+
			   	   		annotationsMax[i].getStart().getSignature()+"->"+
			   	   		end.getSignature().getSignature()+")"+" = "+ ( end.getAnnotations() )[i]+
			   	   "\nMIN("+ annotationNames[i] +"): " + annotationsMin[i].getGraphName() +"("+
			   	   		annotationsMin[i].getStart().getSignature()+"->"+
			   	   		endMin.getSignature().getSignature()+")"+" = "+ ( endMin.getAnnotations() )[i]+
			   	   	"\n---\n";
		}
		return out;
	}

}
