package org.influxdb.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.impl.PointImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;

/**
 * Test for the Point DTO.
 *
 * @author stefan.majer [at] gmail.com
 *
 */
public class PointTest {

	/**
	 * Test that lineprotocol is conformant to:
	 * 
	 * https://github.com/influxdb/influxdb/blob/master/tsdb/README.md
	 *
	 */
	@Test
	public void lineProtocol() {
		Point point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", 1.0).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=1.0 1");

		point = PointImpl.measurement("test,1").time(1, TimeUnit.NANOSECONDS).addField("a", 1.0).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test\\,1 a=1.0 1");

		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", "A").build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=\"A\" 1");

		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", "A\"B").build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=\"A\\\"B\" 1");

		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", "A\"B\"C").build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=\"A\\\"B\\\"C\" 1");

		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", "A B C").build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=\"A B C\" 1");

		point = PointImpl
				.measurement("test")
				.time(1, TimeUnit.NANOSECONDS)
				.addField("a", "A\"B")
				.addField("b", "D E \"F")
				.build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=\"A\\\"B\",b=\"D E \\\"F\" 1");

		//Integer type
		point = PointImpl.measurement("inttest").time(1, TimeUnit.NANOSECONDS).addField("a", (Integer)1).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("inttest a=1i 1");

		point = PointImpl.measurement("inttest,1").time(1, TimeUnit.NANOSECONDS).addField("a", (Integer)1).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("inttest\\,1 a=1i 1");

		point = PointImpl.measurement("inttest,1").time(1, TimeUnit.NANOSECONDS).addField("a", 1L).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("inttest\\,1 a=1i 1");

		point = PointImpl.measurement("inttest,1").time(1, TimeUnit.NANOSECONDS).addField("a", BigInteger.valueOf(100)).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("inttest\\,1 a=100i 1");
	}

	/**
	 * Test for ticket #44
	 */
	@Test(enabled = true)
	public void testTicket44() {
		Point point = PointImpl.measurement("test").time(1, TimeUnit.MICROSECONDS).addField("a", 1.0).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=1.0 1000");

		point = PointImpl.measurement("test").time(1, TimeUnit.MILLISECONDS).addField("a", 1.0).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=1.0 1000000");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", 1.0).build();
			BatchPoints batchPoints = BatchPoints.database("db").build();
			batchPoints.point(point);
			batchPoints.writeTo(baos);
			assertThat(new String(baos.toByteArray(), "UTF-8")).asString().isEqualTo("test a=1.0 1\n");
			baos.reset();
	
			point = PointImpl.measurement("test").time(1, TimeUnit.MICROSECONDS).addField("a", 1.0).build();
			batchPoints = BatchPoints.database("db").build();
			batchPoints.point(point);
			batchPoints.writeTo(baos);
			assertThat(new String(baos.toByteArray(), "UTF-8")).asString().isEqualTo("test a=1.0 1000\n");
			baos.reset();
			
			point = PointImpl.measurement("test").time(1, TimeUnit.MILLISECONDS).addField("a", 1.0).build();
			batchPoints = BatchPoints.database("db").build();
			batchPoints.point(point);
			batchPoints.writeTo(baos);
			assertThat(new String(baos.toByteArray(), "UTF-8")).asString().isEqualTo("test a=1.0 1000000\n");
			baos.reset();
			
			point = PointImpl.measurement("test").addField("a", 1.0).time(1, TimeUnit.MILLISECONDS).build();
			batchPoints = BatchPoints.database("db").build();
			batchPoints = batchPoints.point(point);
			batchPoints.writeTo(baos);
			assertThat(new String(baos.toByteArray(), "UTF-8")).asString().isEqualTo("test a=1.0 1000000\n");
		}catch (IOException e) {
			Assert.fail("Problem", e);
		}


	}

	/**
	 * Test for ticket #54
	 */
	@Test
	public void testTicket54() {
		Byte byteNumber = 100;
		Point point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", byteNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100i 1");

		int intNumber = 100000000;
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", intNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000i 1");

		Integer integerNumber = 100000000;
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", integerNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000i 1");

		AtomicInteger atomicIntegerNumber = new AtomicInteger(100000000);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", atomicIntegerNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000i 1");

		Long longNumber = 1000000000000000000L;
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", longNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=1000000000000000000i 1");

		AtomicLong atomicLongNumber = new AtomicLong(1000000000000000000L);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", atomicLongNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=1000000000000000000i 1");

		BigInteger bigIntegerNumber = BigInteger.valueOf(100000000);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", bigIntegerNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000i 1");

		Double doubleNumber = Double.valueOf(100000000.0001);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", doubleNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000.0001 1");

		Float floatNumber = Float.valueOf(0.1f);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", floatNumber).build();
		assertThat(point.lineProtocol()).asString().startsWith("test a=0.10");

		BigDecimal bigDecimalNumber = BigDecimal.valueOf(100000000.00000001);
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).addField("a", bigDecimalNumber).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test a=100000000.00000001 1");
	}
	
	@Test
	public void testEscapingOfKeysAndValues() {
		// Test escaping of spaces
		Point point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).tag("foo", "bar baz").addField( "a", 1.0 ).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test,foo=bar\\ baz a=1.0 1");
 
		// Test escaping of commas
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).tag("foo", "bar,baz").addField( "a", 1.0 ).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test,foo=bar\\,baz a=1.0 1");

		// Test escaping of equals sign
		point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).tag("foo", "bar=baz").addField( "a", 1.0 ).build();
		assertThat(point.lineProtocol()).asString().isEqualTo("test,foo=bar\\=baz a=1.0 1");
	}

	@Test
	public void testDeprecatedFieldMethodOnlyProducesFloatingPointValues() {
		
		Object[] ints = {(byte) 1, (short) 1, (int) 1, (long) 1, BigInteger.ONE};
		
		for (Object intExample : ints) {
			Point point = PointImpl.measurement("test").time(1, TimeUnit.NANOSECONDS).field("a", intExample ).build();
			assertThat(point.lineProtocol()).asString().isEqualTo("test a=1.0 1");
		}

	}
	/**
	 * Test for issue #117.
	 */
	@Test
	public void testIgnoreNullPointerValue() {
		// Test omission of null values
		PointImpl.Builder pointBuilder = PointImpl.measurement("nulltest").time(1, TimeUnit.NANOSECONDS).tag("foo", "bar");

		pointBuilder.field("field1", "value1");
		pointBuilder.field("field2", (Number) null);
		pointBuilder.field("field3", (Integer) 1);

		Point point = pointBuilder.build();

		assertThat(point.lineProtocol()).asString().isEqualTo("nulltest,foo=bar field1=\"value1\",field3=1.0 1");
	}
	
	/**
	 * Tests for issue #110
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddingTagsWithNullNameThrowsAnError() {
		PointImpl.measurement("dontcare").tag(null, "DontCare");
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddingTagsWithNullValueThrowsAnError() {
		PointImpl.measurement("dontcare").tag("DontCare", null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddingMapOfTagsWithNullNameThrowsAnError() {
		Map<String, String> map = Maps.newHashMap();
		map.put(null, "DontCare");
		PointImpl.measurement("dontcare").tag(map);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddingMapOfTagsWithNullValueThrowsAnError() {
		Map<String, String> map = Maps.newHashMap();
		map.put("DontCare", null);
		PointImpl.measurement("dontcare").tag(map);
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void testNullValueThrowsExceptionViaAddField() {
		PointImpl.measurement("dontcare").addField("field", (String) null);
	}
}
