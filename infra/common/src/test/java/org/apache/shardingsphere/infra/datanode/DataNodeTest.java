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

package org.apache.shardingsphere.infra.datanode;

import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataNodeTest {
    
    @Test
    void assertNewValidDataNode() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds_0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
    }
    
    @Test
    void assertNewInValidDataNodeWithoutDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0tbl_0"));
    }
    
    @Test
    void assertNewInValidDataNodeWithTwoDelimiters() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0.db_0.tbl_0.tbl_1"));
    }
    
    @Test
    void assertNewValidDataNodeWithInvalidDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0,tbl_0"));
    }
    
    @Test
    void assertFormatWithSchema() {
        DataNode dataNode = new DataNode("foo_ds", "foo_tbl");
        dataNode.setSchemaName("foo_schema");
        assertThat(dataNode.format(), is("foo_ds.foo_schema.foo_tbl"));
    }
    
    @Test
    void assertFormatWithoutSchema() {
        DataNode dataNode = new DataNode("foo_ds", "foo_tbl");
        assertThat(dataNode.format(), is("foo_ds.foo_tbl"));
    }
    
    @SuppressWarnings({"SimplifiableAssertion", "ConstantValue"})
    @Test
    void assertEquals() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode, is(new DataNode("ds_0.tbl_0")));
        assertThat(dataNode, is(dataNode));
        assertThat(dataNode, not(new DataNode("ds_0.tbl_1")));
        assertFalse(dataNode.equals(null));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new DataNode("ds_0.tbl_0").hashCode(), is(new DataNode("ds_0.tbl_0").hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new DataNode("ds_0.tbl_0").toString(), is("DataNode(dataSourceName=ds_0, tableName=tbl_0, schemaName=null)"));
    }
    
    @Test
    void assertEmptyDataSourceDataNode() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode(".tbl_0"));
    }
    
    @Test
    void assertEmptyTableDataNode() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0."));
    }
    
    @Test
    void assertNewValidDataNodeIncludeInstance() {
        DataNode dataNode = new DataNode("ds_0.db_0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds_0.db_0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
    }
    
    @Test
    void assertHashCodeIncludeInstance() {
        assertThat(new DataNode("ds_0.db_0.tbl_0").hashCode(), is(new DataNode("ds_0.db_0.tbl_0").hashCode()));
    }
    
    @Test
    void assertToStringIncludeInstance() {
        assertThat(new DataNode("ds_0.db_0.tbl_0").toString(), is("DataNode(dataSourceName=ds_0.db_0, tableName=tbl_0, schemaName=null)"));
    }
}
