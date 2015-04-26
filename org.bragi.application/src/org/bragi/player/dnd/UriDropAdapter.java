/**
 * 
 */
package org.bragi.player.dnd;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.viewers.TableViewer;
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
	
	private TableViewer viewer;
	
	public UriDropAdapter(TableViewer pViewer) {
		super(pViewer);
		viewer=pViewer;
	}

	@Override
	public boolean performDrop(Object data) {
		System.out.println("performDrop");
		String droppedData=data.toString();
		PlaylistInterface playlist=(PlaylistInterface) viewer.getInput();
		if (playlist!=null) {
			List<PlaylistEntry> playlistEntries = playlist.filter("*", MetaDataEnum.values());
			int location = getCurrentLocation();
			Object currentTarget = getCurrentTarget();
			final AtomicInteger i=new AtomicInteger(-1);
			List<String> lines=playlistEntries.stream().map(entry->(i.incrementAndGet())+";;URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).collect(Collectors.toList());
			int index=lines.indexOf(currentTarget);
			index = (index!=-1) ? index : lines.size();
			switch(location) {
			case LOCATION_AFTER:
			case LOCATION_ON:
				index++;
				break;
			}
			final int finalIndex=(index<0) ? 0 : index;
			Pattern pattern = Pattern.compile(".*URI='([^;]*)'");
			Stream<String> droppedDataLine=Arrays.asList(droppedData.split("\n")).stream();
			droppedDataLine.map(line->pattern.matcher(line))
						   .filter(matcher->matcher.find())
						   .map(matcher->matcher.group(1)).forEach(uri->{
								if (currentTarget==null) {
									try {
										playlist.addMedia(uri);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								else
									playlist.insertMedia(finalIndex, uri);
						   });
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
	
	
		
}
