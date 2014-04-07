/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.db.resolver.param;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.module.db.domain.connection.DbConnection;
import org.mule.module.db.domain.param.DefaultInputQueryParam;
import org.mule.module.db.domain.query.QueryTemplate;
import org.mule.module.db.domain.query.QueryType;
import org.mule.module.db.domain.type.DbType;
import org.mule.module.db.domain.type.DbTypeManager;
import org.mule.module.db.domain.type.DynamicDbType;
import org.mule.module.db.domain.type.JdbcTypes;
import org.mule.module.db.domain.type.UnknownDbType;
import org.mule.module.db.domain.type.UnknownDbTypeException;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

@SmallTest
public class DefaultParamTypeResolverTestCase extends AbstractMuleTestCase
{

    public static final String SQL_TEXT = "select * from test where id = ?";
    public static final String CUSTOM_TYPE_NAME = "CUSTOM_TYPE_NAME";

    private final DbTypeManager dbTypeManager = mock(DbTypeManager.class);
    private final ParamTypeResolver metadataParamTypeResolver = mock(ParamTypeResolver.class);
    private final DefaultParamTypeResolver paramTypeResolver = new DefaultParamTypeResolver(dbTypeManager, metadataParamTypeResolver);
    private final DbConnection connection = mock(DbConnection.class);

    @Test
    public void resolvesUnknownTypeUsingMetadata() throws Exception
    {
        QueryTemplate queryTemplate = new QueryTemplate(SQL_TEXT, QueryType.SELECT, Collections.<org.mule.module.db.domain.param.QueryParam>singletonList(new DefaultInputQueryParam(1, UnknownDbType.getInstance(), "7", "param1")));

        when(metadataParamTypeResolver.getParameterTypes(connection, queryTemplate)).thenReturn(Collections.singletonMap(1, JdbcTypes.INTEGER_DB_TYPE));

        Map<Integer, DbType> parameterTypes = paramTypeResolver.getParameterTypes(connection, queryTemplate);

        assertThat(1, equalTo(parameterTypes.size()));
        assertThat(JdbcTypes.INTEGER_DB_TYPE, equalTo(parameterTypes.get(1)));
    }

    @Test
    public void usesUnknownTypesWhenNoMetadataAvailable() throws Exception
    {
        QueryTemplate queryTemplate = new QueryTemplate(SQL_TEXT, QueryType.SELECT, Collections.<org.mule.module.db.domain.param.QueryParam>singletonList(new DefaultInputQueryParam(1, UnknownDbType.getInstance(), "7", "param1")));

        when(metadataParamTypeResolver.getParameterTypes(connection, queryTemplate)).thenThrow(new SQLException("Error"));

        Map<Integer, DbType> parameterTypes = paramTypeResolver.getParameterTypes(connection, queryTemplate);

        assertThat(1, equalTo(parameterTypes.size()));
        assertThat(UnknownDbType.getInstance(), equalTo(parameterTypes.get(1)));
    }

    @Test
    public void resolvesDynamicDbType() throws Exception
    {
        QueryTemplate queryTemplate = new QueryTemplate(SQL_TEXT, QueryType.SELECT, Collections.<org.mule.module.db.domain.param.QueryParam>singletonList(new DefaultInputQueryParam(1, new DynamicDbType(CUSTOM_TYPE_NAME), "7", "param1")));

        when(metadataParamTypeResolver.getParameterTypes(connection, queryTemplate)).thenThrow(new SQLException("Error"));
        DbType customType = mock(DbType.class);
        when(dbTypeManager.lookup(connection, CUSTOM_TYPE_NAME)).thenReturn(customType);

        Map<Integer, DbType> parameterTypes = paramTypeResolver.getParameterTypes(connection, queryTemplate);

        assertThat(1, equalTo(parameterTypes.size()));
        assertThat(customType, equalTo(parameterTypes.get(1)));
    }

    @Test
    public void skipsResolvedTypes() throws Exception
    {
        QueryTemplate queryTemplate = new QueryTemplate(SQL_TEXT, QueryType.SELECT, Collections.<org.mule.module.db.domain.param.QueryParam>singletonList(new DefaultInputQueryParam(1, JdbcTypes.INTEGER_DB_TYPE, "7", "param1")));

        Map<Integer, DbType> parameterTypes = paramTypeResolver.getParameterTypes(connection, queryTemplate);

        assertThat(1, equalTo(parameterTypes.size()));
        assertThat(JdbcTypes.INTEGER_DB_TYPE, equalTo(parameterTypes.get(1)));
    }

    @Test(expected = UnknownDbTypeException.class)
    public void failsResolvingInvalidType() throws Exception
    {
        QueryTemplate queryTemplate = new QueryTemplate(SQL_TEXT, QueryType.SELECT, Collections.<org.mule.module.db.domain.param.QueryParam>singletonList(new DefaultInputQueryParam(1, new DynamicDbType(CUSTOM_TYPE_NAME), "7", "param1")));

        when(metadataParamTypeResolver.getParameterTypes(connection, queryTemplate)).thenThrow(new SQLException("Error"));
        when(dbTypeManager.lookup(connection, CUSTOM_TYPE_NAME)).thenThrow(new UnknownDbTypeException(CUSTOM_TYPE_NAME));

        paramTypeResolver.getParameterTypes(connection, queryTemplate);
    }
}