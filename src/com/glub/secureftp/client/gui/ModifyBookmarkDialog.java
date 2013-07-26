
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ModifyBookmarkDialog.java 141 2009-12-16 03:53:52Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ModifyBookmarkDialog extends ConnectionDialog {
  protected static final long serialVersionUID = 1L;
  public static final short CANCEL = 0;
  public static final short OK = 1;

  protected static JTextField profileField = null;
  protected static Bookmark bookmark = null;

  protected ModifyBookmarkDialog( Frame owner, String title, boolean modal ) {
    super( owner, title, modal );
  }

  public static short showDialog( Frame owner, String title, Bookmark book ) {
    bookmark = book;
    profileField = null;

    Runnable r = new Runnable() {
      public void run() {
        getProfileField().requestFocus();
      }
    }; 

    try {
      SwingUtilities.invokeLater(r);
    }
    catch ( Exception e ) {}

    new ModifyBookmarkDialog( owner, title, true );

    bookmark.setProfile( getProfileField().getText() );
    bookmark.setUserName( getUserNameField().getText() );
    bookmark.setPassword( new String(getPasswordField().getPassword()) );
    bookmark.setHostName( getHostNameField().getText() );

    int defPort = Constants.DEF_EXPLICIT_SSL_PORT;
    if ( getSecurityModeComboBox().getSelectedIndex() == IMPLICIT_SSL ) {
      defPort = Constants.DEF_IMPLICIT_SSL_PORT;
    }
    bookmark.setPort( Util.parseInt(getPortField().getText(), defPort) );

    bookmark.setSecurityMode( (short)getSecurityModeComboBox().getSelectedIndex() );

    bookmark.setDataEncrypt( getDataEncryptionCheckBox().isSelected() );
    bookmark.setCCCEnabled( getClearCommandChannelCheckBox().isSelected() );
    bookmark.setRemoteFolder( getStartingRemoteFolderField().getText() );
    bookmark.setLocalFolder( getStartingLocalFolderField().getText() );
    bookmark.setPassiveConnection( getConnectionTypeCheckBox().isSelected() );
    bookmark.setProxy( getProxyCheckBox().isSelected() );
    bookmark.setAnonymous( getAnonymousCheckBox().isSelected() );

    return result;
  }

  protected JPanel getConnectionFields() {
    JPanel panel = super.getConnectionFields();

    String[] labelStrings = {
      LString.getString( "OpenConnectionDialog.profile", "Profile:" ),
      LString.getString( "OpenConnectionDialog.hostname", "Hostname:" ),
      LString.getString( "OpenConnectionDialog.username", "Username:" ),
      LString.getString( "OpenConnectionDialog.password", "Password:" ),
      "", // this is the anonymous checkbox
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;
   
    fields[ fieldNum++ ] = getProfileField();
    fields[ fieldNum++ ] = getHostNameField();
    fields[ fieldNum++ ] = getUserNameField();
    fields[ fieldNum++ ] = getPasswordField();
    fields[ fieldNum++ ] = getAnonymousCheckBox();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  protected static JTextField getProfileField() {
    if ( null == profileField ) {
      profileField = new JTextField();
    }

    return profileField;
  }

  protected JButton getMainButton() {
    JButton button = super.getMainButton();
    button.setText( LString.getString("Common.button.save", "Save") );
    return button;
  }

  protected void resetFields() {
    getProfileField().setText( bookmark.getProfile() );
    getHostNameField().setText( bookmark.getHostName() );
    getAnonymousCheckBox().setSelected( bookmark.isAnonymous() );
    toggleAnonymousConnectionOptions( bookmark.isAnonymous() );
    getSecurityModeComboBox().setSelectedIndex( bookmark.getSecurityMode() );
    getClearCommandChannelCheckBox().setSelected( bookmark.isCCCEnabled() );
    getStartingRemoteFolderField().setText( bookmark.getRemoteFolder() );
    getStartingLocalFolderField().setText( bookmark.getLocalFolder() );
    setPort( bookmark.getPort() );
    getConnectionTypeCheckBox().setSelected( bookmark.isPassiveConnection() );
    getDataEncryptionCheckBox().setSelected( bookmark.isDataEncrypted() );
    getProxyCheckBox().setSelected( bookmark.usesProxy() );
    getProxyCheckBox().setEnabled( true );
    getUserNameField().setText( bookmark.getUserName() );
    getPasswordField().setText( bookmark.getPassword() );
  }

  protected boolean checkFields() {
    boolean result = super.checkFields();

    if ( getProfileField().getText().trim().length() == 0 ) {
      result = false;
    }

    return result;
  }

  protected void setDefaultTextField() {
    addWindowListener( new WindowAdapter() {
      public void windowOpened( WindowEvent e ) {
        getProfileField().requestFocus();
      }
    } );
  }
}
