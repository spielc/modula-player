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

import java.io.File;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.dnd.UriDragListener;
import org.bragi.player.dnd.UriDropAdapter;
import org.bragi.player.helpers.QueryHelpers;
import org.bragi.player.statemachines.EngineStateEnum;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class PlaylistWidget extends Composite {
	
	private int currentSongIndex;
	private EngineStateEnum currentState;
	private Table playlistTable;
	private TableViewer playlistTableViewer;
	private PlaylistInterface playlist;
	private UriDropAdapter dropAdapter;
	private UriDragListener dragListener;
	private MenuItem mntmRepeat;
	private String playlistPath;
	
	private class PlaylistTableLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {
		
		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		@Override
		public String getColumnText(Object data, int column) {
			String newRow=data.toString();
			String header=playlistTableViewer.getTable().getColumn(column).getText();
			return QueryHelpers.extractValueFromLine(header, newRow);
		}

		@Override
		public Color getBackground(Object data) {
			if ((currentState==EngineStateEnum.PLAYING) || (currentState==EngineStateEnum.PAUSED)) {
				String row=data.toString();
				List<String> plist=(List<String>)playlistTableViewer.getInput();
				if ((currentSongIndex-1)==plist.indexOf(row))
					return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
			return super.getBackground(data);
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
		playlistPath=System.getProperty("user.home")+"/.bragi/Playlist/current.m3u";
		currentSongIndex=-1;
		playlistTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		dragListener=new UriDragListener(this::getPlaylistTableViewerDragSourceEventData);
		dropAdapter = new UriDropAdapter(playlistTableViewer);
		int operations = DND.DROP_COPY;
	    Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
	    playlistTableViewer.addDropSupport(operations, transferTypes, dropAdapter);
	    playlistTableViewer.addDragSupport(operations, transferTypes, dragListener);
	    playlistTable = playlistTableViewer.getTable();
		playlistTable.setHeaderVisible(true);
		
		Menu menu = new Menu(playlistTable);
		playlistTable.setMenu(menu);
		
		MenuItem mntmPlaylist = new MenuItem(menu, SWT.CASCADE);
		mntmPlaylist.setText("Playlist");
		
		Menu menu_1 = new Menu(mntmPlaylist);
		mntmPlaylist.setMenu(menu_1);
		
		MenuItem mntmShuffle = new MenuItem(menu_1, SWT.NONE);
		mntmShuffle.setText("Shuffle");
		mntmShuffle.addListener(SWT.Selection, event -> {
			playlist.shuffle();
			playlistTableViewer.refresh();
		});
		
		mntmRepeat = new MenuItem(menu_1, SWT.CHECK);
		mntmRepeat.setText("Repeat");
		mntmRepeat.addListener(SWT.Selection, event -> {
			playlist.setRepeat(mntmRepeat.getSelection());
		});
		
		MenuItem mntmSave = new MenuItem(menu_1, SWT.NONE);
		mntmSave.setText("Save");
		mntmSave.addListener(SWT.Selection, event -> {
			FileDialog dialog=new FileDialog(getShell(), SWT.SAVE|SWT.SINGLE);
			String fileName=dialog.open();
			if (null!=fileName) {
				File f=new File(fileName);
				playlist.save(f.toURI().toString());
				
				if (f.exists())
					MessageDialog.openInformation(getShell(), "Success...", "Saving successfully finished!");
				else
					MessageDialog.openError(getShell(), "Failed...", "Saving failed!");
			}
		});
		
		MenuItem mntmLoad = new MenuItem(menu_1, SWT.NONE);
		mntmLoad.setText("Load");
		mntmLoad.addListener(SWT.Selection, event -> {
			FileDialog dialog=new FileDialog(getShell(), SWT.OPEN|SWT.SINGLE);
			String fileName=dialog.open();
			if (null!=fileName) {
				File f=new File(fileName);
				playlist.load(f.toURI().toString());
				//TODO currently disabled
//				try {
//					if (!playlistFuture.get().isEmpty()) {
//						MessageDialog.openInformation(getShell(), "Success...", "Playlist successfully loaded!");
//						playlistTableViewer.refresh();
//						playlistPath=fileName;
//					}
//					else
//						MessageDialog.openError(getShell(), "Failed...", "Loading of playlist failed!");
//				} catch (Exception e) {
//					MessageDialog.openError(getShell(), "Failed...", "Loading of playlist failed!");
//				}
			}
		});
		
		
		for (MetaDataEnum metaData : EnumSet.of(MetaDataEnum.TITLE, MetaDataEnum.ARTIST, MetaDataEnum.ALBUM)) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(playlistTableViewer, SWT.NONE);
			TableColumn tblclmnExamplecolumn = tableViewerColumn.getColumn();
			tblclmnExamplecolumn.setWidth(100);
			tblclmnExamplecolumn.setText(metaData.name());
		}
		playlistTableViewer.setLabelProvider(new PlaylistTableLabelProvider());
		playlistTableViewer.setContentProvider(new ArrayContentProvider());
		playlistTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				currentSongIndex=playlistTable.getSelectionIndex();
				if (null!=playlist)
					playlist.playMedia(currentSongIndex);
			}
			
		});
		playlistTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode==SWT.DEL)
				{
					if (null!=playlist) {
						int i=0;
						for (int selectedIndex : playlistTable.getSelectionIndices())
							playlist.removeMedia(selectedIndex-(i++));
						playlistTableViewer.refresh();
					}
				}
				super.keyReleased(e);
			}
			
		});
	}
	
	public void setPlaylist(PlaylistInterface pPlaylist) {
		playlist=pPlaylist;
		dropAdapter.setPlaylist(pPlaylist);
		if (null!=playlist) {
			CompletableFuture.supplyAsync(() -> {
				List<PlaylistEntry> playlistEntries = playlist.filter("SELECT ALBUM,ARTIST,TITLE");
				final AtomicInteger i=new AtomicInteger(-1);
				return playlistEntries.stream().map(entry->(i.incrementAndGet())+";;URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).collect(Collectors.toList());
			}).thenAccept(plist-> {
				getDisplay().asyncExec(() -> {
					playlistTableViewer.setInput(plist);
					playlistTableViewer.refresh();
				});
			});
		}
	}
		
	public void setCurrentSongIndex(int newSongIndex) {
		currentSongIndex=newSongIndex;
	}
	
	public void forward() {
		currentSongIndex++;
	}
	
	public void backward() {
		currentSongIndex--;
	}

	public void setCurrentState(EngineStateEnum currentState) {
		this.currentState = currentState;
	}
	
	public String getPlaylistPath() {
		return playlistPath;
	}

	public void refresh() {
		playlistTableViewer.refresh();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private String getPlaylistTableViewerDragSourceEventData() {
		String retValue="";
		if (playlist!=null) {
			final AtomicInteger i=new AtomicInteger(-1);
			IStructuredSelection selection = (IStructuredSelection) playlistTableViewer.getSelection();
			Iterator selectionIterator=selection.iterator();
			while (selectionIterator.hasNext()) {
				Object selectedObject=selectionIterator.next();
				retValue+=selectedObject.toString()+"\n";
			}
		}
		playlistTableViewer.refresh();
		return retValue;
	}
}
