/**
 * 
 */
package org.bragi.player.dnd;

import java.util.List;

import org.bragi.collection.CollectionEntry;
import org.bragi.collection.CollectionInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.helpers.QueryHelpers;
import org.bragi.player.model.TreeNode;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

/**
 * @author christoph
 *
 */
public class UriDragListener implements DragSourceListener {
	
	private TreeViewer treeViewer;
	private CollectionInterface collection;
	
	public UriDragListener() {
		
	}
	
	
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}



	public void setTreeViewer(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}



	/**
	 * 
	 * @return
	 */
	public CollectionInterface getCollection() {
		return collection;
	}


	/**
	 * 
	 * @param collection
	 */
	public void setCollection(CollectionInterface collection) {
		this.collection = collection;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragFinished(DragSourceEvent evt) {
		System.out.println("Finished Drag");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragSetData(DragSourceEvent evt) {
		System.out.println("dragSetData");
		StructuredSelection selection=(StructuredSelection)treeViewer.getSelection();
		TreeNode node=(TreeNode)selection.getFirstElement();
		System.out.println(node.getQuery());
//		StringBuffer buffer=new StringBuffer();
//		if (collection!=null) {
//			Map<URI, Map<MetaDataEnum, String>> filteredCollection=collection.filter(obj.getQuery(), MetaDataEnum.values());
//			for (Map.Entry<URI, Map<MetaDataEnum, String>> filteredCollectionEntry : filteredCollection.entrySet()) {
//				buffer.append("URI='"+filteredCollectionEntry.getKey().toString()+"'");
//				for (Map.Entry<MetaDataEnum, String> metaData : filteredCollectionEntry.getValue().entrySet()) {
//					buffer.append(";;");
//					buffer.append(metaData.getKey().name()+"='"+metaData.getValue()+"'");
//				}
//				buffer.append("\n");
//			}
//		}
		if (collection!=null) {
			List<CollectionEntry> filteredCollection=collection.filter(node.getQuery(), MetaDataEnum.values());
			evt.data=QueryHelpers.QueryResult2String(filteredCollection);
		}
		else
			evt.data="";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragStart(DragSourceEvent evt) {
		System.out.println("Start Drag");
		evt.doit=true;
	}
	
	

}
