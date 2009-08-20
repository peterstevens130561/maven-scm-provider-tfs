package org.apache.maven.scm.provider.tfs.command;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.TfsChangeLogConsumer;

public class TfsChangeLogCommand
    extends AbstractChangeLogCommand
{

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository r, ScmFileSet f, Date startDate,
                                                          Date endDate, ScmBranch branch, String datePattern )
        throws ScmException
    {
        ArrayList changeLogs = new ArrayList();
        Iterator iter = f.getFileList().iterator();
        if ( !iter.hasNext() )
        {
            ArrayList dir = new ArrayList();
            // No files to iterate
            dir.add( f.getBasedir() );
            iter = dir.iterator();
        }
        TfsCommand command = null;
        // tf history takes only one file arg
        while ( iter.hasNext() )
        {
            TfsChangeLogConsumer out = new TfsChangeLogConsumer( getLogger() );
            ErrorStreamConsumer err = new ErrorStreamConsumer();

            command = createCommand( r, f, ( (File) iter.next() ) );
            int status = command.execute( out, err );

            if ( status != 0 || ( !out.hasBeenFed() && err.hasBeenFed() ) )
                return new ChangeLogScmResult( command.getCommandString(), "Error code for TFS changelog command - "
                    + status, err.getOutput(), false );
            changeLogs.addAll( out.getLogs() );
        }
        return new ChangeLogScmResult( command.getCommandString(), new ChangeLogSet( changeLogs, startDate, endDate ) );

    }

    protected TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, File file )
    {
        TfsCommand command = new TfsCommand( "history", r, f, getLogger() );
        command.addArgument( "-format:detailed" );
        command.addArgument( file.getName() );
        return command;
    }
}

