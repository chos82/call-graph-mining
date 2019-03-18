// ----- BEGIN VARIATION -----

// lc2/JavaToJS/number-002.js

    var testcases = new Array();

    //  In all test cases, the expected type is "object, and the expected
    //  class is "Number"

    var E_TYPE = "number";

    //  Create arrays of actual results (java_array) and expected results
    //  (test_array).

    var java_array = new Array();
    var test_array = new Array();

    var i = 0;

    //  Get a static java field whose type is byte.

    java_array[i] = new JavaValue(  java.lang.Byte.MIN_VALUE );
    test_array[i] = new TestValue(  "java.lang.Byte.MIN_VALUE",
                                    -128 )
    i++;

    // Get a static java field whose type is short.
    java_array[i] = new JavaValue(  java.lang.Short.MIN_VALUE );
    test_array[i] = new TestValue(  "java.lang.Short.MIN_VALUE",
                                    -32768 )
    i++;

    //  Get a static java field whose type is int.

    java_array[i] = new JavaValue( java.lang.Integer.MIN_VALUE );
    test_array[i] = new TestValue( "java.lang.Integer.MIN_VALUE",
                                   -2147483648 )
    i++;


    //  Instantiate a class, and get a field in that class whose type is int.

    var java_rect = new java.awt.Rectangle( 1,2,3,4 );

    java_array[i] = new JavaValue( java_rect.width );
    test_array[i] = new TestValue( "java_object = new java.awt.Rectangle( 1,2,3,4 ); java_object.width",
                                   3 );
    i++;

    //  Get a static java field whose type is long.
    java_array[i] = new JavaValue(  java.lang.Long.MIN_VALUE );
    test_array[i] = new TestValue(  "java.lang.Long.MIN_VALUE",
                                    -9223372036854776000 );
    i++;

    //  Get a static java field whose type is float.
    java_array[i] = new JavaValue(  java.lang.Float.MAX_VALUE );
    test_array[i] = new TestValue(  "java.lang.Float.MAX_VALUE",
                                     3.4028234663852886e+38 )
    i++;

    //  Get a static java field whose type is double.
    java_array[i] = new JavaValue(  java.lang.Double.MAX_VALUE );
    test_array[i] = new TestValue(  "java.lang.Double.MAX_VALUE",
                                     1.7976931348623157e+308 )
    i++;

    //  Get a static java field whose type is char.
    java_array[i] = new JavaValue(  java.lang.Character.MAX_VALUE );
    test_array[i] = new TestValue(  "java.lang.Character.MAX_VALUE",
                                     65535 );
    i++;
    
    function JavaValue( value ) {
    this.value  = value.valueOf();
    this.type   = typeof value;
    return this;
}
function TestValue( description, value, type  ) {
    this.description = description;
    this.value = value;
    this.type =  E_TYPE;
//    this.classname = classname;
    return this;
}

// ----- END VARIATION -----