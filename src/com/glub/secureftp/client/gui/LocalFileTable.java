
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalFileTable.java 128 2009-12-10 09:18:22Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.table.*;
import com.glub.util.*;
import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

public class LocalFileTable extends JSortTable {
  protected static final long serialVersionUID = 1L;	
  private LocalPanel localPanel = null;

  public static final int FILE_COLUMN = 0;

  private static final int NUMBER_OF_COLUMNS = 1;

  private TableDropTargetListener dropTargetListener;

  private LocalFilePopupMenu popupMenu = new LocalFilePopupMenu(this);

  private MouseAdapter ma = null;
  
  public LocalFileTable( SortTableModel model, 
                         final LocalPanel localPanel ) {
    super( model );
    setBackground( Color.WHITE );
    setAutoscrolls( true );
    this.localPanel = localPanel;

    TableColumnModel tcm = getColumnModel();

    TableColumn fileColumn = tcm.getColumn( FILE_COLUMN );
    TableCellRenderer fileRenderer = new FileCell();
    fileColumn.setCellRenderer( fileRenderer );

    LocalFileTransferHandler fth = 
      new LocalFileTransferHandler( localPanel, this );
    setTransferHandler( fth );

    addKeyListener( new KeyAdapter() {
      public void keyPressed( KeyEvent e ) {
        if ( KeyEvent.VK_BACK_SPACE == e.getKeyCode() ) {
          Thread cdThread = new Thread() {
            public void run() {
              localPanel.changeDirectoryUp();
            }
          };
          cdThread.start();
        }
        else if ( KeyEvent.VK_F5 == e.getKeyCode() ) {
          Thread lsThread = new Thread() {
            public void run() {
              localPanel.list();
            }
          };
          lsThread.start();
        }
        else {
          String modifier = 
            KeyEvent.getKeyModifiersText(e.getModifiers()).toLowerCase();

          if ( modifier != null && !modifier.equals("shift") && 
               modifier.length() > 0 ) {
            return;
          }

          int numOfRows = getRowCount();

          int i = 0;
          int val = 0;

          int startingPoint = getSelectedRow() + 1;
          //if ( startingPoint <= 0 ) { startingPoint = 1; }
          if ( startingPoint < 0 ) { startingPoint = 0; }

          boolean foundMatch = false;
          for( i = startingPoint, val = startingPoint; 
               i < numOfRows + startingPoint; i++, val++ ) {
            if ( val >= numOfRows ) {
              //val = 1; 
              val = 0; 
            }

            File file = (File)getValueAt( val, getFileColumnIndex() );

            if ( null != file && file.getName().length() > 0 ) {
              char firstChar = file.getName().toLowerCase().charAt(0);

              StringBuffer buf = new StringBuffer();
              buf.append(e.getKeyChar());
              String typedLowerStr = buf.toString().toLowerCase();

              if ( typedLowerStr.length() > 0 && 
                   firstChar == typedLowerStr.charAt(0) ) {
                foundMatch = true;
                break;
              }
            }
          }

          if ( foundMatch ) {
            changeSelection( val, FILE_COLUMN, false, false );
            Client.getToolBar().updateToolBar();
            Client.getMenus().updateMenuBar();
          }
        }
      }
    } );

    addMouseListener( ma = new MouseAdapter() {
      private ArrayList rowSelections = new ArrayList( 4 );
      public void mouseClicked( MouseEvent e ) {
        if ( e.getClickCount() >= 2 &&
             !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1 ) {
          int currentlySelectedRow = getSelectedRow();
          int[] selectedRows = (int[])rowSelections.get( 0 );
          boolean reselect = false;
          for ( int i = 0; i < selectedRows.length; i++ ) {
            if ( currentlySelectedRow == selectedRows[i] ) {
              reselect = true;
              addSelectedRows( selectedRows );
              break;
            }
          }

          for ( int i = 0; reselect && i < selectedRows.length; i++ ) {
            addRowSelectionInterval( selectedRows[i], selectedRows[i] );
          }

          Thread dblClick = new Thread() {
            public void run() {
              handleDoubleClick();
            }
          };
          dblClick.start();
        }
      }

      public void mouseReleased( MouseEvent e ) {
        int macContextMask = MouseEvent.CTRL_DOWN_MASK;
        int unixContextMask = MouseEvent.META_DOWN_MASK;

        int sysContextMask = Util.isMacOS() ? macContextMask : unixContextMask;

        int rap = rowAtPoint(e.getPoint());
        int[] selectedRows = getSelectedRows();

        if ( e.isPopupTrigger() ) {
          boolean clearSelection = true;
          for ( int i = 0; i < getSelectedRowCount(); i++ ) {
            if ( selectedRows[i] == rap ) {
              clearSelection = false;
            }
          }

          if ( clearSelection ) {
            clearSelection();
          }

          if ( rap >= 0 ) {
            addRowSelectionInterval(rap, rap);
          }

          popupMenu.updateMenu();
          popupMenu.show( e.getComponent(), e.getX(), e.getY() );
        }
        else if ( !Util.isWindows() &&
                  ((e.getModifiersEx() & macContextMask) == sysContextMask ||
                   (e.getModifiersEx() & unixContextMask) == sysContextMask ||
                   4 == e.getModifiers()) ) {
          boolean clearSelection = true;
          for ( int i = 0; i < getSelectedRowCount(); i++ ) {
            if ( selectedRows[i] == rap ) {
              clearSelection = false;
            }
          }

          if ( clearSelection ) {
            clearSelection();
          }

          if ( rap >= 0 ) {
            addRowSelectionInterval(rap, rap);
          }

          popupMenu.updateMenu();
          popupMenu.show( e.getComponent(), e.getX(), e.getY() );
        }

        addSelectedRows( getSelectedRows() );
      }

      private void addSelectedRows( int[] selectedRows ) {
        rowSelections.add( selectedRows );
        if ( rowSelections.size() > 3 ) {
          rowSelections.remove( 0 );
      	}
        Client.getToolBar().updateToolBar(); 
        Client.getMenus().updateMenuBar();
      }
    } );

    /*
    addMouseListener( new MouseAdapter() {
      public void mousePressed( MouseEvent e ) { 
        JComponent comp = (JComponent)e.getSource();
        TransferHandler th = comp.getTransferHandler();
        th.exportAsDrag( comp, e, TransferHandler.COPY );
      }
    } );
    */

    dropTargetListener = new TableDropTargetListener();
    new DropTarget( this, dropTargetListener );
    //DragSource ds = DragSource.getDefaultDragSource();
    //ds.addDragSourceListener( this );

    setEnterBehavior();
    setTabBehavior();
    setArrowKeysBehavior();

    setDragEnabled( true );
  }

