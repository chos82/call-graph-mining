// ----- BEGIN VARIATION -----

/**
 *  File Name:          RegExp/properties-001.js
 *  ECMA Section:       15.7.6.js
 *  Description:        Based on ECMA 2 Draft 7 February 1999
 *
 *  Author:             christine@netscape.com
 *  Date:               19 February 1999
 */

AddRegExpCases_foo( new RegExp, "",   false, false, false, 0 );
    AddRegExpCases_foo( /.*/,       ".*", false, false, false, 0 );
    AddRegExpCases_foo( /[\d]{5}/g, "[\\d]{5}", true, false, false, 0 );
    AddRegExpCases_foo( /[\S]?$/i,  "[\\S]?$", false, true, false, 0 );
    AddRegExpCases_foo( /^([a-z]*)[^\w\s\f\n\r]+/m,  "^([a-z]*)[^\\w\\s\\f\\n\\r]+", false, false, true, 0 );
    AddRegExpCases_foo( /[\D]{1,5}[\ -][\d]/gi,      "[\\D]{1,5}[\\ -][\\d]", true, true, false, 0 );
    AddRegExpCases_foo( /[a-zA-Z0-9]*/gm, "[a-zA-Z0-9]*", true, false, true, 0 );
    AddRegExpCases_foo( /x|y|z/gim, "x|y|z", true, true, true, 0 );

    AddRegExpCases_foo( /\u0051/im, "\\u0051", false, true, true, 0 );
    AddRegExpCases_foo( /\x45/gm, "\\x45", true, false, true, 0 );
    AddRegExpCases_foo( /\097/gi, "\\097", true, true, false, 0 );

function AddRegExpCases_foo( re, s, g, i, m, l ) {

    RegExp.prototype.test;

   RegExp.prototype.toString;

    RegExp.prototype.constructor;

    RegExp.prototype.compile;

    RegExp.prototype.exec;

    // properties

	re.toString();

}

// ----- END VARIATION -----