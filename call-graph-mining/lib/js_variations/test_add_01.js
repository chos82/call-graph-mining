// ----- BEGIN VARIATION -----

/*
Functions in this file are used to generate variations of 
the tests that are provided by iBUGS (<testsforfix>). 
They can be added to a existing test by a call to 
AddTestsCase("Added function", true, testAdd<no.>);
As they only serve to variate a test, which reproduces 
a bug, the functions here do not need to return any 
useful value. So it is convenient to return true in 
any addTest, as well as expect them to return true.
As the AddTestCase function is part of the new Rhino
shell.js, additional modification might be needed for
older test cases.
*/

testAdd();

function testAdd(){
	for(var i = 0; i <= 10000; i++){
		
	}
}

// ----- END VARIATION -----
