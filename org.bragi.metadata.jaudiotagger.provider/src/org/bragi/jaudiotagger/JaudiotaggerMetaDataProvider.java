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
package org.bragi.jaudiotagger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

/**
 * Implementation of the MetaDataProviderInterface using the Jaudiotagger-library
 * @author christoph 
 *
 */
@org.osgi.service.component.annotations.Component
public class JaudiotaggerMetaDataProvider implements MetaDataProviderInterface {
	
	private static final Map<MetaDataEnum,FieldKey> metaDataEnumToFieldKeyMap=createMetaDataEnumToFieldKeyMap();
	
	@Override
	public String[] getMetaData(String uri, EnumSet<MetaDataEnum> metaData)	throws URISyntaxException {
		try {
			URI uriObject=URI.create(uri);
			AudioFile f = AudioFileIO.read(new File(uriObject));
			Tag tag=f.getTag();
			if (tag!=null) {
				String[] metaDataArray=new String[metaData.size()];
				int i=0;
				for (MetaDataEnum metaDataEnum : metaData) {
					metaDataArray[i++]=tag.getFirst(metaDataEnumToFieldKeyMap.get(metaDataEnum));
				}
				return metaDataArray;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[]{};
	}

	@Override
	public byte[] getArtwork(String uri) throws URISyntaxException {
		try {
			URI uriObject=URI.create(uri);
			AudioFile f = AudioFileIO.read(new File(uriObject));
			Tag tag=f.getTag();
			if (tag!=null) {
				Artwork artwork=tag.getFirstArtwork();
				if (artwork!=null)
					return artwork.getBinaryData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[]{};
	}

	@Override
	public Date getDate(String uri) throws URISyntaxException {
		try {
			URI uriObject=URI.create(uri);
			AudioFile f = AudioFileIO.read(new File(uriObject));
			Tag tag = f.getTag();
			if (tag!=null) {
				String yearString=tag.getFirst(FieldKey.YEAR);
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				Date date=format.parse(yearString+"-01-01");
				return date;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Map<MetaDataEnum,FieldKey> createMetaDataEnumToFieldKeyMap() {
		Hashtable<MetaDataEnum, FieldKey> map=new Hashtable<>();
		EnumSet<MetaDataEnum> metaDataEnumSet=EnumSet.allOf(MetaDataEnum.class);
		for (MetaDataEnum metaDataEnum : metaDataEnumSet) {
			FieldKey key=null;
			switch (metaDataEnum) {
			case ALBUM:
				key=FieldKey.ALBUM;
				break;
			case ARTIST:
				key=FieldKey.ARTIST;
				break;
//			case ARTWORK_URL:
//				break;
//			case COPYRIGHT:
//				break;
//			case DESCRIPTION:
//				break;
			case ENCODED_BY:
				key=FieldKey.ENCODER;
				break;
			case GENRE:
				key=FieldKey.GENRE;
				break;
			case LANGUAGE:
				key=FieldKey.LANGUAGE;
				break;
			case PUBLISHER:
				key=FieldKey.PRODUCER;
				break;
			case RATING:
				key=FieldKey.RATING;
				break;
//			case SETTING:
//				break;
			case TITLE:
				key=FieldKey.TITLE;
				break;
			case TRACK_ID:
				key=FieldKey.KEY;
				break;
			case TRACK_NUMBER:
				key=FieldKey.TRACK;
				break;
			case URL:
				key=FieldKey.URL_WIKIPEDIA_RELEASE_SITE;
				break;
			default:
				key=FieldKey.TITLE;
				break;
			}
			map.put(metaDataEnum, key);
		}
		return map;
	}

}
