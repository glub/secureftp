//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteView.java 154 2009-12-31 22:18:20Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*
public class RemoteView extends JTabbedPane implements ChangeListener,
                                                       PreferenceHandler {
*/
public class RemoteView extends CloseableTabbedPane 
                        implements ChangeListener, PreferenceHandler {
  protected static final long serialVersionUID = 1L;
  private Preferences prefs = PreferencesDispatcher.getPreferences( this );

  private static final String SORTED_INDEX = "ServerColumnSortedIndex";
  private static final String SORTED_ASCENDING = "ServerColumnSortedAscending";
  private static final String SERVER_PANEL_DIVIDER_LOCATION = 
                                                  "RemotePanelDividerLocation";

  private int sortedIndex = -1;
  private boolean sortedAscending = true;

  private ArrayList serverList = new ArrayList();

  private static int dividerLocation = -1;

  public RemoteView() {
    //super( JTabbedPane.TOP );
    super();
    addChangeListener( this );

    Client.fixComponentColor( this );

    final int keyMask = ( Util.isMacOS() ) ? Event.META_MASK : Event.CTRL_MASK;

    this.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent ke ) {}
      public void keyPressed( KeyEvent ke ) {
        if ( KeyStroke.getKeyStrokeForEvent(ke) == KeyStroke.getKeyStroke(KeyEvent.VK_L, keyMask) ) {
          getCurrentConnection().selectComboBoxText();
        }
      }
      public void keyTyped( KeyEvent ke ) {}
    } );
  }

  public RemotePanel getCurrentConnection() {
    return (RemotePanel)serverList.get( getSelectedIndex() );
  }

  protected void closeTab() {
    LString msg =
      new LString( "ConnectionDialog.closeTab.warning", 
                   "You are about to disconnect from the server." );
    JPanel info = new JPanel();
    info.setLayout( new BoxLayout(info, BoxLayout.Y_AXIS) );

    String checkMsg =
      LString.getString("Prefs.dont_show_again",
                        "Don't show me again");

    boolean state = !Client.showCloseTabWarning();
    JCheckBox check = new JCheckBox(checkMsg, state);

    info.add( new GTLabel(msg, 400) );
    info.add( Box.createVerticalStrut(40) );
    info.add( check );

    LString title = 
      new LString("Common.dialogTitle.warning", "Secure FTP Warning");

    int dl = JOptionPane.OK_OPTION;

    if ( !state ) {
      dl = JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                           info,
                                           title.getString(),
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.QUESTION_MESSAGE,
                                           null, null, null );
    }

    Client.setCloseTabWarning( !check.isSelected() );

    if ( JOptionPane.OK_OPTION == dl ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
    }
  }

  public void addConnection( FTPSession session, JTextArea loggingTextArea,
		  					 boolean buildComboBox ) {
    String tabName = session.getHostName();

    for( int tabID = 1; indexOfTab(tabName) >= 0; tabID++ ) {
      tabName = session.getHostName() + " " + tabID;
    }

    try {
      session.setLocalDir( Client.getLocalView().getCurrentDirectory() );
    }
    catch ( FileNotFoundException fnfe ) {
      // not likely
    }

    RemotePanel sp = new RemotePanel( loggingTextArea, session );
    session.setRemoteUI( sp );

    String tooltip = session.getUserName() + "@" + session.getHostName() + 
                     ":" + session.getPort();
    addTab( tabName, sp.getTablePanel(), tooltip );
    serverList.add( sp );

    if (buildComboBox)
    	sp.buildDirectoryComboBox();

    setSelectedIndex( getTabCount() - 1 );
    Client.getMenus().updateWindowMenu();
  }

  public void removeConnection() {
    serverList.remove( getSelectedIndex() );
    remove( getSelectedIndex() );
    Client.getMenus().updateWindowMenu();
    resetTabBackground();
  }

  public void stateChanged( ChangeEvent e ) {
    super.stateChanged(e);
    FTPSessionManager.getInstance().setCurrentSession(getSelectedIndex());
    final FTPSession currentSession = 
      FTPSessionManager.getInstance().getCurrentSession();

    if ( null != Client.getLocalView() && null != currentSession ) {
      Client.getLocalView().changeDirectory( currentSession.getLocalDir() );
      Runnable r = new Runnable() {
        public void run() {
          ((RemotePanel)currentSession.getRemoteUI()).setFocus();
        }
      };

      try {
        SwingUtilities.invokeLater(r);
      }
      catch ( Exception ex ) {}
    }

    SecureFTP.getCommandDispatcher().fireCommand( this, new RefreshUICommand() );
  }

  public void setSort( int sortedIndex, boolean sortedAscending ) {
    this.sortedIndex = sortedIndex;
    this.sortedAscending = sortedAscending;
  }

  public int getSortedColumn() {
    return sortedIndex;
  }

  public boolean isSortedAscending() {
    return sortedAscending;
  }

  public static int getDividerLocation() { return dividerLocation; }
  public static void setDividerLocation( int dl ) { dividerLocation = dl; }

  public void readPreferences() {
    sortedIndex = prefs.getInt( SORTED_INDEX, 0 );
    sortedAscending = prefs.getBoolean( SORTED_ASCENDING, true );
    
    if ( Client.getClientType() == Client.APPLET ) {
      setDividerLocation( Client.applet.getIntParameter("remote_divider_location", -1) );
    }
    else {
      setDividerLocation( prefs.getInt(SERVER_PANEL_DIVIDER_LOCATION, -1) );
    }
  }

  public void writePreferences() {
    prefs.putInt( SORTED_INDEX, sortedIndex );
    prefs.putBoolean( SORTED_ASCENDING, sortedAscending );
    prefs.putInt( SERVER_PANEL_DIVIDER_LOCATION, getDividerLocation() );
  }
}

