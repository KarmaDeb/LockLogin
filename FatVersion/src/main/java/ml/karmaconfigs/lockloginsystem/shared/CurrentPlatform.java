package ml.karmaconfigs.lockloginsystem.shared;

public final class CurrentPlatform {

    private static Platform running;

    /**
     * Get the current running platform
     *
     * @return the current platform
     */
    public final Platform getRunning() {
        return running;
    }

    /**
     * Set the current running platform
     *
     * @param platform the current platform
     */
    public final void setRunning(final Platform platform) {
        running = platform;
    }
}
