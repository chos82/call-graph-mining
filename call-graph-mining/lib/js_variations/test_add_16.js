// ----- BEGIN VARIATION -----

// js1_3/Script/new-001.js
    
    function Test_One (x) {
        this.v = x+1;
        return x*2
    }

    function Test_Two( x, y ) {
        this.v = x;
        return y;
    }

    
        Test_One(18);

    new Test_One(18) +"";

    new Test_One(18).v;

    Test_Two(2,7);

    new Test_Two(2,7) +"";

    new Test_Two(2,7).v;

   new (Function)("x","return x+3")(5,6);

   new new Test_Two(String, 2).v(0123) +"";

    new new Test_Two(String, 2).v(0123).length;
	
// ----- END VARIATION -----