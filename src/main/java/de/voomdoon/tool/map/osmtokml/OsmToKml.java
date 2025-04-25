package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class OsmToKml {

	/**
	 * @since 0.1.0
	 */
	private List<String> inputs;

	/**
	 * @since 0.1.0
	 */
	private List<String> outputs;

	/**
	 * DOCME add JavaDoc for method run
	 * 
	 * @throws IOException
	 * @throws InvalidInputFileException
	 * 
	 * @since 0.1.0
	 */
	public void run() throws IOException, InvalidInputFileException {
		validate();

		for (String output : outputs) {
			new File(output).createNewFile();
		}
	}

	/**
	 * DOCME add JavaDoc for method withInputs
	 * 
	 * @param inputs
	 * @return
	 * @throws InvalidInputFileException
	 * @since 0.1.0
	 */
	public OsmToKml withInputs(List<String> inputs) throws InvalidInputFileException {
		if (inputs.isEmpty()) {
			throw new IllegalArgumentException("Argument 'inputs' must not be empty");
		}

		for (String input : inputs) {
			if (!input.endsWith(".pbf")) {
				throw new InvalidInputFileException("Expecting PBF input file, but got: " + input);
			}
		}

		this.inputs = inputs;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method withOutputs
	 * 
	 * @param outputs
	 * @return
	 * @since 0.1.0
	 */
	public OsmToKml withOutputs(List<String> outputs) {
		if (outputs.isEmpty()) {
			throw new IllegalArgumentException("Argument 'outputs' must not be empty");
		}

		this.outputs = outputs;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method validate
	 * 
	 * @since 0.1.0
	 */
	private void validate() {
		if (inputs == null) {
			throw new IllegalStateException("No input files specified");
		}
	}
}
