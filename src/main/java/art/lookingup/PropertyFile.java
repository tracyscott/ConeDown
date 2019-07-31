package art.lookingup;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Utility class for saving and loading of properties.
 */
public class PropertyFile {
  private static final Logger logger = Logger.getLogger(PropertyFile.class.getName());

  public String filename;
  public JsonObject obj;
  public static final int TYPE_STRING = 0;
  public static final int TYPE_INT = 1;
  public static final int TYPE_FLOAT = 2;
  public static final int TYPE_DOUBLE = 3;

  public PropertyFile(String filename) {
    this.filename = filename;
    obj = new JsonObject();
  }

  public boolean exists() {
    File f = new File(filename);
    return f.exists();
  }

  public Set<String> keys() {
    Set<String> keys = new HashSet<String>();
    for (Map.Entry<String, JsonElement> entry: obj.entrySet()) {
      keys.add(entry.getKey());
    }
    return keys;
  }

  public void save() throws IOException {
    File file = new File(filename);
    try {
      JsonWriter writer = new JsonWriter(new FileWriter(file));
      writer.setIndent("  ");
      new GsonBuilder().create().toJson(obj, writer);
      writer.close();
      logger.info("PropertyFile saved successfully to " + file.toString());
    } catch (IOException iox) {
      logger.info(iox.getMessage());
      throw iox;
    }
  }

  /**
   * Load the JSON object file.
   */
  public void load() throws IOException {
    File file = new File(filename);
    FileReader fr = null;
    try {
      fr = new FileReader(file);
      obj = new Gson().fromJson(fr, JsonObject.class);
      logger.info("PropertyFile loaded successfully from " + file.toString());
    } catch (IOException iox) {
      logger.info("Could not load property file: " + iox.getMessage());
      throw iox;
    } finally {
      if (fr != null) {
        try {
          fr.close();
        } catch (IOException ignored) {  }
      }
    }
  }

  public void setObj(String key, JsonObject leaf) {
    obj.add(key, leaf);
  }

  public JsonObject getObj(String key) throws NotFound {
    // We expect everything to be objects, so JsonObject == JsonElement.
    if (obj.has(key))
      return (JsonObject)obj.get(key);
    else
      throw new NotFound();
  }

  public void setString(String key, String value) {
    JsonObject leaf = new JsonObject();
    leaf.addProperty("v", value);
    leaf.addProperty("t", TYPE_STRING);
    obj.add(key, leaf);
  }

  public String getString(String key) throws NotFound {
    if (obj.has(key))
      return obj.get(key).getAsString();
    else
      throw new NotFound();
  }

  public void setFloat(String key, float value) {
    JsonObject leaf = new JsonObject();
    leaf.addProperty("v", value);
    leaf.addProperty("t", TYPE_FLOAT);
    obj.add(key, leaf);
  }

  public float getFloat(String key) throws NotFound {
    if (obj.has(key))
      return obj.get(key).getAsFloat();
    else
      throw new NotFound();
  }

  public void setDouble(String key, double value) {
    obj.addProperty(key, value);
  }

  public double getDouble(String key) throws NotFound {
    if (obj.has(key))
      return obj.get(key).getAsDouble();
    else
      throw new NotFound();
  }

  public static class NotFound extends Exception {

  }
}
