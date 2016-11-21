// index.js

// request message on server
//Calls SimpleServlet to get the "Hello World" message
xhrGet("Controller?act=getRecommendations", function(responseText){
	// add to document
	var mytitle = document.getElementById('message');
	console.log(responseText);
		
	//---------------
	var test = JSON.parse(responseText);
	console.log(test);
	var result="";
	for(var i=0; i < test.length ; i++){
		result += test[i];
		result += "<br>";
	}
	
	//--------------------
	mytitle.innerHTML = result;

}, function(err){
	console.log(err);
});

//utilities
function createXHR(){
	if(typeof XMLHttpRequest != 'undefined'){
		return new XMLHttpRequest();
	}else{
		try{
			return new ActiveXObject('Msxml2.XMLHTTP');
		}catch(e){
			try{
				return new ActiveXObject('Microsoft.XMLHTTP');
			}catch(e){}
		}
	}
	return null;
}
function xhrGet(url, callback, errback){
	var xhr = new createXHR();
	xhr.open("GET", url, true);
	xhr.onreadystatechange = function(){
		if(xhr.readyState == 4){
			if(xhr.status == 200){
				callback(xhr.responseText);
			}else{
				errback('service not available');
			}
		}
	};
	xhr.timeout = 3000;
	xhr.ontimeout = errback;
	xhr.send();
}

//function parseJson(str){
//	return window.JSON ? JSON.parse(str) : eval('(' + str + ')');
//}
//function prettyJson(str){
//	// If browser does not have JSON utilities, just print the raw string value.
//	return window.JSON ? JSON.stringify(JSON.parse(str), null, '  ') : str;
//}

