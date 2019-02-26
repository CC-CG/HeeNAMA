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
package org.apache.batik.css.parser;

/**
 * This class provides an implementation of the
 * {@link org.w3c.css.sac.AttributeCondition} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: DefaultClassCondition.java 1733416 2016-03-03 07:07:13Z gadams $
 */
public class DefaultClassCondition extends DefaultAttributeCondition {

    /**
     * Creates a new DefaultAttributeCondition object.
     */
    public DefaultClassCondition(String namespaceURI,
                                 String value) {
        super("class", namespaceURI, true, value);
    }
    
    /**
     * <b>SAC</b>: Implements {@link
     * org.w3c.css.sac.Condition#getConditionType()}.
     */    
    public short getConditionType() {
        return SAC_CLASS_CONDITION;
    }
    
    /**
     * Returns a text representation of this object.
     */
    public String toString() {
        return "." + getValue();
    }
}
