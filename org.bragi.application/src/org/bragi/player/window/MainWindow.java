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
package org.bragi.player.window;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.bragi.collection.CollectionInterface;
import org.bragi.engine.EngineInterface;
import org.bragi.player.statemachines.EngineStateChangeListener;
import org.bragi.player.statemachines.EngineStateEnum;
import org.bragi.player.widgets.CollectionWidget;
import org.bragi.player.widgets.PlaylistWidget;
import org.bragi.player.widgets.SeekWidget;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import swing2swt.layout.BorderLayout;

@Component(immediate=true) 
public class MainWindow extends ApplicationWindow implements EngineStateChangeListener { 
 
	
	private Thread thread;
	
	private final AtomicReference<EngineInterface> engine=new AtomicReference<>();
	private List<CollectionInterface> collections;
	private PlaylistInterface playlist;

	private Composite container;
	private Composite engineComposite;
	private Button previousButton;
	private Button playButton;
	private Button stopButton;
	private Button nextButton;
	private int currentSongIndex;
	private EngineStateEnum currentState;
	private Slider volumeSlider;
	private boolean uiInitialized;
	
	private SeekWidget seekWidget;
	private CollectionWidget collectionWidget;
	private PlaylistWidget playlistWidget;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void setEngine(EngineInterface pEngine) {
		engine.set(pEngine);
		if (uiInitialized) {
			getShell().getDisplay().asyncExec(()->{
				if (playlistWidget!=null)
					playlistWidget.setEngine(pEngine);
			});
		}
		currentState=EngineStateEnum.LOADED;
		reinitializePlaylist();
	}
	
	public void unsetEngine(EngineInterface pEngine) {
		if (engine.compareAndSet(pEngine, null) && uiInitialized) {
			getShell().getDisplay().asyncExec(()->{
				if (playlistWidget!=null)
					playlistWidget.setEngine(null);
			});
			currentState=EngineStateEnum.UNLOADED;
		}
	}
	
	@Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addCollection(CollectionInterface collection) {
		if (!collections.contains(collection))
			collections.add(collection);
		if (uiInitialized) {
			getShell().getDisplay().asyncExec(()->{
				if (collectionWidget!=null)
					collectionWidget.addCollection(collection);
			});
		}
	}
	
