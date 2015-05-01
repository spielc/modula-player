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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.bragi.collection.CollectionInterface;
import org.bragi.player.dnd.UriDragListener;
import org.bragi.player.model.TreeNode;
import org.bragi.player.viewers.CollectionTreeContentProvider;
import org.bragi.player.viewers.CollectionTreeLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

public class CollectionWidget extends Composite {

	private CollectionTreeContentProvider collectionTreeContentProvider;
	private List<CollectionInterface> collections;
	private TreeViewer collectionTreeViewer;
	private UriDragListener dragListener;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CollectionWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		int operations = DND.DROP_COPY;
	    Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
	    collectionTreeContentProvider=new CollectionTreeContentProvider();
	    dragListener=new UriDragListener();
	    collections=new ArrayList<>();
		collectionTreeViewer = new TreeViewer(this, SWT.BORDER);
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
		collectionTreeViewer.setInput(new Object());
	}
	
	public void addCollection(CollectionInterface collection) {
		collections.add(collection);
		collectionTreeContentProvider.addCollection(collection);
		collectionTreeViewer.refresh();
		dragListener.setCollection(collection);
	}
	
	public void removeCollection(CollectionInterface collection) {
		collections.remove(collection);
		collectionTreeContentProvider.removeCollection(collection);
		collectionTreeViewer.refresh();
		// TODO probably this is needs to be thought through
		//dragListener.setCollection(null);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	
}
