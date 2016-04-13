package org.bragi.player.window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.modulaplayer.script.AbstractScriptEngine;

public class ScriptDialog extends Dialog {
		
	private class ScriptListLabelProvider extends LabelProvider implements IColorProvider {
		
		@Override
		public Color getBackground(Object arg0) {
			return null;
		}

		@Override
		public Color getForeground(Object obj) {
			String value=obj.toString();
			List<String> runningScripts=scriptEngines.stream().flatMap(engine->engine.getRunningScripts().stream()).collect(Collectors.toList());
			int color=SWT.COLOR_BLACK;
			if (runningScripts.contains(value))
				color=SWT.COLOR_GREEN;
			return Display.getCurrent().getSystemColor(color);
		}
		
	}
	
	private Collection<AbstractScriptEngine> scriptEngines;
	private Table scriptList;
	private TableViewer scriptListViewer;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ScriptDialog(Shell parentShell, Collection<AbstractScriptEngine> pScriptEngines) {
		super(parentShell);
		scriptEngines = pScriptEngines;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		scriptListViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		scriptList = scriptListViewer.getTable();
		scriptListViewer.setLabelProvider(new ScriptListLabelProvider());
		
		
		Menu menu = new Menu(scriptList);
		scriptList.setMenu(menu);
		
		MenuItem mntmLoadScript = new MenuItem(menu, SWT.NONE);
		mntmLoadScript.setText("Load script...");
		mntmLoadScript.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog=new FileDialog(getShell(),SWT.OPEN);
				dialog.setText("Load script...");
				String scriptPath = dialog.open();
				if (null!=scriptPath && null!=scriptEngines) {
					File scriptFile=new File(scriptPath);
					String[] splittedPath=scriptPath.split("\\.");
					Optional<AbstractScriptEngine> scriptEngine=scriptEngines.stream().filter(engine -> engine.getExtensions().contains(splittedPath[splittedPath.length-1])).findFirst();
					if (scriptEngine.isPresent()) {
						InputDialog scriptNameDialog=new InputDialog(getShell(), "Name of the script", "Enter a name for the script", "", text -> {
							if (null==text || text.isEmpty())
								return "Name must neither be null nor empty!";
							return null;
						});
						if (scriptNameDialog.open()==Window.OK) {
							try {
								byte[] script=Files.readAllBytes(scriptFile.toPath());
								AbstractScriptEngine scriptEngineInstance = scriptEngine.get();
								scriptEngineInstance.loadScript(scriptNameDialog.getValue(), new String(script));
								fillScriptList();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							
						}
					}
					else 
						MessageDialog.openError(getShell(), "Load error", "No suitable ScriptEngine found!");
				}
				super.widgetSelected(e);
			}
			
			
			
		});
		
		MenuItem mntmStartScript = new MenuItem(menu, SWT.NONE);
		mntmStartScript.setEnabled(false);
		mntmStartScript.setText("Start script");
		mntmStartScript.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String scriptName=scriptList.getItem(scriptList.getSelectionIndex()).getText();
				Optional<AbstractScriptEngine> engine=scriptEngines.stream().filter(eng->eng.getLoadedScripts().contains(scriptName)).findFirst();
				if(engine.isPresent()) {
					try {
						engine.get().runScript(scriptName);
						fillScriptList();
					} catch (Exception ex) {
						MessageDialog.openError(getShell(), "Script execution failed", ex.getMessage());
					}
				}
				super.widgetSelected(e);
			}
			
		});
		
		MenuItem mntmStopScript = new MenuItem(menu, SWT.NONE);
		mntmStopScript.setEnabled(false);
		mntmStopScript.setText("Stop script");
		mntmStopScript.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String scriptName=scriptList.getItem(scriptList.getSelectionIndex()).getText();
				Optional<AbstractScriptEngine> engine=scriptEngines.stream().filter(eng->eng.getRunningScripts().contains(scriptName)).findFirst();
				if(engine.isPresent()) {
					engine.get().stopScript(scriptName);
				}
				super.widgetSelected(e);
			}
			
		});
		
		scriptListViewer.addSelectionChangedListener(event->{
			boolean isSelectionEmpty=event.getSelection().isEmpty();
			mntmStartScript.setEnabled(!isSelectionEmpty);
			mntmStopScript.setEnabled(!isSelectionEmpty);
		});
		
		fillScriptList();

		return container;
	}
	
	private void fillScriptList() {
		List<String> loadedScripts=scriptEngines.stream().flatMap(engine->engine.getLoadedScripts().stream()).collect(Collectors.toList());
		if (!loadedScripts.isEmpty()) {
			String[] lScripts=new String[loadedScripts.size()];
			loadedScripts.toArray(lScripts);
			IntStream.range(0, scriptList.getItemCount()).forEach(idx -> {
				scriptListViewer.remove(0);
			});
			scriptListViewer.refresh();
			scriptListViewer.add(lScripts);
			//scriptListViewer.refresh();
		}
		
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
}
