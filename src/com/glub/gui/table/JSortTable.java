/*
=====================================================================

  JSortTable.java
  
  Created by Claude Duguay
  Copyright (c) 2002
  
=====================================================================
*/

package com.glub.gui.table;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class JSortTable extends JTable
  implements MouseListener
{
  protected static final long serialVersionUID = 1L;	
  protected int sortedColumnIndex = -1;
  protected boolean sortedColumnAscending = true;
  
  public JSortTable()
  {
    this(new DefaultSortTableModel());
  }
  
  public JSortTable(int rows, int cols)
  {
    this(new DefaultSortTableModel(rows, cols));
  }
  
  public JSortTable(Object[][] data, Object[] names)
  {
    this(new DefaultSortTableModel(data, names));
  }
  
  public JSortTable(Vector data, Vector names)
  {
    this(new DefaultSortTableModel(data, names));
  }
  
  public JSortTable(SortTableModel model)
  {
    super(model);
    initSortHeader();
  }

  public JSortTable(SortTableModel model,
    TableColumnModel colModel)
  {
    super(model, colModel);
    initSortHeader();
  }

  public JSortTable(SortTableModel model,
    TableColumnModel colModel,
    ListSelectionModel selModel)
  {
    super(model, colModel, selModel);
    initSortHeader();
  }

  /*
  public boolean getScrollableTracksViewportHeight() {
    if ( getParent() instanceof JViewport ) {
     return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
    }

    return false;
  }
  */

  protected void initSortHeader()
  {
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new SortHeaderRenderer());
    header.addMouseListener(this);
  }

  public int getSortedColumnIndex()
  {
    return sortedColumnIndex;
  }
  
  public boolean isSortedColumnAscending()
  {
    return sortedColumnAscending;
  }
  
  public void mouseReleased(MouseEvent event)
  {
    TableColumnModel colModel = getColumnModel();
    int index = colModel.getColumnIndexAtX(event.getX());
    int modelIndex = colModel.getColumn(index).getModelIndex();
    
    SortTableModel model = (SortTableModel)getModel();
    if (model.isSortable(modelIndex))
    {
      // set to false, because it will get flipped lower down
      if ( sortedColumnIndex != modelIndex ) {
        sortedColumnAscending = false;
      }

      boolean forceSelection = false;
      ArrayList itemsToSelect = new ArrayList();
      // toggle ascension, if already sorted
      if (0 == sortedColumnIndex && sortedColumnIndex == index)
      {
        forceSelection = false;
        sortedColumnAscending = !sortedColumnAscending;
	int[] selectedRows = getSelectedRows();
        int rowCount = getRowCount();
	clearSelection();
	for ( int i = 0; i < selectedRows.length; i++ ) {
          int selectedRow =  rowCount - selectedRows[i];
          if ( selectedRow == rowCount ) {
            addRowSelectionInterval( 0, 0 ); 
          }
          else {
            addRowSelectionInterval( selectedRow, selectedRow ); 
          }
        }
      }
      else {
        forceSelection = true;
        sortedColumnAscending = !sortedColumnAscending;
	int[] selectedRows = getSelectedRows();
        int rowCount = getRowCount();
	clearSelection();
	for ( int i = 0; i < selectedRows.length; i++ ) {
          String selectedRowItem = getValueAt(selectedRows[i], 0).toString();
          int selectedRow =  rowCount - selectedRows[i];

          if ( selectedRow == rowCount ) {
            addRowSelectionInterval( 0, 0 ); 
          }
          else {
            itemsToSelect.add( selectedRowItem );
          }
        }
        //sortedColumnAscending = true;
      }

      model.sortColumn(modelIndex, sortedColumnAscending);

      // if we are switching columns we need to hunt for the old selection
      if (forceSelection || sortedColumnIndex != index) {
        int rowCount = getRowCount();
        for ( int j = 0; j < rowCount; j++ ) {
          for ( int k = 0; k < itemsToSelect.size(); k++ ) {
            String itemToSelect = itemsToSelect.get(k).toString();
            if ( itemToSelect.equals(getValueAt(j, 0).toString()) ) {
              addRowSelectionInterval( j, j ); 
            }
          }
        }
      }

      sortedColumnIndex = index;
    }
  }
  
  public void mousePressed(MouseEvent event) {}
  public void mouseClicked(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
}

