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
package org.bragi.player.widgets;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bragi.engine.EngineInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.dnd.UriDropAdapter;
import org.bragi.player.statemachines.EngineStateEnum;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class PlaylistWidget extends Composite {
	
	private int currentSongIndex;
	private EngineStateEnum currentState;
	private Table playlistTable;
	private TableViewer playlistTableViewer;
	private PlaylistInterface playlist;
	private UriDropAdapter dropAdapter;
	private EngineInterface engine;
	
	private class PlaylistTableLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {
		
		public String extractValueFromLine(String header, String line) {
			Pattern pattern=Pattern.compile(header+"='([^;;]*)'");
			Matcher matcher=pattern.matcher(line);
			if (matcher.find()) 
				return matcher.group(1);
			else
				return "";
		}

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		@Override
		public String getColumnText(Object data, int column) {
			String newRow=data.toString();
			String header=playlistTableViewer.getTable().getColumn(column).getText();
			return extractValueFromLine(header, newRow);
		}

		@Override
		public Color getBackground(Object data) {
			if ((currentState==EngineStateEnum.PLAYING) || (currentState==EngineStateEnum.PAUSED)) {
				String row=data.toString();
				PlaylistInterface playlist=(PlaylistInterface)playlistTableViewer.getInput();
				List<String> lines=Arrays.asList(playlist2StringArray(playlist));
				if (currentSongIndex==lines.indexOf(row))
					return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
			return super.getBackground(data);
		}
	}
	
	private class PlaylistTableContentProvider implements IStructuredContentProvider {

		  @Override
		  public Object[] getElements(Object inputElement) {
			  PlaylistInterface playlist=(PlaylistInterface)inputElement;
			  Object[] lines = playlist2StringArray(playlist);
			  return lines;
		  }

		  @Override
		  public void dispose() {
		    
		  }

		  @Override
		  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		  }

		} 

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PlaylistWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		playlistTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		dropAdapter = new UriDropAdapter(playlistTableViewer);
		int operations = DND.DROP_COPY;
	    Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
	    playlistTableViewer.addDropSupport(operations, transferTypes, dropAdapter);
	    playlistTable = playlistTableViewer.getTable();
		playlistTable.setHeaderVisible(true);
		for (MetaDataEnum metaData : EnumSet.of(MetaDataEnum.TITLE, MetaDataEnum.ARTIST, MetaDataEnum.ALBUM)) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(playlistTableViewer, SWT.NONE);
			TableColumn tblclmnExamplecolumn = tableViewerColumn.getColumn();
			tblclmnExamplecolumn.setWidth(100);
			tblclmnExamplecolumn.setText(metaData.name());
		}
		playlistTableViewer.setLabelProvider(new PlaylistTableLabelProvider());
		playlistTableViewer.setContentProvider(new PlaylistTableContentProvider());
		playlistTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				currentSongIndex=playlistTable.getSelectionIndex();
				if (engine!=null)
					engine.play(currentSongIndex);
			}
			
		});
		playlistTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode==SWT.DEL)
				{
					if (playlist!=null) {
						for (int selectedIndex : playlistTable.getSelectionIndices())
							playlist.removeMedia(selectedIndex);
						playlistTableViewer.refresh();
					}
				}
				super.keyReleased(e);
			}
			
		});
	}
	
	public void setPlaylist(PlaylistInterface pPlaylist) {
		playlist=pPlaylist;
		playlistTableViewer.setInput(pPlaylist);
		playlistTableViewer.refresh();
	}
	
	public void setEngine(EngineInterface pEngine) {
		engine=pEngine;
	}
	
	public void setCurrentSongIndex(int currentSongIndex) {
		this.currentSongIndex = currentSongIndex;
	}

	public void setCurrentState(EngineStateEnum currentState) {
		this.currentState = currentState;
	}

	public void refresh() {
		playlistTableViewer.refresh();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private static String[] playlist2StringArray(PlaylistInterface playlist) {
		List<PlaylistEntry> playlistEntries = playlist.filter("*", MetaDataEnum.values());
		final AtomicInteger i=new AtomicInteger(-1);
		String[] lines=playlistEntries.stream().map(entry->(i.incrementAndGet())+";;URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).toArray(String[]::new);
		return lines;
	}
	
	
}
