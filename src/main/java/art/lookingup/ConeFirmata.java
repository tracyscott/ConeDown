package art.lookingup;

import art.lookingup.ui.UISensorOverride;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.firmata.FirmataMessageFactory;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Encapsulation of all Firmata interactions.  We initialize Firmata on start up.  We also can change the
 * serial port for Firmata in the UI at which point we need to try to re-initialize it.
 */
public class ConeFirmata {
  private static final Logger logger = Logger.getLogger(ConeFirmata.class.getName());

  public static IODevice device;
  public static long[] pinData;
  public static int numPins;
  public static int startPinNum;

  public static void reloadFirmata(String portName, int numberPins, int startPin, List<CompoundParameter> pinParameters) {
    // construct a Firmata device instance
    try {
      if (device != null) device.stop();
    } catch (IOException ioex) {
      logger.info("ConeFirmata: IOException stopping device: " + ioex.getMessage());
    }

    numPins = numberPins;
    pinData = new long[numPins];
    startPinNum = startPin;

    device = new FirmataDevice(portName); // using the name of a port
    // IODevice device = new FirmataDevice(new NetworkTransport("192.168.1.18:4334")); // using a network address
    // subscribe to events using device.addEventListener(...);
    // and/or device.getPin(n).addEventListener(...);
    try {
      logger.info("Initializing Firmata");
      device.start(); // initiate communication to the device
      device.ensureInitializationIsDone(); // wait for initialization is done

      device.sendMessage(FirmataMessageFactory.setSamplingInterval(100));
      logger.info("Device num pins: " + device.getPinsCount());

      for (int i = startPin; i < (startPin + numPins - 1) && i < device.getPinsCount(); i++) {
        Pin pin = device.getPin(i);
        pin.setMode(Pin.Mode.INPUT);
      }

      device.addEventListener(new IODeviceEventListener() {
        @Override
        public void onStart(IOEvent event) {
          // since this moment we are sure that the device is initialized
          // so we can hide initialization spinners and begin doing cool stuff
          logger.info("Firmata device is ready");
        }

        @Override
        public void onStop(IOEvent event) {
          // since this moment we are sure that the device is properly shut down
          logger.info("Firmata device has been stopped");
        }

        @Override
        public void onPinChange(IOEvent event) {
          // here we react to changes of pins' state
          Pin pin = event.getPin();
          if (pin.getIndex() >= startPin && pin.getIndex() < (startPin + numPins)) {
            // System.out.println(String.format("Pin %d got a value of %d", pin.getIndex(), pin.getValue()));
            int sensorIndex = pin.getIndex() - startPin;
            pinData[sensorIndex] = pin.getValue();
            // Set dance floor tile parameter.   If any of the 4 sensors are active then set it to 1.
            int pinBlock = sensorIndex/4;  // This will determine which block of 4 sensors to check.
            // If any sensor is on, then leave it on.  If all sensors are off, turn it off.
            // TODO(tracy): Check the sensor override map to see if we have disabled this sensor because it has broken
            // in a failed on state.  pinBlock will be 0..9.  X = pinBlock % 3, y = pinBlock / 3  Actual sensor number
            // per dance tile is sensorIndex % 4;
            int danceTileX = pinBlock % 3;
            int danceTileY = pinBlock / 3;
            int sensorIndexPerDanceTile = sensorIndex % 4;
            BooleanParameter bp = ConeDown.sensorOverrideUI.getBooleanParameter(
                UISensorOverride.getParameterName(danceTileX, danceTileY, sensorIndexPerDanceTile));
            // logger.info("Checking enabled button: " + UISensorOverride.getParameterName(danceTileX, danceTileY,
            //    sensorIndexPerDanceTile));
            CompoundParameter cp = ConeDown.firmataPortUI.getPinParameter(danceTileX, danceTileY);
            if (bp.getValueb()) {
              if (pinData[pinBlock * 4] > 0 || pinData[pinBlock * 4 + 1] > 0 || pinData[pinBlock * 4 + 2] > 0 || pinData[pinBlock * 4 + 3] > 0) {
                cp.setValue(1);
              } else {
                cp.setValue(0);
              }
            }
          }
        }

        @Override
        public void onMessageReceive(IOEvent event, String message) {
          // here we react to receiving a text message from the device
          logger.info("Firmata: " + message);
        }
      });
    } catch (IOException ioex) {
      logger.info("Firmata IOException ioex: " + ioex.getMessage());
    } catch (InterruptedException iex) {
      logger.info("Firmata: Interrupted Exception: " + iex.getMessage());
    }


  }
}
