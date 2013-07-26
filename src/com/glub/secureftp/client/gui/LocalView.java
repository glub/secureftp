//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalView.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

public class LocalView extends JTabbedPane implements PreferenceHandler {
  protected static final long serialVersionUID = 1L;
  private Preferences prefs = PreferencesDispatcher.getPreferences( this );

  private static final String SORTED_ASCENDING = "LocalColumnSortedAscending";

  private boolean sortedAscending = true;

  private LocalPanel localPanel = null;

  private String machineName = null;

  public LocalView() {
    super();

    localPanel = new LocalPanel( this );
    
    if ( Util.isWindows() ) {
      machineName = LString.getString("Common.machineName", "My Computer");
    }
    else {
      machineName = LString.getString("Common.machineName.mac_os", "Computer");
    }

    String passedInName = System.getProperty("hostname");
    if ( passedInName != null && passedInName.length() > 0 ) {
      machineName = passedInName;
    }

    String tabName = LString.getString("LocalPanel.name", "Your Computer");

    addTab( tabName, localPanel.getTablePanel() );

    Client.fixComponentColor( this );
  }

  public void selectAll() {
    if ( null != localPanel ) {
      localPanel.getTableView().selectAll();
    }
  }

  public void refresh() {
    if ( null != localPanel ) {
      if ( null != localPanel.getCurrentDirectory() ) {
        localPanel.list();
      }
      else {
        localPanel.listRoots();
      }
    }
  }

  public boolean atRootLevel() {
    boolean result = false;
    if ( null != localPanel ) {
      result = localPanel.atRootLevel();
    }
    return result;
  }

  public void setFocus() {
    if ( null != localPanel ) {
      localPanel.setFocus();
    }
  }

  public File getCurrentDirectory() {
    File file = null;

    if ( null != localPanel ) {
      file = localPanel.getCurrentDirectory();
    }

    return file;
  }

  public SecureFTPError changeDirectory( File newDir ) {
    SecureFTPError result = new SecureFTPError();

    if ( null != localPanel ) {
      result = localPanel.changeDirectory( newDir );
    }

    return result;
  }

  public SecureFTPError changeSessionDirectory( File newDir ) {
    SecureFTPError result = new SecureFTPError();

    if ( null != localPanel ) {
      result = localPanel.changeSessionDirectory( newDir );
    }

    return result;
  }

  public void uploadSelected() {
    if ( null != localPanel && Client.getAllowUpload() ) {
      ArrayList fl = localPanel.getTableView().getSelectedFiles();
      FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
      if ( null != session && fl.size() > 0 ) {
        ((RemotePanel)session.getRemoteUI()).upload( fl, null );
      }
    }
  }

  public void selectFile( File file ) {
    if ( null != localPanel ) {
      localPanel.selectFile( file, false );
    }
  }

  public void selectFiles( java.util.List files ) {
    if ( null != localPanel ) {
      localPanel.selectFiles( files );
    }
  }

  public ArrayList getSelectedFiles() {
    ArrayList result = new ArrayList();

    if ( null != localPanel ) {
      result = localPanel.getTableView().getSelectedFiles();
    }

    return result;
  }

  protected synchronized String getMachineName() { return machineName; }

  public void setSort( boolean sortedAscending ) {
    this.sortedAscending = sortedAscending;
  }

  public boolean isSortedAscending() {
    return sortedAscending;
  }

  public void readPreferences() {
    sortedAscending = prefs.getBoolean( SORTED_ASCENDING, true );
  }

  public void writePreferences() {
    prefs.putBoolean( SORTED_ASCENDING, sortedAscending );
  }
}

class LocalPanel extends JPanel {
  protected static final long serialVersionUID = 1L;
 
  private JScrollPane scrollPane = null;

  private int eventLevel = 0;

  private LocalView localView = null;

  private LocalFileTable tableView = null;

  private JPanel tablePanel = null;
  private JPanel directoryPanel = null;

  private JButton upDirButton = null;
  private JComboBox directoryComboBox = null;

  private File currentWorkingDir = null;

  public LocalPanel( LocalView parent ) {
    super();
    setAutoscrolls( true );
    this.localView = parent;
  }