class RemotePanel extends JPanel {
  protected static final long serialVersionUID = 1L;
  private final static String classPath = "RemotePanel.";

  private FTPSession session = null;

  private JScrollPane scrollPane = null;

  private int eventLevel = 0;

  private RemoteFileTable tableView = null;

  private JPanel tablePanel = null;
  private JPanel controlPanel = null;
  private JPanel directoryPanel = null;
  private JScrollPane loggingPanel = null;

  private JTextArea loggingTextArea = null;

  private JButton certInfoButton = null;
  private JButton upDirButton = null;
  private JComboBox directoryComboBox = null;
  private DirectoryComboBoxEditor directoryComboBoxEditor = null;
  private JLabel itemsLabel = null;

  private String currentWorkingDir = null;

  private RemoteFileList remoteFileList = null;

  public RemotePanel() {
    this( null, null );
  }

  public RemotePanel( JTextArea loggingTextArea, FTPSession session ) {
    super();
    this.loggingTextArea = loggingTextArea;
    this.session = session;

    Client.fixComponentColor( this );
  }

  protected JLabel getItemsLabel() {
    if ( null == itemsLabel ) {
      itemsLabel = new JLabel("");

      String fontName = itemsLabel.getFont().getFontName();

      if ( !Util.supportsFont("Verdana", SecureFTP.locale) )
        fontName = "Default";

      Font infoFont = new Font( fontName,
                                itemsLabel.getFont().getStyle(),
                                10 );

      itemsLabel.setFont( infoFont );

      itemsLabel.setHorizontalAlignment( SwingConstants.RIGHT );
      setItemsLabel( -1 );
    }

    return itemsLabel;
  }

  public void setItemsLabel( int numOfFiles ) {
    LString label = null;

    if ( numOfFiles == 1 )
      label = new LString("RemoteView.numOfItem", "[^0] item");
    else
      label = new LString("RemoteView.numOfItems", "[^0] items");

    if ( numOfFiles == -1 )
      label.replace( 0, "-" );
    else
      label.replace( 0, numOfFiles + "" );

    getItemsLabel().setText( label.getString() + " " ); 
  }

