package bl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Date;

import ml.options.OptionSet;
import ml.options.Options;
import bl.postprocessor.Scoring;
import bl.tools.*;

/**
 * Central class for triggering the different tools.
 * @author Christopher Oßner
 *
 */
public class Main {
	
	public static final String FS = "\\";
	
	private static String outDir = null;
	private static String ibugsDir = null;
	private static String java14 = null;
	private static String fixId = null;
	/**
	 * Sub directory for serialized graph objects 
	 */
	private static final String DF = "\\data";
	
	/**
	 * Start date & time.
	 */
	private static final String start = (new Date() ).toString();

	public static void main(String[] args){
		//a single class
		Options opt = new Options(args, 0, 20);
		opt.addSet("scoring").addOption("-scoring");
		opt.addSet("class-sampler").addOption("-class-sampler");
		opt.addSet("cleaner").addOption("-cleaner");
		opt.addSet("converter").addOption("-converter");
		opt.addSet("copier").addOption("-copier");
		opt.addSet("generator").addOption("-generator");
		//advanced
		opt.addSet("prepare").addOption("-prepare").addOption("fixId",
				Options.Separator.EQUALS).addOption("l", Options.Separator.EQUALS).addOption("suffix",
				Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE).addOption("engine",
				Options.Separator.EQUALS).addOption("stlg", Options.Multiplicity.ZERO_OR_ONE);
		opt.addSet("mine").addOption("-mine").addOption("minFreq", Options.Separator.EQUALS,
									Options.Multiplicity.ZERO_OR_ONE).addOption("closeGraph",
									Options.Multiplicity.ZERO_OR_ONE).addOption("reincludeDummies",
									Options.Multiplicity.ZERO_OR_ONE).addOption("s",
									Options.Multiplicity.ZERO_OR_ONE).addOption("wof",
									Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE).addOption("i",
									Options.Separator.BLANK, Options.Multiplicity.ONCE).addOption("package",
									Options.Multiplicity.ZERO_OR_ONE).addOption("class",
									Options.Multiplicity.ZERO_OR_ONE).addOption("method",
									Options.Multiplicity.ZERO_OR_ONE).addOption("classList", Options.Separator.EQUALS,
									Options.Multiplicity.ZERO_OR_ONE).addOption("all",
									Options.Multiplicity.ZERO_OR_ONE).addOption("writeWeights",
									Options.Multiplicity.ZERO_OR_ONE).addOption("includeDummies",
									Options.Multiplicity.ZERO_OR_ONE).addOption("includeJre",
									Options.Multiplicity.ZERO_OR_ONE).addOption("reincludeJre",
									Options.Multiplicity.ZERO_OR_ONE).addOption("skipConstructors",
									Options.Multiplicity.ZERO_OR_ONE).addOption("sc",
									Options.Multiplicity.ZERO_OR_ONE).addOption("suffix",
									Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE).addOption("sgm",
									Options.Separator.EQUALS, Options.Multiplicity.ZERO_OR_ONE);
		opt.addSet("dot").addOption("-dot");

		OptionSet set = opt.getMatchingSet(true, false);
		
		if (set == null) {
			System.out.println("Usage:\n---------------------\n" +
					"Choose which tool to execute:\n"+
					"\t--scoring <tool-args>\n"+
					"\t--class-sampler <tool-args>\n"+
					"\t--converter <tool-args>\n"+
					"\t--cleaner <tool-args>\n"+
					"\t--copier <tool-args>\n"+
					"\t--generator <tool-args>\n"+
					"\t--prepare "+
					"\n\t\t<-fixId=<bug ID>> "+
					"\n\t\t<-l=<likelihood to include a test case>> "+
					"\n\t\t<-engine=<{rhino|rhinoi}]> "+
					"\n\t\t[-suffix=<suffix for out-Dir>]\n"+
					"\n\t\t[-stlg] skip test list generation "+
					"\t--mine "+
						"\n\t\t[-minFreq=<freq>] default=10 "+
						"\n\t\t[-closeGraph] "+
						"\n\t\t[-s] silent "+
						"\n\t\t[-wof=] scoring output file ID "+
						"\n\t\t[-sc] skip converter "+
						"\n\t\t<-i> input objects <{-method|-class|-package|-all}> "+
						"\n\t\t[-classList=] "+
						"\n\t\t[-writeWeights] "+
						"\n\t\t[-includeDummies]"+
						"\n\t\t[-includeJre]"+
						"\n\t\t[-reincludeDummies] "+
						"\n\t\t[-skipConstructors] "+
						"\n\t\t[-sgm=<fragment-file>] skip the graph-mining "+
						"\n\t\t[-suffix=<suffix for produced dirs and files>]");
			System.out.println("\nThe options library says:\n" + opt.getCheckErrors());
			System.exit(1);
		}
		
		//args to pass to the tool
		String[] nargs = new String[args.length -1];
		System.arraycopy(args, 1, nargs, 0, args.length -1);
		
		//evaluate options
		if(set.getSetName().equals("scoring")){
			System.out.println("EXECUTE: bl.postprocessor.Scoring ...");
			Scoring.main(nargs);
		}
		else if(set.getSetName().equals("class-sampler")){
			System.out.println("EXECUTE: bl.tools.ClassSampler ...");
			ClassSampler.main(nargs);
		}
		else if(set.getSetName().equals("cleaner")){
			System.out.println("EXECUTE: bl.tools.Cleaner ...");
			Cleaner.main(nargs);
		}
		else if(set.getSetName().equals("converter")){
			System.out.println("EXECUTE: bl.tools.Converter ...");
			Converter.main(nargs);
		}
		else if(set.getSetName().equals("copier")){
			System.out.println("EXECUTE: bl.tools.Copier ...");
			Copier.main(nargs);
		}
		else if(set.getSetName().equals("generator")){
			System.out.println("EXECUTE: bl.tools.Generator ...");
			Generator.main(nargs);
		}
		else if(set.getSetName().equals("-dot")){
			System.out.println("EXECUTE: bl.tools.DotWriter ...");
			Copier.main(nargs);
		}
		else{ // andvanced
			String miningOut = System.getenv("MINING_OUT");
			ibugsDir = System.getenv("IBUGS_DIR");
			java14 = System.getenv("JAVA_1.4");
			if(miningOut == null || ibugsDir == null || java14 == null){
				System.out.println("Set the environment variables MINING_OUT, IBUGS_DIR and JAVA_1.4");
				System.exit(1);
			}
			if(set.getSetName().equals("prepare")){
				fixId = set.getOption("fixId").getResultValue(0);
				
				outDir = miningOut+FS+fixId;
				prepare(set);
			} else if(set.getSetName().equals("mine"))
				try {
					mine(set, args);
				} catch (Exception e) {
					System.err.println("Exception: " + e);
					e.printStackTrace();
					System.exit(1);
				}
		}
	}
	
