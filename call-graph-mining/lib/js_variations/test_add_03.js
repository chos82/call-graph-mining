// ----- BEGIN VARIATION -----

/**
    File Name:          ecma/Array/15.4-2.js
    ECMA Section:       15.4 Array Objects

    Description:        Whenever a property is added whose name is an array
                        index, the length property is changed, if necessary,
                        to be one more than the numeric value of that array
                        index; and whenever the length property is changed,
                        every property whose name is an array index whose value
                        is not smaller  than the new length is automatically
                        deleted.  This constraint applies only to the Array
                        object itself, and is unaffected by length or array
                        index properties that may be inherited from its
                        prototype.

    Author:             christine@netscape.com
    Date:               28 october 1997

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

    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,16)] = 'hi'; arr.length",      Math.pow(2,16)+1,   eval("var arr=new Array();  arr[Math.pow(2,16)] = 'hi'; arr.length") );

    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,30)-2] = 'hi'; arr.length",    Math.pow(2,30)-1,   eval("var arr=new Array();  arr[Math.pow(2,30)-2] = 'hi'; arr.length") );
    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,30)-1] = 'hi'; arr.length",    Math.pow(2,30),     eval("var arr=new Array();  arr[Math.pow(2,30)-1] = 'hi'; arr.length") );
    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,30)] = 'hi'; arr.length",      Math.pow(2,30)+1,   eval("var arr=new Array();  arr[Math.pow(2,30)] = 'hi'; arr.length") );

    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,31)-2] = 'hi'; arr.length",    Math.pow(2,31)-1,   eval("var arr=new Array();  arr[Math.pow(2,31)-2] = 'hi'; arr.length") );
    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,31)-1] = 'hi'; arr.length",    Math.pow(2,31),     eval("var arr=new Array();  arr[Math.pow(2,31)-1] = 'hi'; arr.length") );
    array[item++] = new TestCase( SECTION, "var arr=new Array();  arr[Math.pow(2,31)] = 'hi'; arr.length",      Math.pow(2,31)+1,   eval("var arr=new Array();  arr[Math.pow(2,31)] = 'hi'; arr.length") );

    array[item++] = new TestCase( SECTION, "var arr = new Array(0,1,2,3,4,5); arr.length = 2; String(arr)",     "0,1",              eval("var arr = new Array(0,1,2,3,4,5); arr.length = 2; String(arr)") );
    array[item++] = new TestCase( SECTION, "var arr = new Array(0,1); arr.length = 3; String(arr)",             "0,1,",             eval("var arr = new Array(0,1); arr.length = 3; String(arr)") );
//    array[item++] = new TestCase( SECTION, "var arr = new Array(0,1,2,3,4,5); delete arr[0]; arr.length",       5,                  eval("var arr = new Array(0,1,2,3,4,5); delete arr[0]; arr.length") );
//    array[item++] = new TestCase( SECTION, "var arr = new Array(0,1,2,3,4,5); delete arr[6]; arr.length",       5,                  eval("var arr = new Array(0,1,2,3,4,5); delete arr[6]; arr.length") );

    return ( array );
}

// ----- END VARIATION -----