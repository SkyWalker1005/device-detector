package io.github.mngsk.devicedetector.device;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.github.mngsk.devicedetector.Detection;
import io.github.mngsk.devicedetector.DeviceDetector;
import io.github.mngsk.devicedetector.DeviceDetector.DeviceDetectorBuilder;

public class NotebookTest {

	private static List<DeviceFixture> FIXTURES;

	private static DeviceDetector DD;

	@BeforeAll
	public static void beforeAll() {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		objectMapper.addMixIn(Device.class, DeviceMixin.class);

		String fixtureFile = "fixtures/device/notebook.yml";
		InputStream inputStream = NotebookTest.class.getClassLoader()
				.getResourceAsStream(fixtureFile);
		CollectionType listType = objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DeviceFixture.class);
		try {
			FIXTURES = objectMapper.readValue(inputStream, listType);
		} catch (IOException e) {
			throw new RuntimeException("Could not load " + fixtureFile, e);
		}

		DD = new DeviceDetectorBuilder().disableEverything().enableNotebooks()
				.build();
	}

	public static Stream<DeviceFixture> fixtureProvider() {
		return FIXTURES.stream();
	}

	@ParameterizedTest
	@MethodSource("fixtureProvider")
	public void fixtures(DeviceFixture fixture) {
		Detection d = DD.detect(fixture.getUserAgent());
		// Notebooks are considered desktops, but we cannot use the isDesktop()
		// method because it takes into account the operating system and browser.
		assertEquals(d.getDevice().map(device -> device.getType()).orElse(null),
				"desktop", fixture.getUserAgent());
		assertEquals(fixture.getDevice(), d.getDevice().get(),
				fixture.getUserAgent());
	}

}
