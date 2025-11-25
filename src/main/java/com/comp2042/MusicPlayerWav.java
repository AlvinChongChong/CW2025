package com.comp2042;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * A simple WAV music player that can play and loop background music.
 * <p>
 * Supports loading a WAV file from the classpath and looping it indefinitely.
 * </p>
 */
public class MusicPlayerWav {

    private Clip clip;
    private String currentResource;
    private boolean playing;

    /**
     * Plays a WAV file from the specified resource path and loops it continuously.
     *
     * @param resourcePath the path to the WAV file within the classpath
     */
    public void playMusic(String resourcePath) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl == null) {
                System.err.println("Sound file not found: " + resourcePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // loop forever
            clip.start();
            currentResource = resourcePath;
            playing = true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            playing = false;
            e.printStackTrace();
        }
    }

    /**
     * Stops the currently playing music if it is running.
     */
    public void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        playing = false;
    }

    /**
     * @return true if the clip is currently playing.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Toggles playback state. If music is playing, it stops.
     * If muted, it resumes using the last loaded track.
     */
    public void togglePlayback() {
        if (isPlaying()) {
            stopMusic();
        } else if (currentResource != null) {
            playMusic(currentResource);
        }
    }
}
