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
package org.bragi.player.dnd;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author christoph
 *
 */
public class UriDropAdapter extends ViewerDropAdapter {
	
	private TableViewer viewer;
	private PlaylistInterface playlist;
	
	public UriDropAdapter(TableViewer pViewer) {
		super(pViewer);
		viewer=pViewer;
	}
	
	private PlaylistEntry playlistEntryFromString(String playlistEntryString) {
		Optional<String> uriString=Arrays.asList(playlistEntryString.split(";;")).stream().filter(part->part.contains("URI")).findFirst();
		if (uriString.isPresent()) {
			PlaylistEntry entry=new PlaylistEntry();
			String realURIString=uriString.get().replace("URI='", "");
			realURIString=realURIString.substring(0, realURIString.length()-1);
			entry.setUri(URI.create(realURIString));
			return entry;
		}
		return null;
	}

	@Override
	public boolean performDrop(Object data) {
		System.out.println("performDrop");
		String droppedData=data.toString();
		if (playlist!=null) {
			List<PlaylistEntry> playlistEntries = playlist.filter("SELECT *");
			int location = getCurrentLocation();
			Object currentTarget = getCurrentTarget();
			int targetIndex=playlistEntries.size();
			if (currentTarget!=null) {
				targetIndex=Integer.parseInt(currentTarget.toString().split(";;")[0]);
				switch (location) {
				case LOCATION_AFTER:
					targetIndex++;
					break;
				default:
					break;
				}
			}
			
			final AtomicInteger deleteIndexAdjustment=new AtomicInteger(0);
			final AtomicInteger indexAdjustment=new AtomicInteger(0);
			final int realTargetIndex=targetIndex;
			Arrays.asList(droppedData.split("\n")).stream().forEach(line->{
				int playlistSize=playlistEntries.size();
				Pattern pattern = Pattern.compile("^(\\d+);;");
				Matcher matcher=pattern.matcher(line);
				if (matcher.find()) {
					int deleteIndex=Integer.parseInt(matcher.group(1));
					System.out.println("deleteIndex<realTargetIndex"+(deleteIndex<realTargetIndex));
					if (deleteIndex<realTargetIndex) {
						indexAdjustment.decrementAndGet();
						deleteIndex-=deleteIndexAdjustment.getAndIncrement();
					}
					playlist.removeMedia(deleteIndex);
					playlistSize--;
				}
				pattern = Pattern.compile(".*URI='([^;]*)'");
				matcher=pattern.matcher(line);
				if (matcher.find())
					try {
						playlist.insertMedia(realTargetIndex+indexAdjustment.getAndIncrement(), matcher.group(1));
					} catch (Exception e) {
						e.printStackTrace();
					}
			});
//			PlaylistEntry currentTargetEntry=playlistEntryFromString(currentTarget.toString());
//			int currentIndex=Integer.parseInt(currentTarget.toString().split(";;")[0]);
//			int bla=playlistEntries.indexOf(currentTargetEntry);
//			final AtomicInteger i=new AtomicInteger(-1);
//			List<String> lines=playlistEntries.stream().map(entry->(i.incrementAndGet())+";;URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).collect(Collectors.toList());
//			//int currentIndex=lines.indexOf(currentTarget);
//			currentIndex = (currentIndex!=-1) ? currentIndex : lines.size();
////			switch(location) {
////			case LOCATION_AFTER:
////			case LOCATION_ON:
////				currentIndex++;
////				break;
////			}
//			final int realCurrentIndex=currentIndex;
//			final AtomicInteger indexAdjustment=new AtomicInteger(0);
//			final AtomicInteger insertionCounter=new AtomicInteger(0);
//			Arrays.asList(droppedData.split("\n")).stream().filter(line->lines.indexOf(line)!=-1).forEach(line->{
//				int droppedIndex=lines.indexOf(line);
//				if (droppedIndex<realCurrentIndex)
//					indexAdjustment.decrementAndGet();
//				playlist.removeMedia(droppedIndex);
//				Pattern pattern = Pattern.compile(".*URI='([^;]*)'");
//				Matcher matcher=pattern.matcher(line);
//				if (matcher.find())
//					playlist.insertMedia(realCurrentIndex+indexAdjustment.get()+insertionCounter.incrementAndGet(), matcher.group(1));
//			});
//			final int finalIndex=(index<0) ? 0 : index;
//			Pattern pattern = Pattern.compile(".*URI='([^;]*)'");
//			Stream<String> droppedDataLine=Arrays.asList(droppedData.split("\n")).stream();
//			droppedDataLine.map(line->pattern.matcher(line))
//						   .filter(matcher->matcher.find())
//						   .map(matcher->matcher.group(1)).forEach(uri->{
//								if (currentTarget==null) {
//									try {
//										playlist.addMedia(uri);
//									} catch (Exception e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//								}
//								else
//									playlist.insertMedia(finalIndex, uri);
//						   });
		}
		viewer.refresh();
		return true;
	}

	@Override
	public boolean validateDrop(Object arg0, int arg1, TransferData data) {
		System.out.println("validateDrop");
		return true;
	}
	
	@Override
	public void drop(DropTargetEvent event) {
		System.out.println("drop");
		int location=determineLocation(event);
	    String target = (String) determineTarget(event);
	    String translatedLocation ="";
	    switch (location){
	    case 1 :
	      translatedLocation = "Dropped before the target ";
	      break;
	    case 2 :
	      translatedLocation = "Dropped after the target ";
	      break;
	    case 3 :
	      translatedLocation = "Dropped on the target ";
	      break;
	    case 4 :
	      translatedLocation = "Dropped into nothing ";
	      break;
	    }
		super.drop(event);
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		super.dragEnter(event);
		event.detail = DND.DROP_COPY; // before or after call to super
	}
	
	public void setPlaylist(PlaylistInterface pPlaylistInterface) {
		playlist=pPlaylistInterface;
	}
		
}
