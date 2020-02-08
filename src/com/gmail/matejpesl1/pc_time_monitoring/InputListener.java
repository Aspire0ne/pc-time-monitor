package com.gmail.matejpesl1.pc_time_monitoring;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;

public class InputListener implements NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {
	Session session;
	private long numberOfClicks;
	private enum ListenersAction{REMOVE, ADD};
	
	public InputListener(Session session) {
		turnOffDataLogging();
		this.session = session;
	}
	
	private void turnOffDataLogging() {
		LogManager.getLogManager().reset();
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
	}
	
	public void startListening() {
		manipulateNativeHook(ListenersAction.ADD);
		manipulateAllListeners(ListenersAction.ADD);
	}
	
	private void manipulateAllListeners(ListenersAction action) {
		switch (action) {
			case ADD: {
				GlobalScreen.addNativeMouseListener(this);
				GlobalScreen.addNativeMouseMotionListener(this);
				GlobalScreen.addNativeKeyListener(this);
				GlobalScreen.addNativeMouseWheelListener(this);
			} break;
			case REMOVE: {
				GlobalScreen.removeNativeMouseListener(this);
				GlobalScreen.removeNativeMouseMotionListener(this);
				GlobalScreen.removeNativeKeyListener(this);
				GlobalScreen.removeNativeMouseWheelListener(this);
			} break;
		}
	}
	
	private void manipulateNativeHook(ListenersAction action) {
		boolean registered = GlobalScreen.isNativeHookRegistered();
		try {
			switch (action) {
			case ADD: {
				if (!registered) {
					GlobalScreen.registerNativeHook();
				}
			} break;
			case REMOVE: {
				if (registered) {
					GlobalScreen.unregisterNativeHook();
				}
			} break;
			}
		}
		catch (NativeHookException e) {
			e.printStackTrace();
			session.cancelSession("Nastala chyba pøi zaznamenávání aktivity na poèítaèi.");
			if (registered) {
				try {
					GlobalScreen.unregisterNativeHook();
				} catch (NativeHookException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void stopListening() {
		manipulateNativeHook(ListenersAction.REMOVE);
		manipulateAllListeners(ListenersAction.REMOVE);
	}
	
	public long getTotalMouseClicks() {
		return numberOfClicks;
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent arg0) {
		++numberOfClicks;
		session.resetIdleTimer();
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent arg0) {
		session.resetIdleTimer();
	}

	@Override
	public void nativeMouseWheelMoved(NativeMouseWheelEvent arg0) {
		session.resetIdleTimer();
	}
}
