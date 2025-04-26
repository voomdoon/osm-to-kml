package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.osmosis.OsmosisReader;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
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
	 * DOCME add JavaDoc for OsmToKml
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	private class EntitySink implements Sink {

		/**
		 * @since 0.1.0
		 */
		private final Logger logger = LogManager.getLogger(getClass());

		/**
		 * @since 0.1.0
		 */
		private Node node;

		/**
		 * @since 0.1.0
		 */
		@Override
		public void close() {
			// nothing to do
		}

		/**
		 * @since 0.1.0
		 */
		@Override
		public void complete() {
			// nothing to do
		}

		/**
		 * @since 0.1.0
		 */
		@Override
		public void initialize(Map<String, Object> metaData) {
			// nothing to do
		}

		/**
		 * @since 0.1.0
		 */
		@Override
		public void process(EntityContainer entityContainer) {
			logger.debug("process: " + entityContainer.getEntity());

			if (entityContainer.getEntity() instanceof Node) {
				node = (Node) entityContainer.getEntity();
			}

			logger.warn("process not implemented for " + entityContainer.getEntity());
			// TODO implement process
		}
	}

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
			OsmData osmData = getOsmData(input);

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
	 * DOCME add JavaDoc for method getOsmData
	 * 
	 * @param input
	 * @return
	 * @since 0.1.0
	 */
	private OsmData getOsmData(String input) {
		File file = new File(input);
		OsmosisReader reader = new OsmosisReader(file);
		EntitySink sink = new EntitySink();
		reader.setSink(sink);

		reader.run();

		return new OsmData() {

			@Override
			public Map<Long, Node> getNodes() {
				return Map.of(sink.node.getId(), sink.node);
			}
		};
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