  public JPanel getTablePanel() {
    if ( null == tablePanel ) {
      tablePanel = new JPanel();

      Client.fixComponentColor( tablePanel );

      tablePanel.setLayout( new BorderLayout() );
      tablePanel.add( getDirectoryPanel(), BorderLayout.NORTH );

      JPanel directoryListPanel = new JPanel( new BorderLayout() );

      Client.fixComponentColor( directoryListPanel );

      scrollPane = new JScrollPane();

      scrollPane.addMouseListener( new MouseAdapter() {
        public void mouseClicked( MouseEvent e ) {
          getTableView().changeSelection( 0, 0, false, false );
          getTableView().clearSelection();
          Client.getToolBar().updateToolBar();
          Client.getMenus().updateMenuBar();
        }
      } );

      FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

      scrollPane.setViewport( new RemoteFileTableViewport(getTableView()) );

      Color tableColor = Color.white;

      if ( session.isSecure() ) {
        tableColor = RemoteFileTable.SECURE_COLOR;
        getTableView().setTableColor( RemoteFileTable.SECURE );
      }
      else if ( !session.getUserName().equals("anonymous") ) {
        tableColor = RemoteFileTable.INSECURE_COLOR;
        getTableView().setTableColor( RemoteFileTable.INSECURE );
      }

      scrollPane.getViewport().setBackground( tableColor );

      scrollPane.getViewport().add( getTableView() );

      directoryListPanel.add( scrollPane, BorderLayout.CENTER );
      JPanel itemPanel = new JPanel(new BorderLayout());
      Client.fixComponentColor( itemPanel );
      itemPanel.add( getItemsLabel(), BorderLayout.EAST );
      //itemPanel.setBorder( BorderFactory.createLineBorder(Color.GRAY, 1) );
      directoryListPanel.setBorder( BorderFactory.createEtchedBorder() );
      directoryListPanel.add( itemPanel, BorderLayout.SOUTH ); 

      final JSplitPane splitPane = 
        new JSplitPane( JSplitPane.VERTICAL_SPLIT, directoryListPanel, 
                        getLoggingPanel() );
      splitPane.setOneTouchExpandable( false );

      if ( -1 != RemoteView.getDividerLocation() ) {
        splitPane.setDividerLocation( RemoteView.getDividerLocation() );
      }

      splitPane.setResizeWeight( 0.8 );

      Client.fixComponentColor( splitPane );

      splitPane.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent e ) {
          if (e.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
           RemoteView.setDividerLocation( splitPane.getDividerLocation() );
          }
        }
      } );

      tablePanel.add( splitPane, BorderLayout.CENTER );

      try {
        currentWorkingDir = session.getFTPBean().pwd();
      }
      catch ( FTPException fe ) {}

      //tablePanel.add( scrollPane, BorderLayout.CENTER );
      //tablePanel.add( getLoggingPanel(), BorderLayout.SOUTH );
    }

    return tablePanel;
  }

  public JScrollPane getScrollPane() { return scrollPane; }

  public String getCurrentDirectory() {
    if ( null == currentWorkingDir ) {
      currentWorkingDir = "";
    }

    return currentWorkingDir;
  }

  public void setFocus() {
    Runnable r = new Runnable() {
      public void run() {
        getTableView().requestFocus();
      }
    };

    try {
      SwingUtilities.invokeLater(r);
    }
    catch ( Exception e1 ) {}
  }

  protected JButton getCertInfoButton() {
    if ( null == certInfoButton ) {
      ImageIcon icon =
        new ImageIcon(getClass().getResource("images/secure_session_button.png"));
      certInfoButton = new JButton( icon );

      ImageIcon pressedIcon =
        new ImageIcon(getClass().getResource("images/secure_session_button_sel.png"));
      certInfoButton.setPressedIcon( pressedIcon );

      certInfoButton.setFocusable( false );
      certInfoButton.setBorder( BorderFactory.createEmptyBorder(2, 2, 2, 2) );
      certInfoButton.setBorderPainted( false );
      certInfoButton.setContentAreaFilled( false );

/*
      int wPad = 15;
      int hPad = 10;

      if ( Util.isMacOS() ) {
        wPad = 20;
        hPad = 15;
      }
      else if ( Util.isSunOS() ) {
        wPad = 30;
        hPad = 23;
      }

      certInfoButton.setPreferredSize( 
        new Dimension(secureIcon.getIconWidth() + wPad, 
                      secureIcon.getIconHeight() + hPad) );
*/
      certInfoButton.setToolTipText( LString.getString(classPath + 
                                                  "tooltip.certInfo",
                                                  "Certificate Information") );
      certInfoButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          SSLCertificateCommand certCommand = new SSLCertificateCommand();
          ArrayList args = new ArrayList(2);
          args.add( session.getCurrentCertificate() );
          args.add( new Short(SSLCertificateCommand.SHOW_CERTIFICATE) );
          certCommand.setArgs( args );

          SecureFTP.getCommandDispatcher().fireCommand( this, certCommand );
          ((RemotePanel)session.getRemoteUI()).setFocus();
        }
      } );
    }

    return certInfoButton;
  }

  protected JButton getUpDirButton() {
    if ( null == upDirButton ) {
      ImageIcon icon =
        new ImageIcon( getClass().getResource("images/prev_dir_button.png") );
      upDirButton = new JButton( icon );

      ImageIcon pressedIcon =
        new ImageIcon( getClass().getResource("images/prev_dir_button_sel.png") 
);      
      upDirButton.setPressedIcon( pressedIcon );

      upDirButton.setFocusable( false );
      upDirButton.setBorder( BorderFactory.createEmptyBorder(2, 2, 2, 2) );
      upDirButton.setBorderPainted( false );
      upDirButton.setContentAreaFilled( false );
      upDirButton.setToolTipText( 
        LString.getString("FileListPanel.tooltip.upDir", "Go to previous directory") );

/*
      int wPad = 15;
      int hPad = 10;

      if ( Util.isMacOS() ) {
        wPad = 20;
        hPad = 15;
      }
      else if ( Util.isSunOS() ) {
        wPad = 30;
        hPad = 23;
      }

      upDirButton.setPreferredSize( 
        new Dimension(icon.getIconWidth() + wPad, icon.getIconHeight() + hPad) );
*/
      upDirButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          JComboBox cb = getDirectoryComboBox();
          cb.setSelectedIndex( cb.getItemCount() - 2 );
        }
      } );
    }

    return upDirButton;
  }

  protected JPanel getDirectoryPanel() {
    if ( null == directoryPanel ) {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      directoryPanel = new JPanel();

      Client.fixComponentColor( directoryPanel );

      directoryPanel.setLayout(gridbag);
      int xpos = 0;

      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.WEST;
      c.insets = new Insets(0,0,0,0);
      c.weightx = 0.0;
      c.weighty = 0.0;

      if ( session.isSecure() ) {
        c.gridx = xpos++;
        c.gridy = 0;
        gridbag.setConstraints(getCertInfoButton(), c);
        directoryPanel.add(getCertInfoButton());
      }

      c.gridx = xpos++;
      c.insets = new Insets(0,0,0,3);

      gridbag.setConstraints(getUpDirButton(), c);
      directoryPanel.add(getUpDirButton());

      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridx = xpos++;
      c.gridy = 0;
      c.insets = new Insets(0,0,0,3);

      gridbag.setConstraints(getDirectoryComboBox(), c);
      directoryPanel.add( getDirectoryComboBox() );
    }

    return directoryPanel;
  }

  protected void selectComboBoxText() {
    directoryComboBoxEditor.selectAll();
  }

  protected JComboBox getDirectoryComboBox() {
    if ( null == directoryComboBox ) {
      directoryComboBox = new JComboBox();
      directoryComboBox.setRenderer( new RemoteDirectoryComboBoxRenderer() );
      directoryComboBoxEditor = new DirectoryComboBoxEditor();
      directoryComboBox.setEditor( directoryComboBoxEditor );
      directoryComboBox.setBackground( Color.white );
      directoryComboBox.setEditable( true );

      directoryComboBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          if ( eventLevel < 1 && 
            e.getActionCommand().equals("comboBoxChanged") ) {
            final String newDir = (String)directoryComboBox.getSelectedItem();
            Thread cdt = new Thread() {
              public void run() {
                changeDirectory( new RemoteFile(newDir), false );
              };
            };
            cdt.start();
          }
        }
      } );
    }

    return directoryComboBox;
  }

  protected JScrollPane getLoggingPanel() {
    if ( null == loggingPanel ) {
      loggingPanel = new JScrollPane( getLoggingTextArea() );
    }

    return loggingPanel;
  }

  public void setLoggingTextArea( JTextArea loggingTextArea ) {
    this.loggingTextArea = loggingTextArea;
  }

  public JTextArea getLoggingTextArea() {
    if ( null == loggingTextArea ) {
      loggingTextArea = new JTextArea( 4, 20 );
      loggingTextArea.setEditable( false );
      loggingTextArea.setAutoscrolls( true );
    }

    return loggingTextArea;
  }

  public SecureFTPError changeDirectory( RemoteFile newDir, 
                                         boolean suppressErrors ) {
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    CDCommand cmd = new CDCommand();
    ArrayList args = new ArrayList( 1 );
    args.add( newDir );
    cmd.setArgs( args );
    cmd.suppressErrors( suppressErrors );

    SecureFTPError result = 
      SecureFTP.getCommandDispatcher().fireCommand( this, cmd );

    if ( SecureFTPError.OK == result.getCode() ) {
      try {
        removeAllRows();
        currentWorkingDir = session.getFTPBean().pwd();
        if ( currentWorkingDir.equals("") ) {
          session.setWorkingDir( "" );
        }
        else if ( currentWorkingDir.equals("/") ) {
          session.setWorkingDir( currentWorkingDir );
        }
        else {
          session.setWorkingDir( currentWorkingDir + "/" );
        }
      }
      catch ( FTPException fe ) {}

      eventLevel++;
      buildDirectoryComboBox();
      eventLevel--;

      DataTransferManager.getInstance().list( session );

      scrollPane.getVerticalScrollBar().setValue( 0 );

      tableView.requestFocus();
    }
    else {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
    }

    return result;
  }

  public void buildDirectoryComboBox() {
    JComboBox cb = getDirectoryComboBox();
    cb.removeAllItems();

    String path = currentWorkingDir;

    cb.setEnabled( null != path && path.trim().length() > 0 );

    if ( null != path ) {
      eventLevel++;

      StringTokenizer tok = new StringTokenizer( path, "/" );
      String curPath = "/";
      
      cb.addItem( curPath );

      for ( int i = 0; tok.hasMoreTokens(); i++ ) {
        curPath += tok.nextToken() + "/";
        cb.addItem( curPath );
      }

      eventLevel--;

      upDirButton.setEnabled( !atRootLevel() );
      cb.setSelectedIndex( cb.getItemCount() - 1 );
    }		     
  }

  public boolean atRootLevel() {
    JComboBox cb = getDirectoryComboBox();
    return cb.getItemCount() <= 1;
  }

  public RemoteFileTable getTableView() {
    if ( null == tableView ) {
      RemoteFileTableModel model = null;
      Vector columns = new Vector( RemoteFileTable.getNumberOfColumns() );

      columns.add( LString.getString("FileListPanel.fileName", "Name") );
      columns.add( LString.getString("FileListPanel.fileSize", "Size") );
      columns.add( LString.getString("FileListPanel.dateModified",
                                     "Date Modified") );

      if ( Client.showFullColumnListing() ) {
        columns.add( LString.getString("FileListPanel.owner", "Owner") );
        columns.add( LString.getString("FileListPanel.permissions", "Permissions") );
      }
  
      model = new RemoteFileTableModel( (Vector)null, columns );
      tableView = new RemoteFileTable( model, this );

      tableView.setRowHeight(17);
      tableView.setRowSelectionAllowed(true);
      tableView.setColumnSelectionAllowed(false);
      tableView.setIntercellSpacing(new Dimension(0,0));
      tableView.setShowHorizontalLines(false);
      tableView.setShowVerticalLines(false);
      tableView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      TableColumn name = 
        tableView.getColumn(LString.getString("FileListPanel.fileName", 
                                              "Name"));
      name.setMinWidth(60);
      name.setPreferredWidth(300);

      TableColumn size = 
        tableView.getColumn(LString.getString("FileListPanel.fileSize", "Size"));
      size.setMinWidth(20);
      size.setPreferredWidth(120);

      TableColumn date = 
        tableView.getColumn(LString.getString("FileListPanel.dateModified",
                                              "Date Modified"));
      
      date.setMinWidth(40);
      date.setPreferredWidth(140);

      if ( Client.showFullColumnListing() ) {
        TableColumn owner =
          tableView.getColumn(LString.getString("FileListPanel.owner", "Owner"));
        owner.setMinWidth(30);
        owner.setPreferredWidth(100);

        TableColumn perms =
          tableView.getColumn(LString.getString("FileListPanel.permissions", "Permissions"));
        perms.setMinWidth(30);
        perms.setPreferredWidth(90);
      }

      //tableView.sizeColumnsToFit(0);
      tableView.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      tableView.doLayout();
    }

    return tableView;
  }
  
  public void buildTable( RemoteFileList rfl ) {
    removeAllRows();

    if (rfl == null)
    	return;
    
    if ( rfl.size() < 1 || !rfl.getFile(0).getFileName().equals(".") ) {
      RemoteFile rf = new RemoteFile();
      rf.setFileName(".");
      rf.setPermissions("d---------");
      rfl.add( 0, rf );
    }

    if ( rfl.size() < 2 || !rfl.getFile(1).getFileName().equals("..")) {
      RemoteFile rf = new RemoteFile();
      rf.setFileName("..");
      rf.setPermissions("d---------");
      rfl.add( 1, rf );
    }    

    int numOfCols = RemoteFileTable.getNumberOfColumns();

    for ( int i = 0; i < rfl.size(); i++ ) {
      Vector row = new Vector( numOfCols );
      RemoteFile file = rfl.getFile( i );

      String fileType = "f";

      if ( file.isDirectory() ) {
        fileType = "d";
      }
      else if ( file.isLink() ) {
        fileType = "d";
      }

      file.setMetaData( "fileType", fileType );

      if ( file.getFileName().equals(".") ) {
        continue;
      }
      else if ( file.getFileName().equals("..") ) {
/*
        row.add( file );
        row.add("");
        row.add("");
        if ( Client.showFullColumnListing() ) {
          row.add("");
          row.add("");
        }
*/
        continue;
      }
      else {
        if ( !Client.showHiddenFiles() && 
             file.getFileName().startsWith(".") ) {
          continue;
        }
        else {
          row.add( file );
          row.add( new Long(file.getFileSize()) );
          row.add( file.getDate() );
          if ( Client.showFullColumnListing() ) {
              row.add( file.getUser() );
              row.add( file.getPermissions() );
          }
        }
      }
      addRow( row );
    }

    int sortedColumn = Client.getRemoteView().getSortedColumn();
    boolean ascending = Client.getRemoteView().isSortedAscending();

    tableView.sortColumn( sortedColumn, ascending );
    //tableView.changeSelection( 0, 0, false, false );

    tableView.revalidate();
    tableView.repaint();

    RemoteFileTableModel model =
      (RemoteFileTableModel)getTableView().getModel();

    int totalRows = model.getRowCount() ;

    //setItemsLabel( totalRows - 1 );
    setItemsLabel( totalRows );

    remoteFileList = rfl;
  }

  protected void addRow( Vector row ) {
    try {
      ((RemoteFileTableModel)getTableView().getModel()).addRow( row ); 
    }
    catch ( ArrayIndexOutOfBoundsException aiobe ) {}
  }

  public void removeAllRows() {
    RemoteFileTableModel model = 
      (RemoteFileTableModel)getTableView().getModel();

    int totalRows = model.getRowCount() ;
    //for ( int i = totalRows-1; i >= 0; i-- ) {
    for ( int i = totalRows; i >= 0; i-- ) {
      try {
        model.removeRow(i);
      }
      catch ( ArrayIndexOutOfBoundsException aiobe ) {}
    }
  }

  public RemoteFileList getCurrentListing() { return remoteFileList; }

  protected void buildFields( JPanel panel, JComponent[] fields ) {

    for ( int i = 0; i < fields.length; i++ ) {
      panel.add( fields[i] );
    }

    SpringUtilities.makeCompactGrid( panel, 
                                     fields.length, 1, // rows, cols
                                     10, 10,           // init x, init y
	                             10, 10            // pad x, pad y
                                   );
  }

  public void download( RemoteFileList fileList ) {
    if ( !Client.getAllowDownload() )
      return;

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    DataTransferManager.getInstance().download( session, fileList );
  }

  public void downloadSelected() {
    RemoteFileList rfl = getTableView().getSelectedFiles();
    download( rfl );
  }

  public void upload( java.util.List fileList, RemoteFile remoteFile ) {
    if ( !Client.getAllowUpload() )
      return;

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null == remoteFile ) {
      remoteFile = new RemoteFile( currentWorkingDir );
    }
    DataTransferManager.getInstance().upload( session, fileList, remoteFile );
  }

  public RemoteFile fileExists( String fileName ) {
    RemoteFile result = null;
    RemoteFileList rfl = getTableView().getAllFiles();
    for ( int i = 0; i < rfl.size(); i++ ) {
      if ( fileName.equals(rfl.getFile(i).getFileName()) ) {
        result = rfl.getFile(i);
        break;
      }
    }
    return result;
  }
}

