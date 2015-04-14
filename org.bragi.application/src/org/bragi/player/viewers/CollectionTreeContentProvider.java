package org.bragi.player.viewers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bragi.collection.CollectionEntry;
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
		//TODO TreeNode.query needs to track the exact query to the current node
		TreeNode parent=((TreeNode)obj);
		String query="";
		if (parent.getType()!=CollectionTreeContentProvider.ROOT) {
			query=parent.getType()+":\""+parent.getValue()+"\" AND ";
		}
		MetaDataEnum meta=null;
		if (parent.getType()==CollectionTreeContentProvider.ROOT)
			meta=MetaDataEnum.ARTIST;
		else if (parent.getType()==MetaDataEnum.ARTIST.name())
			meta=MetaDataEnum.ALBUM;
		else if (parent.getType()==MetaDataEnum.ALBUM.name())
			meta=MetaDataEnum.TITLE;
		else if (parent.getType()==MetaDataEnum.TITLE.name())
			return null;
		List<TreeNode> children=new ArrayList<>();
		CollectionInterface collection=collections.get(0);
		List<String> entries=new ArrayList<>();
		for(char character='A';character<='Z';character++) {
			String newQuery=query+meta.name()+":"+character+"*";
			List<CollectionEntry> tracks=collection.filter(newQuery,MetaDataEnum.values());
			for(CollectionEntry trackEntry : tracks) {
				URI trackURI=trackEntry.getUri();
				Map<MetaDataEnum, String> metaData=trackEntry.getMetaData();
				String entry=metaData.get(meta);
				if (entries.contains(entry))
					continue;
				entries.add(entry);
				TreeNode child=new TreeNode();
				String childQuery="";
				if (parent.getType()!=CollectionTreeContentProvider.ROOT)
					childQuery=parent.getQuery()+" AND ";
				childQuery+=meta.name()+":\""+entry+"\"";
				child.setQuery(childQuery);
				System.out.println(child.getQuery());
				child.setValue(entry);
				child.setType(meta.name());
				child.setParent(parent);
				children.add(child);
			}
		}
		return children.toArray();
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