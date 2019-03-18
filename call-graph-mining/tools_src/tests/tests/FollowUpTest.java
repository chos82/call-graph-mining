package tests;


import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import bl.postprocessor.FollowUpDetector;
import bl.postprocessor.Scoring.Result;

public class FollowUpTest {
	
	private ArrayList<Result> input = new ArrayList<Result>();;
	
	@Test
	public void main(){
		FollowUpDetector fud = new FollowUpDetector(input);
		ArrayList<Result> cleaned = fud.remove();
		assertTrue(  cleaned.contains( new Result("a", "b", 0) )  );
		assertTrue(  cleaned.contains( new Result("d", "c", 0) )  );
		assertFalse(  cleaned.contains( new Result("b", "c", 0) )  );
		assertTrue(  cleaned.contains( new Result("c", "e", 0) )  );
		assertTrue(  cleaned.contains( new Result("e", "c", 0) )  );
		assertTrue(  cleaned.contains( new Result("x", "y", 0) )  );
		assertTrue(  cleaned.contains( new Result("y", "z", 0) )  );
	}

	@Before
	public void setUp() throws Exception {
		input.add(new Result("a", "b", 1));
		input.add(new Result("b", "c", 1));
		input.add(new Result("d", "c", 1));
		input.add(new Result("c", "e", 0.5));
		input.add(new Result("e", "c", 0.5));
		input.add(new Result("x", "y", 0.1));
		input.add(new Result("y", "z", 0.2));
	}

}
