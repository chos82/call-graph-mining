// ----- BEGIN VARIATION -----

// js1_2/statements/switch2.js
    
	function foo(i) {
	    switch(i) {
		case "a":
		case "b":
		default:
		    return "ab*"
		case "c":
		    return "c";
		case "d":
		    return "d";
	    }
	    return "";
	}

	foo("a");

	foo("b");

	foo("*");

	foo("c");

	foo("d");

	// Switch on integer; will use TABLESWITCH opcode in C engine
	function goo(i) {
	    switch (i) {
	        case 0:
	        case 1:
                    return 1;
	        case 2:
 	            return 2;
	    }
	    // with no default, control will fall through
	    return 3;
	}

	goo(0);

	goo(1);

	goo(2);

	goo(3);

	// empty switch: make sure expression is evaluated
	var se = 0;
	switch (se = 1) {
	}

	// only default
	se = 0;
	switch (se) {
	    default:
  	        se = 1;
	}
	
	// in loop, break should only break out of switch
	se = 0;
	for (var i=0; i < 2; i++) {
	    switch (i) {
	        case 0:
	        case 1:
	            break;
	    }
	    se = 1;
	}
	
	// test "fall through"
	se = 0;
	i = 0;
	switch (i) {
	    case 0:
	        se++;
		/* fall through */
	    case 1:
	        se++;
	        break;
	}
	
// ----- END VARIATION -----