	static Scoring mine(OptionSet set, String[] args) throws IOException, InterruptedException{
		String ser = set.getOption("i").getResultValue(0);
		String level;
		if(set.isSet("package")) level = "package";
		else if(set.isSet("class")) level = "class";
		else if(set.isSet("method")) level = "method";
		else level = "all";
		File f = new File(ser);
		if(! (f.exists() && f.isDirectory() ) ){
			System.out.println("Err, this is not what we are looking for: " + ser);
			System.exit(1);
		}
		String outDirName = ( new File(ser) ).getParentFile().getAbsolutePath()+FS;
		if( level.equals("class") && !set.isSet("classList") )
			System.out.println("You did not set -classList=. Hope you got heap!");
		// set path for classList file
		for(int i = 0; i < args.length; i++){
			if( args[i].startsWith("-classList=") ){
				args[i] = args[i].replace("-classList=", "-classList="+outDirName);
				break;}
		}
		String dir = outDirName;
		outDirName += level;
		if(set.isSet("suffix"))
			outDirName += set.getOption("suffix").getResultValue(0);
		// replace -o option in args
		String[] temp = args;
		args = new String[args.length +2];
		System.arraycopy(temp, 0, args, 0, temp.length);
		args[args.length -2] = "-o"; args[args.length -1] = outDirName;
		if( !( set.isSet("sc") || set.isSet("sgm") ) ){
			System.out.println("_____________________________\nEXECUTE: bl.tools.Converter ...\n"+
			"Might need some time...");
			Converter.main(args);
		}
		// execute ParSeMiS ?
		String fragmentsFile, parsemisDuration;
		if(set.isSet("sgm")){
			fragmentsFile = dir + set.getOption("sgm").getResultValue(0);
			parsemisDuration = "NOT EXECUTED";
		} else{
			fragmentsFile = outDirName+"_fragments_uniq.txt";
			parsemisDuration = graphMining(set, outDirName);
		}
		String[] argSnD = {"-i", fragmentsFile,
						"-arff", outDirName+"_entropy-Score.arff","-ser",outDirName};
		String[] argS = argSnD;
		String[] argSD = {"-i", fragmentsFile,
				"-arff", outDirName+"_entropy-Score.arff","-ser",outDirName, "-reincludeDummies"};
		if(set.isSet("reincludeDummies"))
			argS = argSD;
		if(set.isSet("reincludeJre")){
			String[] argsT = new String[argS.length +1];
			System.arraycopy(argS, 0, argsT, 0, argS.length);
			argsT[argS.length] = "-reincludeJre";
			argS = argsT;
		}
		System.out.println("_____________________________\nEXECUTE: bl.postprocessor.Scoring ...\n");
		long scoringStart = System.currentTimeMillis();
		Scoring scoring = Scoring.getInstance(argS);
		String scoringEcho = scoring.toString();
		scoringEcho += "\n"+scoring.toStringFollowUpBugs()+"\n";
		if(! set.isSet("s")){
			System.out.println( "\n### Mining results ###" );
			System.out.println( scoringEcho );
		}
		String scoringDuration = executionTime( scoringStart, System.currentTimeMillis() );
		if( set.isSet("wof") ){
			System.out.println("\n_____________________________\nEXECUTE: bl.tools.GraphStatistics ...");
			GraphStatistic gs = new GraphStatistic(outDirName);
			String graphStats = gs.toString();
			String optionsString = "";
			for(int i = 0; i < args.length; i++){
				optionsString += args[i] + " ";
			}
			String sof = set.getOption("wof").getResultValue(0);
			sof = outDirName + "Scoring" + sof + ".txt";
			String so = start + "\nLEVEL: " +level +
						"\nEXECUTION TIME PARSEMIS: "+parsemisDuration+
						"\nEXECUTION TIME SCORING: "+scoringDuration+"\n" + 
						"USED OPTIONS: " + optionsString +"\n\n"+
						scoringEcho +
						"Graph Mining Statistics:\n------------------------\n" +
						scoring.toStringFragmentStatistics() +"\n\n" +
						"Graph DB Statistics:\n--------------------\n" +
						graphStats;
			FileWriter fw = new FileWriter(sof);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(so);
			bw.close();
			System.out.println("Wrote mining results to: " + sof);
			if(set.isSet("package")){
				System.out.println("You should run the ClassSampler e.g.:"+
						   "\njava -jar tools.jar --class-sampler -ibugs "+ibugsDir+"-o <outDir>"+"\\<listFile>.ls"+
						   "-Id=<bugID> -prefix=<package> -n=30");
			}
		}
		return scoring;
	}
	
