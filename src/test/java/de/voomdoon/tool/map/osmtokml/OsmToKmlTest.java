package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.voomdoon.testing.file.TempFileExtension;
import de.voomdoon.testing.file.TempInputFile;
import de.voomdoon.testing.file.TempOutputFile;
import de.voomdoon.testing.file.WithTempInputFiles;
import de.voomdoon.testing.file.WithTempOutputFiles;
import de.voomdoon.testing.logging.tests.LoggingCheckingTestBase;
import de.voomdoon.testing.tests.TestBase;
import de.voomdoon.util.kml.io.KmlReader;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
class OsmToKmlTest extends LoggingCheckingTestBase {

	/**
	 * Tests for {@link OsmToKml}.
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	@WithTempInputFiles(extension = "pbf")
	@WithTempOutputFiles(extension = "kml")
	class RunTest extends TestBase {

		/**
		 * @since 0.1.0
		 */
		private OsmToKml osmToKml = new OsmToKml();

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
		void test_output_DocumentContainsPlacemark(@TempInputFile File input, @TempOutputFile File output)
				throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assumeThat(actual).isNotNull();
			Document actualDocument = (Document) assumeThat(actual.getFeature()).describedAs("root feature")
					.isInstanceOf(Document.class).actual();

			assertThat(actualDocument.getFeature()).singleElement().isInstanceOf(Placemark.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_rootFeatureIsDocument(@TempInputFile File input, @TempOutputFile File output)
				throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assumeThat(actual).isNotNull();

			assertThat(actual.getFeature()).describedAs("root feature").isInstanceOf(Document.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempInputFile File input, @TempOutputFile File output) throws Exception {
			logTestStart();

			withInputs(input, "node_1566942192.osm.pbf");

			Kml actual = run(output);

			assertThat(actual).isNotNull();
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
		private Kml run(File output) throws IOException, InvalidInputFileException {
			osmToKml.withOutputs(List.of(output.getAbsolutePath()));

			osmToKml.run();

			logger.debug("output:\n" + Files.readString(output.toPath()));

			return new KmlReader().read(output.getAbsolutePath());
		}

		/**
		 * DOCME add JavaDoc for method withInputs
		 * 
		 * @param input
		 * @param resource
		 * @throws InvalidInputFileException
		 * @since 0.1.0
		 */
		private void withInputs(File input, String resource) throws InvalidInputFileException {
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

			osmToKml.withInputs(List.of(input.getAbsolutePath()));
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
		private OsmToKml osmToKml = new OsmToKml();

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
	@WithTempOutputFiles(extension = "kml")
	class WithOutputsTets extends TestBase {

		/**
		 * @since 0.1.0
		 */
		private OsmToKml osmToKml = new OsmToKml();

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_IAE_empty(@TempOutputFile String input) throws Exception {
			logTestStart();

			assertThatThrownBy(() -> osmToKml.withOutputs(List.of())).isInstanceOf(IllegalArgumentException.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempOutputFile String input) throws Exception {
			logTestStart();

			assertDoesNotThrow(() -> osmToKml.withOutputs(List.of(input)));
		}
	}
}
