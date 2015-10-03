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
package org.bragi.engine.vlc.internal.test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bragi.engine.vlc.internal.UnlimitedListIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class UnlimitedListIteratorTest {

	private  UnlimitedListIterator iterator;
	private static final int LIST_SIZE=10;
	
	@Before
	public void setupTest() {
		iterator=new UnlimitedListIterator(IntStream.range(0, LIST_SIZE).boxed().collect(Collectors.toList()));
	}
	
	@Test
	public void nextRepeatedTest() {
		for (int i=0;i<LIST_SIZE;i++)
			Assert.assertEquals(i, iterator.next().intValue());
		iterator.setRepeated(true);
		Assert.assertEquals(0, iterator.next().intValue());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void nextUnrepeatedTest() {
		for (int i=0;i<LIST_SIZE;i++)
			Assert.assertEquals(i, iterator.next().intValue());
		Assert.assertEquals(0, iterator.next().intValue());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void nextEmptyListTest() {
		iterator=new UnlimitedListIterator(new ArrayList<Integer>());
		Assert.assertEquals(0, iterator.next().intValue());
	}
	
	@Test
	public void previousRepeatedTest() {
		iterator.setRepeated(true);
		for (int i=LIST_SIZE-1;i>=0;i--)
			Assert.assertEquals(i, iterator.previous().intValue());
		Assert.assertEquals(9, iterator.previous().intValue());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void previousUnrepeatedTest() {
		for (int i=LIST_SIZE-1;i>=0;i--)
			Assert.assertEquals(i, iterator.previous().intValue());
		Assert.assertEquals(9, iterator.previous().intValue());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void previousEmptyListTest() {
		iterator=new UnlimitedListIterator(new ArrayList<Integer>());
		Assert.assertEquals(0, iterator.previous().intValue());
	}
	
	@Test
	public void setCurrentIndexTest() {
		for (int i=0;i<LIST_SIZE;i++)
			Assert.assertEquals(i, iterator.next().intValue());
		iterator.setCurrentIndex(5);
		Assert.assertEquals(6, iterator.next().intValue());
	}
}
