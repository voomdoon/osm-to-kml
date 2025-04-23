package de.voomdoon.tool.map.osmtokml;

import java.util.List;

import de.voomdoon.util.cli.Program;
import de.voomdoon.util.cli.args.Option;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class OsmToKmlProgram extends Program {

	/**
	 * DOCME add JavaDoc for OsmToKmlProgramV2
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	class OsmToKmlProgramV2Options {

		/**
		 * @since 0.1.0
		 */
		public static final String INPUT = "input";

		/**
		 * @since 0.1.0
		 */
		public static final String OUTPUT = "output";

		/**
		 * @since 0.1.0
		 */
		private Option output;

		/**
		 * DOCME add JavaDoc for method init
		 * 
		 * @since 0.1.0
		 */
		public void init() {
			output = addOption().longName(OUTPUT).hasValue("file").build();
		}
	}

	/**
	 * DOCME add JavaDoc for method main
	 * 
	 * @param args
	 * @since 0.1.0
	 */
	public static void main(String[] args) {
		Program.run(args);
	}

	/**
	 * @since 0.1.0
	 */
	private OsmToKmlProgramV2Options options = new OsmToKmlProgramV2Options();

	/**
	 * @since 0.1.0
	 */
	@Override
	protected void initOptions() {
		options.init();
	}

	/**
	 * @since 0.1.0
	 */
	@Override
	protected void run() throws Exception {
		List<String> outputs = getArguments().getOptionValue(options.output).map(output -> List.of(output))
				.orElse(List.of());

		new OsmToKml().withOutputs(outputs).run();

		// TODO implement run
	}
}
