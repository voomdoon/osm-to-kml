package de.voomdoon.tool.map.osmtokml;

import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

/**
 * DOCME add JavaDoc for
 *
 * @author André Schulz
 *
 * @since 0.1.0
 */
public interface OsmData {

	/**
	 * @return {@link Map} of {@link Node} by ID.
	 * @since 0.1.0
	 */
	Map<Long, Node> getNodes();
}
