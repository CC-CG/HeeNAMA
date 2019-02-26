/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * This implementation of RenderedImage only serves to put the tiles
 * generated by it's input into the TileCache.
 *
 * @author <a href="mailto:thomas.deweese@kodak.com">Thomas DeWeese</a>
 * @version $Id: TileCacheRed.java 1733416 2016-03-03 07:07:13Z gadams $
 */
public class TileCacheRed extends AbstractTiledRed {

    /**
     * Place the results of computations of cr into the global tile cache.
     * @param cr The operation to cache results from.
     */
    public TileCacheRed(CachableRed cr) {
        super(cr, null);
    }

    /**
     * Place the results of computations of cr into the global tile cache.
     * @param cr The operation to cache results from.
     */
    public TileCacheRed(CachableRed cr, int tileWidth, int tileHeight) {
        super();
        ColorModel  cm = cr.getColorModel();
        Rectangle bounds = cr.getBounds();
        if (tileWidth  > bounds.width)  tileWidth  = bounds.width;
        if (tileHeight > bounds.height) tileHeight = bounds.height;
        SampleModel sm = cm.createCompatibleSampleModel(tileWidth, tileHeight);
        init(cr, bounds, cm, sm, 
             cr.getTileGridXOffset(), cr.getTileGridYOffset(),
             null);
    }

    public void genRect(WritableRaster wr) {
        // Get my source.
        CachableRed src = (CachableRed)getSources().get(0);

        src.copyData(wr);
    }

    public void flushCache(Rectangle rect) {
        int tx0 = getXTile(rect.x);
        int ty0 = getYTile(rect.y);
        int tx1 = getXTile(rect.x+rect.width -1);
        int ty1 = getYTile(rect.y+rect.height-1);

        if (tx0 < minTileX) tx0 = minTileX;
        if (ty0 < minTileY) ty0 = minTileY;

        if (tx1 >= minTileX+numXTiles) tx1 = minTileX+numXTiles-1;
        if (ty1 >= minTileY+numYTiles) ty1 = minTileY+numYTiles-1;

        if ((tx1 < tx0) || (ty1 < ty0))
            return;

        TileStore store = getTileStore();
        for (int y=ty0; y<=ty1; y++)
            for (int x=tx0; x<=tx1; x++)
                store.setTile(x, y, null);
    }
}