  public int getFileColumnIndex() {
    return convertColumnIndexToView( FILE_COLUMN );
  }

  protected MouseAdapter getMouseAdapter() { return ma; }

  public TableDropTargetListener getDropTargetListener() { 
    return dropTargetListener; 
  }

  public void setEnterBehavior() {
    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke("ENTER"), "stop-and-next-cell");

    getActionMap().put("stop-and-next-cell", new AbstractAction() {
      protected static final long serialVersionUID = 1L;	
      public void actionPerformed(ActionEvent e) {
        Thread dblClick = new Thread() {
          public void run() {
            handleDoubleClick();
          }
        };
        dblClick.start();
      }
    } );
  }

  public void setTabBehavior() {
    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tab-local-view");

    getActionMap().put("tab-local-view", new AbstractAction() {
      protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        Thread switchToRemote = new Thread() {
          public void run() {
            FTPSession session = 
             FTPSessionManager.getInstance().getCurrentSession();
            if ( null != session ) {
              ((RemotePanel)session.getRemoteUI()).setFocus();
            }
          }
        };
        switchToRemote.start();
      }
    } );
  }

  public void setArrowKeysBehavior() {
    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke("DOWN"), "next-cell");
    getActionMap().put("next-cell", new AbstractAction() {
      protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        int row = getSelectedRow();
        if ( row + 1 < getRowCount() ) {
          changeSelection( row + 1, 0, false, false );
          Client.getToolBar().updateToolBar(); 
          Client.getMenus().updateMenuBar();
        }
      }
    } );

    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke("UP"), "prev-cell");
    getActionMap().put("prev-cell", new AbstractAction() {
      protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        int row = getSelectedRow();
        if ( row - 1 >= 0 ) {
          changeSelection( row - 1, 0, false, false );
          Client.getToolBar().updateToolBar(); 
        }
      }
    } );
  }
    
  public void selectAll() {
    clearSelection();
    //addRowSelectionInterval( 1, getRowCount() - 1 );
    addRowSelectionInterval( 0, getRowCount() - 1 );
    Client.getToolBar().updateToolBar();
    Client.getMenus().updateMenuBar();
  }

  public void handleDoubleClick() {
    int row = getSelectedRow();

    File file = null;

    if ( row >= 0 ) {
      Object currentVal = getValueAt( row, getFileColumnIndex() );
      if ( currentVal instanceof File ) {
        file = (File)currentVal;
      }
      else {
        // must be dir up
        localPanel.changeDirectoryUp();
      }
    }

    //boolean selectedIsFile = true;

    boolean isDirectory = false;

    if ( null != file ) {
      isDirectory = file.isDirectory();

      // check to make sure it's really a "directory"
      if ( isDirectory )
        isDirectory = !Util.isPackage(file);
    }

    // if this is a directory, try changing into it
    if ( isDirectory ) {
      SecureFTPError error = localPanel.changeDirectory( file );

      int resultCode = error.getCode();

      /*
      selectedIsFile = SecureFTPError.NOT_A_DIRECTORY == resultCode ||
                       SecureFTPError.UNKNOWN == resultCode;
	  */
      
      if ( SecureFTPError.PERMISSION_DENIED == resultCode ) {
        LString lstr = LString.getLocalizedString( error.getMessage() );
        ErrorDialog.showDialog( lstr );
      }
    }

    ArrayList selectedFiles = getSelectedFiles();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null != session && selectedFiles.size() > 0 ) {
      RemotePanel remotePanel = (RemotePanel)session.getRemoteUI();
      remotePanel.upload( selectedFiles, new RemoteFile(".") );
    }
  }

  public ArrayList getSelectedFiles() {
    int[] rows = getSelectedRows();

    ArrayList selectedFiles = new ArrayList();

    for( int i = 0; i < rows.length; i++ ) {
      Object currentValue = getValueAt( rows[i], getFileColumnIndex() );
      File currentFile = null;
     
      if ( currentValue instanceof File ) {
        currentFile = (File)getValueAt( rows[i], getFileColumnIndex() );

        boolean selectedIsFile = 
          currentFile.isFile() || currentFile.isDirectory();

        if ( selectedIsFile ) {
          selectedFiles.add( currentFile );
        }
      }
    }

    return selectedFiles;
  }

  public static int getNumberOfColumns() { return NUMBER_OF_COLUMNS; }

  public void sortColumn( int column, boolean ascending ) {
    ((SortTableModel)getModel()).sortColumn( column, ascending );
    sortedColumnIndex = column;
    sortedColumnAscending = ascending;
    getTableHeader().resizeAndRepaint();
    resizeAndRepaint();
  }

  public void mouseReleased( MouseEvent e ) {
    super.mouseReleased( e );
    Client.getLocalView().setSort( sortedColumnAscending );
  }

  public void cueUIDrop( boolean cue ) {
    if ( cue ) {
      localPanel.getScrollPane().setViewportBorder( 
        BorderFactory.createLineBorder(SystemColor.windowBorder, 3) );
    }
    else {
      localPanel.getScrollPane().setViewportBorder( 
      BorderFactory.createLineBorder(Color.white, 0) );
      //clearSelection();
    }

    localPanel.getScrollPane().updateUI();
  }

  /**
   * TableDropTargetListener class
   */
  class TableDropTargetListener extends BasicDropTargetListener {
    private int[] rows;
    private int[] cols;

    private int[] selectedRows;

    protected void saveComponentState(JComponent comp) {
      if ( !(comp instanceof JTable) ) { return; }

      JTable table = (JTable) comp;
      rows = table.getSelectedRows();
      cols = table.getSelectedColumns();
    }

    protected void restoreComponentState(JComponent comp) {
      if ( !(comp instanceof JTable) ) { return; }

      JTable table = (JTable) comp;
      table.clearSelection();
      for (int i = 0; i < rows.length; i++) {
        table.addRowSelectionInterval(rows[i], rows[i]);
      }
      for (int i = 0; i < cols.length; i++) {
        table.addColumnSelectionInterval(cols[i], cols[i]);
      }
    }

    protected void updateInsertionLocation(JComponent comp, Point p) {
      if ( !(comp instanceof JTable) ) { return; }

      JTable table = (JTable) comp;
      int row = table.rowAtPoint( p );

      File file = null;
	
      //if ( row > 0 ) {
      if ( row >= 0 ) {
        file = (File)getValueAt( row, getFileColumnIndex() );
      }
	  
      //if ( null != file && file.isDirectory() && row > 0 ) {
      if ( null != file && file.isDirectory() && row >= 0 ) {
        setRowSelectionInterval( row, row );
      }
      else {
        clearSelection();
      } 
    }

    public void drop( DropTargetDropEvent dtde ) {
      Transferable tr = dtde.getTransferable();
      JComponent component = getComponent( dtde );
      TransferHandler th = component.getTransferHandler();
      boolean canImport = th.canImport(component, dtde.getCurrentDataFlavors());

      DataFlavor[] flavors = dtde.getCurrentDataFlavors();

      DataFlavor urlFlavor =
        new DataFlavor( "application/x-java-url; class=java.net.URL", "URL" );

      DataFlavor arrayListFlavor = 
        new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType +
                        "; class=java.util.ArrayList", 
                        "ArrayList" );

      if ( flavors.length < 0 ) {
        return;
      }

      if ( (urlFlavor.equals(flavors[0]) || 
            DataFlavor.javaFileListFlavor.equals(flavors[0])) && canImport ) {
        dtde.acceptDrop( DnDConstants.ACTION_COPY );

        int row = getSelectedRow();

        File file = null;
	
        if ( row >= 0 ) {
          file = (File)getValueAt( row, getFileColumnIndex() );
        }

        if ( null != file && !file.isDirectory() ) {
          file = null;
        }

        boolean clearSelection = true;

        try {
          java.util.List fileList = 
           (java.util.List)tr.getTransferData( DataFlavor.javaFileListFlavor );

          if ( null != fileList && fileList.size() > 0 ) {
            File curFile = (File)fileList.get(0);
            Client.getLocalView().changeDirectory( curFile );
            if ( curFile.isFile() ) {
              Client.getLocalView().selectFile( curFile );
              clearSelection = false;
            }
          }
        }
        catch ( IOException ioe ) {
        }
        catch ( UnsupportedFlavorException ufe ) {
        }

        cueUIDrop( false );

        if ( clearSelection ) {
          clearSelection();
        }
      }
      else if (arrayListFlavor.equals(flavors[0]) &&
               "RemoteFileList".equals(flavors[0].getHumanPresentableName()) && 
               canImport) {
        try {
          ArrayList remoteFileList = 
            (ArrayList)tr.getTransferData( arrayListFlavor );

          selectedRows = getSelectedRows();

          boolean changedDir = false;
          File currentDir =
            Client.getLocalView().getCurrentDirectory();

          if ( selectedRows.length > 0 ) {
            File file = (File)getValueAt( selectedRows[0], getFileColumnIndex() );
            Client.getLocalView().changeSessionDirectory( file );
            changedDir = true;
          }

          RemoteFileList rfl = new RemoteFileList( remoteFileList );
          RemotePanel remotePanel = (RemotePanel)
            FTPSessionManager.getInstance().getCurrentSession().getRemoteUI();
          if ( null != remotePanel ) {
            remotePanel.download( rfl );
          }

          if ( changedDir ) {
            Client.getLocalView().changeSessionDirectory( currentDir );
          }
        }
        catch ( IOException ioe ) {
        }
        catch ( UnsupportedFlavorException ufe ) {
        }
      }
      else {
        dtde.rejectDrop();
      }

      cleanup();
      cueUIDrop( false );
    }

    public void dragEnter( DropTargetDragEvent dtde ) {
      selectedRows = getSelectedRows();

      super.dragEnter( dtde );

      JComponent component = 
        (JComponent)dtde.getDropTargetContext().getComponent();
      TransferHandler th = component.getTransferHandler();
      boolean canImport = th.canImport(component, dtde.getCurrentDataFlavors());

      if ( canImport ) {
        dtde.acceptDrag( DnDConstants.ACTION_COPY );
      }
      else {
        dtde.rejectDrag();
      }
    }

    public void dragExit( DropTargetEvent dte ) {
      clearSelection();
      for ( int i = 0; i < selectedRows.length; i++ ) {
        addRowSelectionInterval( selectedRows[i], selectedRows[i] ); 
      }

      super.dragExit( dte );
      cueUIDrop( false );
      //changeSelection( 0, 0, false, false );
    }
  }

  /**
   * LocalFileTransferHandler class
   */
  class LocalFileTransferHandler extends TransferHandler {
	protected static final long serialVersionUID = 1L;
    public Transferable transferable;
    private LocalPanel localPanel;
    private JTable table;

    public LocalFileTransferHandler() {
      super();
    }

    public LocalFileTransferHandler( LocalPanel localPanel, JTable table ) {
      super();
      this.localPanel = localPanel;
      this.table = table;
    }

    public Transferable createTransferable( JComponent comp ) {
      transferable = new LocalFileTransfer(localPanel, table);
      return transferable;
    }

    public int getSourceActions( JComponent c ) {
      return TransferHandler.COPY;
    }

    public void exportAsDrag( JComponent comp, InputEvent e, int action ) {
      super.exportAsDrag(comp, e, action);
    }

    public boolean canImport( JComponent comp, DataFlavor[] transferFlavors ) {
      String name = transferFlavors[0].getHumanPresentableName();
      boolean canImport = name.equals("RemoteFileList") ||
                          name.equals("application/x-java-url") ||
                          name.equals("application/x-java-file-list");

      if ( canImport ) {
        cueUIDrop( true );
      }

      return canImport;
    }

    protected void exportDone( JComponent c, Transferable data, int action ) {
      cueUIDrop( false );
    }

    /*
    public boolean importData( JComponent c, Transferable tr ) {
      boolean status = false;

      if ( tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ) {
        int row = getSelectedRow();

        RemoteFile file = 
          //(File)localPanel.getCurrentListing().get( row + 1 );
          (File)localPanel.getCurrentListing().get( row );

        if ( !file.isDirectory() || 
             file.getName().equals("..") ) {
          file = (File)localPanel.getCurrentListing().get( 0 );
        }

        try {
          java.util.List fileList = 
           (java.util.List)tr.getTransferData( DataFlavor.javaFileListFlavor );
        }
        catch ( IOException ioe ) {
        }
        catch ( UnsupportedFlavorException ufe ) {
        }

        cueUIDrop( false );
        status = true;
      }

      return status;
    }
    */
  }

  /*
  public void dragDropEnd( DragSourceDropEvent dsde ) {}
  public void dragEnter( DragSourceDragEvent dsde ) {}
  public void dragExit( DragSourceEvent dse ) {}
  public void dragOver( DragSourceDragEvent dsde ) {}
  public void dropActionChanged( DragSourceDragEvent dsde ) {}
  */
  
  class FileCell extends DefaultTableCellRenderer {
	protected static final long serialVersionUID = 1L;
    private FileSystemView fsv = FileSystemView.getFileSystemView();

    private Icon dirIcon = UIManager.getIcon("FileView.directoryIcon");

    private Icon fileIcon = UIManager.getIcon("FileView.fileIcon");

    private JFileChooser chooser = new JFileChooser();

    /*
    private ImageIcon linkIcon = 
      new ImageIcon( getClass().getResource("images/linkIcon.png") );
    */
    
    protected void setValue( Object value ) {
      File file = (File)value;

      Icon iconToDisplay = fileIcon;

      if ( null != file ) {
        if ( fsv.isDrive(file) ) {
          try {
            iconToDisplay = chooser.getIcon( file );
            //iconToDisplay = fsv.getSystemIcon(file);
          }
          catch ( Exception e ) {
	    iconToDisplay = UIManager.getIcon("FileView.hardDriveIcon");
          }
        }
        else if ( /* Util.isWindows() && */ file.exists() ) {
          try {
            iconToDisplay = chooser.getIcon( file );
            //iconToDisplay = fsv.getSystemIcon(file);
          }
          catch ( Exception e ) {
            iconToDisplay = UIManager.getIcon("FileView.fileIcon");
          }
        }
        else if ( file.isDirectory() ) {
          if ( Util.isPackage(file) ) {
            iconToDisplay = fileIcon;
          }
          else {
            iconToDisplay = dirIcon;
          }
	}
        else {
          iconToDisplay = fileIcon;
        }

        String name = 
          FileSystemView.getFileSystemView().getSystemDisplayName( file );

        if ( null == name || name.length() <= 0 ) {
          name = file.toString();
        }

        //setText( " " + name + " " );
        setText( name + " " );
        setIcon( iconToDisplay );
      }
    }

    // get rid of selection highlight
    public Component getTableCellRendererComponent( JTable table, Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus, int row,
                                                    int col ) {
      JComponent c = 
        (JComponent)super.getTableCellRendererComponent( table, value,
                                                         isSelected, hasFocus,
                                                         row, col );
      if ( hasFocus || isSelected ) {
        c.setBorder( DefaultTableCellRenderer.noFocusBorder );
      }

      return c;
    }
  }

  public String getToolTipText( MouseEvent e ) {
    int row = rowAtPoint( e.getPoint() );
/*
    if ( row < 1 ) {
      return LString.getString("FileListPanel.tooltip.upDir", 
                               "Move up directory");
    }
*/

    StringBuffer buffer = new StringBuffer();
    buffer.append( "<html><table cellpadding=2><tr><td>" );

    File file = (File)getValueAt( row, getFileColumnIndex() );

    String fileName = file.getAbsolutePath();

    try {
      fileName = file.getCanonicalPath();
    }
    catch ( Exception dummy ) {}

    buffer.append( "<b>" + fileName + "</b><br>");

    double size = file.length();
    double kb = size / 1024;
    double mb = kb / 1024;
    double gb = mb / 1024;

    DecimalFormat df = new DecimalFormat("#.##");
    df.setGroupingUsed( true );
    df.setMaximumFractionDigits( 2 );

    String bytesStr = LString.getString("Common.xferMetric.bytes", "bytes");
    String kbStr = LString.getString("Common.xferMetric.kb", "KB");
    String mbStr = LString.getString("Common.xferMetric.mb", "MB");
    String gbStr = LString.getString("Common.xferMetric.gb", "GB");

    if ( kb >= 1 && mb < 1 ) {
      buffer.append( df.format(kb) + " " + kbStr + " <br>" );
    }
    else if ( mb >= 1 && gb < 1 ) {
      buffer.append( df.format(mb) + " " + mbStr + " <br>" );
    }
    else if ( gb >= 1 ) {
      buffer.append( df.format(gb) + " " + gbStr + " <br>" );
    }
    else {
      buffer.append( df.format(size) + " " + bytesStr + " <br>" );
    }

    DateFormat timeDateFormat = 
      new SimpleDateFormat(LString.getString("Common.timeDateFormat", 
                                             "M/d/yyyy h:mm a") );
    
    buffer.append( timeDateFormat.format(new Date(file.lastModified())) );
    buffer.append( "</td></tr></table></html>" );

    return buffer.toString();
  }
}

