package org.influxdb;

import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.impl.PointImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Stopwatch;

public class PerformanceTests {
	private InfluxDB influxDB;
	private final static int COUNT = 1;
	private final static int POINT_COUNT = 100000;
	private final static int SINGLE_POINT_COUNT = 10000;

	@BeforeClass
	public void setUp() {
		this.influxDB = InfluxDBFactory.connect("http://" + TestUtils.getInfluxIP() + ":8086", "root", "root");
		this.influxDB.setLogLevel(LogLevel.NONE);
	}

	@Test(threadPoolSize = 10, enabled = false)
	public void writeSinglePointPerformance() throws InterruptedException {
		String dbName = "write_" + System.currentTimeMillis();
		this.influxDB.createDatabase(dbName);
		Stopwatch watch = Stopwatch.createStarted();
		for (int j = 0; j < SINGLE_POINT_COUNT; j++) {
			Point point = PointImpl.measurement("cpu")
					.addField("idle", (double) j)
					.addField("user", 2.0 * j)
					.addField("system", 3.0 * j).build();
			this.influxDB.write(dbName, "default", point);
		}
		System.out.println("Single Point Write for " + SINGLE_POINT_COUNT + " writes of  Points took:" + watch);
		this.influxDB.deleteDatabase(dbName);
	}

	@Test(enabled = false)
	public void writePerformance() {
		String dbName = "writepoints_" + System.currentTimeMillis();
		this.influxDB.createDatabase(dbName);

		Stopwatch watch = Stopwatch.createStarted();
		for (int i = 0; i < COUNT; i++) {

			BatchPoints batchPoints = BatchPoints
					.database(dbName)
					
					.retentionPolicy("default")
					.build();
			for (int j = 0; j < POINT_COUNT; j++) {
				Point point = PointImpl
						.measurement("cpu")
						.tag("blubber", "bla")
						.addField("idle", (double) j)
						.addField("user", 2.0 * j)
						.addField("system", 3.0 * j)
						.build();
				batchPoints.point(point);
			}

			this.influxDB.write(batchPoints);
		}
		System.out.println("WritePoints for " + COUNT + " writes of " + POINT_COUNT + " Points took:" + watch);
		this.influxDB.deleteDatabase(dbName);
	}

	@Test(enabled = true)
	public void maxWritePointsPerformance() {
		String dbName = "d";
		this.influxDB.createDatabase(dbName);

		Stopwatch watch = Stopwatch.createStarted();
		for (int i = 0; i < 2000000; i++) {
			Point point = PointImpl.measurement("s").addField("v", 1.0).build();
			this.influxDB.write(dbName, "default", point);
		}
		System.out.println("5Mio points:" + watch);
		this.influxDB.deleteDatabase(dbName);
	}
}
