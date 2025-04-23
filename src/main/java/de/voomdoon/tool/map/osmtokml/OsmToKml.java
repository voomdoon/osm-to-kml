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
	private List<String> outputs;

	/**
	 * DOCME add JavaDoc for method run
	 * 
	 * @throws IOException
	 * 
	 * @since 0.1.0
	 */
	public void run() throws IOException {
		for (String output : outputs) {
			new File(output).createNewFile();
		}
	}

	/**
	 * DOCME add JavaDoc for method withOutputs
	 * 
	 * @param outputs
	 * @return
	 * @since 0.1.0
	 */
	public OsmToKml withOutputs(List<String> outputs) {
		this.outputs = outputs;

		return this;
	}
}
