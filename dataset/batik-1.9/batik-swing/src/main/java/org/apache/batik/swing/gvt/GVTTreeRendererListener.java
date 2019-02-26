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
package org.apache.batik.swing.gvt;

/**
 * This interface represents a listener to the GVTTreeRendererEvent events.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: GVTTreeRendererListener.java 1733416 2016-03-03 07:07:13Z gadams $
 */
public interface GVTTreeRendererListener {

    /**
     * Called when a rendering is in its preparing phase.
     */
    void gvtRenderingPrepare(GVTTreeRendererEvent e);

    /**
     * Called when a rendering started.
     */
    void gvtRenderingStarted(GVTTreeRendererEvent e);

    /**
     * Called when a rendering was completed.
     */
    void gvtRenderingCompleted(GVTTreeRendererEvent e);

    /**
     * Called when a rendering was cancelled.
     */
    void gvtRenderingCancelled(GVTTreeRendererEvent e);

    /**
     * Called when a rendering failed.
     */
    void gvtRenderingFailed(GVTTreeRendererEvent e);

}
