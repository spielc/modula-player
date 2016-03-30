function Test() {
	this.test = function(evt) { 
		if (ENGINE!=null) { 
			ENGINE.toggleMute(); 
		}
	}
}

var test = new Test();

engine.registerEventHandler("org/bragi/engine/event/*", test, test.test); 

test;
