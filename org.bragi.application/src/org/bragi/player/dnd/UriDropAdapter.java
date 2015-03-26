/**
 * 
 */
package org.bragi.player.dnd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.helpers.QueryHelpers;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

/**
 * @author christoph
 *
 */
public class UriDropAdapter extends ViewerDropAdapter {
	
	private Viewer viewer;
	
	public UriDropAdapter(Viewer pViewer) {
		super(pViewer);
		viewer=pViewer;
	}

	@Override
	public boolean performDrop(Object data) {
		System.out.println("performDrop");
		String droppedData=data.toString();
		PlaylistInterface playlist=(PlaylistInterface) viewer.getInput();
		if (playlist!=null) {
//			if (viewer instanceof TableViewer) {
//				TableViewer tableViewer=(TableViewer) viewer;
				Map<URI, Map<MetaDataEnum, String>> playlistEntries = playlist.filter("*", MetaDataEnum.values());
				String[] lines=QueryHelpers.QueryResult2String(playlistEntries).split("\n");
				int location = getCurrentLocation();
				Object currentTarget = getCurrentTarget();
				String target=(currentTarget==null)?"":currentTarget.toString();
				int index=0;
				for (String line : lines) {
					if (line.equals(target)) {
						switch(location) {
						case LOCATION_AFTER:
						case LOCATION_ON:
							index++;
							break;
						case LOCATION_NONE:
							index=lines.length;
							break;
						}
						for (String droppedDataLine : droppedData.split("\n")) {
//							if (playlist!=null) {
								//TODO regex broken for for URIs that contain '
								Pattern pattern=Pattern.compile("URI='([^;]*)'");
								Matcher matcher=pattern.matcher(droppedDataLine);
								if (matcher.find()) { 
									String songUri=matcher.group(1);
									if (target.equals("")) {
										try {
											playlist.addMedia(songUri);
										} catch (URISyntaxException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									else
//									try {
//										playlist.addMedia(songUri);
										playlist.insertMedia(index, songUri);
//									} catch (URISyntaxException e) {
//										e.printStackTrace();
//									}
								}
//							}
						}
						break;
					}
					index++;
				}			
			}
			
//		}
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
	
	
		
}
