/**
 * 
 */
package org.bragi.player.helpers;

import org.bragi.engine.EngineInterface;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author christoph
 *
 */
public class ModulaPlayerBundleActivator implements BundleActivator {
	
	private ModulaPlayerServiceTracker<EngineInterface, EngineInterface> tracker;

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		tracker=new ModulaPlayerServiceTracker<>(context, EngineInterface.class);
		tracker.open();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}

}
