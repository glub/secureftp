
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteFileTable.java 154 2009-12-31 22:18:20Z gary $
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
import javax.swing.table.*;

public class RemoteFileTable extends JSortTable {
  protected static final long serialVersionUID = 1L;
  private RemotePanel remotePanel = null;

  public static final int FILE_COLUMN = 0;
  public static final int SIZE_COLUMN = 1;
  public static final int DATE_COLUMN = 2;
  public static final int OWNER_COLUMN = 3;
  public static final int PERMISSION_COLUMN = 4;

  public static final short INSECURE = 0;
  public static final short SECURE = 1;

  public static final Color INSECURE_COLOR = new Color( 255, 240, 240 );
  public static final Color SECURE_COLOR = new Color( 240, 240, 255 );

  private static final int NUMBER_OF_COLUMNS = 3;

  private TableDropTargetListener dropTargetListener;

  private MouseAdapter ma = null;

  private RemoteFilePopupMenu popupMenu = new RemoteFilePopupMenu(this);

  public RemoteFileTable( SortTableModel model, 
                          final RemotePanel remotePanel ) {
    super( model );
    setBackground( Color.white );
    this.remotePanel = remotePanel;

    //getTableHeader().setReorderingAllowed( false );

    TableColumnModel tcm = getColumnModel();

    TableColumn fileColumn = tcm.getColumn( FILE_COLUMN );
    TableCellRenderer fileRenderer = new FileCell();
    fileColumn.setCellRenderer( fileRenderer );

    TableColumn sizeColumn = tcm.getColumn( SIZE_COLUMN );
    TableCellRenderer sizeRenderer = new SizeCell();
    sizeColumn.setCellRenderer( sizeRenderer );

    TableColumn dateColumn = tcm.getColumn( DATE_COLUMN );
    TableCellRenderer dateRenderer = new DateCell();
    dateColumn.setCellRenderer( dateRenderer );

    if (tcm.getColumnCount() > OWNER_COLUMN) {
      TableColumn ownerColumn = tcm.getColumn( OWNER_COLUMN );
      TableCellRenderer ownerRenderer = new OwnerCell();
      ownerColumn.setCellRenderer( ownerRenderer );
    }

    if (tcm.getColumnCount() > PERMISSION_COLUMN) {
      TableColumn permColumn = tcm.getColumn( PERMISSION_COLUMN );
      TableCellRenderer permRenderer = new PermissionCell();
      permColumn.setCellRenderer( permRenderer );
    }

    RemoteFileTransferHandler fth = 
      new RemoteFileTransferHandler( remotePanel, this );
    setTransferHandler( fth );

    addKeyListener( new KeyAdapter() {
      public void keyPressed( KeyEvent e ) {
        if ( KeyEvent.VK_BACK_SPACE == e.getKeyCode() ) {
          final RemoteFile remoteFile = new RemoteFile();
          remoteFile.setFileName( ".." );
          Thread cdThread = new Thread() {
            public void run() {
              remotePanel.changeDirectory( remoteFile, false );
            }
          };
          cdThread.start();
        }
        else if ( KeyEvent.VK_F4 == e.getKeyCode() && 
        KeyEvent.getKeyModifiersText(e.getModifiers()).toLowerCase().equals("ctrl") ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
                                                        new CloseCommand() );
        }
        else if ( KeyEvent.VK_F5 == e.getKeyCode() ) {
          SecureFTP.getCommandDispatcher().fireCommand( this, 
                                                        new LsCommand() );
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

            RemoteFile file = (RemoteFile)getValueAt( val, getFileColumnIndex() );

            if ( null != file ) {
              char firstChar = file.getFileName().toLowerCase().charAt(0);

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

    final int keyMask = ( Util.isMacOS() ) ? Event.META_MASK : Event.CTRL_MASK;

    this.addKeyListener( new KeyListener() {
      public void keyReleased( KeyEvent ke ) {}
      public void keyPressed( KeyEvent ke ) {
	if ( KeyStroke.getKeyStrokeForEvent(ke) == KeyStroke.getKeyStroke(KeyEvent.VK_L, keyMask) ) {
          remotePanel.selectComboBoxText();
        }
      }
      public void keyTyped( KeyEvent ke ) {}
    } );
  }

  public void setTableColor( short level ) {
    Color color = Color.white;

    switch ( level ) {
      case SECURE:
        color = SECURE_COLOR;
        break;

      case INSECURE:
        color = INSECURE_COLOR;
        break;

      default:
        color = Color.WHITE;
    }

    setBackground( color );
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
        Thread switchToLocal = new Thread() {
          public void run() {
            Client.getLocalView().setFocus();
          }
        };
        switchToLocal.start();
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

    KeyStroke shiftDown = 
      KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK);

    final Object shiftDownKeyBinding = 
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(shiftDown);

    final Action shiftDownAction = getActionMap().get(shiftDownKeyBinding);

    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
               shiftDown, "next-cell-sel");

    getActionMap().put("next-cell-sel", new AbstractAction() {
   	  protected static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent e) {
        if ( null != shiftDownAction )
          shiftDownAction.actionPerformed(e);
        Client.getToolBar().updateToolBar();
        Client.getMenus().updateMenuBar();
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
          Client.getMenus().updateMenuBar(); 
        }
      }
    } );

    KeyStroke shiftUp = 
      KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK);

