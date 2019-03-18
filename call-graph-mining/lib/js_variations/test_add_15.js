// ----- BEGIN VARIATION -----

// js1_3/inherit/proto_1.js
    
function Employee () {
     this.name = "";
     this.dept = "general";
}
function Manager () {
     this.reports = [];
}
Manager.prototype = new Employee();

function WorkerBee () {
     this.projects = new Array();
}
WorkerBee.prototype = new Employee();

function SalesPerson () {
    this.dept = "sales";
    this.quota = 100;
}
SalesPerson.prototype = new WorkerBee();

function Engineer () {
    this.dept = "engineering";
    this.machine = "";
}
Engineer.prototype = new WorkerBee();


    var jim = new Employee();

    jim.name;

	jim.dept;

    var sally = new Manager();

    sally.name;
    
    sally.dept;

    sally.reports.length;

    typeof sally.reports;

    var fred = new SalesPerson();

    fred.name;

    fred.dept;

    fred.quota;

    fred.projects.length;

    var jane = new Engineer();

    jane.name;

    jane.dept;

    jane.projects.length;

    jane.machine;
	
// ----- END VARIATION -----