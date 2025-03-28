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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTimestampBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private MySQLBinlogColumnDef columnDef;
    
    @Test
    void assertRead() {
        int currentSeconds = (int) (System.currentTimeMillis() / 1000L);
        when(payload.readInt4()).thenReturn(currentSeconds);
        assertThat(new MySQLTimestampBinlogProtocolValue().read(columnDef, payload),
                is(DateTimeFormatterFactory.getStandardFormatter().format(new Timestamp(currentSeconds * 1000L).toLocalDateTime())));
    }
    
    @Test
    void assertReadNullTime() {
        when(payload.readInt4()).thenReturn(0);
        assertThat(new MySQLTimestampBinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.DATETIME_OF_ZERO));
    }
}
