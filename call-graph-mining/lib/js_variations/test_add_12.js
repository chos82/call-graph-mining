// ----- BEGIN VARIATION -----

// js1_2/function/tostring-1.js

var tab = "    ";

    t1 = new TestFunction_foo( "stub", "value", tab + "return value;" );

    t2 = new TestFunction_foo( "ToString", "object", tab+"return object + \"\";" );

    t3 = new TestFunction_foo( "Add", "a, b, c, d, e",  tab +"var s = a + b + c + d + e;\n" +
                        tab + "return s;" );

    t4 = new TestFunction_foo( "noop", "value" );

    t5 = new TestFunction_foo( "anonymous", "", tab+"return \"hello!\";" );

    var f = new Function( "return \"hello!\"");

                                    t1.valueOf();
                                    stub.toString();

    
                                    t2.valueOf();
                                    ToString.toString();

                                    t3.valueOf();
                                    Add.toString();

                                    t4.toString();
                                    noop.toString();

                                    t5.toString();
                                    f.toString();

function noop( value ) {
}
function Add( a, b, c, d, e ) {
    var s = a + b + c + d + e;
    return s;
}
function stub( value ) {
    return value;
}
function ToString( object ) {
    return object + "";
}

function ToBoolean( value ) {
    if ( value == 0 || value == NaN || value == false ) {
        return false;
    } else {
        return true;
    }
}

function TestFunction_foo( name, args, body ) {
    if ( name == "anonymous" && version() == 120 ) {
        name = "";
    }

    this.name = name;
    this.arguments = args.toString();
    this.body = body;

    /* the format of Function.toString() in JavaScript 1.2 is:
    /n
    function name ( arguments ) {
        body
    }
    */
    this.value = "\nfunction " + (name ? name : "" )+
    "("+args+") {\n"+ (( body ) ? body +"\n" : "") + "}\n";

    this.toString = new Function( "return this.value" );
    this.valueOf = new Function( "return this.value" );
    return this;
}


// ----- END VARIATION -----