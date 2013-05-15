package org.jdleesmiller.trackjd.collector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jdleesmiller.trackjd.data.AbstractPoint;

import com.loopj.android.http.RequestParams;

/**
 * Store measurements. At the moment, these are all stored in memory, which is
 * clearly problematic. We should persist to disk (or maybe database), ideally
 * in a binary format to minimize pointless work. Or we could spill to CSV and
 * then use a SequenceInputStream to read that into the request directly -- that
 * is probably better.
 */
public class CollectorBuffer<Datum extends AbstractPoint> {
  final List<Datum> buffer;

  public CollectorBuffer(int maxData) {
    this.buffer = new ArrayList<Datum>();
  }

  public void store(Datum datum) {
    this.buffer.add(datum);
  }

  public boolean isEmpty() {
    // TODO must check disk
    return this.buffer.isEmpty();
  }

  /**
   * Add the CSV data as a file in a multi-part POST.
   * 
   * @param name
   * @param params
   * @param maxRows
   * @return number of data rows in the file
   */
  public int addCsvToPost(String name, RequestParams params, int maxRows) {
    if (this.isEmpty()) {
      return 0;
    } else {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(os);
      this.buffer.get(0).printCsvHeader(ps);
      ps.print('\n');

      int rows = printCsv(ps, maxRows);
      params.put(name, new ByteArrayInputStream(os.toByteArray()), name
          + ".csv");
      return rows;
    }
  }

  /**
   * Print CSV header with trailing newline. Call only if there is data in the
   * buffer.
   * 
   * @param ps
   * @return number of data rows in the file
   */
  private int printCsv(PrintStream ps, int maxRows) {
    int n = Math.min(maxRows, this.buffer.size());
    for (int i = 0; i < n; ++i) {
      this.buffer.get(i).printCsvData(ps);
      ps.print('\n');
    }
    return n;
  }

  public void clear(int dataUploaded) {
    // TODO need to handle disk
    for (; dataUploaded > 0; --dataUploaded)
      buffer.remove(0);
  }
}
