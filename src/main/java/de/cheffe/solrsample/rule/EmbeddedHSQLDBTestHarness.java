package de.cheffe.solrsample.rule;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.junit.rules.ExternalResource;

public class EmbeddedHSQLDBTestHarness extends ExternalResource {

    private Server server;

    private String name = "mydb";
    private int port = 9001;

    public EmbeddedHSQLDBTestHarness() {
        super();
    }

    public EmbeddedHSQLDBTestHarness(String name, int port) {
        super();
        this.name = name;
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        File hsqlHome = new File("target/temporary/hsqldb/" + name);
        if (hsqlHome.exists()) {
            hsqlHome.delete();
        }
        hsqlHome.mkdirs();

        String dbHome = "file:" + hsqlHome.getAbsolutePath();

        HsqlProperties p = new HsqlProperties();
        p.setProperty("server.database.0", dbHome);
        p.setProperty("server.dbname.0", name);
        p.setProperty("server.port", port);
        server = new Server();
        server.setProperties(p);
        server.setLogWriter(new PrintWriter(System.out));
        server.setErrWriter(new PrintWriter(System.err));
        server.start();
    }

    @Override
    protected void after() {
        super.after();
        if (server != null) {
            server.stop();
        }
    }

    public Connection createConnection() {
        try {
            if (port == 9001) {
                return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/" + name, "sa", "");
            }
            return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:" + port + "/" + name, "sa", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