class LocalFileTableViewport extends JViewport implements DropTargetListener {
  protected static final long serialVersionUID = 1L;
  protected DropTarget dropTarget = new DropTarget( this, this );
  private final LocalFileTable dropTable;

  public LocalFileTableViewport( LocalFileTable dt ) {
    super();
    dropTable = dt;

    addMouseListener( new MouseAdapter() {
      public void mouseClicked( MouseEvent e ) {
        dropTable.clearSelection();
        Client.getLocalView().setFocus();
        Client.getToolBar().updateToolBar(); 
        Client.getMenus().updateMenuBar();
      }

      public void mouseReleased( MouseEvent e ) {
        dropTable.getMouseAdapter().mouseReleased(e);
      }
    } );
  }

  public TransferHandler getTransferHandler() { 
    return dropTable.getTransferHandler(); 
  }

  public void dragEnter( DropTargetDragEvent dtde ) {
    dropTable.getDropTargetListener().dragEnter( dtde );
    dropTable.clearSelection();
  }

  public void dragExit( DropTargetEvent dte ) {
    dropTable.getDropTargetListener().dragExit( dte );
  }

  public void dragOver( DropTargetDragEvent dtde ) {
    dropTable.getDropTargetListener().dragOver( dtde );
  }

  public void dropActionChanged( DropTargetDragEvent dtde ) {
    dropTable.getDropTargetListener().dropActionChanged( dtde );
  }

  public synchronized void drop( DropTargetDropEvent dtde ) {
    dropTable.getDropTargetListener().drop( dtde );
  }
}


