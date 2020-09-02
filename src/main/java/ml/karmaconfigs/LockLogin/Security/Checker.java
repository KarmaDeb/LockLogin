package ml.karmaconfigs.LockLogin.Security;

public interface Checker {

    static boolean isValid(String name) {
        return new NameChecker(name).isValid();
    }

    static String getIllegalChars(String name) {
        return new NameChecker(name).getIllegalChars(name);
    }
}
