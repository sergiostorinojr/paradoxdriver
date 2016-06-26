/*
 * ParadoxStatement.java
 *
 * 03/14/2009
 * Copyright (C) 2009 Leonardo Alves da Costa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.googlecode.paradox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.paradox.data.table.value.FieldValue;
import com.googlecode.paradox.parser.SQLParser;
import com.googlecode.paradox.parser.nodes.SelectNode;
import com.googlecode.paradox.parser.nodes.StatementNode;
import com.googlecode.paradox.planner.Planner;
import com.googlecode.paradox.planner.plan.SelectPlan;
import com.googlecode.paradox.results.Column;
import com.googlecode.paradox.utils.SQLStates;
import com.googlecode.paradox.utils.Utils;

/**
 * JDBC statement implementation.
 *
 * @author Leonardo Alves da Costa
 * @version 1.0
 * @since 1.0
 */
public class ParadoxStatement implements Statement {

    private boolean closed = false;
    private final ParadoxConnection conn;
    String cursorName = "NO_NAME";
    private int fetchDirection = ResultSet.FETCH_FORWARD;
    private int fetchSize = 10;
    private int maxFieldSize = 255;
    private int maxRows = 0;
    private boolean poolable = false;
    private int queryTimeout = 20;
    private ParadoxResultSet rs = null;
    private SQLWarning warnings = null;

    /**
     * Creates a statement.
     *
     * @param conn the paradox connection.
     */
    public ParadoxStatement(final ParadoxConnection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBatch(final String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel() throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearWarnings() throws SQLException {
        warnings = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }
        closed = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeOnCompletion() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final String sql) throws SQLException {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }
        boolean select = false;
        final SQLParser parser = new SQLParser(sql);
        final List<StatementNode> statements = parser.parse();
        for (final StatementNode statement : statements) {
            if (statement instanceof SelectNode) {
                executeSelect((SelectNode) statement);
                select = true;
            }
        }
        return select;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        return execute(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        return execute(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        return execute(sql);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }
        final SQLParser parser = new SQLParser(sql);
        final List<StatementNode> statementList = parser.parse();
        if (statementList.size() > 1) {
            throw new SQLFeatureNotSupportedException("Unsupported operation.", SQLStates.INVALID_SQL);
        }
        final StatementNode node = statementList.get(0);
        if (!(node instanceof SelectNode)) {
            throw new SQLFeatureNotSupportedException("Not a SELECT statement.", SQLStates.INVALID_SQL);
        }
        executeSelect((SelectNode) node);
        return rs;
    }

    private void executeSelect(final SelectNode node) throws SQLException {
        final Planner planner = new Planner(conn);
        final SelectPlan plan = (SelectPlan) planner.create(node);
        plan.execute();
        rs = new ParadoxResultSet(conn, this, plan.getValues(), plan.getColumns());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchDirection() throws SQLException {
        return fetchDirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchSize() throws SQLException {
        return fetchSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new ParadoxResultSet(conn, this, new ArrayList<List<FieldValue>>(), new ArrayList<Column>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxFieldSize() throws SQLException {
        return maxFieldSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxRows() throws SQLException {
        return maxRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getQueryTimeout() throws SQLException {
        return queryTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet getResultSet() throws SQLException {
        return rs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getResultSetHoldability() throws SQLException {
        return conn.getHoldability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getResultSetType() throws SQLException {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUpdateCount() throws SQLException {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return warnings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPoolable() throws SQLException {
        return poolable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return Utils.isWrapperFor(this, iface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCursorName(final String name) throws SQLException {
        cursorName = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw new SQLException("O resultset somente pode ser ResultSet.FETCH_FORWARD", SQLStates.INVALID_PARAMETER);
        }
        fetchDirection = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        fetchSize = rows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        if (max > 255) {
            throw new SQLException("Value bigger than 255.", SQLStates.INVALID_PARAMETER);
        }
        maxFieldSize = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxRows(final int max) throws SQLException {
        maxRows = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        queryTimeout = seconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return Utils.unwrap(this, iface);
    }
}
