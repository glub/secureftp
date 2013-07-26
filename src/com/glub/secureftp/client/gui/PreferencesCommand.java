
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PreferencesCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

public class PreferencesCommand extends LocalCommand {
  private JTextField globalURLField = null;
  private JTextField proxyHostField = null;
  private JTextField proxyPortField = null;
  private JTextField proxyUserField = null;
  private JPasswordField proxyPassField = null;
  private JCheckBox hiddenFileCheckBox = null;
  private JCheckBox showFullColumnCheckBox = null;
  private JCheckBox modeZCheckBox = null;
  private JCheckBox startOpenCheckBox = null;
  private JCheckBox checkUpdateCheckBox = null;
  private JCheckBox closeTabWarningCheckBox = null;
  private JCheckBox forcePasvToUseControlCheckBox = null;

  public PreferencesCommand() {
    super("prefs", CommandID.PREFS_COMMAND_ID, "prefs");
  }

  public SecureFTPError doIt() throws CommandException {
    LString title = new LString( "Prefs.title", "Preferences" );
    String [] options = { LString.getString("Common.button.ok", "OK") };
    JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                  getPrefsPanel(),
                                  title.getString(),
                                  JOptionPane.OK_OPTION,
                                  JOptionPane.PLAIN_MESSAGE,
                                  null, options, options[0] );

    Client.setGlobalBookmarksURL( getGlobalURLField().getText() );
    Client.setSocksHostName( getProxyHostField().getText() );
    Client.setSocksPort( Util.parseInt(getProxyPortField().getText(), 1080) );
    Client.setSocksUserName( getProxyUserField().getText() );
    Client.setSocksPassword(new String(getProxyPassField().getPassword()));
    Client.setShowHiddenFiles( getHiddenFileCheckBox().isSelected() );
    Client.setShowFullColumnListing( getFullColumnCheckBox().isSelected() );
    Client.setUseModeZCompression( getModeZCheckBox().isSelected() );
    Client.setStartWithOpenDialog( getStartOpenCheckBox().isSelected() );
    Client.setCloseTabWarning( getCloseTabWarningCheckBox().isSelected() );
    Client.setForcePasvControlIP( 
                              getForcePasvToUseControlCheckBox().isSelected() );
    Client.setUseProxy(getProxyHostField().getText().trim().length() > 0 &&
                            getProxyPortField().getText().trim().length() > 0); 
    if (Client.getClientType() == Client.APPLICATION)
      Client.setAutoCheckForUpdate( getCheckUpdateCheckBox().isSelected() );

    PreferencesDispatcher.doWritePrefs();

