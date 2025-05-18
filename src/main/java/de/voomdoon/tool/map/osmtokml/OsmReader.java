package de.voomdoon.tool.map.osmtokml;

import java.io.File;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.osmosis.OsmosisReader;
import de.voomdoon.logging.LogManager;
import de.voomdoon.logging.Logger;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public class OsmReader {

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
			// TODO #8: implement process
		}
	}

	/**
	 * @since 0.1.0
	 */
	private final Logger logger = LogManager.getLogger(getClass());

	/**
	 * DOCME add JavaDoc for method read
	 * 
	 * @param input
	 * @return
	 * @since 0.1.0
	 */
	public OsmData read(String input) {
		logger.trace("read " + input);

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
}
