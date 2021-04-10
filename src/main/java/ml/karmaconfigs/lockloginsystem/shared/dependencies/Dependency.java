package ml.karmaconfigs.lockloginsystem.shared.dependencies;

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
public enum Dependency {
    /**
     * LockLogin dependency
     */
    COMMONS,
    /**
     * LockLogin dependency
     */
    JNA,
    /**
     * LockLogin dependency
     */
    GOOGLE,
    /**
     * LockLogin dependency
     */
    LOG4J,
    /**
     * LockLogin dependency
     */
    LOG4J_WEB;

    /**
     * Get the dependency download url
     *
     * @return the dependency download url
     */
    public String downloadURL() {
        switch (this) {
            case COMMONS:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/commons-codec.jar";
            case JNA:
                return "https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.8.0/jna-5.8.0.jar";
            case GOOGLE:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/googleauth.jar";
            case LOG4J:
                return "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.14.1/log4j-core-2.14.1.jar";
            case LOG4J_WEB:
                return "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-web/2.14.1/log4j-web-2.14.1.jar";
            default:
                return "";
        }
    }

    /**
     * Get the dependency file name
     *
     * @return the dependency .jar name
     */
    public String fileName() {
        switch (this) {
            case COMMONS:
                return "CommonsCodec.jar";
            case JNA:
                return "SunJNA.jar";
            case GOOGLE:
                return "GoogleAuthenticator.jar";
            case LOG4J:
                return "Log4jCore.jar";
            case LOG4J_WEB:
                return "Log4jWeb.jar";
            default:
                return "";
        }
    }
}
