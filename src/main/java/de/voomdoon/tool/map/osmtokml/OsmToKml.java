package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.voomdoon.logging.LogManager;
import de.voomdoon.logging.Logger;
import de.voomdoon.util.kml.io.KmlWriter;

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
	private final Logger logger = LogManager.getLogger(getClass());

	/**
	 * @since 0.1.0
	 */
	private String output;

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

		List<OsmData> osmDatas = new ArrayList<>();

		for (String input : inputs) {
			OsmData osmData = new OsmReader().read(input);
			osmDatas.add(osmData);
		}

		Kml kml = new Kml();
		Document document = new Document();
		kml.setFeature(document);

		OsmData osmData = new OsmData() {

			@Override
			public Map<Long, Node> getNodes() {
				Map<Long, Node> result = new HashMap<>();

				osmDatas.forEach(data -> {
					data.getNodes().forEach((key, value) -> {
						result.put(key, value);
					});
				});

				return result;
			}
		};

		new OsmConverter(document).convert(osmData);

		String outputFile = output + "/default.kml";
		logger.debug("Writing KML file: " + outputFile);
		new File(output).mkdirs();
		new KmlWriter().write(kml, outputFile);
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
	 * @param output
	 * @return
	 * @since 0.1.0
	 */
	public OsmToKml withOutput(String output) {
		this.output = output;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method validate
	 * 
	 * @since 0.1.0
	 */
	private void validate() {
		if (inputs == null) {
			throw new IllegalStateException("No input files specified!");
		} else if (output == null) {
			throw new IllegalStateException("No output files specified!");
		}
	}
}
