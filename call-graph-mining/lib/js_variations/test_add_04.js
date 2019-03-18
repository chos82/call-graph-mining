// ----- BEGIN VARIATION -----

/**
    File Name:      ecma/1Boolean/15.6.1.js
    ECMA Section:   15.6.1 The Boolean Function
                    15.6.1.1 Boolean( value )
                    15.6.1.2 Boolean ()
    Description:    Boolean( value ) should return a Boolean value
                    not a Boolean object) computed by
                    Boolean.toBooleanValue( value)

                    15.6.1.2 Boolean() returns false

    Author:         christine@netscape.com
    Date:           27 jun 1997


    Data File Fields:
        VALUE       Argument passed to the Boolean function
        TYPE        typeof VALUE (not used, but helpful in understanding
                    the data file)
        E_RETURN    Expected return value of Boolean( VALUE )
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

    array[item++] = new TestCase( SECTION,   "Boolean(1)",         true,   Boolean(1) );
    array[item++] = new TestCase( SECTION,   "Boolean(0)",         false,  Boolean(0) );
    array[item++] = new TestCase( SECTION,   "Boolean(-1)",        true,   Boolean(-1) );
    array[item++] = new TestCase( SECTION,   "Boolean('1')",       true,   Boolean("1") );
    array[item++] = new TestCase( SECTION,   "Boolean('0')",       true,   Boolean("0") );
    array[item++] = new TestCase( SECTION,   "Boolean('-1')",      true,   Boolean("-1") );
    array[item++] = new TestCase( SECTION,   "Boolean(true)",      true,   Boolean(true) );
    array[item++] = new TestCase( SECTION,   "Boolean(false)",     false,  Boolean(false) );

    array[item++] = new TestCase( SECTION,   "Boolean('true')",    true,   Boolean("true") );
    array[item++] = new TestCase( SECTION,   "Boolean('false')",   true,   Boolean("false") );
    array[item++] = new TestCase( SECTION,   "Boolean(null)",      false,  Boolean(null) );

    array[item++] = new TestCase( SECTION,   "Boolean(-Infinity)", true,   Boolean(Number.NEGATIVE_INFINITY) );
    array[item++] = new TestCase( SECTION,   "Boolean(NaN)",       false,  Boolean(Number.NaN) );
    array[item++] = new TestCase( SECTION,   "Boolean(void(0))",   false,  Boolean( void(0) ) );
    array[item++] = new TestCase( SECTION,   "Boolean(x=0)",       false,  Boolean( x=0 ) );
    array[item++] = new TestCase( SECTION,   "Boolean(x=1)",       true,   Boolean( x=1 ) );
    array[item++] = new TestCase( SECTION,   "Boolean(x=false)",   false,  Boolean( x=false ) );
    array[item++] = new TestCase( SECTION,   "Boolean(x=true)",    true,   Boolean( x=true ) );
    array[item++] = new TestCase( SECTION,   "Boolean(x=null)",    false,  Boolean( x=null ) );
    array[item++] = new TestCase( SECTION,   "Boolean()",          false,  Boolean() );
//    array[item++] = new TestCase( SECTION,   "Boolean(var someVar)",     false,  Boolean( someVar ) );

    return ( array );
}

// ----- END VARIATION -----