package org.thereflectproject.trackjd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;

/**
 * Utility methods for identifying an installation of the software. If none
 * of our device identifiers are available, this is a useful fallback. The
 * approach here is from
 * http://android-developers.blogspot.co.uk/2011/03/identifying-app-installations.html
 * Note that if the user re-installs the app, we cannot re-identify them after
 * the install.
 */
public class Installation {
  private static final String INSTALLATION = "INSTALLATION";
  private static String sID = null;

  /**
   * Get the unique identifier for this installation.
   * 
   * @param context
   * 
   * @return not null
   */
  public synchronized static String id(Context context) {
    if (sID == null) {
      File installation = new File(context.getFilesDir(), INSTALLATION);
      try {
        if (!installation.exists())
          writeInstallationFile(installation);
        sID = readInstallationFile(installation);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return sID;
  }

  private static String readInstallationFile(File installation)
    throws IOException {
    RandomAccessFile f = new RandomAccessFile(installation, "r");
    byte[] bytes = new byte[(int) f.length()];
    f.readFully(bytes);
    f.close();
    return new String(bytes);
  }

  private static void writeInstallationFile(File installation)
    throws IOException {
    FileOutputStream out = new FileOutputStream(installation);
    String id = UUID.randomUUID().toString();
    out.write(id.getBytes());
    out.close();
  }
}
