/**
 * 
 */
package org.bragi.player.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author christoph
 * @param <S>
 * @param <T>
 *
 */
public class ModulaPlayerServiceTracker<S, T> extends ServiceTracker<S, T> {
	
	private List<Consumer<T>> addConsumers;
	private List<Consumer<T>> removeConsumers;
	
	public ModulaPlayerServiceTracker(BundleContext context, Class<S> clazz) {
		super(context, clazz, null);
		addConsumers=new ArrayList<>();
		removeConsumers=new ArrayList<>();
	}
	
	
	
	@Override
	public T addingService(ServiceReference<S> reference) {
		T service=super.addingService(reference);
		addConsumers.forEach(action->action.accept(service));
		return service;
	}



	@Override
	public void removedService(ServiceReference<S> reference, T service) {
		super.removedService(reference, service);
		removeConsumers.forEach(action->action.accept(service));
	}



	public void addAddConsumer(Consumer<T> consumer) {
		addConsumers.add(consumer);
	}
	
	public void removeAddConsumer(Consumer<T> consumer) {
		addConsumers.remove(consumer);
	}
	
	public void addRemoveConsumer(Consumer<T> consumer) {
		removeConsumers.add(consumer);
	}
	
	public void removeRemoveConsumer(Consumer<T> consumer) {
		removeConsumers.remove(consumer);
	}
}
