package de.voomdoon.tool.map.osmtokml;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.voomdoon.testing.tests.TestBase;
import de.voomdoon.util.commons.SystemOutput;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
class OsmToKmlMainTest {

	/**
	 * DOCME add JavaDoc for OsmToolMainTest
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	@Nested
	class MainTest extends TestBase {

		/**
		 * DOCME add JavaDoc for method test
		 * 
		 * @since 0.1.0
		 */
		@Test
		void test_help_cotainsName() throws Exception {
			logTestStart();

			SystemOutput output = SystemOutput.run(() -> OsmToKmlMain.main(new String[] { "--help" }));

			assertThat(output).extracting(SystemOutput::getOut).asString().containsSubsequence("OSM2KML");
		}
	}
}
