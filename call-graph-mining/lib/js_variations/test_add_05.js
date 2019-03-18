// ----- BEGIN VARIATION -----

/**
    File Name:          ecma/lExicalConversion/7.1-3.js
    ECMA Section:       7.1 White Space
    Description:        - readability
                        - separate tokens
                        - otherwise should be insignificant
                        - in strings, white space characters are significant
                        - cannot appear within any other kind of token

                        white space characters are:
                        unicode     name            formal name     string representation
                        \u0009      tab             <TAB>           \t
                        \u000B      veritical tab   <VT>            ??
                        \U000C      form feed       <FF>            \f
                        \u0020      space           <SP>            " "

    Author:             christine@netscape.com
    Date:               11 september 1997
*/

var SECTION = "testVariation";

/*
 * TestCase constructor
 * taken from ecma/shell.js
 */
 
 /*
 * Compare expected result to the actual result and figure out whether
 * the test case passed.
 */
function getTestCaseResult(	expect,	actual ) {
	//	because	( NaN == NaN ) always returns false, need to do
	//	a special compare to see if	we got the right result.
		if ( actual	!= actual )	{
			if ( typeof	actual == "object" ) {
				actual = "NaN object";
			} else {
				actual = "NaN number";
			}
		}
		if ( expect	!= expect )	{
			if ( typeof	expect == "object" ) {
				expect = "NaN object";
			} else {
				expect = "NaN number";
			}
		}

		var	passed = ( expect == actual	) ?	true : false;

	//	if both	objects	are	numbers
	// need	to replace w/ IEEE standard	for	rounding
		if (	!passed
				&& typeof(actual) == "number"
				&& typeof(expect) == "number"
			) {
				if ( Math.abs(actual-expect) < 0.0000001 ) {
					passed = true;
				}
		}

	//	verify type	is the same
		if ( typeof(expect)	!= typeof(actual) )	{
			passed = false;
		}

		return passed;
}

function TestCase( n, d, e,	a )	{
	this.name		 = n;
	this.description = d;
	this.expect		 = e;
	this.actual		 = a;
	this.passed		 = true;
	this.reason		 = "";
	this.bugnumber	  =	BUGNUMBER;

	this.passed	= getTestCaseResult( this.expect, this.actual );
	
}


testAdd();

function testAdd() {
    var array = new Array();
    var item = 0;
    array[item++] = new TestCase( SECTION,    "'var'+'\u000B'+'MYVAR1=10;MYVAR1'",   10, eval('var'+'\u000B'+'MYVAR1=10;MYVAR1') );
    array[item++] = new TestCase( SECTION,    "'var'+'\u0009'+'MYVAR2=10;MYVAR2'",   10, eval('var'+'\u0009'+'MYVAR2=10;MYVAR2') );
    array[item++] = new TestCase( SECTION,    "'var'+'\u000C'+'MYVAR3=10;MYVAR3'",   10, eval('var'+'\u000C'+'MYVAR3=10;MYVAR3') );
    array[item++] = new TestCase( SECTION,    "'var'+'\u0020'+'MYVAR4=10;MYVAR4'",   10, eval('var'+'\u0020'+'MYVAR4=10;MYVAR4') );

    // +<white space>+ should be interpreted as the unary + operator twice, not as a post or prefix increment operator

    array[item++] = new TestCase(   SECTION,
                                    "var VAR = 12345; + + VAR",
                                    12345,
                                    eval("var VAR = 12345; + + VAR") );

    array[item++] = new TestCase(   SECTION,
                                    "var VAR = 12345;VAR+ + VAR",
                                    24690,
                                    eval("var VAR = 12345;VAR+ +VAR") );
    array[item++] = new TestCase(   SECTION,
                                    "var VAR = 12345;VAR - - VAR",
                                    24690,
                                    eval("var VAR = 12345;VAR- -VAR") );


    return ( array );
}

// ----- END VARIATION -----