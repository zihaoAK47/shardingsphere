/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XAEndStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLCacheIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLInstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLLoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLRepairTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLResetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowBinaryLogsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowBinlogEventsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateEventStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowErrorsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLShowWarningsStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLAlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLDropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRenameUserStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dcl.MySQLRevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLDropViewStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLRenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.ddl.MySQLTruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLCallStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLDoStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.rl.MySQLChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.rl.MySQLStartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.rl.MySQLStopSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.tcl.MySQLCommitStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLComStmtPrepareCheckerTest {
    
    @Test
    void assertIsStatementAllowed() {
        MySQLCreateTableStatement createTableStatement = new MySQLCreateTableStatement();
        Collection<SQLStatement> sqlStatements = Arrays.asList(
                new MySQLAlterTableStatement(), new MySQLAlterUserStatement(), new MySQLAnalyzeTableStatement(), new MySQLCacheIndexStatement(),
                new MySQLCallStatement(), new MySQLChangeMasterStatement(), new MySQLChecksumTableStatement(), new MySQLCommitStatement(), new MySQLCreateIndexStatement(),
                new MySQLDropIndexStatement(), new MySQLCreateDatabaseStatement(), new MySQLDropDatabaseStatement(), createTableStatement,
                new MySQLDropTableStatement(), new MySQLCreateUserStatement(), new MySQLRenameUserStatement(), new MySQLDropUserStatement(),
                new MySQLCreateViewStatement(), new MySQLDropViewStatement(), new MySQLDeleteStatement(), new MySQLDoStatement(), new MySQLFlushStatement(),
                new MySQLGrantStatement(), new MySQLInsertStatement(), new MySQLInstallPluginStatement(), new MySQLKillStatement(),
                new MySQLLoadIndexInfoStatement(), new MySQLOptimizeTableStatement(), new MySQLRenameTableStatement(), new MySQLRepairTableStatement(),
                new MySQLResetStatement(), new MySQLRevokeStatement(), new MySQLSelectStatement(), new MySQLSetStatement(), new MySQLShowWarningsStatement(),
                new MySQLShowErrorsStatement(), new MySQLShowBinlogEventsStatement(), new MySQLShowCreateProcedureStatement(), new MySQLShowCreateFunctionStatement(),
                new MySQLShowCreateEventStatement(),
                new MySQLShowCreateTableStatement(), new MySQLShowCreateViewStatement(), new MySQLShowBinaryLogsStatement(), new MySQLShowStatusStatement(), new MySQLStartSlaveStatement(),
                new MySQLStopSlaveStatement(), new MySQLTruncateStatement(), new MySQLUninstallPluginStatement(), new MySQLUpdateStatement(),
                new XABeginStatement("1"), new XAPrepareStatement("1"), new XACommitStatement("1"), new XARollbackStatement("1"), new XAEndStatement("1"), new XARecoveryStatement());
        for (SQLStatement each : sqlStatements) {
            assertTrue(MySQLComStmtPrepareChecker.isAllowedStatement(each));
        }
    }
}