  public JPanel getTablePanel() {
    if ( null == tablePanel ) {
      tablePanel = new JPanel();
      tablePanel.setLayout( new BorderLayout() );

      scrollPane = new JScrollPane();
      scrollPane.setViewport( new LocalFileTableViewport(getTableView()) );
      scrollPane.getViewport().setBackground( Color.white );
      scrollPane.getViewport().add( getTableView() );

      scrollPane.addMouseListener( new MouseAdapter() {
        public void mouseClicked( MouseEvent e ) {
          getTableView().changeSelection( 0, 0, false, false );
          getTableView().clearSelection();
          Client.getToolBar().updateToolBar();
          Client.getMenus().updateMenuBar();
        }
      } );

      tablePanel.add( getDirectoryPanel(), BorderLayout.NORTH );
      tablePanel.add( scrollPane, BorderLayout.CENTER );

      if ( null == getCurrentDirectory() ) {
        String localDirStr = (String)System.getProperty("user.dir");
        currentWorkingDir = new File( localDirStr );
      }

      changeDirectory( getCurrentDirectory(), true );
    }

    return tablePanel;
  }

  public File getCurrentDirectory() {
/*
    if ( null == currentWorkingDir ) {
      String localDirStr = (String)System.getProperty("user.dir");
      currentWorkingDir = new File( localDirStr );
    }
*/

    return currentWorkingDir;
  }

  public JScrollPane getScrollPane() { return scrollPane; }

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

  protected JButton getUpDirButton() {
    if ( null == upDirButton ) {
      ImageIcon icon = 
        new ImageIcon( getClass().getResource("images/prev_dir_button.png") );
      upDirButton = new JButton( icon );

      ImageIcon pressedIcon = 
        new ImageIcon( getClass().getResource("images/prev_dir_button_sel.png") );
      upDirButton.setPressedIcon( pressedIcon );

      upDirButton.setFocusable( false );
      upDirButton.setBorder( BorderFactory.createEmptyBorder(2, 2, 2, 2) );
      upDirButton.setBorderPainted( false );
      upDirButton.setRolloverEnabled( false );
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
          changeDirectoryUp();
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
      directoryPanel.setLayout(gridbag);

      Client.fixComponentColor( directoryPanel );

      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.WEST;
      if ( Util.isMacOS() ) {
        c.insets = new Insets(0,0,0,0);
      }
      else {
        c.insets = new Insets(0,0,0,2);
      }
      c.weightx = 0.0;
      c.weighty = 0.0;
      c.gridx = 0;
      c.gridy = 0;

      gridbag.setConstraints(getUpDirButton(), c);
      directoryPanel.add( getUpDirButton() );

      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.EAST;

      if ( Util.isMacOS() ) {
        c.insets = new Insets(1,0,0,3);
      }
      else {
        c.insets = new Insets(0,0,0,3);
      }

      c.weightx = 1.0;
      c.weighty = 1.0;
      c.gridx = 1;
      c.gridy = 0;

      gridbag.setConstraints(getDirectoryComboBox(), c);
      directoryPanel.add( getDirectoryComboBox() );

      Client.fixComponentColor( directoryPanel );
    }

    return directoryPanel;
  }

