
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EditBookmarksDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.Keymap;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class EditBookmarksDialog extends JDialog {
  protected static final long serialVersionUID = 1L;
  private BookmarkManager bm = BookmarkManager.getInstance();

  private EditBookmarksPanel bookmarksPanel;
  private JPanel buttonPanel, bookmarkInfoPanel = null;

  private JButton addButton, editButton, deleteButton, closeButton;
  private JButton upButton, downButton;

  JLabel profileLabel = null;
  JLabel hostNameLabel = null;
  JLabel userNameLabel = null;
  JLabel secModeLabel = null;
  JLabel pasvLabel = null;
  JLabel proxyLabel = null;
  JLabel dataEncLabel = null;
  JLabel cccLabel = null;
  JLabel startLocalLabel = null;
  JLabel startRemoteLabel = null;

  private static final String classPath = "EditBookmarksDialog.";
 
  public EditBookmarksDialog() {
    super(SecureFTP.getBaseFrame(),
          LString.getString(classPath + "dialogTitle", "Manage Bookmarks"), 
          true);

    getContentPane().setLayout(new BorderLayout());

    bookmarksPanel = new EditBookmarksPanel( this );
    //bookmarksPanel.setBackground(Color.white);
    addBookmarks();

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout());
    getButtons();
    addButtonListeners();

    getContentPane().add(bookmarksPanel, BorderLayout.WEST);
    getContentPane().add(getBookmarkInfoPanel(), BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);  

    bookmarksPanel.setPreferredSize(new Dimension(150, 280));
    bookmarksPanel.setSelectedIndex( 0 );

    pack();
    setLocationRelativeTo(SecureFTP.getBaseFrame());
    setResizable(false);
    setVisible(true);
  }

  public void showBookmarkInfo( int bookmarkIndex ) {
    Bookmark book = null;
   
    if ( bookmarkIndex >= 0 ) {
      if ( bm.isGlobalBookmark(bookmarkIndex) ) {
        upButton.setEnabled( false );
        downButton.setEnabled( false );
        editButton.setEnabled( false );
        deleteButton.setEnabled( false );
      }
      else {
        if ( bookmarkIndex - bm.getGlobalBookmarks().size() == 0 ) {
          upButton.setEnabled( false );
        }
        else {
          upButton.setEnabled( true );
        }

        if ( bookmarkIndex - bm.getGlobalBookmarks().size() ==
             bm.getLocalBookmarks().size() - 1 ) {
          downButton.setEnabled( false );
        }
        else {
          downButton.setEnabled( true );
        }

        editButton.setEnabled( true );
        deleteButton.setEnabled( true );
      }

      book = bm.getBookmark( bookmarkIndex );

      String bookProfileStr = book.getProfile();

      if ( bm.isGlobalBookmark(bookmarkIndex) ) {
        LString bookProfile = new LString("EditBookmarksDialog.read_only",
                                          "[^0] (Read-Only)");
        bookProfile.replace( 0, bookProfileStr );
        bookProfileStr = bookProfile.getString();
      }

      profileLabel.setText( bookProfileStr );

      LString hostName = new LString(classPath + "hostName",
                                     "Host: [^0]");
      hostName.replace(0, book.getHostName() + ":" + book.getPort());
      hostNameLabel.setText( hostName.getString() );

      LString userName = new LString(classPath + "userName",
                                     "User: [^0]");
      userName.replace(0, book.getUserName());
      userNameLabel.setText( userName.getString() );

      LString secMode = new LString(classPath + "securityMode",
                                    "Security Mode: [^0]");
      secMode.replace(0, (String)ConnectionDialog.getSecurityModeComboBox().getItemAt(book.getSecurityMode()));
      secModeLabel.setText( secMode.getString() );

      LString pasv = new LString(classPath + "pasv", 
                                "Passive Connection: [^0]");
      if ( book.isPassiveConnection() ) {
        pasv.replace(0, LString.getString("Common.button.true", "true"));
      }
      else {
        pasv.replace(0, LString.getString("Common.button.false", "false"));
      }
      pasvLabel.setText( pasv.getString() );

      LString proxy = new LString(classPath + "proxy", 
                                "Use Proxy: [^0]");
      if ( book.usesProxy() ) {
        proxy.replace(0, LString.getString("Common.button.true", "true"));
      }
      else {
        proxy.replace(0, LString.getString("Common.button.false", "false"));
      }
      proxyLabel.setText( proxy.getString() );

      LString dataEnc = new LString(classPath + "dataEncryption", 
                                "Data Encryption: [^0]");
      if ( book.isDataEncrypted() ) {
        dataEnc.replace(0, LString.getString("Common.button.true", "true"));
      }
      else {
        dataEnc.replace(0, LString.getString("Common.button.false", "false"));
      }
      dataEncLabel.setText( dataEnc.getString() );

      LString ccc = new LString(classPath + "clearCommandChannel", 
                                "Clear Command Channel: [^0]");
      if ( book.isCCCEnabled() ) {
        ccc.replace(0, LString.getString("Common.button.true", "true"));
      }
      else {
        ccc.replace(0, LString.getString("Common.button.false", "false"));
      }
      cccLabel.setText( ccc.getString() );

      LString localDir = new LString(classPath + "localDir",
                                     "Starting Local Folder: [^0]");
      if ( book.getLocalFolder() != null && 
           book.getLocalFolder().length() > 0) {
        localDir.replace(0, book.getLocalFolder());
      }
      else {
        localDir.replace(0, LString.getString(classPath + "noDir", "none"));
      }
      startLocalLabel.setText( localDir.getString() );

      LString remoteDir = new LString(classPath + "remoteDir",
                                     "Starting Remote Folder: [^0]");
      if ( book.getRemoteFolder() != null && 
           book.getRemoteFolder().length() > 0) {
        remoteDir.replace(0, book.getRemoteFolder());
      }
      else {
        remoteDir.replace(0, LString.getString(classPath + "noDir", "none"));
      }
      startRemoteLabel.setText( remoteDir.getString() );
    }
    else if ( null != profileLabel ) {
      profileLabel.setText("");
      hostNameLabel.setText("");
      userNameLabel.setText("");
      secModeLabel.setText("");
      pasvLabel.setText("");
      proxyLabel.setText("");
      dataEncLabel.setText("");
      cccLabel.setText("");
      startLocalLabel.setText("");
      startRemoteLabel.setText("");
    }
  }

  protected JPanel getBookmarkInfoPanel() {
    if ( null == bookmarkInfoPanel ) {
      bookmarkInfoPanel = new JPanel();
      bookmarkInfoPanel.setLayout( new BoxLayout(bookmarkInfoPanel, 
                                                 BoxLayout.Y_AXIS) );
      profileLabel = new JLabel();
      Font curFont = profileLabel.getFont();
      profileLabel.setFont(new Font(curFont.getFontName(), 
                           Font.BOLD, curFont.getSize()));

      hostNameLabel = new JLabel();
      userNameLabel = new JLabel();
      secModeLabel = new JLabel();
      pasvLabel = new JLabel();
      proxyLabel = new JLabel();
      dataEncLabel = new JLabel();
      cccLabel = new JLabel();
      startLocalLabel = new JLabel();
      startRemoteLabel = new JLabel();

      bookmarkInfoPanel.add(Box.createVerticalStrut(10));
      bookmarkInfoPanel.add(profileLabel);
      bookmarkInfoPanel.add(Box.createVerticalStrut(5));
      bookmarkInfoPanel.add(hostNameLabel);
      bookmarkInfoPanel.add(userNameLabel);
      bookmarkInfoPanel.add(Box.createVerticalStrut(5));
      bookmarkInfoPanel.add(secModeLabel);
      bookmarkInfoPanel.add(dataEncLabel);
      bookmarkInfoPanel.add(cccLabel);
      bookmarkInfoPanel.add(Box.createVerticalStrut(5));
      bookmarkInfoPanel.add(startRemoteLabel);
      bookmarkInfoPanel.add(startLocalLabel);
      bookmarkInfoPanel.add(Box.createVerticalStrut(5));
      bookmarkInfoPanel.add(pasvLabel);
      bookmarkInfoPanel.add(proxyLabel);
    }

    return bookmarkInfoPanel;
  }

  protected void unSetEnterEvent(JTextField obj) {
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap map = obj.getKeymap();
    map.removeKeyStrokeBinding(enter);
  }
  

  protected void addBookmarks() {
    for ( int x = 0; x < bm.size(); x++ ) {
      Bookmark b = bm.getBookmark(x);
      bookmarksPanel.buildRow(b.getProfile());
    }
  }


  protected void getButtons() {
    if ( upButton == null )
      upButton = new JButton(LString.getString(classPath + "button.up", "Up"));

    if ( downButton == null )
      downButton = 
        new JButton( LString.getString(classPath + "button.down", "Down") );

    if ( addButton == null ) 
      addButton =
        new JButton( LString.getString(classPath + "button.add", "Add") );

    if ( editButton == null ) {
      editButton = 
        new JButton( LString.getString(classPath + "button.edit", "Edit") );
      SwingUtilities.getRootPane(this).setDefaultButton( editButton );
    }

    if ( deleteButton == null )
      deleteButton = 
        new JButton( LString.getString(classPath + "button.delete", "Delete") );

    if ( closeButton == null )
      closeButton = new JButton( LString.getString(classPath + "button.close", 
                                                   "Close") );

    buttonPanel.add(upButton);
    buttonPanel.add(downButton);
    buttonPanel.add(Box.createHorizontalStrut(20));
    buttonPanel.add(editButton);
    buttonPanel.add(addButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(Box.createHorizontalStrut(5));
    buttonPanel.add(closeButton);
  }

  
  protected void addButtonListeners() {

    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int value = bookmarksPanel.getSelectedIndex();
        bookmarksPanel.moveRow(value, value - 1); 
      }
    });

    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	int value = bookmarksPanel.getSelectedIndex();
        bookmarksPanel.moveRow(value, value + 1); 
      }
    });

    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarksPanel.addBookmark();
      }
    });

    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	bookmarksPanel.editSelectedRow();
      }
    });


    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	bookmarksPanel.removeSelectedRow();
      }
    });


    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	dispose();
      }
    });


    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
          dispose();
        }
      }
    });

  }
}