    return new SecureFTPError();
  }

  private JPanel getPrefsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );
    panel.add( getURLPanel() );
    panel.add( getProxyPanel() );
    panel.add( getCheckBoxPanel() );
    return panel;
  }

  private JPanel getURLPanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
    };

    LString title = new LString( "Prefs.global_book_url", 
                                 "Global Bookmarks URL:" );

    panel.setBorder( new TitledBorder(title.getString()) );

    panel.add( getGlobalURLField() );
    
    Runnable r = new Runnable() {
      public void run() {
        getGlobalURLField().requestFocus();
        getGlobalURLField().select(0, getGlobalURLField().getText().length());
        getGlobalURLField().setCaretPosition( 0 );
      }
    };

    SwingUtilities.invokeLater( r );
     
    SpringUtilities.makeCompactGrid( panel,
                                     1, 1,    // rows, cols
                                     10, 10,  // init x, init y
                                     10, 10   // pad x, pad y
                                   );
    return panel;
  }

  private JPanel getProxyPanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
    	public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
          return new Dimension( Integer.MAX_VALUE, pref.height );
        }
    };

    panel.setBorder( 
      new TitledBorder(LString.getString("Prefs.proxy.title",
                                         "SOCKS Proxy:")) );

    String[] labelStrings = {
      LString.getString( "Prefs.proxy_host", "Hostname:" ),
      LString.getString( "Prefs.proxy_port", "Port:" ),
      LString.getString( "Prefs.proxy_user", "Username:" ),
      LString.getString( "Prefs.proxy_pass", "Password:" ),
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];
    
    int fieldNum = 0;
    
    fields[ fieldNum++ ] = getProxyHostPanel();
    fields[ fieldNum++ ] = getProxyPortPanel();
    fields[ fieldNum++ ] = getProxyUserPanel();
    fields[ fieldNum++ ] = getProxyPassPanel();
    
    buildFields( panel, labelStrings, fields );

    return panel;
  }

  private JPanel getCheckBoxPanel() {
    JPanel panel = new JPanel( new SpringLayout() ) {
    	protected static final long serialVersionUID = 1L;
        public Dimension getMaximumSize() {
          Dimension pref = getPreferredSize();
	  return new Dimension( Integer.MAX_VALUE, pref.height );
        }
    };

    int components = 0;

    panel.add( getHiddenFileCheckBox() );
    components++;

    panel.add( getFullColumnCheckBox() );
    components++;

    panel.add( getModeZCheckBox() );
    components++;

    panel.add( getStartOpenCheckBox() );
    components++;

    panel.add( getCloseTabWarningCheckBox() );
    components++;

    panel.add( getForcePasvToUseControlCheckBox() );
    components++;


    if (Client.getClientType() == Client.APPLICATION) {
      panel.add( getCheckUpdateCheckBox() );
      components++;
    }

    SpringUtilities.makeCompactGrid( panel,
                                     components, 1,    // rows, cols
                                     10, 10,  // init x, init y
                                     10, 10   // pad x, pad y
                                   );
    return panel;
  }

  protected JTextField getGlobalURLField() {
    if ( null == globalURLField ) {
      globalURLField = new JTextField(15);
      URL url = Client.getGlobalBookmarksURL();
      if ( null != url ) {
        globalURLField.setText(url.toExternalForm());
      }
    }

    return globalURLField;
  }

  protected JPanel getProxyHostPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getProxyHostField(), BorderLayout.WEST );
    return panel;
  }

  protected JTextField getProxyHostField() {
    if ( null == proxyHostField ) {
      proxyHostField = new JTextField(15);
      proxyHostField.setText(Client.getSocksHostName());
    }

    return proxyHostField;
  }

  protected JPanel getProxyPortPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getProxyPortField(), BorderLayout.WEST );
    panel.add( Box.createHorizontalStrut(250), BorderLayout.EAST );
    return panel;
  }

  protected JTextField getProxyPortField() {
    if ( null == proxyPortField ) {
      proxyPortField = new JTextField(5);
      proxyPortField.setText((new Integer(Client.getSocksPort())).toString());
    }

    return proxyPortField;
  }

  protected JPanel getProxyUserPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getProxyUserField(), BorderLayout.WEST );
    return panel;
  }

  protected JTextField getProxyUserField() {
    if ( null == proxyUserField ) {
      proxyUserField = new JTextField(15);
      proxyUserField.setText(Client.getSocksUserName());
    }

    return proxyUserField;
  }

  protected JPanel getProxyPassPanel() {
    JPanel panel = new JPanel( new BorderLayout() );
    panel.add( getProxyPassField(), BorderLayout.WEST );
    return panel;
  }

  protected JPasswordField getProxyPassField() {
    if ( null == proxyPassField ) {
      proxyPassField = new JPasswordField(15);
      proxyPassField.setText(Client.getSocksPassword());
    }

    return proxyPassField;
  }

  protected JCheckBox getHiddenFileCheckBox() {
    if ( null == hiddenFileCheckBox ) {
      LString item = new LString("Prefs.show_hidden_files", 
                                 "Show Hidden Files");
      hiddenFileCheckBox = new JCheckBox( item.getString() );
      hiddenFileCheckBox.setSelected(Client.showHiddenFiles());
    }
    
    return hiddenFileCheckBox;
  }

  protected JCheckBox getFullColumnCheckBox() {
    if ( null == showFullColumnCheckBox ) {
      LString item = new LString("Prefs.show_full_column_listing", 
                                 "Show expanded remote directory information");
      showFullColumnCheckBox = new JCheckBox( item.getString() );
      showFullColumnCheckBox.setSelected(Client.showFullColumnListing());
    }
    
    return showFullColumnCheckBox;
  }

  protected JCheckBox getModeZCheckBox() {
    if ( null == modeZCheckBox ) {
      LString item = new LString("Prefs.use_mode_z_compression", 
                                 "Use Mode Z compression");
      modeZCheckBox = new JCheckBox( item.getString() );
      modeZCheckBox.setSelected(Client.useModeZCompression());
    }
    
    return modeZCheckBox;
  }

  protected JCheckBox getStartOpenCheckBox() {
    if ( null == startOpenCheckBox ) {
      LString item = new LString("Prefs.start_with_open", 
                                 "Show Open Connection Dialog on Startup");
      startOpenCheckBox = new JCheckBox( item.getString() );
      startOpenCheckBox.setSelected(Client.startWithOpenDialog());
    }
    
    return startOpenCheckBox;
  }

  protected JCheckBox getCheckUpdateCheckBox() {
    if ( null == checkUpdateCheckBox ) {
      LString item = new LString("Prefs.check_for_updates", 
                                 "Check for Updates on Startup");
      checkUpdateCheckBox = new JCheckBox( item.getString() );
      checkUpdateCheckBox.setSelected(Client.autoCheckForUpdate());
      checkUpdateCheckBox.setEnabled(!GTOverride.getBoolean("glub.disableUpdates"));
    }
    
    return checkUpdateCheckBox;
  }

  protected JCheckBox getCloseTabWarningCheckBox() {
    if ( null == closeTabWarningCheckBox ) {
      LString item = new LString("Prefs.close_tab_warning", 
                                 "Show warning when disconnecting via tab");
      closeTabWarningCheckBox = new JCheckBox( item.getString() );
      closeTabWarningCheckBox.setSelected(Client.showCloseTabWarning());
    }
    
    return closeTabWarningCheckBox;
  }

  protected JCheckBox getForcePasvToUseControlCheckBox() {
    if ( null == forcePasvToUseControlCheckBox ) {
      LString item = 
        new LString("Prefs.pasv_control_ip", 
                    "Force passive data connection to use control channel IP");
      forcePasvToUseControlCheckBox = new JCheckBox( item.getString() );
      forcePasvToUseControlCheckBox.setSelected(Client.forcePasvControlIP());
    }
    
    return forcePasvToUseControlCheckBox;
  }

  protected void buildFields( JPanel panel, String[] labelStrings,
                              JComponent[] fields ) {
    JLabel[] labels = new JLabel[ labelStrings.length ];

    for ( int i = 0; i < labelStrings.length; i++ ) {
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
                                     10, 10,                 // init x, init y
                                     10, 10                  // pad x, pad y
                                   );
  }
}

