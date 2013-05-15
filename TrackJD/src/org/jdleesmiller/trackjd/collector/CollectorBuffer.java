package org.jdleesmiller.trackjd.collector;

import java.util.ArrayList;
import java.util.List;

import org.jdleesmiller.trackjd.data.AbstractPoint;

public class CollectorBuffer<Datum extends AbstractPoint> {
  final List<Datum> buffer;
  
  public CollectorBuffer(int maxData) {
    this.buffer = new ArrayList<Datum>();
  }
  
  public void store(Datum datum) {
    this.buffer.add(datum);
  }
}
