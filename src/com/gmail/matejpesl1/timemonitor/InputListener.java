package com.gmail.matejpesl1.timemonitor;

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
	private Session session;
	private int numberOfClicks;
	private enum ListenersAction{REMOVE, ADD};
	
	public InputListener(Session session) {
		turnOffDataLogging();
		this.session = session;
	}
	
	private void turnOffDataLogging() {
		LogManager.getLogManager().reset();
		Logger.getLogger(InputListener.class.getPackage().getName()).setLevel(Level.OFF);
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
			if (registered) {
				try {
					GlobalScreen.unregisterNativeHook();
				} catch (NativeHookException e1) {
					e1.printStackTrace();
				}
			} else {
				session.interruptSession("Nastala chyba pøi zapínání záznamu. zkuste to, prosím, znovu.");
			}
		}
	}
	
	public void stopListening() {
		manipulateAllListeners(ListenersAction.REMOVE);
		manipulateNativeHook(ListenersAction.REMOVE);
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
		if (arg0.getButton() == 1) {
			++numberOfClicks;	
		}
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
