package com.gmail.matejpesl1.pc_time_monitoring;

import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class Main {
	public static enum RunState {RUNNING, PAUSED, STOPPED};
	public static RunState state = RunState.STOPPED;
    private static JFrame frame;
    private static int deadlineInSeconds;
    private static Session session;
    private static JSpinner spnHours = new JSpinner();
    private static JSpinner spnMinutes = new JSpinner();
    private static JLabel lblHours = new JLabel();
    private static JLabel lblMinutes = new JLabel();
    private static JButton btnStartPauseResume = new JButton();
    private static JButton btnCancel = new JButton();
    private static JLabel lblWordStatus = new JLabel();
    private static JLabel lblStatus = new JLabel();
    private static JPanel panel = new JPanel();
    public static enum TimerEndAction {SHUTDOWN_PC, SHOW_NOTICE};
    public static enum Mode {UNLIMITED, LIMITED};
    public static Mode mode;
    public static TimerEndAction action;
    
    /*
     * d�vod, pro� jsem m�sto PointerInfo.getLocation() pou�il knihovnu jNativeHook i p�esto, �e pot�ebuji zn�t lokaci kurzoru jen ka�dou 1 sekundu je ten, �e
     * jNativeHook dok�e zaregistrovat pohyb kurzoru i ve fullscreen h�e, narozd�l od PointerInfo.
     */
    public static void main(String[] args) {
    	prepareGUI();
    	showMenu();
    }
    
    public static void showMenu() {
    	clearPanel();
    	JButton unlimited = new JButton("do vypnut�");
    	unlimited.setText("do vypnut�");
    	panel.add(unlimited);
    	
    	JLabel lblQuestion = new JLabel("<html><center>P�ejete si po uplynut� ur�it� aktivn� doby prov�st v�mi stanovenou akci?</center></html>");
    	lblQuestion.setVerticalAlignment(SwingConstants.TOP);
    	lblQuestion.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 15));
    	lblQuestion.setBounds(55, 0, 283, 42);
    	panel.add(lblQuestion);
    	
    	JButton btnNo = new JButton("Ne");
    	btnNo.setToolTipText("monitorovat �as na PC dokud to nevypnu");
    	btnNo.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 13));
    	btnNo.setBounds(210, 57, 83, 50);
    	panel.add(btnNo);
        
    	JButton btnYes = new JButton("Ano");
    	btnYes.setToolTipText("Po ur�it�m aktivn�m �ase na PC prov�st mnou stanovenou akci");
    	btnYes.setFont(new Font("Yu Gothic UI Semibold", Font.BOLD, 13));
    	btnYes.setBounds(100, 57, 83, 50);
    	panel.add(btnYes);
    	if (!actionListenerExists(btnYes) && !actionListenerExists(btnNo)) {
        	btnYes.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			mode = Mode.LIMITED;
        			showSelectActionScreen();
        		}
        	});
        	btnNo.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			mode = Mode.UNLIMITED;
        			showUnlimitedMonitoringScreen();
        		}
        	});	
    	}
        frame.setVisible(true);
    }

    public static void showUnlimitedMonitoringScreen() {
    	clearPanel();
    	addAllUnlimitedMonitoringComponents();
         
         btnStartPauseResume.setText("Za��t");
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
         			case "Zru�it": stopButtonPressed(); break;
         			}
         		}
         	});
         }
         frame.setVisible(true);
    }
    
    private static void addAllUnlimitedMonitoringComponents() {
        panel.add(btnCancel);
        panel.add(btnStartPauseResume);
        panel.add(lblWordStatus);
        panel.add(lblStatus);
    }
    
    private static void showSelectActionScreen() {
    	panel.removeAll();
    	panel.revalidate();
    	panel.repaint();
    	
        JLabel lblHint = new JLabel("Vyberte akci, kter� se provede po uplynut� �asu");
        JButton btnShowNotice = new JButton("zobrazit ozn�men�");
        btnShowNotice.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnShowNotice.setToolTipText("Po uplynut� stanoven� doby uka� ozn�men�");
        JButton btnBack = new JButton("Zp�t");
        btnBack.setToolTipText("zp�t do menu");
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

        btnShutdown.setToolTipText("Za 5 minut od uplynut� stanoven� doby vypni PC");
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
    
    private static void prepareGUI() {
        panel.setLayout(null);
        lblStatus.setText("<html><strong>monitorov�n� neza�alo</strong></html>");
        frame = new JFrame("Monitor �asu na PC");
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setSize(400, 146);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(false);
	    frame.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent windowEvent) {
	    		if (state == RunState.STOPPED) {
	    			System.exit(0);
	    		}
	    		String[] options = {"ano", "ne"};
	            int choice = JOptionPane.showOptionDialog(null, "Opravdu chcete p�eru�it monitoring a zav��t program?", "Vypnut� programu",
	            		JOptionPane.DEFAULT_OPTION, JOptionPane.YES_NO_OPTION, null, options, options[0]);
	            if (choice == 0) {
	            	System.exit(0);
	            }
	    		}        
	    	}); 
	    }
    
    private static void addAllLimitedMonitoringComponents() {
    	panel.add(spnMinutes);
        panel.add(spnHours);
        panel.add(btnCancel);
        panel.add(btnStartPauseResume);
        panel.add(lblHours);
        panel.add(lblMinutes);
        panel.add(lblWordStatus);
        panel.add(lblStatus);
    }
    
    public static void clearPanel() {
    	panel.removeAll();
    	panel.revalidate();
    	panel.repaint();
    }
    
    public static void showLimitedMonitoringScreen() {
    	clearPanel();
        addAllLimitedMonitoringComponents();
        spnHours.setToolTipText("Ur�uje trv�n� �asova�e");
        spnHours.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnHours.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        spnHours.setBounds(10, 11, 38, 35);

        spnMinutes.setToolTipText("Ur�uje trv�n� �asova�e");
        spnMinutes.setModel(new SpinnerNumberModel(20, 1, 59, 2));
        spnMinutes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spnMinutes.setBounds(104, 11, 38, 35);
            
        lblHours.setText("hodin");
        lblHours.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 16));
        lblHours.setBounds(54, 20, 46, 14);
            
        lblMinutes.setText("minut");
        lblMinutes.setFont(new Font("Yu Gothic UI Semibold", Font.PLAIN, 16));
        lblMinutes.setBounds(145, 20, 46, 14);
            
        btnStartPauseResume.setText("Za��t");
        btnStartPauseResume.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnStartPauseResume.setBounds(192, 11, 94, 36);
                
        btnCancel.setText("Menu");
        btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnCancel.setBounds(292, 11, 94, 36);
            
        lblWordStatus.setText("stav:");
        lblWordStatus.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblWordStatus.setBounds(10, 57, 35, 26);
            
        lblStatus.setText("<html><strong>monitorov�n� neza�alo</strong></html>");
        lblStatus.setVerticalAlignment(SwingConstants.TOP);
        lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblStatus.setBounds(70, 62, 314, 50);
        
        if (!actionListenerExists(btnStartPauseResume)) {
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
        			case "Zru�it": stopButtonPressed(); break;
        			}
            	}
            });
      }
    }
    
    public static boolean actionListenerExists(JButton butt) {
    	return butt.getActionListeners().length == 0 ? false : true;
    }
    
    public static void resumeButtonPressed() {
    	changeState(RunState.RUNNING);
    }
    
    public static void pauseButtonPressed() {
    	changeState(RunState.PAUSED);
    }
    
    public static void stopButtonPressed() {
    	changeState(RunState.STOPPED);
    }
    
    public static void startButtonPressed() {
    	if (mode == Mode.LIMITED) {
   		 int minutes = (int)spnMinutes.getValue();
   		 int hours = (int)spnHours.getValue();
   		 deadlineInSeconds = (minutes*60) + (hours*60*60);
    	}
   		 changeState(RunState.RUNNING);	
   	 }
    
    public static void updateRemainingTime(String remainingTimeInWords) {
    	switch (mode) {
    	case UNLIMITED: lblStatus.setText("<html><strong>uplynulo: ~ </strong>" + 
    			Session.convertTimeToWords((int)session.getElapsedTime(TimeUnit.SECONDS)) +
    			"<br><strong>celkov� po�et kliknut�:</strong> " + session.getTotalNumberOfClicks() + "</html>"); break;
    	case LIMITED: lblStatus.setText("<html><strong>Zb�v�: ~ </strong>" + remainingTimeInWords + "<br/><strong>uplynulo: ~ </strong>" +
    			Session.convertTimeToWords((int)session.getElapsedTime(TimeUnit.SECONDS)) + 
    			"<br><strong>celkov� po�et kliknut�:</strong> " + session.getTotalNumberOfClicks() + "</html>"); break;
    	}
    }
    
    public static void timerEnded() {
    	changeState(RunState.STOPPED);
    	doTimerEndAction(action);
    }
    
    public static void doTimerEndAction(TimerEndAction action) {
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
        	JOptionPane.showMessageDialog(null, "Aktivn� doba na po��ta�i ji� dos�hla stanoven� hodnoty", "�asova� je u konce!", JOptionPane.INFORMATION_MESSAGE);	
    	} else if (action == TimerEndAction.SHUTDOWN_PC) {
    	    try {
				Runtime.getRuntime().exec("shutdown -s -t 300");
			} catch (IOException e) {
				e.printStackTrace();
				showRuntimeError("nastal probl�m p�i vyp�n�n� po��ta�e");
			}
    	}
    }
    
    public static void showRuntimeError(String error) {
    	JOptionPane.showMessageDialog(null, error, "Chyba", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void changeState(RunState toState) {
    	switch (toState) {
    		case RUNNING: {
    			if (state == RunState.PAUSED) {
        			session.resumeSession();
    			} else {
    				btnCancel.setText("Zru�it");
    				btnCancel.setVisible(true);
    				spnHours.setFocusable(false);
    				spnMinutes.setFocusable(false);
    				session = new Session(deadlineInSeconds);
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
    			lblStatus.setText("<html><strong>monitorov�n� neza�alo</strong></html>");
    			btnStartPauseResume.setText("Za��t");
    			btnCancel.setText("Menu");
   			 	spnHours.setFocusable(true);
   			 	spnMinutes.setFocusable(true);
   			 	lblMinutes.setEnabled(true);
   			 	lblHours.setEnabled(true);
    		} break;
    		case PAUSED: {
    			state = RunState.PAUSED;
       		 	btnStartPauseResume.setText("pokra�ovat");
       		 	session.pauseSession();
    		} break;
    	}
    }

    public static void showInputError(String error) {
    	lblStatus.setText(error);
    }
}
