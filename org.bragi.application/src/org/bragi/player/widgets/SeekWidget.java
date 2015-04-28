/**
 * 
 */
package org.bragi.player.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

/**
 * This widget is used for seeking in the currently played song and for displaying the current progress of the song
 * @author christoph
 *
 */
public class SeekWidget extends Composite {
	
	/**
	 * Helper enum for the current state of the progressThread
	 * @author christoph
	 *
	 */
	private enum ThreadState {
		RUNNING,
		STOPPED,
		PAUSED
	}

	/**
	 * This inner class implements the thread that is used to frequently update the progress of the song. 
	 * @author christoph
	 *
	 */
	private class SongProgressThread extends Thread {

		// the current state of the thread
		private ThreadState state;
		// the current second
		private int second;
		// the duration of the currently playing song
		private int songDuration;
		
		/**
		 * Constructor
		 * @param pSongDuration the duration of the currently playing song
		 */
		private SongProgressThread(int pSongDuration) {
			state=ThreadState.STOPPED;
			second=0;
			songDuration=pSongDuration;
		}
		
		/**
		 * Run method of the thread
		 */
		@Override
		public void run() {
			try {
				// set the state of the thread to ThreadState.RUNNING
				state=ThreadState.RUNNING;
				// while the state of the thread is either ThreadState.RUNNING or ThreadState.PAUSED and second is less than songDuration
				while ((state==ThreadState.RUNNING || state==ThreadState.PAUSED) && (second<songDuration)) {
					// Check threadstate again => we only want to advance the slider and update the label if the thread is actually running
					if (state==ThreadState.RUNNING) {
						// update second
						second+=1000;
						// update slider and label
						updateUI();
					}
					// sleep for a second before looping
					Thread.sleep(1000);
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} finally {
				// always set the state of the thread to stopped if we are about to end the execution of the thread
				state=ThreadState.STOPPED;
			}
		}
		
		/**
		 * This method stops the thread.
		 */
		public void terminate() {
			// set state to ThreadState.STOPPED;
			state=ThreadState.STOPPED;
			// set second to 0
			second=0;
			// update slider and label
			updateUI();
		}

		/**
		 * This method is used to update slider and label
		 */
		private void updateUI() {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						seekSlider.setSelection(second);
						updateTimeLabel();
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
				
			});
		}
		
		/**
		 * Pause the thread
		 */
		public void pause() {
			// set state of the thread to ThreadState.PAUSED
			state=ThreadState.PAUSED;
		}
		
		/**
		 * Resume the thread
		 */
		public void unpause() {
			// set state of the thread to ThreadState.PAUSED
			state=ThreadState.RUNNING;
		}
		
	}
	
	// Slider widget
	private Slider seekSlider;
	// Label widget
	private Label timeLabel;
	// SongProgressThread
	private SongProgressThread progressThread;

	/**
	 * Constructor
	 * @param parent parent widget
	 * @param style style of the current
	 */
	public SeekWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		seekSlider=new Slider(this, SWT.HORIZONTAL);
		timeLabel=new Label(this, SWT.NONE);
		timeLabel.setText("00:00/00:00");
	}

	/**
	 * This method is called if the duration of a song changes (typically happens if you switch to the next song)
	 * @param songDuration the duration of the currently playing song
	 */
	public void durationChanged(final int songDuration) {
		// if this method is called and the progressThread is still running => kill it
		if((progressThread!=null) && (progressThread.state!=ThreadState.STOPPED)) {
			progressThread.terminate();
		}
		
		// set the new maximum value of the slider and update the label displaying the progress
		getDisplay().asyncExec(new Runnable() {
		
			@Override
			public void run() {
				try {
					seekSlider.setMaximum(songDuration);
					updateTimeLabel();
					layout();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// start a new progressThread
		progressThread=new SongProgressThread(songDuration);
		progressThread.start();
	}
	
	/**
	 * Pause the progressThread
	 */
	public void pause() {
		if (progressThread!=null)
			progressThread.pause();
	}
	
	/**
	 * Resume the progressThread
	 */
	public void resume() {
		if (progressThread!=null)
			progressThread.unpause();
	}
	
	/**
	 * Kill the progressThread
	 */
	public void terminate() {
		if (progressThread!=null)
			progressThread.terminate();
	}
	
	/**
	 * This method is responsible for updating the label which displays the progress of the current song
	 */
	private void updateTimeLabel() {
		Date durationDate=new Date(progressThread.songDuration);
		Date secondDate=new Date(progressThread.second);
		SimpleDateFormat format=new SimpleDateFormat("mm:ss");
		timeLabel.setText(format.format(secondDate)+"/"+format.format(durationDate));
	}
}
