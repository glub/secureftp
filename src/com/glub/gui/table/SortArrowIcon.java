/*
=====================================================================

  SortArrowIcon.java
  
  Created by Claude Duguay
  Copyright (c) 2002
  
=====================================================================
*/

package com.glub.gui.table;

import java.awt.*;
import javax.swing.*;

import com.glub.util.*;

public class SortArrowIcon
  implements Icon
{
  public static final int NONE = 0;
  public static final int DECENDING = 1;
  public static final int ASCENDING = 2;

  protected int direction;
  protected int width = 13;
  protected int height = 13;
  
  public SortArrowIcon(int direction)
  {
    this.direction = direction;
  }
  
  public int getIconWidth()
  {
    return width;
  }
  
  public int getIconHeight()
  {
    return height;
  }
  
  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    Polygon p = new Polygon();
    g.setColor( Color.gray );

    int w = width - 1;
    int h = height - 2;
    int m = w / 2;

    if (direction == ASCENDING) {
      if ( Util.isSunOS() ) {
        h += 4;
      }

      p.addPoint(x, h);
      p.addPoint(x + w - 2, h);
      p.addPoint(x + m - 1, y + 4);
      g.fillPolygon(p);
      g.drawPolygon(p);
    }

    else if (direction == DECENDING) {
      p.addPoint(x, h - 5);
      p.addPoint(x + w - 2, h - 5);
      p.addPoint(x + m - 1, h);
      g.fillPolygon(p);
      g.drawPolygon(p);
    }
  }
}

