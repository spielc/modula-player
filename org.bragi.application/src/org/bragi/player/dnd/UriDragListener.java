/**
 * 
 */
package org.bragi.player.dnd;

import java.util.function.Supplier;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

/**
 * @author christoph
 *
 */
public class UriDragListener implements DragSourceListener {
	
	private Supplier<String> dragSourceEventDataSupplier;
	
	public UriDragListener(Supplier<String> supplier) {
		dragSourceEventDataSupplier=supplier;
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
		evt.data=dragSourceEventDataSupplier.get();
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