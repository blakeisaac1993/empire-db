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
package org.apache.empire.dbms.postgresql;

import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBColumnExpr;
import org.apache.empire.db.DBCommand;
import org.apache.empire.db.DBRowSet;
import org.apache.empire.db.exceptions.NoPrimaryKeyException;
import org.apache.empire.db.expr.compare.DBCompareExpr;

/**
 * Defines the PostgreSQL command type.
 */ 
public class DBCommandPostgres extends DBCommand
{
    // *Deprecated* private static final long serialVersionUID = 1L;
  
    protected int limit = -1;
    protected int skip  = -1;
    
    public DBCommandPostgres(boolean autoPrepareStmt)
    {
        super(autoPrepareStmt);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/functions-datetime.html
     */
    public DBColumnExpr pgAge(DBColumnExpr expr)
    {
        return new PostgresFuncExpr(expr, PostgresSqlPhrase.AGE, null, DataType.INTEGER);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/functions-datetime.html
     */
    public DBColumnExpr pgAge(DBColumnExpr expr1, DBColumnExpr expr2)
    {
        return new PostgresFuncExpr(expr1, PostgresSqlPhrase.AGE_BETWEEN, new Object[] { expr2 }, DataType.INTEGER);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/functions-datetime.html
     */
    public DBColumnExpr pgExtract(PostgresExtractField field, DBColumnExpr expr)
    {
        return new PostgresFuncExpr(expr, PostgresSqlPhrase.EXTRACT, new Object[] { field.name() }, DataType.INTEGER);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public DBColumnExpr pgToTsquery(DBColumnExpr expr)
    {
        return new PostgresFuncExpr(expr, PostgresSqlPhrase.TO_TSQUERY, null, DataType.UNKNOWN);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public DBColumnExpr pgToTsvector(DBColumnExpr expr)
    {
        return new PostgresFuncExpr(expr, PostgresSqlPhrase.TO_TSVECTOR, null, DataType.UNKNOWN);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public DBColumnExpr pgPlaintoTsquery(DBColumnExpr expr)
    {
        return new PostgresFuncExpr(expr, PostgresSqlPhrase.PLAINTO_TSQUERY, null, DataType.UNKNOWN);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public DBColumnExpr pgBoolAnd(DBCompareExpr cmpExpr)
    {
        return new PostgresBoolAndOrExpr(cmpExpr, false);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public DBColumnExpr pgBoolOr(DBCompareExpr cmpExpr)
    {
        return new PostgresBoolAndOrExpr(cmpExpr, true);
    }
    
    /**
     * See https://www.postgresql.org/docs/current/textsearch-controls.html
     */
    public PostgresAtAt pgCompareAtAt(DBColumnExpr left, DBColumnExpr right)
    {
        return new PostgresAtAt(left, right);
    }
    
    @Override
    public DBCommand limitRows(int numRows)
    {
        limit = numRows;
        return this;
    }

    @Override
    public DBCommand skipRows(int numRows)
    {
        skip = numRows;
        return this;
    }
     
    @Override
    public void clearLimit()
    {
        limit = -1;
        skip  = -1;
    }
    
    @Override
    public void getSelect(StringBuilder buf)
    {   // call base class
        super.getSelect(buf);
        // add limit and offset
        if (limit>=0)
        {   buf.append("\r\nLIMIT ");
            buf.append(String.valueOf(limit));
            // Offset
            if (skip>=0) 
            {   buf.append(" OFFSET ");
                buf.append(String.valueOf(skip));
            }    
        }
    }
    
    @Override
    protected void addUpdateWithJoins(StringBuilder buf, DBRowSet table)
    {
        DBColumn[] keyColumns = table.getKeyColumns();
        if (keyColumns==null || keyColumns.length==0)
            throw new NoPrimaryKeyException(table);
        // Join Update
        table.addSQL(buf, CTX_NAME);
        buf.append(" t0");
        long context = CTX_DEFAULT;
        // Set Expressions
        buf.append("\r\nSET ");
        addListExpr(buf, set, context, ", ");
        // From clause
        addFrom(buf);
        // Add Where
        buf.append("\r\nWHERE");
        // key columns
        for (DBColumn col : keyColumns)
        {   // compare 
            buf.append(" t0.");
            col.addSQL(buf, CTX_NAME);
            buf.append("=");
            buf.append(table.getAlias());
            buf.append(".");
            col.addSQL(buf, CTX_NAME);
        }
        // more constraints
        if (where!=null && !where.isEmpty())
        {   // add where expression
            buf.append("\r\n  AND ");
            addListExpr(buf, where, context, " AND ");
        }
    }
}