
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: OpenConnectionDialog.java 141 2009-12-16 03:53:52Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class OpenConnectionDialog extends ConnectionDialog {
  protected static final long serialVersionUID = 1L;
  private static final String classPath = "OpenConnectionDialog.";

  public static final short CANCEL = 0;
  public static final short OK = 1;
  public static final short SAVE_BOOKMARK = 2;

  protected static JCheckBox saveBookmarkCheckBox = null;
  protected static JComboBox bookmarkComboBox = null;

  private static boolean buildingBCB = false;

  protected static java.util.List bookmarks = null;

  protected OpenConnectionDialog( Frame owner, String title, boolean modal ) {
    super( owner, title, modal );
  }

  public static short showDialog( Frame owner, String title, boolean modal ) {
    saveBookmarkCheckBox = null;
    bookmarkComboBox = null;
    buildingBCB = false;

    new OpenConnectionDialog( owner, title, modal );
    return result;
  }

  public static short showDialog( Frame owner, Bookmark bookmark ) {
    setFocus();

    short result = showDialog( owner, 
                               LString.getString(classPath + "dialogTitle", 
                                                 "Open Connection"), true );

    bookmark.setUserName( getUserNameField().getText() );
    bookmark.setPassword( new String(getPasswordField().getPassword()) );
    bookmark.setHostName( getHostNameField().getText() );

    int defPort = Constants.DEF_EXPLICIT_SSL_PORT;
    if ( getSecurityModeComboBox().getSelectedIndex() == IMPLICIT_SSL ) {
      defPort = Constants.DEF_IMPLICIT_SSL_PORT;
    }
    bookmark.setPort( Util.parseInt(getPortField().getText(), defPort) );

    bookmark.setSecurityMode( (short)getSecurityModeComboBox().getSelectedIndex() );

    bookmark.setDataEncrypt( getDataEncryptionCheckBox().isEnabled() &&
                             getDataEncryptionCheckBox().isSelected() );
    bookmark.setCCCEnabled( getClearCommandChannelCheckBox().isEnabled() &&
                            getClearCommandChannelCheckBox().isSelected() );
    bookmark.setRemoteFolder( getStartingRemoteFolderField().getText() );
    bookmark.setLocalFolder( getStartingLocalFolderField().getText() );
    bookmark.setPassiveConnection( getConnectionTypeCheckBox().isSelected() );
    bookmark.setProxy( getProxyCheckBox().isEnabled() && 
                       getProxyCheckBox().isSelected() );
    bookmark.setAnonymous( getAnonymousCheckBox().isSelected() );

    if ( CANCEL != result && getSaveBookmarkCheckBox().isSelected() ) {
      result = SAVE_BOOKMARK;
    }

    return result;
  }

  private static void setFocus() {
    Runnable r = new Runnable() {
      public void run() {
        getHostNameField().requestFocus();
      }
    };

    try {
      SwingUtilities.invokeLater(r);
    }
    catch ( Exception e ) {}
  }

  protected JPanel getConnectionFields() {
    JPanel panel = super.getConnectionFields();

    String[] labelStrings = {
      LString.getString( classPath + "profile", "Profile:" ),
      LString.getString( classPath + "hostname", "Hostname:" ),
      LString.getString( classPath + "username", "Username:" ),
      LString.getString( classPath + "password", "Password:" ),
      "", // this is the anonymous checkbox
      "", // this is the save bookmark checkbox
      ""  // this is the save password checkbox
    };

    JComponent[] fields = new JComponent[ labelStrings.length ];

    int fieldNum = 0;
   
    fields[ fieldNum++ ] = getBookmarkComboBox();
    fields[ fieldNum++ ] = getHostNameField();
    fields[ fieldNum++ ] = getUserNameField();
    fields[ fieldNum++ ] = getPasswordField();
    fields[ fieldNum++ ] = getAnonymousCheckBox();
    //fields[ fieldNum++ ] = getSaveBookmarkPanel();
    fields[ fieldNum++ ] = getSaveBookmarkCheckBox();
    fields[ fieldNum++ ] = getSavePasswordCheckBox();

    buildFields( panel, labelStrings, fields );

    return panel;
  }

  protected static JPanel getSaveBookmarkPanel() {
    JPanel panel = new JPanel();
    panel.setLayout( new BorderLayout() );

    panel.add( getSaveBookmarkCheckBox(), BorderLayout.WEST );

    JPanel subPanel = new JPanel();
    subPanel.setLayout( new BorderLayout() );

    subPanel.add( Box.createHorizontalStrut(18), BorderLayout.WEST );
    subPanel.add( getSavePasswordCheckBox() , BorderLayout.CENTER);

    panel.add( subPanel, BorderLayout.SOUTH );

    return panel;
  }

  protected static JCheckBox getSaveBookmarkCheckBox() {
    if ( null == saveBookmarkCheckBox ) {
      saveBookmarkCheckBox =
        new JCheckBox( LString.getString( classPath + "saveBookmarkCheckBox",
                                          "Save to Bookmarks"), false );
      saveBookmarkCheckBox.addActionListener( new ActionListener()  {
        public void actionPerformed( ActionEvent e ) {
          boolean enabled = saveBookmarkCheckBox.isSelected() &&
                            !getAnonymousCheckBox().isSelected();
          getSavePasswordCheckBox().setEnabled( enabled );
        }
      } );

      getSavePasswordCheckBox().setEnabled( false );
    }

    return saveBookmarkCheckBox;
  }

  protected static void setBookmarkComboBox( int selectedIndex ) {
    if ( selectedIndex < 0 ) { return; }

    if ( selectedIndex >= getBookmarkComboBox().getItemCount() ) {
      selectedIndex = 0;
    }

    getBookmarkComboBox().setSelectedIndex( selectedIndex );

    BookmarkManager bm = BookmarkManager.getInstance();
    if ( bm.hasBookmarks() ) {
      Bookmark book = new Bookmark();

      if ( selectedIndex >= 2 ) {
        int offset = 2;

        if ( bm.hasGlobalBookmarks() ) {
          if ( bm.isGlobalBookmark(selectedIndex - 2) ) {
            offset = 2; 
            book = bm.getBookmark(selectedIndex - offset);
          }
          else {
            offset = 3;
            book = bm.getBookmark(selectedIndex - offset);
          }
        }
        else {
          book = bm.getBookmark(selectedIndex - offset);
        }
      }

      getHostNameField().setText( book.getHostName() );
      getAnonymousCheckBox().setSelected( book.isAnonymous() );
      toggleAnonymousConnectionOptions( book.isAnonymous() );
      getUserNameField().setText( book.getUserName() );
      getPasswordField().setText( book.getPassword() );
      getSecurityModeComboBox().setSelectedIndex( book.getSecurityMode() );
      getDataEncryptionCheckBox().setSelected( book.isDataEncrypted() );
      getClearCommandChannelCheckBox().setSelected( book.isCCCEnabled() );
      getPortField().setText( Integer.toString(book.getPort()) );
      getStartingRemoteFolderField().setText( book.getRemoteFolder() );
      getStartingLocalFolderField().setText( book.getLocalFolder() );
      getConnectionTypeCheckBox().setSelected( book.isPassiveConnection() );

      setFocus();
    }
  }

  protected static JComboBox getBookmarkComboBox() {
    if ( null == bookmarkComboBox ) {
      bookmarkComboBox = new JComboBox();
      bookmarkComboBox.setRenderer( new ComboBoxRenderer() );
      bookmarkComboBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          if ( ! buildingBCB ) {
            String tempItem = bookmarkComboBox.getSelectedItem().toString();
            if ( tempItem.equals( ComboBoxRenderer.SEPARATOR ) ) {
              setBookmarkComboBox( Client.getLastConnectionIndex() );
            }
            else {
              int selectedIndex = bookmarkComboBox.getSelectedIndex();
              setBookmarkComboBox( selectedIndex );
              Client.setLastConnectionIndex( selectedIndex );
            }
           }
	}
      } );
    }

    return bookmarkComboBox;
  }

  protected static JCheckBox getAnonymousCheckBox() {
    if ( null == anonymousCheckBox ) {
      anonymousCheckBox = 
        new JCheckBox( LString.getString( "ConnectionDialog.anonymousCheckBox",
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
    ConnectionDialog.toggleAnonymousConnectionOptions( isSelected );
    getSavePasswordCheckBox().setEnabled( 
      getSaveBookmarkCheckBox().isSelected() && !isSelected );
  }

  protected void resetFields() {
    super.resetFields();
    getSaveBookmarkCheckBox().setSelected( false );
    getSavePasswordCheckBox().setSelected( true );

    buildBookmarkComboBox();

    setBookmarkComboBox( Client.getLastConnectionIndex() );

    getBookmarkComboBox().setEnabled(getBookmarkComboBox().getItemCount() > 1);
    getProxyCheckBox().setEnabled( Client.proxySet() );
  }

  protected void buildBookmarkComboBox() {
    buildingBCB = true;
    JComboBox bcb = getBookmarkComboBox();
    bcb.removeAllItems();
    bcb.addItem( createCBItem(
      LString.getString(classPath+"defaultComboBoxText", "None")) );
    bcb.addItem( createCBItem(ComboBoxRenderer.SEPARATOR) );

    BookmarkManager bm = BookmarkManager.getInstance();
    bookmarks = bm.getBookmarks();

    if ( bm.hasGlobalBookmarks() ) {
      Iterator iter = bm.getGlobalBookmarks().iterator();
      while( iter.hasNext() ) {
        Bookmark book = (Bookmark)iter.next();
        bcb.addItem( createCBItem(book.getProfile()) );
      }
      bcb.addItem( createCBItem(ComboBoxRenderer.SEPARATOR) );
    }
    
    if ( bm.hasLocalBookmarks() ) {
      Iterator iter = bm.getLocalBookmarks().iterator(); 
      while( iter.hasNext() ) {
        Bookmark book = (Bookmark)iter.next();
        bcb.addItem( createCBItem(book.getProfile()) );
      }
    }

    buildingBCB = false;
  }

  private Object createCBItem( String s ) {
    final String str = s;
    return new Object() { public String toString() { return str; } };
  }
}

// grabbed from JGuru
class ComboBoxRenderer extends JLabel implements ListCellRenderer {
  protected static final long serialVersionUID = 1L;
  JSeparator separator;

  public final static String SEPARATOR = "%SEPARATOR%";

  public ComboBoxRenderer() {
    setOpaque(true);
    setBorder(new EmptyBorder(1, 1, 1, 1));
    separator = new JSeparator(JSeparator.HORIZONTAL);
  }

  public Component getListCellRendererComponent( JList list, 
         Object value, int index, boolean isSelected, boolean cellHasFocus) {
    String str = (value == null) ? "" : value.toString();
    if (SEPARATOR.equals(str)) {
      return separator;
    }
    if(isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    } 
    setFont(list.getFont());
    setText(str);
    return this;
  }  
}