class EditBookmarksPanel extends JPanel {
  protected static final long serialVersionUID = 1L;
  private BookmarkManager bm = BookmarkManager.getInstance();
  private JTable tableView;
  private JScrollPane scrollPane;
  private EditBookmarksDialog dialog = null;
  private static final String classPath = "EditBookmarksDialog.";

  public EditBookmarksPanel( EditBookmarksDialog dialog ) {
    super();
    final EditBookmarksDialog ebd = dialog;
    this.dialog = dialog;
    setLayout(new BorderLayout());
    scrollPane = createTable();
    scrollPane.setBackground(Color.white);
    add(scrollPane, BorderLayout.CENTER);
    add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    add(Box.createHorizontalStrut(5), BorderLayout.WEST);
    add(Box.createHorizontalStrut(10), BorderLayout.EAST);

    MouseAdapter listMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) { 
        if ( e.getClickCount() == 2 ) {
	  editSelectedRow();
	}
	else if ( e.getClickCount() == 1 ) {
          ebd.showBookmarkInfo( tableView.getSelectedRow() );
	}
      }
    };

    setEnterBehavior();

    tableView.addMouseListener(listMouseListener);
  }

  private BookmarkTableModel model;

  public JScrollPane createTable() {
    JScrollPane pane = new JScrollPane();

    model = new BookmarkTableModel();

    final EditBookmarksDialog ebd = dialog;

    tableView = new JTable(model) {
      protected static final long serialVersionUID = 1L;	
      public void valueChanged( ListSelectionEvent e ) {
        super.valueChanged(e);
	if ( null != ebd ) {
          ebd.showBookmarkInfo( getSelectedRow() );
	}
      }
    };

    tableView.setRowHeight(20);
    tableView.setRowSelectionAllowed(true);
    tableView.setColumnSelectionAllowed(false);
    tableView.getTableHeader().setReorderingAllowed(false);
    tableView.setIntercellSpacing(new Dimension(0, 0));
    tableView.setShowHorizontalLines(true);
    tableView.setShowVerticalLines(false);
    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    model.addColumn( LString.getString(classPath + "column.bookmark", 
                                       "Bookmark") );

    pane.getViewport().setBackground(Color.white);
    pane.getViewport().add(tableView);
 
    return ( pane );
  }

  public void setSelectedIndex( int index ) {
    tableView.changeSelection( index, index, false, false );
  }

  public int getSelectedIndex() {
    return tableView.getSelectedRow();
  }

  public void moveRow(int row, int toIndex) {
    Client.setLastConnectionIndex( 0 );
    if ( toIndex < 0 ) return;
    else if ( toIndex >= tableView.getRowCount() ) return;

    /* move row in table */
    model.moveRow(row, row, toIndex);
    tableView.setRowSelectionInterval(toIndex, toIndex);

    int bookRow = row - bm.getGlobalBookmarks().size();
    int bookIndex = toIndex - bm.getGlobalBookmarks().size();
    Bookmark b = bm.getBookmark(row);

    /* move bookmark in bookmark vector */
    bm.getLocalBookmarks().remove(bookRow);
    bm.getLocalBookmarks().add(bookIndex, b);
    
    dialog.showBookmarkInfo(toIndex);

    /* fix menu */
    Client.getMenus().updateBookmarks();

    /* write changes to disk */
    try {
      bm.writeBookmarks();
    } 
    catch (IOException ioe) { 
      ErrorDialog.showDialog(
	new LString(classPath + "failedToWriteBookmarks", 
                    "Failed to write bookmarks.") );
    }
  }
 
  public void removeSelectedRow() {
    if ( tableView.getSelectedRowCount() <= 0 ) return;

    int tableSelectedRow = tableView.getSelectedRow();
    int selectedRow = tableSelectedRow - bm.getGlobalBookmarks().size();

    /* remove row from table */
    model.removeRow(tableSelectedRow);

    /* remove bookmark from bookmark vector */
    bm.getLocalBookmarks().remove(selectedRow);
    
    /* remove bookmark from menu */
    Client.getMenus().updateBookmarks();

    /* write changes to disk */
    try {
      bm.writeBookmarks();
      if ( bm.hasBookmarks() ) {
        clearSelections();
      }
    } 
    catch (IOException ioe) { 
      ErrorDialog.showDialog(
	new LString(classPath + "failedToWriteBookmarks", 
                    "Failed to write bookmarks.") );
    }
  }

  public void addBookmark() {
    Bookmark book = new Bookmark();

    /* throw up dialog */
    short result = 
      ModifyBookmarkDialog.showDialog(SecureFTP.getBaseFrame(), 
        LString.getString("AddBookmarkDialog.dialogTitle", "Add Bookmark"), 
        book);

    if ( ModifyBookmarkDialog.OK == result ) {
      String profile = book.getProfile();

      try {
        bm.addBookmark( book );
        bm.writeBookmarks();
        Client.getMenus().updateBookmarks();
        buildRow(profile);
        clearSelections();
      }
      catch ( IOException ioe ) {
        LString lmsg = 
          new LString( "AddBookmarkDialog.saveBookmarkError",
                       "There was a problem saving the bookmarks: [^0]" );
        lmsg.replace( 0, ioe.getMessage() );

        ErrorDialog.showDialog( lmsg );
      }
    }
  }

  public void editSelectedRow() {
    if ( tableView.getSelectedRowCount() <= 0 ) return;

    int tableIndex = tableView.getSelectedRow();

    if ( bm.isGlobalBookmark(tableIndex) ) { return; }

    int index = tableIndex - bm.getGlobalBookmarks().size();
    Bookmark book = bm.getBookmarkCopy(tableIndex);

    /* throw up dialog */

    short result = 
      ModifyBookmarkDialog.showDialog(SecureFTP.getBaseFrame(), 
        LString.getString("EditBookmarkDialog.dialogTitle", "Edit Bookmark"), 
        book);

    if ( ModifyBookmarkDialog.OK == result ) {

      String profile = book.getProfile();

      try {
        bm.getLocalBookmarks().remove(index);
        bm.getLocalBookmarks().add(index, book);
        bm.writeBookmarks();
        tableView.setValueAt(profile, tableIndex, 0);
        Client.getMenus().updateBookmarks();
        //clearSelections();
        dialog.showBookmarkInfo(tableIndex);
      }
      catch (IOException ioe) { 
        LString lmsg = 
          new LString( "AddBookmarkDialog.saveBookmarkError",
                       "There was a problem saving the bookmarks: [^0]" );
        lmsg.replace( 0, ioe.getMessage() );

        ErrorDialog.showDialog( lmsg );
      }
    }
  }


  public void buildRow(String profile) { 
    Vector row = new Vector();
    row.add(profile);
    model.addRow(row);
    setColumnWidths();
  }


  public void removeAllRows() {
    int totalRows = model.getRowCount() ;
    for ( int i = totalRows-1; i >= 0; i-- ) {
      model.removeRow(i);
    }
  }


  private void setColumnWidths() {

    TableColumn bCol = 
      tableView.getColumn( LString.getString(classPath + "column.bookmark", 
                                       "Bookmark") );

/*
    TableColumn lCol = 
       tableView.getColumn( LString.getString(classPath + "column.hostname", 
                                       "Hostname") );

    TableColumn uCol = 
       tableView.getColumn( LString.getString(classPath + "column.username", 
                                       "Username") ); 
*/

    bCol.setMinWidth(60);
    bCol.setResizable(false);
/*
    lCol.setMinWidth(60);
    uCol.setMinWidth(44);
*/

    tableView.sizeColumnsToFit(0);

  }


  protected int getRowCount() {
    return tableView.getRowCount();
  }


  protected void clearSelections() {
    tableView.removeRowSelectionInterval(0, tableView.getRowCount() - 1);
  }

  public void setEnterBehavior() {
    tableView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                          KeyStroke.getKeyStroke("ENTER"),"stop-and-next-cell");

    tableView.getActionMap().put("stop-and-next-cell", new AbstractAction() {
      protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        editSelectedRow();
      }
    } );
  }
}


class BookmarkTableModel extends DefaultTableModel {
  protected static final long serialVersionUID = 1L;
  public BookmarkTableModel() {
    super();
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }
}
