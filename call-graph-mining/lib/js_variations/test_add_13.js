// ----- BEGIN VARIATION -----

// js1_2/regexp/character_class.js
    
String('abcde'.match(new RegExp('ab[ercst]de')));

'abcde'.match(new RegExp('ab[erst]de'));

String('abcdefghijkl'.match(new RegExp('[d-h]+')));

String('abc6defghijkl'.match(new RegExp('[1234567].{2}')));

String('\n\n\abc324234\n'.match(new RegExp('[a-c\\d]+')));

String('abc'.match(new RegExp('ab[.]?c')));

String('abc'.match(new RegExp('a[b]c')));

String('a1b  b2c  c3d  def  f4g'.match(new RegExp('[a-z][^1-9][a-z]')));

String('123*&$abc'.match(new RegExp('[*&$]{3}')));

String('abc'.match(new RegExp('a[^1-9]c')));

'abc'.match(new RegExp('a[^b]c'));

String('abc#$%def%&*@ghi)(*&'.match(new RegExp('[^a-z]{4}')));

String('abc#$%def%&*@ghi)(*&'.match(/[^a-z]{4}/));

// ----- END VARIATION -----