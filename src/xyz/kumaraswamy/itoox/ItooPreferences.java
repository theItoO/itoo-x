package xyz.kumaraswamy.itoox;

import android.content.Context;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.JsonUtil;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ItooPreferences {

  private final File filesDir;

  public ItooPreferences(Context context) {
    filesDir = context.getFilesDir();
  }

  public ItooPreferences(Context context, String namespace) {
    filesDir = new File(context.getFilesDir(), "/" + namespace + "/");
    filesDir.mkdirs();
  }

  public ItooPreferences write(String name, Object value) throws JSONException {
    String content = JsonUtil.getJsonRepresentation(value);

    File file = new File(filesDir, toHex(name));
    FileOutputStream fileOut = null;
    try {
      fileOut = new FileOutputStream(file);
      fileOut.write(content.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly("Itoo", fileOut);
    }
    return this;
  }

  public Object read(String name, Object defaultValue) {
    File file = new File(filesDir, toHex(name));
    if (!file.exists()) {
      return defaultValue;
    }
    FileInputStream fileInput = null;
    try {
      fileInput = new FileInputStream(file);
      byte[] bytes = new byte[fileInput.available()];
      fileInput.read(bytes);

      return JsonUtil.getObjectFromJson(new String(bytes), true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly("Itoo", fileInput);
    }
  }

  public void delete(String name) {
    File file = new File(filesDir, toHex(name));
    if (file.exists()) {
      file.delete();
    }
  }

  public boolean contains(String name) {
    return new File(filesDir, toHex(name)).exists();
  }

  private static String toHex(String name) {
    StringBuilder hex = new StringBuilder();
    byte[] bytes = name.getBytes();
    for (byte b : bytes) {
      hex.append(String.format("%02X ", b));
    }
    return hex.toString();
  }
}
