// ----- BEGIN VARIATION -----

/**
    File Name:          15.5.4.2-3.js
    ECMA Section:       15.5.4.2 String.prototype.toString()

    Description:        Returns this string value.  Note that, for a String
                        object, the toString() method happens to return the same
                        thing as the valueOf() method.

                        The toString function is not generic; it generates a
                        runtime error if its this value is not a String object.
                        Therefore it connot be transferred to the other kinds of
                        objects for use as a method.

    Author:             christine@netscape.com
    Date:               1 october 1997
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

    array[item++] = new TestCase( SECTION,
                                  "var tostr=String.prototype.toString; astring=new String(); astring.toString = tostr; astring.toString()",
                                 "",
                                  "var tostr=String.prototype.toString; astring=new String(); astring.toString = tostr; astring.toString()" );
    array[item++] = new TestCase( SECTION,
                                  "var tostr=String.prototype.toString; astring=new String(0); astring.toString = tostr; astring.toString()",
                                  "0",
                                  "var tostr=String.prototype.toString; astring=new String(0); astring.toString = tostr; astring.toString()" );
    array[item++] = new TestCase( SECTION,
                                  "var tostr=String.prototype.toString; astring=new String('hello'); astring.toString = tostr; astring.toString()",
                                  "hello",
                                  "var tostr=String.prototype.toString; astring=new String('hello'); astring.toString = tostr; astring.toString()" );
    array[item++] = new TestCase( SECTION,
                                  "var tostr=String.prototype.toString; astring=new String(''); astring.toString = tostr; astring.toString()",
                                  "",
                                  "var tostr=String.prototype.toString; astring=new String(''); astring.toString = tostr; astring.toString()" );

    return ( array );
}

// ----- END VARIATION -----