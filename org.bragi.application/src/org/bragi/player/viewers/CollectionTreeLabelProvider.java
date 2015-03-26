package org.bragi.player.viewers;

import org.bragi.player.model.TreeNode;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

public class CollectionTreeLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		TreeNode node = (TreeNode) cell.getElement();
		cell.setText(node.getValue());
		super.update(cell);
	}
	
	
	
}