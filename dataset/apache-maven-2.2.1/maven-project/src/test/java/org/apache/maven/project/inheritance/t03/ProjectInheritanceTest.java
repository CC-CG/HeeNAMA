package org.apache.maven.project.inheritance.t03;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * A test which demonstrates maven's recursive inheritance where
 * a distinct value is taken from each parent contributing to the
 * the final model of the project being assembled. There is no
 * overriding going on amongst the models being used in this test:
 * each model in the lineage is providing a value that is not present
 * anywhere else in the lineage. We are just making sure that values
 * down in the lineage are bubbling up where they should.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: ProjectInheritanceTest.java 640549 2008-03-24 20:05:11Z bentmann $
 */
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    // ----------------------------------------------------------------------
    //
    // p1 inherits from p0
    // p0 inhertis from super model
    //
    // or we can show it graphically as:
    //
    // p1 ---> p0 --> super model
    //
    // ----------------------------------------------------------------------

    public void testProjectInheritance()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        
        File pom0Basedir = pom0.getParentFile();
        
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        
        // load everything...
        MavenProject project0 = getProject( pom0 );
        MavenProject project1 = getProject( pom1 );

        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
    }
}
