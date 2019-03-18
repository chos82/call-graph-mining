// ----- BEGIN VARIATION -----

// ecma_3/Statements/switch-001.js
var cnMatch = 'Match';
var cnNoMatch = 'NoMatch';

actual = match(17, f(fInverse(17)), f, fInverse);
expect = cnMatch;

actual = match(17, 18, f, fInverse);
expect = cnNoMatch;

actual = match(1, 1, Math.exp, Math.log);
expect = cnMatch;

actual = match(1, 2, Math.exp, Math.log);
expect = cnNoMatch;

actual = match(1, 1, Math.sin, Math.cos);
expect = cnNoMatch;

/*
 * If F,G are inverse functions and x==y, this should return cnMatch -
 */
function match(x, y, F, G)
{
  switch (x)
  {
    case F(G(y)):
      return cnMatch;

    default:
      return cnNoMatch;
  }
}

function f(m)
{
  return 2*(m+1);
}


function fInverse(n)
{
  return (n-2)/2;
}

// ----- END VARIATION -----