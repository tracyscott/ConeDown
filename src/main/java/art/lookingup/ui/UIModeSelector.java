package art.lookingup.ui;

import art.lookingup.ConeDown;
import art.lookingup.ParameterFile;
import art.lookingup.PropertyFile;
import art.lookingup.patterns.UninterruptiblePattern;
import com.giantrainbow.UtilsForLX;
import heronarts.lx.*;
import heronarts.lx.parameter.*;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import heronarts.p3lx.ui.component.UIKnob;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class UIModeSelector extends UICollapsibleSection {
  private static final Logger logger = Logger.getLogger(UIModeSelector.class.getName());

  public final UIButton autoMode;
  public final UIButton audioMode;
  public final UIButton standardMode;
  public final UIButton interactiveMode;
  public final UIButton instrumentMode;
  protected LX lx;

  static public BooleanParameter audioModeP = new BooleanParameter("audio", false);
  static public BooleanParameter standardModeP = new BooleanParameter("standard", false);
  static public BooleanParameter interactiveModeP = new BooleanParameter("interactive", false);
  static public BooleanParameter instrumentModeP = new BooleanParameter("instrument", false);

  static public BooleanParameter autoAudioModeP;
  static public DiscreteParameter autoOffStart;
  static DiscreteParameter autoOffEnd;
  static public CompoundParameter timePerChannelP;
  static public CompoundParameter timePerChannelP2;
  static public CompoundParameter timePerChannelP3;
  static public CompoundParameter timePerChannelP4;
  static public CompoundParameter timePerChannelP5;

  static public BoundedParameter fadeTimeP = new BoundedParameter("FadeT", 1000.0, 0.000, 10000.0);

  public final UIKnob timePerChannel;
  public final UIKnob timePerChannel2;
  public final UIKnob timePerChannel3;
  public final UIKnob timePerChannel4;
  public final UIKnob fadeTime;

  public String[] standardModeChannelNames = { "MULTI", "GIF", "STD", "ICE"};
  public List<LXChannelBus> standardModeChannels = new ArrayList<LXChannelBus>(standardModeChannelNames.length);
  public int currentPlayingChannel = 0;
  public int previousPlayingChannel = 0;
  public UIAudioMonitorLevels audioMonitorLevels;

  public ParameterFile paramFile;
  public List<LXParameter> parameters = new ArrayList<LXParameter>();
  public Map<String, LXParameter> paramLookup = new HashMap<String, LXParameter>();
  static public final String filename = "modeselector.json";

  public UIModeSelector(final LXStudio.UI ui, LX lx, UIAudioMonitorLevels audioMonitor) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 200);
    setTitle("MODE");
    setLayout(UI2dContainer.Layout.VERTICAL);
    setChildMargin(2);
    this.lx = lx;

    load();
    autoAudioModeP = registerBooleanParameter("autoaudio", false);
    timePerChannelP = registerCompoundParameter("MultiT", 60000.0, 2000.0, 360000.0);
    timePerChannelP2 = registerCompoundParameter("GifT", 60000.0, 2000.0, 360000.0);
    timePerChannelP3 = registerCompoundParameter("StdT", 60000.0, 2000.0, 360000.0);
    timePerChannelP4 = registerCompoundParameter("IceT", 60000.0, 2000.0, 360000.0);
    timePerChannelP5 = registerCompoundParameter("TPerCh5", 60000.0, 2000.0, 360000.0);
    autoOffStart = registerDiscreteParameter("offSt", 8, 0, 25);
    autoOffEnd = registerDiscreteParameter("offEnd", 15, 0, 25);
    save();


    audioMonitorLevels = audioMonitor;
    // When enabled, audio monitoring can trigger automatic channel switching.
    autoMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18)
    .setParameter(autoAudioModeP)
    .setLabel("Auto Audio Detect")
    .addToContainer(this);

    UI2dContainer knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    timePerChannel = new UIKnob(timePerChannelP);
    timePerChannel.addToContainer(knobsContainer);
    timePerChannel2 = new UIKnob(timePerChannelP2);
    timePerChannel2.addToContainer(knobsContainer);
    timePerChannel3 = new UIKnob(timePerChannelP3);
    timePerChannel3.addToContainer(knobsContainer);
    // Button saving config.
    new UIButton(10, 7, 40, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          save();
        }
      }
    }.setLabel("\u21BA").setMomentary(true).addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);
    knobsContainer = new UI2dContainer(0, 30, getContentWidth(), 45);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(0, 0, 0, 0);
    timePerChannel4 = new UIKnob(timePerChannelP4);
    timePerChannel4.addToContainer(knobsContainer);
    fadeTime = new UIKnob(fadeTimeP);
    fadeTime.addToContainer(knobsContainer);
    new UIKnob(autoOffStart).addToContainer(knobsContainer);
    new UIKnob(autoOffEnd).addToContainer(knobsContainer);
    knobsContainer.addToContainer(this);

    audioMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          standardMode.setActive(false);
          interactiveMode.setActive(false);
          instrumentMode.setActive(false);
          // Enable AUDIO channel
          setAudioChannelEnabled(true);
        } else {
          // Disable AUDIO channel
          setAudioChannelEnabled(false);
          audioModeP.setValue(false);
        }
      }
    }.setParameter(audioModeP).setLabel("Audio").setActive(false).addToContainer(this);

    for (String channelName: standardModeChannelNames) {
      LXChannelBus ch = UtilsForLX.getChannelByLabel(lx, channelName);
      standardModeChannels.add(ch);
    }

    standardMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          logger.info("Enabling standard mode.");
          audioMode.setActive(false);
          interactiveMode.setActive(false);
          instrumentMode.setActive(false);
          // Build our list of Standard Channels based on our names.  Putting it here allows it to
          // work after loading a new file (versus startup initialization).
          setStandardChannelsEnabled(true);
        } else {
          logger.info("Disabling standard mode.");
          // Disable Standard Channels
          setStandardChannelsEnabled(false);
        }
      }
    }.setParameter(standardModeP).setLabel("Standard").setActive(false).addToContainer(this);

    interactiveMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          audioMode.setActive(false);
          standardMode.setActive(false);
          instrumentMode.setActive(false);
          // Enable Interactive channel
          setInteractiveChannelEnabled(true);
        } else {
          // Disable Interactive channel
          setInteractiveChannelEnabled(false);
        }
      }
    }.setParameter(interactiveModeP).setLabel("Interactive").setActive(false).addToContainer(this);

    instrumentMode = (UIButton) new UIButton(0, 0, getContentWidth(), 18) {
      public void onToggle(boolean on) {
        if (on) {
          audioMode.setActive(false);
          standardMode.setActive(false);
          interactiveMode.setActive(false);
          // Enable Instrument/AUDIO channels
          setInstrumentChannelsEnabled(true);
        } else {
          // Disable Instrument/AUDIO channels
          setInstrumentChannelsEnabled(false);
        }
      }
    }.setParameter(instrumentModeP).setLabel("Instrument").setActive(false).addToContainer(this);

    if (lx.engine.audio.input != null) {
      if (lx.engine.audio.input.device.getObject().isAvailable()) {
        //lx.engine.audio.enabled.setValue(true);
        lx.engine.addLoopTask(new AudioMonitor());
      } else {
        logger.warning("Audio Input device is not available!");
      }
    } else {
      logger.warning("Audio Input is null.");
    }

    lx.engine.addLoopTask(new StandardModeCycle());
  }

  public void load() {
    paramFile = new ParameterFile(filename);
    try {
      paramFile.load();
    } catch (PropertyFile.NotFound nfex) {
      System.out.println(filename + ", property not found.");
    } catch (IOException ioex) {
      System.err.println(filename + " not found, will be created.");
    }
  }

  public void save() {
    try {
      paramFile.save();
    } catch (IOException ioex) {
      System.err.println("Error saving " + filename + " " + ioex.getMessage());
    }
  }

  public StringParameter registerStringParameter(String label, String value) {
    StringParameter sp = paramFile.getStringParameter(label, value);
    parameters.add(sp);
    paramLookup.put(label, sp);
    return sp;
  }

  public CompoundParameter registerCompoundParameter(String label, double value, double base, double range) {
    CompoundParameter cp = paramFile.getCompoundParameter(label, value, base, range);
    parameters.add(cp);
    paramLookup.put(label, cp);
    return cp;
  }

  public DiscreteParameter registerDiscreteParameter(String label, int value, int min, int max) {
    DiscreteParameter dp = paramFile.getDiscreteParameter(label, value, min, max);
    parameters.add(dp);
    paramLookup.put(label, dp);
    return dp;
  }

  public BooleanParameter registerBooleanParameter(String label, boolean value) {
    BooleanParameter bp = paramFile.getBooleanParameter(label, value);
    parameters.add(bp);
    paramLookup.put(label, bp);
    return bp;
  }

  public void switchToMode(String mode) {
    if ("Audio".equalsIgnoreCase(mode)){
      // TODO
      audioMode.setActive(true);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);
      channel = UtilsForLX.getChannelByLabel(lx, "ICE");
      if (channel != null)
        channel.enabled.setValue(false);

    } else if ("Standard".equalsIgnoreCase(mode)) {
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(true);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);
      channel = UtilsForLX.getChannelByLabel(lx, "ICE");
      if (channel != null)
        channel.enabled.setValue(false);
      standardMode.setActive(true);
    } else if ("Interactive".equalsIgnoreCase(mode)) {
      // TODO
    } else if ("Instrument".equalsIgnoreCase(mode)) {
      // TODO
    } else if ("Text".equalsIgnoreCase(mode)) {
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(true);
      channel = UtilsForLX.getChannelByLabel(lx, "ICE");
      if (channel != null)
        channel.enabled.setValue(false);

      // Need to find TEXT channel and enable it.
    } else if ("Ice".equalsIgnoreCase(mode)) {
      // Disable standard mode and set ICE channel active.  First, no fade
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      setStandardChannelsEnabled(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "ICE");
      if (channel != null)
        channel.enabled.setValue(true);
      channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);

    } else if ("None".equalsIgnoreCase(mode)) {
      // Disable all modes, will stay on currently selected channel.
      audioMode.setActive(false);
      interactiveMode.setActive(false);
      instrumentMode.setActive(false);
      // We don't want to inactivate current channel, we just want to
      // inactivate the auto-scheduling.
      standardMode.setActive(false);
      LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "ICE");
      if (channel != null)
        channel.enabled.setValue(false);
      channel = UtilsForLX.getChannelByLabel(lx, "TEXT");
      if (channel != null)
        channel.enabled.setValue(false);

    }
  }

  public void setAudioChannelEnabled(boolean on) {
    LXChannelBus audioChannel = UtilsForLX.getChannelByLabel(lx, "AUDIO");
    if (audioChannel != null) audioChannel.enabled.setValue(on);
  }

  public void setStandardChannelsEnabled(boolean on) {
    if (on) {
      LXChannelBus channel = standardModeChannels.get(currentPlayingChannel);
      if (channel != null)
        channel.enabled.setValue(true);
    } else {
      for (LXChannelBus channel : standardModeChannels) {
        if (channel != null)
          channel.enabled.setValue(on);
      }
    }
  }

  public void setInteractiveChannelEnabled(boolean on) {
    LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "INTERACTIVE");
    if (channel != null) channel.enabled.setValue(on);
  }

  public void setInstrumentChannelsEnabled(boolean on) {
    LXChannelBus channel = UtilsForLX.getChannelByLabel(lx, "AUDIO");
    if (channel != null) channel.enabled.setValue(on);
    channel = UtilsForLX.getChannelByLabel(lx, "MIDI");
    if (channel != null) channel.enabled.setValue(on);
  }

  public class StandardModeCycle implements LXLoopTask {
    public double currentChannelPlayTime = 0.0;
    public double timePerChannel = 5000.0;  // Make this settable in the UI.
    public double fadeTime = 1000.0;  // Make this settable in the UI.
    public double prevChannelDisableThreshold = 0.1;  // Settable in UI? How low slider goes before full disable.
    public double channelFadeFullThreshold = 0.1; // At this value just set it to 1.  Deals with chunkiness around time.
    public double fadeTimeRemaining = 0.0;

    // TODO(tracy): Audio-Standard mode switching with faders.
    // When auto audio detection is fading in, we want to fade any active standard channels to 0
    // and fully reset the Standard-mode channel switching state once we are faded to 0.
    // (i.e. reset currentChannelPlayTime and fadeTimeRemaining).
    // When auto audio detection is fading out, we want to fade in some standard channel, but it should
    // be whatever the last currentChannel that was active in standard mode.  We need to reset
    // the Standard-mode channel switching state so that we are only dealing with a single fader.
    // i.e. reset currentChannelPlayTime and fadeTimeRemaining so we don't end up in a three-way
    // fader situation.  Also make sure that autoAudioFadeTime is much less than currentChannelPlayTime
    // or else we could end up in a three-way fader situation.

    public void loop(double deltaMs) {
      //Date date =
      Calendar cal = Calendar.getInstance();
      //cal.setTime(date);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int minutes = cal.get(Calendar.MINUTE);
      if (hour > autoOffStart.getValuei() && hour < autoOffEnd.getValuei()) {
        // Set master fader to 0
        lx.engine.output.brightness.setValue(0f);
      }

      if (!UIModeSelector.this.standardModeP.getValueb())
        return;

      // Disable Standard-mode channel switching for AnimatedTextPP patterns to prevent
      // fading in the middle of text.  We achieve this by effectively stalling the
      // currentChannelPlayTime until the current channel is no longer an
      // UninterruptiblePattern pattern.
      LXChannelBus channelBus = standardModeChannels.get(currentPlayingChannel);
      if (channelBus instanceof LXChannel) {
        LXChannel c = (LXChannel) channelBus;
        if (c.patterns.size() > 0) {
          LXPattern p = c.getActivePattern();
          if (p instanceof UninterruptiblePattern) {
            currentChannelPlayTime = 0.0;
          }
        }
      } else if (channelBus instanceof LXGroup) {
        LXGroup g = (LXGroup) channelBus;
        if (g.channels.size() > 0) {
          for (LXChannel c : g.channels) {
            if (c.patterns.size() > 0) {
              LXPattern p = c.getActivePattern();
              if (p instanceof UninterruptiblePattern) {
                currentChannelPlayTime = 0.0;
              }
            }
          }
        }
      }

      fadeTime = UIModeSelector.fadeTimeP.getValue();
      // TODO(tracy): This should be settable by Channel so that we can adjust
      // it for more fair scheduling.
      timePerChannel = UIModeSelector.timePerChannelP.getValue();
      if (currentPlayingChannel == 0) {
        timePerChannel = UIModeSelector.timePerChannelP.getValue();
      } else if (currentPlayingChannel == 1) {
        timePerChannel = UIModeSelector.timePerChannelP2.getValue();
      } else if (currentPlayingChannel == 2) {
        timePerChannel = UIModeSelector.timePerChannelP3.getValue();
      } else if (currentPlayingChannel == 3) {
        timePerChannel = UIModeSelector.timePerChannelP4.getValue();
      }

      // If our current configuration doesn't have multiple standard channel names, just no-op.
      if (standardModeChannels.size() < 2) {
        logger.warning("Too few standard channels.");
        return;
      }

      // We are still fading,
      if (fadeTimeRemaining > 0.0) {
        double previousChannelPercent = fadeTimeRemaining / fadeTime;
        double percentDone = 1.0 - previousChannelPercent;
        if (percentDone + channelFadeFullThreshold >= 1.0) {
          percentDone = 1.0;
        }
        LXChannelBus previousChannel = standardModeChannels.get(previousPlayingChannel);
        LXChannelBus currentChannel = standardModeChannels.get(currentPlayingChannel);
        if (previousChannel != null) {
          previousChannel.fader.setValue(previousChannelPercent);
          if (previousChannelPercent < prevChannelDisableThreshold)
            previousChannel.enabled.setValue(false);
        }
        if (currentChannel != null) currentChannel.fader.setValue(percentDone);
        // When this goes below zero we are done fading until the timePerChannel time
        // is reached and fadeTimeRemaining is reset to fadeTime.
        fadeTimeRemaining -= deltaMs;
      }

      currentChannelPlayTime += deltaMs;
      // Exceeded our per-channel time, begin to fade.
      if (currentChannelPlayTime + deltaMs > timePerChannel) {
        LXChannelBus currentChannel = standardModeChannels.get(currentPlayingChannel);
        //currentChannel.enabled.setValue(false);
        previousPlayingChannel = currentPlayingChannel;
        ++currentPlayingChannel;
        if (currentPlayingChannel >= standardModeChannels.size()) {
          currentPlayingChannel = 0;
        }
        // logger.info("previous: " + currentChannel.getLabel());
        currentChannel = standardModeChannels.get(currentPlayingChannel);
        // logger.info("current: " + currentChannel.getLabel());
        if (currentChannel != null) {
          currentChannel.enabled.setValue(true);
          currentChannelPlayTime = 0.0; // Reset play time counter.
        }
        fadeTimeRemaining = fadeTime;
        // logger.info("Switching channels:" + currentPlayingChannel);
      }
    }
  }

  public class AudioMonitor implements LXLoopTask {
    public double deltaLastModeSwap = 0.0;
    public boolean audioMode = false;
    public double avgDb = 0.0;
    public long sampleCount = 0;
    public double avgTimeRemaining;

    public void loop(double deltaMs) {
      ConeDown.autoAudio.update();

      // TODO(tracy): We need a number of samples over time and then decide if we should switch
      // based on that.
      if (!autoAudioModeP.isOn()) return;

      double gainDelta = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.GAIN_INCREMENT).getValue();
      double reduceThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.REDUCE_THRESHOLD).getValue();//45.0;
      double gainThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.GAIN_THRESHOLD).getValue(); // 10.0;
      double minThreshold = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.MIN_THRESHOLD).getValue();
      double currentDb = lx.engine.audio.meter.getDecibels();

      avgTimeRemaining -= deltaMs;
      sampleCount++;
      avgDb = avgDb + currentDb/sampleCount;
      if (avgTimeRemaining > 0.0)
        return;
      // We are done averaging samples, reset our variables.
      sampleCount = 0;
      avgTimeRemaining = audioMonitorLevels.getCompoundParameter(UIAudioMonitorLevels.AVG_TIME_SECS).getValue() * 1000.0;

      // We have built our dB level average over some seconds, check to see if we should
      // perform AutoGain Control
      if (avgDb > reduceThreshold) {
        lx.engine.audio.meter.gain.setValue(lx.engine.audio.meter.gain.getValue() - gainDelta);
      } else if (avgDb < gainThreshold) {
        lx.engine.audio.meter.gain.setValue(lx.engine.audio.meter.gain.getValue() + gainDelta);
      }

      if (avgDb > minThreshold
          //&& deltaLastModeSwap > UIAudioMonitorLevels.quietTimeP.getValue() * 1000.0
          && !audioMode) {
        logger.info("Enabling audio mode, avgDb: " + avgDb);
        // TODO: Start fading audio channel up, standard channels down.  Once standard-mode
        // channels are faded down, reset the standard-mode channel switching state.
        UIModeSelector.this.standardMode.setActive(false);
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.audioMode.setActive(true);
        audioMode = true;
        deltaLastModeSwap = 0.0;
      } else if (avgDb < minThreshold
          //&& deltaLastModeSwap > UIAudioMonitorLevels.quietTimeP.getValue() * 1000.0
          && audioMode) {
        logger.info("Disabling audio mode, avgDb: " + avgDb);
        // TODO: Reset standard-mode state.  Start fading current Standard-mode
        // channel up, audio channel down.
        UIModeSelector.this.interactiveMode.setActive(false);
        UIModeSelector.this.instrumentMode.setActive(false);
        UIModeSelector.this.setAudioChannelEnabled(false);
        UIModeSelector.this.standardMode.setActive(true);
        audioMode = false;
        deltaLastModeSwap = 0.0;
      } else {
        deltaLastModeSwap += deltaMs;
      }
      avgDb = 0.0;  // Reset rolling average.
    }
  }
}
