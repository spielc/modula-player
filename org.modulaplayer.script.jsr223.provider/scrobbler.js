var getTokenURL="http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=bef50d03aa4fa431554f3bac85147580&format=json";
var authURL="http://www.last.fm/api/auth/?api_key=bef50d03aa4fa431554f3bac85147580&token=$token";
var sessionURL="";
var MetaDataEnum=MetaDataEnumClass.static;
var sk="";
// if ~/.bragi/Data/lastfm.sk doesn't exist
//	1. get new token using getTokenURL
//	2. request authentication from user using authURL replacing $token with the token retrieved in 1.
//	3. request web service session. On the commandline the message signature can be calculated like this: echo -n "api_keybef50d03aa4fa431554f3bac85147580methodauth.getSessiontoken$token66717d5c66601f8f7789cd9f93444252" | md5sum | cut -d' ' -f1
// then for every trackchange:
// var artist=get artist
// var track=get track
// var timestamp=timestamp (UTC!!!)
// curl --data "artist=$artist&track=$track&timestamp=$timestamp&api_key=bef50d03aa4fa431554f3bac85147580&api_sig=4d0e837d63a2618c408736b3dc9e0ced&sk=2b19d6abdccc11a6825bde6ba305e16c&method=track.scrobble" http://ws.audioscrobbler.com/2.0

// testdata for generating the api-signature
//api_keybef50d03aa4fa431554f3bac85147580artistCalibanmethodtrack.scrobblesk2b19d6abdccc11a6825bde6ba305e16ctimestamp1461172285trackKing66717d5c66601f8f7789cd9f93444252 => 4d0e837d63a2618c408736b3dc9e0ced

// this helper-method takes a java.util.HashMap-object, sorts the keys alphabetically and returns a string of the keys and values
// which can be hashed to generate a valid signature
function prepReqDataForSigning(data) { 
	var sortedKeys=data.keySet().stream().sorted().toArray(); 
	var retValue=""; 
	for each (var key in sortedKeys) { 
		retValue=retValue+key+data.get(key); 
	} 
	retValue=retValue+"66717d5c66601f8f7789cd9f93444252"; 
	return retValue 
};

// this helper-method can be used to generate a last.fm-valid api-call signature
function sign(data) {
	var toDigest=prepReqDataForSigning(data);
	var md5=java.security.MessageDigest.getInstance("MD5");
	var hash=md5.digest(toDigest.getBytes());
	var hexString="";
	for each (var byte in hash) { 
		hexString=hexString+java.lang.String.format("%02x",byte); 
	}
	return hexString;
}

function scrobble(artist, track, timestamp) {
	var requestData=new java.util.HashMap();
	requestData.put("api_key", "bef50d03aa4fa431554f3bac85147580");
	requestData.put("method", "track.scrobble");
	requestData.put("sk", sk);
	requestData.put("artist", artist);
	requestData.put("track", track);
	requestData.put("timestamp", timestamp);
	var signedData=sign(requestData);
	requestData.put("api_sig", signedData);
	requestData.put("format", "json");
	var postData="";
	for each (var key in requestData.keySet()) {
		postData=postData+key+"="+requestData.get(key)+"&";
	}
	postData=postData.substr(0,postData.lastIndexOf("&"));
	//TODO call httpPost with correct params
	print(postData);
	var serverResponse=httpPost("http://ws.audioscrobbler.com/2.0", postData, "");
	var resultMessage="Scrobbling of '"+track+"' by '"+artist+"' at "+timestamp;
	if (serverResponse.statusCode!=200) {
		//TODO queue postData for later scrobble
		resultMessage=resultMessage+" FAILED!";
	}
	else {
		resultMessage=resultMessage+" SUCCEEDED!";
	}
	print(resultMessage);
}

function httpPost(theUrl, data, contentType){
    var con = new java.net.URL(theUrl).openConnection();

    con.requestMethod = "POST";
    //con.setRequestProperty("Content-Type", contentType);

    // Send post request
    con.doOutput=true;
    write(con.outputStream, data);

    return asResponse(con);
}

function asResponse(con){
    var d = read(con.inputStream);

    return {data : d, statusCode : con.responseCode};
}

function write(outputStream, data){
    var wr = new java.io.DataOutputStream(outputStream);
    wr.writeBytes(data);
    wr.flush();
    wr.close();
}

function read(inputStream){
    var inReader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
    var inputLine;
    var response = new java.lang.StringBuffer();

    while ((inputLine = inReader.readLine()) != null) {
           response.append(inputLine);
    }
    inReader.close();
    return response.toString();
}

function eventHandler(event) {
	if (null!=PLAYLIST) {
		var index=event.getProperty("org/bragi/playlist/eventData/INDEX");
		var playlistEntries=PLAYLIST.filter("SELECT ARTIST,TITLE");
		var currentEntry=playlistEntries.get(index);
		var artist=currentEntry.getMetaData().get(MetaDataEnum.ARTIST);
		var track=currentEntry.getMetaData().get(MetaDataEnum.TITLE);
		var currentTick=java.time.Clock.tickSeconds(java.time.Clock.systemUTC().getZone());
		var currentTime=java.lang.String.valueOf(currentTick.millis()/1000);
		//print(artist+":"+track+":"+currentTime);
		scrobble(artist, track, currentTime);
	}    
}

var skFile=new java.io.File(java.lang.System.getProperty("user.home")+"/.bragi/Data/lastfm.sk");
if (skFile.exists()) {
	var skStream=java.nio.file.Files.newInputStream(skFile.toPath());
	var skData=JSON.parse(read(skStream));
	sk=skData.session.key;
} else {
	// TODO request new session-key
}

//var currentTime = java.time.Clock.tickSeconds(java.time.Clock.systemUTC().getZone());
//scrobble("Strapping Young Lad", "The New Black", java.lang.String.valueOf(currentTime.millis()/1000));
engine.registerEventHandler("org/bragi/playlist/event/INDEX_CHANGED", this, eventHandler);
this;
//var toSign=prepReqDataForSigning(data);
//var md5=java.security.MessageDigest.getInstance("MD5");
//md5.update(toSign.getBytes());
//todo write function for converting the md5-digest to a valid string
