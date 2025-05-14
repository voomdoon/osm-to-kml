package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.InstanceOfAssertFactories.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
import de.voomdoon.testing.file.TempInputDirectory;
import de.voomdoon.testing.file.TempInputFile;
import de.voomdoon.testing.file.TempOutputDirectory;
import de.voomdoon.testing.file.WithTempInputFiles;
import de.voomdoon.testing.logging.tests.LoggingCheckingTestBase;
import de.voomdoon.util.file.FileTreeFormatter;
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
	@WithTempInputFiles(extension = "osm.pbf")
	class InputOutputMappingTest extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_inputMultipleFiles_outputSingleDirectory_resultsInMultipleFiles(@TempInputFile File inputFile1,
				@TempInputFile File inputFile2, @TempOutputDirectory File outputDirectory) throws Exception {
			logTestStart();

			withInputs(Map.of(inputFile1, "node_1566942192.osm.pbf", inputFile2, "node_8400710442.osm.pbf"));

			run(outputDirectory);

			assertThat(outputDirectory).isDirectoryContaining(file -> file.getName()
					.equals(inputFile1.getName().substring(0, inputFile1.getName().indexOf('.')) + ".kml"));
			assertThat(outputDirectory).isDirectoryContaining(file -> file.getName()
					.equals(inputFile2.getName().substring(0, inputFile2.getName().indexOf('.')) + ".kml"));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_inputSingleDirectoryWithMultipleFiles_outputSingleDirectory_resultsInSingleFile(
				@TempInputDirectory File inputDirectory, @TempOutputDirectory File outputDirectory) throws Exception {
			logTestStart();

			inputDirectory.mkdirs();
			copyResourceToInputFile("node_1566942192.osm.pbf", new File(inputDirectory + "/1.osm.pbf"));
			copyResourceToInputFile("node_8400710442.osm.pbf", new File(inputDirectory + "/2.osm.pbf"));
			osmToKml.withInputs(List.of(inputDirectory.getAbsolutePath()));

			Kml actual = run(outputDirectory);

			assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
					.hasSize(2);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_inputSingleFile_onePipeline_outputSingleDirectory_resultsInOneFile(@TempInputFile File inputFile,
				@TempOutputDirectory File outputDirectory) throws Exception {
			logTestStart();

			OsmToKmlPipeline pipeline = new OsmToKmlPipeline();
			pipeline.setName("test-pipeline");

			withInputs(Map.of(inputFile, "node_1566942192.osm.pbf"));
			osmToKml.withPipelines(List.of(pipeline));

			runDirectory(outputDirectory);

			Kml actual = read(outputDirectory, "test-pipeline.kml");

			assertCoordinate(assertPoint(actual).extracting(Point::getCoordinates)
					.asInstanceOf(InstanceOfAssertFactories.LIST).singleElement(), 52.5237871, 13.4123426);
		}

		/**
		 * DOCME add JavaDoc for method runDirectory
		 * 
		 * @param outputDirectory
		 * @throws InvalidInputFileException
		 * @throws IOException
		 * @since 0.1.0
		 */
		private void runDirectory(File output) throws IOException, InvalidInputFileException {
			osmToKml.withOutput(output.getAbsolutePath());

			osmToKml.run();
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
		private static final Offset<Double> EPSILON = within(1E-7);

		/**
		 * @since 0.1.0
		 */
		protected OsmToKml osmToKml = new OsmToKml();

		/**
		 * DOCME add JavaDoc for method assertCoordinate
		 * 
		 * @param coordinateAssert
		 * @param expectedLatitude
		 * @param expectedLongitude
		 * @since 0.1.0
		 */
		protected void assertCoordinate(ObjectAssert<? extends Object> coordinateAssert, double expectedLatitude,
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
		 * @return {@link ObjectAssert} for {@link Document}
		 * @since 0.1.0
		 */
		protected ObjectAssert<Document> assertDocument(Kml actual) {
			return assertThat(actual).extracting(Kml::getFeature).describedAs("root feature")
					.isInstanceOf(Document.class).asInstanceOf(InstanceOfAssertFactories.type(Document.class));
		}

		/**
		 * @param actual
		 *            {@link Kml}
		 * @return {@link ObjectAssert} for single {@link Placemark}
		 * @since 0.1.0
		 */
		protected ObjectAssert<Placemark> assertPlacemark(Kml actual) {
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
		protected ObjectAssert<Point> assertPoint(Kml actual) {
			return assertPlacemark(actual).extracting(Placemark::getGeometry).describedAs("geometry")
					.isInstanceOf(Point.class).asInstanceOf(InstanceOfAssertFactories.type(Point.class));
		}

		/**
		 * DOCME add JavaDoc for method read
		 * 
		 * @param outputDirectory
		 * @param file
		 * @return
		 * @throws IOException
		 * @since DOCME add inception version number
		 */
		protected Kml read(File outputDirectory, String file) throws IOException {
			try {
				return new KmlReader().read(outputDirectory + "/" + file);
			} catch (FileNotFoundException e) {
				throw new FileNotFoundException(
						e.getMessage() + "\n" + new FileTreeFormatter().format(outputDirectory));
			}
		}

		/**
		 * DOCME add JavaDoc for method run
		 * 
		 * @param outputDirectory
		 * @return
		 * @throws IOException
		 * @throws InvalidInputFileException
		 * @since 0.1.0
		 */
		protected Kml run(File outputDirectory) throws IOException, InvalidInputFileException {
			osmToKml.withOutput(outputDirectory.getAbsolutePath());

			osmToKml.run();

			File outputFile = new File(outputDirectory + "/default.kml");

			if (!outputFile.isFile()) {
				return null;
			}

			logger.debug("output:\n" + Files.readString(outputFile.toPath()));

			return read(outputDirectory, "default.kml");
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
		try (InputStream inputStream = OsmToKmlTest.class.getResourceAsStream("/input/" + resource)) {
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
