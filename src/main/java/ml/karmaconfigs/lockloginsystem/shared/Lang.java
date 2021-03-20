package ml.karmaconfigs.lockloginsystem.shared;

public enum Lang {
    ENGLISH, SPANISH, SIMPLIFIED_CHINESE, ITALIAN, POLISH, FRENCH, CZECH, RUSSIAN, UNKNOWN;

    public String friendlyName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}
