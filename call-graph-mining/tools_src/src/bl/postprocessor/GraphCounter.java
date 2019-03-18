package bl.postprocessor;

import java.io.File;

public class GraphCounter {
	
	private int noCorr = 0;
	private int noFailed = 0;
	
	private String serData;
	
	boolean executed = false;
	
	public GraphCounter(String serData){
		this.serData = serData;
	}
	
	public void exec(){
		File file = new File(serData);
		if( ! file.isDirectory() ){
			System.out.println("Err, not what we are looking for: " + serData);
			System.exit(1);
		}
		String[] files = file.list();
		for(int i = 0; i < files.length; i++){
			if(files[i].matches(".*_failed.*"))
				noFailed++;
			else
				noCorr++;
		}
		executed = true;
	}
	
	public int getNoCorr(){
		if( ! executed )
			exec();
		return noCorr;
	}
	public int getNoFailed(){
		if( ! executed )
			exec();
		return noFailed;
	}

}
