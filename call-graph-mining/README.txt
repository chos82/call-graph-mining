This is the framework used for http://dbis.ipd.kit.edu/download/eichi/eichinger11scalable.pdf
The framework is tailored to be used with the Rhino dataset of iBugs: https://www.st.cs.uni-saarland.de/ibugs/  

Code found under /tools_src/src is used for defect localization.
Code in /src is code used for instrumentation (modelling the callgraph etc.)

Run bl.Main for instructions on how to use command line interface.
You can find a more comprehensive description of the command line interface in /cmd-synopsis

- REQUIREMENTS
	* aspectj (tested with v1.6) https://www.eclipse.org/aspectj/
		copy aspectjrt.jar in /lib folder
		needs to be on CLASSPATH
	* ParSeMiS (parsemis-2008-12-01 was used) https://www2.cs.fau.de/EN/research/zold/ParSeMiS/index.html
		copy JAR in /lib folder
		needs to be on CLASSPATH
	* uniq http://unxutils.sourceforge.net/ is needed
		put it on PATH
	* WEKA (tested with v3.6) https://www.cs.waikato.ac.nz/ml/weka/index.html
		put it in /lib folder
		
	* iBugs (Rhino dataset) requires Java JDK 1.4.2 and Ant 1.6.5 (see Prerequisites https://www.st.cs.uni-saarland.de/ibugs/)

	* The following environment variables need to be set:
		MINING_OUT=<where all the data will be stroed>
		IBUGS_DIR=<iBUGS directory>
		JAVA_1.4=<JRE v1.4 \bin>
	Further it assumes, that you did not change the default folder (/output) of iBUGS, for the 
	different versions.


Currently it is not ensured that the automatic generated test variations do not cause additional
errors (execpt those caused by the original testForFix). So it is necessary to check is the 
reported messages are the same for all varaitions like for the the original test case. Alternatively
the variations could be run through the post-fix version - they have to pass!


- TODOs
	Currently all signatures are represented by a single class. This really should be changed to 
	an interface Signature and 3 classes e.g. PackageSignature extends Signature
	
	In the moment it is crucial to set the siganture names of dummy signatures that way, 
	that the signature type can be detected later on. E.g a class dummy must get a name 
	beginning with a capital letter, since Edge uses this as indication that we got a 
	class signature (see Edge.detectSignatureType).
	
	GraphConverter (and some other classes) does not need to be in the callgraph-package -> 
	could be written in java v1.6
	
	There is system dependent coding here and there: path dilimeters, file dilimeters and the 
	command for uniq.
	
	AdjacenceList and SuccessorList could implement Iterable.
	
	
	Command line parser will accept non-sens combinations.