// ----- BEGIN VARIATION -----

// js1_5/Scopr/scope-004.js

var A = 'global A';
var B = 'global B';
var C = 'global C';
var D = 'global D';

// an object with 'C' and 'D' properties -
var objTEST = new Object();
objTEST.C = C;
objTEST.D = D;

status = 'Section 1 of test';
with (new Object())
{
  actual = A;
  expect = 'global A';
}

status = 'Section 2 of test';
with (Function)
{
  actual = B;
  expect = 'global B';
}



status = 'Section 3 of test';
with (this)
{
  actual = C;
  expect = 'global C';
}



status = 'Section 4 of test';
localA();


status = 'Section 5 of test';
localB();


status = 'Section 6 of test';
localC();


status = 'Section 7 of test';
localC(new Object());


status = 'Section 8 of test';
localC.apply(new Object());


status = 'Section 9 of test';
localC.apply(new Object(), [objTEST]);


status = 'Section 10 of test';
localC.apply(objTEST, [objTEST]);


status = 'Section 11 of test';
localD(new Object());


status = 'Section 12 of test';
localD.apply(new Object(), [objTEST]);


status = 'Section 13 of test';
localD.apply(objTEST, [objTEST]);


// contains a with(new Object()) block -
function localA()
{
  var A = 'local A';

  with(new Object())
  {
    actual = A;
    expect = 'local A';
  }
}


// contains a with(Number) block -
function localB()
{
  var B = 'local B';

  with(Number)
  {
    actual = B;
    expect = 'local B';
  }
}


// contains a with(this) block -
function localC(obj)
{
  var C = 'local C';

  with(this)
  {
    actual = C;
  }

  if ('C' in this)
    expect = this.C;
  else
    expect = 'local C';
}


// contains a with(obj) block -
function localD(obj)
{
  var D = 'local D';

  with(obj)
  {
    actual = D;
  }

  if ('D' in obj)
    expect = obj.D;
  else
    expect = 'local D';
}

// ----- END VARIATION -----