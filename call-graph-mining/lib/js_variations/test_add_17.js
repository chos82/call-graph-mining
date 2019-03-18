// ----- BEGIN VARIATION -----

// js1_5/Array/array-001.js

var FooBound = 0;
var BIG_INDEX = 4294967290;
var status = '';
var statusitems = [];
var actual = '';
var actualvalues = [];
var expect= '';
var expectedvalues = [];


var arr = Array(BIG_INDEX);
arr[BIG_INDEX - 1] = 'a';
arr[BIG_INDEX - 10000] = 'b';
arr[BIG_INDEX - 0.5] = 'c';  // not an array index - but a valid property name
// Truncate the array -
arr.length = BIG_INDEX - 5000;


// Enumerate its properties with for..in
var s = '';
for (var i in arr)
{
  s += arr[i];
}


/*
 * We expect s == 'cb' or 'bc' (EcmaScript does not fix the order).
 * Note 'c' is included: for..in includes ALL enumerable properties,
 * not just array-index properties. The bug was: Rhino gave s == ''.
 */

actual = sortThis(s);
expect = 'bc';
addThis_foo();

function sortThis(str)
{
  var chars = str.split('');
  chars = chars.sort();
  return chars.join('');
}


function addThis_foo()
{
  statusitems[FooBound] = status;
  actualvalues[FooBound] = actual;
  expectedvalues[FooBound] = expect;
  FooBound++;
}

// ----- END VARIATION -----