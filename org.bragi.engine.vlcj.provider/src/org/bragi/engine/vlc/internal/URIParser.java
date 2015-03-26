/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package org.bragi.engine.vlc.internal;

import java.net.URI;
import java.net.URISyntaxException;

import uk.co.caprica.vlcj.mrl.CdMrl;
import uk.co.caprica.vlcj.mrl.FileMrl;
import uk.co.caprica.vlcj.mrl.FtpMrl;
import uk.co.caprica.vlcj.mrl.HttpMrl;
import uk.co.caprica.vlcj.mrl.Mrl;
import uk.co.caprica.vlcj.mrl.UrlMrl;

/**
 * Final helper class that can be used get a vlc-mrl from a uri.
 * 
 * Currently ftp-, http-, file- and cdda-uris are supported
 * @author christoph
 *
 */
public final class URIParser {
	
	/**
	 * Try to get an MRL for the given uri.
	 * @param uriString the URI
	 * @return a MRL for the given uri
	 * @throws URISyntaxException if the uri-string is invalid
	 */
	public static Mrl getMrl(String uriString) throws URISyntaxException {
		URI uri=new URI(uriString);
		Mrl mrl=null;
		UrlMrl urlMrl=null;
		switch(uri.getScheme()) {
		case "ftp":
			urlMrl=new FtpMrl();
		case "http":
			if (urlMrl==null) //only try to create a new UrlMrl if urlMrl is null (necessary because of fall-through case
				urlMrl=new HttpMrl();
			urlMrl=urlMrl.host(uri.getHost()).port(uri.getPort()).path(uri.getPath());
			mrl=urlMrl;
			break;
		case "file":
			mrl=new FileMrl().file(uri.getPath());
			break;
		case "cdda":
			String path=uri.getPath();
			int atIndex=path.indexOf('@');
			String device=path.substring(0, path.lastIndexOf('/')); //name of the device
			if (atIndex==-1) //no @ in the URI=>the name of the device is the complete path
				device=uri.getPath();
			CdMrl cdMrl=new CdMrl().device(device);
			if (atIndex!=-1) //the URI contains @-sign=>get the tracknumber
				cdMrl=cdMrl.track(Integer.parseInt(path.substring(atIndex+1)));
			mrl=cdMrl;
			break;
		default:
			return null;
		}
		return mrl;
	}
	
	
}
