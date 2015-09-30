/**
 * 
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
