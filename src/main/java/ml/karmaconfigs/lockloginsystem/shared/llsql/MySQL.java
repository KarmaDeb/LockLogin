package ml.karmaconfigs.lockloginsystem.shared.llsql;

/**
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */
@Deprecated
@SuppressWarnings("unused")
public class MySQL {

    private Bucket bucket;

    public MySQL() {
    }

    /**
     * Initialize a bucket MySQl from MySQL
     *
     * @param host               the host
     * @param database           the database
     * @param table              the table
     * @param user               the user
     * @param password           the password
     * @param port               the port
     * @param useSSL             if the MySQL uses SSL
     * @param ignoreCertificates if the MySQL ignores CA certificates
     */
    public MySQL(String host, String database, String table, String user, String password, int port, boolean useSSL, boolean ignoreCertificates) {
        bucket = new Bucket(host, database, table, user, password, port, useSSL, ignoreCertificates);
    }

    /**
     * Get new MySQL method
     *
     * <code>Since version 3.2.1
     * MySQL changed to pool connections
     * and old MySQl method got deprecated</code>
     *
     * @return a Bucket instance
     */
    public Bucket getBucket() {
        return bucket;
    }
}