	static String graphMining(OptionSet set, String outDirName) throws InterruptedException, IOException{
		// graph mining start
		String minFreq = "10";
		if( set.isSet("minFreq" ) )
			minFreq = set.getOption("minFreq").getResultValue(0);
		String[] argsPnCG = {"--graphFile=" + outDirName + ".lg", "--storeEmbeddings=true",
				"--outputFile="+outDirName+"_fragments_temp.txt",
				"--minimumFrequency="+minFreq};
		String[] argsPCG = {"--graphFile="+outDirName+".lg", "--storeEmbeddings=true",
				"--outputFile="+outDirName+"_fragments_temp.txt",
				"--minimumFrequency="+minFreq, "--closeGraph=true"};
		String[] argsP = argsPnCG;
		if( set.isSet("closeGraph") )
			argsP = argsPCG;
		System.out.println("_____________________________\nEXECUTE: ParSeMis ...\n"+
						   "ParSeMiS command is: ");
		for(int i = 0; i < argsP.length; i ++){
			System.out.print(argsP[i] + " ");
		}
		System.out.println();
		long parsemisStart = System.currentTimeMillis();
		try {
			de.parsemis.Miner.main(argsP);
		} catch (Exception e) {
			System.err.println("Exception from ParSeMiS: " +e);
			System.exit(1);
		}
		String parsemisDuration = executionTime( parsemisStart, System.currentTimeMillis() );
		String cmd = "cmd.exe /c uniq "+outDirName+"_fragments_temp.txt > "+outDirName+"_fragments_uniq.txt";
		System.out.println("_____________________________\nEXECUTE: uniq ...\n"+
							"CMD: " +cmd);
		Process p = null;
		p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		// graph mining end
		return parsemisDuration;
	}
	
	
	static void prepare(OptionSet set){
		if(set.isSet("suffix"))
			outDir += set.getOption("suffix").getResultValue(0);
		String l = set.getOption("l").getResultValue(0);
		String engine = set.getOption("engine").getResultValue(0);
		File outFile = new File(outDir);
		try {
			if(outFile.exists()){
				System.out.println("Output directory already exists: " +outDir+
						"\nTerminated, since using this dir could cause data loss!");
				System.exit(1);
			} else if( outFile.mkdirs() ){
				System.out.println("Data directory is: " +outDir);
				
			} else System.out.println("IOException, problem creating: " +outDir);
		} catch (SecurityException e) {
			System.err.println("SecurityManager error"+e);
			System.exit(1);
		}
		String[] args1 = {ibugsDir +FS+"output"+FS+fixId+FS+"post-fix"+FS+"mozilla"+FS+"js"+FS+"tests",
				  fixId, ibugsDir};
		System.out.println("_____________________________\nEXECUTE copying testForFix to pre-fix version...");
		Copier.main(args1);
		String testDir = ibugsDir+FS+"output"+FS+fixId+FS+"pre-fix"+FS+"mozilla"+FS+"js"+FS+"tests";
		String[] argsTv = {testDir, ibugsDir + "\\instrumentation\\lib\\js_variations"};
		System.out.println("_____________________________\nEXECUTE variate testsForFix...");
		TestVariator.main( argsTv );
		if(set.isSet("stlg")){
			System.out.println("You skipped the test list file genaration.");
			File f = new File(testDir+FS+"sampled-tests.ls");
			if(! f.exists()){
				System.out.println("Terminated, test list file does not exist: " +
						testDir+FS+"sampled-tests.ls");
				System.exit(1);
			}
		} else{
			String[] args2 = {ibugsDir+FS+"output"+FS+fixId+FS+"pre-fix"+FS+"mozilla"+FS+"js"+FS+"tests", l};
			System.out.println("_____________________________\nEXECUTE generate the test list file...");
			Generator.main(args2);
		}
		String args3 = "perl " + testDir+FS+"jsDriver.pl " +
				"--engine="+engine+" " + "-j " + java14+" " +
				"-c " + ibugsDir+FS+"output"+FS+fixId+FS+"pre-fix"+FS+"mozilla"+FS+"js"+FS+
					"rhino"+FS+"build"+FS+"rhino_ibugs"+FS+"js_cc.jar " +
				"-v " + outDir+DF+" " + 
				"-f " + outDir+ FS + "TestResults.html " +
				"-l " + testDir+FS+"sampled-tests.ls";
		System.out.println("_____________________________\n"+
				"EXECUTE tests while generating call graphs (you should get a coffee)...\n"+
				"PERL command is: \n"+args3);
		long perlStart = System.currentTimeMillis();
		execute(args3, new File(testDir));
		String perlTime = executionTime(perlStart, System.currentTimeMillis());
		String[] argsCl = {outDir+ FS + "TestResults.html", fixId, ibugsDir, outDir+DF};
		System.out.println("_____________________________\nEXECUTE cleaning up failed tests...");
		Cleaner.main(argsCl);
		System.out.println("\n\n### Everything is prepared for further processing. ###"+
						   "\nTest suite execution time was: "+perlTime);
	}
	
	private static void execute(String cmd, File dir){
		Process p = null;
		try {
			String line;
			p = Runtime.getRuntime().exec(cmd, null, dir);
			BufferedReader input = new BufferedReader
		          (new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null)
				System.out.println(line);
			input.close();
			p.waitFor();
		}
		catch (Exception e) {
			System.err.println("IOException while executing '" +cmd+"'. " +e);
			System.exit(1);
		}
	}
	
	private static String executionTime( long start, long end ) {
		long ms = end - start;
		long sec = ms / 1000;
		long min = sec / 60;
		long remainder = ms - (min * 60 * 1000);
		Double dr = ( new Double(remainder) );
		double out_sec = dr / 1000;
		DecimalFormat df  = new DecimalFormat("##.###");
		return min + "min " + df.format(out_sec) + "sec";
	}

	
}
