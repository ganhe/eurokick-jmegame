package football.stage.sound

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import java.util.ArrayList;
import java.util.EnumMap;
import football.FootballGame
import static football.stage.sound.SoundClip.*

public class FbSoundManager{

    private EnumMap<SoundClip, AudioNode> soundMap;
    private FootballGame app;
    private AudioRenderer audioRenderer;
    private AssetManager assetManager;

    public FbSoundManager(FootballGame app) {
        this.app = app;
        audioRenderer = app.getAudioRenderer();
        assetManager = app.getAssetManager();

        soundMap = new EnumMap<SoundClip, AudioNode>(SoundClip.class);
    }

    public void load(int level) {
        if (level == 1) {
            loadMusic([STADIUM_BACKGROUND,MUSIC] as SoundClip[]);
            
        }
    }

    // loads all sound effects which will be needed for that level
    private void loadSoundEffects(SoundClip[] sounds) {

        for (SoundClip s : sounds) {
            AudioNode soundNode = new AudioNode(assetManager, s.path());
            soundMap.put(s, soundNode);
        }
    }

    // load all music which will be streamed
    public void loadMusic(SoundClip[] music) {

        for (SoundClip s : music) {
            if (s != null) {
                AudioNode musicNode = new AudioNode(assetManager, s.path(), true);
                musicNode.setLooping(true);
                musicNode.setPositional(false);
                musicNode.setDirectional(false);

                soundMap.put(s, musicNode);
            }
        }
    }

    public void unloadMusic(SoundClip[] music) {
        for (SoundClip s : music) {
            soundMap.remove(s);
        }
    }

    public void unloadAllMusic() {
        ArrayList<SoundClip> musicList = new ArrayList<SoundClip>();

        for (SoundClip s : soundMap.keySet()) {
            if (soundMap.get(s).isLooping()) {
                musicList.add(s);
            }
        }

        for (SoundClip soundClip : musicList) {
            soundMap.get(soundClip).stop();
            soundMap.remove(soundClip);
        }
    }

    public void play(SoundClip sound) {
        this.play(sound,1f);
    }

    public void play(SoundClip sound,float startVolume){
        AudioNode toPlay = soundMap.get(sound);

        if (toPlay != null) {
            if (sound.isMusic()) {
                /*
                if (app.getUserSettings().isMusicMuted()) {
                return;
                }
                 */
                toPlay.setVolume(startVolume);
                toPlay.play();
            } else {
                /*
                if (app.getUserSettings().isSoundFXMuted()) {
                return;
                }
                 */              
                toPlay.setVolume(startVolume);
                toPlay.playInstance();
            }
        }
    }
    // pause the music
    public void pause(SoundClip sound) {

        AudioNode toPause = soundMap.get(sound);

        if (toPause != null) {
            audioRenderer.pauseSource(toPause);
        }
    }

    // if paused it will play, if playing it will be paused
    public void togglePlayPause(SoundClip sound) {
        AudioNode toToggle = soundMap.get(sound);

        if (toToggle != null) {
            if (toToggle.getStatus() == AudioNode.Status.Paused
                || toToggle.getStatus() == AudioNode.Status.Stopped) {
                play(sound);
            } else {
                pause(sound);
            }
        }
    }

    // tries to stop a sound, will probably only work for streaming music though
    void stop(SoundClip sound) {
        AudioNode toStop = soundMap.get(sound);

        toStop.stop();
    }

    public void cleanup() {
        unloadAllMusic();
        soundMap.clear();
    }
}
