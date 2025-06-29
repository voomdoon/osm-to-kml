package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Converts OpenStreetMap {@code PBF} to KML {@code XML}.
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
		private List<InputData> getAggregatedInputDataOrEmpty() {
			if (inputs.size() > 1) {
				return getAggregatedInputData();
			}

			// TODO #3: implement getInputDatas
			return List.of();
		}
	}

	/**
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
	 * @throws IOException
	 * @throws InvalidInputFileException
	 * 
	 * @since 0.1.0
	 */
	public void run() throws IOException, InvalidInputFileException {
		validate();

		List<InputData> aggregatedInputDatasOrEmpty = new InputCollector().getAggregatedInputDataOrEmpty();
		logger.debug("inputDatas:"
				+ aggregatedInputDatasOrEmpty.stream().map(d -> "\n• " + d).collect(Collectors.joining("")));

		if (!aggregatedInputDatasOrEmpty.isEmpty()) {
			runAggregatedInputs(aggregatedInputDatasOrEmpty);
		} else if (pipelines.size() > 1) {
			if (inputs.size() == 1 && new File(inputs.get(0)).isFile()) {
				runMultiplePipelinesForSingleInputFile();
			} else if (inputs.size() == 1 && new File(inputs.get(0)).isDirectory()) {
				runMultiplePipelinesForSingleInputDirectory();
			} else {
				// TODO implement run
				throw new UnsupportedOperationException("Method 'run' not implemented yet");
			}
		} else {
			runOld();
		}
	}

	/**
	 * @param inputs
	 *            {@link List} of input file names or directories
	 * @return this {@link OsmToKml}
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

		if (inputs.stream().map(File::new).filter(File::isDirectory).count() > 1) {
			throw new IllegalArgumentException("Multiple input directories are not supported!");
		}

		this.inputs = inputs;

		return this;
	}

	/**
	 * @param output
	 *            output directory for KML files
	 * @return this {@link OsmToKml}
	 * @since 0.1.0
	 */
	public OsmToKml withOutput(String output) {
		logger.trace("withOutput " + output);

		this.output = output;

		return this;
	}

	/**
	 * @param pipelines
	 *            {@link List} of {@link OsmToKmlPipeline} to use for processing
	 * @return this {@link OsmToKml}
	 * @since 0.1.0
	 */
	public OsmToKml withPipelines(List<OsmToKmlPipeline> pipelines) {
		logger.trace("withPipelines " + pipelines.stream().map(OsmToKmlPipeline::getName).toList());

		this.pipelines = pipelines;

		return this;
	}

	/**
	 * @param fileName
	 *            input file name {@link String}
	 * @return the name of the input file without the extension
	 * @since 0.1.0
	 */
	private String getInputName(String fileName) {
		String name = new File(fileName).getName();
		name = name.substring(0, name.length() - 8);

		return name;
	}

	/**
	 * @param input
	 *            input file name {@link String}
	 * @param pipeline
	 *            {@link OsmToKmlPipeline}
	 * @return output file name for the given input and pipeline
	 * @since 0.1.0
	 */
	private String getOutputFile(String input, OsmToKmlPipeline pipeline) {
		logger.debug("getOutputFile " + input + " " + pipeline.getName());

		if (inputs.size() > 1) {
			String name = getInputName(input);

			return output + "/" + name + ".kml";// TESTME
		}

		return output + "/" + pipeline.getName() + ".kml";
	}

	/**
	 * @param input
	 *            input file name {@link String}
	 * @param pipeline
	 *            {@link OsmToKmlPipeline}
	 * @return output file name for the given input and pipeline
	 * @since 0.1.0
	 */
	private String getOutputFile2(String input, OsmToKmlPipeline pipeline) {
		// TODO unify code
		logger.debug("getOutputFile2 " + input + " " + pipeline.getName());

		return output + "/" + pipeline.getName() + "@" + getInputName(input) + ".kml";
	}

	/**
	 * @param input
	 *            input file name {@link String}
	 * @param pipeline
	 *            {@link OsmToKmlPipeline}
	 * @return output file name for the given input and pipeline
	 * @since 0.1.0
	 */
	private String getOutputFileForMultiplePipelinesAndSingleInputDirectory(String input, OsmToKmlPipeline pipeline) {
		return output + "/" + pipeline.getName() + "/" + getInputName(input) + ".kml";
	}

	/**
	 * @param input
	 *            input file name {@link String}
	 * @param pipeline
	 *            {@link OsmToKmlPipeline}
	 * @return output file name for the given input and pipeline
	 * @since 0.1.0
	 */
	private String getOutputFileForMultiplePipelinesAndSingleInputFile(String input, OsmToKmlPipeline pipeline) {
		return output + "/" + pipeline.getName() + "@" + getInputName(input) + ".kml";
	}

	/**
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
	 * @param input
	 * 
	 * @return {@link OsmData}
	 * @since 0.1.0
	 */
	private OsmData read(String input) {
		List<OsmData> osmDatas = new ArrayList<>();
		File file = new File(input);

		if (file.isFile()) {
			OsmData osmData = new OsmReader().read(input);
			osmDatas.add(osmData);
		} else {
			// TESTME
			for (File f : file.listFiles()) {
				OsmData osmData = new OsmReader().read(f.getAbsolutePath());
				osmDatas.add(osmData);
			}
		}

		return joinOsmDatas(osmDatas);
	}

	/**
	 * @param inputDatas
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runAggregatedInputs(List<InputData> inputDatas) throws IOException {
		logger.debug("runAggregatedInputs");

		for (OsmToKmlPipeline pipeline : pipelines) {
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
	}

	/**
	 * @param directory
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runDirectory(String directory) throws IOException {
		for (File file : new File(directory).listFiles()) {
			runFile(file);
		}
	}

	/**
	 * DOCME add JavaDoc for method runFile
	 * 
	 * @param file
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runFile(File file) throws IOException {
		for (OsmToKmlPipeline pipeline : pipelines) {
			if (pipeline.getName().equals("default")) {
				logger.warn("running default pipeline");
			}

			OsmData osmData = read(file.toString());

			Kml kml = new Kml();
			Document document = new Document();
			kml.setFeature(document);

			new OsmToKmlConverter(document).convert(osmData);

			File outputFile = new File(getOutputFile2(file.toString(), pipeline));
			logger.debug("Writing KML file: " + outputFile);
			outputFile.getParentFile().mkdirs();
			new KmlWriter().write(kml, outputFile.getAbsolutePath());
		}
	}

	/**
	 * @throws IOException
	 * 
	 * @since 0.1.0
	 */
	private void runMultiplePipelinesForSingleInputDirectory() throws IOException {
		logger.debug("runMultiplePipelinesForSingleInputDirectory");

		List<String> files = Arrays.asList(new File(inputs.get(0)).listFiles()).stream().map(File::getAbsolutePath)
				.toList();

		for (String input : files) {
			for (OsmToKmlPipeline pipeline : pipelines) {
				if (pipeline.getName().equals("default")) {
					logger.warn("running default pipeline");
				}

				OsmData osmData = read(input);

				Kml kml = new Kml();
				Document document = new Document();
				kml.setFeature(document);

				new OsmToKmlConverter(document).convert(osmData);

				File outputFile = new File(getOutputFileForMultiplePipelinesAndSingleInputDirectory(input, pipeline));
				logger.debug("Writing KML file: " + outputFile);
				outputFile.getParentFile().mkdirs();
				new KmlWriter().write(kml, outputFile.getAbsolutePath());
			}
		}
	}

	/**
	 * @throws IOException
	 * 
	 * @since 0.1.0
	 */
	private void runMultiplePipelinesForSingleInputFile() throws IOException {
		logger.debug("runMultiplePipelinesForSingleInput");

		String input = inputs.get(0);

		for (OsmToKmlPipeline pipeline : pipelines) {
			if (pipeline.getName().equals("default")) {
				logger.warn("running default pipeline");
			}

			OsmData osmData = read(input);

			Kml kml = new Kml();
			Document document = new Document();
			kml.setFeature(document);

			new OsmToKmlConverter(document).convert(osmData);

			File outputFile = new File(getOutputFileForMultiplePipelinesAndSingleInputFile(input, pipeline));
			logger.debug("Writing KML file: " + outputFile);
			outputFile.getParentFile().mkdirs();
			new KmlWriter().write(kml, outputFile.getAbsolutePath());
		}
	}

	/**
	 * @throws IOException
	 * @since 0.1.0
	 */
	private void runOld() throws IOException {
		logger.debug("runOld");

		for (String input : inputs) {
			if (new File(input).isDirectory()) {
				runDirectory(input);
			} else {
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