    final Object shiftUpKeyBinding = 
      getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(shiftUp);

    final Action shiftUpAction = getActionMap().get(shiftUpKeyBinding);

    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
               shiftUp, "prev-cell-sel");

    getActionMap().put("prev-cell-sel", new AbstractAction() {
     protected static final long serialVersionUID = 1L;
     public void actionPerformed(ActionEvent e) {
        if ( null != shiftUpAction )
          shiftUpAction.actionPerformed(e);
        Client.getToolBar().updateToolBar();
        Client.getMenus().updateMenuBar();
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

  public int selectFile( RemoteFile file ) {
    return selectFile( file, false );
  }

  public int selectFile( RemoteFile file, boolean additive ) {
    int row = -1;
    RemoteFileTableModel model = (RemoteFileTableModel)getModel();

    int totalRows = model.getRowCount() ;
    int column = getFileColumnIndex();
    //for ( int i = 1; i < totalRows; i++ ) {
    for ( int i = 0; i < totalRows; i++ ) {
      RemoteFile curFile = (RemoteFile)model.getValueAt(i, column);
      if ( curFile.getFileName().equals(file.getFileName()) ) {
        if ( additive ) {
          addRowSelectionInterval( i, i );
        }
        else {
          changeSelection( i, column, false, false );
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
    clearSelection();
    int lastRow = -1;
    for ( int i = 0; i < files.size(); i++ ) {
      RemoteFile file = (RemoteFile)files.get(i);
      lastRow = selectFile( file, files.size() > 1 );
    }

    if ( lastRow >= 0 && files.size() > 1 && getAutoscrolls() ) {
      Rectangle cellRect = getCellRect( lastRow, 0, false );
      if ( cellRect != null ) {
        scrollRectToVisible( cellRect );
      }
    }
  }

  public void handleDoubleClick() {
    int row = getSelectedRow();

    RemoteFile remoteFile = null;

    if ( row >= 0 ) {
      remoteFile = (RemoteFile)getValueAt( row, getFileColumnIndex() );
    }

    //boolean selectedIsFile = true;

    // if this is a directory, or might be a directory, try changing into it
    if ( null != remoteFile && 
         (remoteFile.isDirectory() || remoteFile.isLink() ||
          !remoteFile.isKnownFileType() ) ) {
      boolean suppressErrors = !remoteFile.getFileName().equals(".."); 

      SecureFTPError error = 
        remotePanel.changeDirectory(remoteFile, suppressErrors);

      int resultCode = error.getCode();

/*
      selectedIsFile = SecureFTPError.NOT_A_DIRECTORY == resultCode ||
                       SecureFTPError.UNKNOWN == resultCode;
*/
      
      if ( suppressErrors && SecureFTPError.PERMISSION_DENIED == resultCode ) {
        LString lstr = LString.getLocalizedString( error.getMessage() );
        ErrorDialog.showDialog( lstr );
        return;
      }
    }

    RemoteFileList selectedFiles = getSelectedFiles();

    if ( selectedFiles.size() > 0 ) {
      remotePanel.download( selectedFiles );
    }
  }

  public RemoteFileList getAllFiles() {
    RemoteFileList rfl = new RemoteFileList();

    //for ( int i = 1; i < getRowCount(); i++ ) {
    for ( int i = 0; i < getRowCount(); i++ ) {
      rfl.add( (RemoteFile)getValueAt(i, getFileColumnIndex()) );
    }

    return rfl;
  }

  public RemoteFileList getSelectedFiles() {
    int[] rows = getSelectedRows();

    RemoteFileList selectedFiles = new RemoteFileList();

    for( int i = 0; i < rows.length; i++ ) {
      RemoteFile currentFile = 
        (RemoteFile)getValueAt( rows[i], getFileColumnIndex() );

/*
      boolean fileFromLimitedListing =
        !currentFile.isKnownFileType() && currentFile.getFileSize() == -1;
*/
      boolean fileFromLimitedListing = !currentFile.isKnownFileType();

      boolean selectedIsFile = 
        (currentFile.isKnownFileType() && 
         currentFile.isFile()) || currentFile.isDirectory() ||
         currentFile.isLink() || fileFromLimitedListing;

      if ( selectedIsFile && !currentFile.getFileName().equals("..") ) {
        FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        currentFile.setMetaData( "pwd", session.getWorkingDir() );
        selectedFiles.add( currentFile );
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
    Client.getRemoteView().setSort( sortedColumnIndex, 
                                         sortedColumnAscending );
  }

  public void cueUIDrop( boolean cue ) {
    if ( cue ) {
      remotePanel.getScrollPane().setViewportBorder( 
        BorderFactory.createLineBorder(SystemColor.windowBorder, 3) );
    }
    else {
      remotePanel.getScrollPane().setViewportBorder( 
        BorderFactory.createLineBorder(Color.white, 0) );
      //clearSelection();
    }

    remotePanel.getScrollPane().updateUI();
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
      int col = table.columnAtPoint( p );

      RemoteFile remoteFile = null;
	
      int fileColumnIndex = getFileColumnIndex();

      if ( row >= 0 && fileColumnIndex == col ) {
        remoteFile = (RemoteFile)getValueAt( row, fileColumnIndex );
      }
	  
      //if ( null != remoteFile && remoteFile.isDirectory() && row > 0 ) {
      if ( null != remoteFile && remoteFile.isDirectory() && row >= 0 ) {
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

      if ( tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && 
           canImport ) {
        dtde.acceptDrop( DnDConstants.ACTION_COPY );

        int row = getSelectedRow();

        RemoteFile remoteFile = new RemoteFile(".");
	
        if ( row >= 0 ) {
          remoteFile = (RemoteFile)getValueAt( row, getFileColumnIndex() );
        }

        if ( !remoteFile.isDirectory() || 
             remoteFile.getFileName().equals("..") ) {
          remoteFile = new RemoteFile(".");
        }

        FTPSession session = 
          FTPSessionManager.getInstance().getCurrentSession();
        remoteFile.setMetaData( "pwd", session.getWorkingDir() );

        try {
          java.util.List fileList = 
           (java.util.List)tr.getTransferData( DataFlavor.javaFileListFlavor );

          if ( remoteFile.getFileName().equals(".") ) {
            remoteFile = null;
          }

          remotePanel.upload( fileList, remoteFile );
        }
        catch ( IOException ioe ) {
        }
        catch ( UnsupportedFlavorException ufe ) {
        }

        cueUIDrop( false );
        clearSelection();
      }
      else {
        dtde.rejectDrop();
      }

      cleanup();
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
   * RemoteFileTransferHandler class
   */
  class RemoteFileTransferHandler extends TransferHandler {
  	protected static final long serialVersionUID = 1L;
    public Transferable transferable;
    private RemotePanel remotePanel;
    private JTable table;

    public RemoteFileTransferHandler() {
      super();
    }

    public RemoteFileTransferHandler( RemotePanel remotePanel, JTable table ) {
      super();
      this.remotePanel = remotePanel;
      this.table = table;
    }

    public Transferable createTransferable( JComponent comp ) {
      transferable = new RemoteFileTransfer(remotePanel, table);
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
      boolean canImport = !name.equals("RemoteFileList");

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

        RemoteFile remoteFile = 
          //remotePanel.getCurrentListing().getFile( row + 1 );
          remotePanel.getCurrentListing().getFile( row );

        if ( !remoteFile.isDirectory() || 
             remoteFile.getFileName().equals("..") ) {
          remoteFile = remotePanel.getCurrentListing().getFile( 0 );
        }

        try {
          java.util.List fileList = 
           (java.util.List)tr.getTransferData( DataFlavor.javaFileListFlavor );

	        remotePanel.upload( fileList, remoteFile );
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
    private Icon dirIcon = UIManager.getIcon("FileView.directoryIcon" );

    private Icon fileIcon = UIManager.getIcon("FileView.fileIcon" );

    //private Icon linkIcon = UIManager.getIcon("FileView.fileIcon" );
    private ImageIcon linkIcon = new ImageIcon( getClass().getResource("images/link_generic.png") );
    private ImageIcon macLinkIcon = new ImageIcon( getClass().getResource("images/link_mac.png") );
    private ImageIcon winLinkIcon = new ImageIcon( getClass().getResource("images/link_windows.png") );

    protected void setValue( Object value ) {
      RemoteFile file = (RemoteFile)value;

      FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

      if ( null != file ) {
        if ( file.isDirectory() ) {
          setIcon( dirIcon );
	}
        else if ( file.isLink() ) {
          if ( Util.isMacOS() ) {
            setIcon( macLinkIcon );
          }
          else if ( Util.isWinOS() ) {
            setIcon( winLinkIcon );
          }
          else {
            setIcon( linkIcon );
          }
        }
        else {
          setIcon( fileIcon );
        }

        String fileName = file.getFileName();
        //setText( " " + fileName + " " );
        setText( fileName + " " );
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

  class SizeCell extends DefaultTableCellRenderer {
  	protected static final long serialVersionUID = 1L;
    protected void setValue( Object value ) {
      if ( value instanceof Long ) {
        Long size = (Long)value;
        double kb = size.doubleValue() / 1024;
        double mb = kb / 1024;
        double gb = mb / 1024;

        String bytesStr = LString.getString("Common.xferMetric.bytes", "bytes");
        String kbStr = LString.getString("Common.xferMetric.kb", "KB");
        String mbStr = LString.getString("Common.xferMetric.mb", "MB");
        String gbStr = LString.getString("Common.xferMetric.gb", "GB");

        if ( size.doubleValue() < 0 ) {
          setText( "-" );
        }
        else if ( kb >= 1 && mb < 1 ) {
          setText( " " + format(kb, 2) + " " + kbStr + " " );
        }
        else if ( mb >= 1 && gb < 1 ) {
          setText( " " + format(mb, 2) + " " + mbStr + " " );
        }
        else if ( gb >= 1 ) {
          setText( " " + format(gb, 2) + " " + gbStr + " " );
        }
        else {
          setText( " " + format(size.doubleValue(), 2) + " " + bytesStr + " " );
        }
      }
      else {
        setText( "" );
      }
    }

    public String format( double val, int decimals ) {
      DecimalFormat df = new DecimalFormat("#.##");
      df.setGroupingUsed( true );
      df.setMaximumFractionDigits( decimals );
      String result = df.format( val );
      return result;
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

      // right justify the size
      ((JLabel)c).setHorizontalAlignment( SwingConstants.RIGHT );

      return c;
    }
  }

  class DateCell extends DefaultTableCellRenderer {
  	protected static final long serialVersionUID = 1L;
    private DateFormat timeDateFormat = 
      new SimpleDateFormat(LString.getString("Common.timeDateFormat", 
                                             "M/d/yyyy h:mm a") );
    
    private DateFormat dateFormat = 
      new SimpleDateFormat(LString.getString("Common.dateFormat", 
                                             "M/d/yyyy") );

    protected void setValue( Object value ) {
      if ( value instanceof Calendar ) {
        Calendar cal = (Calendar)value;

        if ( null != cal ) {
          if ( 0 == cal.get(Calendar.HOUR) && 0 == cal.get(Calendar.MINUTE) &&
               0 == cal.get(Calendar.SECOND) ) {
            setText( " " + dateFormat.format(cal.getTime()) + " " );
          }
	  else {
            setText( " " + timeDateFormat.format(cal.getTime()) + " " );
          }
        }
      }
      else {
        setText( "" );
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

  class OwnerCell extends DefaultTableCellRenderer {
  	protected static final long serialVersionUID = 1L;
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

  class PermissionCell extends DefaultTableCellRenderer {
  	protected static final long serialVersionUID = 1L;
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

    RemoteFile file = (RemoteFile)getValueAt( row, getFileColumnIndex() );

    String fileName = file.getFileName();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    String pwd = session.getWorkingDir();
    if (pwd != null)
      fileName = pwd + fileName;

    buffer.append( "<b>" + fileName + "</b><br>");

    double size = file.getFileSize();
    double kb = size / 1024;
    double mb = kb / 1024;
    double gb = mb / 1024;

    if (size >= 0) {

      DecimalFormat df = new DecimalFormat("#.##");
      df.setGroupingUsed( true );
      df.setMaximumFractionDigits( 2 );

      String bytesStr = LString.getString("Common.xferMetric.bytes", "bytes");
      String kbStr = LString.getString("Common.xferMetric.kb", "KB");
      String mbStr = LString.getString("Common.xferMetric.mb", "MB");
      String gbStr = LString.getString("Common.xferMetric.gb", "GB");

      if ( size < 0 ) {
        buffer.append( "- " + bytesStr + " <br>" );
      }
      else if ( kb >= 1 && mb < 1 ) {
        buffer.append( df.format(kb) + " " + kbStr + " <br>" );
      }
      else if ( mb >= 1 && gb < 1 ) {
        buffer.append( df.format(mb) + " " + mbStr + " <br>" );
      }
      else if ( gb >= 1 ) {
        buffer.append( df.format(gb) + " " + gbStr + " <br>" );
      }
      else {
        buffer.append( df.format(file.getFileSize()) + " " + bytesStr + " <br>" );
      }
    }

    boolean printedUser = false;

    if (file.getUser() != null) {
      printedUser = true;
      String ownership = file.getUser();

      if (file.getGroup() != null)
        ownership += ":" + file.getGroup(); 

      buffer.append( ownership );
    }

    boolean printedPerms = false;

    if (file.getPermissions() != null) {
      printedPerms = true;
      if ( printedUser )
        buffer.append( "; ");
      buffer.append( file.getPermissions() + "<br>" );
    }

    if (printedUser && !printedPerms)
      buffer.append("<br>");

    if (file.getDate() != null) {
      DateFormat timeDateFormat =
        new SimpleDateFormat(LString.getString("Common.timeDateFormat",
                                               "M/d/yyyy h:mm a") );

      buffer.append( timeDateFormat.format(file.getDate().getTime()) );
    }
 
    buffer.append( "</td></tr></table></html>" );

    return buffer.toString();
  }
}

class RemoteFileTableViewport extends JViewport implements DropTargetListener {
  protected static final long serialVersionUID = 1L;
  protected DropTarget dropTarget = new DropTarget( this, this );
  private final RemoteFileTable dropTable;

  public RemoteFileTableViewport( RemoteFileTable dt ) {
    super();
    dropTable = dt;

    addMouseListener( new MouseAdapter() {
      public void mouseClicked( MouseEvent e ) {
        dropTable.clearSelection();

        Runnable r = new Runnable() {
          public void run() {
            dropTable.requestFocus();
          }
        };

        try {
          SwingUtilities.invokeLater(r);
        }
        catch ( Exception e1 ) {}

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
