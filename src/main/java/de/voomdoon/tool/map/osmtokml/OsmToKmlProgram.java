package de.voomdoon.tool.map.osmtokml;

import java.util.List;

import de.voomdoon.util.cli.Program;
import de.voomdoon.util.cli.ProgramRunException;
import de.voomdoon.util.cli.args.Option;
import de.voomdoon.util.cli.args.exception.option.MissingCliOptionException;

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
		List<String> inputs = getArguments().getOptionValue(options.input).map(output -> List.of(output))
				.orElseThrow(() -> new MissingCliOptionException(options.input));
		String output = getArguments().getOptionValue(options.output)
				.orElseThrow(() -> new MissingCliOptionException(options.output));

		OsmToKml osmToKml = new OsmToKml().withInputs(inputs).withOutput(output);

		try {
			osmToKml.run();
		} catch (InvalidInputFileException e) {
			throw new ProgramRunException(e);
		}
	}
}
