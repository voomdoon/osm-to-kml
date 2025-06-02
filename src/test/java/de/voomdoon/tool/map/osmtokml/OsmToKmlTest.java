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
import de.voomdoon.testing.file.WithTempInputDirectories;
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
	 * Tests for the mapping of input files and pipelines to output files.
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
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputFiles(extension = "osm.pbf")
		class Scenario1_singleInputFile_singlePipeline_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test(@TempInputFile File inputFile, @TempOutputDirectory File outputDirectory) throws Exception {
				logTestStart();

				withInputs(Map.of(inputFile, "node_1566942192.osm.pbf"));

				OsmToKmlPipeline pipeline = new OsmToKmlPipeline();
				pipeline.setName("test-pipeline");
				osmToKml.withPipelines(List.of(pipeline));

				Kml actual = runDirectory(outputDirectory, "test-pipeline.kml");

				assertNode1566942192coordinates(actual);
			}
		}

		/**
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputFiles(extension = "osm.pbf")
		class Scenario2_singleInputFile_multiplePipelines_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test(@TempInputFile File inputFile, @TempOutputDirectory File outputDirectory) throws Exception {
				logTestStart();

				withInputs(Map.of(inputFile, "node_1566942192.osm.pbf"));

				osmToKml.withPipelines(List.of(//
						new OsmToKmlPipeline().setName("test-pipeline1"),
						new OsmToKmlPipeline().setName("test-pipeline2")));

				runDirectory(outputDirectory);

				Kml actual = read(outputDirectory, "test-pipeline1@"
						+ inputFile.getName().substring(0, inputFile.getName().length() - 8) + ".kml");
				assertNode1566942192coordinates(actual);

				actual = read(outputDirectory, "test-pipeline2@"
						+ inputFile.getName().substring(0, inputFile.getName().length() - 8) + ".kml");
				assertNode1566942192coordinates(actual);
			}
		}

		/**
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputFiles(extension = "osm.pbf")
		class Scenario3_multipleInputFiles_singlePipeline_aggregatedOutput_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test(@TempInputFile File inputFile1, @TempInputFile File inputFile2,
					@TempOutputDirectory File outputDirectory) throws Exception {
				logTestStart();

				withInputs(Map.of(inputFile1, "node_1566942192.osm.pbf", inputFile2, "node_8400710442.osm.pbf"));

				OsmToKmlPipeline pipeline = new OsmToKmlPipeline();
				pipeline.setName("test-pipeline");
				osmToKml.withPipelines(List.of(pipeline));

				Kml actual = runDirectory(outputDirectory, "test-pipeline.kml");

				assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
						.hasSize(2);
			}
		}

		/**
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputFiles(extension = "osm.pbf")
		class Scenario4_multipleInputFiles_multiplePipelines_aggregatedOutput_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test(@TempInputFile File inputFile1, @TempInputFile File inputFile2,
					@TempOutputDirectory File outputDirectory) throws Exception {
				logTestStart();

				withInputs(Map.of(inputFile1, "node_1566942192.osm.pbf", inputFile2, "node_8400710442.osm.pbf"));

				osmToKml.withPipelines(List.of(//
						new OsmToKmlPipeline().setName("test-pipeline1"),
						new OsmToKmlPipeline().setName("test-pipeline2")));

				runDirectory(outputDirectory);

				Kml actual = read(outputDirectory, "test-pipeline1.kml");
				assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
						.hasSize(2);

				actual = read(outputDirectory, "test-pipeline2.kml");
				assertDocument(actual).extracting(Document::getFeature).asInstanceOf(InstanceOfAssertFactories.LIST)
						.hasSize(2);
			}
		}

		/**
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputDirectories(create = true)
		class Scenario5_singleInputDirectory_singlePipeline_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test_mutilpeFiles(@TempInputDirectory File inputDirectory, @TempOutputDirectory File outputDirectory)
					throws Exception {
				logTestStart();

				copyResourceToInputFile("node_1566942192.osm.pbf", new File(inputDirectory + "/1.osm.pbf"));
				copyResourceToInputFile("node_8400710442.osm.pbf", new File(inputDirectory + "/2.osm.pbf"));
				osmToKml.withInputs(List.of(inputDirectory.getAbsolutePath()));

				OsmToKmlPipeline pipeline = new OsmToKmlPipeline();
				pipeline.setName("test-pipeline");
				osmToKml.withPipelines(List.of(pipeline));

				runDirectory(outputDirectory);

				Kml actual = read(outputDirectory, "test-pipeline@1.kml");
				assertNode1566942192coordinates(actual);

				actual = read(outputDirectory, "test-pipeline@2.kml");
				assertNode8400710442coordinates(actual);
			}
		}

		/**
		 * @author André Schulz
		 *
		 * @since 0.1.0
		 */
		@Nested
		@ExtendWith(TempFileExtension.class)
		@WithTempInputDirectories(create = true)
		class Scenario6_singleInputDirectory_mutiplePipelines_Test extends TestBase {

			/**
			 * @since 0.1.0
			 */
			@Test
			void test_singleFile(@TempInputDirectory File inputDirectory, @TempOutputDirectory File outputDirectory)
					throws Exception {
				logTestStart();

				copyResourceToInputFile("node_1566942192.osm.pbf", new File(inputDirectory + "/single.osm.pbf"));
				osmToKml.withInputs(List.of(inputDirectory.getAbsolutePath()));

				osmToKml.withPipelines(List.of(//
						new OsmToKmlPipeline().setName("test-pipeline1"),
						new OsmToKmlPipeline().setName("test-pipeline2")));

				runDirectory(outputDirectory);

				Kml actual = read(outputDirectory, "test-pipeline1/single.kml");
				assertNode1566942192coordinates(actual);

				actual = read(outputDirectory, "test-pipeline2/single.kml");
				assertNode1566942192coordinates(actual);
			}
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
		@Override
		@AfterEach
		void afterEach_removeAcceptedLogging() {
			super.afterEach_removeAcceptedLogging();

			OsmToKmlTest.this.getLogCache().removeEvents(LogLevel.WARN,
					Pattern.compile(".*running default pipeline.*"));
		}

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

			assertNode8400710442coordinates(actual);
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
	@WithTempInputDirectories(create = true)
	class WithInputsTets extends TestBase {

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_IAE_empty(@TempInputFile String input) throws Exception {
			logTestStart();

			assertThatThrownBy(() -> osmToKml.withInputs(List.of())).isInstanceOf(IllegalArgumentException.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_IAE_multipleDirectoreis(@TempInputDirectory String directory1,
				@TempInputDirectory String directory2) throws Exception {
			logTestStart();

			assertThatThrownBy(() -> osmToKml.withInputs(List.of(directory1, directory2)))
					.isInstanceOf(IllegalArgumentException.class);
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
		 * DOCME add JavaDoc for method assertNode1566942192coordinates
		 * 
		 * @param actual
		 * @since 0.1.0
		 */
		protected void assertNode1566942192coordinates(Kml actual) {
			assertCoordinate(assertPoint(actual).extracting(Point::getCoordinates)
					.asInstanceOf(InstanceOfAssertFactories.LIST).singleElement(), 52.5237871, 13.4123426);
		}

		/**
		 * DOCME add JavaDoc for method assertNode8400710442coordinates
		 * 
		 * @param actual
		 * @since 0.1.0
		 */
		protected void assertNode8400710442coordinates(Kml actual) {
			assertCoordinate(assertPoint(actual).extracting(Point::getCoordinates)
					.asInstanceOf(InstanceOfAssertFactories.LIST).singleElement(), 52.5186776, 13.4075684);
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
		 * Tries to read {@link Kml} from a file relative to the output directory.
		 * 
		 * @param outputDirectory
		 * @param file
		 * @return
		 * @throws IOException
		 * @since 0.1.0
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
		 * @param outputDirectory
		 * @return {@link Kml} or {@code null} if file not found
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
		 * DOCME add JavaDoc for method runDirectory
		 * 
		 * @param outputDirectory
		 * @throws InvalidInputFileException
		 * @throws IOException
		 * @since 0.1.0
		 */
		protected void runDirectory(File output) throws IOException, InvalidInputFileException {
			osmToKml.withOutput(output.getAbsolutePath());

			osmToKml.run();
		}

		/**
		 * DOCME add JavaDoc for method runDirectory
		 * 
		 * @param outputDirectory
		 * @param fileName
		 * @return
		 * @throws InvalidInputFileException
		 * @throws IOException
		 * @since 0.1.0
		 */
		protected Kml runDirectory(File outputDirectory, String fileName)
				throws IOException, InvalidInputFileException {
			runDirectory(outputDirectory);

			return read(outputDirectory, fileName);
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
				throw new RuntimeException("Error at 'withInputs': " + e.getMessage(), e);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error at 'withInputs': " + e.getMessage(), e);
		}
	}
}
