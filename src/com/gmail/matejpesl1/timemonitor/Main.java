package com.gmail.matejpesl1.timemonitor;

import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class Main {
	public static enum RunState {RUNNING, PAUSED, STOPPED};
	public static RunState state = RunState.STOPPED;
    private JFrame frame;
    private static int deadlineInSeconds;
    private Session session;
    private final JSpinner spnHours = new JSpinner();
    private final JSpinner spnMinutes = new JSpinner();
    private final JLabel lblHours = new JLabel();
    private final JLabel lblMinutes = new JLabel();
    private final JButton btnStartPauseResume = new JButton();
    private final JButton btnCancel = new JButton();
    private final JLabel lblWordStatus = new JLabel();
    private final JLabel lblStatus = new JLabel();
    private final JPanel panel = new JPanel();
    public enum TimerEndAction {SHUTDOWN_PC, SHOW_NOTICE};
    public enum Mode {UNLIMITED, LIMITED};
    public Mode mode;
    public TimerEndAction action;
    public boolean ghost;
    
    /*
     * The reason why I used jNtiveHook library instead of PointerInfo.getLocation() even tho I don't need to know the pointer
     * location more than once every second is because jNativeHook is able to register mouse movements while in full-screen game. 
     */

    public static void main(String[] args) {
    	Main main = new Main();
    	main.prepareGUI();
    	main.showMenu();
    }
    
    private void showMenu() {
    	clearPanel();
    	JButton unlimited = new JButton("do vypnutí");
    	unlimited.setText("do vypnutí");
    	panel.add(unlimited);
    	
    	JLabel lblQuestion = new JLabel("<html><center>Pøejete si po uplynutí urèité aktivní doby provést vámi stanovenou akci?</center></html>");
    	lblQuestion.setVerticalAlignment(SwingConstants.TOP);
    	lblQuestion.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 15));
    	lblQuestion.setBounds(55, 0, 283, 42);
    	panel.add(lblQuestion);
    	
    	JButton btnNo = new JButton("Ne");
    	btnNo.setToolTipText("monitorovat èas na PC dokud to nevypnu");
    	btnNo.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 13));
    	btnNo.setBounds(210, 57, 83, 50);
    	panel.add(btnNo);
        
    	JButton btnYes = new JButton("Ano");
    	btnYes.setToolTipText("Po urèitém aktivním èase na PC provést mnou stanovenou akci");
    	btnYes.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 13));
    	btnYes.setBounds(100, 57, 83, 50);
    	panel.add(btnYes);
    	
    	if (!actionListenerExists(btnYes) && !actionListenerExists(btnNo)) {
        	btnYes.addActionListener( e -> {
        			mode = Mode.LIMITED;
        			showSelectActionScreen();
        	});
        	
        	btnNo.addActionListener(e -> {
        			mode = Mode.UNLIMITED;
        			showUnlimitedMonitoringScreen();
        	});
    	}
    	
        frame.setVisible(true);
    }

    private void showUnlimitedMonitoringScreen() {
    	clearPanel();
    	addAllUnlimitedMonitoringComponents();
         
         btnStartPauseResume.setText("Zaèít");
         btnStartPauseResume.setFont(new Font("Tahoma", Font.PLAIN, 12));
         btnStartPauseResume.setBounds(100, 11, 94, 36);
             
         btnCancel.setText("Menu");
         btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
         btnCancel.setBounds(200, 11, 94, 36);
         
         lblWordStatus.setText("stav:");
         lblWordStatus.setFont(new Font("Tahoma", Font.BOLD, 15));
         lblWordStatus.setBounds(10, 57, 42, 26);
         lblStatus.setVerticalAlignment(SwingConstants.TOP);
         lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
         lblStatus.setBounds(70, 62, 314, 48);
         
         JButton btnGhost = new JButton("");
     	btnGhost.setToolTipText("Schová se do system tray");
     	btnGhost.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 8));
     	btnGhost.setBounds(panel.getWidth() - 25, panel.getHeight() - 30, 25, 30);
     	panel.add(btnGhost);
         	
           	btnGhost.addActionListener(e -> {
     			turnOnGhost();
         });
     
         if (!actionListenerExists(btnStartPauseResume) && !actionListenerExists(btnCancel)) {
             btnStartPauseResume.addActionListener(new ActionListener() {
             	public void actionPerformed(ActionEvent e) {
                 		switch (state) {
                 		case RUNNING: pauseButtonPressed(); break;
                 		case PAUSED: resumeButtonPressed(); break;
                 		case STOPPED: startButtonPressed(); break;
                 		}
             	}
             });
             
         	btnCancel.addActionListener(new ActionListener() {
         		public void actionPerformed(ActionEvent e) {
         			switch (btnCancel.getText()) {
         			case "Menu": showMenu(); break;
         			case "Zrušit": stopButtonPressed(); break;
         			}
         		}
         	});
         }
         if (!ghost) {
        	 frame.setVisible(true);	 
         } else {
        	 frame.setVisible(false);
        	 toSystemTray();
         }
         
    }
    
    public void toSystemTray() {
		System.out.println("system tray function started");
		Image image = Toolkit.getDefaultToolkit().getImage("");

	    final PopupMenu popup = new PopupMenu();
	    final TrayIcon trayIcon = new TrayIcon(image, "PC Timer", popup);
	    final SystemTray tray = SystemTray.getSystemTray();

	    MenuItem exitItem = new MenuItem("Exit");
	    MenuItem openItem = new MenuItem("otevøít program");
	    exitItem.addActionListener(e -> {
	            onClose();
	    });
	    
	    openItem.addActionListener(e -> {
	    	tray.remove(trayIcon);
            turnOffGhost();
    });
	    
	    popup.add(exitItem);
	    popup.add(openItem);
	    trayIcon.setPopupMenu(popup);
	
	    try {
	        tray.add(trayIcon);
	    } catch (AWTException e) {
	        System.out.println("TrayIcon could not be added.");
	    }
    }
    
    private void addAllUnlimitedMonitoringComponents() {
        panel.add(btnCancel);
        panel.add(btnStartPauseResume);
        panel.add(lblWordStatus);
        panel.add(lblStatus);
    }
    
    private void showSelectActionScreen() {
    	panel.removeAll();
    	panel.revalidate();
    	panel.repaint();
    	
        JLabel lblHint = new JLabel("Vyberte akci, která se provede po uplynutí èasu");
        JButton btnShowNotice = new JButton("zobrazit oznámení");
        btnShowNotice.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnShowNotice.setToolTipText("Po uplynutí stanovené doby ukaž oznámení");
        JButton btnBack = new JButton("Zpìt");
        btnBack.setToolTipText("zpìt do menu");
        btnBack.setFont(new Font("Tahoma", Font.PLAIN, 11));
        JButton btnShutdown = new JButton("Vypnout PC");
        btnShutdown.setFont(new Font("Tahoma", Font.PLAIN, 11));
        
        lblHint.setVerticalAlignment(SwingConstants.TOP);
        lblHint.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 15));
        lblHint.setBounds(32, 0, 325, 21);
        
        btnShowNotice.setBounds(72, 30, 120, 46);
        btnShowNotice.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		action = TimerEndAction.SHOW_NOTICE;
        		showLimitedMonitoringScreen();
        	}
        });

        btnShutdown.setToolTipText("Za 5 minut od uplynutí stanovené doby vypni PC");
        btnShutdown.setBounds(200, 30, 120, 46);

        btnBack.setBounds(152, 81, 90, 28);
        if (!actionListenerExists(btnBack) && !actionListenerExists(btnShutdown)) {
            btnBack.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		showMenu();
            	}
            });
            
            btnShutdown.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		action = TimerEndAction.SHUTDOWN_PC;
            		showLimitedMonitoringScreen();
            	}
            });	
        }
        
        panel.add(btnShutdown);
        panel.add(lblHint);
        panel.add(btnShowNotice);
        panel.add(btnBack);
    }
    
    private void prepareGUI() {
        panel.setLayout(null);
        lblStatus.setText("<html><strong>monitorování nezaèalo</strong></html>");
        frame = new JFrame("Monitor èasu na PC");
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setSize(400, 146);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(false);
	    frame.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent windowEvent) {
	    		onClose();
	    		}        
	    	});
	    }
    
    private static void onClose() {
		if (state == RunState.STOPPED) {
			System.exit(0);
		}
		String[] options = {"ano", "ne"};
        int choice = JOptionPane.showOptionDialog(null, "Opravdu chcete pøerušit monitoring a zavøít program?", "Vypnutí programu",
        		JOptionPane.DEFAULT_OPTION, JOptionPane.YES_NO_OPTION, null, options, options[0]);
        if (choice == 0) {
        	System.exit(0);
        }
    }
    
    private void addAllLimitedMonitoringComponents() {
    	panel.add(spnMinutes);
        panel.add(spnHours);
        panel.add(btnCancel);
        panel.add(btnStartPauseResume);
        panel.add(lblHours);
        panel.add(lblMinutes);
        panel.add(lblWordStatus);
        panel.add(lblStatus);
    }
    
    private void clearPanel() {
    	panel.removeAll();
    	panel.revalidate();
    	panel.repaint();
    }
    
    private void showLimitedMonitoringScreen() {
    	clearPanel();
        addAllLimitedMonitoringComponents();
        spnHours.setToolTipText("Urèuje trvání èasovaèe");
        spnHours.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnHours.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        spnHours.setBounds(10, 11, 38, 35);

        spnMinutes.setToolTipText("Urèuje trvání èasovaèe");
        spnMinutes.setModel(new SpinnerNumberModel(20, 1, 59, 2));
        spnMinutes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnMinutes.setBounds(104, 11, 38, 35);
            
        lblHours.setText("hodin");
        lblHours.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 16));
        lblHours.setBounds(54, 20, 46, 14);
            
        lblMinutes.setText("minut");
        lblMinutes.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 16));
        lblMinutes.setBounds(145, 20, 46, 14);
            
        btnStartPauseResume.setText("Zaèít");
        btnStartPauseResume.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnStartPauseResume.setBounds(192, 11, 94, 36);
                
        btnCancel.setText("Menu");
        btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnCancel.setBounds(292, 11, 94, 36);
            
        lblWordStatus.setText("stav:");
        lblWordStatus.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblWordStatus.setBounds(10, 57, 35, 26);
            
        lblStatus.setText("<html><strong>monitorování nezaèalo</strong></html>");
        lblStatus.setVerticalAlignment(SwingConstants.TOP);
        lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblStatus.setBounds(70, 62, 314, 50);
        
    	JButton btnGhost = new JButton("");
    	btnGhost.setToolTipText("Schová se do system tray");
    	btnGhost.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 8));
    	btnGhost.setBounds(panel.getWidth() - 25, panel.getHeight() - 30, 25, 30);
    	panel.add(btnGhost);
        
        if (!actionListenerExists(btnStartPauseResume)) {
        	btnStartPauseResume.addActionListener(e -> {
        			switch (state) {
        			case RUNNING: pauseButtonPressed(); break;
        			case PAUSED: resumeButtonPressed(); break;
        			case STOPPED: startButtonPressed(); break;
        			}
        	});
        	
        	btnCancel.addActionListener(e -> {
        			switch (btnCancel.getText()) {
        			case "Menu": showMenu(); break;
        			case "Zrušit": stopButtonPressed(); break;
        			}
            });
        	
          	btnGhost.addActionListener(e -> {
    			turnOnGhost();
        });
      }
    }
    
    public void turnOnGhost() {
    	ghost = true;
    	frame.setVisible(false);
    	toSystemTray();
    }
    
    public void turnOffGhost() {
    	ghost = false;
    	frame.setVisible(true);
    }
    
    private boolean actionListenerExists(JButton butt) {
    	return butt.getActionListeners().length == 0 ? false : true;
    }
    
    private void resumeButtonPressed() {
    	changeState(RunState.RUNNING);
    }
    
    private void pauseButtonPressed() {
    	changeState(RunState.PAUSED);
    }
    
    private void stopButtonPressed() {
    	changeState(RunState.STOPPED);
    }
    
    private void startButtonPressed() {
    	if (mode == Mode.LIMITED) {
   		 int minutes = (int)spnMinutes.getValue();
   		 int hours = (int)spnHours.getValue();
   		 deadlineInSeconds = (minutes*60) + (hours*60*60);
    	}
   		 changeState(RunState.RUNNING);	
   	 }
    
    public void updateRemainingTime(String remainingTimeInWords) {
    	StringBuilder textToSet = new StringBuilder("<html>");
    	if (mode == Mode.LIMITED) {
    		textToSet.append("<strong>Zbývá: ~ </strong>" + remainingTimeInWords + "<br>");	
    	}
    	textToSet.append("<strong>uplynulo: ~ </strong>")
    			.append(Session.convertTimeToWords((int)session.getElapsedTime(TimeUnit.SECONDS)))
    			.append("<br><strong>celkový poèet kliknutí: </strong>")
    			.append(session.getTotalNumberOfClicks())
    			.append("</html>");
    	lblStatus.setText(textToSet.toString());
    }
    
    public void timerEnded() {
    	changeState(RunState.STOPPED);
    	doTimerEndAction(action);
    }
    
    private void doTimerEndAction(TimerEndAction action) {
    	if (action == TimerEndAction.SHOW_NOTICE) {
        	boolean supported = frame.isAlwaysOnTopSupported();
        	if (supported) {
        	  frame.setAlwaysOnTop(true);
        	}
        	frame.toFront();
        	frame.requestFocus();
        	if (supported) {
        		frame.setAlwaysOnTop(false);
            }
        	JOptionPane.showMessageDialog(null, "Aktivní doba na poèítaèi již dosáhla stanovené hodnoty", "Èasovaè je u konce!", JOptionPane.INFORMATION_MESSAGE);	
    	} else if (action == TimerEndAction.SHUTDOWN_PC) {
    	    try {
				Runtime.getRuntime().exec("shutdown -s -t 300");
			} catch (IOException e) {
				e.printStackTrace();
				showRuntimeError("nastal problém pøi vypínání poèítaèe");
			}
    	}
    }
    
    public static void showRuntimeError(String error) {
    	JOptionPane.showMessageDialog(null, error, "Chyba", JOptionPane.ERROR_MESSAGE);
    }
    
    private void changeState(RunState toState) {
    	switch (toState) {
    		case RUNNING: {
    			if (state == RunState.PAUSED) {
        			session.resumeSession();
    			} else {
    				btnCancel.setText("Zrušit");
    				btnCancel.setVisible(true);
    				spnHours.setFocusable(false);
    				spnMinutes.setFocusable(false);
    				session = new Session(deadlineInSeconds, this);
    				session.start();
    				spnHours.setFocusable(false);
    				spnMinutes.setFocusable(false);
    			}
    			state = RunState.RUNNING;
    			btnStartPauseResume.setText("pozastavit");
    			
    		} break;
    	   case	STOPPED: {
    			session.stopSession();
    			session = null;
    			state = RunState.STOPPED;
    			lblStatus.setText("<html><strong>monitorování nezaèalo</strong></html>");
    			btnStartPauseResume.setText("Zaèít");
    			btnCancel.setText("Menu");
   			 	spnHours.setFocusable(true);
   			 	spnMinutes.setFocusable(true);
   			 	lblMinutes.setEnabled(true);
   			 	lblHours.setEnabled(true);
    		} break;
    		case PAUSED: {
    			state = RunState.PAUSED;
       		 	btnStartPauseResume.setText("pokraèovat");
       		 	session.pauseSession();
    		} break;
    	}
    }
}
