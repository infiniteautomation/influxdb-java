package org.influxdb.dto;

import retrofit.mime.TypedOutput;

/**
 * Representation of a InfluxDB database Point.
 * 
 * @author stefan.majer [at] gmail.com
 * 
 */
public interface Point extends TypedOutput{
	
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

}
