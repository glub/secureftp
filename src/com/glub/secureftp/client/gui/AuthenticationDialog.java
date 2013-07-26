
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AuthenticationDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AuthenticationDialog extends JDialog {
  protected static final long serialVersionUID = 1L;
  private static final String classPath = "AuthenticationDialog.";
  private static FTPSession session = null;
  private static boolean result = false;

  private JTextField userNameField = null;
  private JPasswordField passwordField = null;

  protected AuthenticationDialog( Frame owner, String title, boolean modal,
                                  FTPSession ftpSession ) {
    super( owner, title, modal );
    session = ftpSession;
    
    getContentPane().setLayout( new BorderLayout() );

    getContentPane().add( getAuthenticationPanel(), BorderLayout.CENTER );
    getContentPane().add( getButtonPanel(), BorderLayout.SOUTH );

    addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        result = false;
      }
    } );

    pack();
    setDefaultTextField();
    setLocationRelativeTo( owner );
    setResizable( false );
    setVisible( true );
  }

  public static boolean showDialog( Frame owner, FTPSession session ) {
    AuthenticationDialog ad = 
      new AuthenticationDialog( owner,
                                LString.getString(classPath + "dialogTitle", 
                                                  "User Authentication"), 
                                true, session );

    if ( result ) {
      session.setUserName( ad.getUserNameField().getText() );
      session.setPassword( new String(ad.getPasswordField().getPassword()) );
    }

    return result;
  }

  protected JPanel getAuthenticationPanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
	}
    };

    String[] labelStrings = {
      LString.getString( "OpenConnectionDialog.username", "Username:" ),
      LString.getString( "OpenConnectionDialog.password", "Password:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;
   
    fields[ fieldNum++ ] = getUserNameField();
    fields[ fieldNum++ ] = getPasswordField();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  public JTextField getUserNameField() {
    if ( null == userNameField ) {
      userNameField = new JTextField( 25 );
      userNameField.setText( session.getUserName() );
    }

    return userNameField;
  }

  public JPasswordField getPasswordField() {
    if ( null == passwordField ) {
      passwordField = new JPasswordField();
      passwordField.setText( session.getPassword() );
    }

    return passwordField;
  }

  protected JPanel getButtonPanel() {
    JPanel buttonPanel = new JPanel( new FlowLayout() );

    JButton button = new JButton( LString.getString("Common.button.ok", "OK") );
	
    button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        result = true;
        dispose();
      }
    } );

    buttonPanel.add(button);
    SwingUtilities.getRootPane( this ).setDefaultButton( button );

    button = 
      new JButton( LString.getString("Common.button.cancel", "Cancel") );

    button.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        result = false; 
        dispose();
      }
    } );

    buttonPanel.add(button);

    return buttonPanel;
  }

  protected void setDefaultTextField() {
    addWindowListener( new WindowAdapter() {
      public void windowOpened( WindowEvent e ) {
	if ( session.getUserName().length() == 0 ) {
          getUserNameField().requestFocus();
	}
	else {
          getPasswordField().requestFocus();
	}
      }
    } );
  }

  protected void setupEscapeForCancel( Component component ) {
    component.addKeyListener( new KeyAdapter() {
       public void keyPressed(KeyEvent e) {
         if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
           result = false;
           dispose();
         }
       }
     } );
  }

  protected void buildFields( JPanel panel, String[] labelStrings, 
                                        JComponent[] fields ) {
    JLabel[] labels = new JLabel[ labelStrings.length ];

    for ( int i = 0; i < labelStrings.length; i++ ) {
      setupEscapeForCancel( fields[i] );
      labels[ i ] = new JLabel( labelStrings[i], JLabel.TRAILING );
      labels[ i ].setLabelFor( fields[i] );

      fields[ i ].addFocusListener( new FocusListener() {
        public void focusGained( FocusEvent e ) {
          Component c = e.getComponent();

          if ( c instanceof JTextField ) {
	    ((JTextField)c).selectAll();
	  }
	}
	
	public void focusLost( FocusEvent e ) {}
      } );

      panel.add( labels[i] );
      panel.add( fields[i] );
    }

    SpringUtilities.makeCompactGrid( panel, 
                                     labelStrings.length, 2, // rows, cols
                                     10, 20,                 // init x, init y
				     20, 10                  // pad x, pad y
                                   );
  }
}
