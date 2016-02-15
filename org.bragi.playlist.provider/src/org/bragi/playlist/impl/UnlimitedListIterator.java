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
package org.bragi.playlist.impl;

import java.util.List;

/**
 * @author christoph
 *
 */
public class UnlimitedListIterator {
	
	private List<Integer> list;
	private int currentIndex;
	private boolean isRepeated;
	
	public UnlimitedListIterator(List<Integer> pList, int pCurrentIndex) {
		list=pList;
		currentIndex=pCurrentIndex;
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
