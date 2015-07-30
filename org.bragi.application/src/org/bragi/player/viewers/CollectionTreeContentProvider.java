package org.bragi.player.viewers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bragi.collection.CollectionInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.player.model.TreeNode;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * The ITreeContentProvider-implementation for the collection-tree.
 * 
 * Currently hardcoded to look like this:
 * null
 *  |->root
 *      |->artist
 *          |->album
 *              |->title
 *                  |->null
 * @author christoph
 *
 */
public class CollectionTreeContentProvider implements ITreeContentProvider {
	
	public static final String ROOT="ROOT";
	
	private TreeNode structureTree;
	private List<CollectionInterface> collections;

	private TreeViewer viewer;
	
	public CollectionTreeContentProvider() {
		
		structureTree=new TreeNode();
		structureTree.setType(CollectionTreeContentProvider.ROOT);
		structureTree.setValue("Local collection");
		structureTree.setParent(null);
		structureTree.setQuery("SELECT *");
		collections=new ArrayList<>();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * 
	 * @param viewer
	 */
	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public void addCollection(CollectionInterface collection) {
		collections.add(collection);
	}
	
	public void removeCollection(CollectionInterface collection) {
		collections.remove(collection);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
	}
	
	

	@Override
	public Object[] getChildren(Object obj) {
		TreeNode parent=((TreeNode)obj);
		String query=parent.getQuery();
		String childQuery=query;
		MetaDataEnum meta=null;
		if (parent.getType()==CollectionTreeContentProvider.ROOT) {
			meta=MetaDataEnum.ARTIST;
			childQuery+=" WHERE ";
		}
		else if (parent.getType()==MetaDataEnum.ARTIST.name())
			meta=MetaDataEnum.ALBUM;
		else if (parent.getType()==MetaDataEnum.ALBUM.name())
			meta=MetaDataEnum.TITLE;
		else if (parent.getType()==MetaDataEnum.TITLE.name())
			return null;
		CollectionInterface collection=collections.get(0);
		final MetaDataEnum realMeta=meta;
		final String realChildQuery=childQuery;
		List<TreeNode> children=collection.filter(query).stream().map(entry->entry.getMetaData().get(realMeta)).distinct().map(value->createChildNode(parent, realMeta, realChildQuery, value)).collect(Collectors.toList());
		return children.toArray();
	}
	
	private static TreeNode createChildNode(TreeNode parent, MetaDataEnum meta, String childQuery, String value) {
		TreeNode child=new TreeNode();
		child.setValue(value);
		child.setParent(parent);
		if (parent.getType()!=CollectionTreeContentProvider.ROOT)
			childQuery+=" AND ";
		childQuery+=meta.name()+"=\""+value+"\"";
		child.setQuery(childQuery);
		child.setType(meta.name());
		return child;
	}

	@Override
	public Object[] getElements(Object obj) {
		TreeNode node=null;
		if (obj instanceof Object) 
			node=structureTree;
		else
			node=(TreeNode)obj;
		return new Object[] { node };
	}

	@Override
	public Object getParent(Object obj) {
		if (obj == null) {
			return null;
		}
		return ((TreeNode)obj).getParent();
	}

	@Override
	public boolean hasChildren(Object obj) {
		return (!collections.isEmpty()) && (((TreeNode)obj).getType()!=MetaDataEnum.TITLE.name());
	}
	
}