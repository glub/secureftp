//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ConnectionDialog.java 137 2009-12-15 09:15:56Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class ConnectionDialog extends JDialog {
  protected static final long serialVersionUID = 1L;
  private static final String classPath = "ConnectionDialog.";

  public static final short CANCEL = 0;
  public static final short OK = 1;

  public static final int NO_SECURITY = (int)FTPSession.NO_SECURITY;
  public static final int EXPLICIT_SSL = (int)FTPSession.EXPLICIT_SSL;
  public static final int IMPLICIT_SSL = (int)FTPSession.IMPLICIT_SSL;

  protected static short result = CANCEL;

  protected JPanel connectPanel = null;
  protected JPanel optionsPanel = null;
  protected JPanel buttonPanel = null;

  protected JButton mainButton = null;

  protected static JTextField hostNameField = null;
  protected static JTextField userNameField = null;
  protected static JPasswordField passwordField = null;
  protected static JCheckBox savePasswordCheckBox = null;
  protected static JCheckBox anonymousCheckBox = null;

  protected static JComboBox securityModeComboBox = null;
  protected static JCheckBox dataEncryptionCheckBox = null;
  protected static JCheckBox cccCheckBox = null;
  protected static JTextField portField = null;
  protected static JTextField startingRemoteFolderField = null;
  protected static JTextField startingLocalFolderField = null;
  protected JButton browseButton = null;
  protected static JCheckBox connectionTypeCheckBox = null;
  protected static JCheckBox proxyCheckBox = null;

  protected static String realUserName = System.getProperty( "user.name" );
  protected static String realPassword = "";

  protected ConnectionDialog( Frame owner, String title, boolean modal ) {
    super( owner, title, modal );

    hostNameField = null;
    userNameField = null;
    passwordField = null;
    savePasswordCheckBox = null;
    anonymousCheckBox = null;

    securityModeComboBox = null;
    dataEncryptionCheckBox = null;
    cccCheckBox = null;
    portField = null;
    startingRemoteFolderField = null;
    startingLocalFolderField = null;
    connectionTypeCheckBox = null;
    proxyCheckBox = null;

    buildDialog();
    pack();
    resetFields();
    setLocationRelativeTo( owner );
    setDefaultTextField();
    setResizable( false );
    setVisible( true );
  }

  protected void buildDialog() {
    getContentPane().setLayout( new BorderLayout() );

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setRequestFocusEnabled( true );

    tabPane.add( getConnectPanel(), 
                 LString.getString(classPath + "connectPanelTab", 
                 "Connection") );

    tabPane.add( getSecurityPanel(), 
                 LString.getString(classPath + "optionsPanelTab", 
                 "Options") );

    getContentPane().add( tabPane, BorderLayout.CENTER );

    getContentPane().add( getButtonPanel(), BorderLayout.SOUTH );

    //resetFields();
  }

  protected boolean checkFields() {
    boolean result = true;

    if ( getHostNameField().getText().trim().length() == 0 ) {
      result = false;
    }

    String port = getPortField().getText().trim();
    if ( port.length() == 0 ) {
      result = false;
    }
    else {
      try {
        Integer.parseInt( port );
      } 
      catch ( NumberFormatException nfe ) {
        result = false;
      }
    }

    return result;
  }

  protected void resetFields() {
    getHostNameField().setText( "" );
    getUserNameField().setText( System.getProperty("user.name") );
    getPasswordField().setText( "" );
    getSavePasswordCheckBox().setSelected( true );
    getAnonymousCheckBox().setSelected( false );
    toggleAnonymousConnectionOptions( false );
    getSecurityModeComboBox().setSelectedIndex( EXPLICIT_SSL );
    getStartingRemoteFolderField().setText( "" );
    getStartingLocalFolderField().setText( "" );
    setPort();
    getConnectionTypeCheckBox().setSelected( true );
    getProxyCheckBox().setSelected( Client.proxySet() );
    getProxyCheckBox().setEnabled( true );
  }

  protected void setDefaultTextField() {
    addWindowListener( new WindowAdapter() {
      public void windowOpened( WindowEvent e ) {
        getHostNameField().requestFocus();
      }
    } );
  }

  protected JPanel getButtonPanel() {
    if ( null == buttonPanel ) {
      buttonPanel = new JPanel( new FlowLayout() );

      buttonPanel.add( getMainButton() );
      SwingUtilities.getRootPane( this ).setDefaultButton( getMainButton() );

      JButton button = 
        new JButton( LString.getString("Common.button.cancel", "Cancel") );

      button.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          result = CANCEL; 
	  dispose();
	}
      } );

      buttonPanel.add(button);
    }

    return buttonPanel;
  }

  protected JButton getMainButton() {
    if ( null == mainButton ) {
      mainButton = 
        new JButton( LString.getString("Common.button.connect", "Connect") );
	
      mainButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          if ( checkFields() ) {
            result = OK;
            dispose();
          }
          else {
            getToolkit().beep();
          }
        }
      } );
    }

    return mainButton;
  }

  protected JPanel getConnectPanel() {
    if ( null == connectPanel ) {
      connectPanel = new JPanel();
      connectPanel.setLayout( new BoxLayout(connectPanel, BoxLayout.Y_AXIS) );
      connectPanel.add( getConnectionFields() );
    }

    return connectPanel;
  }

  protected JPanel getConnectionFields() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;	
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
	}
    };

    return panel;
  }

  protected static JTextField getHostNameField() {
    if ( null == hostNameField ) {
      hostNameField = new JTextField(25);
    }

    return hostNameField;
  }

  protected static JTextField getUserNameField() {
    if ( null == userNameField ) {
      userNameField = new JTextField();
    }

    return userNameField;
  }

  protected static JPasswordField getPasswordField() {
    if ( null == passwordField ) {
      passwordField = new JPasswordField();
    }

    return passwordField;
  }

  protected static JCheckBox getSavePasswordCheckBox() {
    if ( null == savePasswordCheckBox ) {
      savePasswordCheckBox = 
        new JCheckBox( LString.getString( classPath + "savePasswordCheckBox",
                                          "Save Password"), true );
    }

    return savePasswordCheckBox;
  }

  protected static JCheckBox getAnonymousCheckBox() {
    if ( null == anonymousCheckBox ) {
      anonymousCheckBox = 
        new JCheckBox( LString.getString( classPath + "anonymousCheckBox",
                                          "Anonymous"), false );

      anonymousCheckBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          toggleAnonymousConnectionOptions( anonymousCheckBox.isSelected() );
        }
      } );
    }

    return anonymousCheckBox;
  }

  protected static void toggleAnonymousConnectionOptions( boolean isSelected ) {
    getUserNameField().setEnabled( !isSelected );
    getPasswordField().setEnabled( !isSelected );

    if ( isSelected ) {
      realUserName = getUserNameField().getText();
      getUserNameField().setText( "anonymous" );

      realPassword = new String( getPasswordField().getPassword() );
      getPasswordField().setText( "secureftp@" );
    }
    else {
      getUserNameField().setText( realUserName );
      getPasswordField().setText( realPassword );
    }
  }

  protected JPanel getSecurityPanel() {
    if ( null == optionsPanel ) {
      optionsPanel = new JPanel();
      //optionsPanel.setLayout( new BoxLayout(optionsPanel, BoxLayout.Y_AXIS) );
      optionsPanel.add( getOptionsFields() );
    }

    return optionsPanel;
  }

  protected JPanel getOptionsFields() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
	}
    };

    String[] labelStrings = {
      LString.getString( classPath + "securityMode", "Security Mode:" ),
      LString.getString( classPath + "dataEncryption", "Data Encryption:" ),
      LString.getString( classPath + "ccc", "Clear Command Channel:" ),
      LString.getString( classPath + "port", "Port:" ),
      LString.getString( classPath + "startingRemoteFolder",
                         "Starting Remote Folder:" ),
      LString.getString( classPath + "startingLocalFolder", 
                         "Starting Local Folder:" ),
      LString.getString( classPath + "connectionType", "Connection Type:" ),
      LString.getString( classPath + "proxy", "Proxy:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;
   
    fields[ fieldNum++ ] = getSecurityModeComboBox();
    fields[ fieldNum++ ] = getDataEncryptionCheckBox();
    fields[ fieldNum++ ] = getClearCommandChannelCheckBox();
    fields[ fieldNum++ ] = getPortPanel();
    fields[ fieldNum++ ] = getStartingRemoteFolderField();
    fields[ fieldNum++ ] = getStartingLocalFolderPanel();
    fields[ fieldNum++ ] = getConnectionTypeCheckBox();
    fields[ fieldNum++ ] = getProxyCheckBox();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  protected static JComboBox getSecurityModeComboBox() {
    if ( null == securityModeComboBox ) {
      securityModeComboBox = new JComboBox();
      securityModeComboBox.addItem( LString.getString(classPath + 
                                                    "securityMode.none", 
                                                    "None") );
      securityModeComboBox.addItem( LString.getString(classPath +
                                                    "securityMode.explicitSSL", 
                                                    "Explicit SSL") );
      securityModeComboBox.addItem( LString.getString(classPath +
                                                    "securityMode.implicitSSL", 
                                                    "Implicit SSL") );

      securityModeComboBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          int selIndex = securityModeComboBox.getSelectedIndex();
          getDataEncryptionCheckBox().setEnabled( selIndex > 0 );
          getClearCommandChannelCheckBox().setEnabled(selIndex == EXPLICIT_SSL);
	  setPort();
	}
      } );
    }

    return securityModeComboBox;
  }

  protected static JCheckBox getDataEncryptionCheckBox() {
    if ( null == dataEncryptionCheckBox ) {
      dataEncryptionCheckBox = 
        new JCheckBox( LString.getString( classPath + "dataEncryptionCheckBox",
                                          "Enabled"), true );
    }

    return dataEncryptionCheckBox;
  }

  protected static JCheckBox getClearCommandChannelCheckBox() {
    if ( null == cccCheckBox ) {
      cccCheckBox = 
        new JCheckBox( LString.getString( classPath + "cccCheckBox",
                                          "Enabled (after authentication)"), 
                       false );
      String ttip = 
        LString.getString( classPath + "cccToolTip",
                           "Enable this during an explict SSL secure " +
                           "connection to support a data connection " +
                           "behind a (NAT) firewall." );
      cccCheckBox.setToolTipText(ttip);
      cccCheckBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          getConnectionTypeCheckBox().setSelected( !cccCheckBox.isSelected() );
        }
      } );
    }

    return cccCheckBox;
  }

  protected JPanel getPortPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getPortField(), BorderLayout.WEST );
    return panel;
  }

  protected static JTextField getPortField() {
    if ( null == portField ) {
      portField = new JTextField( 5 );
    }

    return portField;
  }

  protected static void setPort() {
    setPort( -1 );
  }

  protected static void setPort( int portNumber ) {
    int secMode = getSecurityModeComboBox().getSelectedIndex();

    if ( portNumber < 0 ) {
      switch ( secMode ) {
        case IMPLICIT_SSL:
          int implicitPort = Constants.DEF_IMPLICIT_SSL_PORT;
          getPortField().setText( Integer.toString(implicitPort) );
	  break;

        default:
	  int defPort = Constants.DEF_EXPLICIT_SSL_PORT;
          getPortField().setText( Integer.toString(defPort) );
          break;
      }
    }
    else {
      getPortField().setText( Integer.toString(portNumber) );
    }
  }

  protected static JTextField getStartingRemoteFolderField() {
    if ( null == startingRemoteFolderField ) {
      startingRemoteFolderField = new JTextField( 20 );
    }

    return startingRemoteFolderField;
  }

  protected JPanel getStartingLocalFolderPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getStartingLocalFolderField(), BorderLayout.CENTER );

    JPanel buttonPanel = new JPanel( new BorderLayout() );
    buttonPanel.add( Box.createHorizontalStrut(5), BorderLayout.WEST );
    buttonPanel.add( getBrowseButton(), BorderLayout.EAST );

    panel.add( buttonPanel, BorderLayout.EAST );
    return panel;
  }

  protected static JTextField getStartingLocalFolderField() {
    if ( null == startingLocalFolderField ) {
      startingLocalFolderField = new JTextField();
    }

    return startingLocalFolderField;
  }

  protected JButton getBrowseButton() {
    if ( null == browseButton ) {
      ImageIcon icon = 
        new ImageIcon( getClass().getResource("images/browse.png") );
      browseButton = new JButton( icon );
      browseButton.setToolTipText( LString.getString(classPath +
                                                     "tooltip.browse",
                                                     "Browse") );
      int wPad = 20;
      int hPad = 0;

      if ( Util.isSunOS() ) {
        wPad = 30;
        hPad = 23;
      }

      browseButton.setPreferredSize( 
        new Dimension(icon.getIconWidth() + wPad, icon.getIconHeight() + hPad));
      browseButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          File localDir = Client.getLocalView().getCurrentDirectory();
          JFileChooser fc = new JFileChooser( localDir );
	  fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
	  int result = 
            fc.showDialog( getStartingLocalFolderField(), 
                           LString.getString("Common.button.select_folder",
                                             "Select Folder") );
	  if ( JFileChooser.APPROVE_OPTION == result ) {
            String folder = fc.getSelectedFile().getAbsolutePath();
            getStartingLocalFolderField().setText( folder );
            getStartingLocalFolderField().setCaretPosition( 0 );
	  }
	}
      } );
    }

    return browseButton;
  }

  protected static JCheckBox getConnectionTypeCheckBox() {
    if ( null == connectionTypeCheckBox ) {
      connectionTypeCheckBox = 
        new JCheckBox( LString.getString( classPath + "connectionTypeCheckBox",
                                          "Passive"), true );
    }

    return connectionTypeCheckBox;
  }

  protected static JCheckBox getProxyCheckBox() {
    if ( null == proxyCheckBox ) {
      proxyCheckBox = 
        new JCheckBox( LString.getString( classPath + "proxyCheckBox",
                                          "Enabled"), true );
    }

    return proxyCheckBox;
  }

  protected void setupEscapeForCancel( Component component ) {
    component.addKeyListener( new KeyAdapter() {
       public void keyPressed(KeyEvent e) {
         if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
           result = CANCEL;
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
