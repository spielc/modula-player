package org.bragi.player.window;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bragi.collection.CollectionInterface;
import org.bragi.engine.EngineInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.dnd.UriDragListener;
import org.bragi.player.dnd.UriDropAdapter;
import org.bragi.player.helpers.QueryHelpers;
import org.bragi.player.model.TreeNode;
import org.bragi.player.statemachines.EngineStateChangeListener;
import org.bragi.player.statemachines.EngineStateEnum;
import org.bragi.player.viewers.CollectionTreeContentProvider;
import org.bragi.player.viewers.CollectionTreeLabelProvider;
import org.bragi.player.widgets.SeekWidget;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import swing2swt.layout.BorderLayout;

@org.osgi.service.component.annotations.Component(immediate=true) 
public class MainWindow extends ApplicationWindow implements EngineStateChangeListener { 

	private class TableLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		@Override
		public String getColumnText(Object data, int column) {
			String newRow=data.toString();
			String header=playlistTableViewer.getTable().getColumn(column).getText();
			return MainWindow.extractValueFromLine(header, newRow);
		}

		@Override
		public Color getBackground(Object data) {
			if ((currentState==EngineStateEnum.PLAYING) || (currentState==EngineStateEnum.PAUSED)) {
				String row=data.toString();
				PlaylistInterface playlist=(PlaylistInterface)playlistTableViewer.getInput();
				Map<URI, Map<MetaDataEnum, String>> playlistEntries = playlist.filter("*", MetaDataEnum.values());
				String[] lines=QueryHelpers.QueryResult2String(playlistEntries).split("\n");
				int index=0;
				for (String line : lines) {
					if (line.equals(row)) {
						if (index==currentSongIndex)
							return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
					}
					index++;
				}
			}
			return super.getBackground(data);
		}
		
		
		
		
		
	}
	
	private class TableContentProvider implements IStructuredContentProvider {

		  @Override
		  public Object[] getElements(Object inputElement) {
			  PlaylistInterface playlist=(PlaylistInterface)inputElement;
			  Map<URI, Map<MetaDataEnum, String>> playlistEntries = playlist.filter("*", MetaDataEnum.values());
			  String[] lines=QueryHelpers.QueryResult2String(playlistEntries).split("\n");
			  return lines;
		  }

		  @Override
		  public void dispose() {
		    
		  }

		  @Override
		  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		  }

		} 
	
	private class GuiThread extends Thread {
		
		private MainWindow mainWindow;
		
		@Override
		public void run() {
			try {
				mainWindow.setBlockOnOpen(true);
				mainWindow.open();
				Display.getCurrent().dispose();
				if (playlist!=null) {
					Path dirPath=Paths.get("/home/christoph/.bragi/Playlist/");
					if (!Files.exists(dirPath))
						Files.createDirectory(dirPath);
					Path playlistPath=dirPath.resolve("current.m3u");
					playlist.save(playlistPath.toUri().toString());
				}
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	private GuiThread thread;
	
	private EngineInterface engine;
	private List<CollectionInterface> collections;
	private Table playlistTable;
	private TableViewer playlistTableViewer;
	private PlaylistInterface playlist;

	private UriDropAdapter dropAdapter;

	private Composite container;
	private Tree collectionTree;
	private TreeViewer collectionTreeViewer;

	private CollectionTreeContentProvider collectionTreeContentProvider;

	private UriDragListener dragListener;
	private Composite engineComposite;
	private Button previousButton;
	private Button playButton;
	private Button stopButton;
	private Button nextButton;
	private int currentSongIndex;
	private EngineStateEnum currentState;
	private Slider volumeSlider;
	
	private SeekWidget seekWidget;
	
	@org.osgi.service.component.annotations.Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void setEngine(EngineInterface pEngine) {
		engine=pEngine;
		currentState=EngineStateEnum.LOADED;
		if (playlist!=null) {
			playlist.load("file:///home/christoph/.bragi/Playlist/current.m3u");
			getShell().getDisplay().asyncExec(new Runnable() {
			    @Override
				public void run() {
					playlistTableViewer.refresh();
			    }
			});
		}
	}
	
	public void unsetEngine(EngineInterface pEngine) {
		engine=null;
		currentState=EngineStateEnum.UNLOADED;
	}
	
	@org.osgi.service.component.annotations.Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addCollection(CollectionInterface collection) {
		collections.add(collection);
		if (collectionTreeContentProvider!=null) {
			collectionTreeContentProvider.addCollection(collection);
			dragListener.setCollection(collection);
		}
	}
	
	public void removeCollection(CollectionInterface collection) {
		collections.remove(collection);
	}
	
	@org.osgi.service.component.annotations.Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void setPlaylist(PlaylistInterface pPlaylist) {
		playlist=pPlaylist;
		if (playlistTableViewer!=null) {
			getShell().getDisplay().asyncExec(new Runnable() {
			    @Override
				public void run() {
			    	playlistTableViewer.setInput(playlist);
					playlistTableViewer.refresh();
			    }
			});
		}
	}
	
	
	
	public void unsetPlaylist(PlaylistInterface pPlaylist) {
		setPlaylist(pPlaylist);
	}
	
	public static String extractValueFromLine(String header, String line) {
		Pattern pattern=Pattern.compile(header+"='([^;;]*)'");
		Matcher matcher=pattern.matcher(line);
		if (matcher.find()) 
			return matcher.group(1);
		else
			return "";
	}
	

	/**
	 * Create the application window,
	 */
	public MainWindow() {
		super(null);
		currentSongIndex = 0;
		collectionTreeContentProvider = new CollectionTreeContentProvider();
		dragListener = new UriDragListener();
		collections=new ArrayList<>();
		createActions();
		addCoolBar(SWT.FLAT);
		addMenuBar();
		addStatusLine();
		//start gui-thread
		thread=new GuiThread();
		thread.mainWindow=this;
		thread.start();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));
		{
			playlistTableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			dropAdapter = new UriDropAdapter(playlistTableViewer);
			int operations = DND.DROP_COPY;
		    Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
		    playlistTableViewer.addDropSupport(operations, transferTypes, dropAdapter);
			playlistTable = playlistTableViewer.getTable();
			playlistTable.setLayoutData(BorderLayout.EAST);
			playlistTable.setHeaderVisible(true);
			{
				collectionTreeViewer = new TreeViewer(container, SWT.BORDER);
				collectionTreeContentProvider.setViewer(collectionTreeViewer);
				dragListener.setTreeViewer(collectionTreeViewer);
				collectionTreeViewer.setContentProvider(collectionTreeContentProvider);
				collectionTreeViewer.setLabelProvider(new CollectionTreeLabelProvider());
				collectionTreeViewer.addDragSupport(operations, transferTypes, dragListener);
				collectionTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
					
					@Override
					public void doubleClick(DoubleClickEvent event) {
						StructuredSelection selection=(StructuredSelection)collectionTreeViewer.getSelection();
						TreeNode node=(TreeNode)selection.getFirstElement();
						if (node.getType().equals(CollectionTreeContentProvider.ROOT) && collections.get(0)!=null) {
							DirectoryDialog dialog=new DirectoryDialog(getShell());
							String directory=dialog.open();
							if (directory!=null) {
								try {
									collections.get(0).addCollectionRoot(new File(directory).toURI().toString());
								} catch (URISyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});
				collectionTree = collectionTreeViewer.getTree();
				collectionTree.setLayoutData(BorderLayout.WEST);
				{
					engineComposite = new Composite(container, SWT.NONE);
					engineComposite.setLayoutData(BorderLayout.SOUTH);
					engineComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
					{
						previousButton = new Button(engineComposite, SWT.NONE);
						previousButton.setEnabled(false);
						previousButton.setText("<");
						previousButton.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseDown(MouseEvent e) {
								if (engine!=null)
									engine.backward();
								super.mouseDown(e);
							}
							
						});
					}
					{
						playButton = new Button(engineComposite, SWT.NONE);
						playButton.setText("Play");
						playButton.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseDown(MouseEvent e) {
								if (engine!=null) {
									if (playButton.getText().equals("Play"))
										engine.play(currentSongIndex);
									else
										engine.pause();
									
								}
								super.mouseDown(e);
							}
							
						});
					}
					{
						stopButton = new Button(engineComposite, SWT.NONE);
						stopButton.setEnabled(false);
						stopButton.setText("Stop");
						stopButton.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseDown(MouseEvent e) {
								if (engine!=null)
									engine.stop(true);
								super.mouseDown(e);
							}
							
						});
					}
					{
						nextButton = new Button(engineComposite, SWT.NONE);
						nextButton.setEnabled(false);
						nextButton.setText(">");
						nextButton.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseDown(MouseEvent e) {
								if (engine!=null) {
									engine.forward();
									
								}
								super.mouseDown(e);
							}
							
						});
					}
					{
						volumeSlider = new Slider(engineComposite, SWT.NONE);
						volumeSlider.setMinimum(0);
						volumeSlider.setMaximum(200);
						volumeSlider.setThumb(1);
						volumeSlider.setIncrement(1);
						volumeSlider.setPageIncrement(10);
						volumeSlider.addSelectionListener(new SelectionAdapter() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								if (engine!=null)
									engine.setVolume(volumeSlider.getSelection());
								volumeSlider.setToolTipText(String.valueOf(volumeSlider.getSelection()));
								super.widgetSelected(e);
							}
							
						});
					}
				}
				collectionTreeViewer.setInput(new Object());
			}
			{
				for (MetaDataEnum metaData : EnumSet.of(MetaDataEnum.TITLE, MetaDataEnum.ARTIST, MetaDataEnum.ALBUM)) {
					TableViewerColumn tableViewerColumn = new TableViewerColumn(playlistTableViewer, SWT.NONE);
					TableColumn tblclmnExamplecolumn = tableViewerColumn.getColumn();
					tblclmnExamplecolumn.setWidth(100);
					tblclmnExamplecolumn.setText(metaData.name());
				}
				
			}
			playlistTableViewer.setLabelProvider(new TableLabelProvider());
			playlistTableViewer.setContentProvider(new TableContentProvider());
			playlistTableViewer.setInput(playlist);
			playlistTableViewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					currentSongIndex=playlistTable.getSelectionIndex();
					engine.play(currentSongIndex);
				}
				
			});
			playlistTable.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					if (e.keyCode==SWT.DEL)
					{
						if (playlist!=null) {
							StructuredSelection selection=(StructuredSelection)playlistTableViewer.getSelection();
							Iterator iter=selection.iterator();
							while(iter.hasNext()) {
								String line=iter.next().toString();
								String uriString=MainWindow.extractValueFromLine("URI", line);
								playlist.removeMedia(uriString);
							}
							playlistTableViewer.refresh();
						}
					}
					super.keyReleased(e);
				}
				
			});
			seekWidget = new SeekWidget(container, SWT.NONE);
			seekWidget.setLayoutData(BorderLayout.NORTH);
			seekWidget.layout();
		}
		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the coolbar manager.
	 * @return the coolbar manager
	 */
	@Override
	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolBarManager = new CoolBarManager(style);
		return coolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage("test");
		statusLineManager.update(true);
		return statusLineManager;
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Bragi");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(689, 437);
	}

	/**
	 * Update UI based on current state of the engine
	 */
	private void updateUi() {
		getShell().getDisplay().asyncExec(new Runnable() {
		
			@Override
			public void run() {
				boolean isPlayingOrPaused = (currentState==EngineStateEnum.PAUSED) || (currentState==EngineStateEnum.PLAYING);
				stopButton.setEnabled(isPlayingOrPaused);
				nextButton.setEnabled(isPlayingOrPaused);
				previousButton.setEnabled(isPlayingOrPaused && (currentSongIndex>0)); 
				playlistTableViewer.refresh();
				switch(currentState) {
				case INVALID:
					break;
				case LOADED:
					playButton.setText("Play");
					playButton.setEnabled(true);
					previousButton.setEnabled(false);
					break;
				case PAUSED:
					playButton.setText("Play");
					break;
				case PLAYING:
					playButton.setText("Pause");
					if (engine!=null)
						volumeSlider.setSelection(engine.getVolume());
					break;
				case UNLOADED:
					break;
				default:
					break;
				
				}
				
			}
		});
	}

	@Override
	public void stateChange(EngineStateEnum currentState, String engineEvent,
			EngineStateEnum newState, Object... eventData) {
		if (seekWidget==null)
			return;
		if (newState == EngineStateEnum.PLAYING) {
			if (engineEvent.equals(EngineInterface.BACKWARD_EVENT) || engineEvent.equals(EngineInterface.FORWARD_EVENT) || engineEvent.equals(EngineInterface.JUMP_EVENT))
				currentSongIndex = (int) eventData[0];
			else if (engineEvent.equals(EngineInterface.DURATION_CHANGED_EVENT)) {
				try {
					int songDuration = (int) eventData[0];
					seekWidget.durationChanged(songDuration);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (currentState == EngineStateEnum.PAUSED)
				seekWidget.resume();
		}
		else if (newState == EngineStateEnum.PAUSED)
			seekWidget.pause();
		else if (newState == EngineStateEnum.LOADED)
			seekWidget.terminate();
		this.currentState=newState;
		updateUi();
		
	}
}