  protected JComboBox getDirectoryComboBox() {
    if ( null == directoryComboBox ) {
      directoryComboBox = new JComboBox();
      directoryComboBox.setRenderer( new LocalDirectoryComboBoxRenderer() );
      directoryComboBox.setEditable( false );

      directoryComboBox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          if ( eventLevel < 1 && 
            e.getActionCommand().equals("comboBoxChanged") ) {
            //final String newDir = (String)directoryComboBox.getSelectedItem();
            Thread cdt = new Thread() {
              private String sep = System.getProperty("file.separator");
              private String path = "";

              public void run() {
		if ( directoryComboBox.getSelectedIndex() == 0 ) {
                  if ( !Util.isWindows() ) {
                    if ( Util.isMacOS() ) {
                      path = "/Volumes/";
                    }
                    else {
                      path = sep;
                    }
                  }
                }
                else {
		  path = (String)directoryComboBox.getSelectedItem();
                }

                File newDir = null;
                if ( path.length() > 0 ) {
                  newDir = new File( path );
                }

                changeDirectory( newDir );
              };
            };
            cdt.start();
          }
        }
      } );
    }

    return directoryComboBox;
  }

  public void changeDirectoryUp() {
    JComboBox cb = getDirectoryComboBox();
    cb.setSelectedIndex( cb.getItemCount() - 2 );
  }

  public SecureFTPError changeDirectory( File newDir ) {
    return changeDirectory( newDir, false );
  }

  private SecureFTPError changeDirectory( File newDir, 
                                          boolean initializing ) {
    SecureFTPError result = new SecureFTPError();

    if ( null == newDir || newDir.getAbsolutePath().trim().length() == 0 ) {
      newDir = null;
    }
    else {
      if ( !newDir.exists() ) {
        LString msg = 
          new LString("CDCommand.noSuchDir", "[^0]: No such directory.");
        msg.replace(0, newDir.getAbsolutePath());
        ErrorDialog.showDialog( msg );
        result.setCode( SecureFTPError.NO_SUCH_FILE );
        return result;
      }

      if ( !newDir.isDirectory() ) {
        newDir = newDir.getParentFile();
      }
    }

    if ( !initializing && newDir == currentWorkingDir ) {
      return result;
    }

    if ( !initializing ) {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );
    }

    result = changeSessionDirectory( newDir );

    currentWorkingDir = newDir;

    if ( null == newDir ) {
      listRoots();
    }
    else {
      list( newDir );
    }

    scrollPane.getVerticalScrollBar().setValue( 0 );

    eventLevel++;
    buildDirectoryComboBox();
    eventLevel--;

    tableView.requestFocus();

    if ( !initializing ) {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
    }

    return result;
  }

  public SecureFTPError changeSessionDirectory( File newDir ) {
    SecureFTPError result = new SecureFTPError();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    
    if ( session != null ) {
      try {
        session.setLocalDir( newDir );
      }
      catch ( FileNotFoundException fnfe ) {
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }
    }

    return result;
  }

  public void buildDirectoryComboBox() {
    //FontMetrics metrics = getFontMetrics( getFont() );

    JComboBox cb = getDirectoryComboBox();
    cb.removeAllItems();

    cb.addItem( localView.getMachineName() );

    if ( null != currentWorkingDir ) {
      String path = currentWorkingDir.getAbsolutePath();

      if ( Util.isMacOS() ) {
        File[] roots = GTFileSystemView.getProperRoots();
        if ( !path.startsWith("/Volumes") ) {
          cb.addItem( "/Volumes/" + 
                      GTFileSystemView.getProperSystemDisplayName(roots[0]) );
        }
      }

      String sep = System.getProperty("file.separator");

      StringTokenizer tok = new StringTokenizer( path, sep );
      
      String fullPath = "";

      if ( ! Util.isWinOS() )
        fullPath = sep;

      for ( int i = 0; tok.hasMoreTokens(); i++ ) {
        String token = tok.nextToken();

        fullPath += token + sep;

        if ( Util.isMacOS() ) {
          if ( token.equals("Volumes") ) {
            token = null;
          }
        }

        if ( null != token ) {
          cb.addItem( fullPath );
        }
      }
    }

    upDirButton.setEnabled( !atRootLevel() );
    cb.setSelectedIndex( cb.getItemCount() - 1 );
  }

  public boolean atRootLevel() {
    JComboBox cb = getDirectoryComboBox();
    return cb.getItemCount() <= 1;
  }

  public void listRoots() {
    File[] dirList = File.listRoots();
    buildTable( dirList );
  }

  public void list() {
    list( (File)null );
  }

  public void list( File dir ) {
    if ( null == dir ) {
      dir = currentWorkingDir;
    }

    File[] dirList = dir.listFiles();

    if ( Util.isMacOS() ) {
      try {
        if ( dir.getCanonicalPath().equals("/Volumes") ) {
          dirList = GTFileSystemView.getProperRoots();
        }
      }
      catch ( IOException ioe ) {}
    }

    buildTable( dirList );
  }

  private Map getHiddenFilesMap() {
    HashMap result = new HashMap();

    if ( Util.isMacOS() ) {
      File hiddenFileList = new File( getCurrentDirectory(), ".hidden" );
      if ( null != hiddenFileList && !hiddenFileList.exists() ) {
        hiddenFileList = new File( "/.hidden" );
      }

      if ( null != hiddenFileList && hiddenFileList.exists() ) {
        try {
          FileInputStream fis = new FileInputStream( hiddenFileList );
          BufferedReader br =
            new BufferedReader( new InputStreamReader(fis) );
          String line = null;
          while ( (line = br.readLine()) != null ) {
            result.put( line.trim(), "true" );
          }
        }
        catch ( Exception e ) {}

        result.put( "Cleanup At Startup", "true" );
        result.put( "TheFindByContentFolder", "true" );
        result.put( "TheVolumeSettingsFolder", "true" );
      }
    }

    return result;
  }

  public LocalFileTable getTableView() {
    if ( null == tableView ) {
      LocalFileTableModel model = null;
      Vector columns = new Vector( LocalFileTable.getNumberOfColumns() );

      columns.add( LString.getString("FileListPanel.fileName", "Name") );
  
      model = new LocalFileTableModel( (Vector)null, columns );
      tableView = new LocalFileTable( model, this );

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

      name.setMinWidth(15);

      tableView.sizeColumnsToFit(0);
    }

    return tableView;
  }
  
  public void buildTable( File[] dirList ) {
    removeAllRows();

    Vector upDir = new Vector(1);
/*
    upDir.add( ".." );
    addRow( upDir );
*/

    Map hiddenFilesMap = getHiddenFilesMap();

    for( int i = 0; null != dirList && i < dirList.length; i++ ) {
      File fileToAdd = dirList[i];

      if ( !Client.showHiddenFiles() ) {
        if ( hiddenFilesMap.get(dirList[i].getName()) != null &&
             hiddenFilesMap.get(dirList[i].getName()).toString().equals("true") ) {
          fileToAdd = null; 
        }
        else if ( !FileSystemView.getFileSystemView().isDrive(fileToAdd) &&
                  FileSystemView.getFileSystemView().isHiddenFile(fileToAdd) ) {
          fileToAdd = null; 
        }
      }

      if ( null != fileToAdd ) {
        Vector row = new Vector(1);
        row.add( dirList[i] );
        addRow( row );
      }
    }

    boolean ascending = localView.isSortedAscending();

    tableView.sortColumn( 0, ascending );
    //tableView.changeSelection( 0, 0, false, false );
    tableView.revalidate();
    tableView.repaint();
  }

  protected void addRow( Vector row ) {
    try {
      ((LocalFileTableModel)getTableView().getModel()).addRow( row ); 
    }
    catch ( ArrayIndexOutOfBoundsException aiobe ) {}
  }

  public int selectFile( File file, boolean additive ) {
    int row = -1;
    LocalFileTableModel model = 
      (LocalFileTableModel)getTableView().getModel();

    int totalRows = model.getRowCount() ;
    int column = tableView.getFileColumnIndex();
    //for ( int i = 1; i < totalRows; i++ ) {
    for ( int i = 0; i < totalRows; i++ ) {
      File curFile = (File)model.getValueAt(i, column);
      if ( curFile.getAbsolutePath().equals(file.getAbsolutePath()) ) {
        if ( additive ) {
          tableView.addRowSelectionInterval( i, i );
        }
        else {
          tableView.changeSelection( i, column, false, false );
        }
        row = i;
        break;
      }
    }

    Client.getToolBar().updateToolBar();
    Client.getMenus().updateMenuBar();

    return row;
  }

  public void selectFiles( java.util.List files ) {
    tableView.clearSelection();
    int lastRow = -1;
    for ( int i = 0; i < files.size(); i++ ) {
      File file = (File)files.get(i);
      lastRow = selectFile( file, files.size() > 1 );
    }

    if ( lastRow >= 0 && files.size() > 1 && getAutoscrolls() ) {
      Rectangle cellRect = tableView.getCellRect( lastRow, 0, false );
      if ( cellRect != null ) {
        scrollRectToVisible( cellRect );
      }
    }

    tableView.revalidate();
    tableView.repaint();
    scrollPane.repaint();
  }

  public void removeAllRows() {
    LocalFileTableModel model = 
      (LocalFileTableModel)getTableView().getModel();

    int totalRows = model.getRowCount() ;
    //for ( int i = totalRows-1; i >= 0; i-- ) {
    for ( int i = totalRows; i >= 0; i-- ) {
      try {
        model.removeRow(i);
      }
      catch ( ArrayIndexOutOfBoundsException aiobe ) {}
    }
  }
}

