package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.voomdoon.logging.LogLevel;
import de.voomdoon.testing.file.TempFileExtension;
import de.voomdoon.testing.file.TempInputFile;
import de.voomdoon.testing.file.TempOutputDirectory;
import de.voomdoon.testing.file.WithTempInputFiles;
import de.voomdoon.testing.logging.tests.LoggingCheckingTestBase;
import de.voomdoon.util.kml.io.KmlReader;

/**
 * Tests for {@link OsmToKml}.
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
class OsmToKmlTest extends LoggingCheckingTestBase {

	/**
	 * DOCME add JavaDoc for OsmToKmlTest
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	@WithTempInputFiles(extension = "pbf")
	class InputOutputMappingTest extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_input_multiple_output_single(@TempInputFile File input1, @TempInputFile File input2,
				@TempOutputDirectory File output) throws Exception {
			logTestStart();

			withInputs(Map.of(input1, "node_1566942192.osm.pbf", input2, "node_8400710442.osm.pbf"));

			Kml actual = run(output);

			assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
					.hasSize(2);
		}
	}

	/**
	 * Tests for {@link OsmToKml#run()}.
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	@WithTempInputFiles(extension = "pbf")
	class RunTest extends TestBase {

		/**
		 * @since 0.1.0
		 */
		private static final Offset<Double> EPSILON = within(1E-7);

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_noInput() throws Exception {
			logTestStart();

			assertThatThrownBy(() -> osmToKml.run())//
					.isInstanceOf(IllegalStateException.class).hasMessageContaining("input");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_noOutput(@TempInputFile File input) throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			assertThatThrownBy(() -> osmToKml.run())//
					.isInstanceOf(IllegalStateException.class).hasMessageContaining("output");
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_nodeBecomesPlacemark(@TempInputFile File input, @TempOutputDirectory File output)
				throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertPlacemark(actual);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_nodePlacemarkGeometryIsPoint(@TempInputFile File input, @TempOutputDirectory File output)
				throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertPoint(actual);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_nodePlacemarkGeometryPointCoordinatesAreCorrect(@TempInputFile File input,
				@TempOutputDirectory File output) throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertCoordinate(assertPoint(actual).extracting(Point::getCoordinates)
					.asInstanceOf(InstanceOfAssertFactories.LIST).singleElement(), 52.5237871, 13.4123426);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_nodePlacemarkGeometryPointCoordinatesAreCorrect2(@TempInputFile File input,
				@TempOutputDirectory File output) throws Exception {
			logTestStart();

			withInputs(input, "node_8400710442.osm.pbf");

			Kml actual = run(output);

			assertCoordinate(assertPoint(actual).extracting(Point::getCoordinates)
					.asInstanceOf(InstanceOfAssertFactories.LIST).singleElement(), 52.5186776, 13.4075684);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_rootFeatureIsDocument(@TempInputFile File input, @TempOutputDirectory File output)
				throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertDocument(actual);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempInputFile File input, @TempOutputDirectory File output) throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertThat(actual).isNotNull();
		}

		/**
		 * DOCME add JavaDoc for method assertCoordinate
		 * 
		 * @param coordinateAssert
		 * @param expectedLatitude
		 * @param expectedLongitude
		 * @since 0.1.0
		 */
		private void assertCoordinate(ObjectAssert<? extends Object> coordinateAssert, double expectedLatitude,
				double expectedLongitude) {
			coordinateAssert.isInstanceOfSatisfying(Coordinate.class, coordinate -> {
				assertThat(coordinate).extracting(Coordinate::getLatitude, as(DOUBLE)).isCloseTo(expectedLatitude,
						EPSILON);
				assertThat(coordinate).extracting(Coordinate::getLongitude, as(DOUBLE)).isCloseTo(expectedLongitude,
						EPSILON);
			});
		}

		/**
		 * @param actual
		 *            {@link Kml}
		 * @return {@link ObjectAssert} for single {@link Placemark}
		 * @since 0.1.0
		 */
		private ObjectAssert<Placemark> assertPlacemark(Kml actual) {
			return assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
					.singleElement().isInstanceOf(Placemark.class)
					.asInstanceOf(InstanceOfAssertFactories.type(Placemark.class));
		}

		/**
		 * @param actual
		 *            {@link Kml}
		 * @return {@link ObjectAssert} for {@link Point} of first {@link Placemark}
		 * @since 0.1.0
		 */
		private ObjectAssert<Point> assertPoint(Kml actual) {
			return assertPlacemark(actual).extracting(Placemark::getGeometry).describedAs("geometry")
					.isInstanceOf(Point.class).asInstanceOf(InstanceOfAssertFactories.type(Point.class));
		}

	}

	/**
	 * Tests for {@link OsmToKml#withInputs(List)}.
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	@WithTempInputFiles(extension = "pbf")
	class WithInputsTets extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_IAE_empty(@TempInputFile String input) throws Exception {
			logTestStart();

			assertThatThrownBy(() -> osmToKml.withInputs(List.of())).isInstanceOf(IllegalArgumentException.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempInputFile String input) throws Exception {
			logTestStart();

			assertDoesNotThrow(() -> osmToKml.withInputs(List.of(input)));
		}
	}

	/**
	 * Tests for {@link OsmToKml#withInputs(List)}.
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	class WithOutputsTets extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempOutputDirectory String input) throws Exception {
			logTestStart();

			assertDoesNotThrow(() -> osmToKml.withOutput(input));
		}
	}

	/**
	 * DOCME add JavaDoc for OsmToKmlTest
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	private class TestBase extends de.voomdoon.testing.tests.TestBase {

		/**
		 * @since 0.1.0
		 */
		protected OsmToKml osmToKml = new OsmToKml();

		/**
		 * @param actual
		 *            {@link Kml}
		 * @return {@link ObjectAssert} for {@link Document}
		 * @since 0.1.0
		 */
		protected ObjectAssert<Document> assertDocument(Kml actual) {
			return assertThat(actual).extracting(Kml::getFeature).describedAs("root feature")
					.isInstanceOf(Document.class).asInstanceOf(InstanceOfAssertFactories.type(Document.class));
		}

		/**
		 * DOCME add JavaDoc for method run
		 * 
		 * @param output
		 * @return
		 * @throws IOException
		 * @throws InvalidInputFileException
		 * @since 0.1.0
		 */
		protected Kml run(File output) throws IOException, InvalidInputFileException {
			osmToKml.withOutput(output.getAbsolutePath());

			osmToKml.run();

			String outputFile = output + "/default.kml";

			logger.debug("output:\n" + Files.readString(Path.of(outputFile)));

			return new KmlReader().read(outputFile);
		}

		/**
		 * DOCME add JavaDoc for method withInputs
		 * 
		 * @param input
		 * @param resource
		 * @throws InvalidInputFileException
		 * @since 0.1.0
		 */
		protected void withInputs(File input, String resource) throws InvalidInputFileException {
			copyResourceToInputFile(resource, input);

			osmToKml.withInputs(List.of(input.getAbsolutePath()));
		}

		/**
		 * DOCME add JavaDoc for method withInputs
		 * 
		 * @param inputsWithResource
		 * @throws InvalidInputFileException
		 * @since 0.1.0
		 */
		protected void withInputs(Map<File, String> inputsWithResource) throws InvalidInputFileException {
			for (Entry<File, String> entry : inputsWithResource.entrySet()) {
				copyResourceToInputFile(entry.getValue(), entry.getKey());
			}

			osmToKml.withInputs(inputsWithResource.keySet().stream().map(File::getAbsolutePath).toList());
		}

		/**
		 * @since 0.1.0
		 */
		@AfterEach
		void afterEach_removeAcceptedLogging() {
			OsmToKmlTest.this.getLogCache().removeEvents(LogLevel.WARN, Pattern.compile(".*not implemented.*"));
		}
	}

	/**
	 * DOCME add JavaDoc for method copyResourceToInputFile
	 * 
	 * @param resource
	 * @param input
	 * @since 0.1.0
	 */
	public static void copyResourceToInputFile(String resource, File input) {
		try (InputStream inputStream = OsmToKmlTestV1_refactor_extractTestBase.class
				.getResourceAsStream("/input/" + resource)) {
			try {
				Files.copy(inputStream, input.toPath());
			} catch (IOException e) {
				// TODO implement error handling
				throw new RuntimeException("Error at 'withInputs': " + e.getMessage(), e);
			}
		} catch (IOException e) {
			// TODO implement error handling
			throw new RuntimeException("Error at 'withInputs': " + e.getMessage(), e);
		}
	}
}
