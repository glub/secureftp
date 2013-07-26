
/*
=====================================================================

  ColumnComparator.java
  
  Created by Claude Duguay
  Copyright (c) 2002
  
=====================================================================
*/

package com.glub.gui.table;

import java.util.*;

public class ColumnComparator
  implements Comparator
{
  protected int index;
  protected boolean ascending;
  
  public ColumnComparator(int index, boolean ascending)
  {
    this.index = index;
    this.ascending = ascending;
  }
  
  public int compare(Object one, Object two) 
  {
    if (one instanceof Vector &&
        two instanceof Vector)
    {
      Vector vOne = (Vector)one;
      Vector vTwo = (Vector)two;
      Object oOne = vOne.elementAt(index);
      Object oTwo = vTwo.elementAt(index);

      try {
        if (oOne instanceof Comparable &&
            oTwo instanceof Comparable)
        {
          Comparable cOne = (Comparable)oOne;
          Comparable cTwo = (Comparable)oTwo;
          if (ascending)
          {
            return cOne.compareTo(cTwo);
          }
          else
          {
            return cTwo.compareTo(cOne);
          }
	}
      }
      catch ( ClassCastException cse ) {
        return 0;
      }
    }
    return 1;
  }
}

