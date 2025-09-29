package com.valhala.mypokedex.config;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbStartupWaiterTest {

    @Test
    void returnsWhenConnectionAvailable() throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        DbWaitConfiguration config = new DbWaitConfiguration();
        config.setMaxRetries(3);
        config.setWaitSeconds(0); // speed up test

        DbStartupWaiter waiter = new DbStartupWaiter(ds, config);

        // Should not throw
        waiter.onApplicationEvent(null);

        verify(ds, atLeastOnce()).getConnection();
        conn.close();
    }

    @Test
    void throwsWhenUnavailableAfterRetries() throws SQLException {
        DataSource ds = mock(DataSource.class);
        when(ds.getConnection()).thenThrow(new SQLException("nope"));

        DbWaitConfiguration config = new DbWaitConfiguration();
        config.setMaxRetries(2);
        config.setWaitSeconds(0); // speed up test

        DbStartupWaiter waiter = new DbStartupWaiter(ds, config);

        assertThrows(IllegalStateException.class, () -> waiter.onApplicationEvent(null));

        verify(ds, atLeast(2)).getConnection();
    }
}
