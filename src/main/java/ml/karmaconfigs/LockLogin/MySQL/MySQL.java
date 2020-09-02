package ml.karmaconfigs.LockLogin.MySQL;

@Deprecated
@SuppressWarnings("unused")
public class MySQL {

    private Bucket bucket;

    public MySQL() {
    }

    /**
     * Initialize a bucket MySQl from MySQL
     *
     * @param host     the host
     * @param database the database
     * @param table    the table
     * @param user     the user
     * @param password the password
     * @param port     the port
     * @param useSSL   if the MySQL uses SSL
     */
    public MySQL(String host, String database, String table, String user, String password, int port, boolean useSSL) {
        bucket = new Bucket(host, database, table, user, password, port, useSSL);
    }

    /**
     * Get new MySQL method
     *
     * <code>Since version 3.2.1
     * MySQL changed to pool connections
     * and old MySQl method got deprecated</code>
     */
    public Bucket getBucket() {
        return bucket;
    }
}
