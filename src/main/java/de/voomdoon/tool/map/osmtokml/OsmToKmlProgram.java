package de.voomdoon.tool.map.osmtokml;

import java.util.List;

import de.voomdoon.util.cli.Program;
import de.voomdoon.util.cli.args.InvalidProgramOptionException;
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
		private Option input;

		/**
		 * @since 0.1.0
		 */
		private Option output;

		/**
		 * @since 0.1.0
		 */
		public void init() {
			input = addOption().longName(INPUT).hasValue("file").build();
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
	 * @param args
	 * @since 0.1.0
	 */
	public static void runWithoutExit(String[] args) {
		Program.runWithoutExit(args);
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
		List<String> inputs = getArguments().getOptionValue(options.input).map(output -> List.of(output))
				.orElse(List.of());
		List<String> outputs = getArguments().getOptionValue(options.output).map(output -> List.of(output))
				.orElse(List.of());

		OsmToKml osmToKml = new OsmToKml().withInputs(inputs).withOutputs(outputs);

		try {
			osmToKml.run();
		} catch (InvalidInputFileException e) {
			throw new InvalidProgramOptionException(options.input, e.getMessage());
		}

		// TODO implement run
	}
}