	public void removeCollection(CollectionInterface collection) {
		collections.remove(collection);
		if (uiInitialized) {
			getShell().getDisplay().asyncExec(()->{
				if (collectionWidget!=null)
					collectionWidget.removeCollection(collection);
			});
		}
	}
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC)
	public void setPlaylist(PlaylistInterface pPlaylist) {
		if (playlist!=pPlaylist) {
			playlist=pPlaylist;
			if (playlist!=null) {
				playlist.load("file:///home/christoph/.bragi/Playlist/current.m3u");
			}
		}
		reinitializePlaylist();
		if (uiInitialized) {
			getShell().getDisplay().asyncExec(()->{
				if (playlistWidget!=null)
					playlistWidget.setPlaylist(pPlaylist);
			});
		}
	}
	
	
	
	public void unsetPlaylist(PlaylistInterface pPlaylist) {
		setPlaylist(pPlaylist);
	}
	

	/**
	 * Create the application window,
	 */
	public MainWindow() {
		super(null);
		uiInitialized = false;
		currentSongIndex = 0;
		collections=new ArrayList<>();
	}
	
	@Activate
	public void activate() {
		createActions();
		addCoolBar(SWT.FLAT);
		addMenuBar();
		addStatusLine();
		//start gui-thread
		Runnable mainLoop = () -> {
			try {
				setBlockOnOpen(true);
				open();
				Display.getCurrent().dispose();
				if (playlist!=null) {
					Path dirPath=Paths.get("/home/christoph/.bragi/Playlist/");
					if (!Files.exists(dirPath))
						Files.createDirectory(dirPath);
					Path playlistPath=dirPath.resolve("current.m3u");
					playlist.save(playlistPath.toUri().toString());
				}
				if (engine!=null) {
					uiInitialized=false;
					Bundle bundle=FrameworkUtil.getBundle(engine.get().getClass());
					bundle.stop();
				}
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		thread=new Thread(mainLoop);
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
			playlistWidget = new PlaylistWidget(container, SWT.NONE);
			playlistWidget.setLayoutData(BorderLayout.EAST);
			playlistWidget.setEngine(engine.get());
			{
				collectionWidget = new CollectionWidget(container, SWT.NONE);
				collectionWidget.setLayoutData(BorderLayout.WEST);
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
								EngineInterface engineObject=engine.get();
								if (engineObject!=null)
									engineObject.backward();
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
								EngineInterface engineObject=engine.get();
								if (engineObject!=null) {
									if (playButton.getText().equals("Play"))
										engineObject.play(currentSongIndex);
									else
										engineObject.pause();
									
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
								EngineInterface engineObject=engine.get();
								if (engineObject!=null)
									engineObject.stop(true);
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
								EngineInterface engineObject=engine.get();
								if (engineObject!=null)
									engineObject.forward();
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
								EngineInterface engineObject=engine.get();
								if (engineObject!=null)
									engineObject.setVolume(volumeSlider.getSelection());
								volumeSlider.setToolTipText(String.valueOf(volumeSlider.getSelection()));
								super.widgetSelected(e);
							}
							
						});
					}
				}
			}
			seekWidget = new SeekWidget(container, SWT.NONE);
			seekWidget.setLayoutData(BorderLayout.NORTH);
			seekWidget.layout();
		}
		// make sure the sub-widgets are correctly initialized
		uiInitialized=true;
		collections.forEach(this::addCollection);
		setPlaylist(playlist);
		setEngine(engine.get());
		return container;
	}
	
	private void reinitializePlaylist() {
		EngineInterface engineObject=engine.get();
		if (engineObject!=null && playlist!=null) {
			List<PlaylistEntry> playlistEntries=playlist.filter("SELECT ARTIST");
			for (int i=playlistEntries.size()-1;i>=0;i--)
				playlist.removeMedia(i);
			// and now add the tracks again (and thus update all interested parties)
			playlistEntries.stream().map(entry->entry.getUri().toString()).forEach(uri->{
				try {
					playlist.addMedia(uri);
				} catch (Exception e) {
					e.printStackTrace();
				}}
			);
			getShell().getDisplay().asyncExec(()->{
				if (playlistWidget!=null)
					playlistWidget.refresh();
			});
		}
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
		getShell().getDisplay().asyncExec(()->{
				boolean isPlayingOrPaused = (currentState==EngineStateEnum.PAUSED) || (currentState==EngineStateEnum.PLAYING);
				stopButton.setEnabled(isPlayingOrPaused);
				nextButton.setEnabled(isPlayingOrPaused);
				previousButton.setEnabled(isPlayingOrPaused); 
				playlistWidget.refresh();
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
					break;
				case UNLOADED:
					break;
				default:
					break;
				
				}
		});
	}

	@Override
	public void stateChange(EngineStateEnum currentState, String engineEvent,
			EngineStateEnum newState, Object... eventData) {
		if (seekWidget==null)
			return;
		if (engineEvent.equals(EngineInterface.VOLUME_CHANGED_EVENT)) {
			getShell().getDisplay().asyncExec(()->{
				int currentVolume=(int) eventData[0];
				volumeSlider.setSelection(currentVolume);
			});
		}
		else if (newState == EngineStateEnum.PLAYING) {
			if (engineEvent.equals(EngineInterface.BACKWARD_EVENT) || engineEvent.equals(EngineInterface.FORWARD_EVENT) || engineEvent.equals(EngineInterface.JUMP_EVENT)) {
				int songIndex=(int) eventData[0];
				playlistWidget.setCurrentSongIndex(songIndex);
				currentSongIndex=songIndex;
			}
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
		playlistWidget.setCurrentState(newState);
		this.currentState=newState;
		updateUi();
		
	}
}
