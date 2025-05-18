package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * DOCME add JavaDoc for OsmToKml
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	private class InputCollector {

		/**
		 * DOCME add JavaDoc for method getAggregatedinputData
		 * 
		 * @return
		 * @since 0.1.0
		 */
		private List<InputData> getAggregatedInputData() {
			List<OsmData> osmDatas = new ArrayList<>();

			for (String input : inputs) {
				OsmData osmData = read(input);
				osmDatas.add(osmData);
			}

			OsmData joinedData = joinOsmDatas(osmDatas);

			return List.of(new InputData(joinedData));
		}

		/**
		 * DOCME add JavaDoc for method getInputDatas
		 * 
		 * @return
		 * @since 0.1.0
		 */
		private List<InputData> getInputDatas() {
			if (inputs.size() > 1) {
				return getAggregatedInputData();
			}

			// TODO #3: implement getInputDatas
			return List.of();
		}
	}

	/**
	 * DOCME add JavaDoc for OsmToKml
	 *
	 * @author André Schulz
	 *
	 * @since 0.1.0
	 */
	private record InputData(OsmData data) {

	}

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
	 * TODO #3: make mandatory and remove default
	 * 
	 * @since 0.1.0
	 */
	private List<OsmToKmlPipeline> pipelines = List.of(new OsmToKmlPipeline().setName("default"));

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

		List<InputData> inputDatas = new InputCollector().getInputDatas();
		logger.debug("inputDatas:" + inputDatas.stream().map(d -> "\n• " + d).collect(Collectors.joining("")));

		if (!inputDatas.isEmpty()) {
			runNew(inputDatas);
		} else {
			runOld();
		}
	}

	/**
	 * DOCME add JavaDoc for method withInputs
	 * 
	 * @param inputs
	 * @return {@link OsmToKml}
	 * @throws InvalidInputFileException
	 * @since 0.1.0
	 */
	public OsmToKml withInputs(List<String> inputs) throws InvalidInputFileException {
		logger.trace("withInputs " + inputs);

		if (inputs.isEmpty()) {
			throw new IllegalArgumentException("Argument 'inputs' must not be empty");
		}

		for (String input : inputs) {
			if (!input.endsWith(".pbf")) {
				File file = new File(input);

				if (!file.isDirectory()) {
					throw new InvalidInputFileException("Expecting PBF input file, but got: " + input);
				}
			}
		}

		this.inputs = inputs;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method withOutputs
	 * 
	 * @param output
	 * @return {@link OsmToKml}
	 * @since 0.1.0
	 */
	public OsmToKml withOutput(String output) {
		logger.trace("withOutput " + output);

		this.output = output;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method withPipelines
	 * 
	 * @param pipelines
	 * @return {@link OsmToKml}
	 * @since 0.1.0
	 */
	public OsmToKml withPipelines(List<OsmToKmlPipeline> pipelines) {
		logger.trace("withPipelines " + pipelines.stream().map(OsmToKmlPipeline::getName).toList());

		this.pipelines = pipelines;

		return this;
	}

	/**
	 * DOCME add JavaDoc for method getOutputFile
	 * 
	 * @param input
	 * @param pipeline
	 * @return
	 * @since 0.1.0
	 */
	private String getOutputFile(String input, OsmToKmlPipeline pipeline) {
		logger.debug("getOutputFile " + input + " " + pipeline.getName());

		if (inputs.size() > 1) {
			String name = new File(input).getName();
			name = name.substring(0, name.length() - 8);

			return output + "/" + name + ".kml";
		}

		return output + "/" + pipeline.getName() + ".kml";
	}

	/**
	 * DOCME add JavaDoc for method joinOsmDatas
	 * 
	 * @param osmDatas
	 * @return
	 * @since 0.1.0
	 */
	private OsmData joinOsmDatas(List<OsmData> osmDatas) {
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

		return osmData;
	}

	/**
	 * DOCME add JavaDoc for method read
	 * 
	 * @param input
	 * 
	 * @return
	 * @since 0.1.0
	 */
	private OsmData read(String input) {
		List<OsmData> osmDatas = new ArrayList<>();
		File file = new File(input);

		if (file.isFile()) {
			OsmData osmData = new OsmReader().read(input);
			osmDatas.add(osmData);
		} else {
			for (File f : file.listFiles()) {
				OsmData osmData = new OsmReader().read(f.getAbsolutePath());
				osmDatas.add(osmData);
			}
		}

		return joinOsmDatas(osmDatas);
	}

	/**
	 * DOCME add JavaDoc for method runNew
	 * 
	 * @param inputDatas
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runNew(List<InputData> inputDatas) throws IOException {
		OsmToKmlPipeline pipeline = pipelines.get(0);

		if (pipeline.getName().equals("default")) {
			logger.warn("running default pipeline");
		}

		Kml kml = new Kml();
		Document document = new Document();
		kml.setFeature(document);

		new OsmToKmlConverter(document).convert(inputDatas.get(0).data());

		File outputFile = new File(output + "/" + pipeline.getName() + ".kml");
		logger.debug("Writing KML file: " + outputFile);
		outputFile.getParentFile().mkdirs();
		new KmlWriter().write(kml, outputFile.getAbsolutePath());
	}

	/**
	 * DOCME add JavaDoc for method runOld
	 * 
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runOld() throws IOException {
		for (String input : inputs) {
			for (OsmToKmlPipeline pipeline : pipelines) {
				if (pipeline.getName().equals("default")) {
					logger.warn("running default pipeline");
				}

				OsmData osmData = read(input);

				Kml kml = new Kml();
				Document document = new Document();
				kml.setFeature(document);

				new OsmToKmlConverter(document).convert(osmData);

				File outputFile = new File(getOutputFile(input, pipeline));
				logger.debug("Writing KML file: " + outputFile);
				outputFile.getParentFile().mkdirs();
				new KmlWriter().write(kml, outputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * @since 0.1.0
	 */
	private void validate() {
		// TODO #11: validate pipelines are set
		if (inputs == null) {
			throw new IllegalStateException("No input files specified!");
		} else if (output == null) {
			throw new IllegalStateException("No output files specified!");
		}
	}
}
