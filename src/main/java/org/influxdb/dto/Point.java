package org.influxdb.dto;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Representation of a InfluxDB database Point.
 * 
 * @author stefan.majer [at] gmail.com
 * 
 */
public interface Point {
	
	/**
	 * calculate the lineprotocol entry for a single Point.
	 * 
	 * Documentation is WIP : https://github.com/influxdb/influxdb/pull/2997
	 * 
	 * https://github.com/influxdb/influxdb/blob/master/tsdb/README.md
	 *
	 * @return the String without newLine.
	 */
	public String lineProtocol();
	
	/**
	 * For use in batching to write directly to the stream
	 * @param sw
	 */
	public void writeTo(OutputStreamWriter sw) throws IOException;

}
