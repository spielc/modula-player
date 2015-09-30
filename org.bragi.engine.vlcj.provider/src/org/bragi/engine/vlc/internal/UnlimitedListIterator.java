/**
 * 
 */
package org.bragi.engine.vlc.internal;

import java.util.List;

/**
 * @author christoph
 *
 */
public class UnlimitedListIterator {
	
	private List<Integer> list;
	private int currentIndex;
	private boolean isRepeated;
	
	public UnlimitedListIterator(List<Integer> pList) {
		list=pList;
		currentIndex=-1;
		isRepeated=false;
	}

	private boolean hasNext() {
		return (currentIndex<0 && list.size()>0) || (currentIndex>=0 && currentIndex<list.size()-1);
	}

	private boolean hasPrevious() {
		return (currentIndex>0);
	}

	public Integer next() {
		if (!hasNext() && isRepeated)
			currentIndex = -1;
		currentIndex++;
		return list.get(currentIndex);
	}

	public Integer previous() {
		if (!hasPrevious() && isRepeated)
			currentIndex = list.size();
		currentIndex--;
		return list.get(currentIndex);
	}

	public boolean isRepeated() {
		return isRepeated;
	}

	public void setRepeated(boolean isRepeated) {
		this.isRepeated = isRepeated;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}
}
