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

import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This interface represents an object which interacts with a GVT component.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: Interactor.java 1733416 2016-03-03 07:07:13Z gadams $
 */
public interface Interactor
    extends KeyListener,
            MouseListener,
            MouseMotionListener {

    /**
     * Tells whether the given event will start the interactor.
     */
    boolean startInteraction(InputEvent ie);

    /**
     * Tells whether the interaction has finished.
     */
    boolean endInteraction();
}
