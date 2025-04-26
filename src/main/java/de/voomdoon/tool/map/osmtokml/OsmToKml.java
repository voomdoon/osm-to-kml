package de.voomdoon.tool.map.osmtokml;

import java.io.IOException;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
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

		for (String input : inputs) {
			OsmData osmData = new OsmReader().read(input);

			for (String output : outputs) {
				Kml kml = new Kml();
				Document document = new Document();
				Placemark placemark = new Placemark();
				Point point = new Point();
				Node node = osmData.getNodes().entrySet().iterator().next().getValue();
				point.addToCoordinates(node.getLongitude(), node.getLatitude());
				placemark.setGeometry(point);
				document.addToFeature(placemark);
				kml.setFeature(document);

				new KmlWriter().write(kml, output);
			}
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
			throw new IllegalStateException("No input files specified!");
		} else if (outputs == null) {
			throw new IllegalStateException("No output files specified!");
		}
	}
}
