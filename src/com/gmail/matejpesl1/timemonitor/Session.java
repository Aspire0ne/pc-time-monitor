package com.gmail.matejpesl1.timemonitor;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;

import com.gmail.matejpesl1.timemonitor.Main.Mode;

public class Session extends Thread {
	public static final int IDLE_TIME_STARTING_VALUE = 60;
	InputListener listener;
	private StopWatch stopwatch;
	private StopWatch idleTimeTimer;
	public boolean running;
	private int deadline;
	public long remainingTime;
	private long totalIdleTime;
	private boolean inactive;
	private long elapsedTime;
	int idleCount;
	private Main main;
	
	public Session(int deadline, Main main) {
		idleTimeTimer = new StopWatch();
		this.deadline = deadline;
		this.main = main;
		stopwatch = new StopWatch();
		running = true;
	}
	private void startStopwatch() {
		if (!stopwatch.isStarted()) {
			stopwatch.start();	
		}
	}
	
	private void startIdleTimeTimer() {
		if (!idleTimeTimer.isStarted()) {
			idleTimeTimer.start();
		}
	}
	
	public long getElapsedTime(TimeUnit unit) {
		return elapsedTime;
	}
	
	public long getTotalIdleTime(TimeUnit unit) {
		return totalIdleTime;
	}
	
	public void setStopwatchPausedState(boolean state) {
		if (state == true) {
			if (!stopwatch.isSuspended() && stopwatch.isStarted()) {
				stopwatch.suspend();
			}
			if (!idleTimeTimer.isSuspended() && idleTimeTimer.isStarted()) {
				idleTimeTimer.suspend();
			}
		} else {
			if (stopwatch.isSuspended() && stopwatch.isStarted()) {
				stopwatch.resume();
			}
			if (idleTimeTimer.isSuspended() && idleTimeTimer.isStarted()) {
				idleTimeTimer.resume();
			}
		}
	}
	
	public void resetIdleTimer() {
		if (idleTimeTimer.getTime() > 1000) {
			idleTimeTimer.reset();
			idleTimeTimer.start();	
		}
	}
	
	public String getRemainingTimeInWords() {
		updateRemainingTime();
		return convertTimeToWords(remainingTime);
	}
	
	public static String convertTimeToWords(long timeInSeconds) {
		long hours = timeInSeconds / 3600;
		long minutes = (timeInSeconds % 3600)/60;
		long seconds = timeInSeconds % 60;
		String hSuffix = determineSuffix(hours);
		String mSuffix = determineSuffix(minutes);
		String sSuffix = determineSuffix(seconds);
		return ((hours > 0 ? hours + " hodin" + hSuffix + ", " : "") + (minutes > 0 ? minutes + " minut" + mSuffix + ", " : "") + seconds + " sekund" + sSuffix);
	}
	
	private static String determineSuffix(long number) {
	return (number == 1 ? "a" : (number > 1 && number < 5 ? "y" : ""));
	}
	
	public long getRemainingTime() {
		updateRemainingTime();
		return remainingTime;
	}
	
	private void updateRemainingTime() {
		elapsedTime = stopwatch.getTime(TimeUnit.SECONDS) - (IDLE_TIME_STARTING_VALUE * idleCount);
		remainingTime = deadline - elapsedTime;
	}
	
	public void interruptSession(String error) {
		Main.showRuntimeError(error);
		stopSession();
	}
	
	public void pauseSession() {
		setStopwatchPausedState(true);
		listener.stopListening();
	}
	
	public void resumeSession() {
		setStopwatchPausedState(false);
		listener.startListening();	
	}
	
	public void stopSession() {
		listener.stopListening();
		this.interrupt();
		if (!idleTimeTimer.isStopped()) {
			idleTimeTimer.stop();
		}
		
		if (!stopwatch.isStopped()) {
			stopwatch.stop();
		}
		
		idleTimeTimer = null;
		stopwatch = null;
		listener = null;
		running = false;
	}
	
	
	public long getTotalNumberOfClicks() {
		return listener.getTotalMouseClicks();
	}
	
	
	@Override
	public void run() {
		startStopwatch();
		startIdleTimeTimer();
		listener = new InputListener(this);
		listener.startListening();
		
		while (running) {
			main.updateRemainingTime(getRemainingTimeInWords());
			
			if (main.mode == Mode.LIMITED && remainingTime <= 0) {
				main.timerEnded();
				break;
			}
			System.out.println(idleTimeTimer.getTime(TimeUnit.SECONDS));
			if (idleTimeTimer.getTime(TimeUnit.SECONDS) >= IDLE_TIME_STARTING_VALUE) {
				setStopwatchPausedState(true);
				if (inactive == false) {
					inactive = true;
					totalIdleTime += IDLE_TIME_STARTING_VALUE;
					++idleCount;
				} else {
					totalIdleTime += (idleTimeTimer.getTime(TimeUnit.SECONDS) - IDLE_TIME_STARTING_VALUE);
				}
			} else {
				if (inactive == true) {
					setStopwatchPausedState(false);
					inactive = false;
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}