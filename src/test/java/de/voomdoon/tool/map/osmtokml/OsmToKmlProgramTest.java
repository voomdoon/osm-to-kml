package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.voomdoon.logging.LogLevel;
import de.voomdoon.testing.file.TempFile;
import de.voomdoon.testing.file.TempFileExtension;
import de.voomdoon.testing.file.TempInputFile;
import de.voomdoon.testing.file.TempOutputDirectory;
import de.voomdoon.testing.file.WithTempInputFiles;
import de.voomdoon.testing.logging.tests.LoggingCheckingTestBase;
import de.voomdoon.tool.map.osmtokml.OsmToKmlProgram.OsmToKmlProgramV2Options;
import de.voomdoon.util.cli.ProgramExecutionException;
import de.voomdoon.util.cli.ProgramRunException;
import de.voomdoon.util.cli.args.exception.option.MissingCliOptionException;
import de.voomdoon.util.cli.testing.ProgramTestingUtil;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
class OsmToKmlProgramTest {

	/**
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	@ExtendWith(TempFileExtension.class)
	@WithTempInputFiles(extension = "pbf")
	class MainTest extends LoggingCheckingTestBase {

		/**
		 * @since 0.1.0
		 */
		@BeforeAll
		static void beforeAll() {
			ProgramTestingUtil.enableTestingMode();
		}

		/**
		 * @since 0.1.0
		 */
		@AfterEach
		void afterEach_removeAcceptedLogging() {
			getLogCache().removeEvents(LogLevel.WARN, Pattern.compile(".*not implemented.*"));
			getLogCache().removeEvents(LogLevel.WARN, Pattern.compile(".*running default pipeline.*"));
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_input_missing(@TempOutputDirectory File output) throws Exception {
			logTestStart();

			ProgramExecutionException actual = assertThrows(ProgramExecutionException.class, () -> OsmToKmlProgram
					.main(new String[] { "--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() }));

			assertThat(actual).hasCauseInstanceOf(MissingCliOptionException.class).cause()
					.hasMessageContaining(OsmToKmlProgramV2Options.INPUT);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_input_nonPbfIsRejected(@TempFile File input, @TempOutputDirectory File output)
				throws Exception {
			logTestStart();

			ProgramExecutionException actual = assertThrows(ProgramExecutionException.class,
					() -> OsmToKmlProgram
							.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT, input.getAbsolutePath(),
									"--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() }));

			assertThat(actual).hasCauseInstanceOf(ProgramRunException.class)//
					.cause().hasCauseInstanceOf(InvalidInputFileException.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_error_output_missing(@TempFile File input) throws Exception {
			logTestStart();

			ProgramExecutionException actual = assertThrows(ProgramExecutionException.class, () -> OsmToKmlProgram
					.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT, input.getAbsolutePath() }));

			assertThat(actual).hasCauseInstanceOf(MissingCliOptionException.class).cause()
					.hasMessageContaining(OsmToKmlProgramV2Options.OUTPUT);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_fileIsWritten(@TempInputFile File input, @TempOutputDirectory File output) throws Exception {
			logTestStart();

			OsmToKmlTest.copyResourceToInputFile("node_1566942192.osm.pbf", input);

			OsmToKmlProgram.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT, input.getAbsolutePath(),
					"--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() });

			assertThat(output).exists();
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempInputFile File input, @TempOutputDirectory File output) throws Exception {
			logTestStart();

			OsmToKmlTest.copyResourceToInputFile("node_1566942192.osm.pbf", input);

			assertDoesNotThrow(() -> OsmToKmlProgram.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT,
					input.getAbsolutePath(), "--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() }));
		}
	}
}
