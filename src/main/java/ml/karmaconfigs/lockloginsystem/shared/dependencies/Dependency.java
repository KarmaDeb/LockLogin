package ml.karmaconfigs.lockloginsystem.shared.dependencies;

public enum Dependency {
    COMMONS,POOL2,SLF4J,HIKARICP,GOOGLE,ARGON2;

    public String downloadURL() {
        switch (this) {
            case COMMONS:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/commons-codec.jar";
            case POOL2:
                return "https://repo1.maven.org/maven2/org/apache/commons/commons-pool2/2.9.0/commons-pool2-2.9.0.jar";
            case SLF4J:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/slf4j.jar";
            case HIKARICP:
                return "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/4.0.3/HikariCP-4.0.3.jar";
            case GOOGLE:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/googleauth.jar";
            case ARGON2:
                return "https://raw.githubusercontent.com/KarmaConfigs/project_c/main/src/libs/LockLogin/Argon2.jar";
            default:
                return "";
        }
    }

    public String fileName() {
        switch (this) {
            case COMMONS:
                return "CommonsCodec.jar";
            case POOL2:
                return "CommonsPool2.jar";
            case SLF4J:
                return "SLF4J.jar";
            case HIKARICP:
                return "HikariCP.jar";
            case GOOGLE:
                return "GoogleAuth.jar";
            case ARGON2:
                return "Argon2I.jar";
            default:
                return "";
        }
    }
}
