// ----- BEGIN VARIATION -----
/*
* Contributor(s): pschwartau@netscape.com
* Date: 14 Mar 2001
*
* SUMMARY: Testing the internal [[Class]] property of user-defined types.
* See ECMA-262 Edition 3 13-Oct-1999, Section 8.6.2 re [[Class]] property.
*
* Same as class-001.js - but testing user-defined types here, not native types.
* Therefore we expect the [[Class]] property to equal 'Object' in each case - 
*
* The getJSClass() function we use is in a utility file, e.g. "shell.js"
*/

// checks that it's safe to call findType()
// taken from ecma_3/Object/shell.js
var cnNoObject = 'Unexpected Error!!! Parameter to this function must be an object';
var cnNoClass = 'Unexpected Error!!! Cannot find Class property';
var cnObjectToString = Object.prototype.toString;


// checks that it's safe to call findType()
function getJSType(obj)
{
  if (isObject(obj))
    return findType(obj);
  return cnNoObject;
}


// checks that it's safe to call findType()
function getJSClass(obj)
{
  if (isObject(obj))
    return findClass(findType(obj));
  return cnNoObject;
}


function findType(obj)
{
  return cnObjectToString.apply(obj);
}


// given '[object Number]',  return 'Number'
function findClass(sType)
{
  var re =  /^\[.*\s+(\w+)\s*\]$/;
  var a = sType.match(re);
  
  if (a && a[1])
    return a[1];
  return cnNoClass;
}


function isObject(obj)
{
  return obj instanceof Object;
}
// -----------------------------


var status = ''; var statusList = [ ];
var actual = ''; var actualvalue = [ ];
var expect= ''; var expectedvalue = [ ];


Calf.prototype= new Cow();

/*
 * We set the expect variable each time only for readability.
 * We expect 'Object' every time; see discussion above -
 */
status = 'new Cow()';
actual = getJSClass(new Cow());
expect = 'Object';
addThis();

status = 'new Calf()';
actual = getJSClass(new Calf());
expect = 'Object';
addThis();


function addThis()
{
  statusList[UBound] = status;
  actualvalue[UBound] = actual;
  expectedvalue[UBound] = expect;
  UBound++;
}

function getStatus(i)
{
  return statprefix + statusList[i];
}


function Cow(name)
{
  this.name=name;
}


function Calf(name)
{
  this.name=name;
}

// ----- END VARIATION -----