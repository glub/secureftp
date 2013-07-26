/*
=====================================================================

  SortHeaderRenderer.java
  
  Created by Claude Duguay
  Copyright (c) 2002
  
=====================================================================
*/

package com.glub.gui.table;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class SortHeaderRenderer extends DefaultTableCellRenderer {
  protected static final long serialVersionUID = 1L;
	
  public Icon NONSORTED = new SortArrowIcon(SortArrowIcon.NONE);
  public Icon ASCENDING = getArrowIcon(SortArrowIcon.ASCENDING);
  public Icon DECENDING = getArrowIcon(SortArrowIcon.DECENDING);
  
  public SortHeaderRenderer() {
    setHorizontalTextPosition(LEFT);
    setHorizontalAlignment(CENTER);
  }

  private Icon getArrowIcon( int direction ) {
    Icon result = null;
    Class c = getClass();

    switch ( direction ) {
      case SortArrowIcon.DECENDING: 
          result = new ImageIcon(c.getResource("images/arrowDown.png"));
        break;

      case SortArrowIcon.ASCENDING:
      default:
          result = new ImageIcon(c.getResource("images/arrowUp.png"));
        break;
    }

    return result;
  }
  
  public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected,
    boolean hasFocus, int row, int col)
  {
    int index = -1;
    boolean ascending = true;
    if (table instanceof JSortTable)
    {
      JSortTable sortTable = (JSortTable)table;
      index = sortTable.getSortedColumnIndex();
      ascending = sortTable.isSortedColumnAscending();
    }
    if (table != null)
    {
      JTableHeader header = table.getTableHeader();
      if (header != null)
      {
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
      }
    }
    Icon icon = ascending ? ASCENDING : DECENDING;
    setIcon(col == index ? icon : NONSORTED);
    setText((value == null) ? "" : value.toString());
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    return this;
  }
} 
