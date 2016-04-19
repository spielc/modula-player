var getTokenURL="http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=bef50d03aa4fa431554f3bac85147580&format=json";
var authURL="http://www.last.fm/api/auth/?api_key=bef50d03aa4fa431554f3bac85147580&token=$token";
var sessionURL="";
// if ~/.bragi/Scripts/lastfm.sk doesn't exist
//	1. get new token using getTokenURL
//	2. request authentication from user using authURL replacing $token with the token retrieved in 1.
//	3. request web service session using echo -n "api_keybef50d03aa4fa431554f3bac85147580methodauth.getSessiontoken$token66717d5c66601f8f7789cd9f93444252" | md5sum | cut -d' ' -f1
