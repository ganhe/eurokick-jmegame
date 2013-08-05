package football.stage.sound

public enum SoundClip {
    STADIUM_BACKGROUND("background/background.wav", true),
    MUSIC("Music/EndlessSummerSingleInstrumental.ogg", true);
    private final String path;
    private final boolean music;

    SoundClip(String filename, boolean isMusic) {
        path = "Sounds/" + filename;
        music = isMusic;
    }

    public String path() {
        return path;
    }

    public boolean isMusic() {
        return music;
    }
}

