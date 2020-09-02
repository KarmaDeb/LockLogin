package ml.karmaconfigs.LockLogin.Version;

public interface LockLoginVersion {

    String changeLog = new GetLatestVersion().getChangeLog();
    String version = new GetLatestVersion().getVersionString();
    Integer versionID = new GetLatestVersion().GetLatest();
}
