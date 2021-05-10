// SoundSensor.java

/*
 This software is part of the RobotSim library.
 It is Open Source Free Software, so you may
 - run the code for any purpose
 - study how the code works and adapt it to your needs
 - integrate all or parts of the code in your own programs
 - redistribute copies of the code
 - improve the code and release your improvements to the public
 However the use of the code is entirely your responsibility.

 Author: Aegidius Pluess, www.aplu.ch
 */
package ch.aplu.robotsim;

import ch.aplu.jgamegrid.GameGrid;
import ch.aplu.jgamegrid.Location;
import ch.aplu.util.SoundRecorder;
import ch.aplu.util.SoundSampleListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.io.ByteArrayOutputStream;

/**
 * Class that represents a NXT sound sensor. The sound is taken from the standard
 * audio device. A microphone must be available.
 * This is a dublicate of NxtSoundSensor to remain compatible with older versions.
 */
public class SoundSensor extends Part implements SoundSampleListener {
    //
    private static final Location pos1 = new Location(2, 7);
    private static final Location pos2 = new Location(2, -7);
    private static final Location pos3 = new Location(2, 0);
    private SoundListener soundListener = null;
    private final SensorPort port;
    private final AudioFormat audioFormat =
            new AudioFormat(22050.0F, 8, 1, true, false);
    private final SoundRecorder recorder = new SoundRecorder(audioFormat);
    private final ByteArrayOutputStream data = new ByteArrayOutputStream();
    private boolean isQuiet = true;
    private int triggerLevel;
    private int ampl;

    /**
     * Creates a sensor instance connected to the given port.
     * Only one sensor instance is allowed.
     *
     * @param port the port where the sensor is plugged-in
     */
    public SoundSensor(SensorPort port) {
        super("sprites/soundsensor.gif", port == SensorPort.S1
                ? pos1 : (port == SensorPort.S2 ? pos2 : pos3));
        this.port = port;
        try {
            recorder.capture(data);
        } catch (LineUnavailableException ex) {
            LegoRobot.fail("Sound card not available");
        }
        recorder.addSoundSampleListener(this);
    }

    protected void cleanup() {
        recorder.stopCapture();
    }

    /**
     * Registers the given sound listener for the given trigger level.
     *
     * @param listener     the SoundListener to register
     * @param triggerLevel the trigger level where the callback is triggered
     */
    public void addSoundListener(SoundListener listener, int triggerLevel) {
        soundListener = listener;
        this.triggerLevel = triggerLevel;
    }

    /**
     * Registers the given sound listener with default trigger level 50.
     *
     * @param listener the SoundListener to register
     */
    public void addSoundListener(SoundListener listener) {
        addSoundListener(listener, 50);
    }

    /**
     * Sets a new trigger level and returns the previous one.
     *
     * @param triggerLevel the new trigger level
     * @return the previous trigger level
     */
    public int setTriggerLevel(int triggerLevel) {
        this.triggerLevel = triggerLevel;
        return triggerLevel;
    }

    public void sampleReceived(int count) {
        byte[] samples = data.toByteArray();
        ampl = getAmplitude(samples);

        if (ampl > triggerLevel && isQuiet) {
            if (soundListener != null) {
                new Thread(() -> soundListener.loud(port, ampl)).start();
            }
            isQuiet = false;
        }
        if (ampl <= triggerLevel && !isQuiet) {
            if (soundListener != null) {
                new Thread(() -> soundListener.quiet(port, ampl)).start();
            }
            isQuiet = true;
        }
        data.reset();
    }

    int getAmplitude(byte[] samples) {
        byte max = 0;
        for (byte sample : samples) {
            if (sample > max)
                max = sample;
        }
        return max;
    }

    /**
     * Polls the sensor.
     * Calls Thread.sleep(1) to prevent CPU overload in close polling loops.
     *
     * @return the current value the sensor reported: 0 (quiet) .. 150 (loud)
     */
    public int getValue() {
        checkPart();
        delay(1);
        return (int) (150 * ampl / 127.0);
    }

    private void checkPart() {
        if (robot == null) {
            JOptionPane.showMessageDialog(null,
                    "SoundSensor is not part of the LegoRobot.\n"
                            + "Call addPart() to assemble it.",
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
                    || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
                System.exit(1);
            if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
                throw new RuntimeException("SoundSensor is not part of the LegoRobot.\n"
                        + "Call addPart() to assemble it.");
        }
    }
}
