package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.voomdoon.testing.file.TempFile;
import de.voomdoon.testing.file.TempFileExtension;
import de.voomdoon.testing.file.TempInputFile;
import de.voomdoon.testing.file.TempOutputFile;
import de.voomdoon.testing.file.WithTempInputFiles;
import de.voomdoon.testing.file.WithTempOutputFiles;
import de.voomdoon.testing.logging.tests.LoggingCheckingTestBase;
import de.voomdoon.tool.map.osmtokml.OsmToKmlProgram.OsmToKmlProgramV2Options;
import de.voomdoon.util.cli.ProgramExecutionException;
import de.voomdoon.util.cli.ProgramRunException;
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
	@WithTempOutputFiles(extension = "kml")
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
		@Test
		void test_error_input_nonPbfIsRejected(@TempFile File input) throws Exception {
			logTestStart();

			ProgramExecutionException actual = assertThrows(ProgramExecutionException.class, () -> OsmToKmlProgram
					.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT, input.getAbsolutePath() }));

			assertThat(actual).hasCauseInstanceOf(ProgramRunException.class)//
					.cause().hasCauseInstanceOf(InvalidInputFileException.class);
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_output_fileIsWritten(@TempInputFile File input, @TempOutputFile File output) throws Exception {
			logTestStart();

			OsmToKmlProgram.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT, input.getAbsolutePath(),
					"--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() });

			assertThat(output).exists();
		}

		/**
		 * @since 0.1.0
		 */
		@Test
		void test_success(@TempInputFile File input, @TempOutputFile File output) throws Exception {
			logTestStart();

			assertDoesNotThrow(() -> OsmToKmlProgram.main(new String[] { "--" + OsmToKmlProgramV2Options.INPUT,
					input.getAbsolutePath(), "--" + OsmToKmlProgramV2Options.OUTPUT, output.getAbsolutePath() }));
		}
	}
}
