
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalFileTableModel.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.table.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class LocalFileTableModel extends DefaultSortTableModel {
  protected static final long serialVersionUID = 1L;
  public LocalFileTableModel() { super(); }

  public LocalFileTableModel(Vector data, Vector names) {
    super(data, names);
  }
  
  public void sortColumn( int col, boolean ascending ) {
    Collections.sort( getDataVector(), 
                      new LocalFileComparator(col, ascending) );
  }

  // for some reason out of our control, this exception gets thrown
  public Object getValueAt( int row, int column ) {
    Object o = null;
    try {
      o = super.getValueAt( row, column );
    }
    catch ( ArrayIndexOutOfBoundsException aiobe ) {}
    return o;
  }

  public boolean isCellEditable( int rowIndex, int colIndex ) {
    return false;
  }
}

class LocalFileComparator extends ColumnComparator {
  public LocalFileComparator( int index, boolean ascending ) {
    super( index, ascending );
  }

  public int compare( Object one, Object two ) {
    if ( one instanceof Vector && two instanceof Vector ) {
      Vector vOne = (Vector)one;
      Vector vTwo = (Vector)two;

      if ( index < 0 ) { return -1; }

      Object oOne = vOne.elementAt(index);
      Object oTwo = vTwo.elementAt(index);

      if ( oOne.toString().equals("") ) {
        return 0;
      }

      if ( oOne instanceof File && oTwo instanceof File ) {
        File fOne = (File)oOne;
        File fTwo = (File)oTwo;

        if ( fOne.getName().equals("..") ) {
          return 0;
        }

        String ftOne = fOne.getName();
        String ftTwo = fTwo.getName();

        if ( fOne.isDirectory() ) {
          if ( Util.isPackage(fOne) ) {
            ftOne = "E" + fOne;
          }
          else {
            ftOne = "D" + fOne;
          }
        }
        else {
          ftOne = "F" + fOne;
        }

        if ( fTwo.isDirectory() ) {
          if ( Util.isPackage(fTwo) ) {
            ftTwo = "E" + fTwo;
          }
          else {
            ftTwo = "D" + fTwo;
          }
        }
        else {
          ftTwo = "F" + fTwo;
        }

        if ( ascending ) {
          return ftOne.compareTo( ftTwo );
        }
        else {
          return ftTwo.compareTo( ftOne );
        }
      }
      else if ( oOne instanceof Calendar && oTwo instanceof Calendar ) {
        Calendar cOne = (Calendar)oOne;
        Calendar cTwo = (Calendar)oTwo;
        Date dOne = cOne.getTime();
        Date dTwo = cTwo.getTime();

        if ( ascending ) {
          return dOne.compareTo( dTwo );
        }
        else {
          return dTwo.compareTo( dOne );
        }
      }
      else {
        try {
          if (oOne instanceof Comparable && oTwo instanceof Comparable) {

            Comparable cOne = (Comparable)oOne;
            Comparable cTwo = (Comparable)oTwo;

            if (ascending) {
              return cOne.compareTo(cTwo);
            }
            else {
              return cTwo.compareTo(cOne);
            }
          }
        }
        catch ( ClassCastException cse ) {
          return 0;
        }
      }
    }
    return 1;
  }
}
